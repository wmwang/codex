from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Iterable


VBP_REFERENCE_PREFIXES = ("Form=", "Module=", "Class=", "UserControl=", "Designer=")


def _iter_vbp_paths(root: Path) -> Iterable[Path]:
    return root.rglob("*.vbp")


def get_all_projects(root_dir: str = ".") -> list[dict[str, str]]:
    """List all .vbp projects under root_dir."""
    root = Path(root_dir)
    projects = []
    for path in sorted(_iter_vbp_paths(root)):
        projects.append({"name": path.stem, "path": str(path)})
    return projects


def get_project_file_list(vbp_path: str) -> list[str]:
    """Return all .frm/.bas/.cls files referenced by a .vbp."""
    vbp = Path(vbp_path)
    if not vbp.exists():
        raise FileNotFoundError(f"VBP not found: {vbp_path}")
    files: list[str] = []
    for line in vbp.read_text(encoding="utf-8", errors="ignore").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("'"):
            continue
        if stripped.startswith(VBP_REFERENCE_PREFIXES):
            _, value = stripped.split("=", 1)
            if ";" in value:
                _, file_ref = value.split(";", 1)
            else:
                file_ref = value
            file_ref = file_ref.strip()
            if file_ref:
                files.append(str((vbp.parent / file_ref).resolve()))
    return files


def _collect_comment_block(lines: list[str], start_index: int) -> str:
    comments: list[str] = []
    index = start_index
    while index >= 0:
        raw = lines[index].strip()
        if raw.startswith("'"):
            comments.append(raw.lstrip("'").strip())
            index -= 1
            continue
        if raw.lower().startswith("rem "):
            comments.append(raw[4:].strip())
            index -= 1
            continue
        if raw == "":
            index -= 1
            continue
        break
    comments.reverse()
    return "\n".join(comments).strip()


def get_file_structure(file_path: str) -> list[dict[str, str]]:
    """Return list of VB Sub/Function/Property names with comments (no code)."""
    path = Path(file_path)
    if not path.exists():
        raise FileNotFoundError(f"File not found: {file_path}")
    lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    results: list[dict[str, str]] = []
    pattern = re.compile(
        r"^\s*(?:(?:Public|Private|Friend|Static)\s+)?"
        r"(Sub|Function|Property\s+(?:Get|Let|Set))\s+"
        r"([A-Za-z_][A-Za-z0-9_]*)",
        re.IGNORECASE,
    )
    for idx, line in enumerate(lines):
        match = pattern.match(line)
        if not match:
            continue
        kind = match.group(1)
        name = match.group(2)
        comment = _collect_comment_block(lines, idx - 1)
        entry = {"name": name, "kind": kind, "comment": comment}
        results.append(entry)
    return results


def read_function_code(file_path: str, func_name: str) -> str:
    """Return the full code for a specific Sub/Function/Property by name."""
    path = Path(file_path)
    if not path.exists():
        raise FileNotFoundError(f"File not found: {file_path}")
    lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    start_pattern = re.compile(
        rf"^\s*(?:(?:Public|Private|Friend|Static)\s+)?"
        rf"(Sub|Function|Property\s+(?:Get|Let|Set))\s+{re.escape(func_name)}\b",
        re.IGNORECASE,
    )
    end_pattern = re.compile(r"^\s*End\s+(Sub|Function|Property)\b", re.IGNORECASE)
    capturing = False
    captured: list[str] = []
    for line in lines:
        if not capturing and start_pattern.match(line):
            capturing = True
        if capturing:
            captured.append(line)
            if end_pattern.match(line):
                break
    if not captured:
        raise ValueError(f"Function not found: {func_name}")
    return "\n".join(captured)


def search_codebase(keyword: str, root_dir: str = ".") -> list[dict[str, str | int]]:
    """Search keyword in codebase and return matches with file and line info."""
    root = Path(root_dir)
    results: list[dict[str, str | int]] = []
    for path in root.rglob("*"):
        if not path.is_file():
            continue
        if path.suffix.lower() not in {".bas", ".cls", ".frm", ".vbp"}:
            continue
        try:
            lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError:
            continue
        for idx, line in enumerate(lines, start=1):
            if keyword in line:
                results.append(
                    {"file": str(path), "line": idx, "text": line.strip()}
                )
    return results


def build_project_structure(root_dir: str = ".", output_path: str = "project_structure.json") -> dict:
    """Build a project structure map and write it to JSON."""
    root = Path(root_dir)
    projects = get_all_projects(root_dir)
    structure: dict[str, dict[str, list[str]]] = {}
    file_index: dict[str, list[str]] = {}
    for project in projects:
        vbp_path = project["path"]
        files = get_project_file_list(vbp_path)
        structure[project["name"]] = {"vbp": [vbp_path], "files": files}
        for file_path in files:
            file_index.setdefault(file_path, []).append(project["name"])
    payload = {"root": str(root.resolve()), "projects": structure, "file_index": file_index}
    Path(output_path).write_text(
        json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    return payload


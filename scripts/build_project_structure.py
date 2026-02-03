from __future__ import annotations

import argparse

from wiki_tools import build_project_structure


def main() -> None:
    parser = argparse.ArgumentParser(description="Build VB project structure map.")
    parser.add_argument("--root", default=".", help="Root directory to scan.")
    parser.add_argument(
        "--output",
        default="project_structure.json",
        help="Output JSON path.",
    )
    args = parser.parse_args()
    build_project_structure(args.root, args.output)


if __name__ == "__main__":
    main()

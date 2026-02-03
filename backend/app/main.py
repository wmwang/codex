from __future__ import annotations

import os
from pathlib import Path
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from wiki_tools import (
    build_project_structure,
    get_all_projects,
    get_file_structure,
    get_project_file_list,
    read_function_code,
    search_codebase,
)


class ProjectRequest(BaseModel):
    vbp_path: str


class FileStructureRequest(BaseModel):
    file_path: str


class FunctionRequest(BaseModel):
    file_path: str
    func_name: str


class SearchRequest(BaseModel):
    keyword: str
    root_dir: str | None = None


class BuildRequest(BaseModel):
    root_dir: str = "."
    output_path: str = "project_structure.json"


class GenerateRequest(BaseModel):
    prompt: str


app = FastAPI(title="VB Wiki Backend", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/api/projects")
def list_projects(root_dir: str = ".") -> list[dict[str, str]]:
    return get_all_projects(root_dir)


@app.post("/api/projects/files")
def list_project_files(request: ProjectRequest) -> list[str]:
    return get_project_file_list(request.vbp_path)


@app.post("/api/files/structure")
def file_structure(request: FileStructureRequest) -> list[dict[str, str]]:
    return get_file_structure(request.file_path)


@app.post("/api/files/function")
def function_code(request: FunctionRequest) -> dict[str, str]:
    return {"code": read_function_code(request.file_path, request.func_name)}


@app.post("/api/search")
def search(request: SearchRequest) -> list[dict[str, Any]]:
    root_dir = request.root_dir or "."
    return search_codebase(request.keyword, root_dir)


@app.post("/api/projects/build")
def build_structure(request: BuildRequest) -> dict[str, Any]:
    return build_project_structure(request.root_dir, request.output_path)


@app.post("/api/wiki/generate")
def generate_wiki(request: GenerateRequest) -> JSONResponse:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=400, detail="OPENAI_API_KEY not set.")
    import openai

    client = openai.OpenAI(api_key=api_key)
    response = client.responses.create(
        model=os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
        input=request.prompt,
    )
    content = response.output_text
    return JSONResponse({"content": content})


@app.get("/api/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/api/config")
def config() -> dict[str, str]:
    root = Path(".").resolve()
    return {"root_dir": str(root)}

from __future__ import annotations

import asyncio
import uuid
from dataclasses import dataclass, field
from typing import Dict, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel


class JobCreateResponse(BaseModel):
    job_id: str


class JobStatusResponse(BaseModel):
    job_id: str
    status: str
    progress: int
    detail: Optional[str] = None


class JobListResponse(BaseModel):
    jobs: list[JobStatusResponse]


@dataclass
class JobRecord:
    job_id: str
    status: str = "queued"
    progress: int = 0
    detail: Optional[str] = None
    task: Optional[asyncio.Task] = None
    cancel_event: asyncio.Event = field(default_factory=asyncio.Event)


app = FastAPI()
_jobs: Dict[str, JobRecord] = {}
_jobs_lock = asyncio.Lock()


async def _run_job(job: JobRecord) -> None:
    job.status = "running"
    try:
        for step in range(1, 11):
            if job.cancel_event.is_set():
                job.status = "canceled"
                job.detail = "Canceled by user"
                return
            await asyncio.sleep(0.5)
            job.progress = step * 10
        job.status = "completed"
        job.detail = "Job finished"
    except Exception as exc:  # noqa: BLE001
        job.status = "failed"
        job.detail = f"Job failed: {exc}"


@app.post("/api/wiki/jobs", response_model=JobCreateResponse)
async def create_job() -> JobCreateResponse:
    job_id = str(uuid.uuid4())
    job = JobRecord(job_id=job_id)
    async with _jobs_lock:
        _jobs[job_id] = job
        job.task = asyncio.create_task(_run_job(job))
    return JobCreateResponse(job_id=job_id)


@app.get("/api/wiki/jobs", response_model=JobListResponse)
async def list_jobs() -> JobListResponse:
    async with _jobs_lock:
        jobs = [
            JobStatusResponse(
                job_id=job.job_id,
                status=job.status,
                progress=job.progress,
                detail=job.detail,
            )
            for job in _jobs.values()
        ]
    return JobListResponse(jobs=jobs)


@app.get("/api/wiki/jobs/{job_id}", response_model=JobStatusResponse)
async def get_job(job_id: str) -> JobStatusResponse:
    async with _jobs_lock:
        job = _jobs.get(job_id)
        if not job:
            raise HTTPException(status_code=404, detail="Job not found")
        return JobStatusResponse(
            job_id=job.job_id,
            status=job.status,
            progress=job.progress,
            detail=job.detail,
        )


@app.delete("/api/wiki/jobs/{job_id}", response_model=JobStatusResponse)
async def cancel_job(job_id: str) -> JobStatusResponse:
    async with _jobs_lock:
        job = _jobs.get(job_id)
        if not job:
            raise HTTPException(status_code=404, detail="Job not found")
        if job.status in {"completed", "failed", "canceled"}:
            return JobStatusResponse(
                job_id=job.job_id,
                status=job.status,
                progress=job.progress,
                detail=job.detail,
            )
        job.cancel_event.set()
    if job.task:
        await job.task
    return JobStatusResponse(
        job_id=job.job_id,
        status=job.status,
        progress=job.progress,
        detail=job.detail,
    )

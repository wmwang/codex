const API_BASE = '/api/wiki/jobs';

const appRoot = document.getElementById('app') || document.body;

const title = document.createElement('h2');
title.textContent = 'Wiki Job Queue';

const controls = document.createElement('div');
controls.style.display = 'flex';
controls.style.gap = '8px';
controls.style.alignItems = 'center';

const createButton = document.createElement('button');
createButton.textContent = '建立任務';

const refreshButton = document.createElement('button');
refreshButton.textContent = '重新整理';

const statusNote = document.createElement('span');
statusNote.style.fontSize = '12px';
statusNote.style.color = '#555';

controls.append(createButton, refreshButton, statusNote);

const list = document.createElement('div');
list.style.display = 'grid';
list.style.gap = '12px';
list.style.marginTop = '16px';

appRoot.append(title, controls, list);

const renderJobs = (jobs) => {
  list.innerHTML = '';
  if (!jobs.length) {
    const empty = document.createElement('div');
    empty.textContent = '目前沒有任務。';
    list.append(empty);
    return;
  }

  jobs.forEach((job) => {
    const card = document.createElement('div');
    card.style.border = '1px solid #ddd';
    card.style.padding = '12px';
    card.style.borderRadius = '6px';
    card.style.background = '#fafafa';

    const header = document.createElement('div');
    header.style.display = 'flex';
    header.style.justifyContent = 'space-between';
    header.style.alignItems = 'center';

    const jobInfo = document.createElement('div');
    jobInfo.innerHTML = `<strong>${job.job_id}</strong><div>狀態：${job.status}</div>`;

    const cancelButton = document.createElement('button');
    cancelButton.textContent = '取消';
    cancelButton.disabled = ['completed', 'failed', 'canceled'].includes(job.status);
    cancelButton.addEventListener('click', async () => {
      await fetch(`${API_BASE}/${job.job_id}`, { method: 'DELETE' });
      await fetchJobs();
    });

    header.append(jobInfo, cancelButton);

    const progressWrap = document.createElement('div');
    progressWrap.style.marginTop = '8px';

    const progress = document.createElement('div');
    progress.style.height = '10px';
    progress.style.background = '#eee';
    progress.style.borderRadius = '5px';
    progress.style.overflow = 'hidden';

    const bar = document.createElement('div');
    bar.style.height = '100%';
    bar.style.width = `${job.progress}%`;
    bar.style.background = job.status === 'failed' ? '#e57373' : '#64b5f6';
    bar.style.transition = 'width 0.3s ease';

    progress.append(bar);

    const detail = document.createElement('div');
    detail.style.fontSize = '12px';
    detail.style.marginTop = '4px';
    detail.textContent = job.detail || '';

    progressWrap.append(progress, detail);

    card.append(header, progressWrap);
    list.append(card);
  });
};

const fetchJobs = async () => {
  statusNote.textContent = '更新中…';
  try {
    const response = await fetch(API_BASE);
    if (!response.ok) {
      throw new Error('Failed to load jobs');
    }
    const data = await response.json();
    renderJobs(data.jobs || []);
    statusNote.textContent = `最後更新：${new Date().toLocaleTimeString()}`;
  } catch (error) {
    statusNote.textContent = '取得資料失敗';
    console.error(error);
  }
};

createButton.addEventListener('click', async () => {
  await fetch(API_BASE, { method: 'POST' });
  await fetchJobs();
});

refreshButton.addEventListener('click', fetchJobs);

fetchJobs();
setInterval(fetchJobs, 5000);

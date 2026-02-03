const apiBase = "http://localhost:8000";

const projectsList = document.getElementById("projects");
const fileStructure = document.getElementById("fileStructure");
const functionCode = document.getElementById("functionCode");
const searchResults = document.getElementById("searchResults");
const llmOutput = document.getElementById("llmOutput");

const loadProjectsButton = document.getElementById("loadProjects");
const loadFilesButton = document.getElementById("loadFiles");
const loadStructureButton = document.getElementById("loadStructure");
const loadFunctionButton = document.getElementById("loadFunction");
const runSearchButton = document.getElementById("runSearch");
const generateButton = document.getElementById("generate");

function renderList(element, items) {
  element.innerHTML = "";
  items.forEach((item) => {
    const li = document.createElement("li");
    li.textContent = item;
    element.appendChild(li);
  });
}

function renderObjectList(element, items, formatter) {
  element.innerHTML = "";
  items.forEach((item) => {
    const li = document.createElement("li");
    li.textContent = formatter(item);
    element.appendChild(li);
  });
}

loadProjectsButton.addEventListener("click", async () => {
  const rootDir = document.getElementById("rootDir").value || ".";
  const response = await fetch(`${apiBase}/api/projects?root_dir=${encodeURIComponent(rootDir)}`);
  const data = await response.json();
  renderObjectList(projectsList, data, (item) => `${item.name}: ${item.path}`);
});

loadFilesButton.addEventListener("click", async () => {
  const vbpPath = document.getElementById("vbpPath").value;
  const response = await fetch(`${apiBase}/api/projects/files`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ vbp_path: vbpPath }),
  });
  const data = await response.json();
  renderList(fileStructure, data);
});

loadStructureButton.addEventListener("click", async () => {
  const filePath = document.getElementById("filePath").value;
  const response = await fetch(`${apiBase}/api/files/structure`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ file_path: filePath }),
  });
  const data = await response.json();
  renderObjectList(
    fileStructure,
    data,
    (item) => `${item.kind} ${item.name}${item.comment ? ` — ${item.comment}` : ""}`
  );
});

loadFunctionButton.addEventListener("click", async () => {
  const filePath = document.getElementById("functionFilePath").value;
  const funcName = document.getElementById("functionName").value;
  const response = await fetch(`${apiBase}/api/files/function`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ file_path: filePath, func_name: funcName }),
  });
  const data = await response.json();
  functionCode.textContent = data.code || "";
});

runSearchButton.addEventListener("click", async () => {
  const keyword = document.getElementById("searchKeyword").value;
  const response = await fetch(`${apiBase}/api/search`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ keyword }),
  });
  const data = await response.json();
  renderObjectList(searchResults, data, (item) => `${item.file}:${item.line} ${item.text}`);
});

generateButton.addEventListener("click", async () => {
  const prompt = document.getElementById("prompt").value;
  const response = await fetch(`${apiBase}/api/wiki/generate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ prompt }),
  });
  const data = await response.json();
  llmOutput.textContent = data.content || data.detail || "";
});

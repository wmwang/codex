# 產級 AI Wiki 自動化生成計畫

此專案提供 VB6 Code Wiki 的完整雛型：包含後端 API、前端控制台與 VB 工具集，讓 LLM 能以「先看結構、再讀函式」的流程產出 Wiki。

## 快速開始

### 後端（FastAPI）
```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 前端
```bash
cd frontend
python -m http.server 5173
```

打開 `http://localhost:5173` 進入控制台，預設後端連線位址為 `http://localhost:8000`。

## LLM 連線設定

```bash
export OPENAI_API_KEY=your_key
export OPENAI_MODEL=gpt-4o-mini
```

## 工具集
工具集定義於 `wiki_tools.py`，支援：
- 專案列舉
- 專案檔案清單
- 函式結構掃描
- 指定函式內容讀取
- 關鍵字搜尋

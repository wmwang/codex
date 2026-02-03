# 產級 AI Wiki 自動化生成計畫｜實施計畫文件

本文件將「四階段戰略」具體化為可執行的實作清單、里程碑與驗收標準，確保從掃描 300+ 專案到可用的 Wiki 輸出有清楚的落地路徑。

## 目標與範圍
- **目標**：建立可重複執行的流程，將 VB6 專案轉為結構化 Wiki（專案首頁、模組頁、函數詳解）。
- **範圍**：VBP 解析、檔案索引、函式結構抽取、精準函式讀取、搜尋與摘要輸出。
- **非範圍**：自動上線/部署 Wiki 平台（另行規劃）。

## 分階段實作計畫

### Phase 1：全域導航地圖（Global Map）
**目的**：建立「專案 → 檔案 → 反向引用」的可查詢索引。

**工作項目**
1. 盤點專案（掃描 `.vbp`）。
2. 解析 `.vbp` 內引用（`Form=`, `Module=`, `Class=` 等）。
3. 產出 `project_structure.json`：包含專案清單、檔案清單、檔案反向索引。

**輸出物**
- `project_structure.json`
- 產生指令：`python scripts/build_project_structure.py --root <code_root> --output project_structure.json`

**驗收標準**
- 可列出所有專案與 `.vbp` 路徑。
- 任一專案的檔案清單與實際內容一致。
- 任一檔案可反查其被哪些專案引用。

### Phase 2：手術刀工具箱（Toolset）
**目的**：提供 LLM 可呼叫的「窄而精準」查詢能力，避免 Context 爆炸。

**工作項目**
1. `get_all_projects()`：列出所有 `.vbp`。
2. `get_project_file_list(vbp_path)`：列出專案檔案。
3. `get_file_structure(file_path)`：只回傳 Sub/Function/Property 名稱與註解。
4. `read_function_code(file_path, func_name)`：精準讀取單一函式內容。
5. `search_codebase(keyword)`：全域關鍵字搜尋。

**輸出物**
- `wiki_tools.py`（工具函式集合）

**驗收標準**
- 每個工具都能在 1~2 秒內回傳結果（針對單檔/單專案）。
- `get_file_structure` 不回傳程式碼內容。
- `read_function_code` 只能回傳單一函式範圍（不含其他函式）。

### Phase 3：Agent 工作流（Agent Loop）
**目的**：建立「先看結構，再挑函式讀」的遞迴流程，逐步生成摘要。

**工作項目**
1. 選定專案（如 Inventory.vbp）。
2. 列出檔案 → 讀取結構 → 挑重要函式 → 讀取函式。
3. 產出模組摘要、函式摘要（人類可讀文字）。
4. 實作「上下文清理」：一次只保留當前函式。

**輸出物**
- `wiki/<project>/index.md`（專案首頁）
- `wiki/<project>/<module>.md`（模組頁）
- `wiki/<project>/<module>/<function>.md`（函數詳解）

**驗收標準**
- 專案首頁包含：用途、主要模組、依賴關係。
- 模組頁包含：模組職責、函式清單與摘要。
- 函式詳解包含：輸入/輸出、用途、關聯模組/函式。

### Phase 4：組裝與發布（Wiki Assembly）
**目的**：將 Agent 產出的結構化資訊組裝為可瀏覽 Wiki。

**工作項目**
1. 組裝 markdown 結構（專案首頁、模組頁、函式頁）。
2. 產生索引頁（依專案分類）。
3. 選擇輸出管道：Markdown、Confluence、Notion 或靜態網站。

**輸出物**
- `wiki/` 目錄或指定 Wiki 平台的同步資料。

**驗收標準**
- 可從專案首頁一路點到函式詳解。
- 檔案連結正確且可回溯。

## 建議時間軸（可調整）
- **Day 1**：完成 Phase 1（產出 `project_structure.json`）。
- **Day 2-3**：完成 Phase 2（工具可穩定呼叫）。
- **Day 4-7**：完成 Phase 3（跑 1~3 個核心專案作為 PoC）。
- **Day 8+**：完成 Phase 4（組裝與發布）。

## 風險與因應
- **VB 語法異常**：可能需補強解析規則（如帶行號的 VB、特殊 Property 宣告）。
  - *因應*：記錄 parse error 並回報清單，不中斷全流程。
- **檔案路徑非 UTF-8**：部分舊專案可能使用 Big5。
  - *因應*：允許自訂 encoding 或逐檔 fallback。
- **專案間共享檔案**：需避免重複摘要。
  - *因應*：利用反向索引標記共享模組。

## 下一步（立即可執行）
1. 在專案根目錄執行 `python scripts/build_project_structure.py` 產出全域索引。
2. 指定 1 個核心 `.vbp` 作為試點，走完完整 Agent Loop。
3. 驗收輸出格式後，再批次擴展至其他專案。

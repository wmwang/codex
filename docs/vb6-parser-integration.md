# VB6 Parser 原始碼整合建議（Java-only 方案）

本文件提供在 Java-only 方案下，將開源 VB6 Parser 原始碼「直接整合」到自家專案內的建議作法。

## 可以直接整合嗎？

可以，但要注意下列要點：

1. **授權條款與再散佈**
   - 先確認開源專案的 License（常見如 Apache-2.0、MIT、GPL）。
   - 保留原始授權檔案與版權聲明（`LICENSE`/`NOTICE`）。
   - 若是 GPL/LGPL，會對你的專案授權方式產生約束，需要特別評估。

2. **長期維護成本**
   - 將原始碼「整合進專案」代表你要負責同步上游更新與修補。
   - 如果預期會修改 parser 行為，建議 fork 並保留 upstream 來源資訊。

3. **可追溯性與審核**
   - 建議保留 upstream commit hash 或版本 tag。
   - 在文件中記錄來源網址與導入版本。

## 推薦整合方式（可調整）

以下是常見的 Java-only 導入方案，依可維護性由高到低排序：

### 方案 A：Fork + Vendor 原始碼（推薦）

1. Fork 原始 parser 專案。
2. 在你的 repo 中建立 `vendor/` 或 `third_party/` 資料夾。
3. 將 parser 原始碼放入：
   ```
   /vendor/proleap-vb6-parser/
   ```
4. 保留原始 `LICENSE`/`NOTICE`。
5. 在你的 build system（Maven/Gradle）中加入該 module。

### 方案 B：Git Submodule

1. 以 submodule 引入開源 parser。
2. 優點是容易同步 upstream。
3. 缺點是操作與 CI/CD 需要多一道流程。

### 方案 C：直接複製（不建議）

1. 直接 copy 原始碼進 repo root。
2. 短期最快，但長期維護與更新最難。

## 實作建議（Java-only MVP）

1. **Parser module**：負責 VB6 AST 解析。
2. **Extractor module**：走訪 AST，抽出 Form → Event → Call 關係。
3. **Graph module**：保存關係圖（JGraphT 或 Neo4j）。
4. **Docs module**：輸出 Markdown + Mermaid。

## 導入時的必要檔案紀錄

- `docs/third_party.md`（記錄來源、版本、license）
- `vendor/proleap-vb6-parser/LICENSE`
- `vendor/proleap-vb6-parser/NOTICE`

## 測試用 VB6 專案建議（隔離環境）

如果暫時無法提供內部專案，可先用公開專案做解析與關係圖輸出測試。建議篩選條件如下：

1. **含多個 `.vbp` 或多個 `.frm/.bas/.cls`**，以確保能測到跨模組關係。
2. **有明確 GUI 入口**（Startup Form 或 `Sub Main`），便於驗證流程抽取。
3. **規模中等**（數十到上百個檔案），先測出效能與輸出結構。

建議先在 GitHub 以關鍵字搜尋，例如：

- `vb6 vbp`  
- `visual basic 6 project`  
- `vb6 sample app`  

確認專案內容後，再將其作為 parser 測試資料集。

### 指定測試專案：PhotoDemon

若你希望直接指定固定專案作為基準測試，建議使用 **PhotoDemon**：

- Repo：<https://github.com/tannerhelland/PhotoDemon>
- 理由：專案規模大、包含 GUI 入口與多個 `.frm/.bas/.cls` 檔案，適合驗證解析、抽取與效能。
- 實作建議：
  1. 以 `.vbp` 作為入口索引專案檔案清單。
  2. 先從 Startup Form / `Sub Main` 往下抽取事件與呼叫鏈。
  3. 針對常用操作（開檔、濾鏡、工具列）建立 sample 流程輸出，確認關係圖完整性。

---

如果需要，我可以協助你建立：
- 建議的 repo 目錄結構
- Maven/Gradle multi-module 範本
- 解析輸出 JSON schema 與 Mermaid 範本

## 開工任務拆解（可直接指派）

以下是可直接拆派給團隊的工作清單，依優先順序排列，完成後即可跑通 MVP：

### P0（必須完成）

- [ ] **Repo 骨架與模組**：建立 `parser / extractor / graph / docs` 模組與基礎 build 設定（Maven/Gradle 其一）。
- [ ] **VB6 Parser 導入**：以「Fork + Vendor」方式內嵌 parser 原始碼，保留 `LICENSE/NOTICE` 並記錄來源。
- [ ] **.vbp 索引器**：掃描 `.vbp`，輸出該專案包含的 `.frm/.bas/.cls` 清單與 Startup 設定。
- [ ] **AST 解析最小管線**：能對單一 `.frm` 解析 AST 並列出事件入口（例如 `cmdLogin_Click`）。
- [ ] **呼叫關係抽取**：從事件 handler 抽取 `Call/Show/Load` 關係，輸出為 JSON（先不必完整 Graph）。
- [ ] **測試資料**：以 PhotoDemon 作為測試專案，跑通上述流程並輸出樣本 JSON。

### P1（短期擴充）

- [ ] **Graph 建模**：將關係資料轉為圖結構（JGraphT 或 Neo4j 其一）。
- [ ] **Docs 產出**：輸出 Markdown + Mermaid 的 GUI 關係圖。
- [ ] **入口流程驗證**：針對 Startup Form / `Sub Main` 做一條完整流程追蹤。

### P2（可選）

- [ ] **跨專案連結**：支援多個 `.vbp` 之間的引用關係。
- [ ] **Wiki 匯出**：產生可直接貼到 Confluence/GitHub Wiki 的頁面。

## 下一步已開工（本次新增）

- 已建立 Java multi-module 骨架（`parser/extractor/graph/docs/cli`）。
- 已完成 `.vbp` 索引器與 CLI 輸出 JSON。
- 已加入 `.frm` 事件與呼叫關係的最小抽取器（regex-based），並提供 CLI 輸出 JSON。

> 注意：目前 `.frm` 抽取器仍屬 MVP 版本（以 regex 解析），後續可替換為 AST-based 抽取。

## `.bas` 解析（MVP）

已新增 `.bas` 模組解析器（regex-based）與 CLI，可抽出 `Sub/Function` 與 `Call` 關係。
後續可在 AST 整合完成後替換為 AST-based 抽取。

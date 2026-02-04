# VB6 Analyzer 使用手冊（目前版本）

本手冊說明目前專案可用的功能與測試方式（VBP 索引、FRM/BAS 抽取），
並提供專案級輸出與 AST 模式的使用方式。

## 1. 目前已完成的功能

- **VBP 索引**：解析 `.vbp`，輸出專案名稱、Startup、以及 Form/Module/Class 清單。
- **FRM 抽取（MVP）**：從 `.frm` 擷取事件（`Sub xxx_Click`）與 `Call/Show/Load` 關係。
- **BAS 抽取（MVP）**：從 `.bas` 擷取 `Sub/Function` 與 `Call` 關係。
- **全專案分析**：掃描根目錄底下所有 `.vbp`，輸出 `analysis.json` + `report.md`（含 Mermaid）。

> 注意：目前 `.frm/.bas` 抽取是 regex-based MVP，AST 模式需搭配 Proleap VB6 Parser。
> 本專案預設以 Big5 讀取 VB6 檔案（適用於較舊的繁體中文專案）。

## 2. 專案結構

```
backend/
  pom.xml              # Maven parent
  parser/              # VBP index
  extractor/           # FRM/BAS extractor
  cli/                 # CLI tools
```

## 3. 建置方式

### 3.1 命令列（Maven CLI）

在專案根目錄執行：

```bash
mvn -f backend/pom.xml -pl cli -am package
```

> 會產出 `backend/cli/target/cli-0.1.0-SNAPSHOT.jar`

### 3.2 IntelliJ IDEA（Maven 視窗）

若你是用 IntelliJ IDEA 開啟專案，可透過 **Maven Tool Window** 執行：

1. 開啟 `View > Tool Windows > Maven`  
2. 在 `backend` 的 `Lifecycle` 裡執行 `package`  
3. 或直接執行 `backend` 的 Maven module：`backend:cli` → `package`  

產物仍會輸出到 `backend/cli/target/cli-0.1.0-SNAPSHOT.jar`

## 4. CLI 使用方式

### 4.1 VBP 索引

```bash
java -cp backend/cli/target/cli-0.1.0-SNAPSHOT.jar \
  com.codex.vb6.cli.VbpIndexCli /path/to/project.vbp
```

輸出範例（JSON）：

```json
{
  "name": "MyProject",
  "startup": "FormMain",
  "entries": [
    {"type":"FORM","name":null,"path":"FormMain.frm"},
    {"type":"MODULE","name":null,"path":"Module1.bas"}
  ]
}
```

### 4.2 FRM 抽取（事件 + 呼叫關係）

```bash
java -cp backend/cli/target/cli-0.1.0-SNAPSHOT.jar \
  com.codex.vb6.cli.FrmExtractCli /path/to/FormMain.frm
```

輸出範例（JSON）：

```json
{
  "form": "FormMain",
  "events": [
    {
      "name": "cmdLogin_Click",
      "line": 210,
      "calls": [
        {"type":"CALL","target":"AuthModule.Login","line":"Call AuthModule.Login"},
        {"type":"SHOW","target":"FormMenu","line":"FormMenu.Show"}
      ]
    }
  ]
}
```

### 4.3 BAS 抽取（Sub/Function + Call）

```bash
java -cp backend/cli/target/cli-0.1.0-SNAPSHOT.jar \
  com.codex.vb6.cli.BasExtractCli /path/to/Module1.bas
```

輸出範例（JSON）：

```json
{
  "module": "Module1",
  "routines": [
    {
      "name": "Init",
      "kind": "SUB",
      "line": 12,
      "calls": [
        {"type":"CALL","target":"LoadConfig","line":"Call LoadConfig"}
      ]
    }
  ]
}
```

### 4.4 全專案分析（VBP + FRM + BAS）

```bash
java -cp backend/cli/target/cli-0.1.0-SNAPSHOT.jar \
  com.codex.vb6.cli.ProjectAnalyzeCli /path/to/project/root /path/to/output
```

如要嘗試 AST 模式（需先把 Proleap parser 放到 classpath）：

```bash
java -cp backend/cli/target/cli-0.1.0-SNAPSHOT.jar \
  com.codex.vb6.cli.ProjectAnalyzeCli /path/to/project/root /path/to/output --parser=ast
```

輸出：
- `analysis.json`：完整索引與解析結果
- `report.md`：Markdown 報告與 Mermaid 呼叫圖

## 5. 常見問題

### 5.1 AST 模式目前是什麼狀態？
已支援 `--parser=ast` 參數，並會檢查 Proleap VB6 Parser 是否在 classpath。
目前尚未完成 AST walker，因此 AST 模式仍會沿用 regex 的抽取邏輯。

### 5.2 為什麼會出現亂碼？
本專案預設使用 Big5 讀取 VB6 檔案。若你的專案使用其他編碼，需調整程式碼中的 `VB6_CHARSET`。

### 5.3 `.bas/.frm` 不一定全抓得到？
是的，regex 版本只覆蓋常見語法。後續會用 AST 強化正確性。

## 6. 下一步建議

- 導入 Proleap VB6 Parser 原始碼（vendor 或 submodule）
- 以 AST walker 取代 regex 抽取器
- 擴充 `.cls/.ctl/.res` 的解析支援

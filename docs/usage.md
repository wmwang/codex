# VB6 Analyzer 使用手冊（目前版本）

本手冊說明目前專案可用的功能與測試方式（VBP 索引、FRM/BAS 抽取），並提供基礎命令範例。

## 1. 目前已完成的功能

- **VBP 索引**：解析 `.vbp`，輸出專案名稱、Startup、以及 Form/Module/Class 清單。
- **FRM 抽取（MVP）**：從 `.frm` 擷取事件（`Sub xxx_Click`）與 `Call/Show/Load` 關係。
- **BAS 抽取（MVP）**：從 `.bas` 擷取 `Sub/Function` 與 `Call` 關係。

> 注意：目前 `.frm/.bas` 抽取是 regex-based MVP，之後會替換為 AST-based。

## 2. 專案結構

```
backend/
  pom.xml              # Maven parent
  parser/              # VBP index
  extractor/           # FRM/BAS extractor
  cli/                 # CLI tools
```

## 3. 建置方式

在專案根目錄執行：

```bash
mvn -f backend/pom.xml -pl cli -am package
```

> 會產出 `backend/cli/target/cli-0.1.0-SNAPSHOT.jar`

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

## 5. 常見問題

### 5.1 為什麼沒有 AST 解析？
目前以 regex 作為 MVP，待整合 Proleap VB6 Parser 後會替換為 AST-based。

### 5.2 `.bas/.frm` 不一定全抓得到？
是的，regex 版本只覆蓋常見語法。後續會用 AST 強化正確性。

## 6. 下一步建議

- 整合 Proleap VB6 Parser 原始碼
- 建立 AST-based extractor
- 導出 Graph 與 Markdown/Mermaid 文件

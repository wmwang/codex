package com.codex.vb6.cli;

import com.codex.vb6.extractor.BasAnalysis;
import com.codex.vb6.extractor.EntryStatus;
import com.codex.vb6.extractor.FrmAnalysis;
import com.codex.vb6.extractor.FrmCall;
import com.codex.vb6.extractor.FrmEvent;
import com.codex.vb6.extractor.ProjectAnalysis;
import com.codex.vb6.extractor.ProjectAnalyzer;
import com.codex.vb6.extractor.ProleapAstExtractor;
import com.codex.vb6.extractor.RegexVb6Extractor;
import com.codex.vb6.extractor.Vb6Extractor;
import com.codex.vb6.extractor.ProjectSummary;
import com.codex.vb6.graph.GraphBuilder;
import com.codex.vb6.graph.GraphModel;
import com.codex.vb6.graph.MermaidRenderer;
import com.codex.vb6.parser.VbpEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class ProjectAnalyzeCli {
    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: ProjectAnalyzeCli <root-dir> <output-dir> [--parser=regex|ast]");
            System.exit(1);
        }

        Path rootDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        Vb6Extractor extractor = parseExtractor(args.length == 3 ? args[2] : null);
        Files.createDirectories(outputDir);

        ProjectAnalysis analysis = ProjectAnalyzer.analyze(rootDir, extractor);
        GraphModel graph = GraphBuilder.build(analysis);
        writeFile(outputDir.resolve("analysis.json"), toJson(analysis));
        writeFile(outputDir.resolve("report.md"), toMarkdown(analysis, graph));

        System.out.println("Wrote analysis.json and report.md to " + outputDir.toAbsolutePath());
    }

    private static Vb6Extractor parseExtractor(String arg) {
        if (arg == null || arg.isBlank()) {
            return new RegexVb6Extractor();
        }
        String normalized = arg.trim().toLowerCase();
        if (normalized.startsWith("--parser=")) {
            normalized = normalized.substring("--parser=".length());
        }

        return switch (normalized) {
            case "ast" -> new ProleapAstExtractor();
            case "regex" -> new RegexVb6Extractor();
            default -> throw new IllegalArgumentException("Unknown parser mode: " + arg);
        };
    }

    private static void writeFile(Path path, String contents) throws IOException {
        Files.writeString(path, contents, StandardCharsets.UTF_8);
    }

    private static String toJson(ProjectAnalysis analysis) {
        String projects = analysis.projects().stream()
                .map(ProjectAnalyzeCli::projectToJson)
                .collect(Collectors.joining(","));
        return "{" + "\"projects\":[" + projects + "]}";
    }

    private static String projectToJson(ProjectSummary project) {
        String entries = project.entries().stream()
                .map(ProjectAnalyzeCli::entryToJson)
                .collect(Collectors.joining(","));
        String forms = project.forms().stream()
                .map(ProjectAnalyzeCli::formToJson)
                .collect(Collectors.joining(","));
        String modules = project.modules().stream()
                .map(ProjectAnalyzeCli::moduleToJson)
                .collect(Collectors.joining(","));

        return "{" +
                "\"vbpPath\":" + jsonString(project.vbpPath()) + "," +
                "\"name\":" + jsonString(project.project().name()) + "," +
                "\"startup\":" + jsonString(project.project().startup()) + "," +
                "\"entries\":[" + entries + "]," +
                "\"forms\":[" + forms + "]," +
                "\"modules\":[" + modules + "]" +
                "}";
    }

    private static String entryToJson(EntryStatus status) {
        VbpEntry entry = status.entry();
        return "{" +
                "\"type\":" + jsonString(entry.type().name()) + "," +
                "\"name\":" + jsonString(entry.name()) + "," +
                "\"path\":" + jsonString(entry.path()) + "," +
                "\"resolved\":" + jsonString(status.resolvedPath()) + "," +
                "\"exists\":" + status.exists() +
                "}";
    }

    private static String formToJson(FrmAnalysis form) {
        String events = form.events().stream()
                .map(ProjectAnalyzeCli::eventToJson)
                .collect(Collectors.joining(","));
        return "{" +
                "\"form\":" + jsonString(form.formName()) + "," +
                "\"events\":[" + events + "]" +
                "}";
    }

    private static String eventToJson(FrmEvent event) {
        String calls = event.calls().stream()
                .map(ProjectAnalyzeCli::callToJson)
                .collect(Collectors.joining(","));
        return "{" +
                "\"name\":" + jsonString(event.name()) + "," +
                "\"line\":" + event.lineNumber() + "," +
                "\"calls\":[" + calls + "]" +
                "}";
    }

    private static String callToJson(FrmCall call) {
        return "{" +
                "\"type\":" + jsonString(call.type()) + "," +
                "\"target\":" + jsonString(call.target()) + "," +
                "\"line\":" + jsonString(call.line()) +
                "}";
    }

    private static String moduleToJson(BasAnalysis module) {
        return "{" +
                "\"module\":" + jsonString(module.moduleName()) + "," +
                "\"routines\":" + module.routines().size() +
                "}";
    }

    private static String toMarkdown(ProjectAnalysis analysis, GraphModel graph) {
        StringBuilder builder = new StringBuilder();
        builder.append("# VB6 專案分析報告\n\n");
        builder.append("共掃描 ").append(analysis.projects().size()).append(" 個 .vbp 專案。\n\n");

        for (ProjectSummary project : analysis.projects()) {
            builder.append("## ").append(nullSafe(project.project().name(), "(未命名專案)"))
                    .append("\n\n");
            builder.append("- VBP: ").append(project.vbpPath()).append("\n");
            builder.append("- Startup: ").append(nullSafe(project.project().startup(), "(未指定)"))
                    .append("\n");
            builder.append("- Entries: ").append(project.entries().size()).append("\n");
            builder.append("- Forms: ").append(project.forms().size()).append("\n");
            builder.append("- Modules: ").append(project.modules().size()).append("\n\n");

            builder.append("### 缺失檔案\n\n");
            List<EntryStatus> missing = project.entries().stream()
                    .filter(status -> !status.exists())
                    .toList();
            if (missing.isEmpty()) {
                builder.append("- (無)\n\n");
            } else {
                for (EntryStatus status : missing) {
                    builder.append("- ").append(status.entry().type()).append(": ")
                            .append(status.entry().path()).append("\n");
                }
                builder.append("\n");
            }

            builder.append("### 表單事件與呼叫關係\n\n");
            if (project.forms().isEmpty()) {
                builder.append("- (無)\n\n");
            } else {
                for (FrmAnalysis form : project.forms()) {
                    builder.append("#### ").append(nullSafe(form.formName(), "(未命名 Form)"))
                            .append("\n\n");
                    if (form.events().isEmpty()) {
                        builder.append("- (無事件)\n\n");
                        continue;
                    }
                    for (FrmEvent event : form.events()) {
                        builder.append("- ").append(event.name()).append(" (line ")
                                .append(event.lineNumber()).append(")\n");
                        for (FrmCall call : event.calls()) {
                            builder.append("  - ").append(call.type()).append(": ")
                                    .append(call.target()).append("\n");
                        }
                    }
                    builder.append("\n");
                }
            }

            builder.append("### Mermaid 呼叫圖（Form Events）\n\n");
            builder.append(renderFormMermaid(project));
            builder.append("\n");
        }

        builder.append("## 全專案 Mermaid Graph\n\n");
        builder.append(MermaidRenderer.render(graph));
        builder.append("\n");

        return builder.toString();
    }

    private static String renderFormMermaid(ProjectSummary project) {
        StringBuilder builder = new StringBuilder();
        builder.append("```mermaid\n");
        builder.append("graph TD\n");
        for (FrmAnalysis form : project.forms()) {
            for (FrmEvent event : form.events()) {
                String eventNode = sanitize(form.formName()) + "_" + sanitize(event.name());
                builder.append("  ").append(eventNode).append("[\"")
                        .append(nullSafe(event.name(), "event"))
                        .append("\"]\n");
                for (FrmCall call : event.calls()) {
                    String targetNode = sanitize(call.target());
                    builder.append("  ").append(eventNode).append(" --> ")
                            .append(targetNode).append("[\"")
                            .append(call.target()).append("\"]\n");
                }
            }
        }
        builder.append("```\n");
        return builder.toString();
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static String nullSafe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9') || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }
}

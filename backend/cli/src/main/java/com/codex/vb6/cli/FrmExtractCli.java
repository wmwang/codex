package com.codex.vb6.cli;

import com.codex.vb6.extractor.FrmAnalysis;
import com.codex.vb6.extractor.FrmCall;
import com.codex.vb6.extractor.FrmEvent;
import com.codex.vb6.extractor.FrmExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class FrmExtractCli {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: FrmExtractCli <path-to-frm>");
            System.exit(1);
        }

        FrmAnalysis analysis = FrmExtractor.analyze(Path.of(args[0]));
        System.out.println(toJson(analysis));
    }

    private static String toJson(FrmAnalysis analysis) {
        String events = analysis.events().stream()
                .map(FrmExtractCli::eventToJson)
                .collect(Collectors.joining(","));

        return "{" +
                "\"form\":" + jsonString(analysis.formName()) + "," +
                "\"events\":[" + events + "]" +
                "}";
    }

    private static String eventToJson(FrmEvent event) {
        String calls = event.calls().stream()
                .map(FrmExtractCli::callToJson)
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

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}

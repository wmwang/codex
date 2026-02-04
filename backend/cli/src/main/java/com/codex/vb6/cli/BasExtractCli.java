package com.codex.vb6.cli;

import com.codex.vb6.extractor.BasAnalysis;
import com.codex.vb6.extractor.BasCall;
import com.codex.vb6.extractor.BasExtractor;
import com.codex.vb6.extractor.BasRoutine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class BasExtractCli {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: BasExtractCli <path-to-bas>");
            System.exit(1);
        }

        BasAnalysis analysis = BasExtractor.analyze(Path.of(args[0]));
        System.out.println(toJson(analysis));
    }

    private static String toJson(BasAnalysis analysis) {
        String routines = analysis.routines().stream()
                .map(BasExtractCli::routineToJson)
                .collect(Collectors.joining(","));

        return "{" +
                "\"module\":" + jsonString(analysis.moduleName()) + "," +
                "\"routines\":[" + routines + "]" +
                "}";
    }

    private static String routineToJson(BasRoutine routine) {
        String calls = routine.calls().stream()
                .map(BasExtractCli::callToJson)
                .collect(Collectors.joining(","));

        return "{" +
                "\"name\":" + jsonString(routine.name()) + "," +
                "\"kind\":" + jsonString(routine.kind()) + "," +
                "\"line\":" + routine.lineNumber() + "," +
                "\"calls\":[" + calls + "]" +
                "}";
    }

    private static String callToJson(BasCall call) {
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

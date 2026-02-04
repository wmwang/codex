package com.codex.vb6.cli;

import com.codex.vb6.parser.VbpEntry;
import com.codex.vb6.parser.VbpIndex;
import com.codex.vb6.parser.VbpProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class VbpIndexCli {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: VbpIndexCli <path-to-vbp>");
            System.exit(1);
        }

        VbpProject project = VbpIndex.parse(Path.of(args[0]));
        System.out.println(toJson(project));
    }

    private static String toJson(VbpProject project) {
        String entries = project.entries().stream()
                .map(VbpIndexCli::entryToJson)
                .collect(Collectors.joining(","));

        return "{" +
                "\"name\":" + jsonString(project.name()) + "," +
                "\"startup\":" + jsonString(project.startup()) + "," +
                "\"entries\":[" + entries + "]" +
                "}";
    }

    private static String entryToJson(VbpEntry entry) {
        return "{" +
                "\"type\":" + jsonString(entry.type().name()) + "," +
                "\"name\":" + jsonString(entry.name()) + "," +
                "\"path\":" + jsonString(entry.path()) +
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

package com.codex.vb6.extractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BasExtractor {
    private static final Charset VB6_CHARSET = Charset.forName("Big5");
    private static final Pattern MODULE_NAME = Pattern.compile("^\\s*Attribute\\s+VB_Name\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROUTINE_START = Pattern.compile("^\\s*(?:Public|Private|Friend)?\\s*(Sub|Function)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROUTINE_END = Pattern.compile("^\\s*End\\s+(Sub|Function)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CALL_PATTERN = Pattern.compile("\\b(Call\\s+)?([A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?)\\s*(?:\\(|$)", Pattern.CASE_INSENSITIVE);

    private BasExtractor() {
    }

    public static BasAnalysis analyze(Path basPath) throws IOException {
        List<String> lines = Files.readAllLines(basPath, VB6_CHARSET);
        String moduleName = null;
        List<BasRoutine> routines = new ArrayList<>();
        List<BasCall> currentCalls = null;
        String currentRoutine = null;
        String currentKind = null;
        int currentLine = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (moduleName == null) {
                Matcher nameMatcher = MODULE_NAME.matcher(line);
                if (nameMatcher.find()) {
                    moduleName = nameMatcher.group(1);
                }
            }

            Matcher routineStart = ROUTINE_START.matcher(line);
            if (routineStart.find()) {
                currentKind = routineStart.group(1).toUpperCase();
                currentRoutine = routineStart.group(2);
                currentLine = i + 1;
                currentCalls = new ArrayList<>();
                continue;
            }

            if (currentRoutine != null) {
                if (ROUTINE_END.matcher(line).find()) {
                    routines.add(new BasRoutine(currentRoutine, currentKind, currentLine, List.copyOf(currentCalls)));
                    currentRoutine = null;
                    currentKind = null;
                    currentCalls = null;
                    continue;
                }

                currentCalls.addAll(extractCalls(line));
            }
        }

        return new BasAnalysis(moduleName, List.copyOf(routines));
    }

    private static List<BasCall> extractCalls(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("'")) {
            return List.of();
        }

        List<BasCall> calls = new ArrayList<>();
        Matcher callMatcher = CALL_PATTERN.matcher(line);
        while (callMatcher.find()) {
            String target = callMatcher.group(2);
            if (target != null && !target.equalsIgnoreCase("Call")) {
                calls.add(new BasCall("CALL", target, trimmed));
            }
        }

        return calls;
    }
}

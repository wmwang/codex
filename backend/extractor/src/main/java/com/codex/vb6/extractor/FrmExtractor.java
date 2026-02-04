package com.codex.vb6.extractor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FrmExtractor {
    private static final Charset VB6_CHARSET = Charset.forName("Big5");
    private static final Pattern FORM_NAME = Pattern.compile("^\\s*Begin\\s+VB\\.Form\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_START = Pattern.compile("^\\s*(?:Public|Private)?\\s*Sub\\s+(\\w+)_([A-Za-z0-9_]+)\\s*\\(.*\\)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EVENT_END = Pattern.compile("^\\s*End\\s+Sub\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CALL_PATTERN = Pattern.compile("\\b(Call\\s+)?([A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)?)\\s*(?:\\(|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHOW_PATTERN = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\.Show\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOAD_PATTERN = Pattern.compile("\\bLoad\\s+([A-Za-z_][A-Za-z0-9_]*)\\b", Pattern.CASE_INSENSITIVE);

    private FrmExtractor() {
    }

    public static FrmAnalysis analyze(Path frmPath) throws IOException {
        List<String> lines = Files.readAllLines(frmPath, VB6_CHARSET);
        String formName = null;
        List<FrmEvent> events = new ArrayList<>();
        List<FrmCall> currentCalls = null;
        String currentEvent = null;
        int currentEventLine = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (formName == null) {
                Matcher formMatcher = FORM_NAME.matcher(line);
                if (formMatcher.find()) {
                    formName = formMatcher.group(1);
                }
            }

            Matcher startMatcher = EVENT_START.matcher(line);
            if (startMatcher.find()) {
                currentEvent = startMatcher.group(1) + "_" + startMatcher.group(2);
                currentEventLine = i + 1;
                currentCalls = new ArrayList<>();
                continue;
            }

            if (currentEvent != null) {
                if (EVENT_END.matcher(line).find()) {
                    events.add(new FrmEvent(currentEvent, currentEventLine, List.copyOf(currentCalls)));
                    currentEvent = null;
                    currentCalls = null;
                    continue;
                }

                currentCalls.addAll(extractCalls(line));
            }
        }

        return new FrmAnalysis(formName, List.copyOf(events));
    }

    private static List<FrmCall> extractCalls(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("'")) {
            return List.of();
        }

        List<FrmCall> calls = new ArrayList<>();

        Matcher showMatcher = SHOW_PATTERN.matcher(line);
        while (showMatcher.find()) {
            calls.add(new FrmCall("SHOW", showMatcher.group(1), trimmed));
        }

        Matcher loadMatcher = LOAD_PATTERN.matcher(line);
        while (loadMatcher.find()) {
            calls.add(new FrmCall("LOAD", loadMatcher.group(1), trimmed));
        }

        Matcher callMatcher = CALL_PATTERN.matcher(line);
        while (callMatcher.find()) {
            String target = callMatcher.group(2);
            if (target != null && !target.equalsIgnoreCase("Call")) {
                calls.add(new FrmCall("CALL", target, trimmed));
            }
        }

        return calls;
    }
}

package com.codex.vb6.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VbpIndex {
    private static final Charset VB6_CHARSET = Charset.forName("Big5");
    private VbpIndex() {
    }

    public static VbpProject parse(Path vbpPath) throws IOException {
        List<String> lines = Files.readAllLines(vbpPath, VB6_CHARSET);
        List<VbpEntry> entries = new ArrayList<>();
        String name = null;
        String startup = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("'")) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                continue;
            }
            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();

            switch (key.toLowerCase(Locale.ROOT)) {
                case "name" -> name = value;
                case "startup" -> startup = value;
                case "form" -> entries.add(buildEntry(VbpEntryType.FORM, value));
                case "module" -> entries.add(buildEntry(VbpEntryType.MODULE, value));
                case "class" -> entries.add(buildEntry(VbpEntryType.CLASS, value));
                default -> {
                }
            }
        }

        return new VbpProject(name, startup, List.copyOf(entries));
    }

    private static VbpEntry buildEntry(VbpEntryType type, String value) {
        String name = null;
        String path = value;

        int separator = value.indexOf(';');
        if (separator >= 0) {
            name = value.substring(0, separator).trim();
            path = value.substring(separator + 1).trim();
        }

        return new VbpEntry(type, name, path);
    }
}

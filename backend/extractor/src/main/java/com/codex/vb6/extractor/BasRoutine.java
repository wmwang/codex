package com.codex.vb6.extractor;

import java.util.List;

public record BasRoutine(String name, String kind, int lineNumber, List<BasCall> calls) {
}

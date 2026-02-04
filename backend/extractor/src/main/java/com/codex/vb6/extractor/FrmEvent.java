package com.codex.vb6.extractor;

import java.util.List;

public record FrmEvent(String name, int lineNumber, List<FrmCall> calls) {
}

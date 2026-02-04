package com.codex.vb6.extractor;

import com.codex.vb6.parser.VbpEntry;

public record EntryStatus(VbpEntry entry, String resolvedPath, boolean exists) {
}

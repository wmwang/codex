package com.codex.vb6.parser;

import java.util.List;

public record VbpProject(String name, String startup, List<VbpEntry> entries) {
}

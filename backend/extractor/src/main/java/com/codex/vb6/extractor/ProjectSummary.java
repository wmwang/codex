package com.codex.vb6.extractor;

import com.codex.vb6.parser.VbpProject;

import java.util.List;

public record ProjectSummary(String vbpPath,
                             VbpProject project,
                             List<EntryStatus> entries,
                             List<FrmAnalysis> forms,
                             List<BasAnalysis> modules) {
}

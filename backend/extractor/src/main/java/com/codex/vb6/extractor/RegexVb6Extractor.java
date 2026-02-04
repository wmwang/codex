package com.codex.vb6.extractor;

import java.io.IOException;
import java.nio.file.Path;

public final class RegexVb6Extractor implements Vb6Extractor {
    @Override
    public FrmAnalysis analyzeForm(Path frmPath) throws IOException {
        return FrmExtractor.analyze(frmPath);
    }

    @Override
    public BasAnalysis analyzeModule(Path basPath) throws IOException {
        return BasExtractor.analyze(basPath);
    }
}

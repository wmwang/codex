package com.codex.vb6.extractor;

import java.io.IOException;
import java.nio.file.Path;

public interface Vb6Extractor {
    FrmAnalysis analyzeForm(Path frmPath) throws IOException;

    BasAnalysis analyzeModule(Path basPath) throws IOException;
}

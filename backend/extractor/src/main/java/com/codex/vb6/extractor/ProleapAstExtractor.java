package com.codex.vb6.extractor;

import java.io.IOException;
import java.nio.file.Path;

public final class ProleapAstExtractor implements Vb6Extractor {
    private static final String[] REQUIRED_CLASSES = {
            "com.proleap.vb6.VB6Lexer",
            "com.proleap.vb6.VB6Parser"
    };

    private final RegexVb6Extractor fallback = new RegexVb6Extractor();

    @Override
    public FrmAnalysis analyzeForm(Path frmPath) throws IOException {
        ensureAstAvailable();
        return fallback.analyzeForm(frmPath);
    }

    @Override
    public BasAnalysis analyzeModule(Path basPath) throws IOException {
        ensureAstAvailable();
        return fallback.analyzeModule(basPath);
    }

    private void ensureAstAvailable() {
        for (String className : REQUIRED_CLASSES) {
            if (!isClassAvailable(className)) {
                throw new IllegalStateException(
                        "Proleap VB6 parser classes not found on the classpath. " +
                                "Please vendor or add proleap-vb6-parser before using AST mode."
                );
            }
        }
    }

    private boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, ProleapAstExtractor.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}

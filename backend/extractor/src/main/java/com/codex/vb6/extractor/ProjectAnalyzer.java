package com.codex.vb6.extractor;

import com.codex.vb6.parser.VbpEntry;
import com.codex.vb6.parser.VbpEntryType;
import com.codex.vb6.parser.VbpIndex;
import com.codex.vb6.parser.VbpProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProjectAnalyzer {
    private ProjectAnalyzer() {
    }

    public static ProjectAnalysis analyze(Path rootPath) throws IOException {
        return analyze(rootPath, new RegexVb6Extractor());
    }

    public static ProjectAnalysis analyze(Path rootPath, Vb6Extractor extractor) throws IOException {
        List<Path> vbpFiles;
        try (Stream<Path> stream = Files.walk(rootPath)) {
            vbpFiles = stream
                    .filter(path -> path.toString().toLowerCase().endsWith(".vbp"))
                    .sorted()
                    .collect(Collectors.toList());
        }

        List<ProjectSummary> projects = new ArrayList<>();
        for (Path vbpPath : vbpFiles) {
            VbpProject project = VbpIndex.parse(vbpPath);
            Path vbpDir = vbpPath.getParent();

            List<EntryStatus> entries = new ArrayList<>();
            List<FrmAnalysis> forms = new ArrayList<>();
            List<BasAnalysis> modules = new ArrayList<>();

            for (VbpEntry entry : project.entries()) {
                Path resolved = vbpDir.resolve(entry.path()).normalize();
                boolean exists = Files.exists(resolved);
                entries.add(new EntryStatus(entry, resolved.toString(), exists));

                if (!exists) {
                    continue;
                }

                if (entry.type() == VbpEntryType.FORM && entry.path().toLowerCase().endsWith(".frm")) {
                    forms.add(extractor.analyzeForm(resolved));
                } else if (entry.type() == VbpEntryType.MODULE && entry.path().toLowerCase().endsWith(".bas")) {
                    modules.add(extractor.analyzeModule(resolved));
                }
            }

            forms.sort(Comparator.comparing(FrmAnalysis::formName, Comparator.nullsLast(String::compareTo)));
            modules.sort(Comparator.comparing(BasAnalysis::moduleName, Comparator.nullsLast(String::compareTo)));
            entries.sort(Comparator.comparing(status -> status.entry().path()));

            projects.add(new ProjectSummary(vbpPath.toString(), project, List.copyOf(entries),
                    List.copyOf(forms), List.copyOf(modules)));
        }

        projects.sort(Comparator.comparing(ProjectSummary::vbpPath));
        return new ProjectAnalysis(List.copyOf(projects));
    }
}

package com.codex.vb6.graph;

import com.codex.vb6.extractor.BasAnalysis;
import com.codex.vb6.extractor.BasCall;
import com.codex.vb6.extractor.BasRoutine;
import com.codex.vb6.extractor.FrmAnalysis;
import com.codex.vb6.extractor.FrmCall;
import com.codex.vb6.extractor.FrmEvent;
import com.codex.vb6.extractor.ProjectAnalysis;
import com.codex.vb6.extractor.ProjectSummary;

public final class GraphBuilder {
    private GraphBuilder() {
    }

    public static GraphModel build(ProjectAnalysis analysis) {
        GraphModel model = new GraphModel();

        for (ProjectSummary project : analysis.projects()) {
            String projectId = "project:" + nullSafe(project.project().name(), project.vbpPath());
            model.addNode(new GraphNode(projectId, GraphNodeType.PROJECT, nullSafe(project.project().name(), "(project)")));

            for (FrmAnalysis form : project.forms()) {
                String formId = projectId + ":form:" + nullSafe(form.formName(), "unknown");
                model.addNode(new GraphNode(formId, GraphNodeType.FORM, nullSafe(form.formName(), "(form)")));
                model.addEdge(new GraphEdge(projectId, formId, "HAS_FORM"));

                for (FrmEvent event : form.events()) {
                    String eventId = formId + ":event:" + event.name();
                    model.addNode(new GraphNode(eventId, GraphNodeType.EVENT, event.name()));
                    model.addEdge(new GraphEdge(formId, eventId, "HAS_EVENT"));

                    for (FrmCall call : event.calls()) {
                        String targetId = projectId + ":call:" + call.target();
                        model.addNode(new GraphNode(targetId, GraphNodeType.CALL_TARGET, call.target()));
                        model.addEdge(new GraphEdge(eventId, targetId, call.type()));
                    }
                }
            }

            for (BasAnalysis module : project.modules()) {
                String moduleId = projectId + ":module:" + nullSafe(module.moduleName(), "unknown");
                model.addNode(new GraphNode(moduleId, GraphNodeType.MODULE, nullSafe(module.moduleName(), "(module)")));
                model.addEdge(new GraphEdge(projectId, moduleId, "HAS_MODULE"));

                for (BasRoutine routine : module.routines()) {
                    String routineId = moduleId + ":routine:" + routine.name();
                    model.addNode(new GraphNode(routineId, GraphNodeType.ROUTINE, routine.name()));
                    model.addEdge(new GraphEdge(moduleId, routineId, routine.kind()));

                    for (BasCall call : routine.calls()) {
                        String targetId = projectId + ":call:" + call.target();
                        model.addNode(new GraphNode(targetId, GraphNodeType.CALL_TARGET, call.target()));
                        model.addEdge(new GraphEdge(routineId, targetId, call.type()));
                    }
                }
            }
        }

        return model;
    }

    private static String nullSafe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

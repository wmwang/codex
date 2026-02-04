package com.codex.vb6.graph;

public final class MermaidRenderer {
    private MermaidRenderer() {
    }

    public static String render(GraphModel model) {
        StringBuilder builder = new StringBuilder();
        builder.append("```mermaid\n");
        builder.append("graph TD\n");

        for (GraphEdge edge : model.edges()) {
            builder.append("  ").append(sanitize(edge.fromId())).append(" -->|")
                    .append(edge.label()).append("| ")
                    .append(sanitize(edge.toId())).append("\n");
        }

        builder.append("```\n");
        return builder.toString();
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }
}

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
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
                    || (ch >= '0' && ch <= '9') || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }
}

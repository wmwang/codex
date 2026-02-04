package com.codex.vb6.graph;

import java.util.ArrayList;
import java.util.List;

public final class GraphModel {
    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public void addNode(GraphNode node) {
        nodes.add(node);
    }

    public void addEdge(GraphEdge edge) {
        edges.add(edge);
    }

    public List<GraphNode> nodes() {
        return List.copyOf(nodes);
    }

    public List<GraphEdge> edges() {
        return List.copyOf(edges);
    }
}

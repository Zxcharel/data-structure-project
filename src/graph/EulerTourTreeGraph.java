package src.graph;

import java.util.*;

/**
 * Adapter implementation named after Euler Tour Trees.
 * NOTE: Euler Tour Trees maintain dynamic forests; our route graph is a
 * general directed graph with cycles. This class delegates to an
 * AdjacencyListGraph to satisfy the Graph API while providing a placeholder
 * for future tree-specific operations where applicable.
 */
public class EulerTourTreeGraph implements Graph {
    private final AdjacencyListGraph delegate;

    public EulerTourTreeGraph() {
        this.delegate = new AdjacencyListGraph();
    }

    @Override
    public void addNode(String node) { delegate.addNode(node); }

    @Override
    public void addEdge(String from, Edge edge) { delegate.addEdge(from, edge); }

    @Override
    public List<String> nodes() { return delegate.nodes(); }

    @Override
    public List<Edge> neighbors(String node) { return delegate.neighbors(node); }

    @Override
    public boolean hasNode(String node) { return delegate.hasNode(node); }

    @Override
    public int edgeCount() { return delegate.edgeCount(); }

    @Override
    public int nodeCount() { return delegate.nodeCount(); }

    public String getStats() { return delegate.getStats(); }

    @Override
    public String toString() {
        return "EulerTourTreeGraph(delegate=" + delegate.toString() + ")";
    }
}



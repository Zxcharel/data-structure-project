package src.graph;

import java.util.*;

/**
 * Adapter implementation named after Link-Cut Trees.
 * NOTE: True Link-Cut Trees represent dynamic forests (acyclic). Our flight graph
 * is a general directed graph with cycles, so a full link-cut structure is not
 * applicable here. This class delegates to an AdjacencyListGraph while exposing
 * the same Graph API, providing a placeholder for future specialized usage on trees.
 */
public class LinkCutTreeGraph implements Graph {
    private final AdjacencyListGraph delegate;

    public LinkCutTreeGraph() {
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
        return "LinkCutTreeGraph(delegate=" + delegate.toString() + ")";
    }
}



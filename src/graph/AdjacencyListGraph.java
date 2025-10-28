package graph;

import java.util.*;

/**
 * Adjacency list implementation of a directed graph.
 * Uses HashMap for O(1) node lookup and ArrayList for edge storage.
 */
public class AdjacencyListGraph implements Graph {
    private final Map<String, List<Edge>> adjacencyList;
    private int edgeCount;
    
    public AdjacencyListGraph() {
        this.adjacencyList = new HashMap<>();
        this.edgeCount = 0;
    }
    
    @Override
    public void addNode(String node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }
    
    @Override
    public void addEdge(String from, Edge edge) {
        // Ensure both nodes exist
        addNode(from);
        addNode(edge.getDestination());
        
        // Add edge to adjacency list
        adjacencyList.get(from).add(edge);
        edgeCount++;
    }
    
    @Override
    public List<String> nodes() {
        return new ArrayList<>(adjacencyList.keySet());
    }
    
    @Override
    public List<Edge> neighbors(String node) {
        List<Edge> edges = adjacencyList.get(node);
        return edges != null ? new ArrayList<>(edges) : new ArrayList<>();
    }
    
    @Override
    public boolean hasNode(String node) {
        return adjacencyList.containsKey(node);
    }
    
    @Override
    public int edgeCount() {
        return edgeCount;
    }
    
    @Override
    public int nodeCount() {
        return adjacencyList.size();
    }
    
    /**
     * Gets all edges in the graph (for debugging/analysis)
     */
    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (List<Edge> edges : adjacencyList.values()) {
            allEdges.addAll(edges);
        }
        return allEdges;
    }
    
    /**
     * Gets statistics about the graph
     */
    public String getStats() {
        int totalEdges = edgeCount();
        int totalNodes = nodeCount();
        double avgEdgesPerNode = totalNodes > 0 ? (double) totalEdges / totalNodes : 0;
        
        return String.format("Graph Stats: %d nodes, %d edges, %.2f avg edges per node",
                totalNodes, totalEdges, avgEdgesPerNode);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AdjacencyListGraph{\n");
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ");
            for (Edge edge : entry.getValue()) {
                sb.append(edge.getDestination()).append("(").append(edge.getAirline()).append(") ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}

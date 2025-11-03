package src.graph;

import java.util.*;

/**
 * Sorted adjacency list implementation of a directed graph.
 * 
 * Maintains edges sorted by weight (lowest first) for each node.
 * This can improve Dijkstra's algorithm performance by accessing
 * lowest-weight edges first, and enables binary search for edge lookups.
 * 
 * Key differences from AdjacencyListGraph:
 * - Edges are maintained in sorted order (by weight)
 * - O(log n) insertion per edge (to maintain sorted order)
 * - O(log n) lookup for specific weight ranges
 * - Potentially faster for algorithms that process edges in weight order
 */
public class SortedAdjacencyListGraph implements Graph {
    private final Map<String, List<Edge>> adjacencyList;
    private int edgeCount;
    
    // Comparator to sort edges by weight (ascending), then by destination for ties
    private static final Comparator<Edge> WEIGHT_COMPARATOR = (e1, e2) -> {
        int weightCompare = Double.compare(e1.getWeight(), e2.getWeight());
        if (weightCompare != 0) {
            return weightCompare;
        }
        // If weights are equal, sort by destination for consistency
        return e1.getDestination().compareTo(e2.getDestination());
    };
    
    public SortedAdjacencyListGraph() {
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
        
        // Get the list for this node
        List<Edge> edges = adjacencyList.get(from);
        
        // Binary search to find insertion point to maintain sorted order
        int insertionIndex = Collections.binarySearch(edges, edge, WEIGHT_COMPARATOR);
        
        // If not found, Collections.binarySearch returns -(insertionPoint) - 1
        if (insertionIndex < 0) {
            insertionIndex = -(insertionIndex + 1);
        }
        
        // Insert at the correct position to maintain sorted order
        edges.add(insertionIndex, edge);
        edgeCount++;
    }
    
    @Override
    public List<String> nodes() {
        return new ArrayList<>(adjacencyList.keySet());
    }
    
    @Override
    public List<Edge> neighbors(String node) {
        List<Edge> edges = adjacencyList.get(node);
        if (edges != null) {
            // Return a copy to maintain immutability
            return new ArrayList<>(edges);
        }
        return new ArrayList<>();
    }
    
    /**
     * Gets neighbors sorted by weight (lowest first) - already sorted, just returns them
     * This is the default behavior, but provided for clarity.
     */
    public List<Edge> getNeighborsSortedByWeight(String node) {
        return neighbors(node); // Already sorted
    }
    
    /**
     * Finds edges with weight less than or equal to a threshold using binary search.
     * 
     * @param node The source node
     * @param maxWeight Maximum weight to search for
     * @return List of edges with weight <= maxWeight, sorted by weight
     */
    public List<Edge> getNeighborsWithWeightAtMost(String node, double maxWeight) {
        List<Edge> edges = adjacencyList.get(node);
        if (edges == null || edges.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Create a dummy edge with the max weight for comparison
        Edge dummyEdge = new Edge("", "", 0, 0, 0, 0, 0, maxWeight);
        
        // Binary search for insertion point (would be after all edges with weight <= maxWeight)
        int index = Collections.binarySearch(edges, dummyEdge, WEIGHT_COMPARATOR);
        
        if (index < 0) {
            index = -(index + 1);
        }
        
        // Return all edges up to this index (they all have weight <= maxWeight)
        return new ArrayList<>(edges.subList(0, index));
    }
    
    /**
     * Checks if the neighbors list is properly sorted (for validation).
     * 
     * @param node The node to check
     * @return true if sorted, false otherwise
     */
    public boolean isSorted(String node) {
        List<Edge> edges = adjacencyList.get(node);
        if (edges == null || edges.size() <= 1) {
            return true;
        }
        
        for (int i = 1; i < edges.size(); i++) {
            if (WEIGHT_COMPARATOR.compare(edges.get(i - 1), edges.get(i)) > 0) {
                return false;
            }
        }
        return true;
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
        
        return String.format("SortedAdjacencyListGraph: %d nodes, %d edges, %.2f avg edges per node",
                totalNodes, totalEdges, avgEdgesPerNode);
    }
    
    /**
     * Validates that all neighbor lists are properly sorted.
     * Useful for debugging and testing.
     * 
     * @return true if all lists are sorted, false otherwise
     */
    public boolean validateSorting() {
        for (String node : adjacencyList.keySet()) {
            if (!isSorted(node)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SortedAdjacencyListGraph{\n");
        for (Map.Entry<String, List<Edge>> entry : adjacencyList.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ");
            for (Edge edge : entry.getValue()) {
                sb.append(edge.getDestination())
                  .append("(w:")
                  .append(String.format("%.2f", edge.getWeight()))
                  .append(") ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}


package src.graph;

import java.util.*;

/**
 * Matrix-based graph implementation for comparison with adjacency list.
 * Uses a 2D array to store edge weights and airline information.
 */
public class MatrixGraph implements Graph {
    private final Map<String, Integer> nodeToIndex;
    private final List<String> indexToNode;
    private final double[][] weights;
    private final String[][] airlines;
    private int edgeCount;
    
    public MatrixGraph(int maxNodes) {
        this.nodeToIndex = new HashMap<>();
        this.indexToNode = new ArrayList<>();
        this.weights = new double[maxNodes][maxNodes];
        this.airlines = new String[maxNodes][maxNodes];
        this.edgeCount = 0;
        
        // Initialize with infinity (no edge)
        for (int i = 0; i < maxNodes; i++) {
            Arrays.fill(weights[i], Double.MAX_VALUE);
        }
    }
    
    @Override
    public void addNode(String node) {
        if (!nodeToIndex.containsKey(node)) {
            int index = indexToNode.size();
            nodeToIndex.put(node, index);
            indexToNode.add(node);
        }
    }
    
    @Override
    public void addEdge(String from, Edge edge) {
        addNode(from);
        addNode(edge.getDestination());
        
        int fromIndex = nodeToIndex.get(from);
        int toIndex = nodeToIndex.get(edge.getDestination());
        
        weights[fromIndex][toIndex] = edge.getWeight();
        airlines[fromIndex][toIndex] = edge.getAirline();
        edgeCount++;
    }
    
    @Override
    public List<String> nodes() {
        return new ArrayList<>(indexToNode);
    }
    
    @Override
    public List<Edge> neighbors(String node) {
        List<Edge> neighbors = new ArrayList<>();
        Integer nodeIndex = nodeToIndex.get(node);
        
        if (nodeIndex != null) {
            for (int i = 0; i < indexToNode.size(); i++) {
                if (weights[nodeIndex][i] != Double.MAX_VALUE) {
                    String destination = indexToNode.get(i);
                    String airline = airlines[nodeIndex][i];
                    neighbors.add(new Edge(destination, airline, 0, 0, 0, 0, 0, weights[nodeIndex][i]));
                }
            }
        }
        
        return neighbors;
    }
    
    @Override
    public boolean hasNode(String node) {
        return nodeToIndex.containsKey(node);
    }
    
    @Override
    public int edgeCount() {
        return edgeCount;
    }
    
    @Override
    public int nodeCount() {
        return indexToNode.size();
    }
    
    /**
     * Gets the weight between two nodes directly
     */
    public double getWeight(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);
        Integer toIndex = nodeToIndex.get(to);
        
        if (fromIndex != null && toIndex != null) {
            return weights[fromIndex][toIndex];
        }
        
        return Double.MAX_VALUE;
    }
    
    /**
     * Gets the airline between two nodes directly
     */
    public String getAirline(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);
        Integer toIndex = nodeToIndex.get(to);
        
        if (fromIndex != null && toIndex != null) {
            return airlines[fromIndex][toIndex];
        }
        
        return null;
    }
    
    /**
     * Gets memory usage estimate
     */
    public long getMemoryUsage() {
        return (long) weights.length * weights.length * 8 + // weights array
               (long) airlines.length * airlines.length * 4 + // airlines array (rough estimate)
               (long) nodeToIndex.size() * 50; // HashMap overhead
    }
    
    @Override
    public String toString() {
        return String.format("MatrixGraph{nodes=%d, edges=%d, memory=%d bytes}",
                           nodeCount(), edgeCount(), getMemoryUsage());
    }
}

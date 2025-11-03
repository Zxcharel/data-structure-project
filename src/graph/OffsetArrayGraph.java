package src.graph;

import java.util.*;

/**
 * Offset-based segmented array graph implementation (CSR-style).
 * Uses a single contiguous array to store all edges with offset pointers
 * indicating where each node's edges begin. Provides excellent cache
 * performance for pathfinding algorithms.
 */
public class OffsetArrayGraph implements Graph {
    private final Map<String, Integer> nodeToIndex;
    private final List<String> indexToNode;
    
    // CSR structure (built after edges are added)
    private Edge[] edgeArray;           // Single contiguous array of all edges
    private int[] edgeOffsets;          // Starting index in edgeArray for each node
    private int[] edgeCounts;           // Number of edges for each node
    
    // Temporary storage during building
    private List<List<Edge>> tempEdges;  // Temporary per-node edge lists
    private int edgeCount;
    private boolean finalized;
    
    public OffsetArrayGraph() {
        this.nodeToIndex = new HashMap<>();
        this.indexToNode = new ArrayList<>();
        this.tempEdges = new ArrayList<>();
        this.edgeCount = 0;
        this.finalized = false;
    }
    
    @Override
    public void addNode(String node) {
        if (!nodeToIndex.containsKey(node)) {
            int index = indexToNode.size();
            nodeToIndex.put(node, index);
            indexToNode.add(node);
            
            // Ensure tempEdges list is large enough
            while (tempEdges.size() <= index) {
                tempEdges.add(new ArrayList<>());
            }
        }
    }
    
    @Override
    public void addEdge(String from, Edge edge) {
        addNode(from);
        addNode(edge.getDestination());
        
        int fromIndex = nodeToIndex.get(from);
        
        // If finalized, we need to rebuild (or defer to temp storage)
        if (finalized) {
            // Reset finalized state and rebuild on next neighbors() call
            finalized = false;
        }
        
        // Add to temporary storage
        tempEdges.get(fromIndex).add(edge);
        edgeCount++;
    }
    
    /**
     * Builds the CSR structure from temporary storage.
     * Called automatically on first neighbors() access, or can be called explicitly.
     */
    private void buildCSR() {
        if (finalized) {
            return; // Already built
        }
        
        int numNodes = indexToNode.size();
        edgeOffsets = new int[numNodes];
        edgeCounts = new int[numNodes];
        
        // Calculate offsets and counts
        int totalEdges = 0;
        for (int i = 0; i < numNodes; i++) {
            edgeOffsets[i] = totalEdges;
            edgeCounts[i] = tempEdges.get(i).size();
            totalEdges += edgeCounts[i];
        }
        
        // Build the single contiguous edge array
        edgeArray = new Edge[totalEdges];
        int arrayIndex = 0;
        for (int i = 0; i < numNodes; i++) {
            List<Edge> edges = tempEdges.get(i);
            for (Edge edge : edges) {
                edgeArray[arrayIndex++] = edge;
            }
        }
        
        finalized = true;
    }
    
    @Override
    public List<String> nodes() {
        return new ArrayList<>(indexToNode);
    }
    
    @Override
    public List<Edge> neighbors(String node) {
        Integer nodeIndex = nodeToIndex.get(node);
        
        if (nodeIndex == null) {
            return new ArrayList<>();
        }
        
        // Build CSR structure if not yet built
        if (!finalized) {
            buildCSR();
        }
        
        // Fast contiguous access using offsets
        int start = edgeOffsets[nodeIndex];
        int count = edgeCounts[nodeIndex];
        
        List<Edge> neighbors = new ArrayList<>(count);
        for (int i = start; i < start + count; i++) {
            neighbors.add(edgeArray[i]);
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
     * Gets the weight between two nodes directly (if edge exists)
     */
    public double getWeight(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);
        
        if (fromIndex == null) {
            return Double.MAX_VALUE;
        }
        
        if (!finalized) {
            buildCSR();
        }
        
        int start = edgeOffsets[fromIndex];
        int count = edgeCounts[fromIndex];
        
        for (int i = start; i < start + count; i++) {
            if (edgeArray[i].getDestination().equals(to)) {
                return edgeArray[i].getWeight();
            }
        }
        
        return Double.MAX_VALUE;
    }
    
    /**
     * Gets the airline between two nodes directly (if edge exists)
     */
    public String getAirline(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);
        
        if (fromIndex == null) {
            return null;
        }
        
        if (!finalized) {
            buildCSR();
        }
        
        int start = edgeOffsets[fromIndex];
        int count = edgeCounts[fromIndex];
        
        for (int i = start; i < start + count; i++) {
            if (edgeArray[i].getDestination().equals(to)) {
                return edgeArray[i].getAirline();
            }
        }
        
        return null;
    }
    
    /**
     * Gets memory usage estimate
     */
    public long getMemoryUsage() {
        if (!finalized) {
            buildCSR();
        }
        
        // Memory for edge array
        long edgeArrayMemory = edgeArray != null ? (long) edgeArray.length * 200 : 0; // Rough estimate per Edge object
        
        // Memory for offset and count arrays
        long arrayMemory = edgeOffsets != null ? (long) edgeOffsets.length * 4 : 0; // 4 bytes per int
        long countMemory = edgeCounts != null ? (long) edgeCounts.length * 4 : 0;
        
        // Memory for node mapping
        long nodeMappingMemory = (long) nodeToIndex.size() * 50; // HashMap overhead
        
        // Temporary storage (before finalization)
        long tempMemory = 0;
        if (tempEdges != null) {
            for (List<Edge> edges : tempEdges) {
                tempMemory += edges.size() * 200; // Rough estimate
            }
        }
        
        return edgeArrayMemory + arrayMemory + countMemory + nodeMappingMemory + tempMemory;
    }
    
    /**
     * Forces finalization of the CSR structure (optimization)
     * Call this method after adding all edges to optimize performance.
     */
    public void finalizeCSR() {
        if (!finalized) {
            buildCSR();
        }
    }
    
    /**
     * Gets the total size of the edge array
     */
    public int getEdgeArraySize() {
        if (!finalized) {
            buildCSR();
        }
        return edgeArray != null ? edgeArray.length : 0;
    }
    
    /**
     * Gets the average edges per node
     */
    public double getAverageEdgesPerNode() {
        int nodes = nodeCount();
        return nodes > 0 ? (double) edgeCount / nodes : 0;
    }
    
    @Override
    public String toString() {
        if (!finalized) {
            buildCSR();
        }
        
        return String.format(
                "OffsetArrayGraph{nodes=%d, edges=%d, memory=%d bytes, array_size=%d, avg_edges_per_node=%.2f}",
                nodeCount(), edgeCount(), getMemoryUsage(), getEdgeArraySize(), getAverageEdgesPerNode());
    }
}


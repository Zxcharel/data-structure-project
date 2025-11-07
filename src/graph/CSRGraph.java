package src.graph;

import java.util.*;

/**
 * Compressed Sparse Row (CSR) graph implementation.
 * 
 * Stores the graph using three arrays for memory efficiency:
 * - rowPtr: Starting index of edges for each node (cumulative edge offsets)
 * - edgeDests: Destination nodes for all edges
 * - edgeWeights: Weights for all edges
 * - edgeAirlines: Airlines for all edges
 * 
 * This format is optimal for sparse graphs and provides:
 * - O(V + E) space complexity (vs O(V²) for dense matrix)
 * - Cache-friendly sequential array access
 * - Fast neighbor iteration with minimal memory overhead
 * 
 * Used in high-performance graph libraries and scientific computing.
 */
public class CSRGraph implements Graph {
    private final Map<String, Integer> nodeToIndex;
    private final List<String> indexToNode;
    
    // CSR arrays
    private int[] rowPtr;           // Cumulative offsets: rowPtr[i] = starting index of edges for node i
    private String[] edgeDests;     // All edge destinations
    private double[] edgeWeights;   // All edge weights
    private String[] edgeAirlines;  // All airline names
    private Edge[] edges;           // Cached edge objects for zero-copy neighbor views
    private List<Edge>[] neighborViews; // Pre-built neighbor list views per node
    
    private int numNodes;
    private int numEdges;
    
    /**
     * Creates a CSR graph from an existing graph.
     * This is the recommended way to construct a CSR graph.
     * 
     * @param sourceGraph The graph to compress
     */
    public CSRGraph(Graph sourceGraph) {
        List<String> nodes = sourceGraph.nodes();
        numNodes = nodes.size();
        numEdges = sourceGraph.edgeCount();
        
        // Initialize node mappings
        nodeToIndex = new HashMap<>(numNodes);
        indexToNode = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            String node = nodes.get(i);
            nodeToIndex.put(node, i);
            indexToNode.add(node);
        }
        
        // Build CSR arrays
        buildCSR(sourceGraph);
    }
    
    /**
     * Creates an empty CSR graph with pre-allocated capacity.
     * Note: This is less efficient than constructing from an existing graph.
     * 
     * @param maxNodes Maximum number of nodes
     * @param maxEdges Maximum number of edges
     */
    public CSRGraph(int maxNodes, int maxEdges) {
        this.nodeToIndex = new HashMap<>(maxNodes);
        this.indexToNode = new ArrayList<>(maxNodes);
        this.rowPtr = new int[maxNodes + 1];
        this.edgeDests = new String[maxEdges];
        this.edgeWeights = new double[maxEdges];
        this.edgeAirlines = new String[maxEdges];
        this.numNodes = 0;
        this.numEdges = 0;
    }
    
    /**
     * Builds the CSR representation from a graph.
     */
    private void buildCSR(Graph graph) {
        // First pass: count edges per node
        int[] edgeCounts = new int[numNodes];
        for (String node : graph.nodes()) {
            int nodeIndex = nodeToIndex.get(node);
            edgeCounts[nodeIndex] = graph.neighbors(node).size();
        }
        
        // Build rowPtr (cumulative offsets)
        rowPtr = new int[numNodes + 1];
        for (int i = 0; i < numNodes; i++) {
            rowPtr[i + 1] = rowPtr[i] + edgeCounts[i];
        }
        
        // Allocate arrays
        edgeDests = new String[numEdges];
        edgeWeights = new double[numEdges];
        edgeAirlines = new String[numEdges];
        edges = new Edge[numEdges];
        @SuppressWarnings("unchecked")
        List<Edge>[] views = (List<Edge>[]) new List<?>[numNodes];
        neighborViews = views;
        
        // Second pass: fill edge arrays
        for (String node : graph.nodes()) {
            int nodeIndex = nodeToIndex.get(node);
            int edgeStart = rowPtr[nodeIndex];
            
            List<Edge> neighbors = graph.neighbors(node);
            int neighborCount = neighbors.size();
            for (int i = 0; i < neighborCount; i++) {
                Edge edge = neighbors.get(i);
                int targetIndex = edgeStart + i;
                edgeDests[targetIndex] = edge.getDestination();
                edgeWeights[targetIndex] = edge.getWeight();
                edgeAirlines[targetIndex] = edge.getAirline();
                edges[targetIndex] = edge;
            }

            neighborViews[nodeIndex] = neighborCount == 0
                ? Collections.emptyList()
                : new NeighborView(edges, edgeStart, neighborCount);
        }
    }
    
    @Override
    public void addNode(String node) {
        if (!nodeToIndex.containsKey(node)) {
            nodeToIndex.put(node, numNodes);
            indexToNode.add(node);
            numNodes++;
            
            // Note: This invalidates rowPtr array. CSR graphs should be built from complete data.
            // For dynamic additions, consider using a builder pattern.
        }
    }
    
    @Override
    public void addEdge(String from, Edge edge) {
        // Note: Adding edges to a CSR graph built from another graph is not efficient.
        // Consider rebuilding from a temporary structure instead.
        addNode(from);
        addNode(edge.getDestination());
    }
    
    @Override
    public List<String> nodes() {
        return new ArrayList<>(indexToNode);
    }
    
    @Override
    public List<Edge> neighbors(String node) {
        Integer nodeIndex = nodeToIndex.get(node);
        if (nodeIndex == null) {
            return Collections.emptyList();
        }

        List<Edge> view = neighborViews[nodeIndex];
        return view != null ? view : Collections.emptyList();
    }
    
    @Override
    public boolean hasNode(String node) {
        return nodeToIndex.containsKey(node);
    }
    
    @Override
    public int edgeCount() {
        return numEdges;
    }
    
    @Override
    public int nodeCount() {
        return numNodes;
    }
    
    /**
     * Gets the CSR arrays for direct access (advanced usage).
     * @return Array containing {rowPtr, edgeDests, edgeWeights, edgeAirlines}
     */
    public Object[] getCSRArrays() {
        return new Object[]{rowPtr, edgeDests, edgeWeights, edgeAirlines};
    }
    
    /**
     * Gets memory usage estimate.
     * @return Estimated memory in bytes
     */
    public long getMemoryUsage() {
        long memory = 0;
        
        // Arrays
        memory += (long) rowPtr.length * 4;  // int array
        memory += (long) edgeDests.length * 8;  // String references (rough estimate)
        memory += (long) edgeWeights.length * 8;  // double array
        memory += (long) edgeAirlines.length * 8;  // String references (rough estimate)
        
        // Mappings
        memory += (long) nodeToIndex.size() * 50;  // HashMap overhead
        memory += (long) indexToNode.size() * 8;  // ArrayList overhead
        
        return memory;
    }
    
    /**
     * Gets statistics about the graph.
     * @return String with statistics
     */
    public String getStats() {
        double avgDegree = numNodes > 0 ? (double) numEdges / numNodes : 0;
        return String.format(
            "CSRGraph: nodes=%d, edges=%d, avg_degree=%.2f, memory=%d bytes",
            numNodes, numEdges, avgDegree, getMemoryUsage()
        );
    }
    
    @Override
    public String toString() {
        return String.format("CSRGraph{nodes=%d, edges=%d, memory=%d bytes}",
                           numNodes, numEdges, getMemoryUsage());
    }

    private static final class NeighborView extends AbstractList<Edge> {
        private final Edge[] edges;
        private final int start;
        private final int length;

        NeighborView(Edge[] edges, int start, int length) {
            this.edges = edges;
            this.start = start;
            this.length = length;
        }

        @Override
        public Edge get(int index) {
            if (index < 0 || index >= length) {
                throw new IndexOutOfBoundsException(index);
            }
            return edges[start + index];
        }

        @Override
        public int size() {
            return length;
        }
    }
}


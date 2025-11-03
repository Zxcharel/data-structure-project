package src.graph;

import java.util.*;

/**
 * Dynamic array-based graph implementation.
 * Uses dynamically-resizing arrays for edge storage with automatic capacity
 * management. Each node maintains its own dynamic array of edges that grows
 * as needed, providing cache-friendly sequential access patterns.
 */
public class DynamicArrayGraph implements Graph {
    private final Map<String, Integer> nodeToIndex;
    private final List<String> indexToNode;
    private final List<DynamicEdgeArray> nodeEdges; // Array of dynamic arrays, one per node
    private int edgeCount;
    private static final int INITIAL_CAPACITY = 4; // Initial capacity for each node's edge array

    /**
     * Internal dynamic array class that manages edge storage for a single node.
     * Automatically resizes when capacity is exceeded.
     */
    private static class DynamicEdgeArray {
        private Edge[] edges;
        private int size;
        private int capacity;

        DynamicEdgeArray() {
            this.capacity = INITIAL_CAPACITY;
            this.edges = new Edge[capacity];
            this.size = 0;
        }

        /**
         * Adds an edge to the dynamic array, resizing if necessary
         */
        void add(Edge edge) {
            if (size >= capacity) {
                resize();
            }
            edges[size++] = edge;
        }

        /**
         * Dynamically resizes the array (doubles capacity)
         */
        private void resize() {
            capacity *= 2;
            Edge[] newEdges = new Edge[capacity];
            System.arraycopy(edges, 0, newEdges, 0, size);
            edges = newEdges;
        }

        /**
         * Gets all edges as a list
         */
        List<Edge> toList() {
            List<Edge> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(edges[i]);
            }
            return result;
        }

        /**
         * Gets the current size
         */
        int size() {
            return size;
        }

        /**
         * Gets the current capacity
         */
        int capacity() {
            return capacity;
        }

        /**
         * Gets edge at index (for iteration)
         */
        Edge get(int index) {
            if (index >= 0 && index < size) {
                return edges[index];
            }
            return null;
        }
    }

    public DynamicArrayGraph() {
        this.nodeToIndex = new HashMap<>();
        this.indexToNode = new ArrayList<>();
        this.nodeEdges = new ArrayList<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        if (!nodeToIndex.containsKey(node)) {
            int index = indexToNode.size();
            nodeToIndex.put(node, index);
            indexToNode.add(node);
            nodeEdges.add(new DynamicEdgeArray());
        }
    }

    @Override
    public void addEdge(String from, Edge edge) {
        addNode(from);
        addNode(edge.getDestination());

        int fromIndex = nodeToIndex.get(from);
        nodeEdges.get(fromIndex).add(edge);
        edgeCount++;
    }

    @Override
    public List<String> nodes() {
        return new ArrayList<>(indexToNode);
    }

    @Override
    public List<Edge> neighbors(String node) {
        Integer nodeIndex = nodeToIndex.get(node);

        if (nodeIndex != null && nodeIndex < nodeEdges.size()) {
            return nodeEdges.get(nodeIndex).toList();
        }

        return new ArrayList<>();
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

        if (fromIndex != null && fromIndex < nodeEdges.size()) {
            DynamicEdgeArray edgeArray = nodeEdges.get(fromIndex);
            for (int i = 0; i < edgeArray.size(); i++) {
                Edge edge = edgeArray.get(i);
                if (edge != null && edge.getDestination().equals(to)) {
                    return edge.getWeight();
                }
            }
        }

        return Double.MAX_VALUE;
    }

    /**
     * Gets the airline between two nodes directly (if edge exists)
     */
    public String getAirline(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);

        if (fromIndex != null && fromIndex < nodeEdges.size()) {
            DynamicEdgeArray edgeArray = nodeEdges.get(fromIndex);
            for (int i = 0; i < edgeArray.size(); i++) {
                Edge edge = edgeArray.get(i);
                if (edge != null && edge.getDestination().equals(to)) {
                    return edge.getAirline();
                }
            }
        }

        return null;
    }

    /**
     * Gets memory usage estimate
     */
    public long getMemoryUsage() {
        long baseMemory = 1000; // Base object overhead
        long nodeMappingMemory = nodeToIndex.size() * 50; // HashMap overhead

        // Calculate memory for dynamic arrays
        long edgeArrayMemory = 0;
        for (DynamicEdgeArray edgeArray : nodeEdges) {
            // Array overhead + Edge references
            edgeArrayMemory += edgeArray.capacity() * 8; // 8 bytes per reference (64-bit)
            edgeArrayMemory += edgeArray.size() * 100; // Rough estimate for Edge objects
        }

        return baseMemory + nodeMappingMemory + edgeArrayMemory;
    }

    /**
     * Gets total capacity across all dynamic arrays
     */
    public long getTotalCapacity() {
        long totalCapacity = 0;
        for (DynamicEdgeArray edgeArray : nodeEdges) {
            totalCapacity += edgeArray.capacity();
        }
        return totalCapacity;
    }

    /**
     * Gets average capacity per node
     */
    public double getAverageCapacity() {
        if (nodeEdges.isEmpty()) {
            return 0;
        }
        return (double) getTotalCapacity() / nodeEdges.size();
    }

    /**
     * Gets capacity utilization (used capacity vs total capacity)
     */
    public double getCapacityUtilization() {
        long totalCapacity = getTotalCapacity();
        return totalCapacity > 0 ? (double) edgeCount / totalCapacity : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "DynamicArrayGraph{nodes=%d, edges=%d, memory=%d bytes, total_capacity=%d, utilization=%.2f%%}",
                nodeCount(), edgeCount(), getMemoryUsage(), getTotalCapacity(),
                getCapacityUtilization() * 100);
    }
}


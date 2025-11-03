package src.graph;

import java.util.*;

/**
 * Linear array-based graph implementation for comparison with other
 * implementations.
 * Uses a flat array to store edges with node mappings for efficient access.
 */
public class LinearArrayGraph implements Graph {
    private final Map<String, Integer> nodeToIndex;
    private final List<String> indexToNode;
    private final List<EdgeEntry> edges; // Linear array of edges
    private final Map<Integer, List<Integer>> nodeToEdgeIndices; // Maps node index to edge indices in the array
    private int edgeCount;

    /**
     * Internal class to store edge information in the linear array
     */
    private static class EdgeEntry {
        final Edge edge;

        EdgeEntry(Edge edge) {
            this.edge = edge;
        }
    }

    public LinearArrayGraph() {
        this.nodeToIndex = new HashMap<>();
        this.indexToNode = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nodeToEdgeIndices = new HashMap<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        if (!nodeToIndex.containsKey(node)) {
            int index = indexToNode.size();
            nodeToIndex.put(node, index);
            indexToNode.add(node);
            nodeToEdgeIndices.put(index, new ArrayList<>());
        }
    }

    @Override
    public void addEdge(String from, Edge edge) {
        addNode(from);
        addNode(edge.getDestination());

        int fromIndex = nodeToIndex.get(from);

        // Add edge to linear array
        int edgeIndex = edges.size();
        edges.add(new EdgeEntry(edge));

        // Map from node to edge index
        nodeToEdgeIndices.get(fromIndex).add(edgeIndex);

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
            List<Integer> edgeIndices = nodeToEdgeIndices.get(nodeIndex);
            if (edgeIndices != null) {
                for (Integer edgeIndex : edgeIndices) {
                    neighbors.add(edges.get(edgeIndex).edge);
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
     * Gets the weight between two nodes directly (if edge exists)
     */
    public double getWeight(String from, String to) {
        Integer fromIndex = nodeToIndex.get(from);
        Integer toIndex = nodeToIndex.get(to);

        if (fromIndex != null && toIndex != null) {
            List<Integer> edgeIndices = nodeToEdgeIndices.get(fromIndex);
            if (edgeIndices != null) {
                for (Integer edgeIndex : edgeIndices) {
                    EdgeEntry entry = edges.get(edgeIndex);
                    if (entry.edge.getDestination().equals(to)) {
                        return entry.edge.getWeight();
                    }
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
        Integer toIndex = nodeToIndex.get(to);

        if (fromIndex != null && toIndex != null) {
            List<Integer> edgeIndices = nodeToEdgeIndices.get(fromIndex);
            if (edgeIndices != null) {
                for (Integer edgeIndex : edgeIndices) {
                    EdgeEntry entry = edges.get(edgeIndex);
                    if (entry.edge.getDestination().equals(to)) {
                        return entry.edge.getAirline();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Gets memory usage estimate
     */
    public long getMemoryUsage() {
        // Estimate memory: edges array + node mappings + edge index mappings
        long edgeMemory = edges.size() * 32; // EdgeEntry objects + Edge objects
        long nodeMemory = nodeToIndex.size() * 50; // HashMap overhead
        long indexMemory = nodeToEdgeIndices.size() * 40; // HashMap + ArrayList overhead

        return edgeMemory + nodeMemory + indexMemory;
    }

    /**
     * Gets the internal edges array (for analysis)
     */
    public List<EdgeEntry> getEdgesArray() {
        return new ArrayList<>(edges);
    }

    /**
     * Converts to array format (linear array of edge objects)
     */
    public Edge[] toEdgeArray() {
        Edge[] edgeArray = new Edge[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            edgeArray[i] = edges.get(i).edge;
        }
        return edgeArray;
    }

    @Override
    public String toString() {
        return String.format("LinearArrayGraph{nodes=%d, edges=%d, memory=%d bytes, array_size=%d}",
                nodeCount(), edgeCount(), getMemoryUsage(), edges.size());
    }
}

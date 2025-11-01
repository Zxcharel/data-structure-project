package src.graph;

import java.util.*;

/**
 * Route-partitioned Trie-based graph implementation.
 * For each origin node, destinations are indexed in a trie by their string
 * characters. The trie root also maintains a flat list of all outgoing edges
 * to provide fast neighbor iteration for pathfinding algorithms, while the
 * trie index enables efficient direct lookups by destination.
 */
public class RoutePartitionedTrieGraph implements Graph {
    private final Set<String> nodes; // All nodes seen in the graph
    private final Map<String, TrieNode> originToTrieRoot; // One trie per origin
    private int edgeCount;

    /**
     * Trie node used to index destination strings.
     * Each origin has its own trie; the root node also aggregates a flat list
     * of all outgoing edges from that origin for fast neighbors() traversal.
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        // Edges that terminate at this exact destination string (can be multiple airlines)
        List<Edge> terminalEdges = null;
        // On the root only: aggregate of all outgoing edges for the origin
        List<Edge> allOutgoingEdges = null;
    }

    public RoutePartitionedTrieGraph() {
        this.nodes = new HashSet<>();
        this.originToTrieRoot = new HashMap<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        if (nodes.add(node)) {
            // Ensure a trie root exists for this node (even if no outgoing edges yet)
            originToTrieRoot.computeIfAbsent(node, k -> new TrieNode());
        }
    }

    @Override
    public void addEdge(String from, Edge edge) {
        // Ensure both endpoints are registered as nodes
        addNode(from);
        addNode(edge.getDestination());

        // Insert destination into the origin's trie
        TrieNode root = originToTrieRoot.computeIfAbsent(from, k -> new TrieNode());

        // Maintain flat neighbor list at root for fast neighbors()
        if (root.allOutgoingEdges == null) {
            root.allOutgoingEdges = new ArrayList<>();
        }
        root.allOutgoingEdges.add(edge);

        // Walk down the trie for destination characters
        TrieNode current = root;
        String dest = edge.getDestination();
        for (int i = 0; i < dest.length(); i++) {
            char c = dest.charAt(i);
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }

        // Attach this edge to the terminal node (destination fully matched)
        if (current.terminalEdges == null) {
            current.terminalEdges = new ArrayList<>();
        }
        current.terminalEdges.add(edge);

        edgeCount++;
    }

    @Override
    public List<String> nodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public List<Edge> neighbors(String node) {
        TrieNode root = originToTrieRoot.get(node);
        if (root == null || root.allOutgoingEdges == null) {
            return new ArrayList<>();
        }
        // Return a copy to protect internal structure
        return new ArrayList<>(root.allOutgoingEdges);
    }

    @Override
    public boolean hasNode(String node) {
        return nodes.contains(node);
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }

    @Override
    public int nodeCount() {
        return nodes.size();
    }

    /**
     * Direct weight lookup between two nodes if an edge exists.
     * Returns Double.MAX_VALUE when not found.
     */
    public double getWeight(String from, String to) {
        List<Edge> edges = getEdgesTo(from, to);
        if (!edges.isEmpty()) {
            return edges.get(0).getWeight();
        }
        return Double.MAX_VALUE;
    }

    /**
     * Direct airline lookup between two nodes if an edge exists.
     * Returns null when not found.
     */
    public String getAirline(String from, String to) {
        List<Edge> edges = getEdgesTo(from, to);
        if (!edges.isEmpty()) {
            return edges.get(0).getAirline();
        }
        return null;
    }

    /**
     * Helper: returns all edges for the exact (from -> to) destination match.
     */
    private List<Edge> getEdgesTo(String from, String to) {
        TrieNode root = originToTrieRoot.get(from);
        if (root == null) {
            return Collections.emptyList();
        }
        TrieNode current = root;
        for (int i = 0; i < to.length(); i++) {
            char c = to.charAt(i);
            TrieNode next = current.children.get(c);
            if (next == null) {
                return Collections.emptyList();
            }
            current = next;
        }
        return current.terminalEdges != null ? current.terminalEdges : Collections.emptyList();
    }

    /**
     * Estimates memory usage by traversing all tries.
     * This is a rough estimate consistent with other implementations.
     */
    public long getMemoryUsage() {
        long baseMemory = 1000; // Object overhead
        long nodeSetMemory = (long) nodes.size() * 50;

        // Count trie nodes and edges stored in tries
        long[] counts = countTrieNodesAndTerminals();
        long trieNodeCount = counts[0];
        long terminalEdgeRefs = counts[1];

        long trieNodeMemory = trieNodeCount * 64; // rough per-trie-node overhead
        long terminalMemory = terminalEdgeRefs * 8; // references to Edge objects

        long outgoingListsMemory = 0;
        for (TrieNode root : originToTrieRoot.values()) {
            if (root.allOutgoingEdges != null) {
                outgoingListsMemory += (long) root.allOutgoingEdges.size() * 8;
            }
        }

        return baseMemory + nodeSetMemory + trieNodeMemory + terminalMemory + outgoingListsMemory;
    }

    private long[] countTrieNodesAndTerminals() {
        long[] result = new long[]{0L, 0L};
        for (TrieNode root : originToTrieRoot.values()) {
            result[0]++; // count root itself
            if (root.allOutgoingEdges != null) {
                // accounted for in outgoingListsMemory, not in terminalEdgeRefs
            }
            Deque<TrieNode> stack = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                TrieNode node = stack.pop();
                for (TrieNode child : node.children.values()) {
                    result[0]++;
                    stack.push(child);
                }
                if (node.terminalEdges != null) {
                    result[1] += node.terminalEdges.size();
                }
            }
        }
        return result;
    }

    /**
     * Gets the average edges per node.
     */
    public double getAverageEdgesPerNode() {
        int n = nodeCount();
        return n > 0 ? (double) edgeCount / n : 0.0;
    }

    @Override
    public String toString() {
        return String.format(
                "RoutePartitionedTrieGraph{nodes=%d, edges=%d, memory=%d bytes, avg_edges_per_node=%.2f}",
                nodeCount(), edgeCount(), getMemoryUsage(), getAverageEdgesPerNode());
    }
}



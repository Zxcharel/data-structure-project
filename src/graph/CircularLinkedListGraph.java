package src.graph;

import java.util.*;

/**
 * Circular linked list implementation of a directed graph.
 * Uses HashMap for O(1) node lookup and a circular singly linked list per node for edges.
 */
public class CircularLinkedListGraph implements Graph {
    /**
     * Node representing an edge in a circular singly linked list.
     */
    private static class EdgeNode {
        final Edge edge;
        EdgeNode next;

        EdgeNode(Edge edge) {
            this.edge = edge;
        }
    }

    /**
     * Wrapper around a circular list represented by its tail pointer.
     * If empty, tail == null. When non-empty, tail.next is the head.
     */
    private static class CircularEdgeList {
        EdgeNode tail;

        void addLast(Edge edge) {
            EdgeNode newNode = new EdgeNode(edge);
            if (tail == null) {
                tail = newNode;
                tail.next = tail; // circular reference to itself
                return;
            }
            // Insert newNode after tail (i.e., at the end) and update tail
            newNode.next = tail.next; // new head reference
            tail.next = newNode;
            tail = newNode;
        }

        List<Edge> toList() {
            List<Edge> result = new ArrayList<>();
            if (tail == null) return result;
            EdgeNode current = tail.next; // head
            do {
                result.add(current.edge);
                current = current.next;
            } while (current != tail.next);
            return result;
        }
    }

    private final Map<String, CircularEdgeList> adjacency;
    private int edgeCount;

    public CircularLinkedListGraph() {
        this.adjacency = new HashMap<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        adjacency.putIfAbsent(node, new CircularEdgeList());
    }

    @Override
    public void addEdge(String from, Edge edge) {
        // Ensure both nodes exist
        addNode(from);
        addNode(edge.getDestination());

        // Append to the circular list for this source node
        adjacency.get(from).addLast(edge);
        edgeCount++;
    }

    @Override
    public List<String> nodes() {
        return new ArrayList<>(adjacency.keySet());
    }

    @Override
    public List<Edge> neighbors(String node) {
        CircularEdgeList list = adjacency.get(node);
        return list != null ? list.toList() : new ArrayList<>();
    }

    @Override
    public boolean hasNode(String node) {
        return adjacency.containsKey(node);
    }

    @Override
    public int edgeCount() {
        return edgeCount;
    }

    @Override
    public int nodeCount() {
        return adjacency.size();
    }

    /**
     * Gets all edges in the graph (for debugging/analysis)
     */
    public List<Edge> getAllEdges() {
        List<Edge> allEdges = new ArrayList<>();
        for (CircularEdgeList list : adjacency.values()) {
            allEdges.addAll(list.toList());
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
        sb.append("CircularLinkedListGraph{\n");
        for (Map.Entry<String, CircularEdgeList> entry : adjacency.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ");
            CircularEdgeList list = entry.getValue();
            for (Edge edge : list.toList()) {
                sb.append(edge.getDestination()).append("(").append(edge.getAirline()).append(") ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}



package src.graph;

import java.util.*;

/**
 * Doubly linked list implementation of a directed graph.
 * Uses HashMap for O(1) node lookup and a custom doubly linked list per node for edges.
 */
public class DoublyLinkedListGraph implements Graph {
    /**
     * Node representing an edge in a doubly linked list.
     */
    private static class EdgeNode {
        final Edge edge;
        EdgeNode previous;
        EdgeNode next;

        EdgeNode(Edge edge) {
            this.edge = edge;
        }
    }

    /**
     * Simple wrapper around head/tail pointers for a doubly linked list of edges.
     */
    private static class EdgeList {
        EdgeNode head;
        EdgeNode tail;

        void addLast(Edge edge) {
            EdgeNode newNode = new EdgeNode(edge);
            if (head == null) {
                head = newNode;
                tail = newNode;
                return;
            }
            tail.next = newNode;
            newNode.previous = tail;
            tail = newNode;
        }

        List<Edge> toList() {
            List<Edge> result = new ArrayList<>();
            for (EdgeNode current = head; current != null; current = current.next) {
                result.add(current.edge);
            }
            return result;
        }
    }

    private final Map<String, EdgeList> adjacency;
    private int edgeCount;

    public DoublyLinkedListGraph() {
        this.adjacency = new HashMap<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        adjacency.putIfAbsent(node, new EdgeList());
    }

    @Override
    public void addEdge(String from, Edge edge) {
        // Ensure both nodes exist
        addNode(from);
        addNode(edge.getDestination());

        // Append to the doubly linked list for this source node
        adjacency.get(from).addLast(edge);
        edgeCount++;
    }

    @Override
    public List<String> nodes() {
        return new ArrayList<>(adjacency.keySet());
    }

    @Override
    public List<Edge> neighbors(String node) {
        EdgeList list = adjacency.get(node);
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
        for (EdgeList list : adjacency.values()) {
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
        sb.append("DoublyLinkedListGraph{\n");
        for (Map.Entry<String, EdgeList> entry : adjacency.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ");
            EdgeList list = entry.getValue();
            for (Edge edge : list.toList()) {
                sb.append(edge.getDestination()).append("(").append(edge.getAirline()).append(") ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}




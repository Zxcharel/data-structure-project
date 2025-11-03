package src.graph;

import java.util.*;

/**
 * Minimal half-edge style graph for directed edges.
 * Maintains per-node lists of HalfEdge objects that wrap the public Edge.
 * This is a pragmatic adaptation for our pathfinding API; it does not
 * construct full twins across an undirected embedding.
 */
public class HalfEdgeGraph implements Graph {
    /**
     * Half-edge representation for a single directed edge from a source node.
     * Only maintains local next/prev within the adjacency sequence of the source.
     */
    private static class HalfEdge {
        final String from;
        final Edge edge; // public data
        HalfEdge next;   // next half-edge in the same source's list
        HalfEdge prev;   // prev half-edge in the same source's list

        HalfEdge(String from, Edge edge) {
            this.from = from;
            this.edge = edge;
        }
    }

    /**
     * Stores the head/tail of a doubly-linked list of half-edges per source node.
     */
    private static class HalfEdgeList {
        HalfEdge head;
        HalfEdge tail;

        void addLast(HalfEdge he) {
            if (head == null) {
                head = he;
                tail = he;
                return;
            }
            tail.next = he;
            he.prev = tail;
            tail = he;
        }

        List<Edge> toEdgeList() {
            List<Edge> result = new ArrayList<>();
            for (HalfEdge cur = head; cur != null; cur = cur.next) {
                result.add(cur.edge);
            }
            return result;
        }
    }

    private final Map<String, HalfEdgeList> adjacency;
    private int edgeCount;

    public HalfEdgeGraph() {
        this.adjacency = new HashMap<>();
        this.edgeCount = 0;
    }

    @Override
    public void addNode(String node) {
        adjacency.putIfAbsent(node, new HalfEdgeList());
    }

    @Override
    public void addEdge(String from, Edge edge) {
        addNode(from);
        addNode(edge.getDestination());
        HalfEdge he = new HalfEdge(from, edge);
        adjacency.get(from).addLast(he);
        edgeCount++;
    }

    @Override
    public List<String> nodes() {
        return new ArrayList<>(adjacency.keySet());
    }

    @Override
    public List<Edge> neighbors(String node) {
        HalfEdgeList list = adjacency.get(node);
        return list != null ? list.toEdgeList() : new ArrayList<>();
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
        sb.append("HalfEdgeGraph{\n");
        for (Map.Entry<String, HalfEdgeList> entry : adjacency.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" -> ");
            for (Edge e : entry.getValue().toEdgeList()) {
                sb.append(e.getDestination()).append("(").append(e.getAirline()).append(") ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}



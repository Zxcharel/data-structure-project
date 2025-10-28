package graph;

import java.util.List;

/**
 * Interface for graph data structures.
 * Defines the basic operations needed for pathfinding algorithms.
 */
public interface Graph {
    /**
     * Adds a node to the graph
     * @param node The node identifier
     */
    void addNode(String node);
    
    /**
     * Adds an edge to the graph
     * @param from The source node
     * @param edge The edge to add
     */
    void addEdge(String from, Edge edge);
    
    /**
     * Gets all nodes in the graph
     * @return List of node identifiers
     */
    List<String> nodes();
    
    /**
     * Gets all outgoing edges from a node
     * @param node The source node
     * @return List of edges from the node
     */
    List<Edge> neighbors(String node);
    
    /**
     * Checks if a node exists in the graph
     * @param node The node to check
     * @return true if the node exists
     */
    boolean hasNode(String node);
    
    /**
     * Gets the total number of edges in the graph
     * @return Number of edges
     */
    int edgeCount();
    
    /**
     * Gets the total number of nodes in the graph
     * @return Number of nodes
     */
    int nodeCount();
}

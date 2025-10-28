package src.algo;

import src.graph.Edge;
import src.graph.Graph;
import src.util.Stopwatch;
import java.util.*;

/**
 * Dijkstra's algorithm implementation for finding shortest paths in weighted graphs.
 * Uses a priority queue and tracks algorithm statistics.
 */
public class Dijkstra {
    
    /**
     * Finds the shortest path from origin to destination using Dijkstra's algorithm.
     * 
     * @param graph The graph to search
     * @param origin Starting node
     * @param destination Target node
     * @param constraints Optional constraints (max stops, airline filters)
     * @return PathResult containing the path and statistics
     */
    public PathResult findPath(Graph graph, String origin, String destination, Constraints constraints) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        
        // Validate inputs
        if (!graph.hasNode(origin)) {
            return new PathResult(null, null, 0, 0, 0, 0, false);
        }
        if (!graph.hasNode(destination)) {
            return new PathResult(null, null, 0, 0, 0, 0, false);
        }
        
        // Use default constraints if none provided
        if (constraints == null) {
            constraints = new Constraints();
        }
        
        // Initialize data structures
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Map<String, String> previousAirline = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<PathNode> pq = new PriorityQueue<>();
        
        // Initialize distances
        for (String node : graph.nodes()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(origin, 0.0);
        
        // Add origin to priority queue
        pq.offer(new PathNode(origin, 0.0, 0));
        
        int nodesVisited = 0;
        int edgesRelaxed = 0;
        
        // Main algorithm loop
        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            String currentNode = current.node;
            
            // Skip if already visited
            if (visited.contains(currentNode)) {
                continue;
            }
            
            visited.add(currentNode);
            nodesVisited++;
            
            // Check if we reached the destination
            if (currentNode.equals(destination)) {
                break;
            }
            
            // Relax all outgoing edges
            for (Edge edge : graph.neighbors(currentNode)) {
                edgesRelaxed++;
                
                // Check constraints
                if (!constraints.isEdgeAllowed(edge, current.stops)) {
                    continue;
                }
                
                String neighbor = edge.getDestination();
                double newDistance = distances.get(currentNode) + edge.getWeight();
                
                // Update distance if we found a shorter path
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentNode);
                    previousAirline.put(neighbor, edge.getAirline());
                    
                    // Add to priority queue with updated distance
                    pq.offer(new PathNode(neighbor, newDistance, current.stops + 1));
                }
            }
        }
        
        stopwatch.stop();
        
        // Reconstruct path if destination was reached
        if (distances.get(destination) == Double.MAX_VALUE) {
            return new PathResult(null, null, 0, nodesVisited, edgesRelaxed, stopwatch.getElapsedMs(), false);
        }
        
        // Build path
        List<String> path = new ArrayList<>();
        List<String> airlines = new ArrayList<>();
        
        String current = destination;
        while (current != null) {
            path.add(current);
            if (previousAirline.get(current) != null) {
                airlines.add(previousAirline.get(current));
            }
            current = previous.get(current);
        }
        
        Collections.reverse(path);
        Collections.reverse(airlines);
        
        return new PathResult(path, airlines, distances.get(destination), 
                             nodesVisited, edgesRelaxed, stopwatch.getElapsedMs(), true);
    }
    
    /**
     * Convenience method without constraints
     */
    public PathResult findPath(Graph graph, String origin, String destination) {
        return findPath(graph, origin, destination, new Constraints());
    }
    
    /**
     * Helper class for priority queue entries
     */
    private static class PathNode implements Comparable<PathNode> {
        final String node;
        final double distance;
        final int stops;
        
        PathNode(String node, double distance, int stops) {
            this.node = node;
            this.distance = distance;
            this.stops = stops;
        }
        
        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}

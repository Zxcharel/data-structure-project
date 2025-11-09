package src.algo;

import src.graph.Edge;
import src.graph.Graph;
import src.util.Stopwatch;
import java.util.*;

/**
 * A* algorithm implementation for finding shortest paths in weighted graphs.
 * Uses a heuristic function to guide the search more efficiently than Dijkstra.
 * We did not implement A* in this project because we felt that we were more familiar with Dijkstra instead.
 */
public class AStar {
    
    /**
     * Finds the shortest path from origin to destination using A* algorithm.
     * 
     * @param graph The graph to search
     * @param origin Starting node
     * @param destination Target node
     * @param heuristic Heuristic function to estimate distance to destination
     * @param constraints Optional constraints (max stops, airline filters)
     * @return PathResult containing the path and statistics
     */
    public PathResult findPath(Graph graph, String origin, String destination, 
                               Heuristic heuristic, Constraints constraints) {
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
        Map<String, Double> gScore = new HashMap<>(); // Cost from start
        Map<String, Double> fScore = new HashMap<>(); // Estimated total cost (g + h)
        Map<String, String> previous = new HashMap<>();
        Map<String, String> previousAirline = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<AStarNode> pq = new PriorityQueue<>();
        
        // Initialize scores
        for (String node : graph.nodes()) {
            gScore.put(node, Double.MAX_VALUE);
            fScore.put(node, Double.MAX_VALUE);
        }
        gScore.put(origin, 0.0);
        fScore.put(origin, heuristic.estimate(origin, destination));
        
        // Add origin to priority queue
        pq.offer(new AStarNode(origin, fScore.get(origin), 0));
        
        int nodesVisited = 0;
        int edgesRelaxed = 0;
        
        // Main algorithm loop
        while (!pq.isEmpty()) {
            AStarNode current = pq.poll();
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
                double tentativeGScore = gScore.get(currentNode) + edge.getWeight();
                
                // Update if we found a better path
                if (tentativeGScore < gScore.get(neighbor)) {
                    gScore.put(neighbor, tentativeGScore);
                    double h = heuristic.estimate(neighbor, destination);
                    fScore.put(neighbor, tentativeGScore + h);
                    previous.put(neighbor, currentNode);
                    previousAirline.put(neighbor, edge.getAirline());
                    
                    // Add to priority queue with updated f-score
                    pq.offer(new AStarNode(neighbor, fScore.get(neighbor), current.stops + 1));
                }
            }
        }
        
        stopwatch.stop();
        
        // Reconstruct path if destination was reached
        if (gScore.get(destination) == Double.MAX_VALUE) {
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
        
        return new PathResult(path, airlines, gScore.get(destination), 
                             nodesVisited, edgesRelaxed, stopwatch.getElapsedMs(), true);
    }
    
    /**
     * Heuristic interface for A* algorithm
     */
    public interface Heuristic {
        double estimate(String from, String to);
    }
    
    /**
     * Zero heuristic - makes A* behave like Dijkstra
     */
    public static class ZeroHeuristic implements Heuristic {
        @Override
        public double estimate(String from, String to) {
            return 0.0;
        }
    }
    
    /**
     * Hop-based heuristic - estimates distance based on number of hops
     */
    public static class HopHeuristic implements Heuristic {
        @Override
        public double estimate(String from, String to) {
            // Simple hop-based heuristic: assume 1.0 weight per hop
            // This is admissible (never overestimates) if edge weights are >= 1.0
            return 1.0;
        }
    }
    
    /**
     * Helper class for priority queue entries
     */
    private static class AStarNode implements Comparable<AStarNode> {
        final String node;
        final double fScore;
        final int stops;
        
        AStarNode(String node, double fScore, int stops) {
            this.node = node;
            this.fScore = fScore;
            this.stops = stops;
        }
        
        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}


package algo;

import graph.Edge;
import graph.Graph;
import util.Stopwatch;
import java.util.*;

/**
 * A* algorithm implementation with configurable heuristics.
 * Supports zero heuristic (equivalent to Dijkstra) and hop-based heuristic.
 */
public class AStar {
    
    /**
     * Interface for heuristic functions
     */
    public interface Heuristic {
        double estimate(String from, String to, Graph graph);
    }
    
    /**
     * Zero heuristic - A* reduces to Dijkstra
     */
    public static class ZeroHeuristic implements Heuristic {
        @Override
        public double estimate(String from, String to, Graph graph) {
            return 0.0;
        }
    }
    
    /**
     * Hop-based heuristic - estimates remaining hops using BFS
     */
    public static class HopHeuristic implements Heuristic {
        private final Map<String, Map<String, Integer>> hopCache = new HashMap<>();
        
        @Override
        public double estimate(String from, String to, Graph graph) {
            // Check cache first
            if (hopCache.containsKey(from) && hopCache.get(from).containsKey(to)) {
                return hopCache.get(from).get(to);
            }
            
            // Compute hop distance using BFS
            int hops = computeHopDistance(from, to, graph);
            
            // Cache the result
            hopCache.computeIfAbsent(from, k -> new HashMap<>()).put(to, hops);
            
            return hops;
        }
        
        private int computeHopDistance(String from, String to, Graph graph) {
            if (from.equals(to)) {
                return 0;
            }
            
            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();
            Map<String, Integer> distances = new HashMap<>();
            
            queue.offer(from);
            visited.add(from);
            distances.put(from, 0);
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                
                for (Edge edge : graph.neighbors(current)) {
                    String neighbor = edge.getDestination();
                    
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        distances.put(neighbor, distances.get(current) + 1);
                        queue.offer(neighbor);
                        
                        if (neighbor.equals(to)) {
                            return distances.get(neighbor);
                        }
                    }
                }
            }
            
            // If no path found, return a large number (but not infinity)
            return 1000;
        }
    }
    
    /**
     * Finds the shortest path using A* algorithm with the specified heuristic.
     * 
     * @param graph The graph to search
     * @param origin Starting node
     * @param destination Target node
     * @param heuristic The heuristic function to use
     * @param constraints Optional constraints
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
        Map<String, Double> gScore = new HashMap<>(); // Actual distance from start
        Map<String, Double> fScore = new HashMap<>(); // gScore + heuristic
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
        fScore.put(origin, heuristic.estimate(origin, destination, graph));
        
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
                
                // Update scores if we found a better path
                if (tentativeGScore < gScore.get(neighbor)) {
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + heuristic.estimate(neighbor, destination, graph));
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
     * Convenience method with zero heuristic
     */
    public PathResult findPath(Graph graph, String origin, String destination, Constraints constraints) {
        return findPath(graph, origin, destination, new ZeroHeuristic(), constraints);
    }
    
    /**
     * Convenience method with zero heuristic and no constraints
     */
    public PathResult findPath(Graph graph, String origin, String destination) {
        return findPath(graph, origin, destination, new ZeroHeuristic(), new Constraints());
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

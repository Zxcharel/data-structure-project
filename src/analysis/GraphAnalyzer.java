package src.analysis;

import src.graph.Graph;
import src.graph.Edge;
import java.util.*;

/**
 * Graph analysis utilities for analyzing connectivity, centrality, and structure.
 * Provides methods to analyze the flight route graph and generate insights.
 */
public class GraphAnalyzer {
    public final Graph graph;
    
    public GraphAnalyzer(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Analyzes the overall structure of the graph
     */
    public GraphStructureAnalysis analyzeStructure() {
        List<String> nodes = graph.nodes();
        int totalNodes = nodes.size();
        int totalEdges = graph.edgeCount();
        
        // Calculate basic metrics
        double avgDegree = totalNodes > 0 ? (2.0 * totalEdges) / totalNodes : 0;
        double density = totalNodes > 1 ? (2.0 * totalEdges) / (totalNodes * (totalNodes - 1)) : 0;
        
        // Find strongly connected components
        List<Set<String>> components = findStronglyConnectedComponents();
        int largestComponentSize = components.stream().mapToInt(Set::size).max().orElse(0);
        
        // Calculate clustering coefficient
        double clusteringCoeff = calculateClusteringCoefficient();
        
        return new GraphStructureAnalysis(
            totalNodes, totalEdges, avgDegree, density,
            components.size(), largestComponentSize, clusteringCoeff
        );
    }
    
    /**
     * Calculates node centrality metrics (optimized for large graphs)
     */
    public Map<String, CentralityMetrics> calculateCentrality() {
        Map<String, CentralityMetrics> centrality = new HashMap<>();
        List<String> nodes = graph.nodes();
        
        // For large graphs, limit analysis to top nodes by degree
        int maxNodes = Math.min(100, nodes.size());
        List<String> topNodes = nodes.stream()
            .sorted((a, b) -> Integer.compare(graph.neighbors(b).size(), graph.neighbors(a).size()))
            .limit(maxNodes)
            .toList();
        
        System.out.println("Calculating centrality for top " + maxNodes + " nodes by degree...");
        
        for (String node : topNodes) {
            int degree = graph.neighbors(node).size();
            int betweenness = calculateBetweennessCentralityFast(node, topNodes);
            int closeness = calculateClosenessCentrality(node);
            
            centrality.put(node, new CentralityMetrics(degree, betweenness, closeness));
        }
        
        return centrality;
    }
    
    /**
     * Analyzes airline distribution and popularity
     */
    public AirlineAnalysis analyzeAirlines() {
        Map<String, Integer> airlineCounts = new HashMap<>();
        Map<String, Double> airlineWeights = new HashMap<>();
        Map<String, Set<String>> airlineRoutes = new HashMap<>();
        
        for (String node : graph.nodes()) {
            for (Edge edge : graph.neighbors(node)) {
                String airline = edge.getAirline();
                
                // Count routes per airline
                airlineCounts.merge(airline, 1, Integer::sum);
                
                // Sum weights per airline
                airlineWeights.merge(airline, edge.getWeight(), Double::sum);
                
                // Track routes per airline
                airlineRoutes.computeIfAbsent(airline, k -> new HashSet<>())
                            .add(node + "->" + edge.getDestination());
            }
        }
        
        // Find most/least popular airlines
        String mostPopular = airlineCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        String leastPopular = airlineCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        // Calculate average weight per airline
        Map<String, Double> avgWeights = new HashMap<>();
        for (String airline : airlineCounts.keySet()) {
            avgWeights.put(airline, airlineWeights.get(airline) / airlineCounts.get(airline));
        }
        
        String bestRated = avgWeights.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        String worstRated = avgWeights.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        return new AirlineAnalysis(
            airlineCounts, airlineRoutes, avgWeights,
            mostPopular, leastPopular, bestRated, worstRated
        );
    }
    
    /**
     * Analyzes route patterns and connectivity
     */
    public RouteAnalysis analyzeRoutes() {
        Map<String, Integer> countryConnections = new HashMap<>();
        Map<String, Double> countryWeights = new HashMap<>();
        
        // Count connections per country
        for (String node : graph.nodes()) {
            int outDegree = graph.neighbors(node).size();
            countryConnections.put(node, outDegree);
            
            // Calculate average weight of outgoing edges
            double totalWeight = graph.neighbors(node).stream()
                .mapToDouble(Edge::getWeight)
                .sum();
            countryWeights.put(node, outDegree > 0 ? totalWeight / outDegree : 0);
        }
        
        // Find hub countries (most connected)
        String biggestHub = countryConnections.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        // Find countries with best average route quality
        String bestRoutes = countryWeights.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        
        // Calculate path length statistics
        PathLengthStats pathStats = calculatePathLengthStats();
        
        return new RouteAnalysis(
            countryConnections, countryWeights, biggestHub, bestRoutes, pathStats
        );
    }
    
    /**
     * Finds strongly connected components using Kosaraju's algorithm
     */
    private List<Set<String>> findStronglyConnectedComponents() {
        List<String> nodes = graph.nodes();
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();
        
        // First pass: fill stack with finish times
        for (String node : nodes) {
            if (!visited.contains(node)) {
                dfsFillStack(node, visited, stack);
            }
        }
        
        // Second pass: process in reverse order
        visited.clear();
        List<Set<String>> components = new ArrayList<>();
        
        while (!stack.isEmpty()) {
            String node = stack.pop();
            if (!visited.contains(node)) {
                Set<String> component = new HashSet<>();
                dfsCollectComponent(node, visited, component);
                components.add(component);
            }
        }
        
        return components;
    }
    
    private void dfsFillStack(String node, Set<String> visited, Stack<String> stack) {
        visited.add(node);
        for (Edge edge : graph.neighbors(node)) {
            if (!visited.contains(edge.getDestination())) {
                dfsFillStack(edge.getDestination(), visited, stack);
            }
        }
        stack.push(node);
    }
    
    private void dfsCollectComponent(String node, Set<String> visited, Set<String> component) {
        visited.add(node);
        component.add(node);
        
        // Check all nodes that can reach this node
        for (String otherNode : graph.nodes()) {
            if (!visited.contains(otherNode)) {
                for (Edge edge : graph.neighbors(otherNode)) {
                    if (edge.getDestination().equals(node)) {
                        dfsCollectComponent(otherNode, visited, component);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Calculates clustering coefficient (optimized version)
     */
    private double calculateClusteringCoefficient() {
        List<String> nodes = graph.nodes();
        double totalCoeff = 0;
        int validNodes = 0;
        
        // Sample only high-degree nodes for efficiency
        int sampleSize = Math.min(200, nodes.size());
        List<String> sampleNodes = nodes.stream()
            .sorted((a, b) -> Integer.compare(graph.neighbors(b).size(), graph.neighbors(a).size()))
            .limit(sampleSize)
            .toList();
        
        for (String node : sampleNodes) {
            List<Edge> neighbors = graph.neighbors(node);
            int degree = neighbors.size();
            
            if (degree >= 2 && degree <= 20) { // Skip nodes with too many neighbors
                // Count triangles (simplified)
                int triangles = 0;
                for (int i = 0; i < neighbors.size(); i++) {
                    for (int j = i + 1; j < neighbors.size(); j++) {
                        String neighbor1 = neighbors.get(i).getDestination();
                        String neighbor2 = neighbors.get(j).getDestination();
                        
                        // Check if neighbor1 and neighbor2 are connected
                        if (graph.neighbors(neighbor1).stream()
                            .anyMatch(e -> e.getDestination().equals(neighbor2))) {
                            triangles++;
                        }
                    }
                }
                
                int possibleTriangles = degree * (degree - 1) / 2;
                if (possibleTriangles > 0) {
                    totalCoeff += (double) triangles / possibleTriangles;
                    validNodes++;
                }
            }
        }
        
        return validNodes > 0 ? totalCoeff / validNodes : 0;
    }
    
    /**
     * Calculates betweenness centrality (optimized version)
     */
    private int calculateBetweennessCentralityFast(String node, List<String> sampleNodes) {
        // Only check paths between sample nodes (much faster)
        int betweenness = 0;
        int maxChecks = Math.min(50, sampleNodes.size()); // Limit to 50 checks per node
        
        for (int i = 0; i < maxChecks; i++) {
            for (int j = i + 1; j < maxChecks; j++) {
                String source = sampleNodes.get(i);
                String target = sampleNodes.get(j);
                
                if (!source.equals(node) && !target.equals(node)) {
                    if (isOnShortestPath(source, target, node)) {
                        betweenness++;
                    }
                }
            }
        }
        
        return betweenness;
    }
    
    /**
     * Calculates closeness centrality
     */
    private int calculateClosenessCentrality(String node) {
        // Simplified version - count reachable nodes
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.offer(node);
        reachable.add(node);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Edge edge : graph.neighbors(current)) {
                if (!reachable.contains(edge.getDestination())) {
                    reachable.add(edge.getDestination());
                    queue.offer(edge.getDestination());
                }
            }
        }
        
        return reachable.size() - 1; // Exclude the node itself
    }
    
    /**
     * Checks if a node is on the shortest path between two other nodes
     */
    private boolean isOnShortestPath(String source, String target, String intermediate) {
        // Simplified BFS to check if intermediate is on shortest path
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>();
        
        queue.offer(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(target)) {
                // Reconstruct path and check if intermediate is on it
                String node = target;
                while (node != null) {
                    if (node.equals(intermediate)) {
                        return true;
                    }
                    node = parent.get(node);
                }
                return false;
            }
            
            for (Edge edge : graph.neighbors(current)) {
                String neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        
        return false;
    }
    
    /**
     * Calculates path length statistics
     */
    private PathLengthStats calculatePathLengthStats() {
        List<String> nodes = graph.nodes();
        List<Integer> pathLengths = new ArrayList<>();
        
        // Sample some random paths to estimate statistics
        Random random = new Random(42);
        int samples = Math.min(100, nodes.size() * nodes.size() / 10);
        
        for (int i = 0; i < samples; i++) {
            String source = nodes.get(random.nextInt(nodes.size()));
            String target = nodes.get(random.nextInt(nodes.size()));
            
            if (!source.equals(target)) {
                int pathLength = findShortestPathLength(source, target);
                if (pathLength > 0) {
                    pathLengths.add(pathLength);
                }
            }
        }
        
        if (pathLengths.isEmpty()) {
            return new PathLengthStats(0, 0, 0, 0);
        }
        
        Collections.sort(pathLengths);
        int min = pathLengths.get(0);
        int max = pathLengths.get(pathLengths.size() - 1);
        double avg = pathLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
        int median = pathLengths.get(pathLengths.size() / 2);
        
        return new PathLengthStats(min, max, avg, median);
    }
    
    /**
     * Finds shortest path length using BFS
     */
    private int findShortestPathLength(String source, String target) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> distances = new HashMap<>();
        
        queue.offer(source);
        visited.add(source);
        distances.put(source, 0);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(target)) {
                return distances.get(current);
            }
            
            for (Edge edge : graph.neighbors(current)) {
                String neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    distances.put(neighbor, distances.get(current) + 1);
                    queue.offer(neighbor);
                }
            }
        }
        
        return -1; // No path found
    }
}

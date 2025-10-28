package src.experiments;

import src.algo.*;
import src.graph.Graph;
import src.util.IOUtils;
import java.io.IOException;
import java.util.*;

/**
 * Runs automated experiments comparing different pathfinding algorithms.
 * Generates random queries and compares performance metrics.
 */
public class ExperimentRunner {
    private final Graph graph;
    private final Random random;
    
    public ExperimentRunner(Graph graph) {
        this.graph = graph;
        this.random = new Random(42); // Fixed seed for reproducible results
    }
    
    /**
     * Runs experiments comparing Dijkstra vs A* algorithms
     * 
     * @param numQueries Number of random queries to run
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be written
     */
    public void runExperiments(int numQueries, String outputDir) throws IOException {
        List<String> nodes = graph.nodes();
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Graph must have at least 2 nodes for experiments");
        }
        
        // Prepare output directory
        IOUtils.ensureParentDirectoryExists(outputDir + "/algorithms.csv");
        
        // Generate random queries
        List<ExperimentQuery> queries = generateRandomQueries(nodes, numQueries);
        
        // Run experiments
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "query_id", "origin", "destination", "algorithm", "heuristic", 
            "maxStops", "edges_relaxed", "nodes_visited", "runtime_ms", 
            "path_len", "total_weight"
        };
        
        int queryId = 1;
        for (ExperimentQuery query : queries) {
            // Test Dijkstra
            Dijkstra dijkstra = new Dijkstra();
            PathResult dijkstraResult = dijkstra.findPath(graph, query.origin, query.destination, query.constraints);
            csvRows.add(createCsvRow(queryId, query, "Dijkstra", "none", dijkstraResult));
            
            // Test A* with zero heuristic
            AStar aStarZero = new AStar();
            PathResult aStarZeroResult = aStarZero.findPath(graph, query.origin, query.destination, 
                                                           new AStar.ZeroHeuristic(), query.constraints);
            csvRows.add(createCsvRow(queryId, query, "A*", "zero", aStarZeroResult));
            
            // Test A* with hop heuristic
            AStar aStarHop = new AStar();
            PathResult aStarHopResult = aStarHop.findPath(graph, query.origin, query.destination, 
                                                          new AStar.HopHeuristic(), query.constraints);
            csvRows.add(createCsvRow(queryId, query, "A*", "hop", aStarHopResult));
            
            queryId++;
        }
        
        // Write CSV results
        IOUtils.writeCsv(outputDir + "/algorithms.csv", headers, csvRows);
        
        // Generate summary report
        generateSummaryReport(csvRows, outputDir + "/README.md");
        
        System.out.printf("Experiments completed. Results written to %s%n", outputDir);
    }
    
    /**
     * Generates random queries from the available nodes
     */
    private List<ExperimentQuery> generateRandomQueries(List<String> nodes, int numQueries) {
        List<ExperimentQuery> queries = new ArrayList<>();
        
        for (int i = 0; i < numQueries; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String destination = nodes.get(random.nextInt(nodes.size()));
            
            // Ensure origin != destination
            while (destination.equals(origin)) {
                destination = nodes.get(random.nextInt(nodes.size()));
            }
            
            // Random constraints (30% chance of max stops, 20% chance of airline filters)
            Constraints constraints = new Constraints();
            if (random.nextDouble() < 0.3) {
                int maxStops = random.nextInt(5) + 1; // 1-5 stops
                constraints = new Constraints(maxStops, null, null);
            } else if (random.nextDouble() < 0.2) {
                // Random airline allowlist (pick 1-3 random airlines)
                Set<String> airlines = getAllAirlines();
                if (!airlines.isEmpty()) {
                    List<String> airlineList = new ArrayList<>(airlines);
                    Collections.shuffle(airlineList, random);
                    int numAirlines = Math.min(random.nextInt(3) + 1, airlineList.size());
                    Set<String> allowlist = new HashSet<>(airlineList.subList(0, numAirlines));
                    constraints = new Constraints(Integer.MAX_VALUE, allowlist, null);
                }
            }
            
            queries.add(new ExperimentQuery(origin, destination, constraints));
        }
        
        return queries;
    }
    
    /**
     * Gets all unique airlines from the graph
     */
    private Set<String> getAllAirlines() {
        Set<String> airlines = new HashSet<>();
        for (String node : graph.nodes()) {
            for (graph.Edge edge : graph.neighbors(node)) {
                airlines.add(edge.getAirline());
            }
        }
        return airlines;
    }
    
    /**
     * Creates a CSV row for an experiment result
     */
    private String[] createCsvRow(int queryId, ExperimentQuery query, String algorithm, 
                                 String heuristic, PathResult result) {
        return new String[]{
            String.valueOf(queryId),
            query.origin,
            query.destination,
            algorithm,
            heuristic,
            query.constraints.getMaxStops() == Integer.MAX_VALUE ? "unlimited" : String.valueOf(query.constraints.getMaxStops()),
            String.valueOf(result.getEdgesRelaxed()),
            String.valueOf(result.getNodesVisited()),
            String.valueOf(result.getRuntimeMs()),
            String.valueOf(result.getPathLength()),
            IOUtils.formatDouble(result.getTotalWeight(), 3)
        };
    }
    
    /**
     * Generates a summary report in Markdown format
     */
    private void generateSummaryReport(List<String[]> csvRows, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Algorithm Performance Comparison\n\n");
        
        // Calculate statistics
        Map<String, AlgorithmStats> stats = calculateStatistics(csvRows);
        
        // Summary table
        report.append("## Summary Statistics\n\n");
        report.append("| Algorithm | Heuristic | Avg Runtime (ms) | Avg Edges Relaxed | Avg Nodes Visited | Success Rate |\n");
        report.append("|-----------|-----------|------------------|-------------------|-------------------|--------------|\n");
        
        for (Map.Entry<String, AlgorithmStats> entry : stats.entrySet()) {
            AlgorithmStats stat = entry.getValue();
            report.append(String.format("| %s | %s | %.1f | %.1f | %.1f | %s |\n",
                stat.algorithm, stat.heuristic, stat.avgRuntime, stat.avgEdgesRelaxed,
                stat.avgNodesVisited, IOUtils.formatPercentage(stat.successRate)));
        }
        
        report.append("\n## Key Findings\n\n");
        
        // Find best performing algorithm
        AlgorithmStats bestRuntime = stats.values().stream()
            .min(Comparator.comparing(s -> s.avgRuntime))
            .orElse(null);
        
        if (bestRuntime != null) {
            report.append(String.format("- **Fastest Algorithm**: %s with %s heuristic (%.1f ms avg)\n", 
                bestRuntime.algorithm, bestRuntime.heuristic, bestRuntime.avgRuntime));
        }
        
        AlgorithmStats mostEfficient = stats.values().stream()
            .min(Comparator.comparing(s -> s.avgEdgesRelaxed))
            .orElse(null);
        
        if (mostEfficient != null) {
            report.append(String.format("- **Most Efficient**: %s with %s heuristic (%.1f edges relaxed avg)\n", 
                mostEfficient.algorithm, mostEfficient.heuristic, mostEfficient.avgEdgesRelaxed));
        }
        
        report.append("\n## Notes\n\n");
        report.append("- All algorithms use the same weight calculation formula\n");
        report.append("- A* with zero heuristic is equivalent to Dijkstra\n");
        report.append("- Hop heuristic uses BFS-based distance estimation\n");
        report.append("- Results may vary based on graph structure and query patterns\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Calculates statistics for each algorithm
     */
    private Map<String, AlgorithmStats> calculateStatistics(List<String[]> csvRows) {
        Map<String, AlgorithmStats> stats = new HashMap<>();
        
        for (String[] row : csvRows) {
            String algorithm = row[3];
            String heuristic = row[4];
            String key = algorithm + "_" + heuristic;
            
            AlgorithmStats stat = stats.computeIfAbsent(key, k -> new AlgorithmStats(algorithm, heuristic));
            
            stat.totalQueries++;
            stat.totalRuntime += Long.parseLong(row[8]);
            stat.totalEdgesRelaxed += Integer.parseInt(row[6]);
            stat.totalNodesVisited += Integer.parseInt(row[7]);
            
            if (!row[10].equals("0.000")) { // Path found
                stat.successfulQueries++;
            }
        }
        
        // Calculate averages
        for (AlgorithmStats stat : stats.values()) {
            stat.avgRuntime = (double) stat.totalRuntime / stat.totalQueries;
            stat.avgEdgesRelaxed = (double) stat.totalEdgesRelaxed / stat.totalQueries;
            stat.avgNodesVisited = (double) stat.totalNodesVisited / stat.totalQueries;
            stat.successRate = (double) stat.successfulQueries / stat.totalQueries;
        }
        
        return stats;
    }
    
    /**
     * Helper class for experiment queries
     */
    private static class ExperimentQuery {
        final String origin;
        final String destination;
        final Constraints constraints;
        
        ExperimentQuery(String origin, String destination, Constraints constraints) {
            this.origin = origin;
            this.destination = destination;
            this.constraints = constraints;
        }
    }
    
    /**
     * Helper class for algorithm statistics
     */
    private static class AlgorithmStats {
        final String algorithm;
        final String heuristic;
        int totalQueries = 0;
        int successfulQueries = 0;
        long totalRuntime = 0;
        int totalEdgesRelaxed = 0;
        int totalNodesVisited = 0;
        double avgRuntime = 0;
        double avgEdgesRelaxed = 0;
        double avgNodesVisited = 0;
        double successRate = 0;
        
        AlgorithmStats(String algorithm, String heuristic) {
            this.algorithm = algorithm;
            this.heuristic = heuristic;
        }
    }
}

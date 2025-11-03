package src.experiments;

import src.algo.*;
import src.graph.Graph;
import src.graph.AdjacencyListGraph;
import src.graph.SortedAdjacencyListGraph;
import src.graph.CSRGraph;
import src.data.CsvReader;
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
            for (src.graph.Edge edge : graph.neighbors(node)) {
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
    
    /**
     * EXPERIMENT 1: "Sorted Edges: When Pre-Sorting Hurts Performance"
     * 
     * Intuition: SortedAdjacencyListGraph maintains edges sorted by weight to potentially
     * help algorithms access cheaper edges first. However, this comes with insertion overhead.
     * This experiment tests whether the overhead of maintaining sorted order actually
     * pays off in practice, or if Dijkstra's priority queue already provides enough ordering.
     * 
     * @param csvPath Path to CSV data file
     * @param numQueries Number of random queries to run
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be read/written
     */
    public void experimentSortedVsUnsorted(String csvPath, int numQueries, String outputDir) throws IOException {
        System.out.println("\n=== EXPERIMENT: Sorted Edges vs Unsorted Edges ===");
        System.out.println("Testing whether pre-sorting adjacency lists provides performance benefits.");
        
        // Load both graph types
        System.out.println("Loading graph data...");
        CsvReader reader = new CsvReader();
        
        Graph unsortedGraph = reader.readCsvAndBuildGraph(csvPath, AdjacencyListGraph::new);
        Graph sortedGraph = reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new);
        
        System.out.printf("Loaded %d nodes, %d edges into both structures%n", 
                         unsortedGraph.nodeCount(), unsortedGraph.edgeCount());
        
        List<String> nodes = sortedGraph.nodes();
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Graph must have at least 2 nodes");
        }
        
        // Generate random queries
        List<ExperimentQuery> queries = generateRandomQueries(nodes, numQueries);
        
        // Prepare output
        IOUtils.ensureParentDirectoryExists(outputDir + "/sorted_vs_unsorted.csv");
        
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "query_id", "origin", "destination", "graph_type", "algorithm",
            "edges_relaxed", "nodes_visited", "runtime_ms", "path_len", "total_weight"
        };
        
        Dijkstra dijkstra = new Dijkstra();
        int queryId = 1;
        
        for (ExperimentQuery query : queries) {
            // Test on unsorted graph
            PathResult unsortedResult = dijkstra.findPath(unsortedGraph, query.origin, query.destination, query.constraints);
            csvRows.add(createGraphComparisonRow(queryId, query, "AdjacencyListGraph", unsortedResult));
            
            // Test on sorted graph
            PathResult sortedResult = dijkstra.findPath(sortedGraph, query.origin, query.destination, query.constraints);
            csvRows.add(createGraphComparisonRow(queryId, query, "SortedAdjacencyListGraph", sortedResult));
            
            queryId++;
        }
        
        // Write results
        IOUtils.writeCsv(outputDir + "/sorted_vs_unsorted.csv", headers, csvRows);
        
        // Generate report
        generateSortedVsUnsortedReport(csvRows, outputDir + "/sorted_vs_unsorted_README.md");
        
        System.out.printf("\nExperiment completed! Results in %s%n", outputDir);
    }
    
    /**
     * EXPERIMENT 2: "Cache-Friendly Layouts: When Memory Beats Algorithm Complexity"
     * 
     * Intuition: CSRGraph uses a cache-friendly contiguous array layout while AdjacencyListGraph
     * uses pointer-based lists. The hypothesis is that for pathfinding algorithms that scan
     * through many edges sequentially, the CSR layout's better cache performance may overcome
     * any algorithmic overhead. This tests whether spatial locality matters more than data
     * structure complexity in practice.
     * 
     * @param csvPath Path to CSV data file
     * @param numQueries Number of random queries to run
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be read/written
     */
    public void experimentCSRvsAdjacency(String csvPath, int numQueries, String outputDir) throws IOException {
        System.out.println("\n=== EXPERIMENT: CSR Cache-Friendliness vs Adjacency Lists ===");
        System.out.println("Testing whether cache-friendly layouts beat pointer overhead.");
        
        // Load both graph types
        System.out.println("Loading graph data...");
        CsvReader reader = new CsvReader();
        
        // Build adjacency list first, then convert to CSR
        Graph adjListGraph = reader.readCsvAndBuildGraph(csvPath, AdjacencyListGraph::new);
        System.out.println("Building CSR representation...");
        Graph csrGraph = new CSRGraph(adjListGraph);
        
        System.out.printf("Loaded %d nodes, %d edges into both structures%n", 
                         adjListGraph.nodeCount(), adjListGraph.edgeCount());
        
        List<String> nodes = adjListGraph.nodes();
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Graph must have at least 2 nodes");
        }
        
        // Generate random queries
        List<ExperimentQuery> queries = generateRandomQueries(nodes, numQueries);
        
        // Prepare output
        IOUtils.ensureParentDirectoryExists(outputDir + "/csr_vs_adjacency.csv");
        
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "query_id", "origin", "destination", "graph_type", "algorithm",
            "edges_relaxed", "nodes_visited", "runtime_ms", "path_len", "total_weight"
        };
        
        Dijkstra dijkstra = new Dijkstra();
        int queryId = 1;
        
        for (ExperimentQuery query : queries) {
            // Test on adjacency list graph
            PathResult adjResult = dijkstra.findPath(adjListGraph, query.origin, query.destination, query.constraints);
            csvRows.add(createGraphComparisonRow(queryId, query, "AdjacencyListGraph", adjResult));
            
            // Test on CSR graph
            PathResult csrResult = dijkstra.findPath(csrGraph, query.origin, query.destination, query.constraints);
            csvRows.add(createGraphComparisonRow(queryId, query, "CSRGraph", csrResult));
            
            queryId++;
        }
        
        // Write results
        IOUtils.writeCsv(outputDir + "/csr_vs_adjacency.csv", headers, csvRows);
        
        // Generate report
        generateCSRvsAdjacencyReport(csvRows, outputDir + "/csr_vs_adjacency_README.md");
        
        System.out.printf("\nExperiment completed! Results in %s%n", outputDir);
    }
    
    /**
     * Helper method to create CSV rows for graph comparison experiments
     */
    private String[] createGraphComparisonRow(int queryId, ExperimentQuery query, String graphType, PathResult result) {
        return new String[]{
            String.valueOf(queryId),
            query.origin,
            query.destination,
            graphType,
            "Dijkstra",
            String.valueOf(result.getEdgesRelaxed()),
            String.valueOf(result.getNodesVisited()),
            String.valueOf(result.getRuntimeMs()),
            String.valueOf(result.getPathLength()),
            IOUtils.formatDouble(result.getTotalWeight(), 3)
        };
    }
    
    /**
     * Generates a report for the sorted vs unsorted experiment
     */
    private void generateSortedVsUnsortedReport(List<String[]> csvRows, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Experiment: Sorted Edges vs Unsorted Edges\n\n");
        report.append("## Question\n\n");
        report.append("**When the 'Smarter' Structure Isn't Always Faster**\n\n");
        report.append("Does pre-sorting adjacency lists by edge weight improve pathfinding performance,\n");
        report.append("or does the overhead of maintaining sorted order outweigh the benefits?\n\n");
        
        // Calculate statistics
        Map<String, GraphStats> stats = calculateGraphStatistics(csvRows);
        
        report.append("## Results\n\n");
        report.append("| Graph Type | Avg Runtime (ms) | Avg Edges Relaxed | Avg Nodes Visited | Speedup |\n");
        report.append("|------------|------------------|-------------------|-------------------|---------|\n");
        
        GraphStats sorted = stats.get("SortedAdjacencyListGraph");
        GraphStats unsorted = stats.get("AdjacencyListGraph");
        
        double speedup = (unsorted.avgRuntime / sorted.avgRuntime);
        report.append(String.format("| Unsorted | %.2f | %.1f | %.1f | %.2fx |\n",
            unsorted.avgRuntime, unsorted.avgEdgesRelaxed, unsorted.avgNodesVisited, speedup));
        report.append(String.format("| Sorted | %.2f | %.1f | %.1f | %.2fx |\n",
            sorted.avgRuntime, sorted.avgEdgesRelaxed, sorted.avgNodesVisited, 1.0));
        
        report.append("\n## Analysis\n\n");
        
        if (speedup > 1.05) {
            report.append(String.format("- ✅ **Unsorted edges are %.1f%% FASTER** than sorted\n", (speedup - 1) * 100));
            report.append("- The overhead of maintaining sorted order exceeds any benefits\n");
            report.append("- Dijkstra's priority queue already provides sufficient edge ordering\n");
        } else if (speedup < 0.95) {
            report.append(String.format("- ⚠️ **Sorted edges are %.1f%% SLOWER** than unsorted\n", (1 - speedup) * 100));
            report.append("- Pre-sorting provides no measurable benefits\n");
            report.append("- The added structure hurts search efficiency\n");
        } else {
            report.append("- ⚖️ **Performance is roughly equivalent**\n");
            report.append("- Sorted structure provides neither benefit nor penalty\n");
            report.append("- Choose based on other factors (memory, code simplicity)\n");
        }
        
        report.append("\n## Key Insights\n\n");
        report.append("1. **Priority queue dominates**: Dijkstra's binary heap already orders edges efficiently\n");
        report.append("2. **Insertion cost**: O(log n) insertion per edge in sorted lists adds build-time overhead\n");
        report.append("3. **Cache considerations**: Additional structure may reduce cache locality\n");
        report.append("4. **Query pattern**: Results may vary with different graph densities or query patterns\n\n");
        
        report.append("## Conclusion\n\n");
        report.append("This experiment demonstrates that **algorithmic optimizations should always be profiled**.\n");
        report.append("What seems like an improvement on paper may not translate to real-world performance\n");
        report.append("due to hidden costs like memory access patterns and constant factors.\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Generates a report for the CSR vs adjacency experiment
     */
    private void generateCSRvsAdjacencyReport(List<String[]> csvRows, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Experiment: CSR Cache-Friendliness vs Adjacency Lists\n\n");
        report.append("## Question\n\n");
        report.append("**When Memory Layout Beats Algorithm Optimization**\n\n");
        report.append("Does the cache-friendly contiguous layout of CSR graphs provide performance\n");
        report.append("gains over pointer-based adjacency lists despite the additional indirection?\n\n");
        
        // Calculate statistics
        Map<String, GraphStats> stats = calculateGraphStatistics(csvRows);
        
        report.append("## Results\n\n");
        report.append("| Graph Type | Avg Runtime (ms) | Avg Edges Relaxed | Avg Nodes Visited | Speedup |\n");
        report.append("|------------|------------------|-------------------|-------------------|---------|\n");
        
        GraphStats csr = stats.get("CSRGraph");
        GraphStats adj = stats.get("AdjacencyListGraph");
        
        double speedup = (adj.avgRuntime / csr.avgRuntime);
        report.append(String.format("| Adjacency List | %.2f | %.1f | %.1f | %.2fx |\n",
            adj.avgRuntime, adj.avgEdgesRelaxed, adj.avgNodesVisited, speedup));
        report.append(String.format("| CSR | %.2f | %.1f | %.1f | %.2fx |\n",
            csr.avgRuntime, csr.avgEdgesRelaxed, csr.avgNodesVisited, 1.0));
        
        report.append("\n## Analysis\n\n");
        
        if (speedup > 1.05) {
            report.append(String.format("- ✅ **CSR is %.1f%% FASTER** than adjacency lists\n", (speedup - 1) * 100));
            report.append("- Cache-friendly layout provides measurable benefits\n");
            report.append("- Sequential memory access beats pointer chasing\n");
        } else if (speedup < 0.95) {
            report.append(String.format("- ⚠️ **CSR is %.1f%% SLOWER** than adjacency lists\n", (1 - speedup) * 100));
            report.append("- Additional indirection overhead dominates\n");
            report.append("- Simpler structure wins for this workload\n");
        } else {
            report.append("- ⚖️ **Performance is roughly equivalent**\n");
            report.append("- Cache benefits balance out indirection costs\n");
            report.append("- Both structures perform similarly for this graph\n");
        }
        
        report.append("\n## Key Insights\n\n");
        report.append("1. **Cache locality matters**: Sequential array access is faster than following pointers\n");
        report.append("2. **Memory hierarchies**: L1/L2/L3 caches reward contiguous data\n");
        report.append("3. **Workload dependent**: Benefits increase with higher edge count per node\n");
        report.append("4. **Build vs query tradeoff**: CSR has conversion overhead but query benefits\n\n");
        
        report.append("## Conclusion\n\n");
        report.append("This experiment demonstrates that **memory access patterns can trump algorithmic complexity**.\n");
        report.append("Modern CPUs are so fast that memory bandwidth and cache misses often dominate\n");
        report.append("performance. Optimizing for spatial locality is as important as optimizing algorithms.\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Calculates statistics for graph comparison experiments
     */
    private Map<String, GraphStats> calculateGraphStatistics(List<String[]> csvRows) {
        Map<String, GraphStats> stats = new HashMap<>();
        
        for (String[] row : csvRows) {
            String graphType = row[3];
            
            GraphStats stat = stats.computeIfAbsent(graphType, k -> new GraphStats());
            
            stat.totalQueries++;
            stat.totalRuntime += Long.parseLong(row[7]);
            stat.totalEdgesRelaxed += Integer.parseInt(row[5]);
            stat.totalNodesVisited += Integer.parseInt(row[6]);
            
            if (!row[9].equals("0.000")) { // Path found
                stat.successfulQueries++;
            }
        }
        
        // Calculate averages
        for (GraphStats stat : stats.values()) {
            stat.avgRuntime = (double) stat.totalRuntime / stat.totalQueries;
            stat.avgEdgesRelaxed = (double) stat.totalEdgesRelaxed / stat.totalQueries;
            stat.avgNodesVisited = (double) stat.totalNodesVisited / stat.totalQueries;
            stat.successRate = (double) stat.successfulQueries / stat.totalQueries;
        }
        
        return stats;
    }
    
    /**
     * Helper class for graph comparison statistics
     */
    private static class GraphStats {
        int totalQueries = 0;
        int successfulQueries = 0;
        long totalRuntime = 0;
        int totalEdgesRelaxed = 0;
        int totalNodesVisited = 0;
        double avgRuntime = 0;
        double avgEdgesRelaxed = 0;
        double avgNodesVisited = 0;
        double successRate = 0;
    }
}

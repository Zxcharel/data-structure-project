package src.experiments;

import src.algo.Dijkstra;
import src.algo.PathResult;
import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Experiment 1: Pathfinding Performance Benchmark
 * 
 * Provides a comprehensive, end-to-end performance comparison of all graph 
 * implementations when running Dijkstra's algorithm. Tests complete pathfinding 
 * scenarios to identify which structures perform best in real-world usage.
 */
public class PathfindingBenchmarkExperiment {
    
    private static final String DEFAULT_CSV_PATH = "data/cleaned_flights.csv";
    private static final int NUM_RUNS_PER_QUERY = 5;
    private static final Random random = new Random(42); // Fixed seed for reproducibility
    
    /**
     * Runs the pathfinding benchmark experiment
     */
    public void runBenchmark(String csvPath, int numQueries, String outputDir) throws IOException {
        System.out.println("=== Experiment 1: Pathfinding Performance Benchmark ===");
        System.out.println("Building all graph types from: " + csvPath);
        
        // Build all graph types
        Map<String, Graph> graphs = buildAllGraphTypes(csvPath);
        System.out.println("Built " + graphs.size() + " graph types\n");
        
        // Generate test queries
        System.out.println("Generating " + numQueries + " test queries...");
        List<Query> queries = generateQueries(graphs.values().iterator().next(), numQueries);
        System.out.println("Generated queries\n");
        
        // Run Dijkstra on each graph for each query
        System.out.println("Running Dijkstra's algorithm...");
        Map<String, List<QueryResult>> results = new HashMap<>();
        Dijkstra dijkstra = new Dijkstra();
        
        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String graphType = entry.getKey();
            Graph graph = entry.getValue();
            System.out.println("Testing " + graphType + "...");
            
            List<QueryResult> graphResults = new ArrayList<>();
            for (Query query : queries) {
                // Run multiple times for averaging
                List<QueryResult> runResults = new ArrayList<>();
                for (int run = 0; run < NUM_RUNS_PER_QUERY; run++) {
                    PathResult pathResult = dijkstra.findPath(graph, query.origin, query.destination);
                    runResults.add(extractMetrics(query, pathResult, graph));
                }
                // Average the runs
                graphResults.add(averageResults(runResults));
            }
            results.put(graphType, graphResults);
        }
        
        // Analyze and write results
        System.out.println("\nAnalyzing results...");
        analyzeAndWriteResults(results, outputDir);
        System.out.println("Benchmark completed! Results written to: " + outputDir);
    }
    
    /**
     * Builds all graph types from CSV
     */
    private Map<String, Graph> buildAllGraphTypes(String csvPath) throws IOException {
        Map<String, Graph> graphs = new LinkedHashMap<>();
        CsvReader reader = new CsvReader();
        
        // Build base graph first (for CSR and Matrix)
        Graph baseGraph = reader.readCsvAndBuildGraph(csvPath);
        
        // 1. AdjacencyListGraph (default)
        graphs.put("AdjacencyListGraph", baseGraph);
        
        // 2. SortedAdjacencyListGraph
        graphs.put("SortedAdjacencyListGraph", 
            reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new));
        
        // 3. MatrixGraph (needs maxNodes)
        int nodeCount = baseGraph.nodeCount();
        MatrixGraph matrixGraph = new MatrixGraph(nodeCount * 2);
        reader.readCsvAndBuildGraph(csvPath, matrixGraph);
        graphs.put("MatrixGraph", matrixGraph);
        
        // 4. CSRGraph (built from existing graph)
        graphs.put("CSRGraph", new CSRGraph(baseGraph));
        
        // 5-13. Other graph types
        graphs.put("DoublyLinkedListGraph", 
            reader.readCsvAndBuildGraph(csvPath, DoublyLinkedListGraph::new));
        graphs.put("CircularLinkedListGraph", 
            reader.readCsvAndBuildGraph(csvPath, CircularLinkedListGraph::new));
        graphs.put("HalfEdgeGraph", 
            reader.readCsvAndBuildGraph(csvPath, HalfEdgeGraph::new));
        graphs.put("LinearArrayGraph", 
            reader.readCsvAndBuildGraph(csvPath, LinearArrayGraph::new));
        graphs.put("DynamicArrayGraph", 
            reader.readCsvAndBuildGraph(csvPath, DynamicArrayGraph::new));
        graphs.put("OffsetArrayGraph", 
            reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new));
        graphs.put("RoutePartitionedTrieGraph", 
            reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new));
        graphs.put("LinkCutTreeGraph", 
            reader.readCsvAndBuildGraph(csvPath, LinkCutTreeGraph::new));
        graphs.put("EulerTourTreeGraph", 
            reader.readCsvAndBuildGraph(csvPath, EulerTourTreeGraph::new));
        
        return graphs;
    }
    
    /**
     * Generates diverse test queries (short, medium, long paths)
     */
    private List<Query> generateQueries(Graph graph, int numQueries) {
        List<String> nodes = graph.nodes();
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Graph must have at least 2 nodes");
        }
        
        List<Query> queries = new ArrayList<>();
        int shortPaths = numQueries * 30 / 100;  // 30%
        int mediumPaths = numQueries * 40 / 100; // 40%
        int longPaths = numQueries - shortPaths - mediumPaths; // 30%
        
        // Generate random queries (categorization by path length happens during analysis)
        for (int i = 0; i < numQueries; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String destination = nodes.get(random.nextInt(nodes.size()));
            
            while (destination.equals(origin)) {
                destination = nodes.get(random.nextInt(nodes.size()));
            }
            
            queries.add(new Query(origin, destination));
        }
        
        return queries;
    }
    
    /**
     * Extracts metrics from a pathfinding result
     */
    private QueryResult extractMetrics(Query query, PathResult result, Graph graph) {
        long memoryEstimate = estimateMemoryUsage(graph);
        return new QueryResult(
            query.origin,
            query.destination,
            result.getRuntimeMs(),
            result.getNodesVisited(),
            result.getEdgesRelaxed(),
            result.getPathLength(),
            result.isFound(),
            result.getTotalWeight(),
            memoryEstimate
        );
    }
    
    /**
     * Averages multiple query results
     */
    private QueryResult averageResults(List<QueryResult> results) {
        if (results.isEmpty()) {
            return null;
        }
        
        QueryResult first = results.get(0);
        double avgRuntime = results.stream().mapToLong(r -> r.runtimeMs).average().orElse(0);
        double avgNodesVisited = results.stream().mapToInt(r -> r.nodesVisited).average().orElse(0);
        double avgEdgesRelaxed = results.stream().mapToInt(r -> r.edgesRelaxed).average().orElse(0);
        int successes = (int) results.stream().filter(r -> r.pathFound).count();
        
        return new QueryResult(
            first.origin,
            first.destination,
            (long) avgRuntime,
            (int) avgNodesVisited,
            (int) avgEdgesRelaxed,
            first.pathLength,
            successes > results.size() / 2,
            first.totalWeight,
            first.memoryEstimate
        );
    }
    
    /**
     * Estimates memory usage (rough estimate)
     */
    private long estimateMemoryUsage(Graph graph) {
        // Rough estimate: node count * 100 bytes + edge count * 50 bytes
        return graph.nodeCount() * 100L + graph.edgeCount() * 50L;
    }
    
    /**
     * Analyzes results and writes to files
     */
    private void analyzeAndWriteResults(Map<String, List<QueryResult>> results, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/benchmark.csv");
        
        // Write detailed CSV
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "graph_type", "query_id", "origin", "destination", "runtime_ms",
            "nodes_visited", "edges_relaxed", "path_length", "path_found",
            "total_weight", "memory_estimate_bytes"
        };
        
        int queryId = 0;
        for (Map.Entry<String, List<QueryResult>> entry : results.entrySet()) {
            String graphType = entry.getKey();
            for (QueryResult result : entry.getValue()) {
                csvRows.add(new String[]{
                    graphType,
                    String.valueOf(queryId++),
                    result.origin,
                    result.destination,
                    String.valueOf(result.runtimeMs),
                    String.valueOf(result.nodesVisited),
                    String.valueOf(result.edgesRelaxed),
                    String.valueOf(result.pathLength),
                    String.valueOf(result.pathFound),
                    IOUtils.formatDouble(result.totalWeight, 3),
                    String.valueOf(result.memoryEstimate)
                });
            }
            queryId = 0; // Reset for next graph type
        }
        
        IOUtils.writeCsv(outputDir + "/benchmark.csv", headers, csvRows);
        
        // Write summary report
        writeSummaryReport(results, outputDir + "/summary.md");
    }
    
    /**
     * Writes a summary report in Markdown format
     */
    private void writeSummaryReport(Map<String, List<QueryResult>> results, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Pathfinding Performance Benchmark - Summary\n\n");
        report.append("## Overall Performance Rankings\n\n");
        
        // Calculate statistics per graph type
        Map<String, GraphStats> stats = new HashMap<>();
        for (Map.Entry<String, List<QueryResult>> entry : results.entrySet()) {
            stats.put(entry.getKey(), calculateStats(entry.getValue()));
        }
        
        // Sort by average runtime
        List<Map.Entry<String, GraphStats>> sorted = new ArrayList<>(stats.entrySet());
        sorted.sort(Comparator.comparing(e -> e.getValue().avgRuntime));
        
        report.append("| Graph Type | Avg Runtime (ms) | Avg Nodes Visited | Avg Edges Relaxed | Success Rate |\n");
        report.append("|-----------|------------------|-------------------|-------------------|-------------|\n");
        
        for (Map.Entry<String, GraphStats> entry : sorted) {
            GraphStats stat = entry.getValue();
            report.append(String.format("| %s | %.2f | %.1f | %.1f | %.1f%% |\n",
                entry.getKey(), stat.avgRuntime, stat.avgNodesVisited,
                stat.avgEdgesRelaxed, stat.successRate * 100));
        }
        
        report.append("\n## Key Findings\n\n");
        
        if (!sorted.isEmpty()) {
            Map.Entry<String, GraphStats> fastest = sorted.get(0);
            report.append(String.format("- **Fastest**: %s (%.2f ms avg)\n", 
                fastest.getKey(), fastest.getValue().avgRuntime));
            
            // Find relative speedup vs baseline (AdjacencyListGraph)
            GraphStats baseline = stats.get("AdjacencyListGraph");
            if (baseline != null) {
                for (Map.Entry<String, GraphStats> entry : sorted) {
                    double speedup = baseline.avgRuntime / entry.getValue().avgRuntime;
                    if (speedup > 1.1) {
                        report.append(String.format("- **Speedup**: %s is %.1fx faster than AdjacencyListGraph\n",
                            entry.getKey(), speedup));
                    }
                }
            }
        }
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Calculates statistics for a graph type
     */
    private GraphStats calculateStats(List<QueryResult> results) {
        GraphStats stats = new GraphStats();
        
        for (QueryResult result : results) {
            stats.totalQueries++;
            stats.totalRuntime += result.runtimeMs;
            stats.totalNodesVisited += result.nodesVisited;
            stats.totalEdgesRelaxed += result.edgesRelaxed;
            
            if (result.pathFound) {
                stats.successfulQueries++;
            }
        }
        
        stats.avgRuntime = (double) stats.totalRuntime / stats.totalQueries;
        stats.avgNodesVisited = (double) stats.totalNodesVisited / stats.totalQueries;
        stats.avgEdgesRelaxed = (double) stats.totalEdgesRelaxed / stats.totalQueries;
        stats.successRate = (double) stats.successfulQueries / stats.totalQueries;
        
        return stats;
    }
    
    /**
     * Helper classes
     */
    private static class Query {
        final String origin;
        final String destination;
        
        Query(String origin, String destination) {
            this.origin = origin;
            this.destination = destination;
        }
    }
    
    private static class QueryResult {
        final String origin;
        final String destination;
        final long runtimeMs;
        final int nodesVisited;
        final int edgesRelaxed;
        final int pathLength;
        final boolean pathFound;
        final double totalWeight;
        final long memoryEstimate;
        
        QueryResult(String origin, String destination, long runtimeMs, int nodesVisited,
                   int edgesRelaxed, int pathLength, boolean pathFound, double totalWeight,
                   long memoryEstimate) {
            this.origin = origin;
            this.destination = destination;
            this.runtimeMs = runtimeMs;
            this.nodesVisited = nodesVisited;
            this.edgesRelaxed = edgesRelaxed;
            this.pathLength = pathLength;
            this.pathFound = pathFound;
            this.totalWeight = totalWeight;
            this.memoryEstimate = memoryEstimate;
        }
    }
    
    private static class GraphStats {
        int totalQueries = 0;
        int successfulQueries = 0;
        long totalRuntime = 0;
        int totalNodesVisited = 0;
        int totalEdgesRelaxed = 0;
        double avgRuntime = 0;
        double avgNodesVisited = 0;
        double avgEdgesRelaxed = 0;
        double successRate = 0;
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) throws IOException {
        PathfindingBenchmarkExperiment experiment = new PathfindingBenchmarkExperiment();
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        int numQueries = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        String outputDir = args.length > 2 ? args[2] : "out/experiments/benchmark";
        
        experiment.runBenchmark(csvPath, numQueries, outputDir);
    }
}


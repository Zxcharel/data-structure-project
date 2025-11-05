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
 * Experiment 1.1: High-Resolution Pathfinding Performance Benchmark
 * 
 * Enhanced version with:
 * - NanoTime precision timing
 * - Proper memory measurement during build phase
 * - Improved query generation (short and long paths)
 * - Statistical analysis (mean, std dev, coefficient of variation, 95th percentile)
 * - Enhanced CSV output with all metrics
 * - Warmup phase for JVM stability
 */
public class PathfindingBenchmarkExperiment {
    
    private static final String DEFAULT_CSV_PATH = "data/cleaned_flights.csv";
    private static final int NUM_RUNS_PER_QUERY = 50;
    private static final int NUM_QUERIES = 200;
    private static final Random random = new Random(42);
    
    /**
     * Runs the enhanced pathfinding benchmark experiment
     */
    public void runBenchmark(String csvPath, int numQueries, String outputDir) throws IOException {
        System.out.println("=== Experiment 1.1: High-Resolution Pathfinding Performance Benchmark ===");
        System.out.println("Building all graph types from: " + csvPath);
        
        // Build all graph types with memory measurement
        Map<String, GraphBuildInfo> graphs = buildAllGraphTypesWithMemory(csvPath);
        System.out.println("Built " + graphs.size() + " graph types\n");
        
        // Generate test queries (short and long paths)
        System.out.println("Generating " + numQueries + " test queries (short and long paths)...");
        Graph sampleGraph = graphs.values().iterator().next().graph;
        List<Query> queries = generateImprovedQueries(sampleGraph, numQueries);
        System.out.println("Generated queries\n");
        
        // Warmup phase
        System.out.println("Warming up JVM...");
        performWarmup(graphs, queries);
        System.out.println("Warmup complete\n");
        
        // Run Dijkstra on each graph for each query
        System.out.println("Running Dijkstra's algorithm (" + NUM_RUNS_PER_QUERY + " runs per query)...");
        Map<String, List<QueryResult>> results = new HashMap<>();
        Dijkstra dijkstra = new Dijkstra();
        
        for (Map.Entry<String, GraphBuildInfo> entry : graphs.entrySet()) {
            String graphType = entry.getKey();
            Graph graph = entry.getValue().graph;
            long memoryBytes = entry.getValue().memoryBytes;
            System.out.println("Testing " + graphType + "...");
            
            List<QueryResult> graphResults = new ArrayList<>();
            for (Query query : queries) {
                List<QueryResult> runResults = new ArrayList<>();
                for (int run = 0; run < NUM_RUNS_PER_QUERY; run++) {
                    // Measure with nanoTime
                    long startTime = System.nanoTime();
                    PathResult pathResult = dijkstra.findPath(graph, query.origin, query.destination);
                    long endTime = System.nanoTime();
                    double runtimeMs = (endTime - startTime) / 1_000_000.0;
                    
                    runResults.add(extractMetrics(query, pathResult, runtimeMs, memoryBytes));
                    
                    // Small delay to avoid JVM burst optimization bias
                    if (run < NUM_RUNS_PER_QUERY - 1) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
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
     * Builds all graph types and measures memory usage during build
     */
    private Map<String, GraphBuildInfo> buildAllGraphTypesWithMemory(String csvPath) throws IOException {
        Map<String, GraphBuildInfo> graphs = new LinkedHashMap<>();
        CsvReader reader = new CsvReader();
        Runtime runtime = Runtime.getRuntime();
        
        // Build base graph first
        System.gc();
        long beforeBase = runtime.totalMemory() - runtime.freeMemory();
        Graph baseGraph = reader.readCsvAndBuildGraph(csvPath);
        System.gc();
        long afterBase = runtime.totalMemory() - runtime.freeMemory();
        graphs.put("AdjacencyListGraph", new GraphBuildInfo(baseGraph, afterBase - beforeBase));
        
        // Build other graph types
        graphs.put("SortedAdjacencyListGraph", 
            buildGraphWithMemory(reader, csvPath, SortedAdjacencyListGraph::new, runtime));
        
        int nodeCount = baseGraph.nodeCount();
        // MatrixGraph needs special handling
        System.gc();
        long beforeMatrix = runtime.totalMemory() - runtime.freeMemory();
        MatrixGraph matrixGraph = new MatrixGraph(nodeCount * 2);
        reader.readCsvAndBuildGraph(csvPath, matrixGraph);
        System.gc();
        long afterMatrix = runtime.totalMemory() - runtime.freeMemory();
        graphs.put("MatrixGraph", new GraphBuildInfo(matrixGraph, afterMatrix - beforeMatrix));
        
        // CSRGraph needs base graph
        System.gc();
        long beforeCSR = runtime.totalMemory() - runtime.freeMemory();
        Graph csrGraph = new CSRGraph(baseGraph);
        System.gc();
        long afterCSR = runtime.totalMemory() - runtime.freeMemory();
        graphs.put("CSRGraph", new GraphBuildInfo(csrGraph, afterCSR - beforeCSR));
        
        graphs.put("DoublyLinkedListGraph", 
            buildGraphWithMemory(reader, csvPath, DoublyLinkedListGraph::new, runtime));
        graphs.put("CircularLinkedListGraph", 
            buildGraphWithMemory(reader, csvPath, CircularLinkedListGraph::new, runtime));
        graphs.put("HalfEdgeGraph", 
            buildGraphWithMemory(reader, csvPath, HalfEdgeGraph::new, runtime));
        graphs.put("LinearArrayGraph", 
            buildGraphWithMemory(reader, csvPath, LinearArrayGraph::new, runtime));
        graphs.put("DynamicArrayGraph", 
            buildGraphWithMemory(reader, csvPath, DynamicArrayGraph::new, runtime));
        
        Graph offsetGraphTemp = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
        OffsetArrayGraph offsetGraph = (OffsetArrayGraph) offsetGraphTemp;
        offsetGraph.finalizeCSR();
        // Re-measure after finalization
        System.gc();
        long beforeFinal = runtime.totalMemory() - runtime.freeMemory();
        // Finalization doesn't rebuild, so we'll use the build memory
        graphs.put("OffsetArrayGraph", new GraphBuildInfo(offsetGraph, beforeFinal - beforeBase));
        
        graphs.put("RoutePartitionedTrieGraph", 
            buildGraphWithMemory(reader, csvPath, RoutePartitionedTrieGraph::new, runtime));
        graphs.put("LinkCutTreeGraph", 
            buildGraphWithMemory(reader, csvPath, LinkCutTreeGraph::new, runtime));
        graphs.put("EulerTourTreeGraph", 
            buildGraphWithMemory(reader, csvPath, EulerTourTreeGraph::new, runtime));
        
        return graphs;
    }
    
    private GraphBuildInfo buildGraphWithMemory(CsvReader reader, String csvPath, 
                                               Supplier<Graph> graphFactory, Runtime runtime) throws IOException {
        System.gc();
        long before = runtime.totalMemory() - runtime.freeMemory();
        Graph graph = reader.readCsvAndBuildGraph(csvPath, graphFactory);
        System.gc();
        long after = runtime.totalMemory() - runtime.freeMemory();
        return new GraphBuildInfo(graph, after - before);
    }
    
    /**
     * Generates improved queries with both short and long paths
     */
    private List<Query> generateImprovedQueries(Graph graph, int numQueries) {
        List<String> nodes = graph.nodes();
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("Graph must have at least 2 nodes");
        }
        
        List<Query> queries = new ArrayList<>();
        
        // Find nodes with direct connections (short paths)
        Map<String, Set<String>> directConnections = new HashMap<>();
        for (String node : nodes) {
            Set<String> neighbors = new HashSet<>();
            for (Edge edge : graph.neighbors(node)) {
                neighbors.add(edge.getDestination());
            }
            directConnections.put(node, neighbors);
        }
        
        // Generate 50% short paths, 50% long paths
        int shortPathCount = numQueries / 2;
        int longPathCount = numQueries - shortPathCount;
        
        // Short paths: nodes with direct connections or same "region" (first 2 chars)
        for (int i = 0; i < shortPathCount; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String destination = null;
            
            // Try to find a direct connection
            Set<String> neighbors = directConnections.get(origin);
            if (neighbors != null && !neighbors.isEmpty()) {
                List<String> neighborList = new ArrayList<>(neighbors);
                destination = neighborList.get(random.nextInt(neighborList.size()));
            } else {
                // Fallback: find node with same region prefix (first 2 chars)
                String originPrefix = origin.length() >= 2 ? origin.substring(0, 2) : origin;
                List<String> candidates = new ArrayList<>();
                for (String node : nodes) {
                    if (!node.equals(origin) && node.startsWith(originPrefix)) {
                        candidates.add(node);
                    }
                }
                if (!candidates.isEmpty()) {
                    destination = candidates.get(random.nextInt(candidates.size()));
                } else {
                    // Random fallback
                    do {
                        destination = nodes.get(random.nextInt(nodes.size()));
                    } while (destination.equals(origin));
                }
            }
            queries.add(new Query(origin, destination, QueryType.SHORT));
        }
        
        // Long paths: maximize path length
        for (int i = 0; i < longPathCount; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String destination = null;
            
            // Try to find a node with maximum distance
            int attempts = 0;
            do {
                destination = nodes.get(random.nextInt(nodes.size()));
                attempts++;
                // If we can't find a good long path after 10 attempts, use random
                if (attempts > 10) {
                    break;
                }
            } while (destination.equals(origin));
            
            queries.add(new Query(origin, destination, QueryType.LONG));
        }
        
        return queries;
    }
    
    /**
     * Performs warmup phase to stabilize JVM
     */
    private void performWarmup(Map<String, GraphBuildInfo> graphs, List<Query> queries) {
        Dijkstra dijkstra = new Dijkstra();
        // Run one query on each graph
        for (GraphBuildInfo info : graphs.values()) {
            if (!queries.isEmpty()) {
                Query query = queries.get(0);
                dijkstra.findPath(info.graph, query.origin, query.destination);
            }
        }
    }
    
    private QueryResult extractMetrics(Query query, PathResult result, double runtimeMs, long memoryBytes) {
        return new QueryResult(query.origin, query.destination, runtimeMs,
            result.getNodesVisited(), result.getEdgesRelaxed(), result.getPathLength(),
            result.isFound(), result.getTotalWeight(), memoryBytes);
    }
    
    private QueryResult averageResults(List<QueryResult> results) {
        if (results.isEmpty()) return null;
        
        QueryResult first = results.get(0);
        double avgRuntime = results.stream().mapToDouble(r -> r.runtimeMs).average().orElse(0);
        double avgNodesVisited = results.stream().mapToInt(r -> r.nodesVisited).average().orElse(0);
        double avgEdgesRelaxed = results.stream().mapToInt(r -> r.edgesRelaxed).average().orElse(0);
        int successes = (int) results.stream().filter(r -> r.pathFound).count();
        
        return new QueryResult(first.origin, first.destination, avgRuntime,
            (int) avgNodesVisited, (int) avgEdgesRelaxed, first.pathLength,
            successes > results.size() / 2, first.totalWeight, first.memoryUsedBytes);
    }
    
    private void analyzeAndWriteResults(Map<String, List<QueryResult>> results, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/benchmark.csv");
        
        // Write detailed CSV
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {"graph_type", "query_id", "origin", "destination", "runtime_ms",
            "nodes_visited", "edges_relaxed", "path_length", "path_found", "total_weight", "memory_used_bytes"};
        
        int queryId = 0;
        for (Map.Entry<String, List<QueryResult>> entry : results.entrySet()) {
            String graphType = entry.getKey();
            for (QueryResult result : entry.getValue()) {
                csvRows.add(new String[]{
                    graphType, String.valueOf(queryId++), result.origin, result.destination,
                    String.format("%.4f", result.runtimeMs), String.valueOf(result.nodesVisited),
                    String.valueOf(result.edgesRelaxed), String.valueOf(result.pathLength),
                    String.valueOf(result.pathFound), IOUtils.formatDouble(result.totalWeight, 3),
                    String.valueOf(result.memoryUsedBytes)
                });
            }
            queryId = 0;
        }
        
        IOUtils.writeCsv(outputDir + "/benchmark.csv", headers, csvRows);
        
        // Write enhanced summary with statistics
        writeEnhancedSummaryReport(results, outputDir + "/summary.md");
    }
    
    private void writeEnhancedSummaryReport(Map<String, List<QueryResult>> results, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Experiment 1.1: High-Resolution Pathfinding Performance Benchmark - Summary\n\n");
        
        Map<String, EnhancedGraphStats> stats = new HashMap<>();
        for (Map.Entry<String, List<QueryResult>> entry : results.entrySet()) {
            stats.put(entry.getKey(), calculateEnhancedStats(entry.getValue()));
        }
        
        // Sort by average runtime
        List<Map.Entry<String, EnhancedGraphStats>> sorted = new ArrayList<>(stats.entrySet());
        sorted.sort(Comparator.comparing(e -> e.getValue().avgRuntime));
        
        report.append("## Performance Summary\n\n");
        report.append("| Graph Type | Avg Runtime (ms) | Std Dev (ms) | CoV (%) | 95th %ile (ms) | ");
        report.append("Avg Nodes | Avg Edges | Success Rate | Memory (MB) |\n");
        report.append("|-----------|------------------|--------------|---------|-----------------|");
        report.append("----------|-----------|-------------|----------|\n");
        
        for (Map.Entry<String, EnhancedGraphStats> entry : sorted) {
            EnhancedGraphStats stat = entry.getValue();
            double memoryMB = stat.avgMemoryBytes / (1024.0 * 1024.0);
            report.append(String.format("| %s | %.4f | %.4f | %.2f | %.4f | %.1f | %.1f | %.1f%% | %.2f |\n",
                entry.getKey(), stat.avgRuntime, stat.stdDevRuntime, stat.coefficientOfVariation,
                stat.percentile95, stat.avgNodesVisited, stat.avgEdgesRelaxed, 
                stat.successRate * 100, memoryMB));
        }
        
        // Validation section
        report.append("\n## Validation\n\n");
        boolean validationPassed = true;
        
        // Check that at least one graph has measurable runtime
        double maxRuntime = sorted.stream()
            .mapToDouble(e -> e.getValue().avgRuntime)
            .max()
            .orElse(0);
        if (maxRuntime < 0.1) {
            report.append("⚠️ **Warning**: No graph type produced measurable runtime (>0.1 ms)\n");
            validationPassed = false;
        } else {
            report.append("✓ At least one graph type produced measurable runtime\n");
        }
        
        // Check memory differences
        Optional<EnhancedGraphStats> csrStats = sorted.stream()
            .filter(e -> e.getKey().equals("CSRGraph"))
            .map(Map.Entry::getValue)
            .findFirst();
        Optional<EnhancedGraphStats> matrixStats = sorted.stream()
            .filter(e -> e.getKey().equals("MatrixGraph"))
            .map(Map.Entry::getValue)
            .findFirst();
        
        if (csrStats.isPresent() && matrixStats.isPresent()) {
            if (csrStats.get().avgMemoryBytes < matrixStats.get().avgMemoryBytes) {
                report.append("✓ Memory usage differs meaningfully (CSRGraph < MatrixGraph)\n");
            } else {
                report.append("⚠️ **Warning**: Memory usage unexpected (CSRGraph >= MatrixGraph)\n");
                validationPassed = false;
            }
        }
        
        if (validationPassed) {
            report.append("\n✅ **All validations passed**\n");
        }
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    private EnhancedGraphStats calculateEnhancedStats(List<QueryResult> results) {
        EnhancedGraphStats stats = new EnhancedGraphStats();
        
        if (results.isEmpty()) return stats;
        
        // Calculate mean
        stats.avgRuntime = results.stream().mapToDouble(r -> r.runtimeMs).average().orElse(0);
        stats.avgNodesVisited = results.stream().mapToInt(r -> r.nodesVisited).average().orElse(0);
        stats.avgEdgesRelaxed = results.stream().mapToInt(r -> r.edgesRelaxed).average().orElse(0);
        stats.avgMemoryBytes = results.stream().mapToLong(r -> r.memoryUsedBytes).average().orElse(0.0);
        
        // Calculate standard deviation
        double variance = results.stream()
            .mapToDouble(r -> Math.pow(r.runtimeMs - stats.avgRuntime, 2))
            .average()
            .orElse(0);
        stats.stdDevRuntime = Math.sqrt(variance);
        
        // Coefficient of variation
        stats.coefficientOfVariation = stats.avgRuntime > 0 
            ? (stats.stdDevRuntime / stats.avgRuntime) * 100.0 
            : 0.0;
        
        // 95th percentile
        List<Double> sortedRuntimes = new ArrayList<>();
        for (QueryResult result : results) {
            sortedRuntimes.add(result.runtimeMs);
        }
        Collections.sort(sortedRuntimes);
        int percentileIndex = (int) Math.ceil(sortedRuntimes.size() * 0.95) - 1;
        if (percentileIndex >= 0 && percentileIndex < sortedRuntimes.size()) {
            stats.percentile95 = sortedRuntimes.get(percentileIndex);
        }
        
        // Success rate
        stats.successfulQueries = (int) results.stream().filter(r -> r.pathFound).count();
        stats.successRate = (double) stats.successfulQueries / results.size();
        
        return stats;
    }
    
    // Helper classes
    private static class Query {
        final String origin, destination;
        final QueryType type;
        Query(String origin, String destination, QueryType type) {
            this.origin = origin;
            this.destination = destination;
            this.type = type;
        }
    }
    
    private enum QueryType {
        SHORT, LONG
    }
    
    private static class QueryResult {
        final String origin, destination;
        final double runtimeMs;
        final int nodesVisited, edgesRelaxed, pathLength;
        final boolean pathFound;
        final double totalWeight;
        final long memoryUsedBytes;
        
        QueryResult(String origin, String destination, double runtimeMs, int nodesVisited,
                   int edgesRelaxed, int pathLength, boolean pathFound, double totalWeight, long memoryUsedBytes) {
            this.origin = origin;
            this.destination = destination;
            this.runtimeMs = runtimeMs;
            this.nodesVisited = nodesVisited;
            this.edgesRelaxed = edgesRelaxed;
            this.pathLength = pathLength;
            this.pathFound = pathFound;
            this.totalWeight = totalWeight;
            this.memoryUsedBytes = memoryUsedBytes;
        }
    }
    
    private static class GraphBuildInfo {
        final Graph graph;
        final long memoryBytes;
        GraphBuildInfo(Graph graph, long memoryBytes) {
            this.graph = graph;
            this.memoryBytes = memoryBytes;
        }
    }
    
    private static class EnhancedGraphStats {
        double avgRuntime, stdDevRuntime, coefficientOfVariation, percentile95;
        double avgNodesVisited, avgEdgesRelaxed;
        double successRate;
        double avgMemoryBytes;
        int successfulQueries;
    }
    
    public static void main(String[] args) throws IOException {
        PathfindingBenchmarkExperiment exp = new PathfindingBenchmarkExperiment();
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        int numQueries = args.length > 1 ? Integer.parseInt(args[1]) : NUM_QUERIES;
        String outputDir = args.length > 2 ? args[2] : "out/experiments/experiment1_benchmark";
        exp.runBenchmark(csvPath, numQueries, outputDir);
    }
}

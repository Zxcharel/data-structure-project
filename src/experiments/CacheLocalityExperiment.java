package src.experiments;

import src.algo.Dijkstra;
import src.algo.PathResult;
import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import src.util.Stopwatch;
import java.io.IOException;
import java.util.*;

/**
 * Experiment 4: Memory Layout vs Algorithm Optimization (CSR vs Adjacency Lists)
 * 
 * Tests whether cache-friendly memory layout (CSR) outperforms pointer-based 
 * structures (Adjacency Lists) even when algorithmic overhead exists.
 */
public class CacheLocalityExperiment {
    
    private static final String DEFAULT_CSV_PATH = "data/cleaned_flights.csv";
    private static final int NUM_RUNS_PER_QUERY = 5;
    private static final int NUM_ITERATION_TESTS = 100;
    private static final int WARMUP_ITERATIONS = 5;
    private static final Random random = new Random(42);
    
    /**
     * Runs the cache locality experiment
     */
    public void runExperiment(String csvPath, int numQueries, String outputDir) throws IOException {
        System.out.println("=== Experiment 4: Cache Locality (CSR vs Adjacency Lists) ===");
        System.out.println("Building graphs from: " + csvPath);
        
        // Build both graph types
        CsvReader reader = new CsvReader();
        Graph adjacencyListGraph = reader.readCsvAndBuildGraph(csvPath);
        Graph csrGraph = new CSRGraph(adjacencyListGraph);
        
        System.out.println("AdjacencyListGraph: " + adjacencyListGraph.nodeCount() + " nodes, " + 
                          adjacencyListGraph.edgeCount() + " edges");
        System.out.println("CSRGraph: " + csrGraph.nodeCount() + " nodes, " + 
                          csrGraph.edgeCount() + " edges\n");
        
        // Measure build time
        Stopwatch buildTimer = new Stopwatch();
        buildTimer.start();
        Graph testCSR = new CSRGraph(adjacencyListGraph);
        buildTimer.stop();
        long csrBuildOverhead = buildTimer.getElapsedMs();
        
        System.out.println("CSR build overhead: " + csrBuildOverhead + " ms\n");
        
        // Generate test queries
        List<Query> queries = generateQueries(adjacencyListGraph, numQueries);
        
        // Measure neighbor iteration performance
        System.out.println("Measuring neighbor iteration performance...");
        Map<String, IterationStats> iterationStats = measureNeighborIteration(adjacencyListGraph, csrGraph);
        
        // Run full Dijkstra comparison
        System.out.println("Running Dijkstra's algorithm on " + numQueries + " queries...");
        Map<String, List<QueryResult>> dijkstraResults = runDijkstraComparison(
            adjacencyListGraph, csrGraph, queries);
        
        // Analyze and write results
        System.out.println("\nAnalyzing results...");
        analyzeAndWriteResults(iterationStats, dijkstraResults, csrBuildOverhead, outputDir);
        System.out.println("Experiment completed! Results written to: " + outputDir);
    }
    
    private Map<String, IterationStats> measureNeighborIteration(Graph adjGraph, Graph csrGraph) {
        Map<String, IterationStats> stats = new HashMap<>();
        
        // Use ALL nodes for testing
        List<String> nodes = adjGraph.nodes();
        List<String> testNodes = new ArrayList<>(nodes);  // Use ALL nodes
        
        // Measure iteration for AdjacencyList
        long adjTotalTime = 0;
        int adjEdgeCount = 0;
        for (String node : testNodes) {
            for (int i = 0; i < NUM_ITERATION_TESTS; i++) {
                if (i < WARMUP_ITERATIONS) {
                    adjGraph.neighbors(node);
                    continue;
                }
                long start = System.nanoTime();
                List<Edge> neighbors = adjGraph.neighbors(node);
                long end = System.nanoTime();
                adjTotalTime += (end - start);
                adjEdgeCount += neighbors.size();
            }
        }
        
        // Measure iteration for CSR
        long csrTotalTime = 0;
        int csrEdgeCount = 0;
        for (String node : testNodes) {
            if (!csrGraph.hasNode(node)) continue;
            for (int i = 0; i < NUM_ITERATION_TESTS; i++) {
                if (i < WARMUP_ITERATIONS) {
                    csrGraph.neighbors(node);
                    continue;
                }
                long start = System.nanoTime();
                List<Edge> neighbors = csrGraph.neighbors(node);
                long end = System.nanoTime();
                csrTotalTime += (end - start);
                csrEdgeCount += neighbors.size();
            }
        }
        
        int validTests = (testNodes.size() * (NUM_ITERATION_TESTS - WARMUP_ITERATIONS));
        stats.put("AdjacencyListGraph", new IterationStats(
            adjTotalTime / validTests, adjEdgeCount / validTests,
            (double) adjTotalTime / adjEdgeCount));
        stats.put("CSRGraph", new IterationStats(
            csrTotalTime / validTests, csrEdgeCount / validTests,
            (double) csrTotalTime / csrEdgeCount));
        
        return stats;
    }
    
    private Map<String, List<QueryResult>> runDijkstraComparison(
            Graph adjGraph, Graph csrGraph, List<Query> queries) {
        
        Map<String, List<QueryResult>> results = new HashMap<>();
        results.put("AdjacencyListGraph", new ArrayList<>());
        results.put("CSRGraph", new ArrayList<>());
        
        Dijkstra dijkstra = new Dijkstra();
        
        for (Query query : queries) {
            // Run on AdjacencyList
            List<PathResult> adjRuns = new ArrayList<>();
            for (int run = 0; run < NUM_RUNS_PER_QUERY; run++) {
                PathResult result = dijkstra.findPath(adjGraph, query.origin, query.destination);
                adjRuns.add(result);
            }
            results.get("AdjacencyListGraph").add(extractMetrics(query, adjRuns, adjGraph));
            
            // Run on CSR
            List<PathResult> csrRuns = new ArrayList<>();
            for (int run = 0; run < NUM_RUNS_PER_QUERY; run++) {
                PathResult result = dijkstra.findPath(csrGraph, query.origin, query.destination);
                csrRuns.add(result);
            }
            results.get("CSRGraph").add(extractMetrics(query, csrRuns, csrGraph));
        }
        
        return results;
    }
    
    private List<Query> generateQueries(Graph graph, int numQueries) {
        List<String> nodes = graph.nodes();
        List<Query> queries = new ArrayList<>();
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
    
    private QueryResult extractMetrics(Query query, List<PathResult> runs, Graph graph) {
        double avgRuntime = runs.stream().mapToLong(PathResult::getRuntimeMs).average().orElse(0);
        double avgNodesVisited = runs.stream().mapToInt(PathResult::getNodesVisited).average().orElse(0);
        double avgEdgesRelaxed = runs.stream().mapToInt(PathResult::getEdgesRelaxed).average().orElse(0);
        boolean pathFound = runs.stream().anyMatch(PathResult::isFound);
        double avgWeight = runs.stream().mapToDouble(PathResult::getTotalWeight).average().orElse(0);
        
        return new QueryResult(query.origin, query.destination, (long) avgRuntime,
            (int) avgNodesVisited, (int) avgEdgesRelaxed, runs.get(0).getPathLength(),
            pathFound, avgWeight, estimateMemory(graph));
    }
    
    private long estimateMemory(Graph graph) {
        return graph.nodeCount() * 100L + graph.edgeCount() * 50L;
    }
    
    private void analyzeAndWriteResults(Map<String, IterationStats> iterationStats,
                                       Map<String, List<QueryResult>> dijkstraResults,
                                       long csrBuildOverhead, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/cache_locality.csv");
        
        // Write iteration stats
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {"graph_type", "avg_iteration_time_ns", "avg_edges_per_node", 
            "time_per_edge_ns", "query_runtime_ms", "nodes_visited", "edges_relaxed", 
            "path_found", "build_overhead_ms"};
        
        for (Map.Entry<String, IterationStats> entry : iterationStats.entrySet()) {
            String graphType = entry.getKey();
            IterationStats iterStats = entry.getValue();
            List<QueryResult> queryResults = dijkstraResults.get(graphType);
            
            if (queryResults != null && !queryResults.isEmpty()) {
                double avgQueryTime = queryResults.stream().mapToLong(r -> r.runtimeMs).average().orElse(0);
                double avgNodes = queryResults.stream().mapToInt(r -> r.nodesVisited).average().orElse(0);
                double avgEdges = queryResults.stream().mapToInt(r -> r.edgesRelaxed).average().orElse(0);
                double successRate = queryResults.stream().filter(r -> r.pathFound).count() / (double) queryResults.size();
                
                csvRows.add(new String[]{
                    graphType,
                    String.valueOf(iterStats.avgIterationTime),
                    String.valueOf(iterStats.avgEdgesPerNode),
                    IOUtils.formatDouble(iterStats.timePerEdge, 2),
                    IOUtils.formatDouble(avgQueryTime, 2),
                    IOUtils.formatDouble(avgNodes, 1),
                    IOUtils.formatDouble(avgEdges, 1),
                    IOUtils.formatDouble(successRate * 100, 1) + "%",
                    graphType.equals("CSRGraph") ? String.valueOf(csrBuildOverhead) : "0"
                });
            }
        }
        
        IOUtils.writeCsv(outputDir + "/cache_locality.csv", headers, csvRows);
        writeSummaryReport(iterationStats, dijkstraResults, csrBuildOverhead, outputDir + "/summary.md");
    }
    
    private void writeSummaryReport(Map<String, IterationStats> iterationStats,
                                   Map<String, List<QueryResult>> dijkstraResults,
                                   long csrBuildOverhead, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Cache Locality Experiment - Summary\n\n");
        
        IterationStats adjStats = iterationStats.get("AdjacencyListGraph");
        IterationStats csrStats = iterationStats.get("CSRGraph");
        
        List<QueryResult> adjResults = dijkstraResults.get("AdjacencyListGraph");
        List<QueryResult> csrResults = dijkstraResults.get("CSRGraph");
        
        double adjAvgRuntime = adjResults.stream().mapToLong(r -> r.runtimeMs).average().orElse(0);
        double csrAvgRuntime = csrResults.stream().mapToLong(r -> r.runtimeMs).average().orElse(0);
        
        double speedup = adjAvgRuntime / csrAvgRuntime;
        double iterationSpeedup = (double) adjStats.avgIterationTime / csrStats.avgIterationTime;
        
        report.append("## Performance Comparison\n\n");
        report.append("| Metric | AdjacencyListGraph | CSRGraph | Speedup |\n");
        report.append("|--------|-------------------|----------|----------|\n");
        report.append(String.format("| Avg Query Time (ms) | %.2f | %.2f | %.2fx |\n",
            adjAvgRuntime, csrAvgRuntime, speedup));
        report.append(String.format("| Iteration Time (ns) | %d | %d | %.2fx |\n",
            adjStats.avgIterationTime, csrStats.avgIterationTime, iterationSpeedup));
        report.append(String.format("| Time Per Edge (ns) | %.2f | %.2f | %.2fx |\n",
            adjStats.timePerEdge, csrStats.timePerEdge, adjStats.timePerEdge / csrStats.timePerEdge));
        report.append(String.format("| Build Overhead (ms) | 0 | %d | - |\n", csrBuildOverhead));
        
        report.append("\n## Key Findings\n\n");
        if (speedup > 1.0) {
            report.append(String.format("- **CSR is %.1f%% faster** than AdjacencyList for queries\n", (speedup - 1) * 100));
        }
        if (iterationSpeedup > 1.0) {
            report.append(String.format("- **CSR iteration is %.1f%% faster** due to cache locality\n", (iterationSpeedup - 1) * 100));
        }
        report.append(String.format("- CSR build overhead: %d ms\n", csrBuildOverhead));
        report.append(String.format("- Break-even point: ~%.0f queries justify CSR build cost\n", 
            csrBuildOverhead / Math.max(adjAvgRuntime - csrAvgRuntime, 0.1)));
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    private static class Query {
        final String origin, destination;
        Query(String origin, String destination) {
            this.origin = origin;
            this.destination = destination;
        }
    }
    
    private static class QueryResult {
        final String origin, destination;
        final long runtimeMs;
        final int nodesVisited, edgesRelaxed, pathLength;
        final boolean pathFound;
        final double totalWeight;
        final long memoryEstimate;
        
        QueryResult(String origin, String destination, long runtimeMs, int nodesVisited,
                   int edgesRelaxed, int pathLength, boolean pathFound, double totalWeight, long memoryEstimate) {
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
    
    private static class IterationStats {
        final long avgIterationTime;
        final int avgEdgesPerNode;
        final double timePerEdge;
        
        IterationStats(long avgIterationTime, int avgEdgesPerNode, double timePerEdge) {
            this.avgIterationTime = avgIterationTime;
            this.avgEdgesPerNode = avgEdgesPerNode;
            this.timePerEdge = timePerEdge;
        }
    }
    
    public static void main(String[] args) throws IOException {
        CacheLocalityExperiment exp = new CacheLocalityExperiment();
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        int numQueries = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        String outputDir = args.length > 2 ? args[2] : "out/experiments/experiment4_cache_locality";
        exp.runExperiment(csvPath, numQueries, outputDir);
    }
}


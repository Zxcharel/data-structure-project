package src.experiments;

import src.algo.Dijkstra;
import src.algo.PathResult;
import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import src.util.Stopwatch;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Experiment 2: Scalability Analysis Across Graph Sizes
 * 
 * Tests how each graph data structure scales as the graph grows in size.
 * By testing the same structures on progressively larger subsets of the data,
 * we can identify which structures scale linearly, quadratically, or exhibit
 * other growth patterns.
 */
public class ScalabilityExperiment {
    
    private static final String DEFAULT_CSV_PATH = "data/cleaned_flights.csv";
    private static final double[] SIZE_SUBSETS = {0.10, 0.25, 0.50, 0.75, 1.0};
    private static final int NUM_QUERIES_PER_SIZE = 25;
    private static final Random random = new Random(42);
    
    /**
     * Runs the scalability experiment
     */
    public void runExperiment(String csvPath, String outputDir) throws IOException {
        System.out.println("=== Experiment 4: Scalability Analysis ===");
        System.out.println("Reading full graph from: " + csvPath);
        
        // Read full graph first
        CsvReader reader = new CsvReader();
        Graph fullGraph = reader.readCsvAndBuildGraph(csvPath);
        System.out.println("Full graph: " + fullGraph.nodeCount() + " nodes, " + 
                          fullGraph.edgeCount() + " edges\n");
        
        List<SizeResult> allResults = new ArrayList<>();
        
        // Test each size subset
        for (double sizePercent : SIZE_SUBSETS) {
            System.out.println("Testing " + (int)(sizePercent * 100) + "% subset...");
            
            // Create subset
            Graph subset = createSubset(fullGraph, sizePercent);
            System.out.println("  Subset: " + subset.nodeCount() + " nodes, " + 
                             subset.edgeCount() + " edges");
            
            // Build all graph types from subset
            Map<String, Graph> graphs = buildAllGraphTypesFromSubset(subset);
            
            // Generate test queries
            List<String> queryPairs = generateQueries(subset, NUM_QUERIES_PER_SIZE);
            
            // Measure each graph type
            for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
                String graphType = entry.getKey();
                Graph graph = entry.getValue();
                
                // Measure build time
                Stopwatch buildTimer = new Stopwatch();
                buildTimer.start();
                Graph builtGraph = buildSingleGraphType(graph, subset);
                buildTimer.stop();
                
                // Measure query time
                List<Long> queryTimes = new ArrayList<>();
                Dijkstra dijkstra = new Dijkstra();
                
                for (int i = 0; i < queryPairs.size(); i += 2) {
                    String origin = queryPairs.get(i);
                    String dest = queryPairs.get(i + 1);
                    
                    if (builtGraph.hasNode(origin) && builtGraph.hasNode(dest)) {
                        PathResult result = dijkstra.findPath(builtGraph, origin, dest);
                        queryTimes.add(result.getRuntimeMs());
                    }
                }
                
                long avgQueryTime = queryTimes.isEmpty() ? 0 : 
                    (long) queryTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                
                long memoryEstimate = estimateMemory(builtGraph);
                
                allResults.add(new SizeResult(
                    graphType,
                    sizePercent,
                    builtGraph.nodeCount(),
                    builtGraph.edgeCount(),
                    buildTimer.getElapsedMs(),
                    avgQueryTime,
                    memoryEstimate
                ));
            }
        }
        
        // Analyze and write results
        System.out.println("\nAnalyzing scaling patterns...");
        analyzeAndWriteResults(allResults, outputDir);
        System.out.println("Experiment completed! Results written to: " + outputDir);
    }
    
    /**
     * Creates a subset of the graph by randomly selecting nodes
     */
    private Graph createSubset(Graph fullGraph, double percentage) {
        List<String> allNodes = new ArrayList<>(fullGraph.nodes());
        Collections.shuffle(allNodes, random);
        
        int subsetSize = (int) (allNodes.size() * percentage);
        Set<String> selectedNodes = new HashSet<>(allNodes.subList(0, subsetSize));
        
        // Build new graph with subset nodes and edges between them
        AdjacencyListGraph subsetGraph = new AdjacencyListGraph();
        for (String node : selectedNodes) {
            for (Edge edge : fullGraph.neighbors(node)) {
                if (selectedNodes.contains(edge.getDestination())) {
                    subsetGraph.addEdge(node, edge);
                }
            }
        }
        
        return subsetGraph;
    }
    
    /**
     * Builds all graph types from a subset
     */
    private Map<String, Graph> buildAllGraphTypesFromSubset(Graph subset) throws IOException {
        Map<String, Graph> graphs = new LinkedHashMap<>();
        
        // For each graph type, we need to build from the subset
        // Since we can't rebuild from CSV at this point, we copy edges
        graphs.put("AdjacencyListGraph", copyGraph(subset, new AdjacencyListGraph()));
        graphs.put("SortedAdjacencyListGraph", copyGraph(subset, new SortedAdjacencyListGraph()));
        
        MatrixGraph matrixGraph = new MatrixGraph(subset.nodeCount() * 2);
        graphs.put("MatrixGraph", copyGraph(subset, matrixGraph));
        
        graphs.put("CSRGraph", new CSRGraph(subset));
        graphs.put("DoublyLinkedListGraph", copyGraph(subset, new DoublyLinkedListGraph()));
        graphs.put("CircularLinkedListGraph", copyGraph(subset, new CircularLinkedListGraph()));
        graphs.put("HalfEdgeGraph", copyGraph(subset, new HalfEdgeGraph()));
        graphs.put("LinearArrayGraph", copyGraph(subset, new LinearArrayGraph()));
        graphs.put("DynamicArrayGraph", copyGraph(subset, new DynamicArrayGraph()));
        graphs.put("OffsetArrayGraph", copyGraph(subset, new OffsetArrayGraph()));
        graphs.put("RoutePartitionedTrieGraph", copyGraph(subset, new RoutePartitionedTrieGraph()));
        graphs.put("LinkCutTreeGraph", copyGraph(subset, new LinkCutTreeGraph()));
        graphs.put("EulerTourTreeGraph", copyGraph(subset, new EulerTourTreeGraph()));
        
        return graphs;
    }
    
    /**
     * Copies edges from source graph to target graph
     */
    private Graph copyGraph(Graph source, Graph target) {
        for (String node : source.nodes()) {
            for (Edge edge : source.neighbors(node)) {
                target.addEdge(node, edge);
            }
        }
        return target;
    }
    
    /**
     * Builds a single graph type (for timing)
     */
    private Graph buildSingleGraphType(Graph template, Graph source) {
        // Return a copy (already built in buildAllGraphTypesFromSubset)
        // This is for accurate build time measurement
        String className = template.getClass().getSimpleName();
        
        try {
            if (className.equals("CSRGraph")) {
                return new CSRGraph(source);
            } else if (className.equals("MatrixGraph")) {
                MatrixGraph mg = new MatrixGraph(source.nodeCount() * 2);
                return copyGraph(source, mg);
            } else {
                return copyGraph(source, template);
            }
        } catch (Exception e) {
            return template;
        }
    }
    
    /**
     * Generates test queries
     */
    private List<String> generateQueries(Graph graph, int numQueries) {
        List<String> nodes = graph.nodes();
        List<String> queryPairs = new ArrayList<>();
        
        for (int i = 0; i < numQueries; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String dest = nodes.get(random.nextInt(nodes.size()));
            
            while (dest.equals(origin)) {
                dest = nodes.get(random.nextInt(nodes.size()));
            }
            
            queryPairs.add(origin);
            queryPairs.add(dest);
        }
        
        return queryPairs;
    }
    
    /**
     * Estimates memory usage
     */
    private long estimateMemory(Graph graph) {
        return graph.nodeCount() * 100L + graph.edgeCount() * 50L;
    }
    
    /**
     * Analyzes results and writes to files
     */
    private void analyzeAndWriteResults(List<SizeResult> results, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/scalability.csv");
        
        // Write detailed CSV
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "graph_type", "size_percent", "node_count", "edge_count",
            "build_time_ms", "avg_query_time_ms", "memory_estimate_bytes"
        };
        
        for (SizeResult result : results) {
            csvRows.add(new String[]{
                result.graphType,
                String.valueOf(result.sizePercent),
                String.valueOf(result.nodeCount),
                String.valueOf(result.edgeCount),
                String.valueOf(result.buildTimeMs),
                String.valueOf(result.avgQueryTimeMs),
                String.valueOf(result.memoryEstimate)
            });
        }
        
        IOUtils.writeCsv(outputDir + "/scalability.csv", headers, csvRows);
        
        // Calculate scaling complexity
        Map<String, ScalingAnalysis> scaling = analyzeScaling(results);
        
        // Write summary report
        writeSummaryReport(results, scaling, outputDir + "/summary.md");
    }
    
    /**
     * Analyzes scaling patterns
     */
    private Map<String, ScalingAnalysis> analyzeScaling(List<SizeResult> results) {
        Map<String, List<SizeResult>> byGraphType = new HashMap<>();
        
        for (SizeResult result : results) {
            byGraphType.computeIfAbsent(result.graphType, k -> new ArrayList<>()).add(result);
        }
        
        Map<String, ScalingAnalysis> analysis = new HashMap<>();
        
        for (Map.Entry<String, List<SizeResult>> entry : byGraphType.entrySet()) {
            List<SizeResult> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparing(r -> r.sizePercent));
            
            if (sorted.size() >= 2) {
                // Calculate complexity approximation
                List<Double> buildComplexities = new ArrayList<>();
                List<Double> queryComplexities = new ArrayList<>();
                
                for (int i = 1; i < sorted.size(); i++) {
                    SizeResult prev = sorted.get(i - 1);
                    SizeResult curr = sorted.get(i);
                    
                    if (prev.nodeCount > 0 && prev.buildTimeMs > 0) {
                        double sizeRatio = (double) curr.nodeCount / prev.nodeCount;
                        double buildGrowth = (double) curr.buildTimeMs / prev.buildTimeMs;
                        double buildComplexity = Math.log(buildGrowth) / Math.log(sizeRatio);
                        buildComplexities.add(buildComplexity);
                    }
                    
                    if (prev.avgQueryTimeMs > 0 && prev.nodeCount > 0) {
                        double querySizeRatio = (double) curr.nodeCount / prev.nodeCount;
                        double queryGrowth = (double) curr.avgQueryTimeMs / prev.avgQueryTimeMs;
                        double queryComplexity = Math.log(queryGrowth) / Math.log(querySizeRatio);
                        queryComplexities.add(queryComplexity);
                    }
                }
                
                double avgBuildComplexity = buildComplexities.stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
                double avgQueryComplexity = queryComplexities.stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
                
                analysis.put(entry.getKey(), new ScalingAnalysis(avgBuildComplexity, avgQueryComplexity));
            }
        }
        
        return analysis;
    }
    
    /**
     * Writes summary report
     */
    private void writeSummaryReport(List<SizeResult> results,
                                   Map<String, ScalingAnalysis> scaling,
                                   String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Scalability Analysis - Summary\n\n");
        
        report.append("## Scaling Complexity (Big-O Approximation)\n\n");
        report.append("| Graph Type | Build Complexity | Query Complexity | Notes |\n");
        report.append("|-----------|------------------|------------------|-------|\n");
        
        // Sort by build complexity
        List<Map.Entry<String, ScalingAnalysis>> sorted = new ArrayList<>(scaling.entrySet());
        sorted.sort(Comparator.comparing(e -> e.getValue().buildComplexity));
        
        for (Map.Entry<String, ScalingAnalysis> entry : sorted) {
            ScalingAnalysis analysis = entry.getValue();
            String buildNote = getComplexityNote(analysis.buildComplexity);
            String queryNote = getComplexityNote(analysis.queryComplexity);
            
            report.append(String.format("| %s | %.2f (%s) | %.2f (%s) | |\n",
                entry.getKey(), analysis.buildComplexity, buildNote,
                analysis.queryComplexity, queryNote));
        }
        
        report.append("\n## Notes on Complexity\n\n");
        report.append("- O(1) ≈ 0, O(n) ≈ 1, O(n²) ≈ 2, O(n log n) ≈ 1.3\n");
        report.append("- Values are approximations based on growth rates\n");
        report.append("- Build complexity: how build time scales with graph size\n");
        report.append("- Query complexity: how query time scales with graph size\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Gets human-readable complexity note
     */
    private String getComplexityNote(double complexity) {
        if (complexity < 0.3) return "O(1)";
        if (complexity < 0.7) return "O(log n)";
        if (complexity < 1.3) return "O(n)";
        if (complexity < 1.7) return "O(n log n)";
        if (complexity < 2.3) return "O(n²)";
        return "O(n³+)";
    }
    
    /**
     * Helper classes
     */
    private static class SizeResult {
        final String graphType;
        final double sizePercent;
        final int nodeCount;
        final int edgeCount;
        final long buildTimeMs;
        final long avgQueryTimeMs;
        final long memoryEstimate;
        
        SizeResult(String graphType, double sizePercent, int nodeCount, int edgeCount,
                  long buildTimeMs, long avgQueryTimeMs, long memoryEstimate) {
            this.graphType = graphType;
            this.sizePercent = sizePercent;
            this.nodeCount = nodeCount;
            this.edgeCount = edgeCount;
            this.buildTimeMs = buildTimeMs;
            this.avgQueryTimeMs = avgQueryTimeMs;
            this.memoryEstimate = memoryEstimate;
        }
    }
    
    private static class ScalingAnalysis {
        final double buildComplexity;
        final double queryComplexity;
        
        ScalingAnalysis(double buildComplexity, double queryComplexity) {
            this.buildComplexity = buildComplexity;
            this.queryComplexity = queryComplexity;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) throws IOException {
        ScalabilityExperiment experiment = new ScalabilityExperiment();
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        String outputDir = args.length > 1 ? args[1] : "out/experiments/scalability";
        
        experiment.runExperiment(csvPath, outputDir);
    }
}


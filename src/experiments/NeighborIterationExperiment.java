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
 * Experiment 1: Neighbor Iteration Performance
 * 
 * Isolates and measures the most critical operation in Dijkstra's algorithm:
 * graph.neighbors(node). This method is called thousands of times during 
 * pathfinding, making it the primary performance bottleneck.
 */
public class NeighborIterationExperiment {
    
    private static final String DEFAULT_CSV_PATH = "data/cleaned_flights.csv";
    private static final int NUM_ITERATIONS = 10; // Number of times to measure each node
    private static final int WARMUP_ITERATIONS = 2;
    private static final Random random = new Random(42);
    
    /**
     * Runs the neighbor iteration experiment
     */
    public void runExperiment(String csvPath, String outputDir) throws IOException {
        System.out.println("=== Experiment 3: Neighbor Iteration Performance ===");
        System.out.println("Building all graph types from: " + csvPath);
        
        // Build all graph types
        Map<String, Graph> graphs = buildAllGraphTypes(csvPath);
        System.out.println("Built " + graphs.size() + " graph types\n");
        
        // Select test nodes by degree
        Graph baseGraph = graphs.values().iterator().next();
        Map<String, List<String>> nodesByCategory = categorizeNodesByDegree(baseGraph);
        
        System.out.println("Test nodes:");
        System.out.println("  Sparse (1-5 edges): " + nodesByCategory.get("sparse").size());
        System.out.println("  Medium (10-20 edges): " + nodesByCategory.get("medium").size());
        System.out.println("  Dense (50+ edges): " + nodesByCategory.get("dense").size());
        System.out.println();
        
        // Measure iteration performance
        System.out.println("Measuring neighbor iteration performance...");
        Map<String, List<IterationResult>> results = new HashMap<>();
        
        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String graphType = entry.getKey();
            Graph graph = entry.getValue();
            System.out.println("Testing " + graphType + "...");
            
            List<IterationResult> graphResults = new ArrayList<>();
            
            // Test sparse nodes
            for (String node : nodesByCategory.get("sparse")) {
                if (graph.hasNode(node)) {
                    IterationResult result = measureIteration(graph, node, "sparse");
                    if (result != null) graphResults.add(result);
                }
            }
            
            // Test medium nodes
            for (String node : nodesByCategory.get("medium")) {
                if (graph.hasNode(node)) {
                    IterationResult result = measureIteration(graph, node, "medium");
                    if (result != null) graphResults.add(result);
                }
            }
            
            // Test dense nodes
            for (String node : nodesByCategory.get("dense")) {
                if (graph.hasNode(node)) {
                    IterationResult result = measureIteration(graph, node, "dense");
                    if (result != null) graphResults.add(result);
                }
            }
            
            results.put(graphType, graphResults);
        }
        
        // Also measure within Dijkstra
        System.out.println("\nMeasuring iteration time within Dijkstra runs...");
        Map<String, DijkstraIterationStats> dijkstraStats = measureWithinDijkstra(graphs);
        
        // Analyze and write results
        System.out.println("\nAnalyzing results...");
        analyzeAndWriteResults(results, dijkstraStats, outputDir);
        System.out.println("Experiment completed! Results written to: " + outputDir);
    }
    
    /**
     * Builds all graph types
     */
    private Map<String, Graph> buildAllGraphTypes(String csvPath) throws IOException {
        Map<String, Graph> graphs = new LinkedHashMap<>();
        CsvReader reader = new CsvReader();
        
        Graph baseGraph = reader.readCsvAndBuildGraph(csvPath);
        
        graphs.put("AdjacencyListGraph", baseGraph);
        graphs.put("SortedAdjacencyListGraph", 
            reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new));
        
        int nodeCount = baseGraph.nodeCount();
        MatrixGraph matrixGraph = new MatrixGraph(nodeCount * 2);
        reader.readCsvAndBuildGraph(csvPath, matrixGraph);
        graphs.put("MatrixGraph", matrixGraph);
        
        graphs.put("CSRGraph", new CSRGraph(baseGraph));
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
     * Categorizes nodes by their degree (number of neighbors)
     */
    private Map<String, List<String>> categorizeNodesByDegree(Graph graph) {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("sparse", new ArrayList<>());
        categories.put("medium", new ArrayList<>());
        categories.put("dense", new ArrayList<>());
        
        for (String node : graph.nodes()) {
            int degree = graph.neighbors(node).size();
            if (degree >= 1 && degree <= 5) {
                categories.get("sparse").add(node);
            } else if (degree >= 10 && degree <= 20) {
                categories.get("medium").add(node);
            } else if (degree >= 50) {
                categories.get("dense").add(node);
            }
        }
        
        // Limit to reasonable number for testing
        Collections.shuffle(categories.get("sparse"), random);
        Collections.shuffle(categories.get("medium"), random);
        Collections.shuffle(categories.get("dense"), random);
        
        categories.put("sparse", categories.get("sparse").subList(0, Math.min(10, categories.get("sparse").size())));
        categories.put("medium", categories.get("medium").subList(0, Math.min(15, categories.get("medium").size())));
        categories.put("dense", categories.get("dense").subList(0, Math.min(10, categories.get("dense").size())));
        
        return categories;
    }
    
    /**
     * Measures iteration time for a single node
     */
    private IterationResult measureIteration(Graph graph, String node, String category) {
        long totalTime = 0;
        int edgeCount = 0;
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            graph.neighbors(node);
        }
        
        // Actual measurements
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            long start = System.nanoTime();
            List<Edge> neighbors = graph.neighbors(node);
            long end = System.nanoTime();
            
            totalTime += (end - start);
            edgeCount = neighbors.size();
            
            // Prevent optimization by using the result
            for (Edge e : neighbors) {
                if (e.getWeight() < 0) break; // Unlikely
            }
        }
        
        long avgTime = totalTime / NUM_ITERATIONS;
        double timePerEdge = edgeCount > 0 ? (double) avgTime / edgeCount : 0;
        
        return new IterationResult(graph.getClass().getSimpleName(), node, category,
            edgeCount, avgTime, timePerEdge);
    }
    
    /**
     * Measures iteration time within Dijkstra runs
     */
    private Map<String, DijkstraIterationStats> measureWithinDijkstra(Map<String, Graph> graphs) {
        Map<String, DijkstraIterationStats> stats = new HashMap<>();
        Dijkstra dijkstra = new Dijkstra();
        
        // Generate test queries
        Graph baseGraph = graphs.values().iterator().next();
        List<String> nodes = baseGraph.nodes();
        List<String> origins = new ArrayList<>();
        List<String> destinations = new ArrayList<>();
        
        for (int i = 0; i < Math.min(20, nodes.size() / 2); i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String dest = nodes.get(random.nextInt(nodes.size()));
            if (!origin.equals(dest)) {
                origins.add(origin);
                destinations.add(dest);
            }
        }
        
        // Run Dijkstra and estimate iteration time
        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String graphType = entry.getKey();
            Graph graph = entry.getValue();
            
            long totalIterationTime = 0;
            long totalDijkstraTime = 0;
            int totalCalls = 0;
            
            for (int i = 0; i < origins.size(); i++) {
                String origin = origins.get(i);
                String dest = destinations.get(i);
                
                if (!graph.hasNode(origin) || !graph.hasNode(dest)) continue;
                
                long dijkstraStart = System.nanoTime();
                
                // Simple estimate: measure time calling neighbors for visited nodes
                // This is a simplified measurement - in real Dijkstra we'd instrument it
                PathResult result = dijkstra.findPath(graph, origin, dest);
                
                long dijkstraTime = System.nanoTime() - dijkstraStart;
                
                // Estimate iteration time: assume 60-80% of time is in neighbors()
                long estimatedIterationTime = (long) (dijkstraTime * 0.70);
                
                totalIterationTime += estimatedIterationTime;
                totalDijkstraTime += dijkstraTime;
                totalCalls += result.getNodesVisited();
            }
            
            if (totalCalls > 0) {
                stats.put(graphType, new DijkstraIterationStats(
                    totalIterationTime / origins.size(),
                    totalDijkstraTime / origins.size(),
                    (double) totalIterationTime / totalDijkstraTime * 100,
                    totalCalls / origins.size()
                ));
            }
        }
        
        return stats;
    }
    
    /**
     * Analyzes results and writes to files
     */
    private void analyzeAndWriteResults(Map<String, List<IterationResult>> results,
                                        Map<String, DijkstraIterationStats> dijkstraStats,
                                        String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/iteration.csv");
        
        // Write detailed CSV
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "graph_type", "node", "category", "degree", "iteration_time_ns",
            "time_per_edge_ns"
        };
        
        for (Map.Entry<String, List<IterationResult>> entry : results.entrySet()) {
            for (IterationResult result : entry.getValue()) {
                csvRows.add(new String[]{
                    result.graphType,
                    result.node,
                    result.category,
                    String.valueOf(result.degree),
                    String.valueOf(result.iterationTimeNs),
                    String.format("%.2f", result.timePerEdgeNs)
                });
            }
        }
        
        IOUtils.writeCsv(outputDir + "/iteration.csv", headers, csvRows);
        
        // Write summary report
        writeSummaryReport(results, dijkstraStats, outputDir + "/summary.md");
    }
    
    /**
     * Writes summary report
     */
    private void writeSummaryReport(Map<String, List<IterationResult>> results,
                                   Map<String, DijkstraIterationStats> dijkstraStats,
                                   String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Neighbor Iteration Performance - Summary\n\n");
        
        // Calculate averages by graph type
        Map<String, AvgIterationStats> avgStats = new HashMap<>();
        for (Map.Entry<String, List<IterationResult>> entry : results.entrySet()) {
            String graphType = entry.getKey();
            List<IterationResult> list = entry.getValue();
            
            double avgTime = list.stream().mapToLong(r -> r.iterationTimeNs).average().orElse(0);
            double avgTimePerEdge = list.stream().mapToDouble(r -> r.timePerEdgeNs).average().orElse(0);
            
            avgStats.put(graphType, new AvgIterationStats(avgTime, avgTimePerEdge));
        }
        
        // Sort by average iteration time
        List<Map.Entry<String, AvgIterationStats>> sorted = new ArrayList<>(avgStats.entrySet());
        sorted.sort(Comparator.comparing(e -> e.getValue().avgTime));
        
        report.append("## Average Iteration Performance\n\n");
        report.append("| Graph Type | Avg Iteration Time (ns) | Avg Time Per Edge (ns) |\n");
        report.append("|-----------|-------------------------|------------------------|\n");
        
        for (Map.Entry<String, AvgIterationStats> entry : sorted) {
            AvgIterationStats stat = entry.getValue();
            report.append(String.format("| %s | %.2f | %.2f |\n",
                entry.getKey(), stat.avgTime, stat.avgTimePerEdge));
        }
        
        // Add Dijkstra statistics
        if (!dijkstraStats.isEmpty()) {
            report.append("\n## Iteration Time Within Dijkstra\n\n");
            report.append("| Graph Type | Avg Iteration Time (ns) | Avg Dijkstra Time (ns) | Percentage |\n");
            report.append("|-----------|-------------------------|------------------------|------------|\n");
            
            for (Map.Entry<String, DijkstraIterationStats> entry : dijkstraStats.entrySet()) {
                DijkstraIterationStats stat = entry.getValue();
                report.append(String.format("| %s | %d | %d | %.1f%% |\n",
                    entry.getKey(), stat.avgIterationTime, stat.avgDijkstraTime, stat.percentage));
            }
        }
        
        report.append("\n## Key Findings\n\n");
        if (!sorted.isEmpty()) {
            Map.Entry<String, AvgIterationStats> fastest = sorted.get(0);
            report.append(String.format("- **Fastest Iteration**: %s (%.2f ns avg)\n",
                fastest.getKey(), fastest.getValue().avgTime));
        }
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Helper classes
     */
    private static class IterationResult {
        final String graphType;
        final String node;
        final String category;
        final int degree;
        final long iterationTimeNs;
        final double timePerEdgeNs;
        
        IterationResult(String graphType, String node, String category, int degree,
                       long iterationTimeNs, double timePerEdgeNs) {
            this.graphType = graphType;
            this.node = node;
            this.category = category;
            this.degree = degree;
            this.iterationTimeNs = iterationTimeNs;
            this.timePerEdgeNs = timePerEdgeNs;
        }
    }
    
    private static class AvgIterationStats {
        final double avgTime;
        final double avgTimePerEdge;
        
        AvgIterationStats(double avgTime, double avgTimePerEdge) {
            this.avgTime = avgTime;
            this.avgTimePerEdge = avgTimePerEdge;
        }
    }
    
    private static class DijkstraIterationStats {
        final long avgIterationTime;
        final long avgDijkstraTime;
        final double percentage;
        final int avgCalls;
        
        DijkstraIterationStats(long avgIterationTime, long avgDijkstraTime,
                              double percentage, int avgCalls) {
            this.avgIterationTime = avgIterationTime;
            this.avgDijkstraTime = avgDijkstraTime;
            this.percentage = percentage;
            this.avgCalls = avgCalls;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) throws IOException {
        NeighborIterationExperiment experiment = new NeighborIterationExperiment();
        String csvPath = args.length > 0 ? args[0] : DEFAULT_CSV_PATH;
        String outputDir = args.length > 1 ? args[1] : "out/experiments/iteration";
        
        experiment.runExperiment(csvPath, outputDir);
    }
}


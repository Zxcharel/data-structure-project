package src.experiments;

import src.algo.*;
import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import src.util.Stopwatch;
import java.io.IOException;
import java.util.*;

/**
 * Experiment 4: The Graph Size Deception
 * Tests how graph implementations scale across different graph sizes.
 * 
 * Title: When Bigger Graphs Make Smaller Structures Slower: The Scaling Paradox
 */
public class ScalingExperiment {
    
    /**
     * Runs the scaling experiment comparing graph implementations using CSV data
     * 
     * @param csvPath Path to CSV file
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be written
     */
    public void run(String csvPath, String outputDir) throws IOException {
        System.out.println("=== Experiment 4: The Graph Size Deception ===");
        System.out.println("Reading graph from CSV: " + csvPath + "\n");
        
        // Load graph from CSV
        CsvReader reader = new CsvReader();
        Graph realGraph = reader.readCsvAndBuildGraph(csvPath);
        
        if (realGraph == null || realGraph.nodeCount() == 0 || realGraph.edgeCount() == 0) {
            throw new IllegalArgumentException("Graph loaded from CSV is empty");
        }
        
        // Prepare output directory
        IOUtils.ensureParentDirectoryExists(outputDir + "/scaling_results.csv");
        
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {
            "graph_size", "nodes", "edges", "implementation",
            "build_time_ms", "memory_bytes", "memory_per_edge",
            "query_time_ms", "avg_query_time_ms", "scaling_factor"
        };
        
        System.out.println("Graph Size | Implementation | Build Time | Memory | Query Time");
        System.out.println("-----------|----------------|------------|--------|------------");
        
        // Extract all nodes and edges from CSV graph
        List<String> allNodes = new ArrayList<>(realGraph.nodes());
        List<TestEdge> allEdges = extractEdgesFromGraph(realGraph, allNodes);
        
        System.out.println("Full graph: " + allNodes.size() + " nodes, " + allEdges.size() + " edges");
        
        // Create subsets to show inverse effect at different scales
        // Use ALL nodes for all test cases, but vary edge count
        // Small: ~10% of edges, Medium: ~33% of edges, Large: ~67% of edges, Full: 100%
        int totalEdges = allEdges.size();
        int smallSize = Math.max(100, totalEdges / 10);      // ~10% or at least 100 edges
        int mediumSize = Math.max(500, totalEdges / 3);      // ~33% or at least 500 edges
        int largeSize = Math.max(1000, (totalEdges * 2) / 3); // ~67% or at least 1000 edges
        
        // Shuffle edges to ensure random sampling (but use fixed seed for reproducibility)
        List<TestEdge> shuffledEdges = new ArrayList<>(allEdges);
        Collections.shuffle(shuffledEdges, new Random(42));
        
        // Define test cases with different graph sizes
        TestCase[] testCases = {
            new TestCase("Small Subset", smallSize, shuffledEdges),
            new TestCase("Medium Subset", mediumSize, shuffledEdges),
            new TestCase("Large Subset", largeSize, shuffledEdges),
            new TestCase("Full Dataset", totalEdges, allEdges)  // Full uses all edges in original order
        };
        
        ScalingResult baseline = null;
        
        // Test each graph size
        for (TestCase testCase : testCases) {
            // Get subset of edges for this test case (randomly sampled)
            List<TestEdge> subsetEdges = new ArrayList<>(
                testCase.sourceEdges.subList(0, Math.min(testCase.edgeCount, testCase.sourceEdges.size()))
            );
            
            // Extract only nodes that are actually used in this edge subset
            Set<String> nodeSet = new HashSet<>();
            for (TestEdge edge : subsetEdges) {
                nodeSet.add(edge.from);
                nodeSet.add(edge.to);
            }
            List<String> subsetNodes = new ArrayList<>(nodeSet);
            
            System.out.printf("\n--- Testing %s Graph (%d nodes, ~%d edges) ---\n", 
                            testCase.name, subsetNodes.size(), subsetEdges.size());
            
            // Test each implementation on this subset
            ScalingResult[] results = new ScalingResult[4];
            
            // 1. AdjacencyListGraph
            results[0] = testGraphImplementation("AdjacencyListGraph", 
                () -> new AdjacencyListGraph(), subsetNodes, subsetEdges, testCase.name);
            if (baseline == null) baseline = results[0];
            
            // 2. LinearArrayGraph
            results[1] = testGraphImplementation("LinearArrayGraph",
                () -> new LinearArrayGraph(), subsetNodes, subsetEdges, testCase.name);
            
            // 3. DynamicArrayGraph
            results[2] = testGraphImplementation("DynamicArrayGraph",
                () -> new DynamicArrayGraph(), subsetNodes, subsetEdges, testCase.name);
            
            // 4. OffsetArrayGraph
            results[3] = testGraphImplementation("OffsetArrayGraph",
                () -> new OffsetArrayGraph(), subsetNodes, subsetEdges, testCase.name);
            
            // Add results to CSV (using baseline from first test for scaling factor)
            for (ScalingResult result : results) {
                double scalingFactor = baseline != null ? 
                    (double) result.buildTime / baseline.buildTime : 1.0;
                
                csvRows.add(new String[]{
                    testCase.name,
                    String.valueOf(subsetNodes.size()),
                    String.valueOf(subsetEdges.size()),
                    result.implementation,
                    String.valueOf(result.buildTime),
                    String.valueOf(result.memory),
                    String.valueOf(result.memoryPerEdge),
                    String.valueOf(result.queryTime),
                    String.valueOf(result.avgQueryTime),
                    IOUtils.formatDouble(scalingFactor, 2)
                });
            }
        }
        
        // Write CSV results
        IOUtils.writeCsv(outputDir + "/scaling_results.csv", headers, csvRows);
        
        // Generate analysis report
        generateScalingReport(csvRows, outputDir + "/scaling_analysis.md");
        
        System.out.println("\n=== Scaling Experiment Complete ===");
        System.out.printf("Results written to %s/\n", outputDir);
    }
    
    /**
     * Tests a graph implementation and returns performance metrics
     */
    private ScalingResult testGraphImplementation(String implName, 
                                                  java.util.function.Supplier<Graph> graphFactory,
                                                  List<String> nodes, 
                                                  List<TestEdge> edges,
                                                  String sizeName) {
        Graph graph = graphFactory.get();
        Stopwatch timer = new Stopwatch();
        
        // Measure build time
        timer.start();
        for (TestEdge testEdge : edges) {
            Edge edge = new Edge(
                testEdge.to, testEdge.airline, 0, 0, 0, 0, 0, testEdge.weight
            );
            graph.addEdge(testEdge.from, edge);
        }
        
        // Finalize if needed (for OffsetArrayGraph)
        if (graph instanceof OffsetArrayGraph) {
            ((OffsetArrayGraph) graph).finalizeCSR();
        }
        
        timer.stop();
        long buildTime = timer.getElapsedMs();
        
        // Measure memory usage
        long memory = getGraphMemory(graph);
        long memoryPerEdge = edges.size() > 0 ? memory / edges.size() : 0;
        
        // Generate test queries
        List<ExperimentQuery> queries = generateRandomQueries(nodes, 500);
        
        // Measure query performance
        Dijkstra dijkstra = new Dijkstra();
        timer.reset();
        long totalQueryTime = 0;
        int successfulQueries = 0;
        
        for (ExperimentQuery query : queries) {
            if (graph.hasNode(query.origin) && graph.hasNode(query.destination)) {
                PathResult result = dijkstra.findPath(graph, query.origin, query.destination, query.constraints);
                totalQueryTime += result.getRuntimeMs();
                if (result.isFound()) {
                    successfulQueries++;
                }
            }
        }
        
        long queryTime = totalQueryTime;
        double avgQueryTime = queries.size() > 0 ? (double) queryTime / queries.size() : 0;
        
        ScalingResult result = new ScalingResult(
            implName, buildTime, memory, memoryPerEdge, queryTime, avgQueryTime
        );
        
        System.out.printf("%-10s | %-18s | %10d ms | %8s | %10.2f ms\n",
            sizeName,
            implName,
            buildTime,
            formatBytes(memory),
            avgQueryTime);
        
        return result;
    }
    
    /**
     * Gets memory usage for a graph
     */
    private long getGraphMemory(Graph graph) {
        try {
            if (graph instanceof MatrixGraph) {
                return ((MatrixGraph) graph).getMemoryUsage();
            } else if (graph instanceof LinearArrayGraph) {
                return ((LinearArrayGraph) graph).getMemoryUsage();
            } else if (graph instanceof DynamicArrayGraph) {
                return ((DynamicArrayGraph) graph).getMemoryUsage();
            } else if (graph instanceof OffsetArrayGraph) {
                return ((OffsetArrayGraph) graph).getMemoryUsage();
            } else {
                // Estimate for AdjacencyListGraph
                long estimate = 1000; // Base overhead
                estimate += graph.nodeCount() * 50; // Per node
                estimate += graph.edgeCount() * 100; // Per edge
                return estimate;
            }
        } catch (Exception e) {
            // Fallback estimation
            return graph.nodeCount() * 50 + graph.edgeCount() * 100;
        }
    }
    
    /**
     * Generates test nodes with unique names
     */
    private List<String> generateTestNodes(int numNodes) {
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            nodes.add("Node_" + i);
        }
        return nodes;
    }
    
    /**
     * Extracts all edges from a real graph
     */
    private List<TestEdge> extractEdgesFromGraph(Graph graph, List<String> nodes) {
        List<TestEdge> edges = new ArrayList<>();
        
        for (String from : nodes) {
            List<Edge> neighbors = graph.neighbors(from);
            for (Edge edge : neighbors) {
                edges.add(new TestEdge(from, edge.getDestination(), 
                                      edge.getAirline(), edge.getWeight()));
            }
        }
        
        return edges;
    }
    
    /**
     * Generates test edges
     */
    private List<TestEdge> generateTestEdges(List<String> nodes, int numEdges) {
        List<TestEdge> edges = new ArrayList<>();
        Random edgeRandom = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < numEdges; i++) {
            String from = nodes.get(edgeRandom.nextInt(nodes.size()));
            String to = nodes.get(edgeRandom.nextInt(nodes.size()));
            
            // Ensure from != to
            while (to.equals(from)) {
                to = nodes.get(edgeRandom.nextInt(nodes.size()));
            }
            
            String airline = "Airline_" + (i % 10);
            double weight = 1.0 + edgeRandom.nextDouble() * 4.0; // 1.0 to 5.0
            
            edges.add(new TestEdge(from, to, airline, weight));
        }
        
        return edges;
    }
    
    /**
     * Generates random queries from nodes
     */
    private List<ExperimentQuery> generateRandomQueries(List<String> nodes, int numQueries) {
        List<ExperimentQuery> queries = new ArrayList<>();
        Random queryRandom = new Random(42); // Fixed seed
        
        for (int i = 0; i < numQueries; i++) {
            String origin = nodes.get(queryRandom.nextInt(nodes.size()));
            String destination = nodes.get(queryRandom.nextInt(nodes.size()));
            
            // Ensure origin != destination
            while (destination.equals(origin)) {
                destination = nodes.get(queryRandom.nextInt(nodes.size()));
            }
            
            queries.add(new ExperimentQuery(origin, destination, new Constraints()));
        }
        
        return queries;
    }
    
    /**
     * Formats bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Generates scaling analysis report
     */
    private void generateScalingReport(List<String[]> csvRows, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Experiment 3: The Graph Size Deception\n\n");
        report.append("## When Bigger Graphs Make Smaller Structures Slower: The Scaling Paradox\n\n");
        
        report.append("This experiment tests how graph implementations perform at different scales using CSV data subsets.\n");
        report.append("It demonstrates the **inverse effect** - how the \"best\" implementation changes with graph size.\n\n");
        
        // Parse and organize results
        Map<String, Map<String, ScalingResult>> resultsBySizeAndImpl = new HashMap<>();
        Map<String, Integer> nodeCountsBySize = new HashMap<>();
        Map<String, Integer> edgeCountsBySize = new HashMap<>();
        
        for (String[] row : csvRows) {
            String size = row[0];
            String impl = row[3];
            int nodes = Integer.parseInt(row[1]);
            int edges = Integer.parseInt(row[2]);
            long buildTime = Long.parseLong(row[4]);
            long memory = Long.parseLong(row[5]);
            long memoryPerEdge = Long.parseLong(row[6]);
            long queryTime = Long.parseLong(row[7]);
            double avgQueryTime = Double.parseDouble(row[8]);
            
            ScalingResult result = new ScalingResult(impl, buildTime, memory, memoryPerEdge, queryTime, avgQueryTime);
            resultsBySizeAndImpl.computeIfAbsent(size, k -> new HashMap<>()).put(impl, result);
            nodeCountsBySize.put(size, nodes);
            edgeCountsBySize.put(size, edges);
        }
        
        // Summary tables for each graph size
        report.append("## Performance Summary by Graph Size\n\n");
        
        String[] sizes = {"Small Subset", "Medium Subset", "Large Subset", "Full Dataset"};
        for (String size : sizes) {
            Map<String, ScalingResult> sizeResults = resultsBySizeAndImpl.get(size);
            if (sizeResults != null) {
                int nodes = nodeCountsBySize.getOrDefault(size, 0);
                int edges = edgeCountsBySize.getOrDefault(size, 0);
                
                report.append(String.format("### %s (%d nodes, %d edges)\n\n", size, nodes, edges));
                report.append("| Implementation | Build Time (ms) | Memory | Memory/Edge | Avg Query Time (ms) |\n");
                report.append("|----------------|-----------------|--------|-------------|---------------------|\n");
                
                for (String impl : new String[]{"AdjacencyListGraph", "LinearArrayGraph", "DynamicArrayGraph", "OffsetArrayGraph"}) {
                    ScalingResult r = sizeResults.get(impl);
                    if (r != null) {
                        report.append(String.format("| %s | %d | %s | %d | %.2f |\n",
                            impl, r.buildTime, formatBytes(r.memory), r.memoryPerEdge, r.avgQueryTime));
                    }
                }
                report.append("\n");
            }
        }
        
        // Show inverse effect analysis
        report.append("## The Inverse Effect: How Winners Change with Scale\n\n");
        
        // Track best performers at each size
        Map<String, Map<String, String>> bestBySize = new HashMap<>();
        String[] categories = {"Build", "Memory", "Query"};
        
        for (String size : sizes) {
            Map<String, ScalingResult> sizeResults = resultsBySizeAndImpl.get(size);
            if (sizeResults == null) continue;
            
            Map<String, String> best = new HashMap<>();
            
            // Find best in each category
            ScalingResult adjList = sizeResults.get("AdjacencyListGraph");
            ScalingResult linear = sizeResults.get("LinearArrayGraph");
            ScalingResult dynamic = sizeResults.get("DynamicArrayGraph");
            ScalingResult offset = sizeResults.get("OffsetArrayGraph");
            
            if (adjList != null && linear != null && dynamic != null && offset != null) {
                // Best build time
                ScalingResult bestBuild = adjList;
                if (linear.buildTime < bestBuild.buildTime) bestBuild = linear;
                if (dynamic.buildTime < bestBuild.buildTime) bestBuild = dynamic;
                if (offset.buildTime < bestBuild.buildTime) bestBuild = offset;
                best.put("Build", bestBuild.implementation);
                
                // Best memory
                ScalingResult bestMemory = adjList;
                if (linear.memory < bestMemory.memory) bestMemory = linear;
                if (dynamic.memory < bestMemory.memory) bestMemory = dynamic;
                if (offset.memory < bestMemory.memory) bestMemory = offset;
                best.put("Memory", bestMemory.implementation);
                
                // Best query time
                ScalingResult bestQuery = adjList;
                if (linear.avgQueryTime < bestQuery.avgQueryTime) bestQuery = linear;
                if (dynamic.avgQueryTime < bestQuery.avgQueryTime) bestQuery = dynamic;
                if (offset.avgQueryTime < bestQuery.avgQueryTime) bestQuery = offset;
                best.put("Query", bestQuery.implementation);
            }
            
            bestBySize.put(size, best);
        }
        
        // Show how winners change
        report.append("### Best Performer by Category Across Sizes\n\n");
        
        for (String category : categories) {
            report.append(String.format("#### %s Time Winners\n\n", category));
            report.append("| Graph Size | Winner | Reason |\n");
            report.append("|------------|--------|--------|\n");
            
            for (String size : sizes) {
                Map<String, String> best = bestBySize.get(size);
                if (best != null && best.containsKey(category)) {
                    String winner = best.get(category);
                    String reason = "";
                    if (category.equals("Build")) {
                        reason = winner.equals("OffsetArrayGraph") ? 
                            "Finalization overhead becomes negligible" : 
                            "Simple structure scales well";
                    } else if (category.equals("Memory")) {
                        reason = winner.equals("LinearArrayGraph") ? 
                            "Most efficient storage" : 
                            "Optimized memory layout";
                    } else {
                        reason = winner.equals("OffsetArrayGraph") ? 
                            "CSR cache efficiency pays off" : 
                            "Better locality at scale";
                    }
                    report.append(String.format("| %s | %s | %s |\n", size, winner, reason));
                }
            }
            report.append("\n");
        }
        
        // Key insights
        report.append("### Key Insights: The Inverse Effect\n\n");
        report.append("1. **Small Graphs**: Simple structures (AdjacencyList) often win due to low overhead\n");
        report.append("2. **Medium Graphs**: Trade-offs become visible - no clear winner\n");
        report.append("3. **Large Graphs**: Optimized structures (OffsetArray/CSR) show their advantages\n");
        report.append("4. **Memory vs Speed**: Structures that use more memory often query faster\n");
        report.append("5. **Build Cost**: Structures with high build cost (OffsetArray) amortize better at scale\n\n");
        
        report.append("### Key Learning\n\n");
        report.append("**The inverse effect**: The \"best\" data structure depends on graph size. ");
        report.append("What's fastest for 100 edges may be slowest for 3000 edges, and vice versa. ");
        report.append("Always benchmark at your target scale - small-scale performance doesn't predict large-scale behavior.\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    /**
     * Helper class for scaling experiment results
     */
    private static class ScalingResult {
        final String implementation;
        final long buildTime;
        final long memory;
        final long memoryPerEdge;
        final long queryTime;
        final double avgQueryTime;
        
        ScalingResult(String implementation, long buildTime, long memory, 
                     long memoryPerEdge, long queryTime, double avgQueryTime) {
            this.implementation = implementation;
            this.buildTime = buildTime;
            this.memory = memory;
            this.memoryPerEdge = memoryPerEdge;
            this.queryTime = queryTime;
            this.avgQueryTime = avgQueryTime;
        }
    }
    
    /**
     * Helper class for test cases
     */
    private static class TestCase {
        final String name;
        final int edgeCount;
        final List<TestEdge> sourceEdges;
        
        TestCase(String name, int edgeCount, List<TestEdge> sourceEdges) {
            this.name = name;
            this.edgeCount = edgeCount;
            this.sourceEdges = sourceEdges;
        }
    }
    
    /**
     * Helper class for test edges
     */
    private static class TestEdge {
        final String from;
        final String to;
        final String airline;
        final double weight;

        TestEdge(String from, String to, String airline, double weight) {
            this.from = from;
            this.to = to;
            this.airline = airline;
            this.weight = weight;
        }
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
}


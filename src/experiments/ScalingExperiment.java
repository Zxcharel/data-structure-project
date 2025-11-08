package src.experiments;

import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import src.util.Stopwatch;
import java.io.IOException;
import java.util.*;

/**
 * Experiment 2: The Graph Size Deception
 * Tests how graph implementations scale across different graph sizes.
 * 
 * Title: When Bigger Graphs Make Smaller Structures Slower: The Scaling Paradox
 */
public class ScalingExperiment {
    private static final String[] IMPLEMENTATIONS = {
            "AdjacencyListGraph",
            "CSRGraph",
            "SortedAdjacencyListGraph",
            "OffsetArrayGraph",
            "MatrixGraph",
            "RoutePartitionedTrieGraph",
            "HalfEdgeGraph"
    };
    
    /**
     * Runs the scaling experiment comparing graph implementations using CSV data
     * 
     * @param csvPath Path to CSV file
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be written
     */
    public void run(String csvPath, String outputDir) throws IOException {
        System.out.println("=== Experiment 2: The Graph Size Deception ===");
        System.out.println("Reading graph from CSV: " + csvPath + "\n");
        
        CsvReader reader = new CsvReader();
        Graph sourceGraph = reader.readCsvAndBuildGraph(csvPath);
        
        if (sourceGraph == null || sourceGraph.nodeCount() == 0 || sourceGraph.edgeCount() == 0) {
            throw new IllegalArgumentException("Graph loaded from CSV is empty");
        }
        
        List<String> allNodes = new ArrayList<>(sourceGraph.nodes());
        List<TestEdge> allEdges = extractEdgesFromGraph(sourceGraph, allNodes);
        
        Map<String, RandomRunResults> randomResults = runRandomTests(allNodes, allEdges);
        
        IOUtils.ensureParentDirectoryExists(outputDir + "/random_results.csv");
        generateRandomCSV(outputDir, randomResults);
        displaySummary(randomResults);
        
        System.out.println("\n=== Scaling Experiment Complete ===");
        System.out.println("Results written to:");
        System.out.println("- " + outputDir + "/random_results.csv");
    }
    
    /**
     * Counts distinct paths between origin and destination using DFS
     * Limits search depth to avoid exponential explosion
     * Also limits total paths found to avoid expensive computations
     */
    private int countDistinctPaths(Graph graph, String origin, String destination, int maxDepth) {
        if (!graph.hasNode(origin) || !graph.hasNode(destination)) {
            return 0;
        }
        
        Set<String> distinctPaths = new HashSet<>(); // Store path as string representation
        List<String> currentPath = new ArrayList<>();
        Set<String> visitedInPath = new HashSet<>(); // Track nodes in current path to avoid cycles
        
        // Limit to counting max 10 paths to avoid expensive computation
        int maxPathsToCount = 10;
        
        dfsCountPaths(graph, origin, destination, currentPath, visitedInPath, distinctPaths, maxDepth, maxPathsToCount);
        
        return distinctPaths.size();
    }
    
    /**
     * DFS helper to find all distinct paths
     * Stops early if we've found enough paths
     */
    private void dfsCountPaths(Graph graph, String current, String destination,
                               List<String> currentPath, Set<String> visitedInPath,
                               Set<String> distinctPaths, int maxDepth, int maxPathsToCount) {
        // Early termination: if we've found enough paths, stop searching
        if (distinctPaths.size() >= maxPathsToCount) {
            return;
        }
        
        // Base case: reached destination
        if (current.equals(destination)) {
            // Create path string representation
            List<String> path = new ArrayList<>(currentPath);
            path.add(destination);
            String pathStr = String.join("->", path);
            distinctPaths.add(pathStr);
            return;
        }
        
        // Limit depth to avoid exponential explosion
        if (currentPath.size() >= maxDepth) {
            return;
        }
        
        // Add current node to path
        currentPath.add(current);
        visitedInPath.add(current);
        
        // Explore neighbors
        for (Edge edge : graph.neighbors(current)) {
            String neighbor = edge.getDestination();
            
            // Skip if already in current path (avoid cycles)
            if (!visitedInPath.contains(neighbor)) {
                dfsCountPaths(graph, neighbor, destination, currentPath, visitedInPath, distinctPaths, maxDepth, maxPathsToCount);
                
                // Early termination check after each recursive call
                if (distinctPaths.size() >= maxPathsToCount) {
                    break;
                }
            }
        }
        
        // Backtrack
        currentPath.remove(currentPath.size() - 1);
        visitedInPath.remove(current);
    }
    
    /**
     * Builds all 7 graph implementations from the source graph
     */
    private Map<String, BuildInfo> buildAllGraphs(Graph sourceGraph) {
        Map<String, BuildInfo> graphs = new LinkedHashMap<>();
        List<TestEdge> allEdges = extractEdgesFromGraph(sourceGraph, new ArrayList<>(sourceGraph.nodes()));

        Stopwatch timer = new Stopwatch();

        timer.start();
        AdjacencyListGraph adjList = new AdjacencyListGraph();
        buildGraph(adjList, allEdges);
        timer.stop();
        graphs.put("AdjacencyListGraph", new BuildInfo(adjList, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        CSRGraph csrGraph = new CSRGraph(adjList);
        timer.stop();
        graphs.put("CSRGraph", new BuildInfo(csrGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        SortedAdjacencyListGraph sortedAdj = new SortedAdjacencyListGraph();
        buildGraph(sortedAdj, allEdges);
        timer.stop();
        graphs.put("SortedAdjacencyListGraph", new BuildInfo(sortedAdj, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        OffsetArrayGraph offsetArray = new OffsetArrayGraph();
        buildGraph(offsetArray, allEdges);
        offsetArray.finalizeCSR();
        timer.stop();
        graphs.put("OffsetArrayGraph", new BuildInfo(offsetArray, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        MatrixGraph matrixGraph = new MatrixGraph(sourceGraph.nodeCount() * 2);
        buildGraph(matrixGraph, allEdges);
        timer.stop();
        graphs.put("MatrixGraph", new BuildInfo(matrixGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        RoutePartitionedTrieGraph trieGraph = new RoutePartitionedTrieGraph();
        buildGraph(trieGraph, allEdges);
        timer.stop();
        graphs.put("RoutePartitionedTrieGraph", new BuildInfo(trieGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        HalfEdgeGraph halfEdge = new HalfEdgeGraph();
        buildGraph(halfEdge, allEdges);
        timer.stop();
        graphs.put("HalfEdgeGraph", new BuildInfo(halfEdge, timer.getElapsedMs()));

        return graphs;
    }
    
    /**
     * Builds a graph from test edges
     */
    private void buildGraph(Graph graph, List<TestEdge> edges) {
        for (TestEdge testEdge : edges) {
            Edge edge = new Edge(testEdge.to, testEdge.airline, 0, 0, 0, 0, 0, testEdge.weight);
            graph.addEdge(testEdge.from, edge);
        }
    }
    
    /**
     * Runs 1000 random queries for real-life simulation with different graph sizes (small, medium, large)
     */
    private Map<String, RandomRunResults> runRandomTests(List<String> allNodes, List<TestEdge> allEdges) {
        Map<String, RandomRunResults> results = new LinkedHashMap<>();
        Random random = new Random(42);
        final int iterations = 1000;

        int totalNodes = allNodes.size();
        int totalEdges = allEdges.size();

        int smallNodes = Math.max(50, totalNodes / 4);
        int mediumNodes = Math.max(100, totalNodes / 2);
        int largeNodes = totalNodes;

        int smallEdges = Math.max(100, totalEdges / 4);
        int mediumEdges = Math.max(500, totalEdges / 2);
        int largeEdges = totalEdges;

        List<String> shuffledNodes = new ArrayList<>(allNodes);
        Collections.shuffle(shuffledNodes, new Random(42));
        List<TestEdge> shuffledEdges = new ArrayList<>(allEdges);
        Collections.shuffle(shuffledEdges, new Random(42));

        String[] sizes = {"Small", "Medium", "Large"};
        int[] nodeCounts = {smallNodes, mediumNodes, largeNodes};
        int[] edgeCounts = {smallEdges, mediumEdges, largeEdges};

        for (int sizeIdx = 0; sizeIdx < sizes.length; sizeIdx++) {
            String size = sizes[sizeIdx];
            int nodeCount = nodeCounts[sizeIdx];
            int edgeCount = edgeCounts[sizeIdx];

            List<String> subsetNodes = new ArrayList<>(shuffledNodes.subList(0, Math.min(nodeCount, shuffledNodes.size())));
            List<TestEdge> subsetEdges = new ArrayList<>(shuffledEdges.subList(0, Math.min(edgeCount, shuffledEdges.size())));

            Set<String> nodeSet = new HashSet<>();
            for (TestEdge edge : subsetEdges) {
                nodeSet.add(edge.from);
                nodeSet.add(edge.to);
            }
            subsetNodes = new ArrayList<>(nodeSet);

            System.out.printf("\n--- %s Graph: %d nodes, %d edges ---%n", size, subsetNodes.size(), subsetEdges.size());

            Map<String, Long> totalBuildTimes = new LinkedHashMap<>();
            Map<String, Graph> latestGraphs = new LinkedHashMap<>();

            for (int iter = 0; iter < iterations; iter++) {
                List<TestEdge> mutatedEdges = mutateEdgeWeights(subsetEdges, random);
                Map<String, BuildInfo> iterationBuilds = buildGraphsForSize(subsetNodes, mutatedEdges);
                for (String implName : IMPLEMENTATIONS) {
                    BuildInfo info = iterationBuilds.get(implName);
                    if (info == null) {
                        continue;
                    }
                    totalBuildTimes.merge(implName, info.buildTimeMs, Long::sum);
                    latestGraphs.put(implName, info.graph);
                }
            }

            for (String implName : IMPLEMENTATIONS) {
                Long totalBuild = totalBuildTimes.get(implName);
                Graph graph = latestGraphs.get(implName);
                if (totalBuild == null || graph == null) {
                    continue;
                }
                double avgBuild = totalBuild / (double) iterations;
                long memory = getGraphMemory(graph);
                String key = implName + "_" + size;
                results.put(key, new RandomRunResults(implName, size, avgBuild, memory, iterations,
                        subsetNodes.size(), subsetEdges.size()));
            }
        }

        return results;
    }
    
    /**
     * Builds graphs for a specific size (subset of nodes/edges)
     */
    private Map<String, BuildInfo> buildGraphsForSize(List<String> nodes, List<TestEdge> edges) {
        Map<String, BuildInfo> graphs = new LinkedHashMap<>();
        Stopwatch timer = new Stopwatch();

        timer.start();
        AdjacencyListGraph adjList = new AdjacencyListGraph();
        buildGraph(adjList, edges);
        timer.stop();
        graphs.put("AdjacencyListGraph", new BuildInfo(adjList, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        CSRGraph csrGraph = new CSRGraph(adjList);
        timer.stop();
        graphs.put("CSRGraph", new BuildInfo(csrGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        SortedAdjacencyListGraph sortedAdj = new SortedAdjacencyListGraph();
        buildGraph(sortedAdj, edges);
        timer.stop();
        graphs.put("SortedAdjacencyListGraph", new BuildInfo(sortedAdj, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        OffsetArrayGraph offsetArray = new OffsetArrayGraph();
        buildGraph(offsetArray, edges);
        offsetArray.finalizeCSR();
        timer.stop();
        graphs.put("OffsetArrayGraph", new BuildInfo(offsetArray, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        MatrixGraph matrixGraph = new MatrixGraph(Math.max(nodes.size() * 2, 4));
        buildGraph(matrixGraph, edges);
        timer.stop();
        graphs.put("MatrixGraph", new BuildInfo(matrixGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        RoutePartitionedTrieGraph trieGraph = new RoutePartitionedTrieGraph();
        buildGraph(trieGraph, edges);
        timer.stop();
        graphs.put("RoutePartitionedTrieGraph", new BuildInfo(trieGraph, timer.getElapsedMs()));

        timer.reset();
        timer.start();
        HalfEdgeGraph halfEdge = new HalfEdgeGraph();
        buildGraph(halfEdge, edges);
        timer.stop();
        graphs.put("HalfEdgeGraph", new BuildInfo(halfEdge, timer.getElapsedMs()));

        return graphs;
    }
    
    /**
     * Generates CSV output for random runs
     */
    private void generateRandomCSV(String outputDir,
                                  Map<String, RandomRunResults> randomResults) throws IOException {
        List<String[]> csvRows = new ArrayList<>();
        
        String[] headers = {
            "graph_size", "implementation", "nodes", "edges",
            "avg_build_ms", "iterations", "memory_kb"
        };
        
        String[] sizes = {"Small", "Medium", "Large"};
        
        for (String size : sizes) {
            for (String impl : IMPLEMENTATIONS) {
                String key = impl + "_" + size;
                RandomRunResults result = randomResults.get(key);
                if (result != null) {
                    csvRows.add(new String[]{
                        result.size,
                        result.implementation,
                        String.valueOf(result.nodes),
                        String.valueOf(result.edges),
                        String.format("%.4f", result.avgBuildMs),
                        String.valueOf(result.iterations),
                        String.format("%.2f", result.memory / 1024.0)
                    });
                }
            }
        }
        
        IOUtils.writeCsv(outputDir + "/random_results.csv", headers, csvRows);
    }
    
    /**
     * Displays summary of results
     */
    private void displaySummary(Map<String, RandomRunResults> randomResults) {
        System.out.println("\n=== Summary ===");
        System.out.println("\nRebuild Trials (each implementation per size rebuilt 1000 times; table sorted by average build time):\n");
        
        System.out.println("Small Graph (subset ~25% nodes / edges):");
        printRandomResultsForSize(randomResults, IMPLEMENTATIONS, "Small");
        
        System.out.println("\nMedium Graph (subset ~50% nodes / edges):");
        printRandomResultsForSize(randomResults, IMPLEMENTATIONS, "Medium");
        
        System.out.println("\nLarge Graph (full dataset):");
        printRandomResultsForSize(randomResults, IMPLEMENTATIONS, "Large");
    }
    
    private void printRandomResultsForSize(Map<String, RandomRunResults> randomResults, String[] impls, String size) {
        List<RandomRunResults> results = new ArrayList<>();
        for (String impl : impls) {
            String key = impl + "_" + size;
            RandomRunResults result = randomResults.get(key);
            if (result != null) {
                results.add(result);
            }
        }
        results.sort(Comparator.comparingDouble(r -> r.avgBuildMs));
        
        System.out.printf("%-26s | %-12s | %-12s | %-10s | %-6s | %-6s%n", 
            "Implementation", "Avg Build", "Iterations", "Memory", "Nodes", "Edges");
        System.out.println("--------------------------|--------------|--------------|----------|-------|-------");
        
        for (RandomRunResults result : results) {
            System.out.printf("%-26s | %12.2f | %12d | %8.2f KB | %-6d | %-6d%n",
                result.implementation,
                result.avgBuildMs,
                result.iterations,
                result.memory / 1024.0,
                result.nodes,
                result.edges);
        }
    }
    
    /**
     * Extracts all edges from a graph
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
     * Mutates the weights of edges in a list.
     * This is a placeholder for a more sophisticated weight mutation logic.
     * For now, it just returns the original edges.
     */
    private List<TestEdge> mutateEdgeWeights(List<TestEdge> edges, Random random) {
        List<TestEdge> mutated = new ArrayList<>(edges.size());
        for (TestEdge edge : edges) {
            double base = edge.weight == 0 ? 1.0 : edge.weight;
            double factor = 0.8 + random.nextDouble() * 0.4; // 0.8x to 1.2x variation
            mutated.add(new TestEdge(edge.from, edge.to, edge.airline, base * factor));
        }
        return mutated;
    }
    
    /**
     * Gets memory usage for a graph
     */
    private long getGraphMemory(Graph graph) {
        try {
            if (graph instanceof MatrixGraph) {
                return ((MatrixGraph) graph).getMemoryUsage();
            } else if (graph instanceof CSRGraph) {
                return ((CSRGraph) graph).getMemoryUsage();
            } else if (graph instanceof OffsetArrayGraph) {
                return ((OffsetArrayGraph) graph).getMemoryUsage();
            } else if (graph instanceof RoutePartitionedTrieGraph) {
                long estimate = 2000;
                estimate += graph.nodeCount() * 100;
                estimate += graph.edgeCount() * 120;
                return estimate;
            } else if (graph instanceof HalfEdgeGraph) {
                long estimate = 1000;
                estimate += graph.nodeCount() * 60;
                estimate += graph.edgeCount() * 120;
                return estimate;
            } else {
                long estimate = 1000;
                estimate += graph.nodeCount() * 50;
                estimate += graph.edgeCount() * 100;
                return estimate;
            }
        } catch (Exception e) {
            return graph.nodeCount() * 50 + graph.edgeCount() * 100;
        }
    }
    
    private static class RandomRunResults {
        final String implementation;
        final String size;
        final double avgBuildMs;
        final long memory;
        final int iterations;
        final int nodes;
        final int edges;
        
        RandomRunResults(String implementation, String size, double avgBuildMs, long memory,
                        int iterations, int nodes, int edges) {
            this.implementation = implementation;
            this.size = size;
            this.avgBuildMs = avgBuildMs;
            this.memory = memory;
            this.iterations = iterations;
            this.nodes = nodes;
            this.edges = edges;
        }
    }
    
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

    private static class BuildInfo {
        final Graph graph;
        final long buildTimeMs;

        BuildInfo(Graph graph, long buildTimeMs) {
            this.graph = graph;
            this.buildTimeMs = buildTimeMs;
        }
    }
}

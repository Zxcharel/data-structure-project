package src.comparison;

import src.graph.Graph;
import src.graph.AdjacencyListGraph;
import src.graph.SortedAdjacencyListGraph;
import src.graph.MatrixGraph;
import src.graph.CSRGraph;
import src.algo.Dijkstra;
import src.algo.AStar;
import src.algo.PathResult;
import src.algo.Constraints;
import src.util.Stopwatch;
import java.util.*;

/**
 * Framework for comparing different graph data structures and algorithms.
 * Provides performance metrics, memory usage, and functionality comparisons.
 */
public class DataStructureComparator {
    
    /**
     * Compares different graph implementations
     * @deprecated Use compareGraphImplementationsWithCSR() instead to include CSRGraph
     */
    @Deprecated
    public GraphComparisonResult compareGraphImplementations(List<String> testNodes, int numEdges) {
        // Create test data
        List<TestEdge> testEdges = generateTestEdges(testNodes, numEdges);
        
        // Test AdjacencyListGraph
        AdjacencyListGraph adjListGraph = new AdjacencyListGraph();
        Stopwatch adjListBuildTime = new Stopwatch();
        adjListBuildTime.start();
        buildGraph(adjListGraph, testEdges);
        adjListBuildTime.stop();
        
        // Test MatrixGraph
        MatrixGraph matrixGraph = new MatrixGraph(testNodes.size() * 2); // Extra capacity
        Stopwatch matrixBuildTime = new Stopwatch();
        matrixBuildTime.start();
        buildGraph(matrixGraph, testEdges);
        matrixBuildTime.stop();
        
        // Performance comparison
        PerformanceMetrics adjListPerf = measureGraphPerformance(adjListGraph, testNodes);
        PerformanceMetrics matrixPerf = measureGraphPerformance(matrixGraph, testNodes);
        
        return new GraphComparisonResult(
            adjListGraph, matrixGraph,
            adjListBuildTime.getElapsedMs(), matrixBuildTime.getElapsedMs(),
            adjListPerf, matrixPerf
        );
    }
    
    /**
     * Compares graph implementations: AdjacencyListGraph, MatrixGraph, and CSRGraph
     * Convenience method that calls compareGraphImplementationsWithCSR().
     */
    public ExtendedGraphComparisonResult compareGraphImplementationsThree(List<String> testNodes, int numEdges) {
        return compareGraphImplementationsWithCSR(testNodes, numEdges);
    }
    
    /**
     * Compares different pathfinding algorithms
     */
    public AlgorithmComparisonResult compareAlgorithms(Graph graph, List<String> testQueries) {
        Dijkstra dijkstra = new Dijkstra();
        AStar aStarZero = new AStar();
        AStar aStarHop = new AStar();
        
        List<AlgorithmResult> results = new ArrayList<>();
        
        for (String query : testQueries) {
            String[] parts = query.split("->");
            if (parts.length != 2) continue;
            
            String origin = parts[0].trim();
            String destination = parts[1].trim();
            
            if (!graph.hasNode(origin) || !graph.hasNode(destination)) continue;
            
            // Test Dijkstra
            PathResult dijkstraResult = dijkstra.findPath(graph, origin, destination);
            results.add(new AlgorithmResult("Dijkstra", origin, destination, dijkstraResult));
            
            // Test A* Zero
            PathResult aStarZeroResult = aStarZero.findPath(graph, origin, destination, 
                                                           new AStar.ZeroHeuristic(), new Constraints());
            results.add(new AlgorithmResult("A* Zero", origin, destination, aStarZeroResult));
            
            // Test A* Hop
            PathResult aStarHopResult = aStarHop.findPath(graph, origin, destination, 
                                                          new AStar.HopHeuristic(), new Constraints());
            results.add(new AlgorithmResult("A* Hop", origin, destination, aStarHopResult));
        }
        
        return new AlgorithmComparisonResult(results);
    }
    
    /**
     * Compares graph implementations including CSR
     */
    public ExtendedGraphComparisonResult compareGraphImplementationsWithCSR(List<String> testNodes, int numEdges) {
        List<TestEdge> testEdges = generateTestEdges(testNodes, numEdges);
        
        // Build AdjacencyListGraph
        AdjacencyListGraph adjListGraph = new AdjacencyListGraph();
        Stopwatch timer = new Stopwatch();
        timer.start();
        buildGraph(adjListGraph, testEdges);
        timer.stop();
        long adjListBuildTime = timer.getElapsedMs();
        PerformanceMetrics adjListPerf = measureGraphPerformance(adjListGraph, testNodes);
        
        // Build MatrixGraph
        MatrixGraph matrixGraph = new MatrixGraph(testNodes.size() * 2);
        timer.reset();
        timer.start();
        buildGraph(matrixGraph, testEdges);
        timer.stop();
        long matrixBuildTime = timer.getElapsedMs();
        PerformanceMetrics matrixPerf = measureGraphPerformance(matrixGraph, testNodes);
        
        // Build CSRGraph from AdjacencyListGraph
        timer.reset();
        timer.start();
        CSRGraph csrGraph = new CSRGraph(adjListGraph);
        timer.stop();
        long csrBuildTime = timer.getElapsedMs();
        PerformanceMetrics csrPerf = measureGraphPerformance(csrGraph, testNodes);
        
        return new ExtendedGraphComparisonResult(
            adjListGraph, matrixGraph, csrGraph,
            adjListBuildTime, matrixBuildTime, csrBuildTime,
            adjListPerf, matrixPerf, csrPerf
        );
    }
    
    /**
     * Compares all graph implementations including SortedAdjacencyListGraph.
     * Compares: AdjacencyListGraph, SortedAdjacencyListGraph, MatrixGraph, and CSRGraph.
     * 
     * @param testNodes List of node names for testing
     * @param numEdges Number of edges to generate for testing
     * @return FullGraphComparisonResult with metrics for all four graph types
     */
    public FullGraphComparisonResult compareAllGraphImplementations(List<String> testNodes, int numEdges) {
        List<TestEdge> testEdges = generateTestEdges(testNodes, numEdges);
        
        // Build AdjacencyListGraph
        AdjacencyListGraph adjListGraph = new AdjacencyListGraph();
        Stopwatch timer = new Stopwatch();
        timer.start();
        buildGraph(adjListGraph, testEdges);
        timer.stop();
        long adjListBuildTime = timer.getElapsedMs();
        PerformanceMetrics adjListPerf = measureGraphPerformance(adjListGraph, testNodes);
        
        // Build SortedAdjacencyListGraph
        SortedAdjacencyListGraph sortedAdjListGraph = new SortedAdjacencyListGraph();
        timer.reset();
        timer.start();
        buildGraph(sortedAdjListGraph, testEdges);
        timer.stop();
        long sortedAdjListBuildTime = timer.getElapsedMs();
        PerformanceMetrics sortedAdjListPerf = measureGraphPerformance(sortedAdjListGraph, testNodes);
        
        // Build MatrixGraph
        MatrixGraph matrixGraph = new MatrixGraph(testNodes.size() * 2);
        timer.reset();
        timer.start();
        buildGraph(matrixGraph, testEdges);
        timer.stop();
        long matrixBuildTime = timer.getElapsedMs();
        PerformanceMetrics matrixPerf = measureGraphPerformance(matrixGraph, testNodes);
        
        // Build CSRGraph from AdjacencyListGraph
        timer.reset();
        timer.start();
        CSRGraph csrGraph = new CSRGraph(adjListGraph);
        timer.stop();
        long csrBuildTime = timer.getElapsedMs();
        PerformanceMetrics csrPerf = measureGraphPerformance(csrGraph, testNodes);
        
        return new FullGraphComparisonResult(
            adjListGraph, sortedAdjListGraph, matrixGraph, csrGraph,
            adjListBuildTime, sortedAdjListBuildTime, matrixBuildTime, csrBuildTime,
            adjListPerf, sortedAdjListPerf, matrixPerf, csrPerf
        );
    }
    
    /**
     * Measures memory usage of different data structures
     * @deprecated Use compareMemoryUsageWithCSR() instead to include CSRGraph
     */
    @Deprecated
    public MemoryComparisonResult compareMemoryUsage(List<String> nodes, int numEdges) {
        List<TestEdge> testEdges = generateTestEdges(nodes, numEdges);
        
        // AdjacencyListGraph memory
        AdjacencyListGraph adjListGraph = new AdjacencyListGraph();
        buildGraph(adjListGraph, testEdges);
        long adjListMemory = estimateAdjacencyListMemory(adjListGraph);
        
        // MatrixGraph memory
        MatrixGraph matrixGraph = new MatrixGraph(nodes.size() * 2);
        buildGraph(matrixGraph, testEdges);
        long matrixMemory = matrixGraph.getMemoryUsage();
        
        return new MemoryComparisonResult(adjListMemory, matrixMemory, nodes.size(), numEdges);
    }
    
    /**
     * Measures memory usage including CSRGraph for all three graph types.
     * Returns ExtendedMemoryComparisonResult with CSR memory included.
     */
    public ExtendedMemoryComparisonResult compareMemoryUsageWithCSR(List<String> nodes, int numEdges) {
        List<TestEdge> testEdges = generateTestEdges(nodes, numEdges);
        
        // AdjacencyListGraph memory
        AdjacencyListGraph adjListGraph = new AdjacencyListGraph();
        buildGraph(adjListGraph, testEdges);
        long adjListMemory = estimateAdjacencyListMemory(adjListGraph);
        
        // MatrixGraph memory
        MatrixGraph matrixGraph = new MatrixGraph(nodes.size() * 2);
        buildGraph(matrixGraph, testEdges);
        long matrixMemory = matrixGraph.getMemoryUsage();
        
        // CSRGraph memory
        CSRGraph csrGraph = new CSRGraph(adjListGraph);
        long csrMemory = csrGraph.getMemoryUsage();
        
        return new ExtendedMemoryComparisonResult(adjListMemory, matrixMemory, csrMemory, nodes.size(), numEdges);
    }
    
    /**
     * Generates test edges for performance testing
     */
    private List<TestEdge> generateTestEdges(List<String> nodes, int numEdges) {
        List<TestEdge> edges = new ArrayList<>();
        Random random = new Random(42);
        
        for (int i = 0; i < numEdges; i++) {
            String from = nodes.get(random.nextInt(nodes.size()));
            String to = nodes.get(random.nextInt(nodes.size()));
            String airline = "test-airline-" + (i % 10);
            double weight = 1.0 + random.nextDouble() * 4.0; // 1.0 to 5.0
            
            edges.add(new TestEdge(from, to, airline, weight));
        }
        
        return edges;
    }
    
    /**
     * Builds a graph from test edges
     */
    private void buildGraph(Graph graph, List<TestEdge> edges) {
        for (TestEdge edge : edges) {
            src.graph.Edge graphEdge = new src.graph.Edge(
                edge.to, edge.airline, 0, 0, 0, 0, 0, edge.weight
            );
            graph.addEdge(edge.from, graphEdge);
        }
    }
    
    /**
     * Measures graph performance metrics
     */
    private PerformanceMetrics measureGraphPerformance(Graph graph, List<String> testNodes) {
        Stopwatch stopwatch = new Stopwatch();
        
        // Test neighbor lookup performance
        stopwatch.start();
        for (String node : testNodes) {
            graph.neighbors(node);
        }
        stopwatch.stop();
        long neighborLookupTime = stopwatch.getElapsedMs();
        
        // Test node existence check performance
        stopwatch.reset();
        stopwatch.start();
        for (String node : testNodes) {
            graph.hasNode(node);
        }
        stopwatch.stop();
        long nodeCheckTime = stopwatch.getElapsedMs();
        
        return new PerformanceMetrics(neighborLookupTime, nodeCheckTime);
    }
    
    /**
     * Estimates memory usage for adjacency list graph
     */
    private static long estimateAdjacencyListMemory(AdjacencyListGraph graph) {
        long baseMemory = 1000; // Base object overhead
        long nodeMemory = graph.nodeCount() * 50; // Per node overhead
        long edgeMemory = graph.edgeCount() * 100; // Per edge overhead
        
        return baseMemory + nodeMemory + edgeMemory;
    }
    
    /**
     * Estimates memory usage for sorted adjacency list graph (same structure)
     */
    private static long estimateSortedAdjacencyListMemory(SortedAdjacencyListGraph graph) {
        long baseMemory = 1000; // Base object overhead
        long nodeMemory = graph.nodeCount() * 50; // Per node overhead
        long edgeMemory = graph.edgeCount() * 100; // Per edge overhead (same as regular adj list)
        
        return baseMemory + nodeMemory + edgeMemory;
    }
    
    /**
     * Data classes for comparison results
     */
    public static class TestEdge {
        final String from, to, airline;
        final double weight;
        
        TestEdge(String from, String to, String airline, double weight) {
            this.from = from;
            this.to = to;
            this.airline = airline;
            this.weight = weight;
        }
    }
    
    public static class GraphComparisonResult {
        private final AdjacencyListGraph adjListGraph;
        private final MatrixGraph matrixGraph;
        private final long adjListBuildTime;
        private final long matrixBuildTime;
        private final PerformanceMetrics adjListPerf;
        private final PerformanceMetrics matrixPerf;
        
        public GraphComparisonResult(AdjacencyListGraph adjListGraph, MatrixGraph matrixGraph,
                                   long adjListBuildTime, long matrixBuildTime,
                                   PerformanceMetrics adjListPerf, PerformanceMetrics matrixPerf) {
            this.adjListGraph = adjListGraph;
            this.matrixGraph = matrixGraph;
            this.adjListBuildTime = adjListBuildTime;
            this.matrixBuildTime = matrixBuildTime;
            this.adjListPerf = adjListPerf;
            this.matrixPerf = matrixPerf;
        }
        
        // Getters
        public AdjacencyListGraph getAdjListGraph() { return adjListGraph; }
        public MatrixGraph getMatrixGraph() { return matrixGraph; }
        public long getAdjListBuildTime() { return adjListBuildTime; }
        public long getMatrixBuildTime() { return matrixBuildTime; }
        public PerformanceMetrics getAdjListPerf() { return adjListPerf; }
        public PerformanceMetrics getMatrixPerf() { return matrixPerf; }
        
        @Override
        public String toString() {
            return String.format(
                "Graph Implementation Comparison:\n" +
                "AdjacencyList: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Matrix: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Memory: AdjList ~%d bytes, Matrix %d bytes",
                adjListBuildTime, adjListPerf.getNeighborLookupTime(), adjListPerf.getNodeCheckTime(),
                matrixBuildTime, matrixPerf.getNeighborLookupTime(), matrixPerf.getNodeCheckTime(),
                estimateAdjacencyListMemory(adjListGraph), matrixGraph.getMemoryUsage()
            );
        }
    }
    
    public static class AlgorithmComparisonResult {
        private final List<AlgorithmResult> results;
        
        public AlgorithmComparisonResult(List<AlgorithmResult> results) {
            this.results = results;
        }
        
        public List<AlgorithmResult> getResults() { return results; }
        
        public Map<String, AlgorithmStats> getAlgorithmStats() {
            Map<String, AlgorithmStats> stats = new HashMap<>();
            
            for (AlgorithmResult result : results) {
                String algorithm = result.getAlgorithm();
                AlgorithmStats stat = stats.computeIfAbsent(algorithm, k -> new AlgorithmStats(algorithm));
                stat.addResult(result.getResult());
            }
            
            return stats;
        }
        
        @Override
        public String toString() {
            Map<String, AlgorithmStats> stats = getAlgorithmStats();
            StringBuilder sb = new StringBuilder("Algorithm Comparison Results:\n");
            
            for (AlgorithmStats stat : stats.values()) {
                sb.append(String.format("%s: Avg Runtime: %.1fms, Avg Edges Relaxed: %.1f, Success Rate: %.1f%%\n",
                    stat.getAlgorithm(), stat.getAvgRuntime(), stat.getAvgEdgesRelaxed(), stat.getSuccessRate() * 100));
            }
            
            return sb.toString();
        }
    }
    
    public static class MemoryComparisonResult {
        private final long adjListMemory;
        private final long matrixMemory;
        private final int numNodes;
        private final int numEdges;
        
        public MemoryComparisonResult(long adjListMemory, long matrixMemory, int numNodes, int numEdges) {
            this.adjListMemory = adjListMemory;
            this.matrixMemory = matrixMemory;
            this.numNodes = numNodes;
            this.numEdges = numEdges;
        }
        
        // Getters
        public long getAdjListMemory() { return adjListMemory; }
        public long getMatrixMemory() { return matrixMemory; }
        public int getNumNodes() { return numNodes; }
        public int getNumEdges() { return numEdges; }
        
        public double getMemoryRatio() {
            return matrixMemory > 0 ? (double) adjListMemory / matrixMemory : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Memory Comparison (%d nodes, %d edges):\n" +
                "AdjacencyList: %d bytes\n" +
                "Matrix: %d bytes\n" +
                "Ratio: %.2fx",
                numNodes, numEdges, adjListMemory, matrixMemory, getMemoryRatio()
            );
        }
    }
    
    public static class ExtendedMemoryComparisonResult {
        private final long adjListMemory;
        private final long matrixMemory;
        private final long csrMemory;
        private final int numNodes;
        private final int numEdges;
        
        public ExtendedMemoryComparisonResult(long adjListMemory, long matrixMemory, long csrMemory, 
                                            int numNodes, int numEdges) {
            this.adjListMemory = adjListMemory;
            this.matrixMemory = matrixMemory;
            this.csrMemory = csrMemory;
            this.numNodes = numNodes;
            this.numEdges = numEdges;
        }
        
        // Getters
        public long getAdjListMemory() { return adjListMemory; }
        public long getMatrixMemory() { return matrixMemory; }
        public long getCSRMemory() { return csrMemory; }
        public int getNumNodes() { return numNodes; }
        public int getNumEdges() { return numEdges; }
        
        public double getMatrixToCSRRatio() {
            return csrMemory > 0 ? (double) matrixMemory / csrMemory : 0;
        }
        
        public double getAdjListToCSRRatio() {
            return csrMemory > 0 ? (double) adjListMemory / csrMemory : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Memory Comparison (%d nodes, %d edges):\n" +
                "AdjacencyList: %d bytes\n" +
                "Matrix: %d bytes\n" +
                "CSR: %d bytes\n" +
                "CSR vs Matrix: %.2fx smaller\n" +
                "CSR vs AdjList: %.2fx smaller",
                numNodes, numEdges, adjListMemory, matrixMemory, csrMemory,
                getMatrixToCSRRatio(), getAdjListToCSRRatio()
            );
        }
    }
    
    public static class ExtendedGraphComparisonResult {
        private final AdjacencyListGraph adjListGraph;
        private final MatrixGraph matrixGraph;
        private final CSRGraph csrGraph;
        private final long adjListBuildTime;
        private final long matrixBuildTime;
        private final long csrBuildTime;
        private final PerformanceMetrics adjListPerf;
        private final PerformanceMetrics matrixPerf;
        private final PerformanceMetrics csrPerf;
        
        public ExtendedGraphComparisonResult(AdjacencyListGraph adjListGraph, MatrixGraph matrixGraph, CSRGraph csrGraph,
                                           long adjListBuildTime, long matrixBuildTime, long csrBuildTime,
                                           PerformanceMetrics adjListPerf, PerformanceMetrics matrixPerf, PerformanceMetrics csrPerf) {
            this.adjListGraph = adjListGraph;
            this.matrixGraph = matrixGraph;
            this.csrGraph = csrGraph;
            this.adjListBuildTime = adjListBuildTime;
            this.matrixBuildTime = matrixBuildTime;
            this.csrBuildTime = csrBuildTime;
            this.adjListPerf = adjListPerf;
            this.matrixPerf = matrixPerf;
            this.csrPerf = csrPerf;
        }
        
        // Getters
        public AdjacencyListGraph getAdjListGraph() { return adjListGraph; }
        public MatrixGraph getMatrixGraph() { return matrixGraph; }
        public CSRGraph getCSRGraph() { return csrGraph; }
        public long getAdjListBuildTime() { return adjListBuildTime; }
        public long getMatrixBuildTime() { return matrixBuildTime; }
        public long getCSRBuildTime() { return csrBuildTime; }
        public PerformanceMetrics getAdjListPerf() { return adjListPerf; }
        public PerformanceMetrics getMatrixPerf() { return matrixPerf; }
        public PerformanceMetrics getCSRPerf() { return csrPerf; }
        
        @Override
        public String toString() {
            return String.format(
                "Graph Implementation Comparison (with CSR):\n" +
                "AdjacencyList: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Matrix: %dms build, %dms neighbor lookup, %dms node check\n" +
                "CSR: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Memory: AdjList ~%d bytes, Matrix %d bytes, CSR %d bytes",
                adjListBuildTime, adjListPerf.getNeighborLookupTime(), adjListPerf.getNodeCheckTime(),
                matrixBuildTime, matrixPerf.getNeighborLookupTime(), matrixPerf.getNodeCheckTime(),
                csrBuildTime, csrPerf.getNeighborLookupTime(), csrPerf.getNodeCheckTime(),
                estimateAdjacencyListMemory(adjListGraph), matrixGraph.getMemoryUsage(), csrGraph.getMemoryUsage()
            );
        }
    }
    
    public static class FullGraphComparisonResult {
        private final AdjacencyListGraph adjListGraph;
        private final SortedAdjacencyListGraph sortedAdjListGraph;
        private final MatrixGraph matrixGraph;
        private final CSRGraph csrGraph;
        private final long adjListBuildTime;
        private final long sortedAdjListBuildTime;
        private final long matrixBuildTime;
        private final long csrBuildTime;
        private final PerformanceMetrics adjListPerf;
        private final PerformanceMetrics sortedAdjListPerf;
        private final PerformanceMetrics matrixPerf;
        private final PerformanceMetrics csrPerf;
        
        public FullGraphComparisonResult(AdjacencyListGraph adjListGraph, SortedAdjacencyListGraph sortedAdjListGraph,
                                        MatrixGraph matrixGraph, CSRGraph csrGraph,
                                        long adjListBuildTime, long sortedAdjListBuildTime, long matrixBuildTime, long csrBuildTime,
                                        PerformanceMetrics adjListPerf, PerformanceMetrics sortedAdjListPerf,
                                        PerformanceMetrics matrixPerf, PerformanceMetrics csrPerf) {
            this.adjListGraph = adjListGraph;
            this.sortedAdjListGraph = sortedAdjListGraph;
            this.matrixGraph = matrixGraph;
            this.csrGraph = csrGraph;
            this.adjListBuildTime = adjListBuildTime;
            this.sortedAdjListBuildTime = sortedAdjListBuildTime;
            this.matrixBuildTime = matrixBuildTime;
            this.csrBuildTime = csrBuildTime;
            this.adjListPerf = adjListPerf;
            this.sortedAdjListPerf = sortedAdjListPerf;
            this.matrixPerf = matrixPerf;
            this.csrPerf = csrPerf;
        }
        
        // Getters
        public AdjacencyListGraph getAdjListGraph() { return adjListGraph; }
        public SortedAdjacencyListGraph getSortedAdjListGraph() { return sortedAdjListGraph; }
        public MatrixGraph getMatrixGraph() { return matrixGraph; }
        public CSRGraph getCSRGraph() { return csrGraph; }
        public long getAdjListBuildTime() { return adjListBuildTime; }
        public long getSortedAdjListBuildTime() { return sortedAdjListBuildTime; }
        public long getMatrixBuildTime() { return matrixBuildTime; }
        public long getCSRBuildTime() { return csrBuildTime; }
        public PerformanceMetrics getAdjListPerf() { return adjListPerf; }
        public PerformanceMetrics getSortedAdjListPerf() { return sortedAdjListPerf; }
        public PerformanceMetrics getMatrixPerf() { return matrixPerf; }
        public PerformanceMetrics getCSRPerf() { return csrPerf; }
        
        @Override
        public String toString() {
            return String.format(
                "Graph Implementation Comparison (All 4 Types):\n" +
                "AdjacencyList: %dms build, %dms neighbor lookup, %dms node check\n" +
                "SortedAdjList: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Matrix: %dms build, %dms neighbor lookup, %dms node check\n" +
                "CSR: %dms build, %dms neighbor lookup, %dms node check\n" +
                "Memory: AdjList ~%d bytes, SortedAdjList ~%d bytes, Matrix %d bytes, CSR %d bytes",
                adjListBuildTime, adjListPerf.getNeighborLookupTime(), adjListPerf.getNodeCheckTime(),
                sortedAdjListBuildTime, sortedAdjListPerf.getNeighborLookupTime(), sortedAdjListPerf.getNodeCheckTime(),
                matrixBuildTime, matrixPerf.getNeighborLookupTime(), matrixPerf.getNodeCheckTime(),
                csrBuildTime, csrPerf.getNeighborLookupTime(), csrPerf.getNodeCheckTime(),
                estimateAdjacencyListMemory(adjListGraph), estimateSortedAdjacencyListMemory(sortedAdjListGraph),
                matrixGraph.getMemoryUsage(), csrGraph.getMemoryUsage()
            );
        }
    }
    
    public static class PerformanceMetrics {
        private final long neighborLookupTime;
        private final long nodeCheckTime;
        
        public PerformanceMetrics(long neighborLookupTime, long nodeCheckTime) {
            this.neighborLookupTime = neighborLookupTime;
            this.nodeCheckTime = nodeCheckTime;
        }
        
        public long getNeighborLookupTime() { return neighborLookupTime; }
        public long getNodeCheckTime() { return nodeCheckTime; }
    }
    
    public static class AlgorithmResult {
        private final String algorithm;
        private final String origin;
        private final String destination;
        private final PathResult result;
        
        public AlgorithmResult(String algorithm, String origin, String destination, PathResult result) {
            this.algorithm = algorithm;
            this.origin = origin;
            this.destination = destination;
            this.result = result;
        }
        
        public String getAlgorithm() { return algorithm; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public PathResult getResult() { return result; }
    }
    
    public static class AlgorithmStats {
        private final String algorithm;
        private int totalQueries = 0;
        private int successfulQueries = 0;
        private long totalRuntime = 0;
        private int totalEdgesRelaxed = 0;
        
        public AlgorithmStats(String algorithm) {
            this.algorithm = algorithm;
        }
        
        public void addResult(PathResult result) {
            totalQueries++;
            if (result.isFound()) {
                successfulQueries++;
            }
            totalRuntime += result.getRuntimeMs();
            totalEdgesRelaxed += result.getEdgesRelaxed();
        }
        
        public String getAlgorithm() { return algorithm; }
        public double getAvgRuntime() { return totalQueries > 0 ? (double) totalRuntime / totalQueries : 0; }
        public double getAvgEdgesRelaxed() { return totalQueries > 0 ? (double) totalEdgesRelaxed / totalQueries : 0; }
        public double getSuccessRate() { return totalQueries > 0 ? (double) successfulQueries / totalQueries : 0; }
    }
}

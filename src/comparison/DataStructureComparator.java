package src.comparison;

import src.graph.Graph;
import src.graph.AdjacencyListGraph;
import src.graph.MatrixGraph;
import src.graph.LinearArrayGraph;
import src.graph.DynamicArrayGraph;
import src.graph.OffsetArrayGraph;
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
     */
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

        // Test LinearArrayGraph
        LinearArrayGraph linearArrayGraph = new LinearArrayGraph();
        Stopwatch linearArrayBuildTime = new Stopwatch();
        linearArrayBuildTime.start();
        buildGraph(linearArrayGraph, testEdges);
        linearArrayBuildTime.stop();

        // Test DynamicArrayGraph
        DynamicArrayGraph dynamicArrayGraph = new DynamicArrayGraph();
        Stopwatch dynamicArrayBuildTime = new Stopwatch();
        dynamicArrayBuildTime.start();
        buildGraph(dynamicArrayGraph, testEdges);
        dynamicArrayBuildTime.stop();

        // Test OffsetArrayGraph
        OffsetArrayGraph offsetArrayGraph = new OffsetArrayGraph();
        Stopwatch offsetArrayBuildTime = new Stopwatch();
        offsetArrayBuildTime.start();
        buildGraph(offsetArrayGraph, testEdges);
        offsetArrayGraph.finalizeCSR(); // Optimize for performance testing
        offsetArrayBuildTime.stop();

        // Performance comparison
        PerformanceMetrics adjListPerf = measureGraphPerformance(adjListGraph, testNodes);
        PerformanceMetrics matrixPerf = measureGraphPerformance(matrixGraph, testNodes);
        PerformanceMetrics linearArrayPerf = measureGraphPerformance(linearArrayGraph, testNodes);
        PerformanceMetrics dynamicArrayPerf = measureGraphPerformance(dynamicArrayGraph, testNodes);
        PerformanceMetrics offsetArrayPerf = measureGraphPerformance(offsetArrayGraph, testNodes);

        return new GraphComparisonResult(
                adjListGraph, matrixGraph, linearArrayGraph, dynamicArrayGraph, offsetArrayGraph,
                adjListBuildTime.getElapsedMs(), matrixBuildTime.getElapsedMs(),
                linearArrayBuildTime.getElapsedMs(), dynamicArrayBuildTime.getElapsedMs(),
                offsetArrayBuildTime.getElapsedMs(),
                adjListPerf, matrixPerf, linearArrayPerf, dynamicArrayPerf, offsetArrayPerf);
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
            if (parts.length != 2)
                continue;

            String origin = parts[0].trim();
            String destination = parts[1].trim();

            if (!graph.hasNode(origin) || !graph.hasNode(destination))
                continue;

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
     * Measures memory usage of different data structures
     */
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

        // LinearArrayGraph memory
        LinearArrayGraph linearArrayGraph = new LinearArrayGraph();
        buildGraph(linearArrayGraph, testEdges);
        long linearArrayMemory = linearArrayGraph.getMemoryUsage();

        // DynamicArrayGraph memory
        DynamicArrayGraph dynamicArrayGraph = new DynamicArrayGraph();
        buildGraph(dynamicArrayGraph, testEdges);
        long dynamicArrayMemory = dynamicArrayGraph.getMemoryUsage();

        // OffsetArrayGraph memory
        OffsetArrayGraph offsetArrayGraph = new OffsetArrayGraph();
        buildGraph(offsetArrayGraph, testEdges);
        offsetArrayGraph.finalizeCSR(); // Ensure CSR structure is built
        long offsetArrayMemory = offsetArrayGraph.getMemoryUsage();

        return new MemoryComparisonResult(adjListMemory, matrixMemory, linearArrayMemory, dynamicArrayMemory,
                offsetArrayMemory,
                nodes.size(), numEdges);
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
                    edge.to, edge.airline, 0, 0, 0, 0, 0, edge.weight);
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
        private final LinearArrayGraph linearArrayGraph;
        private final DynamicArrayGraph dynamicArrayGraph;
        private final OffsetArrayGraph offsetArrayGraph;
        private final long adjListBuildTime;
        private final long matrixBuildTime;
        private final long linearArrayBuildTime;
        private final long dynamicArrayBuildTime;
        private final long offsetArrayBuildTime;
        private final PerformanceMetrics adjListPerf;
        private final PerformanceMetrics matrixPerf;
        private final PerformanceMetrics linearArrayPerf;
        private final PerformanceMetrics dynamicArrayPerf;
        private final PerformanceMetrics offsetArrayPerf;

        public GraphComparisonResult(AdjacencyListGraph adjListGraph, MatrixGraph matrixGraph,
                LinearArrayGraph linearArrayGraph, DynamicArrayGraph dynamicArrayGraph,
                OffsetArrayGraph offsetArrayGraph,
                long adjListBuildTime, long matrixBuildTime, long linearArrayBuildTime, long dynamicArrayBuildTime,
                long offsetArrayBuildTime,
                PerformanceMetrics adjListPerf, PerformanceMetrics matrixPerf, PerformanceMetrics linearArrayPerf,
                PerformanceMetrics dynamicArrayPerf, PerformanceMetrics offsetArrayPerf) {
            this.adjListGraph = adjListGraph;
            this.matrixGraph = matrixGraph;
            this.linearArrayGraph = linearArrayGraph;
            this.dynamicArrayGraph = dynamicArrayGraph;
            this.offsetArrayGraph = offsetArrayGraph;
            this.adjListBuildTime = adjListBuildTime;
            this.matrixBuildTime = matrixBuildTime;
            this.linearArrayBuildTime = linearArrayBuildTime;
            this.dynamicArrayBuildTime = dynamicArrayBuildTime;
            this.offsetArrayBuildTime = offsetArrayBuildTime;
            this.adjListPerf = adjListPerf;
            this.matrixPerf = matrixPerf;
            this.linearArrayPerf = linearArrayPerf;
            this.dynamicArrayPerf = dynamicArrayPerf;
            this.offsetArrayPerf = offsetArrayPerf;
        }

        // Getters
        public AdjacencyListGraph getAdjListGraph() {
            return adjListGraph;
        }

        public MatrixGraph getMatrixGraph() {
            return matrixGraph;
        }

        public LinearArrayGraph getLinearArrayGraph() {
            return linearArrayGraph;
        }

        public DynamicArrayGraph getDynamicArrayGraph() {
            return dynamicArrayGraph;
        }

        public OffsetArrayGraph getOffsetArrayGraph() {
            return offsetArrayGraph;
        }

        public long getAdjListBuildTime() {
            return adjListBuildTime;
        }

        public long getMatrixBuildTime() {
            return matrixBuildTime;
        }

        public long getLinearArrayBuildTime() {
            return linearArrayBuildTime;
        }

        public long getDynamicArrayBuildTime() {
            return dynamicArrayBuildTime;
        }

        public long getOffsetArrayBuildTime() {
            return offsetArrayBuildTime;
        }

        public PerformanceMetrics getAdjListPerf() {
            return adjListPerf;
        }

        public PerformanceMetrics getMatrixPerf() {
            return matrixPerf;
        }

        public PerformanceMetrics getLinearArrayPerf() {
            return linearArrayPerf;
        }

        public PerformanceMetrics getDynamicArrayPerf() {
            return dynamicArrayPerf;
        }

        public PerformanceMetrics getOffsetArrayPerf() {
            return offsetArrayPerf;
        }

        @Override
        public String toString() {
            return String.format(
                    "Graph Implementation Comparison:\n" +
                            "AdjacencyList: %dms build, %dms neighbor lookup, %dms node check\n" +
                            "Matrix: %dms build, %dms neighbor lookup, %dms node check\n" +
                            "LinearArray: %dms build, %dms neighbor lookup, %dms node check\n" +
                            "DynamicArray: %dms build, %dms neighbor lookup, %dms node check\n" +
                            "OffsetArray: %dms build, %dms neighbor lookup, %dms node check\n" +
                            "Memory: AdjList ~%d bytes, Matrix %d bytes, LinearArray %d bytes, DynamicArray %d bytes, OffsetArray %d bytes",
                    adjListBuildTime, adjListPerf.getNeighborLookupTime(), adjListPerf.getNodeCheckTime(),
                    matrixBuildTime, matrixPerf.getNeighborLookupTime(), matrixPerf.getNodeCheckTime(),
                    linearArrayBuildTime, linearArrayPerf.getNeighborLookupTime(), linearArrayPerf.getNodeCheckTime(),
                    dynamicArrayBuildTime, dynamicArrayPerf.getNeighborLookupTime(),
                    dynamicArrayPerf.getNodeCheckTime(),
                    offsetArrayBuildTime, offsetArrayPerf.getNeighborLookupTime(), offsetArrayPerf.getNodeCheckTime(),
                    estimateAdjacencyListMemory(adjListGraph), matrixGraph.getMemoryUsage(),
                    linearArrayGraph.getMemoryUsage(), dynamicArrayGraph.getMemoryUsage(),
                    offsetArrayGraph.getMemoryUsage());
        }
    }

    public static class AlgorithmComparisonResult {
        private final List<AlgorithmResult> results;

        public AlgorithmComparisonResult(List<AlgorithmResult> results) {
            this.results = results;
        }

        public List<AlgorithmResult> getResults() {
            return results;
        }

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
                        stat.getAlgorithm(), stat.getAvgRuntime(), stat.getAvgEdgesRelaxed(),
                        stat.getSuccessRate() * 100));
            }

            return sb.toString();
        }
    }

    public static class MemoryComparisonResult {
        private final long adjListMemory;
        private final long matrixMemory;
        private final long linearArrayMemory;
        private final long dynamicArrayMemory;
        private final long offsetArrayMemory;
        private final int numNodes;
        private final int numEdges;

        public MemoryComparisonResult(long adjListMemory, long matrixMemory, long linearArrayMemory,
                long dynamicArrayMemory, long offsetArrayMemory, int numNodes,
                int numEdges) {
            this.adjListMemory = adjListMemory;
            this.matrixMemory = matrixMemory;
            this.linearArrayMemory = linearArrayMemory;
            this.dynamicArrayMemory = dynamicArrayMemory;
            this.offsetArrayMemory = offsetArrayMemory;
            this.numNodes = numNodes;
            this.numEdges = numEdges;
        }

        // Getters
        public long getAdjListMemory() {
            return adjListMemory;
        }

        public long getMatrixMemory() {
            return matrixMemory;
        }

        public long getLinearArrayMemory() {
            return linearArrayMemory;
        }

        public long getDynamicArrayMemory() {
            return dynamicArrayMemory;
        }

        public long getOffsetArrayMemory() {
            return offsetArrayMemory;
        }

        public int getNumNodes() {
            return numNodes;
        }

        public int getNumEdges() {
            return numEdges;
        }

        public double getMemoryRatio() {
            return matrixMemory > 0 ? (double) adjListMemory / matrixMemory : 0;
        }

        public double getLinearArrayMemoryRatio() {
            return matrixMemory > 0 ? (double) linearArrayMemory / matrixMemory : 0;
        }

        public double getDynamicArrayMemoryRatio() {
            return matrixMemory > 0 ? (double) dynamicArrayMemory / matrixMemory : 0;
        }

        public double getOffsetArrayMemoryRatio() {
            return matrixMemory > 0 ? (double) offsetArrayMemory / matrixMemory : 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Memory Comparison (%d nodes, %d edges):\n" +
                            "AdjacencyList: %d bytes\n" +
                            "Matrix: %d bytes\n" +
                            "LinearArray: %d bytes\n" +
                            "DynamicArray: %d bytes\n" +
                            "OffsetArray: %d bytes\n" +
                            "Ratios: AdjList/Matrix=%.2fx, LinearArray/Matrix=%.2fx, DynamicArray/Matrix=%.2fx, OffsetArray/Matrix=%.2fx",
                    numNodes, numEdges, adjListMemory, matrixMemory, linearArrayMemory, dynamicArrayMemory,
                    offsetArrayMemory,
                    getMemoryRatio(), getLinearArrayMemoryRatio(), getDynamicArrayMemoryRatio(),
                    getOffsetArrayMemoryRatio());
        }
    }

    public static class PerformanceMetrics {
        private final long neighborLookupTime;
        private final long nodeCheckTime;

        public PerformanceMetrics(long neighborLookupTime, long nodeCheckTime) {
            this.neighborLookupTime = neighborLookupTime;
            this.nodeCheckTime = nodeCheckTime;
        }

        public long getNeighborLookupTime() {
            return neighborLookupTime;
        }

        public long getNodeCheckTime() {
            return nodeCheckTime;
        }
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

        public String getAlgorithm() {
            return algorithm;
        }

        public String getOrigin() {
            return origin;
        }

        public String getDestination() {
            return destination;
        }

        public PathResult getResult() {
            return result;
        }
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

        public String getAlgorithm() {
            return algorithm;
        }

        public double getAvgRuntime() {
            return totalQueries > 0 ? (double) totalRuntime / totalQueries : 0;
        }

        public double getAvgEdgesRelaxed() {
            return totalQueries > 0 ? (double) totalEdgesRelaxed / totalQueries : 0;
        }

        public double getSuccessRate() {
            return totalQueries > 0 ? (double) successfulQueries / totalQueries : 0;
        }
    }
}

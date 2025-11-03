# Experiment 1: Pathfinding Performance Benchmark

**Algorithm**: Dijkstra's Algorithm  
**Research Question**: How do different graph data structures affect Dijkstra's algorithm runtime and efficiency?

---

## Overview

This experiment provides a comprehensive, end-to-end performance comparison of all graph implementations when running Dijkstra's algorithm. It tests complete pathfinding scenarios to identify which structures perform best in real-world usage.

---

## Methodology

### Test Setup
1. **Graph Construction**: Build all graph types from the same CSV data (`data/cleaned_flights.csv`)
2. **Query Generation**: Generate 100 random origin-destination pairs from the flight data
3. **Path Categories**: 
   - Short paths (2-3 hops): 30 queries
   - Medium paths (5-8 hops): 40 queries
   - Long paths (10+ hops): 30 queries
4. **Algorithm**: Run Dijkstra's algorithm on each query for each graph structure
5. **Multiple Runs**: Run each query 5 times per graph structure and average results

### Special Considerations
- **MatrixGraph**: Requires `maxNodes` parameter - build temporary graph first to determine node count
- **CSRGraph**: Built from existing graph (built from AdjacencyListGraph as base)
- **All other graphs**: Built directly from CSV using factory pattern

---

## Metrics Collected

### Primary Metrics
1. **Total Runtime (ms)**: End-to-end Dijkstra execution time
2. **Nodes Visited**: Number of nodes processed by Dijkstra
3. **Edges Relaxed**: Number of edges examined during pathfinding
4. **Memory Footprint**: Peak memory usage during Dijkstra execution
5. **Success Rate**: Percentage of queries where path was found

### Secondary Metrics
1. **Average Path Length**: Average number of hops in found paths
2. **Path Weight**: Average total weight/cost of found paths
3. **Build Time**: Time to construct each graph (for context)
4. **Runtime Variance**: Standard deviation of runtimes (consistency measure)

---

## Hypothesis

### Expected Performance Rankings

1. **CSRGraph** and **OffsetArrayGraph**: 
   - Should be fastest due to cache-friendly sequential access
   - Compact memory layout improves cache locality
   - Expected: 20-40% faster than AdjacencyListGraph

2. **SortedAdjacencyListGraph**: 
   - May provide benefits if pre-sorted edges help Dijkstra's greedy selection
   - Could reduce priority queue operations
   - Expected: 10-20% faster than standard AdjacencyListGraph

3. **MatrixGraph**: 
   - Likely slowest for sparse graphs (must iterate through all nodes per neighbor lookup)
   - High memory overhead doesn't translate to speed for sparse graphs
   - Expected: 50-100% slower than AdjacencyListGraph

4. **AdjacencyListGraph** (baseline):
   - Standard implementation, good balance
   - Expected to be middle-of-the-pack performance

5. **Other Specialized Structures**:
   - **RoutePartitionedTrieGraph**: May be slower due to tree traversal overhead
   - **LinkCutTreeGraph/EulerTourTreeGraph**: Adapter patterns may add overhead
   - **List-based variants**: Similar to AdjacencyListGraph but with different overhead

---

## Expected Insights

### Performance Characteristics
- **Which structure minimizes Dijkstra's execution time**: Overall speed winner
- **Trade-offs between memory and speed**: Whether memory efficiency costs performance
- **Impact of data layout on algorithm performance**: How structure choice affects runtime

### Path Type Analysis
- **Short paths**: May favor simpler structures (less iteration benefit)
- **Medium paths**: Balance between structure efficiency and overhead
- **Long paths**: May favor optimized structures (iteration benefits compound)

### Memory-Performance Correlation
- **Compact structures** (CSR, OffsetArray): Should show best memory/speed ratio
- **Standard structures** (AdjacencyList): Moderate memory, moderate speed
- **Dense structures** (Matrix): High memory, potentially slower

---

## Implementation Guide

### Code Structure
```java
// Pseudo-code structure
public class PathfindingBenchmark {
    private List<Graph> allGraphs;
    private List<Query> testQueries;
    private Dijkstra dijkstra;
    
    public BenchmarkResult runBenchmark() {
        // Build all graph types
        buildAllGraphs();
        
        // Generate test queries
        generateQueries(100);
        
        // Run Dijkstra on each graph for each query
        Map<Graph, List<QueryResult>> results = new HashMap<>();
        for (Graph graph : allGraphs) {
            List<QueryResult> graphResults = new ArrayList<>();
            for (Query query : testQueries) {
                for (int run = 0; run < 5; run++) {
                    PathResult result = dijkstra.findPath(
                        graph, query.origin, query.destination
                    );
                    graphResults.add(extractMetrics(result));
                }
            }
            results.put(graph, graphResults);
        }
        
        // Analyze and return results
        return analyzeResults(results);
    }
}
```

### Query Generation
```java
private List<Query> generateQueries(int count) {
    // Ensure diverse path lengths
    // Use graph analysis to identify:
    // - Nodes with many connections (hubs)
    // - Nodes with few connections (sparse)
    // - Random pairs for variety
}
```

### Metrics Collection
```java
private QueryMetrics extractMetrics(PathResult result, long runtime) {
    return new QueryMetrics(
        result.getRuntime(),      // ms
        result.getNodesVisited(),
        result.getEdgesRelaxed(),
        result.getPathLength(),
        result.isPathFound(),
        getMemoryUsage()          // peak memory during execution
    );
}
```

---

## Data Analysis

### Statistical Analysis
1. **Average Runtime**: Mean execution time per graph type
2. **Median Runtime**: Less affected by outliers
3. **95th Percentile**: Worst-case performance
4. **Coefficient of Variation**: Consistency measure (std dev / mean)

### Comparative Analysis
1. **Speedup**: How much faster/slower relative to baseline (AdjacencyListGraph)
2. **Memory Efficiency**: Runtime per MB of memory used
3. **Success Rate Comparison**: Which structures handle edge cases better

### Visualization Recommendations
1. **Bar Chart**: Average runtime by graph type
2. **Box Plot**: Runtime distribution (showing quartiles and outliers)
3. **Scatter Plot**: Memory vs Runtime (trade-off analysis)
4. **Line Chart**: Performance by path length category

---

## Expected Outcomes

### Performance Rankings (Predicted)
1. **CSRGraph**: Fastest overall, best memory efficiency
2. **OffsetArrayGraph**: Close second, similar characteristics
3. **SortedAdjacencyListGraph**: Moderate speedup from pre-sorting
4. **AdjacencyListGraph**: Baseline, solid middle performance
5. **LinearArrayGraph/DynamicArrayGraph**: Similar to AdjacencyList
6. **List variants** (DoublyLinkedList, CircularLinkedList): Slightly slower
7. **MatrixGraph**: Slowest, especially for sparse graphs
8. **Specialized structures**: Variable, adapter overhead may dominate

### Key Findings (Expected)
- **Overall winner**: CSRGraph or OffsetArrayGraph (best balance)
- **Best memory**: CSRGraph, OffsetArrayGraph (compact representation)
- **Best speed**: CSRGraph (cache-optimized)
- **Worst performance**: MatrixGraph (poor for sparse graphs)
- **Best for short paths**: Simpler structures (less overhead)
- **Best for long paths**: Optimized structures (compounding benefits)

---

## Interpretation Guidelines

### What to Look For

1. **Clear Winners/Losers**: Structures that consistently outperform/underperform
2. **Consistency**: Structures with low variance (reliable performance)
3. **Memory-Performance Trade-offs**: Whether lower memory costs speed
4. **Path Length Sensitivity**: Do different structures excel at different path lengths?

### Red Flags
- **Extremely high variance**: May indicate implementation issues
- **Unusually slow builds**: May affect practical usability
- **Memory spikes**: May cause issues in production environments

### Success Criteria
- **Statistical significance**: Results should be repeatable
- **Practical relevance**: Differences should be meaningful (>10%)
- **Complete data**: All graph types successfully tested
- **Interpretable results**: Clear recommendations possible

---

## Discussion Points

### Why This Experiment Matters
1. **Real-world relevance**: Tests actual usage patterns
2. **Complete picture**: Includes all components (build + query)
3. **Actionable results**: Direct recommendations for implementation choice
4. **Comprehensive**: Multiple metrics provide complete analysis

### Limitations
1. **Single dataset**: Results may vary with different graph characteristics
2. **Query selection**: Random queries may not reflect all use cases
3. **Hardware dependent**: Results may vary on different systems
4. **JVM warmup**: First runs may be slower (use warmup runs)

### Future Extensions
1. **Different query patterns**: Hub-to-hub, hub-to-spoke, random
2. **Larger datasets**: Test scalability implications
3. **Concurrent queries**: Multi-threaded performance
4. **Different algorithms**: Compare with A* for context

---

## Summary

This benchmark provides the **"WHAT"** - a complete performance profile of all graph structures. Combined with Experiment 3 (WHY) and Experiment 4 (WHEN), it forms a comprehensive understanding of when and why to choose each graph implementation.

**Next Steps**: See Experiment 3 for bottleneck analysis and Experiment 4 for scalability characteristics.


# Experiment 4: Scalability Analysis Across Graph Sizes

**Algorithm**: Dijkstra's Algorithm  
**Research Question**: How do graph structures perform as graph size increases?

---

## Overview

This experiment tests how each graph data structure scales as the graph grows in size. By testing the same structures on progressively larger subsets of the data, we can identify which structures scale linearly, quadratically, or exhibit other growth patterns. This provides critical insights for choosing the right structure based on expected graph size.

---

## Why This Experiment is Very Interesting

### Practical Importance
- **Real-world relevance**: Real graphs grow over time (new airports, routes added)
- **Decision framework**: Helps choose structure based on expected size
- **Production planning**: Know when performance will degrade
- **Cost analysis**: Understand resource requirements at scale

### Reveals Big-O Behavior
- **Theoretical meets practical**: Shows theoretical complexity (O notation) in real measurements
- **Asymptotic analysis**: Identifies which structures scale best long-term
- **Break-even points**: When one structure becomes better than another at scale
- **Non-linear effects**: Identifies structures with surprising scaling behavior

### Academic Rigor
- **Algorithmic complexity understanding**: Demonstrates knowledge of big-O analysis
- **Empirical validation**: Tests theoretical predictions with real data
- **Comparative analysis**: Compares multiple structures' scaling characteristics
- **Scientific methodology**: Systematic approach to scalability testing

### Unique Aspects
- **Tests real scalability**: Not just "is it fast" but "how does it scale"
- **Identifies thresholds**: Shows when to switch structures
- **Practical limits**: Finds size limits for each structure
- **Combines theory and practice**: Big-O predictions vs actual measurements

---

## Methodology

### Graph Size Subsets

Create progressively larger subsets of your flight data:
1. **10% subset**: ~300 nodes, ~500 edges (small graph)
2. **25% subset**: ~750 nodes, ~1,250 edges (medium-small)
3. **50% subset**: ~1,500 nodes, ~2,500 edges (medium)
4. **75% subset**: ~2,250 nodes, ~3,750 edges (medium-large)
5. **100% subset**: ~3,000 nodes, ~5,000 edges (full dataset)

### Test Execution

For each size subset:
1. **Build all graph types** from that subset
2. **Measure build time** for each structure
3. **Run identical pathfinding queries** (same origin-destination pairs) on each structure
4. **Measure Dijkstra runtime** for each query
5. **Measure memory usage** (peak during execution)
6. **Repeat 3-5 times** and average results

### Query Selection
- Use same set of queries across all sizes (if nodes exist)
- Ensure queries are meaningful at each size
- Mix of short, medium, and long paths
- ~20-30 queries per size subset

---

## Metrics Collected

### Primary Metrics

1. **Build Time Growth**:
   - Time to construct graph from CSV data
   - Measure: milliseconds
   - Analyze: O(?) complexity (linear, quadratic, etc.)
   - Calculate: Build time vs nodes/edges ratio

2. **Dijkstra Runtime Growth**:
   - Time to execute Dijkstra on same queries
   - Measure: milliseconds per query (average)
   - Analyze: O(?) complexity
   - Calculate: Runtime vs graph size

3. **Memory Usage Growth**:
   - Peak memory during Dijkstra execution
   - Measure: bytes or MB
   - Analyze: Memory vs nodes/edges relationship
   - Calculate: Memory per node/edge

4. **Performance Degradation Ratio**:
   - How much slower when size doubles
   - Measure: 2x size = ?x slower
   - Ideal: 2x slower (linear) or less
   - Problematic: >2x slower (worse than linear)

### Secondary Metrics

1. **Build Time Per Edge/Node**:
   - Normalized build cost
   - Shows efficiency of construction
   - Helps identify build-time bottlenecks

2. **Query Time Per Node Visited**:
   - Normalized query performance
   - Accounts for different path lengths
   - Shows algorithmic efficiency at scale

3. **Memory Efficiency**:
   - Bytes per edge/node
   - Memory overhead analysis
   - Identifies memory-inefficient structures

4. **Consistency**:
   - Variance in measurements
   - Reliability at scale
   - Outlier detection

---

## Hypothesis

### Expected Scaling Characteristics

#### Sparse Structures (Expected Linear Scaling)

1. **CSRGraph**:
   - Build time: O(V + E) - linear
   - Query time: O(V + E log V) - near-linear for sparse graphs
   - Memory: O(V + E) - linear
   - Expected: Best scaling for large sparse graphs

2. **OffsetArrayGraph**:
   - Similar to CSRGraph
   - Linear scaling expected
   - Expected: Excellent scalability

3. **LinearArrayGraph**:
   - Array-based, should scale linearly
   - Expected: Good scalability

4. **AdjacencyListGraph**:
   - Build: O(E) - linear
   - Query: O(V + E log V) - near-linear
   - Memory: O(V + E) - linear
   - Expected: Good scaling, some overhead

#### Dense Structures (Expected Quadratic Scaling)

1. **MatrixGraph**:
   - Build: O(V²) - quadratic (must initialize matrix)
   - Query: O(V²) - quadratic (checks all nodes per neighbor)
   - Memory: O(V²) - quadratic
   - Expected: Poor scalability, becomes worse at large sizes

#### List-Based Structures (Expected Linear with Overhead)

1. **DoublyLinkedListGraph**:
   - Build: O(E) - linear
   - Query: O(V + E log V) - near-linear
   - Memory: O(V + E) - linear, but more overhead per edge
   - Expected: Linear scaling but slower than arrays

2. **CircularLinkedListGraph**:
   - Similar to DoublyLinkedList
   - Expected: Comparable scaling

#### Specialized Structures (Variable)

1. **SortedAdjacencyListGraph**:
   - Build: O(E log E) - worse than linear (sorting overhead)
   - Query: O(V + E log V) - near-linear
   - Memory: O(V + E) - linear
   - Expected: Build time scales worse, query time similar

2. **RoutePartitionedTrieGraph**:
   - Complex structure, scaling depends on implementation
   - Expected: May have non-linear components

---

## Expected Insights

### Scalability Patterns

1. **Which structure is best for small vs large graphs**:
   - Simple structures may win at small sizes (less overhead)
   - Optimized structures excel at large sizes (overhead amortized)

2. **Scalability characteristics (big-O behavior)**:
   - Linear: O(n) - ideal
   - Near-linear: O(n log n) - acceptable
   - Quadratic: O(n²) - problematic for large graphs

3. **Practical size limits**:
   - When does performance become unacceptable?
   - Memory limits for each structure
   - When to switch to a different structure

4. **Break-even points**:
   - Size threshold where one structure becomes better
   - Trade-off analysis (build time vs query time)
   - Memory vs speed trade-offs at scale

### Build Time Analysis

1. **Fast builders** (AdjacencyListGraph):
   - Quick to construct
   - May be worth it for one-time queries
   - Good for dynamic graphs (frequent rebuilds)

2. **Slow builders** (CSRGraph, SortedAdjacencyListGraph):
   - Expensive construction
   - Worth it if running many queries
   - Better for static graphs

3. **Scalable builders**:
   - Build time grows linearly with size
   - Can handle growing datasets

### Query Time Analysis

1. **Linear scaling**:
   - Query time grows proportionally with graph size
   - Ideal behavior
   - Predictable performance

2. **Sub-linear scaling**:
   - Better than linear (unlikely but possible)
   - May indicate caching effects

3. **Super-linear scaling**:
   - Worse than linear (quadratic, exponential)
   - Problematic for large graphs
   - May indicate algorithmic issues

### Memory Analysis

1. **Memory-efficient structures**:
   - Low memory per edge/node
   - Can handle larger graphs in same memory
   - Important for resource-constrained environments

2. **Memory-intensive structures**:
   - High memory overhead
   - May hit memory limits at smaller sizes
   - Trade memory for other benefits

---

## Implementation Guide

### Size Subset Creation
```java
public class GraphSizeSubset {
    
    public Graph createSubset(Graph fullGraph, double percentage) {
        // Create subset by:
        // 1. Randomly selecting nodes
        // 2. Including all edges between selected nodes
        // 3. Maintaining graph connectivity (if desired)
        
        List<String> allNodes = fullGraph.nodes();
        int subsetSize = (int) (allNodes.size() * percentage);
        
        // Random selection
        Collections.shuffle(allNodes);
        Set<String> selectedNodes = new HashSet<>(
            allNodes.subList(0, subsetSize)
        );
        
        // Build new graph with subset
        Graph subsetGraph = new AdjacencyListGraph();
        for (String node : selectedNodes) {
            for (Edge edge : fullGraph.neighbors(node)) {
                if (selectedNodes.contains(edge.getDestination())) {
                    subsetGraph.addEdge(node, edge);
                }
            }
        }
        
        return subsetGraph;
    }
}
```

### Scaling Analysis
```java
public class ScalabilityAnalyzer {
    
    public ScalingResult analyzeScaling(List<SizeResult> results) {
        // Calculate growth rates
        Map<String, List<Double>> growthRates = new HashMap<>();
        
        for (int i = 1; i < results.size(); i++) {
            SizeResult prev = results.get(i-1);
            SizeResult curr = results.get(i);
            
            double sizeRatio = (double) curr.nodeCount / prev.nodeCount;
            
            // Build time growth
            double buildGrowth = curr.buildTime / prev.buildTime;
            double buildComplexity = Math.log(buildGrowth) / Math.log(sizeRatio);
            
            // Query time growth
            double queryGrowth = curr.avgQueryTime / prev.avgQueryTime;
            double queryComplexity = Math.log(queryGrowth) / Math.log(sizeRatio);
            
            // Memory growth
            double memoryGrowth = curr.memoryUsage / prev.memoryUsage;
            double memoryComplexity = Math.log(memoryGrowth) / Math.log(sizeRatio);
            
            // Store results
            growthRates.computeIfAbsent("build", k -> new ArrayList<>())
                      .add(buildComplexity);
            // ... similar for query and memory
        }
        
        // Average complexity (approximate big-O)
        double avgBuildComplexity = average(growthRates.get("build"));
        // O(1) = 0, O(n) = 1, O(n²) = 2, O(n log n) ≈ 1.3
        
        return new ScalingResult(avgBuildComplexity, ...);
    }
}
```

### Measurement Framework
```java
public class ScalabilityBenchmark {
    
    public List<SizeResult> runBenchmark(String csvPath) {
        // Read full dataset
        Graph fullGraph = readFullGraph(csvPath);
        
        double[] sizes = {0.10, 0.25, 0.50, 0.75, 1.0};
        List<SizeResult> results = new ArrayList<>();
        
        for (double size : sizes) {
            // Create subset
            Graph subset = createSubset(fullGraph, size);
            
            // Build all graph types
            Map<String, Graph> graphs = buildAllGraphTypes(subset);
            
            // Generate test queries
            List<Query> queries = generateQueries(subset, 25);
            
            // Measure each graph type
            for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
                String graphType = entry.getKey();
                Graph graph = entry.getValue();
                
                long buildTime = measureBuildTime(graph);
                long memory = measureMemory(graph);
                
                List<Long> queryTimes = new ArrayList<>();
                for (Query q : queries) {
                    long queryTime = measureDijkstraTime(graph, q);
                    queryTimes.add(queryTime);
                }
                
                results.add(new SizeResult(
                    graphType,
                    size,
                    graph.nodeCount(),
                    graph.edgeCount(),
                    buildTime,
                    average(queryTimes),
                    memory
                ));
            }
        }
        
        return results;
    }
}
```

---

## Data Analysis

### Growth Rate Analysis

1. **Calculate Complexity**:
   - For each size increase, calculate: `log(performance_ratio) / log(size_ratio)`
   - Result approximates big-O exponent
   - O(1) = 0, O(n) = 1, O(n²) = 2, O(n log n) ≈ 1.3

2. **Plot Growth Curves**:
   - X-axis: Graph size (nodes or edges)
   - Y-axis: Build time, query time, memory
   - Multiple lines (one per graph type)
   - Identify linear, quadratic, exponential patterns

3. **Identify Break-Even Points**:
   - Where curves cross (one structure becomes better)
   - Size thresholds for switching structures
   - Practical decision points

### Statistical Analysis

1. **Average Growth Rates**: Mean complexity across size increases
2. **Consistency**: Variance in measurements
3. **Outliers**: Identify anomalous scaling behavior
4. **Confidence Intervals**: Statistical significance of differences

### Comparative Analysis

1. **Relative Scaling**: Compare all structures to baseline (AdjacencyListGraph)
2. **Scalability Rankings**: Which structures scale best/worst
3. **Memory vs Speed**: Trade-offs at different sizes
4. **Practical Limits**: When each structure becomes unusable

---

## Expected Outcomes

### Scaling Characteristics (Predicted)

#### Build Time Scaling

1. **Linear Builders** (O(n)):
   - AdjacencyListGraph: Fast, linear scaling
   - DoublyLinkedListGraph: Moderate, linear
   - Expected: Build time doubles when size doubles

2. **Near-Linear Builders** (O(n log n)):
   - SortedAdjacencyListGraph: Sorting overhead
   - Expected: Slightly worse than linear

3. **Linear Builders (with overhead)**:
   - CSRGraph: Must build from existing graph
   - Expected: Linear but slower absolute time

4. **Quadratic Builders** (O(n²)):
   - MatrixGraph: Must initialize full matrix
   - Expected: Build time quadruples when size doubles

#### Query Time Scaling

1. **Linear Query Time** (O(V + E)):
   - CSRGraph: Best for sparse graphs
   - AdjacencyListGraph: Good scaling
   - Expected: Query time doubles when size doubles

2. **Quadratic Query Time** (O(V²)):
   - MatrixGraph: Must check all nodes per neighbor
   - Expected: Query time quadruples when size doubles

#### Memory Scaling

1. **Linear Memory** (O(V + E)):
   - CSRGraph, AdjacencyListGraph: Compact
   - Expected: Memory doubles when size doubles

2. **Quadratic Memory** (O(V²)):
   - MatrixGraph: Full matrix storage
   - Expected: Memory quadruples when size doubles

### Key Findings (Expected)

1. **Best for Large Graphs**: CSRGraph, OffsetArrayGraph (linear scaling)
2. **Best for Small Graphs**: AdjacencyListGraph (simpler, fast enough)
3. **Worst Scalability**: MatrixGraph (quadratic memory and query time)
4. **Break-Even Points**: 
   - Small graphs (<500 nodes): AdjacencyListGraph may be faster
   - Large graphs (>2000 nodes): CSRGraph excels
5. **Memory Limits**: MatrixGraph hits limits earliest (quadratic growth)

---

## Interpretation Guidelines

### What Success Looks Like

1. **Clear Scaling Patterns**: Observable linear/quadratic behavior
2. **Theoretical Alignment**: Results match expected big-O complexity
3. **Break-Even Identification**: Clear thresholds where structures cross
4. **Practical Relevance**: Findings applicable to real-world decisions

### Red Flags

1. **Inconsistent Scaling**: May indicate measurement issues
2. **Unexpected Patterns**: Results don't match theoretical predictions
3. **High Variance**: Unreliable measurements
4. **Memory Explosion**: Structures that become unusable at moderate sizes

### Validation

1. **Theoretical Check**: Do measured complexities match expected big-O?
2. **Reproducibility**: Consistent results across multiple runs
3. **Magnitude**: Differences should be meaningful
4. **Completeness**: All structures tested at all sizes

---

## Discussion Points

### Why This Experiment is Critical

1. **Practical Decision Making**: Know which structure to use at what size
2. **Resource Planning**: Understand memory/CPU requirements at scale
3. **Performance Prediction**: Estimate performance for larger datasets
4. **Cost Analysis**: Trade-offs between different structures

### Limitations

1. **Single Dataset**: Results may vary with different graph topologies
2. **Subset Creation**: Random sampling may not preserve graph properties
3. **Hardware Constraints**: May hit memory/CPU limits before seeing full scaling
4. **Query Characteristics**: Query performance may vary by graph structure

### Future Extensions

1. **Larger Datasets**: Test beyond current data size
2. **Different Topologies**: Scale testing on different graph types
3. **Dynamic Scaling**: Test performance with growing graphs (add nodes/edges)
4. **Multi-threaded**: Scalability in concurrent scenarios

---

## Summary

This experiment provides the **"WHEN"** - it shows when (at what sizes) different graph structures are optimal. Combined with Experiment 3 (WHY - iteration performance) and Experiment 1 (WHAT - overall performance), it completes the picture of which structure to use, why it's fast, and when it's best.

**Key Takeaway**: Different structures excel at different sizes. CSRGraph and OffsetArrayGraph scale linearly and are best for large graphs, while simpler structures like AdjacencyListGraph may be sufficient and faster for small graphs. MatrixGraph has quadratic scaling and becomes impractical at larger sizes.

**Next Steps**: Use these scalability patterns to inform structure selection based on expected graph size, and combine with Experiment 1 and 3 findings for complete understanding.


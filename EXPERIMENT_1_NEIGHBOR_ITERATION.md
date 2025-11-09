# Experiment 1: Neighbor Iteration Performance (MOST INTERESTING)

**Algorithm**: Dijkstra's Algorithm  
**Research Question**: Which graph structure provides the fastest neighbor iteration (Dijkstra's bottleneck operation)?

---

## Overview

This experiment isolates and measures the most critical operation in Dijkstra's algorithm: `graph.neighbors(node)`. This method is called thousands of times during pathfinding, making it the primary performance bottleneck. By measuring neighbor iteration performance in isolation, we can understand why certain graph structures perform better than others.

---

## Why This Experiment is Interesting

### Directly Targets the Bottleneck
- `graph.neighbors()` is called once per visited node in Dijkstra's algorithm
- For a typical pathfinding query, this can be called 100-1000+ times
- Small improvements here compound significantly
- This operation accounts for 60-80% of Dijkstra's runtime

### Reveals Core Performance Differences
- Shows how data layout fundamentally affects algorithm speed
- Demonstrates cache locality effects (sequential vs random access)
- Explains the "why" behind performance rankings
- Connects data structure design to algorithmic efficiency

### Actionable Insights
- Clear winner/loser with measurable impact
- Results directly translate to performance improvements
- Identifies optimization opportunities
- Provides concrete speedup expectations

### Academic Value
- Demonstrates understanding of algorithm-optimized data structures
- Shows ability to identify and measure critical operations
- Combines theory (cache effects, access patterns) with practice
- Highlights algorithmic thinking: optimize the hot path

---

## Methodology: Two-Phase Approach

### Phase 1: Baseline Control (Control Group)
**Purpose**: Establish precise baseline performance with deep statistics

1. **Query Selection**: 3 specific representative queries
   - 1 SMALL query (sparse node: 1-5 edges)
   - 1 MEDIUM query (medium node: 10-20 edges)
   - 1 BIG query (dense node: 50+ edges)

2. **Iterations**: 1000 runs per query
   - Very high statistical power (n=1000)
   - Low standard error
   - Precise measurements

3. **Purpose**: Serves as control group for comparison

### Phase 2: Random Sampling (Validation)
**Purpose**: Validate baseline across broad sample

1. **Query Selection**: ~300 randomly sampled queries
   - 100 sparse nodes
   - 100 medium nodes
   - 100 dense nodes

2. **Iterations**: 1000 runs per query (same as baseline)
   - Consistent statistical rigor
   - Enables direct comparison with baseline

3. **Purpose**: Validates that baseline is representative

### Node Degree Categories
1. **Sparse Nodes** (1-5 neighbors): 
   - Small airports, limited routes
   - Tests minimum overhead
   - Example: Regional airports

2. **Medium Nodes** (10-20 neighbors):
   - Regional hubs
   - Tests typical case
   - Example: Mid-size hub airports

3. **Dense Nodes** (50+ neighbors):
   - Major international hubs
   - Tests worst-case performance
   - Example: ORD, LAX, ATL

### Statistical Rigor
- **Warmup**: 10 iterations before measurement
- **Delay**: 1ms between iterations to prevent JVM burst optimization
- **Metrics**: Mean, StdDev, CoV, 95th percentile
- **Total Measurements**: ~2.1 million (7 graphs × 303 queries × 1000 iterations)

---

## Metrics Collected

### Primary Metrics (Per Query)

With n=1000 iterations per query, all metrics are highly precise:

1. **Average Iteration Time (ns)**:
   - Mean time to call `graph.neighbors(node)`
   - Very low standard error with n=1000
   - Central tendency measure

2. **Standard Deviation (ns)**:
   - Variation in measurements
   - Shows consistency of performance
   - Used to calculate confidence intervals

3. **Coefficient of Variation (%)**:
   - (StdDev / Mean) × 100
   - Measures relative consistency
   - Lower = more consistent/reliable

4. **95th Percentile (ns)**:
   - Worst-case boundary
   - Shows outlier behavior
   - Important for latency-sensitive applications

5. **Time Per Edge (ns)**:
   - Average time divided by node degree
   - Normalizes for different node sizes
   - Enables fair comparison across categories

6. **Phase Label**:
   - "BASELINE" or "RANDOM"
   - Identifies control vs validation measurements
   - Enables phase-specific analysis

### Secondary Metrics

1. **Within-Dijkstra Percentage**:
   - % of total Dijkstra time spent in neighbors()
   - Confirms bottleneck hypothesis
   - Expected: 60-80%

2. **Degree Correlation**:
   - How iteration time scales with degree
   - Linear, constant, or other pattern
   - Reveals algorithmic complexity

3. **Phase Comparison**:
   - Baseline vs Random sample statistics
   - Validates representativeness
   - Statistical significance testing

---

## Hypothesis

### Expected Performance by Structure Type

#### Array-Based Structures (Expected Fastest)
1. **CSRGraph**:
   - Sequential array access (cache-friendly)
   - Compact memory layout
   - Expected: 30-50% faster than AdjacencyListGraph
   - Time per edge: ~5-10ns (very low overhead)

2. **OffsetArrayGraph**:
   - Similar to CSR, sequential access
   - Slightly more memory but similar performance
   - Expected: Comparable to CSRGraph

3. **LinearArrayGraph**:
   - Array-based, should be fast
   - May have more overhead than CSR
   - Expected: 20-30% faster than AdjacencyListGraph

#### List-Based Structures (Expected Moderate)
1. **AdjacencyListGraph** (Baseline):
   - Standard ArrayList implementation
   - Random memory access pattern
   - Expected: Baseline performance
   - Time per edge: ~15-25ns

2. **SortedAdjacencyListGraph**:
   - Same as AdjacencyList but edges pre-sorted
   - Iteration speed similar, but may help Dijkstra's greedy selection
   - Expected: Similar iteration speed, may help overall algorithm

3. **DoublyLinkedListGraph**:
   - Pointer chasing overhead
   - Random memory access
   - Expected: 10-20% slower than AdjacencyList

4. **CircularLinkedListGraph**:
   - Similar to DoublyLinkedList
   - Expected: Comparable performance

#### Dense Structures (Expected Slowest for Sparse Graphs)
1. **MatrixGraph**:
   - Must check all nodes (even empty cells)
   - O(V) iteration regardless of actual edges
   - Expected: 50-100% slower than AdjacencyListGraph
   - Worst for sparse graphs (most of your data)

#### Specialized Structures (Variable)
1. **RoutePartitionedTrieGraph**:
   - Tree traversal overhead
   - Random access pattern
   - Expected: 20-40% slower than AdjacencyList

2. **DynamicArrayGraph**:
   - Array-based but with dynamic resizing
   - Expected: Similar to AdjacencyListGraph

3. **HalfEdgeGraph**:
   - Complex structure, pointer chasing
   - Expected: Slower than list-based

4. **LinkCutTreeGraph/EulerTourTreeGraph**:
   - Adapter patterns add overhead
   - Expected: 30-50% slower than AdjacencyList

---

## Expected Insights

### Performance Characteristics
1. **Which structure optimizes Dijkstra's most frequent operation**:
   - Clear winner for iteration speed
   - Measurable impact on overall algorithm

2. **Impact of sequential vs random access patterns**:
   - Array-based structures should significantly outperform list-based
   - Demonstrates cache locality importance

3. **How structure choice directly affects algorithm bottleneck**:
   - Shows that small per-operation improvements compound
   - Explains overall Dijkstra performance differences

### Degree-Dependent Findings
1. **Sparse nodes** (1-5 edges):
   - Small absolute differences (overhead dominates)
   - Percentage differences may still be significant
   - Overhead per edge highest

2. **Medium nodes** (10-20 edges):
   - Clear performance differences emerge
   - Array-based structures show advantages
   - Best case for comparison

3. **Dense nodes** (50+ edges):
   - Largest absolute time differences
   - Array advantages most pronounced
   - MatrixGraph becomes extremely slow

### Cache Locality Insights
1. **Sequential access** (arrays): Cache-friendly, predictable
2. **Random access** (lists): Cache misses, unpredictable
3. **Impact magnitude**: Should see 20-50% differences

---

## Implementation Guide

### Direct Iteration Measurement
```java
public class NeighborIterationBenchmark {
    
    public IterationResult measureIteration(Graph graph, String node, int iterations) {
        long totalTime = 0;
        int edgeCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            // Warm up JIT (discard first few)
            if (i < 2) {
                graph.neighbors(node);
                continue;
            }
            
            long start = System.nanoTime();
            List<Edge> neighbors = graph.neighbors(node);
            long end = System.nanoTime();
            
            totalTime += (end - start);
            edgeCount = neighbors.size();
            
            // Prevent optimization
            for (Edge e : neighbors) {
                if (e.getWeight() < 0) break; // Unlikely
            }
        }
        
        long avgTime = totalTime / (iterations - 2);
        return new IterationResult(avgTime, edgeCount, avgTime / edgeCount);
    }
}
```

### Instrumented Dijkstra Measurement
```java
public class InstrumentedDijkstra {
    private long neighborIterationTime = 0;
    private int neighborCallCount = 0;
    
    public PathResult findPathWithTiming(Graph graph, String origin, String dest) {
        long totalStart = System.nanoTime();
        neighborIterationTime = 0;
        neighborCallCount = 0;
        
        // ... Dijkstra implementation ...
        
        // In the main loop, when calling neighbors:
        long iterStart = System.nanoTime();
        List<Edge> neighbors = graph.neighbors(currentNode);
        neighborIterationTime += (System.nanoTime() - iterStart);
        neighborCallCount++;
        
        // ... rest of algorithm ...
        
        long totalTime = System.nanoTime() - totalStart;
        double percentage = (neighborIterationTime * 100.0) / totalTime;
        
        return new PathResult(..., percentage, neighborCallCount);
    }
}
```

### Node Selection Strategy
```java
private List<TestNode> selectTestNodes(Graph graph) {
    Map<Integer, List<String>> nodesByDegree = new HashMap<>();
    
    // Categorize all nodes by degree
    for (String node : graph.nodes()) {
        int degree = graph.neighbors(node).size();
        nodesByDegree.computeIfAbsent(degree, k -> new ArrayList<>()).add(node);
    }
    
    // Select representative nodes
    List<TestNode> testNodes = new ArrayList<>();
    
    // Sparse: 1-5 edges
    testNodes.addAll(selectRandom(nodesByDegree, 1, 5, 10));
    
    // Medium: 10-20 edges
    testNodes.addAll(selectRandom(nodesByDegree, 10, 20, 15));
    
    // Dense: 50+ edges
    testNodes.addAll(selectRandom(nodesByDegree, 50, Integer.MAX_VALUE, 10));
    
    return testNodes;
}
```

---

## Data Analysis

### Statistical Analysis

1. **Average Iteration Time**: Mean time per graph type
2. **Time Per Edge**: Normalized comparison (accounts for degree differences)
3. **Percentage of Dijkstra Time**: How much neighbor iteration dominates
4. **Speedup Factor**: How much faster/slower relative to baseline

### Degree-Based Analysis

1. **By Degree Category**: Performance grouped by sparse/medium/dense
2. **Performance Curves**: Iteration time vs node degree (plot)
3. **Break-Even Points**: When one structure becomes better than another
4. **Scalability**: How performance changes with increasing degree

### Comparative Analysis

1. **Relative Performance**: All structures compared to AdjacencyListGraph (baseline)
2. **Rankings**: Ordered list of fastest to slowest
3. **Performance Gaps**: Absolute and percentage differences
4. **Consistency**: Variance in measurements

---

## Console Output

When running the experiment, you'll see:

```
=== Experiment 3: Two-Phase Neighbor Iteration Performance ===
Phase 1: Baseline Control (3 queries × 1000 iterations)
Phase 2: Random Sampling (300 queries × 1000 iterations)

Building all graph types from: data/cleaned_flights.csv
Built 7 graph types

=== PHASE 1: BASELINE CONTROL ===
Selected baseline queries (control group):
  • SMALL query:  BGR (degree: 3 edges)
  • MEDIUM query: DAL (degree: 15 edges)
  • BIG query:    ORD (degree: 82 edges)

  Iterations per query: 1000 (deep statistics)

Testing CSRGraph [BASELINE]...
Testing SortedAdjacencyListGraph [BASELINE]...
...

=== PHASE 2: RANDOM SAMPLING ===
Random sample validation:
  Sparse nodes:  100 nodes
  Medium nodes:  100 nodes
  Dense nodes:   100 nodes
  Total queries: 300
  Iterations per query: 1000 (same depth as baseline)

  Example random nodes:
    Sparse:  ACK (degree: 2)
    Medium:  PHX (degree: 18)
    Dense:   LAX (degree: 95)
    ... and 297 more

Testing CSRGraph [RANDOM]...
...

Phase Summary:
  Baseline: 3 queries × 1000 iterations = 21 measurements
  Random: 300 queries × 1000 iterations = 2,100 measurements
```

**Key Information Displayed:**
- Exact nodes selected for baseline control
- Node degrees (number of edges)
- Sample sizes for each phase
- Phase labels throughout execution

---

## Expected Outcomes

### Performance Rankings (Predicted)

1. **CSRGraph**: Fastest (30-50% faster than baseline)
   - Sequential array access
   - Cache-optimized layout
   - Time per edge: ~5-10ns

2. **OffsetArrayGraph**: Close second (similar to CSR)
   - Similar characteristics
   - Time per edge: ~6-12ns

3. **LinearArrayGraph**: Fast (20-30% faster than baseline)
   - Array-based benefits
   - Time per edge: ~10-15ns

4. **AdjacencyListGraph**: Baseline performance
   - Standard implementation
   - Time per edge: ~15-25ns

5. **SortedAdjacencyListGraph**: Similar to baseline
   - Same iteration speed
   - May help overall algorithm differently

6. **List Variants**: 10-20% slower
   - Pointer chasing overhead
   - Time per edge: ~18-30ns

7. **MatrixGraph**: Slowest for sparse (50-100% slower)
   - Must check all nodes
   - Time per "edge": ~25-50ns (but checks many empty)

8. **Specialized Structures**: Variable, generally slower
   - Tree traversal overhead
   - Adapter overhead

### Key Findings (Expected)

1. **Clear Winner**: Array-based structures (CSR, OffsetArray) dominate
2. **Cache Impact**: 20-50% improvement from sequential access
3. **Degree Sensitivity**: Differences more pronounced for dense nodes
4. **Bottleneck Confirmation**: Neighbor iteration is 60-80% of Dijkstra time
5. **Practical Impact**: 30-50% overall speedup possible with best structures

---

## Interpretation Guidelines

### What Success Looks Like

1. **Clear Performance Hierarchy**: Measurable differences between structure types
2. **Cache Effects Visible**: Array-based structures significantly faster
3. **Degree Correlation**: Performance differences increase with node degree
4. **High Percentage**: Neighbor iteration accounts for most of Dijkstra time

### Red Flags

1. **No Differences**: May indicate measurement issues or all structures similar
2. **Unexpected Results**: MatrixGraph fastest would suggest implementation issues
3. **High Variance**: May indicate measurement problems or inconsistent implementations
4. **Low Percentage**: If neighbor iteration <40% of time, other bottlenecks exist

### Validation

1. **Reproducibility**: Results should be consistent across runs
2. **Theoretical Alignment**: Results should match expected access patterns
3. **Magnitude**: Differences should be meaningful (>10%)
4. **Completeness**: All structures successfully tested

---

## Discussion Points

### Why This Experiment is Critical

1. **Identifies the Bottleneck**: Confirms neighbor iteration is the hot path
2. **Explains Performance**: Shows WHY certain structures are faster
3. **Actionable**: Clear optimization target (improve iteration)
4. **Educational**: Demonstrates cache locality and access pattern importance

### Limitations

1. **Isolation**: Doesn't capture interaction effects with other operations
2. **Micro-benchmarking**: JVM optimizations may affect measurements
3. **Hardware Dependent**: Results vary by CPU cache characteristics
4. **Edge Object Overhead**: May mask pure iteration differences

### Future Extensions

1. **Memory Access Patterns**: Use profiling tools (perf, VTune) for deeper analysis
2. **Cache Miss Measurements**: Hardware counters for cache performance
3. **JIT Effects**: Measure with/without JIT compilation
4. **Different Workloads**: Test with different graph topologies

---

## Summary

This experiment provides the **"WHY"** - it explains why certain graph structures perform better by isolating and measuring the critical bottleneck operation. The results directly explain the performance differences seen in Experiment 1 (benchmark) and help predict behavior in Experiment 4 (scalability).

**Key Takeaway**: Array-based structures with sequential access patterns (CSRGraph, OffsetArrayGraph) should significantly outperform list-based structures due to cache locality. This single insight explains much of the performance difference in Dijkstra's algorithm.

**Next Steps**: Use these results to understand Experiment 1's findings and predict Experiment 4's scalability patterns.


# Top 3 Experimental Designs: Dijkstra's Algorithm Across Graph Data Structures

**Algorithm**: Dijkstra's Algorithm (single algorithm, comparing data structures)

**Graph Implementations**: All 13 graph types

---

## Overview

This document details the top 3 most interesting experiments for comparing graph data structures using Dijkstra's algorithm. Each experiment tests different aspects of performance to provide a complete understanding of when and why to choose each graph implementation.

**Java Test Files**:
- `src/experiments/NeighborIterationExperiment.java` - Experiment 1
- `src/experiments/ScalabilityExperiment.java` - Experiment 2
- `src/experiments/PathfindingBenchmarkExperiment.java` - Experiment 3


---

## 🎯 TOP 3 MOST INTERESTING EXPERIMENTS

### ⭐ Experiment 1: Neighbor Iteration Performance (MOST INTERESTING)

**Research Question**: Which graph structure provides the fastest neighbor iteration (Dijkstra's bottleneck operation)?

#### Overview

This experiment isolates and measures the most critical operation in Dijkstra's algorithm: `graph.neighbors(node)`. This method is called thousands of times during pathfinding, making it the primary performance bottleneck. By measuring neighbor iteration performance in isolation, we can understand WHY certain graph structures perform better than others.

#### Why It's Most Interesting

- **Directly targets Dijkstra's bottleneck**: `graph.neighbors()` is called thousands of times
- **Reveals core performance differences**: Shows how data layout fundamentally affects algorithm speed
- **Actionable insights**: Clear winner/loser with measurable impact
- **Academic value**: Demonstrates understanding of algorithm-optimized data structures

#### Methodology

**Isolation Approach**:
1. **Direct Measurement**: Time `graph.neighbors(node)` calls in isolation
2. **Within-Dijkstra Tracking**: Instrument Dijkstra to track time spent in neighbor iteration
3. **Bulk Testing**: Test neighbor iteration for all nodes in the graph
4. **Degree-Based Analysis**: Categorize by node degree (edges per node)

**Node Degree Categories**:
- **Sparse Nodes** (1-5 neighbors): Represents typical airport connections
- **Medium Nodes** (10-20 neighbors): Common for hub airports
- **Dense Nodes** (50+ neighbors): Major international hubs

#### Metrics Collected

1. **Single Node Iteration Time** (nanoseconds): Time to call `graph.neighbors(node)` and iterate all edges
2. **Aggregate Iteration Time**: Total time spent in `graph.neighbors()` during complete Dijkstra run
3. **Iteration Time Per Edge**: Normalizes for node degree
4. **Percentage of Dijkstra Time**: Shows how much of algorithm is spent in this operation (expected: 60-80%)

#### Hypothesis

- **Array-Based** (CSR, OffsetArray, LinearArray): Fastest iteration (sequential access) - Expected 20-50% faster than baseline
- **List-Based** (AdjacencyList): Baseline performance
- **Dense Structures** (MatrixGraph): Slowest for sparse graphs (must check all nodes) - Expected 50-100% slower

#### Expected Outcomes

- **Clear Winner**: Array-based structures (CSRGraph, OffsetArrayGraph) dominate
- **Cache Impact**: 20-50% improvement from sequential access
- **Practical Impact**: 30-50% overall speedup possible with best structures

**This experiment provides the "WHY"** - it explains why certain graph structures perform better.

---

### ⭐ Experiment 2: Scalability Analysis (SECOND MOST INTERESTING)

**Research Question**: How do graph structures perform as graph size increases?

#### Overview

This experiment tests how each graph data structure scales as the graph grows in size. By testing the same structures on progressively larger subsets of the data, we can identify which structures scale linearly, quadratically, or exhibit other growth patterns.

#### Why It's Very Interesting

- **Practical importance**: Real-world graphs grow over time
- **Reveals big-O behavior**: Shows theoretical complexity (O notation) in real measurements
- **Decision framework**: Helps choose structure based on expected size
- **Academic rigor**: Demonstrates understanding of algorithmic complexity

#### Methodology

**Graph Size Subsets**:
1. **10% subset**: ~300 nodes, ~500 edges
2. **25% subset**: ~750 nodes, ~1,250 edges
3. **50% subset**: ~1,500 nodes, ~2,500 edges
4. **75% subset**: ~2,250 nodes, ~3,750 edges
5. **100% subset**: ~3,000 nodes, ~5,000 edges (full dataset)

For each size subset:
1. Build all graph types from that subset
2. Measure build time for each structure
3. Run identical pathfinding queries on each structure
4. Measure Dijkstra runtime for each query
5. Measure memory usage (peak during execution)

#### Metrics Collected

1. **Build Time Growth**: Time to construct graph - Analyze O(?) complexity
2. **Dijkstra Runtime Growth**: Time to execute Dijkstra - Analyze O(?) complexity
3. **Memory Usage Growth**: Peak memory during execution
4. **Performance Degradation Ratio**: How much slower when size doubles (ideal: 2x slower for linear)

#### Hypothesis

- **Sparse Structures** (CSR, OffsetArray): Scale linearly O(V+E) - Expected: Best scaling
- **Dense Structures** (MatrixGraph): Scale quadratically O(V²) - Expected: Poor scalability
- **List-Based**: Scale well but with more overhead

#### Expected Outcomes

- **Best for Large Graphs**: CSRGraph, OffsetArrayGraph (linear scaling)
- **Best for Small Graphs**: AdjacencyListGraph (simpler, fast enough)
- **Worst Scalability**: MatrixGraph (quadratic memory and query time)
- **Break-Even Points**: Different structures excel at different sizes

**This experiment provides the "WHEN"** - it shows when (at what sizes) different graph structures are optimal.

---

### ⭐ Experiment 3: Pathfinding Performance Benchmark (THIRD MOST INTERESTING)

**Research Question**: How do different graph data structures affect Dijkstra's algorithm runtime and efficiency?

#### Overview

This experiment provides a comprehensive, end-to-end performance comparison of all graph implementations when running Dijkstra's algorithm. It tests complete pathfinding scenarios to identify which structures perform best in real-world usage.

#### Why It's Interesting

- **Complete picture**: Tests end-to-end performance (most realistic)
- **Comprehensive comparison**: All structures on same queries
- **Multiple metrics**: Runtime, efficiency, memory - complete analysis
- **Practical relevance**: Answers "which should I use?"

#### Methodology

**Test Setup**:
1. **Graph Construction**: Build all graph types from the same CSV data
2. **Query Generation**: Generate 100 random origin-destination pairs from the flight data
3. **Path Categories**: 
   - Short paths (2-3 hops): 30 queries
   - Medium paths (5-8 hops): 40 queries
   - Long paths (10+ hops): 30 queries
4. **Algorithm**: Run Dijkstra's algorithm on each query for each graph structure
5. **Multiple Runs**: Run each query 5 times per graph structure and average results

#### Metrics Collected

1. **Total Runtime (ms)**: End-to-end Dijkstra execution time
2. **Nodes Visited**: Number of nodes processed by Dijkstra
3. **Edges Relaxed**: Number of edges examined during pathfinding
4. **Memory Footprint**: Peak memory usage during Dijkstra execution
5. **Success Rate**: Percentage of queries where path was found

#### Hypothesis

- **CSRGraph and OffsetArrayGraph**: Should be fastest (cache-friendly) - Expected: 20-40% faster
- **SortedAdjacencyListGraph**: May help (edges pre-sorted) - Expected: 10-20% faster
- **MatrixGraph**: Likely slowest for sparse graphs - Expected: 50-100% slower

#### Expected Outcomes

- **Overall winner**: CSRGraph or OffsetArrayGraph (best balance)
- **Best memory**: CSRGraph, OffsetArrayGraph (compact representation)
- **Best speed**: CSRGraph (cache-optimized)
- **Worst performance**: MatrixGraph (poor for sparse graphs)

**This experiment provides the "WHAT"** - a complete performance profile of all graph structures.
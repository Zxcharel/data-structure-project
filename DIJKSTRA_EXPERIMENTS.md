# Top 3 Experimental Designs: Dijkstra's Algorithm Across Graph Data Structures

**Algorithm**: Dijkstra's Algorithm (single algorithm, comparing data structures)

**Graph Implementations**: All 13 graph types

---

## Overview

This document summarizes the top 3 most interesting experiments for comparing graph data structures using Dijkstra's algorithm. Each experiment has been detailed in a separate document for comprehensive analysis.

---

## 🎯 TOP 3 MOST INTERESTING EXPERIMENTS

### ⭐ Experiment 3: Neighbor Iteration Performance (MOST INTERESTING)

**Document**: [`EXPERIMENT_3_NEIGHBOR_ITERATION.md`](EXPERIMENT_3_NEIGHBOR_ITERATION.md)

**Why it's the most interesting**:
- **Directly targets Dijkstra's bottleneck**: `graph.neighbors()` is called thousands of times
- **Reveals core performance differences**: Shows how data layout fundamentally affects algorithm speed
- **Actionable insights**: Clear winner/loser with measurable impact
- **Academic value**: Demonstrates understanding of algorithm-optimized data structures

**Key Metrics**:
- Time to iterate neighbors for single node
- Aggregate iteration time across entire Dijkstra run
- Percentage of Dijkstra time spent in neighbor iteration
- Iteration speed vs node degree (edges per node)

**Expected Findings**:
- CSRGraph/OffsetArrayGraph: 20-40% faster iteration than ArrayList
- MatrixGraph: Slow for sparse graphs (checks empty cells)
- SortedAdjacencyListGraph: May help if Dijkstra benefits from sorted edges

---

### ⭐ Experiment 4: Scalability Analysis (SECOND MOST INTERESTING)

**Document**: [`EXPERIMENT_4_SCALABILITY.md`](EXPERIMENT_4_SCALABILITY.md)

**Why it's very interesting**:
- **Practical importance**: Real-world graphs grow over time
- **Reveals big-O behavior**: Shows theoretical complexity in practice
- **Decision framework**: Helps choose structure based on expected size
- **Academic rigor**: Demonstrates understanding of algorithmic complexity

**Key Metrics**:
- Build time growth (O(?))
- Dijkstra runtime growth (O(?))
- Memory usage growth (O(?))
- Performance degradation ratio as size doubles

**Expected Findings**:
- CSRGraph: Linear scaling, best for large sparse graphs
- MatrixGraph: Quadratic memory, poor scalability
- Different structures excel at different size ranges
- Break-even points where one structure becomes better

---

### ⭐ Experiment 1: Pathfinding Performance Benchmark (THIRD MOST INTERESTING)

**Document**: [`EXPERIMENT_1_PATHFINDING_BENCHMARK.md`](EXPERIMENT_1_PATHFINDING_BENCHMARK.md)

**Why it's interesting**:
- **Complete picture**: Tests end-to-end performance (most realistic)
- **Comprehensive comparison**: All structures on same queries
- **Multiple metrics**: Runtime, efficiency, memory - complete analysis
- **Practical relevance**: Answers "which should I use?"

**Key Metrics**:
- Total runtime per query
- Nodes visited (algorithm efficiency)
- Edges relaxed (edge processing overhead)
- Memory footprint during execution
- Success rate (path found vs not found)

**Expected Findings**:
- Overall winner for your use case
- Which structure is best for different query types
- Complete performance profile
- Clear recommendations

---

## Comparison of Top 3

| Aspect | Exp 3 (Neighbor Iteration) | Exp 4 (Scalability) | Exp 1 (Benchmark) |
|--------|---------------------------|---------------------|-------------------|
| **Academic Value** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Practical Value** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Uniqueness** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Insight Depth** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Actionability** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## Recommendation

**Focus on Experiments 3, 4, and 1** in that order:
1. **Experiment 3** reveals the WHY (iteration performance)
2. **Experiment 4** shows the WHEN (scalability characteristics)  
3. **Experiment 1** provides the WHAT (overall performance)

Together, they tell a complete story: which structure, why it's fast, and when to use it.

---

## Experiment Documents

- **[Experiment 1: Pathfinding Performance Benchmark](EXPERIMENT_1_PATHFINDING_BENCHMARK.md)** - Complete end-to-end performance comparison
- **[Experiment 3: Neighbor Iteration Performance](EXPERIMENT_3_NEIGHBOR_ITERATION.md)** - Isolates Dijkstra's critical bottleneck operation
- **[Experiment 4: Scalability Analysis](EXPERIMENT_4_SCALABILITY.md)** - Performance across different graph sizes


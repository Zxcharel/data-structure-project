# Interesting Graph Experiments

This document describes the interesting experiments you can run to explore counter-intuitive performance behaviors in graph data structures and algorithms.

## Available Experiments

### 1. When Pre-Sorting Hurts Performance (Sorted vs Unsorted Edges)

**Location:** Menu option 8

**Hypothesis:** Pre-sorting adjacency lists by edge weight should help Dijkstra's algorithm by accessing cheaper edges first. However, the overhead of maintaining sorted order might outweigh any benefits.

**What We Test:**
- `AdjacencyListGraph` (unsorted edges) vs `SortedAdjacencyListGraph` (sorted by weight)
- Same queries run on both structures using Dijkstra's algorithm
- Measures: runtime, edges relaxed, nodes visited

**Key Question:** Does the O(log n) insertion cost per edge in sorted lists pay off, or does Dijkstra's priority queue already provide sufficient ordering?

**Insights:**
- Expected result: Sorted edges may actually be *slower* due to insertion overhead
- Demonstrates that algorithmic improvements must be profiled
- Shows that constant factors and hidden costs matter in practice

---

### 2. When Memory Layout Beats Algorithm Optimization (CSR vs Adjacency Lists)

**Location:** Menu option 9

**Hypothesis:** CSR (Compressed Sparse Row) graphs use cache-friendly contiguous arrays while adjacency lists use pointer-based structures. For pathfinding algorithms that scan edges sequentially, better cache locality may overcome any algorithmic overhead.

**What We Test:**
- `AdjacencyListGraph` (pointer-based) vs `CSRGraph` (contiguous arrays)
- Same queries run on both structures using Dijkstra's algorithm
- Measures: runtime, edges relaxed, cache performance

**Key Question:** Does sequential memory access beat pointer chasing in real-world scenarios?

**Insights:**
- Modern CPUs have deep memory hierarchies (L1/L2/L3 caches)
- Spatial locality can be more important than algorithmic complexity
- Demonstrates that memory access patterns matter for performance

---

## How to Run Experiments

### From the Main Menu

1. Start the program: `java -cp out src.Main`
2. Choose menu option 8 or 9
3. Provide CSV path (or press Enter for default: `data/cleaned_flights.csv`)
4. Specify number of queries (or press Enter for default: 50)
5. Choose output directory (or press Enter for defaults)

### Output Files

Each experiment generates:
- **CSV file**: Raw performance data for all queries
- **README.md**: Formatted report with:
  - Summary statistics table
  - Performance analysis with speedup calculations
  - Key insights and conclusions
  - Visual comparisons

### Example Output

```
# Experiment: Sorted Edges vs Unsorted Edges

## Results
| Graph Type | Avg Runtime (ms) | Speedup |
|------------|------------------|---------|
| Unsorted   | 2.45             | 1.15x   |
| Sorted     | 2.83             | 1.00x   |

## Analysis
⚠️ Sorted edges are 15% SLOWER than unsorted
- The overhead of maintaining sorted order exceeds any benefits
- Dijkstra's priority queue already provides sufficient edge ordering
```

---

## Why These Experiments Matter

These experiments demonstrate important principles:

1. **Empirical validation is essential**: What seems better on paper may not work in practice
2. **Constant factors matter**: O(n log n) with good constants beats O(n) with poor locality
3. **Profiling reveals truth**: Always measure before optimizing
4. **Context matters**: The best structure depends on workload characteristics
5. **Modern hardware matters**: Cache effects can dominate computational complexity

---

## Extending the Experiments

You can easily modify the experiments to test different hypotheses:

### Adding More Graph Types
Edit `src/experiments/ExperimentRunner.java` to include:
- `MatrixGraph` for dense graph comparisons
- `OffsetArrayGraph` for another cache-friendly layout
- `DoublyLinkedListGraph` for different pointer overhead

### Testing Different Algorithms
Current experiments use Dijkstra. You could add:
- A* with different heuristics
- BFS for unweighted paths
- Bidirectional search

### Varying Query Patterns
Current experiments use random queries. You could test:
- Short paths (1-2 hops) vs long paths (5+ hops)
- Dense regions vs sparse regions
- High-degree nodes vs low-degree nodes

---

## References

These experiments are inspired by real-world performance analysis patterns:

- **"When the Faster Heap Isn't Always Faster"**: Sorted vs unsorted data structures
- **"Memory Matters More Than Algorithms"**: Cache-friendly layouts in high-performance computing
- **"Measure Twice, Optimize Once"**: The importance of profiling

---

## License

Part of the data structure project demonstrating practical performance analysis techniques.




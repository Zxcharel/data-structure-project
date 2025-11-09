# Best Airline Path Finder

A Java application that finds optimal airline routes based on review ratings using graph algorithms.

## Features

- **Route finding with ratings**: Builds a directed, weighted graph from airline reviews and runs Dijkstra/A* to find best routes.
- **Seven core graph structures**: AdjacencyList, SortedAdjacencyList, CSR, OffsetArray, Matrix, RoutePartitionedTrie, HalfEdge (plus adapters for Link-Cut / Euler Tour).
- **Configurable constraints**: Limit max stops, allow/block specific airlines during queries.
- **Comprehensive experiments**: Five automated benchmarks covering neighbor iteration, scalability, autocomplete, cache locality, and full pathfinding.
- **Graph analytics & reports**: Centrality, airline performance, route stats, and auto-generated markdown + CSV summaries.
- **Menu-driven console**: Build graphs, run queries, execute experiments, and export analyses from one CLI.

## Weight Calculation

The system converts airline ratings (1-5 scale) to edge weights using the formula:
- Weight = 6 - Rating (so 5→1, 4→2, etc.)
- Combined weight = (Overall × 0.4) + (ValueForMoney × 0.2) + (InflightEntertainment × 0.1) + (CabinStaff × 0.1) + (SeatComfort × 0.2)
- Missing ratings are handled by dropping them and re-normalising weights

## Compilation and Running

```bash
# Compile all Java files
javac -d out $(find src -name "*.java")

# Run the application
java -cp out src.Main
```

## Usage

1. **Build graph from CSV** – load the cleaned flight dataset into your chosen graph implementation.
2. **Query best route** – run Dijkstra with optional constraints between any two airports.
3. **Run experiments** – execute any of the five benchmark suites (or all of them in sequence).
4. **Graph analysis** – print on-the-spot structural stats, airline breakdowns, and centrality leaders.
5. **Generate analysis report** – export the full markdown + CSV report bundle to disk.
6. **Exit** – quit the application.

## Experiments Overview

| # | Name | What it measures | Key takeaway |
|---|------|------------------|--------------|
| 1 | Neighbor Iteration Performance | Micro-benchmark of `graph.neighbors(node)` across seven structures (baseline + random phases, ns resolution) | Highlights cache locality + constant-factor wins that make CSR/OffsetArray fast. |
| 2 | Scalability Analysis (“Graph Size Deception”) | End-to-end Dijkstra runs on 10% → 100% graph sizes, tracking runtime, build cost, memory | Shows when each structure breaks down as the dataset grows. |
| 3 | Prefix Autocomplete | Trie vs array-based autocomplete throughput and latency (including user-input simulation) | Demonstrates when specialized structures like the RoutePartitionedTrie win. |
| 4 | Cache Locality | CSR vs list-based adjacency under controlled neighbor access patterns | Quantifies cache effects and validates Experiment 1 findings on a controlled workload. |
| 5 | Pathfinding Benchmark | Full Dijkstra benchmark with 200 queries × 50 runs, collecting runtime, nodes, edges, memory | Publication-grade comparison of all structures under realistic queries. |

Each experiment writes a CSV + markdown summary to `out/experiments/<name>/…`.

## CSV Format

The CSV file contain columns for:
- `airline` 
- `origin` (origin airport code)
- `destination` (destination airport code)  
- `overall_rating` (1-5 scale)
- `value_for_money` (1-5 scale)
- `inflight_entertainment` (1-5 scale)
- `cabin_staff` (1-5 scale)
- `seat_comfort` (1-5 scale)


## Project Structure

```
src/
├── Main.java                 # Console interface
├── data/                     # Data model classes
│   ├── CsvReader.java
│   ├── FlightRecord.java
│   └── RouteAggregate.java
├── graph/                    # Graph implementations
│   ├── Graph.java            # Graph interface
│   ├── Edge.java             # Edge representation
│   ├── AdjacencyListGraph.java
│   ├── SortedAdjacencyListGraph.java
│   ├── MatrixGraph.java
│   ├── CSRGraph.java
│   ├── OffsetArrayGraph.java
│   ├── RoutePartitionedTrieGraph.java
│   └── HalfEdgeGraph.java
├── algo/                     # Pathfinding algorithms
│   ├── PathResult.java
│   ├── Constraints.java
│   ├── Dijkstra.java
│   └── AStar.java
├── comparison/               # Data structure comparison
│   └── DataStructureComparator.java
├── experiments/              # Benchmark suites (NeighborIteration, Scaling, CacheLocality, etc.)
├── analysis/                 # Graph analysis tools
│   ├── GraphAnalyzer.java
│   ├── CentralityMetrics.java
│   └── ...
└── util/                     # Utility classes
    ├── Stopwatch.java
    └── IOUtils.java
```

## Graph Data Structures

Seven core implementations ship with the experiments (plus optional adapters for Link-Cut / Euler Tour trees):

### AdjacencyListGraph
- HashMap → ArrayList adjacency list; cheap to mutate, great baseline.
- `O(1)` node lookups, amortised `O(1)` edge inserts, `O(deg(u))` scans.

### SortedAdjacencyListGraph
- Edges kept sorted by weight (binary-search insert).
- Same asymptotic costs as adjacency list, but deterministic order + faster weight-based queries.

### CSRGraph (Compressed Sparse Row)
- Built once from another graph; stores destinations/weights in contiguous arrays.
- Zero-copy neighbor views and stellar cache locality; dynamic updates require rebuild.

### OffsetArrayGraph
- Builder-friendly variant of CSR: collect edges in lists, then “finalize” into a contiguous `Edge[]` with offsets.
- Behaves like CSR after finalisation but accepts incremental inserts beforehand.

### MatrixGraph
- Dense `V × V` matrix of weights/airlines.
- Constant-time lookups, but quadratic space and Dijkstra degrades to `O(V²)` relaxations.

### RoutePartitionedTrieGraph
- Each origin owns a trie that indexes destinations by prefix while keeping a flat neighbor list for fast iteration.
- Powers the autocomplete experiment and prefix filters.

### HalfEdgeGraph
- Minimal half-edge lists per origin (doubly linked). Models pointer-rich structures common in meshes / planar graphs.
- Useful contrast for pointer-chasing vs array-based traversal.

## Complexity Cheat Sheet (Core Structures)

| Structure | Edge Insert | Edge Delete | Lookup `u→v` | Space | Dijkstra (binary heap) | Notes |
|-----------|-------------|-------------|--------------|-------|------------------------|-------|
| Adjacency List | `O(1)` amortised | `O(deg(u))` | `O(deg(u))` | `O(V + E)` | `O((V + E) log V)` | Flexible, fully mutable baseline. |
| Adjacency Matrix | `O(1)` | `O(1)` | `O(1)` | `O(V²)` | `O(V²)` | Only worthwhile for dense graphs. |
| CSR | Rebuild (`O(E)`) | Rebuild (`O(E)`) | `O(deg(u))` (or `O(log deg)` if sorted) | `O(V + E)` | `O((V + E) log V)` with tiny constants | Best read performance; treat as immutable. |
| RoutePartitionedTrie | `O(L)` | `O(L)` | `O(L)` | `O(V + E·L)` | `O((V + E) log V)` | Supports prefix queries; `L` = destination code length. |

## Testing and Comparison

### Compare All Graph Types

```java
import src.comparison.DataStructureComparator;
import src.graph.*;

DataStructureComparator comp = new DataStructureComparator();
List<String> nodes = graph.nodes();

// Compare all 4 graph types
FullGraphComparisonResult result = 
    comp.compareAllGraphImplementations(nodes, graph.edgeCount());
System.out.println(result);
```

### Compare Three Graph Types (without sorted)

```java
// Compare AdjList, Matrix, and CSR
ExtendedGraphComparisonResult result = 
    comp.compareGraphImplementationsWithCSR(nodes, edgeCount);
```

### Memory Comparison

```java
// Compare memory usage of all graph types
ExtendedMemoryComparisonResult memResult = 
    comp.compareMemoryUsageWithCSR(nodes, edgeCount);
System.out.println(memResult);
```


## Example Output

### Pathfinding Result
```
=== Results ===
Algorithm: Dijkstra
Route found: heathrow → auckland → london → jfk → vie
Airlines: [china-southern-airlines, china-southern-airlines, virgin-atlantic-airways, austrian-airlines]
Total weight: 5.000
Path length: 4 hops
Nodes visited: 120
Edges relaxed: 1130
Runtime: 2 ms

=== Route Visualization ===
heathrow --(china-southern-airlines)--> auckland --(china-southern-airlines)--> london --(virgin-atlantic-airways)--> jfk --(austrian-airlines)--> vie
```

### Graph Comparison Result
```
Graph Implementation Comparison (All 4 Types):
AdjacencyList: 15ms build, 3ms neighbor lookup, 1ms node check
SortedAdjList: 23ms build, 3ms neighbor lookup, 1ms node check
Matrix: 23ms build, 5ms neighbor lookup, 2ms node check
CSR: 18ms build, 2ms neighbor lookup, 1ms node check
Memory: AdjList ~220000 bytes, SortedAdjList ~220000 bytes, Matrix 5408000 bytes, CSR 85234 bytes
```


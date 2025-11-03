# Best Airline Path Finder

A Java application that finds optimal airline routes based on review ratings using graph algorithms.

## Features

- **Graph-based route finding**: Builds a directed graph from flight data with countries as nodes and airline routes as edges
- **Multiple graph implementations**: 
  - AdjacencyListGraph (standard)
  - SortedAdjacencyListGraph (edges sorted by weight)
  - MatrixGraph (dense matrix representation)
  - CSRGraph (compressed sparse row, memory-efficient)
- **Algorithm**: Implements Dijkstra's algorithm for pathfinding
- **Rating-based weights**: Converts airline ratings to edge weights using a weighted formula
- **Constraint support**: Filter routes by maximum stops, airline allowlist/blocklist
- **Data structure comparisons**: Automated performance and memory comparison between graph implementations
- **Experiments**: Three comprehensive Dijkstra experiments comparing graph data structures
- **Console interface**: Simple menu-driven user interface

## Weight Calculation

The system converts airline ratings (1-5 scale) to edge weights using the formula:
- Weight = 6 - Rating (so 5→1, 4→2, etc.)
- Combined weight = (Overall × 0.4) + (ValueForMoney × 0.2) + (InflightEntertainment × 0.1) + (CabinStaff × 0.1) + (SeatComfort × 0.2)
- Missing ratings are handled by dropping them and re-normalizing weights

## Compilation and Running

```bash
# Compile all Java files
javac -d out $(find src -name "*.java")

# Run the application
java -cp out src.Main
```

## Usage

1. **Build graph from CSV**: Load flight data from a CSV file
2. **Query best route**: Find optimal routes between countries
3. **Run Dijkstra experiments**: Compare graph structure performance (3 experiments: Neighbor Iteration, Scalability, Pathfinding Benchmark)
4. **Graph analysis**: Analyze graph structure and statistics
5. **Data structure comparison**: Compare different graph implementations (AdjList, SortedAdjList, Matrix, CSR)
6. **Generate analysis report**: Create comprehensive analysis reports
7. **Exit**: Quit the application

## CSV Format

The CSV file should contain columns for:
- `airline` (or similar)
- `origin` (origin country)
- `destination` (destination country)  
- `overall_rating` (1-5 scale)
- `value_for_money` (1-5 scale)
- `inflight_entertainment` (1-5 scale)
- `cabin_staff` (1-5 scale)
- `seat_comfort` (1-5 scale)

Column names are case-insensitive and partial matches are supported.

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
│   ├── AdjacencyListGraph.java         # Standard adjacency list
│   ├── SortedAdjacencyListGraph.java   # Sorted by weight
│   ├── MatrixGraph.java                # Dense matrix representation
│   └── CSRGraph.java                   # Compressed sparse row
├── algo/                     # Pathfinding algorithms
│   ├── PathResult.java
│   ├── Constraints.java
│   └── Dijkstra.java
├── comparison/               # Data structure comparison
│   └── DataStructureComparator.java
├── experiments/              # Dijkstra performance testing
│   ├── NeighborIterationExperiment.java
│   ├── ScalabilityExperiment.java
│   └── PathfindingBenchmarkExperiment.java
├── analysis/                 # Graph analysis tools
│   ├── GraphAnalyzer.java
│   ├── CentralityMetrics.java
│   └── ...
└── util/                     # Utility classes
    ├── Stopwatch.java
    └── IOUtils.java
```

## Graph Data Structures

The project implements 13 graph data structures for comparison, including:

### AdjacencyListGraph
- Standard adjacency list using HashMap and ArrayList
- O(1) node lookup, O(1) edge insertion
- Flexible and easy to use

### SortedAdjacencyListGraph
- Adjacency list with edges sorted by weight (ascending)
- O(log n) edge insertion (maintains sorted order)
- Useful for algorithms that benefit from pre-sorted edges
- Supports binary search for weight-based queries

### MatrixGraph
- Dense matrix representation using 2D arrays
- O(1) edge weight lookup
- O(V²) memory - best for dense graphs

### CSRGraph (Compressed Sparse Row)
- Memory-efficient representation using three arrays
- O(V + E) memory - optimal for sparse graphs
- Cache-friendly sequential access
- Industry-standard format used in high-performance libraries

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

See `DIJKSTRA_EXPERIMENTS.md` for detailed experiment documentation.

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

## Requirements

- Java 21 or higher
- No external dependencies (plain Java only)
- CSV file with flight data

## License

This project is provided as-is for educational purposes.

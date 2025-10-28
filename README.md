# Best Airline Path Finder

A Java application that finds optimal airline routes based on review ratings using graph algorithms.

## Features

- **Graph-based route finding**: Builds a directed graph from flight data with countries as nodes and airline routes as edges
- **Multiple algorithms**: Implements Dijkstra's algorithm and A* with different heuristics
- **Rating-based weights**: Converts airline ratings to edge weights using a weighted formula
- **Constraint support**: Filter routes by maximum stops, airline allowlist/blocklist
- **Experiments**: Automated performance comparison between algorithms
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
java -cp out Main
```

## Usage

1. **Build graph from CSV**: Load flight data from a CSV file
2. **Query best route**: Find optimal routes between countries
3. **Run experiments**: Compare algorithm performance automatically
4. **Exit**: Quit the application

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
├── graph/                    # Graph implementation
│   ├── Graph.java
│   ├── Edge.java
│   └── AdjacencyListGraph.java
├── algo/                     # Pathfinding algorithms
│   ├── PathResult.java
│   ├── Constraints.java
│   ├── Dijkstra.java
│   └── AStar.java
├── experiments/              # Performance testing
│   └── ExperimentRunner.java
└── util/                     # Utility classes
    ├── Stopwatch.java
    └── IOUtils.java
```

## Example Output

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

## Requirements

- Java 21 or higher
- No external dependencies (plain Java only)
- CSV file with flight data

## License

This project is provided as-is for educational purposes.

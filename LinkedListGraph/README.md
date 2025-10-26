# Flight Graph Implementation - LinkedList Version

This project implements a graph data structure using LinkedList to represent flight routes between airports, populated from CSV data.

## Project Structure

- `Edge.java` - Represents a flight route between two airports
- `FlightGraph.java` - Main graph class using LinkedList implementation
- `CSVParser.java` - Parses CSV files and populates the graph
- `LinkedListGraphDemo.java` - Main program to test the LinkedList implementation
- `README.md` - This documentation file

## Graph Representation (LinkedList Implementation)

### Data Structure
- **AirportNode**: Inner class containing airport code and LinkedList of outgoing flights
- **Main Graph**: LinkedList of AirportNode objects
- **Edges**: LinkedList of Edge objects within each AirportNode

### Vertices (Nodes)
- Represent airports (identified by airport codes)
- Stored as AirportNode objects in a LinkedList
- Each AirportNode contains a LinkedList of outgoing flights

### Edges
- Represent direct flight routes between airports
- Stored as LinkedList within each AirportNode
- Each edge stores:
  - Destination airport code
  - Airline name
  - Weight rating (calculated from multiple factors)

## Weight Calculation

### Rating-to-Weight Mapping
Higher ratings correspond to lower weights (better airlines = smaller weight):
- Rating: 5 = Weight: 1
- Rating: 4 = Weight: 2
- Rating: 3 = Weight: 3
- Rating: 2 = Weight: 4
- Rating: 1 = Weight: 5

Formula: `Weight = 6 - Rating`

### Combined Weight Calculation
Each airline's total edge weight is calculated as a weighted average:

```
Weight Rating = (Overall_rating_weight × 0.4)
              + (Value_for_money_weight × 0.2)
              + (Inflight_entertainment_weight × 0.1)
              + (Cabin_staff_weight × 0.1)
              + (Seat_comfort_weight × 0.2)
```

## How to Run

### Quick Start
```bash
# Compile all classes
javac *.java

# Run the LinkedList implementation
java LinkedListGraphDemo
```

### Prerequisites
- Java 11 or higher
- CSV file with flight data (path: `../src/cleaned_flights.csv`)

## LinkedList vs HashMap Comparison

### LinkedList Implementation
**Advantages:**
- Memory efficient for sparse graphs
- Good for sequential traversal
- Simple implementation
- O(1) insertion at end
- No hash collisions

**Disadvantages:**
- O(n) search time for airports
- O(n) access time for specific airports
- Slower for random access patterns

### HashMap Implementation
**Advantages:**
- O(1) average search time
- Fast random access
- Efficient for frequent lookups
- Better for dense graphs

**Disadvantages:**
- Higher memory overhead
- Hash collision handling
- More complex implementation

## Graph Operations

- `addAirport(String airportCode)` - Add an airport
- `addFlight(...)` - Add a flight route
- `addFlightWithRatings(...)` - Add flight with automatic weight calculation
- `getFlightsFrom(String airport)` - Get outgoing flights
- `getFlightsTo(String airport)` - Get incoming flights
- `getBestFlight(String from, String to)` - Find best direct route
- `getAirlinesFrom(String airport)` - Get airlines from airport
- `getBusiestAirport()` - Find airport with most flights
- `getAirportsByFlightCount()` - Get airports sorted by flight count
- `printGraphStats()` - Display statistics
- `printGraph()` - Display full graph structure

## Performance Characteristics

### Time Complexity
- **Add Airport**: O(1) - insertion at end of LinkedList
- **Add Flight**: O(n) - need to find source airport
- **Get Flights From**: O(n) - need to find airport, then O(1) to get flights
- **Search Airport**: O(n) - linear search through LinkedList
- **Get All Airports**: O(n) - traverse entire LinkedList

### Space Complexity
- **Overall**: O(V + E) where V = vertices (airports), E = edges (flights)
- **Per Airport**: O(1) + O(flights_from_airport)
- **Memory Efficient**: No hash table overhead

## When to Use LinkedList Implementation

**Good for:**
- Sparse graphs (few connections per airport)
- Sequential processing of all airports
- Memory-constrained environments
- Educational purposes (simpler to understand)

**Not ideal for:**
- Frequent random access to specific airports
- Dense graphs with many connections
- Performance-critical applications requiring fast lookups

## Usage Example

```java
// Create a new LinkedList-based graph
FlightGraph graph = new FlightGraph();

// Add a flight with ratings (weight calculated automatically)
graph.addFlightWithRatings(
    "LAX", "JFK", "Delta Airlines",
    4.5, 3.8, 4.2, 4.0, 3.5
);

// Get all outgoing flights from an airport
List<Edge> flights = graph.getFlightsFrom("LAX");

// Find the best flight between two airports
Edge bestFlight = graph.getBestFlight("LAX", "JFK");

// Get busiest airport
String busiest = graph.getBusiestAirport();

// Get graph statistics
graph.printGraphStats();
```

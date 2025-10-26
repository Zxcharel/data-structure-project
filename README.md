# Flight Graph Data Structure Project

This project implements two different graph data structures to represent flight routes between airports, populated from CSV data. It demonstrates the differences between HashMap and LinkedList implementations for graph representation.

## Project Structure

```
data-structure-project/
├── common/                          # Shared classes used by both implementations
│   ├── Edge.java                   # Edge class representing flight routes
│   ├── FlightGraphInterface.java   # Common interface for both implementations
│   └── package-info.java           # Package documentation
├── HashMapGraph/                    # HashMap-based implementation
│   ├── HashMapFlightGraph.java     # HashMap implementation of FlightGraph
│   ├── HashMapGraphDemo.java       # Demo program for HashMap implementation
│   └── HashMapGraphComparison.java # Performance comparison utilities
├── LinkedListGraph/                 # LinkedList-based implementation
│   ├── LinkedListFlightGraph.java  # LinkedList implementation of FlightGraph
│   ├── LinkedListGraphDemo.java    # Demo program for LinkedList implementation
│   └── GraphComparison.java        # Performance comparison utilities
├── CSVParser.java                   # Unified CSV parser for both implementations
├── LoadYourData.java               # Main program to load and analyze flight data
├── cleaned_flights.csv              # Flight data (converted from Excel)
└── README.md                       # This documentation file
```

## Graph Implementations

### 1. HashMap Implementation (HashMapGraph Directory)
- **File**: `HashMapGraph/HashMapFlightGraph.java`
- **Data Structure**: `HashMap<String, List<Edge>>`
- **Airport Storage**: Keys in HashMap
- **Edge Storage**: ArrayList in HashMap values
- **Search Time**: O(1) average
- **Best for**: Frequent random access, dense graphs

### 2. LinkedList Implementation (LinkedListGraph Directory)
- **File**: `LinkedListGraph/LinkedListFlightGraph.java`
- **Data Structure**: `LinkedList<AirportNode>`
- **Airport Storage**: AirportNode objects in LinkedList
- **Edge Storage**: LinkedList within each AirportNode
- **Search Time**: O(n) linear search
- **Best for**: Sequential processing, memory-constrained environments

## Common Components

### Edge Class (`common/Edge.java`)
Represents a flight route between two airports with:
- Destination airport code
- Airline name
- Weight rating (calculated from multiple factors)
- Static methods for weight calculation

### CSVParser Class (`CSVParser.java`)
Handles CSV file parsing and graph population:
- Flexible column mapping based on header names
- Error handling for invalid data
- Support for quoted CSV values
- Automatic weight calculation from ratings
- Works with both HashMap and LinkedList implementations

## Weight Calculation

### Rating-to-Weight Mapping
Higher ratings correspond to lower weights (better airlines = smaller weight):
- Rating: 5 = Weight: 1
- Rating: 4 = Weight: 2
- Rating: 3 = Weight: 3
- Rating: 2 = Weight: 4
- Rating: 1 = Weight: 5

**Formula**: `Weight = 6 - Rating`

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

### Prerequisites
- Java 11 or higher
- Your Excel file converted to CSV format (`cleaned_flights.csv`)

### Quick Start
```bash
# Compile all classes (including common package)
javac common/*.java *.java HashMapGraph/*.java LinkedListGraph/*.java

# Run HashMap implementation with your data
java LoadYourData

# Run HashMap implementation demo
java HashMapGraphDemo

# Run LinkedList implementation demo
java LinkedListGraphDemo

# Run performance comparison
java HashMapGraphComparison
java GraphComparison
```

### What Each Program Does

#### LoadYourData.java
- Loads flight data from `cleaned_flights.csv`
- Creates HashMap-based graph with all airports and routes
- Shows comprehensive statistics and analysis
- Displays complete graph structure

#### HashMapGraphDemo.java
- Demonstrates HashMap-based implementation
- Shows HashMap-specific features (random access performance)
- Compares performance characteristics
- Loads data from `cleaned_flights.csv`

#### LinkedListGraphDemo.java
- Demonstrates LinkedList-based implementation
- Shows LinkedList-specific features (busiest airports, sorted lists)
- Compares performance characteristics
- Loads data from `../cleaned_flights.csv`

#### HashMapGraphComparison.java & GraphComparison.java
- Compare HashMap vs LinkedList implementations
- Show implementation differences
- Provide performance analysis
- Demonstrate time complexity characteristics

## CSV File Format

The CSV parser expects columns with names containing these keywords:
- **Source Airport**: "source", "origin", "from", "departure"
- **Destination Airport**: "destination", "dest", "to", "arrival"
- **Airline**: "airline", "carrier"
- **Overall Rating**: "overall" + "rating"
- **Value for Money**: "value" + "money"
- **Inflight Entertainment**: "entertainment", "inflight"
- **Cabin Staff**: "staff", "cabin"
- **Seat Comfort**: "seat" + "comfort"

Example CSV format:
```csv
Source Airport,Destination Airport,Airline,Overall Rating,Value for Money Rating,Inflight Entertainment Rating,Cabin Staff Rating,Seat Comfort Rating
LAX,JFK,Delta Airlines,4.5,3.8,4.2,4.0,3.5
LAX,JFK,American Airlines,4.2,4.0,3.5,4.1,3.8
```

**Note**: Convert your Excel file to CSV format in Excel (File → Save As → CSV).

## Implementation Comparison

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

## Graph Operations

Both implementations support the same interface:

- `addAirport(String airportCode)` - Add an airport
- `addFlight(...)` - Add a flight route
- `addFlightWithRatings(...)` - Add flight with automatic weight calculation
- `getFlightsFrom(String airport)` - Get outgoing flights
- `getFlightsTo(String airport)` - Get incoming flights
- `getBestFlight(String from, String to)` - Find best direct route
- `getAirlinesFrom(String airport)` - Get airlines from airport
- `printGraphStats()` - Display statistics
- `printGraph()` - Display full graph structure

### LinkedList-Specific Operations
- `getBusiestAirport()` - Find airport with most flights
- `getAirportsByFlightCount()` - Get airports sorted by flight count

## Performance Characteristics

### HashMap Implementation
- **Add Airport**: O(1) - HashMap insertion
- **Add Flight**: O(1) - HashMap lookup + ArrayList append
- **Get Flights From**: O(1) - HashMap lookup
- **Search Airport**: O(1) - HashMap lookup
- **Get All Airports**: O(n) - HashMap keySet iteration

### LinkedList Implementation
- **Add Airport**: O(1) - LinkedList insertion at end
- **Add Flight**: O(n) - Need to find source airport
- **Get Flights From**: O(n) - Need to find airport, then O(1) to get flights
- **Search Airport**: O(n) - Linear search through LinkedList
- **Get All Airports**: O(n) - Traverse entire LinkedList

## Usage Examples

### HashMap Implementation
```java
// Create a new HashMap-based graph
HashMapFlightGraph graph = new HashMapFlightGraph();

// Add a flight with ratings (weight calculated automatically)
graph.addFlightWithRatings(
    "LAX", "JFK", "Delta Airlines",
    4.5, 3.8, 4.2, 4.0, 3.5
);

// Get all outgoing flights from an airport
List<Edge> flights = graph.getFlightsFrom("LAX");

// Find the best flight between two airports
Edge bestFlight = graph.getBestFlight("LAX", "JFK");

// Get graph statistics
graph.printGraphStats();
```

### LinkedList Implementation
```java
// Create a new LinkedList-based graph
LinkedListFlightGraph graph = new LinkedListFlightGraph();

// Add a flight with ratings (weight calculated automatically)
graph.addFlightWithRatings(
    "LAX", "JFK", "Delta Airlines",
    4.5, 3.8, 4.2, 4.0, 3.5
);

// Get busiest airport
String busiest = graph.getBusiestAirport();

// Get airports sorted by flight count
List<String> airportsByCount = graph.getAirportsByFlightCount();

// Get graph statistics
graph.printGraphStats();
```

## Features

- **Dual Implementation**: Both HashMap and LinkedList approaches
- **Automatic Weight Calculation**: Converts ratings to weights using specified formula
- **Flexible CSV Parsing**: Automatically maps columns based on header names
- **Graph Operations**: Find best flights, get airlines, statistics, etc.
- **Error Handling**: Graceful handling of missing or invalid data
- **Real Data Analysis**: Works with actual flight datasets
- **Performance Comparison**: Built-in tools to compare implementations
- **Clean Architecture**: Common components shared between implementations

## When to Use Each Implementation

### Use HashMap Implementation When:
- Frequent random access to specific airports
- Dense graphs with many connections
- Performance-critical applications requiring fast lookups
- Working with large datasets where search time matters

### Use LinkedList Implementation When:
- Sparse graphs (few connections per airport)
- Sequential processing of all airports
- Memory-constrained environments
- Educational purposes (simpler to understand)
- Need LinkedList-specific features (sorted operations)

## Your Data Analysis Results

Based on your `cleaned_flights.csv` data:
- **Hundreds of airports** worldwide
- **Thousands of flight routes**
- **Busiest hub**: London (83 outgoing flights)
- **Best rated flight**: Air Astana (weight: 1.00)
- **Worst rated flight**: Swiss International Air Lines (weight: 5.20)
- **Major hubs**: Los Angeles, Rome, Melbourne, Paris, Manchester
# Flight Graph Implementation

This project implements a graph data structure using HashMap to represent flight routes between airports, populated from CSV data.

## Project Structure

- `Edge.java` - Represents a flight route between two airports
- `FlightGraph.java` - Main graph class using HashMap implementation
- `CSVParser.java` - Parses CSV files and populates the graph (no dependencies required)
- `LoadYourData.java` - Main program to load and analyze your flight data
- `cleaned_flights.csv` - Your actual flight data (converted from Excel)
- `README.md` - This documentation file

## Graph Representation

### Vertices (Nodes)
- Represent airports (identified by airport codes)
- Stored as keys in the HashMap

### Edges
- Represent direct flight routes between airports
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

# Run with your flight data
java LoadYourData
```

### Prerequisites
- Java 11 or higher
- Your Excel file converted to CSV format (`cleaned_flights.csv`)

### What LoadYourData Does
- Loads your actual flight data from `cleaned_flights.csv`
- Creates the graph with all airports and routes
- Calculates weights using your specified formula
- Shows comprehensive statistics and analysis
- Displays the complete graph structure

## Usage Example

```java
// Create a new graph
FlightGraph graph = new FlightGraph();

// Add a flight with ratings (weight calculated automatically)
graph.addFlightWithRatings(
    "LAX", "JFK", "Delta Airlines",
    4.5, 3.8, 4.2, 4.0, 3.5
);

// Add a flight with pre-calculated weight
graph.addFlight("LAX", "ORD", "United Airlines", 2.1);

// Get all outgoing flights from an airport
List<Edge> flights = graph.getFlightsFrom("LAX");

// Find the best flight between two airports
Edge bestFlight = graph.getBestFlight("LAX", "JFK");

// Get graph statistics
graph.printGraphStats();
```

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

## Features

- **HashMap Implementation**: Efficient adjacency list representation
- **Automatic Weight Calculation**: Converts ratings to weights using the specified formula
- **Flexible CSV Parsing**: Automatically maps columns based on header names
- **Graph Operations**: Find best flights, get airlines, statistics, etc.
- **Error Handling**: Graceful handling of missing or invalid data
- **Real Data Analysis**: Works with your actual flight dataset

## Graph Operations

- `addAirport(String airportCode)` - Add an airport
- `addFlight(...)` - Add a flight route
- `addFlightWithRatings(...)` - Add flight with automatic weight calculation
- `getFlightsFrom(String airport)` - Get outgoing flights
- `getFlightsTo(String airport)` - Get incoming flights
- `getBestFlight(String from, String to)` - Find best direct route
- `getAirlinesFrom(String airport)` - Get airlines from airport
- `printGraphStats()` - Display statistics
- `printGraph()` - Display full graph structure

## Your Data Analysis Results

Based on your `cleaned_flights.csv` data:
- **Hundreds of airports** worldwide
- **Thousands of flight routes**
- **Busiest hub**: London (83 outgoing flights)
- **Best rated flight**: Air Astana (weight: 1.00)
- **Worst rated flight**: Swiss International Air Lines (weight: 5.20)
- **Major hubs**: Los Angeles, Rome, Melbourne, Paris, Manchester

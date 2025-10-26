import java.io.IOException;
import java.util.List;

/**
 * Main program to test the LinkedList-based FlightGraph implementation
 */
public class LinkedListGraphDemo {
    
    public static void main(String[] args) {
        try {
            // Load flight data from CSV
            String csvFilePath = "../src/cleaned_flights.csv"; // Path to your CSV file
            System.out.println("Loading flight data from: " + csvFilePath);
            
            FlightGraph graph = CSVParser.parseCSVToGraph(csvFilePath);
            
            // Display graph statistics
            graph.printGraphStats();
            
            // Demonstrate LinkedList-specific features
            demonstrateLinkedListFeatures(graph);
            
            // Compare with HashMap implementation
            compareImplementations(graph);
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.err.println("Make sure the CSV file path is correct.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Demonstrate LinkedList-specific features
     */
    private static void demonstrateLinkedListFeatures(FlightGraph graph) {
        System.out.println("\n=== LinkedList Implementation Features ===");
        
        // Show busiest airports
        String busiestAirport = graph.getBusiestAirport();
        if (busiestAirport != null) {
            System.out.println("Busiest Airport: " + busiestAirport);
            System.out.println("Outgoing flights: " + graph.getFlightsFrom(busiestAirport).size());
        }
        
        // Show airports sorted by flight count
        System.out.println("\nTop 10 Busiest Airports:");
        List<String> airportsByFlightCount = graph.getAirportsByFlightCount();
        for (int i = 0; i < Math.min(10, airportsByFlightCount.size()); i++) {
            String airport = airportsByFlightCount.get(i);
            int flightCount = graph.getFlightsFrom(airport).size();
            System.out.println("  " + (i + 1) + ". " + airport + " (" + flightCount + " flights)");
        }
        
        // Show sample graph structure (first 5 airports)
        System.out.println("\n=== Sample Graph Structure (First 5 Airports) ===");
        int count = 0;
        for (String airport : graph.getAllAirports()) {
            if (count >= 5) break;
            System.out.println(airport + ":");
            var flights = graph.getFlightsFrom(airport);
            for (int i = 0; i < Math.min(3, flights.size()); i++) {
                System.out.println("  -> " + flights.get(i));
            }
            if (flights.size() > 3) {
                System.out.println("  ... and " + (flights.size() - 3) + " more flights");
            }
            System.out.println();
            count++;
        }
    }
    
    /**
     * Compare LinkedList vs HashMap implementations
     */
    private static void compareImplementations(FlightGraph graph) {
        System.out.println("\n=== LinkedList vs HashMap Comparison ===");
        
        // Test search operations
        long startTime = System.nanoTime();
        
        // Test finding flights from a specific airport
        String testAirport = "london";
        var flights = graph.getFlightsFrom(testAirport);
        
        long endTime = System.nanoTime();
        long searchTime = endTime - startTime;
        
        System.out.println("Search time for flights from '" + testAirport + "': " + 
                          String.format("%.2f", searchTime / 1_000_000.0) + " ms");
        System.out.println("Found " + flights.size() + " flights");
        
        // Test finding best flight
        startTime = System.nanoTime();
        Edge bestFlight = graph.getBestFlight("london", "paris");
        endTime = System.nanoTime();
        long bestFlightTime = endTime - startTime;
        
        if (bestFlight != null) {
            System.out.println("Best flight from London to Paris: " + bestFlight);
            System.out.println("Best flight search time: " + 
                              String.format("%.2f", bestFlightTime / 1_000_000.0) + " ms");
        }
        
        // Show LinkedList characteristics
        System.out.println("\nLinkedList Implementation Characteristics:");
        System.out.println("- Sequential access pattern");
        System.out.println("- O(n) search time for airports");
        System.out.println("- O(1) insertion at end");
        System.out.println("- Memory efficient for sparse graphs");
        System.out.println("- Good for traversal operations");
    }
    
    /**
     * Demonstrate graph operations
     */
    private static void demonstrateGraphOperations(FlightGraph graph) {
        System.out.println("\n=== Graph Operations Demo ===");
        
        // Get a sample airport for demonstration
        String sampleAirport = graph.getAllAirports().iterator().next();
        System.out.println("Sample airport: " + sampleAirport);
        
        // Show outgoing flights
        var outgoingFlights = graph.getFlightsFrom(sampleAirport);
        System.out.println("Outgoing flights from " + sampleAirport + ": " + outgoingFlights.size());
        
        if (!outgoingFlights.isEmpty()) {
            // Show best flight (lowest weight)
            Edge bestFlight = outgoingFlights.get(0);
            for (Edge flight : outgoingFlights) {
                if (flight.getWeight() < bestFlight.getWeight()) {
                    bestFlight = flight;
                }
            }
            System.out.println("Best flight from " + sampleAirport + ": " + bestFlight);
            
            // Show airlines operating from this airport
            var airlines = graph.getAirlinesFrom(sampleAirport);
            System.out.println("Airlines operating from " + sampleAirport + ": " + airlines);
        }
    }
}

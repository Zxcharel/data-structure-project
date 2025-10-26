import java.io.IOException;
import java.util.List;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Main program to test the HashMap-based FlightGraph implementation
 */
public class HashMapGraphDemo {
    
    public static void main(String[] args) {
        try {
            // Load flight data from CSV
            String csvFilePath = "cleaned_flights.csv"; // Path to your CSV file
            System.out.println("Loading flight data from: " + csvFilePath);
            
            HashMapFlightGraph graph = (HashMapFlightGraph) CSVParser.parseCSVToGraph(csvFilePath);
            
            // Display graph statistics
            graph.printGraphStats();
            
            // Demonstrate HashMap-specific features
            demonstrateHashMapFeatures(graph);
            
            // Compare with LinkedList implementation
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
     * Demonstrate HashMap-specific features
     */
    private static void demonstrateHashMapFeatures(FlightGraphInterface graph) {
        System.out.println("\n=== HashMap Implementation Features ===");
        
        // Show random access capabilities
        System.out.println("HashMap Random Access Performance:");
        
        // Test multiple random airport lookups
        long startTime = System.nanoTime();
        for (String airport : graph.getAllAirports()) {
            var flights = graph.getFlightsFrom(airport);
            if (flights.size() > 0) break; // Just test one to show O(1) access
        }
        long endTime = System.nanoTime();
        long accessTime = endTime - startTime;
        
        System.out.println("  - Random airport access time: " + 
                          String.format("%.2f", accessTime / 1_000_000.0) + " ms");
        
        // Show graph structure (first 5 airports)
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
     * Compare HashMap vs LinkedList implementations
     */
    private static void compareImplementations(FlightGraphInterface graph) {
        System.out.println("\n=== HashMap vs LinkedList Comparison ===");
        
        // Test search operations
        long startTime = System.nanoTime();
        
        // Test finding flights from a specific airport
        String testAirport = "london";
        var flights = graph.getFlightsFrom(testAirport);
        
        long endTime = System.nanoTime();
        long searchTime = endTime - startTime;
        
        System.out.println("HashMap Search Performance:");
        System.out.println("  - Search time for flights from '" + testAirport + "': " + 
                          String.format("%.2f", searchTime / 1_000_000.0) + " ms");
        System.out.println("  - Found " + flights.size() + " flights");
        
        // Test finding best flight
        startTime = System.nanoTime();
        Edge bestFlight = graph.getBestFlight("london", "paris");
        endTime = System.nanoTime();
        long bestFlightTime = endTime - startTime;
        
        if (bestFlight != null) {
            System.out.println("  - Best flight from London to Paris: " + bestFlight);
            System.out.println("  - Best flight search time: " + 
                              String.format("%.2f", bestFlightTime / 1_000_000.0) + " ms");
        }
        
        // Show HashMap characteristics
        System.out.println("\nHashMap Implementation Characteristics:");
        System.out.println("- O(1) average search time");
        System.out.println("- Fast random access to any airport");
        System.out.println("- Efficient for frequent lookups");
        System.out.println("- Better for dense graphs");
        System.out.println("- Higher memory overhead due to hash table");
    }
    
    /**
     * Demonstrate graph operations
     */
    private static void demonstrateGraphOperations(FlightGraphInterface graph) {
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

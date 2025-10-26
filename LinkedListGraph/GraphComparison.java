import java.io.IOException;

/**
 * Comparison between HashMap and LinkedList implementations
 */
public class GraphComparison {
    
    public static void main(String[] args) {
        System.out.println("=== Graph Implementation Comparison ===");
        System.out.println("HashMap vs LinkedList Implementation");
        System.out.println();
        
        try {
            // Test with sample data first
            testWithSampleData();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testWithSampleData() {
        System.out.println("=== Testing with Sample Data ===");
        
        // Create LinkedList-based graph
        FlightGraph linkedListGraph = new FlightGraph();
        
        // Add sample flights
        linkedListGraph.addFlightWithRatings("LAX", "JFK", "Delta Airlines", 4.5, 3.8, 4.2, 4.0, 3.5);
        linkedListGraph.addFlightWithRatings("LAX", "JFK", "American Airlines", 4.2, 4.0, 3.5, 4.1, 3.8);
        linkedListGraph.addFlightWithRatings("JFK", "LAX", "Delta Airlines", 4.5, 3.8, 4.2, 4.0, 3.5);
        linkedListGraph.addFlightWithRatings("LAX", "ORD", "United Airlines", 3.8, 4.2, 3.0, 3.9, 3.2);
        linkedListGraph.addFlightWithRatings("ORD", "JFK", "American Airlines", 4.0, 3.9, 3.8, 4.2, 3.6);
        
        // Display results
        linkedListGraph.printGraphStats();
        
        // Show implementation differences
        showImplementationDifferences();
        
        // Performance comparison
        performanceComparison(linkedListGraph);
    }
    
    private static void showImplementationDifferences() {
        System.out.println("\n=== Implementation Differences ===");
        
        System.out.println("HashMap Implementation:");
        System.out.println("  - Data Structure: HashMap<String, List<Edge>>");
        System.out.println("  - Airport Storage: Keys in HashMap");
        System.out.println("  - Edge Storage: ArrayList in HashMap values");
        System.out.println("  - Search Time: O(1) average");
        System.out.println("  - Memory: Higher overhead due to hash table");
        System.out.println("  - Best for: Frequent random access, dense graphs");
        
        System.out.println("\nLinkedList Implementation:");
        System.out.println("  - Data Structure: LinkedList<AirportNode>");
        System.out.println("  - Airport Storage: AirportNode objects in LinkedList");
        System.out.println("  - Edge Storage: LinkedList within each AirportNode");
        System.out.println("  - Search Time: O(n) linear search");
        System.out.println("  - Memory: Lower overhead, more efficient for sparse graphs");
        System.out.println("  - Best for: Sequential processing, memory-constrained environments");
    }
    
    private static void performanceComparison(FlightGraph graph) {
        System.out.println("\n=== Performance Comparison ===");
        
        // Test search operations
        String testAirport = "LAX";
        
        long startTime = System.nanoTime();
        var flights = graph.getFlightsFrom(testAirport);
        long endTime = System.nanoTime();
        long searchTime = endTime - startTime;
        
        System.out.println("LinkedList Search Performance:");
        System.out.println("  - Search time for '" + testAirport + "': " + 
                          String.format("%.2f", searchTime / 1_000_000.0) + " ms");
        System.out.println("  - Found " + flights.size() + " flights");
        
        // Test best flight search
        startTime = System.nanoTime();
        Edge bestFlight = graph.getBestFlight("LAX", "JFK");
        endTime = System.nanoTime();
        long bestFlightTime = endTime - startTime;
        
        if (bestFlight != null) {
            System.out.println("  - Best flight search time: " + 
                              String.format("%.2f", bestFlightTime / 1_000_000.0) + " ms");
            System.out.println("  - Best flight: " + bestFlight);
        }
        
        System.out.println("\nTime Complexity Analysis:");
        System.out.println("  - Add Airport: O(1) - insertion at end");
        System.out.println("  - Add Flight: O(n) - need to find source airport");
        System.out.println("  - Get Flights From: O(n) - linear search for airport");
        System.out.println("  - Search Airport: O(n) - sequential search");
        System.out.println("  - Get All Airports: O(n) - traverse entire list");
    }
}

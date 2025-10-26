import common.Edge;

/**
 * Comparison utilities for HashMap implementation
 */
public class HashMapGraphComparison {
    
    public static void main(String[] args) {
        System.out.println("=== HashMap Graph Implementation Analysis ===");
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
        
        // Create HashMap-based graph
        HashMapFlightGraph hashMapGraph = new HashMapFlightGraph();
        
        // Add sample flights
        hashMapGraph.addFlightWithRatings("LAX", "JFK", "Delta Airlines", 4.5, 3.8, 4.2, 4.0, 3.5);
        hashMapGraph.addFlightWithRatings("LAX", "JFK", "American Airlines", 4.2, 4.0, 3.5, 4.1, 3.8);
        hashMapGraph.addFlightWithRatings("JFK", "LAX", "Delta Airlines", 4.5, 3.8, 4.2, 4.0, 3.5);
        hashMapGraph.addFlightWithRatings("LAX", "ORD", "United Airlines", 3.8, 4.2, 3.0, 3.9, 3.2);
        hashMapGraph.addFlightWithRatings("ORD", "JFK", "American Airlines", 4.0, 3.9, 3.8, 4.2, 3.6);
        
        // Display results
        hashMapGraph.printGraphStats();
        
        // Show implementation differences
        showImplementationDifferences();
        
        // Performance comparison
        performanceComparison(hashMapGraph);
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
    
    private static void performanceComparison(HashMapFlightGraph graph) {
        System.out.println("\n=== Performance Comparison ===");
        
        // Test search operations
        String testAirport = "LAX";
        
        long startTime = System.nanoTime();
        var flights = graph.getFlightsFrom(testAirport);
        long endTime = System.nanoTime();
        long searchTime = endTime - startTime;
        
        System.out.println("HashMap Search Performance:");
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
        System.out.println("  - Add Airport: O(1) - HashMap insertion");
        System.out.println("  - Add Flight: O(1) - HashMap lookup + ArrayList append");
        System.out.println("  - Get Flights From: O(1) - HashMap lookup");
        System.out.println("  - Search Airport: O(1) - HashMap lookup");
        System.out.println("  - Get All Airports: O(n) - HashMap keySet iteration");
        
        System.out.println("\nSpace Complexity Analysis:");
        System.out.println("  - Overall: O(V + E) where V = vertices (airports), E = edges (flights)");
        System.out.println("  - Per Airport: O(1) + O(flights_from_airport)");
        System.out.println("  - Memory Overhead: Hash table overhead + ArrayList overhead");
    }
}

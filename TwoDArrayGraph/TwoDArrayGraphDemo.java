package TwoDArrayGraph;

import java.util.List;
import common.Edge;
import common.FlightGraphInterface;
import common.CSVParser;

/**
 * Demo program for the TwoDArrayFlightGraph implementation
 */
public class TwoDArrayGraphDemo {

    public static void main(String[] args) {
        try {
            String csvFilePath = "cleaned_flights.csv";
            System.out.println("Loading flight data from: " + csvFilePath);

            // Parse CSV directly into a new TwoDArrayFlightGraph (avoid reflection)
            TwoDArrayFlightGraph graph = CSVParser.parseCSVIntoGraph(csvFilePath, new TwoDArrayFlightGraph());

            // Print basic stats
            graph.printGraphStats();

            // Demonstrate 2D array specific features
            demonstrateTwoDArrayFeatures(graph);

            // Show sample graph structure
            showSampleGraphStructure(graph);

            // Compare with other implementations
            compareImplementations(graph);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demonstrate 2D array specific features
     */
    private static void demonstrateTwoDArrayFeatures(TwoDArrayFlightGraph graph) {
        System.out.println("\n=== Two-Dimensional Array Implementation Features ===");
        
        // Show density and sparsity analysis
        System.out.println("Graph Density Analysis:");
        System.out.println("  - Density: " + String.format("%.2f%%", graph.getDensity()));
        System.out.println("  - Sparsity: " + String.format("%.2f%%", graph.getSparsity()));
        System.out.println("  - Is Dense: " + (graph.isDense() ? "Yes" : "No"));
        System.out.println("  - Memory Efficiency: " + String.format("%.2f%%", graph.getMemoryEfficiency()));
        
        // Show direct access performance
        System.out.println("\nDirect Access Performance:");
        long startTime = System.nanoTime();
        
        // Test direct access to specific flights
        Edge testFlight = graph.getBestFlight("london", "paris");
        
        long endTime = System.nanoTime();
        long accessTime = endTime - startTime;
        
        System.out.println("  - Direct flight lookup time: " + 
                          String.format("%.2f", accessTime / 1_000_000.0) + " ms");
        
        if (testFlight != null) {
            System.out.println("  - Found flight: " + testFlight);
        } else {
            System.out.println("  - No direct flight found between London and Paris");
        }
        
        // Show matrix characteristics
        System.out.println("\nMatrix Characteristics:");
        System.out.println("  - O(1) direct flight lookup");
        System.out.println("  - O(n) to get all flights from an airport");
        System.out.println("  - O(n) to get all flights to an airport");
        System.out.println("  - Memory usage: O(n²) where n = number of airports");
        System.out.println("  - Best for: Dense graphs with many direct connections");
        System.out.println("  - Worst for: Sparse graphs (wastes memory)");
    }

    /**
     * Show sample graph structure
     */
    private static void showSampleGraphStructure(TwoDArrayFlightGraph graph) {
        System.out.println("\n=== Sample Graph Structure (First 5 Airports) ===");
        int count = 0;
        for (String airport : graph.getAllAirports()) {
            if (count >= 5) break;
            System.out.println(airport + ":");
            List<Edge> flights = graph.getFlightsFrom(airport);
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
     * Compare 2D array with other implementations
     */
    private static void compareImplementations(TwoDArrayFlightGraph graph) {
        System.out.println("\n=== Implementation Comparison ===");
        
        System.out.println("Two-Dimensional Array Implementation:");
        System.out.println("  - Data Structure: Edge[][] adjacencyMatrix");
        System.out.println("  - Airport Storage: Map<String, Integer> for index mapping");
        System.out.println("  - Edge Storage: Direct array access [source][destination]");
        System.out.println("  - Direct Flight Lookup: O(1) - instant access");
        System.out.println("  - Get All Flights From: O(n) - scan row");
        System.out.println("  - Get All Flights To: O(n) - scan column");
        System.out.println("  - Memory: O(n²) - always allocates full matrix");
        System.out.println("  - Best for: Dense graphs, frequent direct lookups");
        System.out.println("  - Worst for: Sparse graphs, memory-constrained environments");
        
        System.out.println("\nWhen to Use Each Implementation:");
        System.out.println("  - HashMap: General purpose, good balance of speed and memory");
        System.out.println("  - LinkedList: Memory efficient, good for sparse graphs");
        System.out.println("  - DoublyLinkedList: Educational, demonstrates manual pointer manipulation");
        System.out.println("  - TwoDArray: Dense graphs, frequent direct flight queries");
        
        // Show performance characteristics
        System.out.println("\nPerformance Characteristics:");
        System.out.println("  - Add Airport: O(1) - just add to map");
        System.out.println("  - Add Flight: O(1) - direct array assignment");
        System.out.println("  - Get Direct Flight: O(1) - array lookup");
        System.out.println("  - Get All From Airport: O(n) - scan entire row");
        System.out.println("  - Get All To Airport: O(n) - scan entire column");
        System.out.println("  - Memory Usage: O(n²) - always full matrix");
    }

}

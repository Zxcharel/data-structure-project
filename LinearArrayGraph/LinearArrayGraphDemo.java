package LinearArrayGraph;

import java.io.IOException;
import java.util.List;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Main program to test the Linear Array-based FlightGraph implementation
 */

// java-cp.LinearArrayGraph.LinearArrayGraphDemo
public class LinearArrayGraphDemo {

    public static void main(String[] args) {
        try {
            // Load flight data from CSV
            String csvFilePath = "cleaned_flights.csv"; // Path to your CSV file
            System.out.println("Loading flight data from: " + csvFilePath);

            // Use LinearArrayCSVLoader to create LinearArrayFlightGraph
            LinearArrayFlightGraph graph = LinearArrayCSVLoader.loadFromCSV(csvFilePath);

            // Display graph statistics
            graph.printGraphStats();

            // Demonstrate Linear Array-specific features
            demonstrateLinearArrayFeatures(graph);

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
     * Demonstrate Linear Array-specific features
     */
    private static void demonstrateLinearArrayFeatures(FlightGraphInterface graph) {
        System.out.println("\n=== Linear Array Implementation Features ===");

        LinearArrayFlightGraph linearGraph = (LinearArrayFlightGraph) graph;

        // Show array utilization
        System.out.println("Array Utilization Statistics:");
        System.out.println("  - " + linearGraph.getArrayUtilization());
        System.out.println("  - Estimated memory usage: " +
                String.format("%.2f", linearGraph.getEstimatedMemoryUsage() / 1024.0) + " KB");

        // Show sequential access performance
        System.out.println("\nLinear Array Sequential Access Performance:");

        long startTime = System.nanoTime();
        int totalFlights = 0;
        for (String airport : graph.getAllAirports()) {
            totalFlights += graph.getFlightsFrom(airport).size();
        }
        long endTime = System.nanoTime();
        long sequentialTime = endTime - startTime;

        System.out.println("  - Sequential traversal time: " +
                String.format("%.2f", sequentialTime / 1_000_000.0) + " ms");
        System.out.println("  - Processed " + totalFlights + " total flights");

        // Show busiest airport functionality
        System.out.println("\n=== Busiest Airport Analysis ===");
        String busiestAirport = linearGraph.getBusiestAirport();
        if (busiestAirport != null) {
            int busiestFlights = graph.getFlightsFrom(busiestAirport).size();
            System.out.println("Busiest Airport: " + busiestAirport + " (" + busiestFlights + " flights)");

            // Show top 5 busiest airports
            List<String> airportsByFlightCount = linearGraph.getAirportsByFlightCount();
            System.out.println("\nTop 5 Busiest Airports:");
            for (int i = 0; i < Math.min(5, airportsByFlightCount.size()); i++) {
                String airport = airportsByFlightCount.get(i);
                int flightCount = graph.getFlightsFrom(airport).size();
                System.out.println("  " + (i + 1) + ". " + airport + " (" + flightCount + " flights)");
            }
        }

        // Show graph structure (first 5 airports)
        System.out.println("\n=== Sample Graph Structure (First 5 Airports) ===");
        int count = 0;
        for (String airport : graph.getAllAirports()) {
            if (count >= 5)
                break;
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
     * Compare Linear Array vs HashMap implementations
     */
    private static void compareImplementations(FlightGraphInterface graph) {
        System.out.println("\n=== Linear Array vs HashMap Comparison ===");

        LinearArrayFlightGraph linearGraph = (LinearArrayFlightGraph) graph;

        // Test search operations
        long startTime = System.nanoTime();

        // Test finding flights from a specific airport
        String testAirport = "london";
        var flights = graph.getFlightsFrom(testAirport);

        long endTime = System.nanoTime();
        long searchTime = endTime - startTime;

        System.out.println("Linear Array Search Performance:");
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

        // Show Linear Array characteristics
        System.out.println("\nLinear Array Implementation Characteristics:");
        System.out.println("- O(1) average search time with HashMap index mapping");
        System.out.println("- Sequential memory layout for better cache performance");
        System.out.println("- Dynamic array expansion when capacity is reached");
        System.out.println("- Lower memory overhead compared to HashMap");
        System.out.println("- Efficient for both random access and sequential traversal");
        System.out.println("- Built-in sorting capabilities for airport analysis");

        // Memory comparison
        System.out.println("\nMemory Usage Analysis:");
        System.out.println("  - " + linearGraph.getArrayUtilization());
        System.out.println("  - Estimated memory: " +
                String.format("%.2f", linearGraph.getEstimatedMemoryUsage() / 1024.0) + " KB");
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

        // Demonstrate Linear Array specific operations
        LinearArrayFlightGraph linearGraph = (LinearArrayFlightGraph) graph;
        System.out.println("\nLinear Array Specific Operations:");
        System.out.println("  - " + linearGraph.getArrayUtilization());

        // Show airport sorting capability
        List<String> sortedAirports = linearGraph.getAirportsByFlightCount();
        System.out.println("  - Airports sorted by flight count: " +
                sortedAirports.subList(0, Math.min(3, sortedAirports.size())));
    }
}

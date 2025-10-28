package CircularLinkedListGraph;

import java.io.IOException;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Demonstrates the Circular Linked List implementation of the flight graph.
 */
public class CircularLinkedListGraphDemo {

    public static void main(String[] args) {
        try {
            // Load flight data
            String csvFile = "cleaned_flights.csv";
            System.out.println("Loading data from: " + csvFile);

            CircularLinkedListFlightGraph graph = (CircularLinkedListFlightGraph)
                    CSVParser.parseCSVToGraph(csvFile);

            graph.printGraphStats();
            demonstrateCircularFeatures(graph);

        } catch (IOException e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void demonstrateCircularFeatures(FlightGraphInterface graph) {
        System.out.println("\n=== Circular Linked List Implementation Features ===");

        // Show traversal performance
        long start = System.nanoTime();
        for (String airport : graph.getAllAirports()) {
            graph.getFlightsFrom(airport);
        }
        long end = System.nanoTime();
        System.out.println("Traversal completed in " +
                String.format("%.2f", (end - start) / 1_000_000.0) + " ms");

        // Show structure
        graph.printGraph();

        // Example comparison
        System.out.println("\n=== Example: Best Flight ===");
        Edge best = graph.getBestFlight("london", "paris");
        if (best != null)
            System.out.println("Best flight from London → Paris: " + best);
        else
            System.out.println("No direct flight found.");

        System.out.println("\n=== Circular Linked List Characteristics ===");
        System.out.println("- O(n) traversal for airports");
        System.out.println("- Efficient memory usage (no HashMap overhead)");
        System.out.println("- Good for small, sequential graphs");
        System.out.println("- Simpler pointer-based structure");
    }
}

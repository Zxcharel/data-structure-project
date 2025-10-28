package DoublyLinkedListGraph;

import java.util.List;
import common.Edge;
import common.FlightGraphInterface;
import common.CSVParser;

// Note: CSVParser is in the root package, so we need to reference it directly

/**
 * Demo program for the DoublyLinkedListFlightGraph
 */
public class DoublyLinkedListGraphDemo {

    public static void main(String[] args) {
        try {
            String csvFilePath = "cleaned_flights.csv";
            System.out.println("Loading flight data from: " + csvFilePath);

            // Parse CSV directly into a new DoublyLinkedListFlightGraph (avoid reflection)
            DoublyLinkedListFlightGraph graph = CSVParser.parseCSVIntoGraph(csvFilePath, new DoublyLinkedListFlightGraph());

            // Print basic stats
            graph.printGraphStats();

            // Show first 5 airports with up to 3 outgoing flights
            System.out.println("\nSample Graph Structure (First 5 Airports):");
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
                count++;
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

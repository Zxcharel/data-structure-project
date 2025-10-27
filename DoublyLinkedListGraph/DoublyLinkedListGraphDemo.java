package DoublyLinkedListGraph;

import java.io.IOException;
import java.util.List;
import common.Edge;

/**
 * Demo program for the DoublyLinkedListFlightGraph
 */
public class DoublyLinkedListGraphDemo {

    public static void main(String[] args) {
        try {
            String csvFilePath = "cleaned_flights.csv";
            System.out.println("Loading flight data from: " + csvFilePath);

            // Parse CSV using CSVParser (assumes parseCSVToGraph returns FlightGraphInterface)
            DoublyLinkedListFlightGraph graph =
                    (DoublyLinkedListFlightGraph) CSVParser.parseCSVToGraph(csvFilePath);

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

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

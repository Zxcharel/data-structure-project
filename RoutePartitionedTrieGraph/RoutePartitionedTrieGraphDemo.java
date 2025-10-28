import java.io.IOException;
import java.util.List;
import common.Edge;
import common.FlightGraphInterface;
import common.CSVParser;

/**
 * Demo for the RoutePartitionedTrieFlightGraph implementation
 */
public class RoutePartitionedTrieGraphDemo {

    public static void main(String[] args) {
        try {
            String csvFilePath = "cleaned_flights.csv";
            System.out.println("Loading flight data from: " + csvFilePath);

            RoutePartitionedTrieFlightGraph graph = CSVParser.parseCSVIntoGraph(csvFilePath, new RoutePartitionedTrieFlightGraph());

            // Print basic stats
            graph.printGraphStats();

            // Show first 5 airports, up to 3 flights each
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

            // Demonstrate best-flight by route
            System.out.println("\nBest flight sample (london -> paris):");
            Edge best = graph.getBestFlight("london", "paris");
            if (best != null) System.out.println("  " + best);
            else System.out.println("  No direct flight found");

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}



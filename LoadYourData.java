import java.io.IOException;

/**
 * Demo to load your actual Excel data (converted to CSV)
 */
public class LoadYourData {
    
    public static void main(String[] args) {
        try {
            // Load your actual flight data
            String csvFilePath = "cleaned_flights.csv"; // Your converted CSV file
            System.out.println("Loading your flight data from: " + csvFilePath);
            
            FlightGraph graph = CSVParser.parseCSVToGraph(csvFilePath);
            
            // Display comprehensive statistics
            System.out.println("\n=== Your Flight Data Analysis ===");
            graph.printGraphStats();
            
            // Show detailed graph structure
            System.out.println("\n=== Complete Graph Structure ===");
            graph.printGraph();
            
            // Analyze the data
            analyzeFlightData(graph);
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.err.println("Make sure you've converted your Excel file to CSV format.");
            System.err.println("In Excel: File → Save As → CSV (Comma delimited)");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeFlightData(FlightGraph graph) {
        System.out.println("\n=== Flight Data Analysis ===");
        
        // Find airports with most connections
        String busiestAirport = "";
        int maxConnections = 0;
        
        for (String airport : graph.getAllAirports()) {
            int connections = graph.getFlightsFrom(airport).size();
            if (connections > maxConnections) {
                maxConnections = connections;
                busiestAirport = airport;
            }
        }
        
        System.out.println("Busiest airport: " + busiestAirport + " (" + maxConnections + " outgoing flights)");
        
        // Find best and worst rated flights
        Edge bestFlight = null;
        Edge worstFlight = null;
        double bestWeight = Double.MAX_VALUE;
        double worstWeight = Double.MIN_VALUE;
        
        for (String airport : graph.getAllAirports()) {
            for (Edge flight : graph.getFlightsFrom(airport)) {
                if (flight.getWeight() < bestWeight) {
                    bestWeight = flight.getWeight();
                    bestFlight = flight;
                }
                if (flight.getWeight() > worstWeight) {
                    worstWeight = flight.getWeight();
                    worstFlight = flight;
                }
            }
        }
        
        if (bestFlight != null) {
            System.out.println("Best rated flight: " + bestFlight);
        }
        if (worstFlight != null) {
            System.out.println("Worst rated flight: " + worstFlight);
        }
        
        // Show airline statistics
        System.out.println("\n=== Airline Analysis ===");
        for (String airport : graph.getAllAirports()) {
            var airlines = graph.getAirlinesFrom(airport);
            System.out.println("Airlines operating from " + airport + ": " + airlines.size() + " airlines");
            for (String airline : airlines) {
                System.out.println("  - " + airline);
            }
        }
    }
}

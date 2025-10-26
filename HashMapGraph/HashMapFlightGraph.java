import java.util.*;
import common.Edge;
import common.FlightGraphInterface;

/**
 * HashMapFlightGraph class representing flight routes between airports.
 * Implemented using HashMap where:
 * - Key: Airport code (String)
 * - Value: List of edges (List<Edge>) representing outgoing flights
 */
public class HashMapFlightGraph implements FlightGraphInterface {
    private Map<String, List<Edge>> adjacencyList;
    private Set<String> airports;
    
    /**
     * Constructor initializes empty graph
     */
    public HashMapFlightGraph() {
        this.adjacencyList = new HashMap<>();
        this.airports = new HashSet<>();
    }
    
    /**
     * Add an airport to the graph
     * @param airportCode the airport code to add
     */
    public void addAirport(String airportCode) {
        if (!adjacencyList.containsKey(airportCode)) {
            adjacencyList.put(airportCode, new ArrayList<>());
            airports.add(airportCode);
        }
    }
    
    /**
     * Add a flight route (edge) between two airports
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @param airline airline name
     * @param weight calculated weight rating
     */
    public void addFlight(String sourceAirport, String destinationAirport, 
                         String airline, double weight) {
        // Ensure both airports exist in the graph
        addAirport(sourceAirport);
        addAirport(destinationAirport);
        
        // Create new edge
        Edge edge = new Edge(destinationAirport, airline, weight);
        
        // Add edge to adjacency list
        adjacencyList.get(sourceAirport).add(edge);
    }
    
    /**
     * Add a flight route with rating components (calculates weight automatically)
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @param airline airline name
     * @param overallRating overall airline rating (1-5)
     * @param valueForMoneyRating value for money rating (1-5)
     * @param inflightEntertainmentRating inflight entertainment rating (1-5)
     * @param cabinStaffRating cabin staff rating (1-5)
     * @param seatComfortRating seat comfort rating (1-5)
     */
    public void addFlightWithRatings(String sourceAirport, String destinationAirport,
                                   String airline, double overallRating,
                                   double valueForMoneyRating, double inflightEntertainmentRating,
                                   double cabinStaffRating, double seatComfortRating) {
        double weight = Edge.calculateCombinedWeight(overallRating, valueForMoneyRating,
                                                   inflightEntertainmentRating, cabinStaffRating,
                                                   seatComfortRating);
        addFlight(sourceAirport, destinationAirport, airline, weight);
    }
    
    /**
     * Get all outgoing flights from a specific airport
     * @param airportCode the source airport code
     * @return list of edges representing outgoing flights
     */
    public List<Edge> getFlightsFrom(String airportCode) {
        return adjacencyList.getOrDefault(airportCode, new ArrayList<>());
    }
    
    /**
     * Get all airports in the graph
     * @return set of all airport codes
     */
    public Set<String> getAllAirports() {
        return new HashSet<>(airports);
    }
    
    /**
     * Check if an airport exists in the graph
     * @param airportCode the airport code to check
     * @return true if airport exists, false otherwise
     */
    public boolean hasAirport(String airportCode) {
        return airports.contains(airportCode);
    }
    
    /**
     * Get the number of airports in the graph
     * @return number of airports
     */
    public int getAirportCount() {
        return airports.size();
    }
    
    /**
     * Get the total number of flight routes in the graph
     * @return total number of edges
     */
    public int getFlightCount() {
        int totalFlights = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            totalFlights += edges.size();
        }
        return totalFlights;
    }
    
    /**
     * Get all flights to a specific destination airport
     * @param destinationAirport the destination airport code
     * @return list of edges representing incoming flights
     */
    public List<Edge> getFlightsTo(String destinationAirport) {
        List<Edge> incomingFlights = new ArrayList<>();
        for (List<Edge> edges : adjacencyList.values()) {
            for (Edge edge : edges) {
                if (edge.getDestinationAirport().equals(destinationAirport)) {
                    incomingFlights.add(edge);
                }
            }
        }
        return incomingFlights;
    }
    
    /**
     * Find the best flight route between two airports (lowest weight)
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @return the edge with the lowest weight, or null if no direct route exists
     */
    public Edge getBestFlight(String sourceAirport, String destinationAirport) {
        List<Edge> flights = getFlightsFrom(sourceAirport);
        Edge bestFlight = null;
        double bestWeight = Double.MAX_VALUE;
        
        for (Edge edge : flights) {
            if (edge.getDestinationAirport().equals(destinationAirport) && 
                edge.getWeight() < bestWeight) {
                bestFlight = edge;
                bestWeight = edge.getWeight();
            }
        }
        
        return bestFlight;
    }
    
    /**
     * Get all airlines operating flights from a specific airport
     * @param airportCode the source airport code
     * @return set of airline names
     */
    public Set<String> getAirlinesFrom(String airportCode) {
        Set<String> airlines = new HashSet<>();
        List<Edge> flights = getFlightsFrom(airportCode);
        for (Edge edge : flights) {
            airlines.add(edge.getAirline());
        }
        return airlines;
    }
    
    /**
     * Print graph statistics
     */
    public void printGraphStats() {
        System.out.println("=== Flight Graph Statistics ===");
        System.out.println("Total Airports: " + getAirportCount());
        System.out.println("Total Flight Routes: " + getFlightCount());
        System.out.println();
        
        System.out.println("Airports in the graph:");
        for (String airport : airports) {
            int outgoingFlights = getFlightsFrom(airport).size();
            System.out.println("  " + airport + " (" + outgoingFlights + " outgoing flights)");
        }
    }
    
    /**
     * Print detailed graph structure
     */
    public void printGraph() {
        System.out.println("=== Flight Graph Structure ===");
        for (String airport : airports) {
            System.out.println(airport + ":");
            List<Edge> flights = getFlightsFrom(airport);
            if (flights.isEmpty()) {
                System.out.println("  No outgoing flights");
            } else {
                for (Edge edge : flights) {
                    System.out.println("  -> " + edge);
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Clear all data from the graph
     */
    public void clear() {
        adjacencyList.clear();
        airports.clear();
    }
}

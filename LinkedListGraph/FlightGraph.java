import java.util.*;

/**
 * FlightGraph class representing flight routes between airports.
 * Implemented using LinkedList where:
 * - Each airport is represented as a node containing a LinkedList of edges
 * - Airports are stored in a LinkedList of AirportNode objects
 * - Each AirportNode contains the airport code and its outgoing flights
 */
public class FlightGraph {
    
    /**
     * Inner class representing an airport node
     */
    private static class AirportNode {
        private String airportCode;
        private LinkedList<Edge> outgoingFlights;
        
        public AirportNode(String airportCode) {
            this.airportCode = airportCode;
            this.outgoingFlights = new LinkedList<>();
        }
        
        public String getAirportCode() {
            return airportCode;
        }
        
        public LinkedList<Edge> getOutgoingFlights() {
            return outgoingFlights;
        }
        
        public void addFlight(Edge edge) {
            outgoingFlights.add(edge);
        }
        
        public boolean hasFlights() {
            return !outgoingFlights.isEmpty();
        }
        
        public int getFlightCount() {
            return outgoingFlights.size();
        }
    }
    
    private LinkedList<AirportNode> airports;
    
    /**
     * Constructor initializes empty graph
     */
    public FlightGraph() {
        this.airports = new LinkedList<>();
    }
    
    /**
     * Add an airport to the graph
     * @param airportCode the airport code to add
     */
    public void addAirport(String airportCode) {
        if (!hasAirport(airportCode)) {
            airports.add(new AirportNode(airportCode));
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
        
        // Find the source airport node and add the edge
        AirportNode sourceNode = findAirportNode(sourceAirport);
        if (sourceNode != null) {
            sourceNode.addFlight(edge);
        }
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
     * Find an airport node by airport code
     * @param airportCode the airport code to find
     * @return the AirportNode or null if not found
     */
    private AirportNode findAirportNode(String airportCode) {
        for (AirportNode node : airports) {
            if (node.getAirportCode().equals(airportCode)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Get all outgoing flights from a specific airport
     * @param airportCode the source airport code
     * @return list of edges representing outgoing flights
     */
    public List<Edge> getFlightsFrom(String airportCode) {
        AirportNode node = findAirportNode(airportCode);
        if (node != null) {
            return new ArrayList<>(node.getOutgoingFlights());
        }
        return new ArrayList<>();
    }
    
    /**
     * Get all airports in the graph
     * @return set of all airport codes
     */
    public Set<String> getAllAirports() {
        Set<String> airportCodes = new HashSet<>();
        for (AirportNode node : airports) {
            airportCodes.add(node.getAirportCode());
        }
        return airportCodes;
    }
    
    /**
     * Check if an airport exists in the graph
     * @param airportCode the airport code to check
     * @return true if airport exists, false otherwise
     */
    public boolean hasAirport(String airportCode) {
        return findAirportNode(airportCode) != null;
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
        for (AirportNode node : airports) {
            totalFlights += node.getFlightCount();
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
        for (AirportNode node : airports) {
            for (Edge edge : node.getOutgoingFlights()) {
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
        AirportNode sourceNode = findAirportNode(sourceAirport);
        if (sourceNode == null) return null;
        
        Edge bestFlight = null;
        double bestWeight = Double.MAX_VALUE;
        
        for (Edge edge : sourceNode.getOutgoingFlights()) {
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
        AirportNode node = findAirportNode(airportCode);
        if (node != null) {
            for (Edge edge : node.getOutgoingFlights()) {
                airlines.add(edge.getAirline());
            }
        }
        return airlines;
    }
    
    /**
     * Print graph statistics
     */
    public void printGraphStats() {
        System.out.println("=== Flight Graph Statistics (LinkedList Implementation) ===");
        System.out.println("Total Airports: " + getAirportCount());
        System.out.println("Total Flight Routes: " + getFlightCount());
        System.out.println();
        
        System.out.println("Airports in the graph:");
        for (AirportNode node : airports) {
            System.out.println("  " + node.getAirportCode() + " (" + node.getFlightCount() + " outgoing flights)");
        }
    }
    
    /**
     * Print detailed graph structure
     */
    public void printGraph() {
        System.out.println("=== Flight Graph Structure (LinkedList Implementation) ===");
        for (AirportNode node : airports) {
            System.out.println(node.getAirportCode() + ":");
            if (!node.hasFlights()) {
                System.out.println("  No outgoing flights");
            } else {
                for (Edge edge : node.getOutgoingFlights()) {
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
        airports.clear();
    }
    
    /**
     * Get airports sorted by number of outgoing flights (busiest first)
     * @return list of airport codes sorted by flight count
     */
    public List<String> getAirportsByFlightCount() {
        List<AirportNode> sortedNodes = new ArrayList<>(airports);
        sortedNodes.sort((a, b) -> Integer.compare(b.getFlightCount(), a.getFlightCount()));
        
        List<String> result = new ArrayList<>();
        for (AirportNode node : sortedNodes) {
            result.add(node.getAirportCode());
        }
        return result;
    }
    
    /**
     * Find the busiest airport (most outgoing flights)
     * @return airport code of the busiest airport
     */
    public String getBusiestAirport() {
        if (airports.isEmpty()) return null;
        
        AirportNode busiest = airports.get(0);
        for (AirportNode node : airports) {
            if (node.getFlightCount() > busiest.getFlightCount()) {
                busiest = node;
            }
        }
        return busiest.getAirportCode();
    }
}

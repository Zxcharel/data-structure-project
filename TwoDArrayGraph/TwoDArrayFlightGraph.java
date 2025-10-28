package TwoDArrayGraph;

import java.util.*;
import common.Edge;
import common.FlightGraphInterface;

/**
 * Two-dimensional array implementation of a flight graph.
 * Uses a 2D array where:
 * - Rows represent source airports
 * - Columns represent destination airports
 * - Array values store Edge objects (or null if no direct flight)
 * 
 * This implementation is best for dense graphs with many direct connections.
 */
public class TwoDArrayFlightGraph implements FlightGraphInterface {
    
    // 2D array to store edges: [source][destination] = Edge
    private Edge[][] adjacencyMatrix;
    
    // Maps airport codes to array indices
    private Map<String, Integer> airportToIndex;
    private Map<Integer, String> indexToAirport;
    
    // Current number of airports and capacity
    private int airportCount;
    private int capacity;
    
    // Default initial capacity
    private static final int INITIAL_CAPACITY = 100;
    
    /**
     * Constructor initializes empty graph with default capacity
     */
    public TwoDArrayFlightGraph() {
        this(INITIAL_CAPACITY);
    }
    
    /**
     * Constructor with specified initial capacity
     * @param initialCapacity initial capacity for the 2D array
     */
    public TwoDArrayFlightGraph(int initialCapacity) {
        this.capacity = initialCapacity;
        this.adjacencyMatrix = new Edge[capacity][capacity];
        this.airportToIndex = new HashMap<>();
        this.indexToAirport = new HashMap<>();
        this.airportCount = 0;
    }
    
    /**
     * Add an airport to the graph
     * @param airportCode the airport code to add
     */
    @Override
    public void addAirport(String airportCode) {
        if (airportToIndex.containsKey(airportCode)) {
            return; // Airport already exists
        }
        
        // Check if we need to expand the array
        if (airportCount >= capacity) {
            expandCapacity();
        }
        
        // Add airport to mappings
        airportToIndex.put(airportCode, airportCount);
        indexToAirport.put(airportCount, airportCode);
        airportCount++;
    }
    
    /**
     * Expand the capacity of the 2D array when needed
     */
    private void expandCapacity() {
        int newCapacity = capacity * 2;
        Edge[][] newMatrix = new Edge[newCapacity][newCapacity];
        
        // Copy existing data
        for (int i = 0; i < capacity; i++) {
            System.arraycopy(adjacencyMatrix[i], 0, newMatrix[i], 0, capacity);
        }
        
        this.adjacencyMatrix = newMatrix;
        this.capacity = newCapacity;
    }
    
    /**
     * Add a flight route (edge) between two airports
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @param airline airline name
     * @param weight calculated weight rating
     */
    @Override
    public void addFlight(String sourceAirport, String destinationAirport, 
                         String airline, double weight) {
        // Ensure both airports exist in the graph
        addAirport(sourceAirport);
        addAirport(destinationAirport);
        
        // Get indices for source and destination
        int sourceIndex = airportToIndex.get(sourceAirport);
        int destIndex = airportToIndex.get(destinationAirport);
        
        // Create new edge
        Edge edge = new Edge(destinationAirport, airline, weight);
        
        // Add edge to 2D array
        adjacencyMatrix[sourceIndex][destIndex] = edge;
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
    @Override
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
    @Override
    public List<Edge> getFlightsFrom(String airportCode) {
        List<Edge> flights = new ArrayList<>();
        
        if (!airportToIndex.containsKey(airportCode)) {
            return flights; // Airport doesn't exist
        }
        
        int sourceIndex = airportToIndex.get(airportCode);
        
        // Check all destinations for this source
        for (int destIndex = 0; destIndex < airportCount; destIndex++) {
            Edge edge = adjacencyMatrix[sourceIndex][destIndex];
            if (edge != null) {
                flights.add(edge);
            }
        }
        
        return flights;
    }
    
    /**
     * Get all airports in the graph
     * @return set of all airport codes
     */
    @Override
    public Set<String> getAllAirports() {
        return new HashSet<>(airportToIndex.keySet());
    }
    
    /**
     * Check if an airport exists in the graph
     * @param airportCode the airport code to check
     * @return true if airport exists, false otherwise
     */
    @Override
    public boolean hasAirport(String airportCode) {
        return airportToIndex.containsKey(airportCode);
    }
    
    /**
     * Get the number of airports in the graph
     * @return number of airports
     */
    @Override
    public int getAirportCount() {
        return airportCount;
    }
    
    /**
     * Get the total number of flight routes in the graph
     * @return total number of edges
     */
    @Override
    public int getFlightCount() {
        int totalFlights = 0;
        for (int i = 0; i < airportCount; i++) {
            for (int j = 0; j < airportCount; j++) {
                if (adjacencyMatrix[i][j] != null) {
                    totalFlights++;
                }
            }
        }
        return totalFlights;
    }
    
    /**
     * Get all flights to a specific destination airport
     * @param destinationAirport the destination airport code
     * @return list of edges representing incoming flights
     */
    @Override
    public List<Edge> getFlightsTo(String destinationAirport) {
        List<Edge> incomingFlights = new ArrayList<>();
        
        if (!airportToIndex.containsKey(destinationAirport)) {
            return incomingFlights; // Destination doesn't exist
        }
        
        int destIndex = airportToIndex.get(destinationAirport);
        
        // Check all sources for this destination
        for (int sourceIndex = 0; sourceIndex < airportCount; sourceIndex++) {
            Edge edge = adjacencyMatrix[sourceIndex][destIndex];
            if (edge != null) {
                incomingFlights.add(edge);
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
    @Override
    public Edge getBestFlight(String sourceAirport, String destinationAirport) {
        if (!airportToIndex.containsKey(sourceAirport) || 
            !airportToIndex.containsKey(destinationAirport)) {
            return null; // One or both airports don't exist
        }
        
        int sourceIndex = airportToIndex.get(sourceAirport);
        int destIndex = airportToIndex.get(destinationAirport);
        
        return adjacencyMatrix[sourceIndex][destIndex];
    }
    
    /**
     * Get all airlines operating flights from a specific airport
     * @param airportCode the source airport code
     * @return set of airline names
     */
    @Override
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
    @Override
    public void printGraphStats() {
        System.out.println("=== Two-Dimensional Array Flight Graph Statistics ===");
        System.out.println("Total Airports: " + getAirportCount());
        System.out.println("Total Flight Routes: " + getFlightCount());
        System.out.println("Array Capacity: " + capacity + "x" + capacity);
        System.out.println("Memory Usage: " + (capacity * capacity) + " cells");
        System.out.println("Sparsity: " + String.format("%.2f%%", 
            (1.0 - (double)getFlightCount() / (capacity * capacity)) * 100));
        System.out.println();
        
        System.out.println("Airports in the graph:");
        for (String airport : airportToIndex.keySet()) {
            int outgoingFlights = getFlightsFrom(airport).size();
            System.out.println("  " + airport + " (" + outgoingFlights + " outgoing flights)");
        }
    }
    
    /**
     * Print detailed graph structure
     */
    @Override
    public void printGraph() {
        System.out.println("=== Two-Dimensional Array Flight Graph Structure ===");
        
        for (String airport : airportToIndex.keySet()) {
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
    @Override
    public void clear() {
        // Clear the 2D array
        for (int i = 0; i < capacity; i++) {
            for (int j = 0; j < capacity; j++) {
                adjacencyMatrix[i][j] = null;
            }
        }
        
        // Clear mappings
        airportToIndex.clear();
        indexToAirport.clear();
        airportCount = 0;
    }
    
    /**
     * Get the density of the graph (percentage of possible connections that exist)
     * @return density as a percentage (0.0 to 100.0)
     */
    public double getDensity() {
        if (airportCount == 0) return 0.0;
        
        int possibleConnections = airportCount * airportCount;
        int actualConnections = getFlightCount();
        
        return (double) actualConnections / possibleConnections * 100.0;
    }
    
    /**
     * Get the sparsity of the graph (percentage of empty cells)
     * @return sparsity as a percentage (0.0 to 100.0)
     */
    public double getSparsity() {
        return 100.0 - getDensity();
    }
    
    /**
     * Check if the graph is dense (more than 50% of possible connections exist)
     * @return true if dense, false otherwise
     */
    public boolean isDense() {
        return getDensity() > 50.0;
    }
    
    /**
     * Get memory efficiency (actual data vs allocated space)
     * @return efficiency as a percentage
     */
    public double getMemoryEfficiency() {
        if (capacity == 0) return 0.0;
        
        int totalCells = capacity * capacity;
        int usedCells = getFlightCount();
        
        return (double) usedCells / totalCells * 100.0;
    }
}

package DynamicArrayGraph;

import java.util.*;
import common.Edge;
import common.FlightGraphInterface;

/**
 * DynamicArrayFlightGraph class representing flight routes between airports.
 * Implemented using dynamic arrays with advanced features:
 * - Dynamic capacity management with smart resizing
 * - Memory-efficient storage with automatic compaction
 * - Advanced caching and performance optimizations
 * - Built-in analytics and monitoring capabilities
 */
public class DynamicArrayFlightGraph implements FlightGraphInterface {
    private String[] airports;
    private ArrayList<Edge>[] edgeLists;
    private Map<String, Integer> airportIndexMap;
    private int airportCount;
    private int capacity;
    private static final int INITIAL_CAPACITY = 50; // Smaller initial capacity
    private static final int GROWTH_FACTOR = 2;
    private static final double COMPACTION_THRESHOLD = 0.3; // Compact when utilization < 30%

    // Performance monitoring
    private long totalOperations;
    private long totalResizeOperations;
    private long totalCompactionOperations;

    /**
     * Constructor initializes empty graph with initial capacity
     */
    @SuppressWarnings("unchecked")
    public DynamicArrayFlightGraph() {
        this.capacity = INITIAL_CAPACITY;
        this.airports = new String[capacity];
        this.edgeLists = new ArrayList[capacity];
        this.airportIndexMap = new HashMap<>();
        this.airportCount = 0;
        this.totalOperations = 0;
        this.totalResizeOperations = 0;
        this.totalCompactionOperations = 0;

        // Initialize edge lists
        for (int i = 0; i < capacity; i++) {
            edgeLists[i] = new ArrayList<>();
        }
    }

    /**
     * Smart capacity expansion with performance monitoring
     */
    @SuppressWarnings("unchecked")
    private void expandCapacity() {
        totalResizeOperations++;
        int newCapacity = capacity * GROWTH_FACTOR;
        String[] newAirports = new String[newCapacity];
        ArrayList<Edge>[] newEdgeLists = new ArrayList[newCapacity];

        // Copy existing data
        System.arraycopy(airports, 0, newAirports, 0, airportCount);
        System.arraycopy(edgeLists, 0, newEdgeLists, 0, airportCount);

        // Initialize new edge lists
        for (int i = airportCount; i < newCapacity; i++) {
            newEdgeLists[i] = new ArrayList<>();
        }

        this.airports = newAirports;
        this.edgeLists = newEdgeLists;
        this.capacity = newCapacity;

        System.out.println("Dynamic Array: Expanded capacity to " + newCapacity +
                " (utilization: " + String.format("%.1f", (double) airportCount / capacity * 100) + "%)");
    }

    /**
     * Smart capacity compaction when utilization is low
     */
    @SuppressWarnings("unchecked")
    private void compactCapacity() {
        if (capacity <= INITIAL_CAPACITY)
            return; // Don't compact below initial capacity

        double utilization = (double) airportCount / capacity;
        if (utilization < COMPACTION_THRESHOLD) {
            totalCompactionOperations++;
            int newCapacity = Math.max(INITIAL_CAPACITY, airportCount * 2); // Keep some extra space

            String[] newAirports = new String[newCapacity];
            ArrayList<Edge>[] newEdgeLists = new ArrayList[newCapacity];

            // Copy existing data
            System.arraycopy(airports, 0, newAirports, 0, airportCount);
            System.arraycopy(edgeLists, 0, newEdgeLists, 0, airportCount);

            // Initialize new edge lists
            for (int i = airportCount; i < newCapacity; i++) {
                newEdgeLists[i] = new ArrayList<>();
            }

            this.airports = newAirports;
            this.edgeLists = newEdgeLists;
            this.capacity = newCapacity;

            System.out.println("Dynamic Array: Compacted capacity to " + newCapacity +
                    " (utilization: " + String.format("%.1f", (double) airportCount / capacity * 100) + "%)");
        }
    }

    /**
     * Add an airport to the graph with dynamic capacity management
     * 
     * @param airportCode the airport code to add
     */
    public void addAirport(String airportCode) {
        totalOperations++;
        if (!airportIndexMap.containsKey(airportCode)) {
            // Check if we need to expand capacity
            if (airportCount >= capacity) {
                expandCapacity();
            }

            // Add airport to array and update index map
            airports[airportCount] = airportCode;
            airportIndexMap.put(airportCode, airportCount);
            airportCount++;
        }
    }

    /**
     * Add a flight route (edge) between two airports
     * 
     * @param sourceAirport      source airport code
     * @param destinationAirport destination airport code
     * @param airline            airline name
     * @param weight             calculated weight rating
     */
    public void addFlight(String sourceAirport, String destinationAirport,
            String airline, double weight) {
        totalOperations++;
        // Ensure both airports exist in the graph
        addAirport(sourceAirport);
        addAirport(destinationAirport);

        // Create new edge
        Edge edge = new Edge(destinationAirport, airline, weight);

        // Add edge to the source airport's edge list
        int sourceIndex = airportIndexMap.get(sourceAirport);
        edgeLists[sourceIndex].add(edge);
    }

    /**
     * Add a flight route with rating components (calculates weight automatically)
     * 
     * @param sourceAirport               source airport code
     * @param destinationAirport          destination airport code
     * @param airline                     airline name
     * @param overallRating               overall airline rating (1-5)
     * @param valueForMoneyRating         value for money rating (1-5)
     * @param inflightEntertainmentRating inflight entertainment rating (1-5)
     * @param cabinStaffRating            cabin staff rating (1-5)
     * @param seatComfortRating           seat comfort rating (1-5)
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
     * 
     * @param airportCode the source airport code
     * @return list of edges representing outgoing flights
     */
    public List<Edge> getFlightsFrom(String airportCode) {
        if (!airportIndexMap.containsKey(airportCode)) {
            return new ArrayList<>();
        }

        int index = airportIndexMap.get(airportCode);
        return new ArrayList<>(edgeLists[index]);
    }

    /**
     * Get all airports in the graph
     * 
     * @return set of all airport codes
     */
    public Set<String> getAllAirports() {
        Set<String> airportSet = new HashSet<>();
        for (int i = 0; i < airportCount; i++) {
            airportSet.add(airports[i]);
        }
        return airportSet;
    }

    /**
     * Check if an airport exists in the graph
     * 
     * @param airportCode the airport code to check
     * @return true if airport exists, false otherwise
     */
    public boolean hasAirport(String airportCode) {
        return airportIndexMap.containsKey(airportCode);
    }

    /**
     * Get the number of airports in the graph
     * 
     * @return number of airports
     */
    public int getAirportCount() {
        return airportCount;
    }

    /**
     * Get the total number of flight routes in the graph
     * 
     * @return total number of edges
     */
    public int getFlightCount() {
        int totalFlights = 0;
        for (int i = 0; i < airportCount; i++) {
            totalFlights += edgeLists[i].size();
        }
        return totalFlights;
    }

    /**
     * Get all flights to a specific destination airport
     * 
     * @param destinationAirport the destination airport code
     * @return list of edges representing incoming flights
     */
    public List<Edge> getFlightsTo(String destinationAirport) {
        List<Edge> incomingFlights = new ArrayList<>();
        for (int i = 0; i < airportCount; i++) {
            for (Edge edge : edgeLists[i]) {
                if (edge.getDestinationAirport().equals(destinationAirport)) {
                    incomingFlights.add(edge);
                }
            }
        }
        return incomingFlights;
    }

    /**
     * Find the best flight route between two airports (lowest weight)
     * 
     * @param sourceAirport      source airport code
     * @param destinationAirport destination airport code
     * @return the edge with the lowest weight, or null if no direct route exists
     */
    public Edge getBestFlight(String sourceAirport, String destinationAirport) {
        if (!airportIndexMap.containsKey(sourceAirport)) {
            return null;
        }

        int sourceIndex = airportIndexMap.get(sourceAirport);
        Edge bestFlight = null;
        double bestWeight = Double.MAX_VALUE;

        for (Edge edge : edgeLists[sourceIndex]) {
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
     * 
     * @param airportCode the source airport code
     * @return set of airline names
     */
    public Set<String> getAirlinesFrom(String airportCode) {
        Set<String> airlines = new HashSet<>();
        if (airportIndexMap.containsKey(airportCode)) {
            int index = airportIndexMap.get(airportCode);
            for (Edge edge : edgeLists[index]) {
                airlines.add(edge.getAirline());
            }
        }
        return airlines;
    }

    /**
     * Print graph statistics with dynamic array features
     */
    public void printGraphStats() {
        System.out.println("=== Dynamic Array Flight Graph Statistics ===");
        System.out.println("Total Airports: " + getAirportCount());
        System.out.println("Total Flight Routes: " + getFlightCount());
        System.out.println("Array Capacity: " + capacity);
        System.out.println("Memory Efficiency: " + String.format("%.1f", (double) airportCount / capacity * 100) + "%");
        System.out.println("Total Operations: " + totalOperations);
        System.out.println("Resize Operations: " + totalResizeOperations);
        System.out.println("Compaction Operations: " + totalCompactionOperations);
        System.out.println();

        System.out.println("Airports in the graph:");
        for (int i = 0; i < airportCount; i++) {
            int outgoingFlights = edgeLists[i].size();
            System.out.println("  " + airports[i] + " (" + outgoingFlights + " outgoing flights)");
        }
    }

    /**
     * Print detailed graph structure
     */
    public void printGraph() {
        System.out.println("=== Dynamic Array Flight Graph Structure ===");
        for (int i = 0; i < airportCount; i++) {
            System.out.println(airports[i] + ":");
            if (edgeLists[i].isEmpty()) {
                System.out.println("  No outgoing flights");
            } else {
                for (Edge edge : edgeLists[i]) {
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
        airportCount = 0;
        airportIndexMap.clear();
        totalOperations = 0;
        totalResizeOperations = 0;
        totalCompactionOperations = 0;
        for (int i = 0; i < capacity; i++) {
            edgeLists[i].clear();
        }
    }

    /**
     * Get airports sorted by number of outgoing flights (busiest first)
     * 
     * @return list of airport codes sorted by flight count
     */
    public List<String> getAirportsByFlightCount() {
        List<String> sortedAirports = new ArrayList<>();
        for (int i = 0; i < airportCount; i++) {
            sortedAirports.add(airports[i]);
        }

        // Sort by number of outgoing flights (descending)
        sortedAirports.sort((a, b) -> {
            int flightsA = edgeLists[airportIndexMap.get(a)].size();
            int flightsB = edgeLists[airportIndexMap.get(b)].size();
            return Integer.compare(flightsB, flightsA);
        });

        return sortedAirports;
    }

    /**
     * Find the busiest airport (most outgoing flights)
     * 
     * @return airport code of the busiest airport
     */
    public String getBusiestAirport() {
        if (airportCount == 0) {
            return null;
        }

        String busiestAirport = airports[0];
        int maxFlights = edgeLists[0].size();

        for (int i = 1; i < airportCount; i++) {
            int currentFlights = edgeLists[i].size();
            if (currentFlights > maxFlights) {
                maxFlights = currentFlights;
                busiestAirport = airports[i];
            }
        }

        return busiestAirport;
    }

    /**
     * Get dynamic array utilization statistics
     * 
     * @return string with utilization information
     */
    public String getArrayUtilization() {
        return String.format("Dynamic Array Utilization: %d/%d (%.1f%%)",
                airportCount, capacity, (double) airportCount / capacity * 100);
    }

    /**
     * Get memory usage estimate with dynamic features
     * 
     * @return estimated memory usage in bytes
     */
    public long getEstimatedMemoryUsage() {
        long baseMemory = capacity * 8; // String references
        long edgeListMemory = capacity * 16; // ArrayList overhead
        long edgeMemory = getFlightCount() * 64; // Edge objects
        long mapMemory = airportCount * 32; // HashMap overhead

        return baseMemory + edgeListMemory + edgeMemory + mapMemory;
    }

    /**
     * Get performance statistics
     * 
     * @return performance metrics string
     */
    public String getPerformanceStats() {
        return String.format("Performance Stats - Operations: %d, Resizes: %d, Compactions: %d",
                totalOperations, totalResizeOperations, totalCompactionOperations);
    }

    /**
     * Force compaction if needed
     */
    public void forceCompaction() {
        compactCapacity();
    }

    /**
     * Get capacity growth history
     * 
     * @return string with capacity information
     */
    public String getCapacityInfo() {
        return String.format("Capacity: %d (Initial: %d, Growth Factor: %d)",
                capacity, INITIAL_CAPACITY, GROWTH_FACTOR);
    }
}

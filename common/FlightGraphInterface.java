package common;

import java.util.List;
import java.util.Set;

/**
 * Common interface for FlightGraph implementations
 * This interface defines the contract that both HashMap and LinkedList
 * implementations must follow
 */
public interface FlightGraphInterface {
    
    /**
     * Add an airport to the graph
     * @param airportCode the airport code to add
     */
    void addAirport(String airportCode);
    
    /**
     * Add a flight route (edge) between two airports
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @param airline airline name
     * @param weight calculated weight rating
     */
    void addFlight(String sourceAirport, String destinationAirport, 
                   String airline, double weight);
    
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
    void addFlightWithRatings(String sourceAirport, String destinationAirport,
                             String airline, double overallRating,
                             double valueForMoneyRating, double inflightEntertainmentRating,
                             double cabinStaffRating, double seatComfortRating);
    
    /**
     * Get all outgoing flights from a specific airport
     * @param airportCode the source airport code
     * @return list of edges representing outgoing flights
     */
    List<Edge> getFlightsFrom(String airportCode);
    
    /**
     * Get all airports in the graph
     * @return set of all airport codes
     */
    Set<String> getAllAirports();
    
    /**
     * Check if an airport exists in the graph
     * @param airportCode the airport code to check
     * @return true if airport exists, false otherwise
     */
    boolean hasAirport(String airportCode);
    
    /**
     * Get the number of airports in the graph
     * @return number of airports
     */
    int getAirportCount();
    
    /**
     * Get the total number of flight routes in the graph
     * @return total number of edges
     */
    int getFlightCount();
    
    /**
     * Get all flights to a specific destination airport
     * @param destinationAirport the destination airport code
     * @return list of edges representing incoming flights
     */
    List<Edge> getFlightsTo(String destinationAirport);
    
    /**
     * Find the best flight route between two airports (lowest weight)
     * @param sourceAirport source airport code
     * @param destinationAirport destination airport code
     * @return the edge with the lowest weight, or null if no direct route exists
     */
    Edge getBestFlight(String sourceAirport, String destinationAirport);
    
    /**
     * Get all airlines operating flights from a specific airport
     * @param airportCode the source airport code
     * @return set of airline names
     */
    Set<String> getAirlinesFrom(String airportCode);
    
    /**
     * Print graph statistics
     */
    void printGraphStats();
    
    /**
     * Print detailed graph structure
     */
    void printGraph();
    
    /**
     * Clear all data from the graph
     */
    void clear();
    
    // LinkedList-specific methods (optional - implementations can throw UnsupportedOperationException)
    
    /**
     * Get airports sorted by number of outgoing flights (busiest first)
     * @return list of airport codes sorted by flight count
     */
    default List<String> getAirportsByFlightCount() {
        throw new UnsupportedOperationException("This method is only supported by LinkedList implementation");
    }
    
    /**
     * Find the busiest airport (most outgoing flights)
     * @return airport code of the busiest airport
     */
    default String getBusiestAirport() {
        throw new UnsupportedOperationException("This method is only supported by LinkedList implementation");
    }
}

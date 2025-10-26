/**
 * Edge class represents a flight route between two airports.
 * Each edge stores airline information and calculated weight rating.
 */
public class Edge {
    private String destinationAirport;
    private String airline;
    private double weight;
    
    /**
     * Constructor for Edge
     * @param destinationAirport The destination airport code
     * @param airline The airline name operating this route
     * @param weight The calculated weight rating (1-5, where 1 is best)
     */
    public Edge(String destinationAirport, String airline, double weight) {
        this.destinationAirport = destinationAirport;
        this.airline = airline;
        this.weight = weight;
    }
    
    /**
     * Get the destination airport code
     * @return destination airport code
     */
    public String getDestinationAirport() {
        return destinationAirport;
    }
    
    /**
     * Get the airline name
     * @return airline name
     */
    public String getAirline() {
        return airline;
    }
    
    /**
     * Get the weight rating
     * @return weight rating (1-5, where 1 is best)
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * Set the weight rating
     * @param weight the new weight rating
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /**
     * Calculate weight from rating using the inverse mapping formula
     * Weight = 6 - Rating
     * @param rating the rating (1-5, where 5 is best)
     * @return the corresponding weight (1-5, where 1 is best)
     */
    public static double calculateWeightFromRating(double rating) {
        return 6.0 - rating;
    }
    
    /**
     * Calculate combined weight from multiple rating components
     * @param overallRating Overall airline rating (1-5)
     * @param valueForMoneyRating Value for money rating (1-5)
     * @param inflightEntertainmentRating Inflight entertainment rating (1-5)
     * @param cabinStaffRating Cabin staff rating (1-5)
     * @param seatComfortRating Seat comfort rating (1-5)
     * @return combined weight rating
     */
    public static double calculateCombinedWeight(double overallRating, 
                                                double valueForMoneyRating,
                                                double inflightEntertainmentRating,
                                                double cabinStaffRating,
                                                double seatComfortRating) {
        // Convert ratings to weights using inverse mapping
        double overallWeight = calculateWeightFromRating(overallRating);
        double valueForMoneyWeight = calculateWeightFromRating(valueForMoneyRating);
        double inflightEntertainmentWeight = calculateWeightFromRating(inflightEntertainmentRating);
        double cabinStaffWeight = calculateWeightFromRating(cabinStaffRating);
        double seatComfortWeight = calculateWeightFromRating(seatComfortRating);
        
        // Calculate weighted average
        double combinedWeight = (overallWeight * 0.4) +
                               (valueForMoneyWeight * 0.2) +
                               (inflightEntertainmentWeight * 0.1) +
                               (cabinStaffWeight * 0.1) +
                               (seatComfortWeight * 0.2);
        
        return combinedWeight;
    }
    
    @Override
    public String toString() {
        return String.format("Edge{to: %s, airline: %s, weight: %.2f}", 
                           destinationAirport, airline, weight);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return Double.compare(edge.weight, weight) == 0 &&
               destinationAirport.equals(edge.destinationAirport) &&
               airline.equals(edge.airline);
    }
    
    @Override
    public int hashCode() {
        return destinationAirport.hashCode() * 31 + airline.hashCode();
    }
}

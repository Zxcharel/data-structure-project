package graph;

/**
 * Represents an edge in the flight route graph.
 * Contains destination, airline, ratings, and calculated weight.
 */
public class Edge {
    private final String destination;
    private final String airline;
    private final int overallRating;
    private final int valueForMoney;
    private final int inflightEntertainment;
    private final int cabinStaff;
    private final int seatComfort;
    private final double weight;
    
    public Edge(String destination, String airline,
                int overallRating, int valueForMoney, int inflightEntertainment,
                int cabinStaff, int seatComfort, double weight) {
        this.destination = destination;
        this.airline = airline;
        this.overallRating = overallRating;
        this.valueForMoney = valueForMoney;
        this.inflightEntertainment = inflightEntertainment;
        this.cabinStaff = cabinStaff;
        this.seatComfort = seatComfort;
        this.weight = weight;
    }
    
    // Getters
    public String getDestination() { return destination; }
    public String getAirline() { return airline; }
    public int getOverallRating() { return overallRating; }
    public int getValueForMoney() { return valueForMoney; }
    public int getInflightEntertainment() { return inflightEntertainment; }
    public int getCabinStaff() { return cabinStaff; }
    public int getSeatComfort() { return seatComfort; }
    public double getWeight() { return weight; }
    
    @Override
    public String toString() {
        return String.format("Edge{to=%s, airline=%s, weight=%.3f, ratings=[%d,%d,%d,%d,%d]}",
                destination, airline, weight,
                overallRating, valueForMoney, inflightEntertainment, cabinStaff, seatComfort);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return destination.equals(edge.destination) && airline.equals(edge.airline);
    }
    
    @Override
    public int hashCode() {
        return destination.hashCode() * 31 + airline.hashCode();
    }
}

package data;

/**
 * Represents a single flight record from the CSV file.
 * Contains all the rating information for a specific airline route.
 */
public class FlightRecord {
    private final String airline;
    private final String originCountry;
    private final String destinationCountry;
    private final int overallRating;
    private final int valueForMoney;
    private final int inflightEntertainment;
    private final int cabinStaff;
    private final int seatComfort;
    
    public FlightRecord(String airline, String originCountry, String destinationCountry,
                       int overallRating, int valueForMoney, int inflightEntertainment,
                       int cabinStaff, int seatComfort) {
        this.airline = airline;
        this.originCountry = originCountry;
        this.destinationCountry = destinationCountry;
        this.overallRating = overallRating;
        this.valueForMoney = valueForMoney;
        this.inflightEntertainment = inflightEntertainment;
        this.cabinStaff = cabinStaff;
        this.seatComfort = seatComfort;
    }
    
    // Getters
    public String getAirline() { return airline; }
    public String getOriginCountry() { return originCountry; }
    public String getDestinationCountry() { return destinationCountry; }
    public int getOverallRating() { return overallRating; }
    public int getValueForMoney() { return valueForMoney; }
    public int getInflightEntertainment() { return inflightEntertainment; }
    public int getCabinStaff() { return cabinStaff; }
    public int getSeatComfort() { return seatComfort; }
    
    /**
     * Creates a key for grouping records by (origin, destination, airline)
     */
    public String getRouteKey() {
        return originCountry + "|" + destinationCountry + "|" + airline;
    }
    
    @Override
    public String toString() {
        return String.format("FlightRecord{airline=%s, route=%s->%s, ratings=[%d,%d,%d,%d,%d]}",
                airline, originCountry, destinationCountry,
                overallRating, valueForMoney, inflightEntertainment, cabinStaff, seatComfort);
    }
}

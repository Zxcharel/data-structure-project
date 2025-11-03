package src.data;

/**
 * Aggregates multiple flight records for the same route (origin, destination, airline).
 * Computes average ratings and provides methods for weight calculation.
 */
public class RouteAggregate {
    private final String originCode;
    private final String destinationCode;
    private final String airline;
    
    // Running totals for averaging
    private int overallSum = 0;
    private int valueForMoneySum = 0;
    private int inflightEntertainmentSum = 0;
    private int cabinStaffSum = 0;
    private int seatComfortSum = 0;
    private int count = 0;
    
    // Counters for missing ratings (0 values)
    private int overallMissing = 0;
    private int valueForMoneyMissing = 0;
    private int inflightEntertainmentMissing = 0;
    private int cabinStaffMissing = 0;
    private int seatComfortMissing = 0;
    
    public RouteAggregate(String originCode, String destinationCode, String airline) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.airline = airline;
    }
    
    /**
     * Adds a flight record to this aggregate
     */
    public void addRecord(FlightRecord record) {
        count++;
        
        if (record.getOverallRating() == 0) {
            overallMissing++;
        } else {
            overallSum += record.getOverallRating();
        }
        
        if (record.getValueForMoney() == 0) {
            valueForMoneyMissing++;
        } else {
            valueForMoneySum += record.getValueForMoney();
        }
        
        if (record.getInflightEntertainment() == 0) {
            inflightEntertainmentMissing++;
        } else {
            inflightEntertainmentSum += record.getInflightEntertainment();
        }
        
        if (record.getCabinStaff() == 0) {
            cabinStaffMissing++;
        } else {
            cabinStaffSum += record.getCabinStaff();
        }
        
        if (record.getSeatComfort() == 0) {
            seatComfortMissing++;
        } else {
            seatComfortSum += record.getSeatComfort();
        }
    }
    
    /**
     * Gets the average rating for overall, rounded to nearest integer
     */
    public int getAverageOverallRating() {
        int validCount = count - overallMissing;
        return validCount > 0 ? Math.round((float) overallSum / validCount) : 0;
    }
    
    /**
     * Gets the average rating for value for money, rounded to nearest integer
     */
    public int getAverageValueForMoney() {
        int validCount = count - valueForMoneyMissing;
        return validCount > 0 ? Math.round((float) valueForMoneySum / validCount) : 0;
    }
    
    /**
     * Gets the average rating for inflight entertainment, rounded to nearest integer
     */
    public int getAverageInflightEntertainment() {
        int validCount = count - inflightEntertainmentMissing;
        return validCount > 0 ? Math.round((float) inflightEntertainmentSum / validCount) : 0;
    }
    
    /**
     * Gets the average rating for cabin staff, rounded to nearest integer
     */
    public int getAverageCabinStaff() {
        int validCount = count - cabinStaffMissing;
        return validCount > 0 ? Math.round((float) cabinStaffSum / validCount) : 0;
    }
    
    /**
     * Gets the average rating for seat comfort, rounded to nearest integer
     */
    public int getAverageSeatComfort() {
        int validCount = count - seatComfortMissing;
        return validCount > 0 ? Math.round((float) seatComfortSum / validCount) : 0;
    }
    
    /**
     * Calculates the edge weight using the specified formula.
     * Handles missing ratings by dropping them and re-normalizing weights.
     */
    public double calculateWeight() {
        int overall = getAverageOverallRating();
        int valueForMoney = getAverageValueForMoney();
        int inflightEntertainment = getAverageInflightEntertainment();
        int cabinStaff = getAverageCabinStaff();
        int seatComfort = getAverageSeatComfort();
        
        // Convert ratings to weights (6 - rating)
        double overallWeight = overall > 0 ? 6.0 - overall : 0;
        double valueForMoneyWeight = valueForMoney > 0 ? 6.0 - valueForMoney : 0;
        double inflightEntertainmentWeight = inflightEntertainment > 0 ? 6.0 - inflightEntertainment : 0;
        double cabinStaffWeight = cabinStaff > 0 ? 6.0 - cabinStaff : 0;
        double seatComfortWeight = seatComfort > 0 ? 6.0 - seatComfort : 0;
        
        // Count how many components are present
        int presentComponents = 0;
        if (overall > 0) presentComponents++;
        if (valueForMoney > 0) presentComponents++;
        if (inflightEntertainment > 0) presentComponents++;
        if (cabinStaff > 0) presentComponents++;
        if (seatComfort > 0) presentComponents++;
        
        // If all components are missing, use fallback
        if (presentComponents == 0) {
            return 3.0;
        }
        
        // Calculate weighted sum with original coefficients
        double weightedSum = (overallWeight * 0.4) + 
                            (valueForMoneyWeight * 0.2) + 
                            (inflightEntertainmentWeight * 0.1) + 
                            (cabinStaffWeight * 0.1) + 
                            (seatComfortWeight * 0.2);
        
        // Calculate sum of present coefficients for normalization
        double presentCoefficients = 0;
        if (overall > 0) presentCoefficients += 0.4;
        if (valueForMoney > 0) presentCoefficients += 0.2;
        if (inflightEntertainment > 0) presentCoefficients += 0.1;
        if (cabinStaff > 0) presentCoefficients += 0.1;
        if (seatComfort > 0) presentCoefficients += 0.2;
        
        // Re-normalize by the sum of present coefficients
        return presentCoefficients > 0 ? weightedSum / presentCoefficients : 3.0;
    }
    
    // Getters
    public String getOriginCode() { return originCode; }
    public String getDestinationCode() { return destinationCode; }
    public String getAirline() { return airline; }
    public int getCount() { return count; }
    
    @Override
    public String toString() {
        return String.format("RouteAggregate{route=%s->%s, airline=%s, count=%d, avgRatings=[%d,%d,%d,%d,%d], weight=%.3f}",
                originCode, destinationCode, airline, count,
                getAverageOverallRating(), getAverageValueForMoney(), getAverageInflightEntertainment(),
                getAverageCabinStaff(), getAverageSeatComfort(), calculateWeight());
    }
}

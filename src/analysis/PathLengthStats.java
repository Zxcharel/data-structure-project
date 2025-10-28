package src.analysis;

/**
 * Path length statistics
 */
public class PathLengthStats {
    private final int minLength;
    private final int maxLength;
    private final double avgLength;
    private final int medianLength;
    
    public PathLengthStats(int minLength, int maxLength, double avgLength, int medianLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.avgLength = avgLength;
        this.medianLength = medianLength;
    }
    
    // Getters
    public int getMinLength() { return minLength; }
    public int getMaxLength() { return maxLength; }
    public double getAvgLength() { return avgLength; }
    public int getMedianLength() { return medianLength; }
    
    @Override
    public String toString() {
        return String.format("Min: %d, Max: %d, Avg: %.2f, Median: %d",
                           minLength, maxLength, avgLength, medianLength);
    }
}

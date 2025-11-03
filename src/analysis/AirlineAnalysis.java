package src.analysis;

import java.util.Map;
import java.util.Set;

/**
 * Airline analysis results
 */
public class AirlineAnalysis {
    private final Map<String, Integer> routeCounts;
    private final Map<String, Set<String>> airlineRoutes;
    private final Map<String, Double> avgWeights;
    private final String mostPopular;
    private final String leastPopular;
    private final String bestRated;
    private final String worstRated;
    
    public AirlineAnalysis(Map<String, Integer> routeCounts, Map<String, Set<String>> airlineRoutes,
                          Map<String, Double> avgWeights, String mostPopular, String leastPopular,
                          String bestRated, String worstRated) {
        this.routeCounts = routeCounts;
        this.airlineRoutes = airlineRoutes;
        this.avgWeights = avgWeights;
        this.mostPopular = mostPopular;
        this.leastPopular = leastPopular;
        this.bestRated = bestRated;
        this.worstRated = worstRated;
    }
    
    // Getters
    public Map<String, Integer> getRouteCounts() { return routeCounts; }
    public Map<String, Set<String>> getAirlineRoutes() { return airlineRoutes; }
    public Map<String, Double> getAvgWeights() { return avgWeights; }
    public String getMostPopular() { return mostPopular; }
    public String getLeastPopular() { return leastPopular; }
    public String getBestRated() { return bestRated; }
    public String getWorstRated() { return worstRated; }
    
    @Override
    public String toString() {
        return String.format(
            "Airline Analysis:\n" +
            "  Most Popular: %s (%d routes)\n" +
            "  Least Popular: %s (%d routes)\n" +
            "  Best Rated: %s (avg weight: %.3f)\n" +
            "  Worst Rated: %s (avg weight: %.3f)\n" +
            "  Total Airlines: %d",
            mostPopular, routeCounts.getOrDefault(mostPopular, 0),
            leastPopular, routeCounts.getOrDefault(leastPopular, 0),
            bestRated, avgWeights.getOrDefault(bestRated, 0.0),
            worstRated, avgWeights.getOrDefault(worstRated, 0.0),
            routeCounts.size()
        );
    }
}

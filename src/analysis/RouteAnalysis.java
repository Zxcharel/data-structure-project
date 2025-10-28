package src.analysis;

import java.util.Map;

/**
 * Route analysis results
 */
public class RouteAnalysis {
    private final Map<String, Integer> countryConnections;
    private final Map<String, Double> countryWeights;
    private final String biggestHub;
    private final String bestRoutes;
    private final PathLengthStats pathStats;
    
    public RouteAnalysis(Map<String, Integer> countryConnections, Map<String, Double> countryWeights,
                        String biggestHub, String bestRoutes, PathLengthStats pathStats) {
        this.countryConnections = countryConnections;
        this.countryWeights = countryWeights;
        this.biggestHub = biggestHub;
        this.bestRoutes = bestRoutes;
        this.pathStats = pathStats;
    }
    
    // Getters
    public Map<String, Integer> getCountryConnections() { return countryConnections; }
    public Map<String, Double> getCountryWeights() { return countryWeights; }
    public String getBiggestHub() { return biggestHub; }
    public String getBestRoutes() { return bestRoutes; }
    public PathLengthStats getPathStats() { return pathStats; }
    
    @Override
    public String toString() {
        return String.format(
            "Route Analysis:\n" +
            "  Biggest Hub: %s (%d connections)\n" +
            "  Best Routes: %s (avg weight: %.3f)\n" +
            "  Path Length Stats: %s",
            biggestHub, countryConnections.getOrDefault(biggestHub, 0),
            bestRoutes, countryWeights.getOrDefault(bestRoutes, 0.0),
            pathStats.toString()
        );
    }
}

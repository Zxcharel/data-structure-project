package src.algo;

import java.util.List;

/**
 * Represents the result of a pathfinding algorithm.
 * Contains the path, airlines used, total weight, and algorithm statistics.
 */
public class PathResult {
    private final List<String> countries;
    private final List<String> airlines;
    private final double totalWeight;
    private final int nodesVisited;
    private final int edgesRelaxed;
    private final long runtimeMs;
    private final boolean found;
    
    public PathResult(List<String> countries, List<String> airlines, double totalWeight,
                     int nodesVisited, int edgesRelaxed, long runtimeMs, boolean found) {
        this.countries = countries;
        this.airlines = airlines;
        this.totalWeight = totalWeight;
        this.nodesVisited = nodesVisited;
        this.edgesRelaxed = edgesRelaxed;
        this.runtimeMs = runtimeMs;
        this.found = found;
    }
    
    // Getters
    public List<String> getCountries() { return countries; }
    public List<String> getAirlines() { return airlines; }
    public double getTotalWeight() { return totalWeight; }
    public int getNodesVisited() { return nodesVisited; }
    public int getEdgesRelaxed() { return edgesRelaxed; }
    public long getRuntimeMs() { return runtimeMs; }
    public boolean isFound() { return found; }
    
    /**
     * Gets the path length (number of hops)
     */
    public int getPathLength() {
        return countries != null ? countries.size() - 1 : 0;
    }
    
    /**
     * Formats the path as a string (e.g., "SG → AE → IT")
     */
    public String getPathString() {
        if (countries == null || countries.isEmpty()) {
            return "No path";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < countries.size(); i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append(countries.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Formats the airlines as a string (e.g., "[SQ, EK]")
     */
    public String getAirlinesString() {
        if (airlines == null || airlines.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < airlines.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(airlines.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Formats a detailed summary of the result
     */
    public String getDetailedSummary() {
        if (!found) {
            return "No route found.";
        }
        
        return String.format(
            "Route found: %s%n" +
            "Airlines: %s%n" +
            "Total weight: %.3f%n" +
            "Path length: %d hops%n" +
            "Nodes visited: %d%n" +
            "Edges relaxed: %d%n" +
            "Runtime: %d ms",
            getPathString(),
            getAirlinesString(),
            totalWeight,
            getPathLength(),
            nodesVisited,
            edgesRelaxed,
            runtimeMs
        );
    }
    
    @Override
    public String toString() {
        return String.format("PathResult{found=%s, path=%s, weight=%.3f, runtime=%dms}",
                found, getPathString(), totalWeight, runtimeMs);
    }
}

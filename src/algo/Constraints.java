package src.algo;

import src.graph.Edge;
import src.graph.Graph;
import java.util.*;

/**
 * Constraints for pathfinding algorithms.
 * Allows filtering by max stops, airline allowlist, and airline blocklist.
 */
public class Constraints {
    private final int maxStops;
    private final Set<String> airlineAllowlist;
    private final Set<String> airlineBlocklist;
    
    public Constraints(int maxStops, Set<String> airlineAllowlist, Set<String> airlineBlocklist) {
        this.maxStops = maxStops;
        this.airlineAllowlist = airlineAllowlist != null ? airlineAllowlist : new HashSet<>();
        this.airlineBlocklist = airlineBlocklist != null ? airlineBlocklist : new HashSet<>();
    }
    
    public Constraints() {
        this(Integer.MAX_VALUE, null, null);
    }
    
    /**
     * Checks if an edge satisfies the constraints
     */
    public boolean isEdgeAllowed(Edge edge, int currentStops) {
        // Check max stops constraint
        if (currentStops >= maxStops) {
            return false;
        }
        
        // Check airline allowlist (if not empty, only allowlisted airlines are permitted)
        if (!airlineAllowlist.isEmpty() && !airlineAllowlist.contains(edge.getAirline())) {
            return false;
        }
        
        // Check airline blocklist
        if (airlineBlocklist.contains(edge.getAirline())) {
            return false;
        }
        
        return true;
    }
    
    // Getters
    public int getMaxStops() { return maxStops; }
    public Set<String> getAirlineAllowlist() { return airlineAllowlist; }
    public Set<String> getAirlineBlocklist() { return airlineBlocklist; }
    
    @Override
    public String toString() {
        return String.format("Constraints{maxStops=%d, allowlist=%s, blocklist=%s}",
                maxStops, airlineAllowlist, airlineBlocklist);
    }
}

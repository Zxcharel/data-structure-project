package src.analysis;

/**
 * Centrality metrics for a node
 */
public class CentralityMetrics {
    private final int degree;
    private final int betweenness;
    private final int closeness;
    
    public CentralityMetrics(int degree, int betweenness, int closeness) {
        this.degree = degree;
        this.betweenness = betweenness;
        this.closeness = closeness;
    }
    
    // Getters
    public int getDegree() { return degree; }
    public int getBetweenness() { return betweenness; }
    public int getCloseness() { return closeness; }
    
    @Override
    public String toString() {
        return String.format("Degree: %d, Betweenness: %d, Closeness: %d", 
                           degree, betweenness, closeness);
    }
}

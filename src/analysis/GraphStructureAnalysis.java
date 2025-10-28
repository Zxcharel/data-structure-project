package src.analysis;

/**
 * Graph structure analysis results
 */
public class GraphStructureAnalysis {
    private final int totalNodes;
    private final int totalEdges;
    private final double avgDegree;
    private final double density;
    private final int numComponents;
    private final int largestComponentSize;
    private final double clusteringCoefficient;
    
    public GraphStructureAnalysis(int totalNodes, int totalEdges, double avgDegree, 
                                 double density, int numComponents, int largestComponentSize,
                                 double clusteringCoefficient) {
        this.totalNodes = totalNodes;
        this.totalEdges = totalEdges;
        this.avgDegree = avgDegree;
        this.density = density;
        this.numComponents = numComponents;
        this.largestComponentSize = largestComponentSize;
        this.clusteringCoefficient = clusteringCoefficient;
    }
    
    // Getters
    public int getTotalNodes() { return totalNodes; }
    public int getTotalEdges() { return totalEdges; }
    public double getAvgDegree() { return avgDegree; }
    public double getDensity() { return density; }
    public int getNumComponents() { return numComponents; }
    public int getLargestComponentSize() { return largestComponentSize; }
    public double getClusteringCoefficient() { return clusteringCoefficient; }
    
    @Override
    public String toString() {
        return String.format(
            "Graph Structure Analysis:\n" +
            "  Total Nodes: %d\n" +
            "  Total Edges: %d\n" +
            "  Average Degree: %.2f\n" +
            "  Density: %.4f\n" +
            "  Components: %d\n" +
            "  Largest Component: %d nodes\n" +
            "  Clustering Coefficient: %.4f",
            totalNodes, totalEdges, avgDegree, density,
            numComponents, largestComponentSize, clusteringCoefficient
        );
    }
}

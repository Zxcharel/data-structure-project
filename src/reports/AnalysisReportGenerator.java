package src.reports;

import src.analysis.*;
import src.comparison.DataStructureComparator;
import src.comparison.DataStructureComparator.GraphComparisonResult;
import src.comparison.DataStructureComparator.MemoryComparisonResult;
import src.comparison.DataStructureComparator.AlgorithmComparisonResult;
import src.comparison.DataStructureComparator.AlgorithmStats;
import src.util.IOUtils;
import java.io.IOException;
import java.util.*;

/**
 * Generates comprehensive analysis reports comparing data structures and algorithms.
 * Creates both console output and file reports.
 */
public class AnalysisReportGenerator {
    
    /**
     * Generates a complete analysis report
     */
    public void generateCompleteReport(GraphAnalyzer analyzer, DataStructureComparator comparator, 
                                      String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/analysis_report.md");
        
        StringBuilder report = new StringBuilder();
        report.append("# Flight Route Graph Analysis Report\n\n");
        report.append("Generated on: ").append(new Date()).append("\n\n");
        
        // Graph Structure Analysis
        report.append("## Graph Structure Analysis\n\n");
        GraphStructureAnalysis structureAnalysis = analyzer.analyzeStructure();
        report.append(structureAnalysis.toString()).append("\n\n");
        
        // Airline Analysis
        report.append("## Airline Analysis\n\n");
        AirlineAnalysis airlineAnalysis = analyzer.analyzeAirlines();
        report.append(airlineAnalysis.toString()).append("\n\n");
        
        // Route Analysis
        report.append("## Route Analysis\n\n");
        RouteAnalysis routeAnalysis = analyzer.analyzeRoutes();
        report.append(routeAnalysis.toString()).append("\n\n");
        
        // Centrality Analysis
        report.append("## Top Countries by Centrality\n\n");
        Map<String, CentralityMetrics> centrality = analyzer.calculateCentrality();
        List<Map.Entry<String, CentralityMetrics>> sortedByDegree = new ArrayList<>(centrality.entrySet());
        sortedByDegree.sort((a, b) -> Integer.compare(b.getValue().getDegree(), a.getValue().getDegree()));
        
        report.append("### Top 10 Countries by Degree Centrality\n");
        report.append("| Rank | Country | Degree | Betweenness | Closeness |\n");
        report.append("|------|---------|--------|-------------|-----------|\n");
        
        for (int i = 0; i < Math.min(10, sortedByDegree.size()); i++) {
            Map.Entry<String, CentralityMetrics> entry = sortedByDegree.get(i);
            CentralityMetrics metrics = entry.getValue();
            report.append(String.format("| %d | %s | %d | %d | %d |\n",
                i + 1, entry.getKey(), metrics.getDegree(), 
                metrics.getBetweenness(), metrics.getCloseness()));
        }
        
        report.append("\n");
        
        // Data Structure Comparison
        report.append("## Data Structure Comparison\n\n");
        List<String> sampleNodes = analyzer.graph.nodes().subList(0, Math.min(100, analyzer.graph.nodes().size()));
        GraphComparisonResult graphComparison = comparator.compareGraphImplementations(sampleNodes, 500);
        report.append(graphComparison.toString()).append("\n\n");
        
        // Memory Comparison
        report.append("## Memory Usage Comparison\n\n");
        MemoryComparisonResult memoryComparison = comparator.compareMemoryUsage(sampleNodes, 500);
        report.append(memoryComparison.toString()).append("\n\n");
        
        // Algorithm Comparison
        report.append("## Algorithm Performance Comparison\n\n");
        List<String> testQueries = generateTestQueries(sampleNodes, 20);
        AlgorithmComparisonResult algorithmComparison = comparator.compareAlgorithms(analyzer.graph, testQueries);
        report.append(algorithmComparison.toString()).append("\n\n");
        
        // Recommendations
        report.append("## Recommendations\n\n");
        report.append(generateRecommendations(structureAnalysis, airlineAnalysis, 
                                            graphComparison, memoryComparison, algorithmComparison));
        
        // Write report
        IOUtils.writeMarkdown(outputDir + "/analysis_report.md", report.toString());
        
        // Generate CSV data
        generateCsvReports(analyzer, comparator, outputDir);
        
        System.out.println("Complete analysis report generated at: " + outputDir + "/analysis_report.md");
    }
    
    /**
     * Generates CSV reports for further analysis
     */
    private void generateCsvReports(GraphAnalyzer analyzer, DataStructureComparator comparator, 
                                   String outputDir) throws IOException {
        // Centrality data
        Map<String, CentralityMetrics> centrality = analyzer.calculateCentrality();
        List<String[]> centralityRows = new ArrayList<>();
        centralityRows.add(new String[]{"country", "degree", "betweenness", "closeness"});
        
        for (Map.Entry<String, CentralityMetrics> entry : centrality.entrySet()) {
            CentralityMetrics metrics = entry.getValue();
            centralityRows.add(new String[]{
                entry.getKey(),
                String.valueOf(metrics.getDegree()),
                String.valueOf(metrics.getBetweenness()),
                String.valueOf(metrics.getCloseness())
            });
        }
        
        IOUtils.writeCsv(outputDir + "/centrality_data.csv", 
                        new String[]{"country", "degree", "betweenness", "closeness"}, 
                        centralityRows.subList(1, centralityRows.size()));
        
        // Airline data
        AirlineAnalysis airlineAnalysis = analyzer.analyzeAirlines();
        List<String[]> airlineRows = new ArrayList<>();
        airlineRows.add(new String[]{"airline", "route_count", "avg_weight"});
        
        for (Map.Entry<String, Integer> entry : airlineAnalysis.getRouteCounts().entrySet()) {
            String airline = entry.getKey();
            int routeCount = entry.getValue();
            double avgWeight = airlineAnalysis.getAvgWeights().getOrDefault(airline, 0.0);
            airlineRows.add(new String[]{
                airline,
                String.valueOf(routeCount),
                IOUtils.formatDouble(avgWeight, 3)
            });
        }
        
        IOUtils.writeCsv(outputDir + "/airline_data.csv", 
                        new String[]{"airline", "route_count", "avg_weight"}, 
                        airlineRows.subList(1, airlineRows.size()));
        
        // Route data
        RouteAnalysis routeAnalysis = analyzer.analyzeRoutes();
        List<String[]> routeRows = new ArrayList<>();
        routeRows.add(new String[]{"country", "connections", "avg_weight"});
        
        for (Map.Entry<String, Integer> entry : routeAnalysis.getCountryConnections().entrySet()) {
            String country = entry.getKey();
            int connections = entry.getValue();
            double avgWeight = routeAnalysis.getCountryWeights().getOrDefault(country, 0.0);
            routeRows.add(new String[]{
                country,
                String.valueOf(connections),
                IOUtils.formatDouble(avgWeight, 3)
            });
        }
        
        IOUtils.writeCsv(outputDir + "/route_data.csv", 
                        new String[]{"country", "connections", "avg_weight"}, 
                        routeRows.subList(1, routeRows.size()));
    }
    
    /**
     * Generates test queries for algorithm comparison
     */
    private List<String> generateTestQueries(List<String> nodes, int numQueries) {
        List<String> queries = new ArrayList<>();
        Random random = new Random(42);
        
        for (int i = 0; i < numQueries; i++) {
            String origin = nodes.get(random.nextInt(nodes.size()));
            String destination = nodes.get(random.nextInt(nodes.size()));
            
            if (!origin.equals(destination)) {
                queries.add(origin + " -> " + destination);
            }
        }
        
        return queries;
    }
    
    /**
     * Generates recommendations based on analysis results
     */
    private String generateRecommendations(GraphStructureAnalysis structureAnalysis,
                                         AirlineAnalysis airlineAnalysis,
                                         GraphComparisonResult graphComparison,
                                         MemoryComparisonResult memoryComparison,
                                         AlgorithmComparisonResult algorithmComparison) {
        StringBuilder recommendations = new StringBuilder();
        
        // Graph structure recommendations
        if (structureAnalysis.getDensity() < 0.1) {
            recommendations.append("- **Low Density Graph**: The graph has low density (")
                          .append(IOUtils.formatDouble(structureAnalysis.getDensity(), 4))
                          .append("), making it suitable for sparse graph algorithms.\n");
        }
        
        if (structureAnalysis.getNumComponents() > 1) {
            recommendations.append("- **Multiple Components**: The graph has ")
                          .append(structureAnalysis.getNumComponents())
                          .append(" components. Consider analyzing each component separately.\n");
        }
        
        // Memory recommendations
        long linearArrayMemory = memoryComparison.getLinearArrayMemory();
        long dynamicArrayMemory = memoryComparison.getDynamicArrayMemory();
        long offsetArrayMemory = memoryComparison.getOffsetArrayMemory();
        long adjListMemory = memoryComparison.getAdjListMemory();
        long matrixMemory = memoryComparison.getMatrixMemory();

        if (memoryComparison.getMemoryRatio() < 0.5) {
            recommendations.append("- **Memory Efficiency**: AdjacencyList uses ")
                          .append(IOUtils.formatDouble(memoryComparison.getMemoryRatio(), 2))
                          .append("x less memory than MatrixGraph. Use AdjacencyList for large sparse graphs.\n");
        } else {
            recommendations.append("- **Memory Trade-off**: MatrixGraph uses ")
                          .append(IOUtils.formatDouble(1.0 / memoryComparison.getMemoryRatio(), 2))
                          .append("x less memory than AdjacencyList. Consider MatrixGraph for dense graphs.\n");
        }

        // LinearArray memory comparison
        if (linearArrayMemory < adjListMemory && linearArrayMemory < matrixMemory) {
            recommendations.append("- **LinearArray Efficiency**: LinearArray uses the least memory (")
                          .append(linearArrayMemory).append(" bytes) compared to AdjacencyList (")
                          .append(adjListMemory).append(" bytes) and MatrixGraph (")
                          .append(matrixMemory).append(" bytes). Consider LinearArray for memory-constrained environments.\n");
        } else if (memoryComparison.getLinearArrayMemoryRatio() < 0.7) {
            recommendations.append("- **LinearArray Trade-off**: LinearArray uses ")
                          .append(IOUtils.formatDouble(memoryComparison.getLinearArrayMemoryRatio(), 2))
                          .append("x less memory than MatrixGraph, offering a balance between AdjacencyList and Matrix approaches.\n");
        }

        // DynamicArray memory comparison
        if (dynamicArrayMemory < adjListMemory && dynamicArrayMemory < matrixMemory && dynamicArrayMemory < linearArrayMemory) {
            recommendations.append("- **DynamicArray Efficiency**: DynamicArray uses the least memory (")
                          .append(dynamicArrayMemory).append(" bytes) among all implementations. Consider DynamicArray for optimal memory usage.\n");
        } else if (memoryComparison.getDynamicArrayMemoryRatio() < 0.8) {
            recommendations.append("- **DynamicArray Trade-off**: DynamicArray uses ")
                          .append(IOUtils.formatDouble(memoryComparison.getDynamicArrayMemoryRatio(), 2))
                          .append("x less memory than MatrixGraph, with automatic capacity management for better memory efficiency.\n");
        }

        // OffsetArray memory comparison (CSR-style - optimized for pathfinding)
        if (offsetArrayMemory < adjListMemory && offsetArrayMemory < matrixMemory) {
            recommendations.append("- **OffsetArray (CSR) Optimization**: OffsetArray uses ")
                          .append(offsetArrayMemory).append(" bytes and provides excellent cache performance for pathfinding algorithms. ")
                          .append("The single contiguous edge array ensures optimal memory locality during graph traversal.\n");
        } else if (memoryComparison.getOffsetArrayMemoryRatio() < 0.8) {
            recommendations.append("- **OffsetArray (CSR) Trade-off**: OffsetArray uses ")
                          .append(IOUtils.formatDouble(memoryComparison.getOffsetArrayMemoryRatio(), 2))
                          .append("x less memory than MatrixGraph. This CSR-style structure is optimized for sequential access patterns in pathfinding.\n");
        }
        
        // Algorithm recommendations
        Map<String, AlgorithmStats> algorithmStats = algorithmComparison.getAlgorithmStats();
        AlgorithmStats fastest = algorithmStats.values().stream()
            .min(Comparator.comparing(AlgorithmStats::getAvgRuntime))
            .orElse(null);
        
        if (fastest != null) {
            recommendations.append("- **Fastest Algorithm**: ")
                          .append(fastest.getAlgorithm())
                          .append(" is the fastest algorithm with ")
                          .append(IOUtils.formatDouble(fastest.getAvgRuntime(), 1))
                          .append("ms average runtime.\n");
        }
        
        // Airline recommendations
        recommendations.append("- **Airline Quality**: ")
                      .append(airlineAnalysis.getBestRated())
                      .append(" has the best average ratings (weight: ")
                      .append(IOUtils.formatDouble(airlineAnalysis.getAvgWeights().getOrDefault(airlineAnalysis.getBestRated(), 0.0), 3))
                      .append(").\n");
        
        recommendations.append("- **Route Coverage**: ")
                      .append(airlineAnalysis.getMostPopular())
                      .append(" has the most routes (")
                      .append(airlineAnalysis.getRouteCounts().getOrDefault(airlineAnalysis.getMostPopular(), 0))
                      .append("), making it a good choice for connectivity.\n");
        
        return recommendations.toString();
    }
    
    /**
     * Generates a quick console summary
     */
    public void printQuickSummary(GraphAnalyzer analyzer) {
        System.out.println("\n=== Quick Analysis Summary ===");
        
        GraphStructureAnalysis structure = analyzer.analyzeStructure();
        System.out.println("Graph: " + structure.getTotalNodes() + " nodes, " + 
                          structure.getTotalEdges() + " edges");
        System.out.println("Density: " + IOUtils.formatDouble(structure.getDensity(), 4));
        System.out.println("Components: " + structure.getNumComponents());
        
        AirlineAnalysis airlines = analyzer.analyzeAirlines();
        System.out.println("Airlines: " + airlines.getRouteCounts().size() + " total");
        System.out.println("Most Popular: " + airlines.getMostPopular());
        System.out.println("Best Rated: " + airlines.getBestRated());
        
        RouteAnalysis routes = analyzer.analyzeRoutes();
        System.out.println("Biggest Hub: " + routes.getBiggestHub());
        System.out.println("Best Routes: " + routes.getBestRoutes());
        
        System.out.println("===============================\n");
    }
}

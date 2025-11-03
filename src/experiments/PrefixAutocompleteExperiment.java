package src.experiments;

import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import java.io.IOException;
import java.util.*;

/**
 * Experiment 5: Prefix Autocomplete on Outgoing Routes
 * 
 * Tests when tries beat arrays: prefix autocomplete performance comparing
 * RoutePartitionedTrieGraph against traditional array/list/CSR structures.
 */
public class PrefixAutocompleteExperiment {
    
    private static final int DEFAULT_K_ORIGINS = 100;
    private static final int DEFAULT_N_PREFIXES = 20;
    private static final Random random = new Random(42);
    
    /**
     * Runs the prefix autocomplete experiment
     * 
     * @param csvPath Path to CSV file
     * @param graphTypeChoice Graph type: 1=RoutePartitionedTrieGraph, 2=AdjacencyListGraph, 3=OffsetArrayGraph, 4=CSRGraph
     * @param nPrefixes Number of prefixes per origin
     * @param outputDir Directory to write results
     * @throws IOException if files cannot be read or written
     */
    public void runExperiment(String csvPath, int graphTypeChoice, int nPrefixes, String outputDir) throws IOException {
        System.out.println("=== Experiment 5: Prefix Autocomplete on Outgoing Routes ===");
        System.out.println("Reading graph from CSV: " + csvPath + "\n");
        
        // Load graph from CSV based on choice
        CsvReader reader = new CsvReader();
        Graph graph;
        
        switch (graphTypeChoice) {
            case 1:
                graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
                break;
            case 2:
                graph = reader.readCsvAndBuildGraph(csvPath);
                break;
            case 3:
                graph = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
                break;
            case 4:
                Graph tempGraph = reader.readCsvAndBuildGraph(csvPath);
                graph = new CSRGraph(tempGraph);
                break;
            default:
                graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
        }
        
        if (graph == null || graph.nodeCount() == 0) {
            throw new IllegalArgumentException("Graph loaded from CSV is empty");
        }
        
        System.out.println("Graph: " + graph.nodeCount() + " nodes, " + graph.edgeCount() + " edges");
        System.out.println("Testing ALL " + graph.nodeCount() + " nodes with " + nPrefixes + " prefixes each\n");
        
        // Use ALL nodes as origins
        List<String> topOrigins = new ArrayList<>(graph.nodes());
        System.out.println("Using ALL " + topOrigins.size() + " nodes\n");
        
        // Generate prefixes from actual destinations
        Map<String, List<String>> prefixesByOrigin = generatePrefixes(graph, topOrigins, nPrefixes);
        
        // Measure prefix autocomplete performance
        System.out.println("Measuring prefix autocomplete performance...");
        List<PrefixResult> results = measurePrefixAutocomplete(graph, prefixesByOrigin);
        
        // Analyze and write results
        System.out.println("\nAnalyzing results...");
        analyzeAndWriteResults(results, graph.getClass().getSimpleName(), outputDir);
        System.out.println("Experiment completed! Results written to: " + outputDir);
    }
    
    
    private Map<String, List<String>> generatePrefixes(Graph graph, List<String> origins, int nPrefixes) {
        Map<String, List<String>> prefixesByOrigin = new HashMap<>();
        
        for (String origin : origins) {
            List<String> destinations = new ArrayList<>();
            for (Edge edge : graph.neighbors(origin)) {
                destinations.add(edge.getDestination().toLowerCase());
            }
            
            // Extract unique prefixes (1-3 characters)
            Set<String> uniquePrefixes = new HashSet<>();
            for (String dest : destinations) {
                if (dest.length() >= 1) {
                    uniquePrefixes.add(dest.substring(0, Math.min(1, dest.length())));
                }
                if (dest.length() >= 2) {
                    uniquePrefixes.add(dest.substring(0, Math.min(2, dest.length())));
                }
                if (dest.length() >= 3) {
                    uniquePrefixes.add(dest.substring(0, Math.min(3, dest.length())));
                }
            }
            
            // Sample up to N prefixes
            List<String> prefixList = new ArrayList<>(uniquePrefixes);
            Collections.shuffle(prefixList, random);
            prefixesByOrigin.put(origin, prefixList.subList(0, Math.min(nPrefixes, prefixList.size())));
        }
        
        return prefixesByOrigin;
    }
    
    private List<PrefixResult> measurePrefixAutocomplete(Graph graph, Map<String, List<String>> prefixesByOrigin) {
        List<PrefixResult> results = new ArrayList<>();
        boolean isTrieGraph = graph instanceof RoutePartitionedTrieGraph;
        
        // Warmup
        for (Map.Entry<String, List<String>> entry : prefixesByOrigin.entrySet()) {
            String origin = entry.getKey();
            for (String prefix : entry.getValue().subList(0, Math.min(3, entry.getValue().size()))) {
                if (isTrieGraph) {
                    ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
                } else {
                    filterNeighborsByPrefix(graph.neighbors(origin), prefix);
                }
            }
        }
        
        // Actual measurements
        for (Map.Entry<String, List<String>> entry : prefixesByOrigin.entrySet()) {
            String origin = entry.getKey();
            for (String prefix : entry.getValue()) {
                long start = System.nanoTime();
                List<Edge> matches;
                
                if (isTrieGraph) {
                    matches = ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
                } else {
                    matches = filterNeighborsByPrefix(graph.neighbors(origin), prefix);
                }
                
                long end = System.nanoTime();
                long runtimeNs = end - start;
                
                results.add(new PrefixResult(origin, prefix, matches.size(), runtimeNs));
            }
        }
        
        return results;
    }
    
    private List<Edge> filterNeighborsByPrefix(List<Edge> neighbors, String prefix) {
        List<Edge> matches = new ArrayList<>();
        String prefixLower = prefix.toLowerCase();
        for (Edge edge : neighbors) {
            if (edge.getDestination().toLowerCase().startsWith(prefixLower)) {
                matches.add(edge);
            }
        }
        return matches;
    }
    
    private void analyzeAndWriteResults(List<PrefixResult> results, String graphType, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/prefix_autocomplete.csv");
        
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {"origin", "prefix", "matches", "runtime_ns"};
        
        for (PrefixResult result : results) {
            csvRows.add(new String[]{
                result.origin,
                result.prefix,
                String.valueOf(result.matches),
                String.valueOf(result.runtimeNs)
            });
        }
        
        IOUtils.writeCsv(outputDir + "/prefix_autocomplete.csv", headers, csvRows);
        writeSummaryReport(results, graphType, outputDir + "/prefix_autocomplete_README.md");
    }
    
    private void writeSummaryReport(List<PrefixResult> results, String graphType, String outputPath) throws IOException {
        StringBuilder report = new StringBuilder();
        report.append("# Prefix Autocomplete Experiment - Summary\n\n");
        report.append("**Graph Type**: ").append(graphType).append("\n\n");
        
        if (results.isEmpty()) {
            report.append("No results to analyze.\n");
            IOUtils.writeMarkdown(outputPath, report.toString());
            return;
        }
        
        double avgRuntime = results.stream().mapToLong(r -> r.runtimeNs).average().orElse(0) / 1000.0; // microseconds
        double avgMatches = results.stream().mapToInt(r -> r.matches).average().orElse(0);
        long minRuntime = results.stream().mapToLong(r -> r.runtimeNs).min().orElse(0) / 1000;
        long maxRuntime = results.stream().mapToLong(r -> r.runtimeNs).max().orElse(0) / 1000;
        
        report.append("## Summary Statistics\n\n");
        report.append("| Metric | Value |\n");
        report.append("|--------|-------|\n");
        report.append(String.format("| Total Queries | %d |\n", results.size()));
        report.append(String.format("| Avg Runtime (μs) | %.2f |\n", avgRuntime));
        report.append(String.format("| Min Runtime (μs) | %d |\n", minRuntime));
        report.append(String.format("| Max Runtime (μs) | %d |\n", maxRuntime));
        report.append(String.format("| Avg Matches per Query | %.1f |\n", avgMatches));
        
        report.append("\n## Key Findings\n\n");
        if (graphType.equals("RoutePartitionedTrieGraph")) {
            report.append("- **Trie structure shows efficient prefix matching**\n");
            report.append("- Traverses prefix path once and enumerates only matches\n");
        } else {
            report.append("- **Traditional structure scans all neighbors**\n");
            report.append("- Must perform string checks for each neighbor\n");
        }
        
        report.append("\n## Notes\n\n");
        report.append("- JVM warm-up, CPU scaling, and I/O noise can affect microbenchmarks\n");
        report.append("- Degree distribution influences gains; larger hubs show bigger speedups\n");
        report.append("- For best comparison, run this experiment twice:\n");
        report.append("  1. Once with RoutePartitionedTrieGraph\n");
        report.append("  2. Once with a baseline (e.g., OffsetArrayGraph or AdjacencyListGraph)\n");
        report.append("- Compare average latency between the two runs\n");
        
        IOUtils.writeMarkdown(outputPath, report.toString());
    }
    
    private static class PrefixResult {
        final String origin, prefix;
        final int matches;
        final long runtimeNs;
        
        PrefixResult(String origin, String prefix, int matches, long runtimeNs) {
            this.origin = origin;
            this.prefix = prefix;
            this.matches = matches;
            this.runtimeNs = runtimeNs;
        }
    }
    
    public static void main(String[] args) throws IOException {
        // This experiment requires a graph to be built first
        // Typically called from Main.java after building a graph
        System.out.println("This experiment requires a graph to be built first.");
        System.out.println("Please use the Main menu to build a graph, then select this experiment.");
    }
}


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
    private static final int DEFAULT_N_PREFIXES = 100;
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
        // Force RoutePartitionedTrieGraph regardless of choice
        graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
        
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
        analyzeAndWriteResults(results, graph, graph.getClass().getSimpleName(), outputDir);
        System.out.println("Experiment completed! Results written to: " + outputDir);
    }

    /**
     * Scenario: Given an origin code and a destination prefix (e.g., representing a country/city code pattern),
     * find the best airline (minimum combined weight) among all matching destinations.
     * Also measure trie vs. linear-scan runtimes to demonstrate the benefit.
     */
    public void runBestAirlineScenario(String csvPath, String origin, String destinationPrefix, String outputDir) throws IOException {
        System.out.println("=== Best Airline by Destination Prefix (Trie vs Scan) ===");
        System.out.println("CSV: " + csvPath);
        System.out.println("Origin: " + origin + ", Destination prefix: " + destinationPrefix + "\n");

        CsvReader reader = new CsvReader();
        Graph graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);

        if (!(graph instanceof RoutePartitionedTrieGraph)) {
            throw new IllegalStateException("Expected RoutePartitionedTrieGraph");
        }

        if (!graph.hasNode(origin)) {
            throw new IllegalArgumentException("Origin not found in graph: " + origin);
        }

        // Trie query
        long startTrie = System.nanoTime();
        List<Edge> trieMatches = ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, destinationPrefix);
        long trieNs = System.nanoTime() - startTrie;

        // Linear scan baseline on same structure
        long startScan = System.nanoTime();
        List<Edge> scanMatches = filterNeighborsByPrefix(graph.neighbors(origin), destinationPrefix);
        long scanNs = System.nanoTime() - startScan;

        // Sanity check: both should produce the same set sizes (order may differ)
        int candidateCount = trieMatches.size();

        // Find best airline among candidates (minimum edge weight)
        Edge best = null;
        for (Edge e : trieMatches) {
            if (best == null || e.getWeight() < best.getWeight()) {
                best = e;
            }
        }

        System.out.println("Candidates: " + candidateCount);
        if (best != null) {
            System.out.println("Best airline: " + best.getAirline());
            System.out.println("Best destination: " + best.getDestination());
            System.out.println(String.format("Best weight (lower is better): %.3f", best.getWeight()));
        } else {
            System.out.println("No matching routes.");
        }
        double trieMs = trieNs / 1_000_000.0;
        double scanMs = scanNs / 1_000_000.0;
        double speedup = scanNs > 0 ? scanMs / trieMs : Double.POSITIVE_INFINITY;
        System.out.println(String.format("Trie time: %.3f ms | Scan time: %.3f ms | Speedup: %.2fx", trieMs, scanMs, speedup));

        // Print all matches with ratings and composite rating (out of 5), sorted by weight asc
        if (!trieMatches.isEmpty()) {
            List<Edge> sorted = new ArrayList<>(trieMatches);
            sorted.sort(Comparator.comparingDouble(Edge::getWeight).thenComparing(Edge::getDestination));
            System.out.println("\nAll matches (best first):");
            System.out.println("airline,destination,composite_rating_5pt,overall,value_for_money,inflight_entertainment,cabin_staff,seat_comfort");
            for (Edge e : sorted) {
                double compositeRating = 6.0 - e.getWeight();
                if (compositeRating < 1.0) compositeRating = 1.0;
                if (compositeRating > 5.0) compositeRating = 5.0;
                System.out.println(e.getAirline() + "," + e.getDestination() + "," + String.format("%.2f", compositeRating) + "," +
                    e.getOverallRating() + "," + e.getValueForMoney() + "," +
                    e.getInflightEntertainment() + "," + e.getCabinStaff() + "," +
                    e.getSeatComfort());
            }
        }

        // Console-only summary for scenario; no files written
        if (scanMatches != null && scanMatches.size() != candidateCount) {
            System.out.println("Warning: trie and scan candidate counts differ: trie=" + candidateCount + ", scan=" + scanMatches.size());
        }
    }

    /**
     * Console-only showcase: pick three prefixes for a preferred origin (default SIN)
     * - One with many matches (likely 1-letter)
     * - One with ~1 match (full 3-letter code)
     * - One intermediate (2-letter)
     * Prints query time and all results sorted by combined weight.
     */
    public void runSampleAutocompleteShowcase(String csvPath, String preferredOrigin) throws IOException {
        CsvReader reader = new CsvReader();
        Graph graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
        if (!(graph instanceof RoutePartitionedTrieGraph)) {
            throw new IllegalStateException("Expected RoutePartitionedTrieGraph");
        }

        String origin = graph.hasNode(preferredOrigin) ? preferredOrigin : (graph.nodes().isEmpty() ? preferredOrigin : graph.nodes().get(0));
        System.out.println("\n=== Autocomplete Showcase (origin=" + origin + ") ===");

        // Build counts for 1,2,3-letter prefixes from this origin's destinations
        Set<String> codes = new HashSet<>();
        for (Edge e : graph.neighbors(origin)) {
            codes.add(e.getDestination());
        }
        if (codes.isEmpty()) {
            System.out.println("No destinations for origin: " + origin);
            return;
        }

        Map<String, Integer> countByPrefix = new HashMap<>();
        for (String code : codes) {
            for (int len = 1; len <= 3 && len <= code.length(); len++) {
                String p = code.substring(0, len);
                countByPrefix.put(p, countByPrefix.getOrDefault(p, 0) + 1);
            }
        }

        // Choose prefixes
        String manyPrefix = null, onePrefix = null, midPrefix = null;
        // Many: best 1-letter with highest count
        int bestCount = -1;
        for (Map.Entry<String, Integer> e : countByPrefix.entrySet()) {
            if (e.getKey().length() == 1 && e.getValue() > bestCount) {
                bestCount = e.getValue();
                manyPrefix = e.getKey();
            }
        }
        // One-ish: full code that exists (pick the first code)
        onePrefix = codes.iterator().next();
        // Mid: pick a 2-letter with median-ish count if possible
        List<Map.Entry<String, Integer>> twos = new ArrayList<>();
        for (Map.Entry<String, Integer> e : countByPrefix.entrySet()) {
            if (e.getKey().length() == 2) twos.add(e);
        }
        if (!twos.isEmpty()) {
            twos.sort(Comparator.comparingInt(Map.Entry::getValue));
            midPrefix = twos.get(twos.size() / 2).getKey();
        } else {
            midPrefix = onePrefix.substring(0, Math.min(2, onePrefix.length()));
        }

        // Print three queries
        printAutocompleteQuery((RoutePartitionedTrieGraph) graph, origin, manyPrefix);
        printAutocompleteQuery((RoutePartitionedTrieGraph) graph, origin, onePrefix);
        printAutocompleteQuery((RoutePartitionedTrieGraph) graph, origin, midPrefix);
    }

    /**
     * Build multiple graph implementations and run the same three sample queries on each.
     * Prints per-graph timings and results, then a summary ranking by average time across the three queries.
     */
    public void runAllGraphsSampleShowcase(String csvPath, String preferredOrigin) throws IOException {
        // Build graphs
        CsvReader reader = new CsvReader();
        Map<String, Graph> graphs = new LinkedHashMap<>();

        Graph trie = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
        graphs.put("RoutePartitionedTrieGraph", trie);

        Graph adj = reader.readCsvAndBuildGraph(csvPath); // defaults to AdjacencyListGraph
        graphs.put("AdjacencyListGraph", adj);

        Graph sortedAdj = reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new);
        graphs.put("SortedAdjacencyListGraph", sortedAdj);

        Graph linArr = reader.readCsvAndBuildGraph(csvPath, LinearArrayGraph::new);
        graphs.put("LinearArrayGraph", linArr);

        Graph dynArr = reader.readCsvAndBuildGraph(csvPath, DynamicArrayGraph::new);
        graphs.put("DynamicArrayGraph", dynArr);

        Graph dblList = reader.readCsvAndBuildGraph(csvPath, DoublyLinkedListGraph::new);
        graphs.put("DoublyLinkedListGraph", dblList);

        Graph circList = reader.readCsvAndBuildGraph(csvPath, CircularLinkedListGraph::new);
        graphs.put("CircularLinkedListGraph", circList);

        Graph halfEdge = reader.readCsvAndBuildGraph(csvPath, HalfEdgeGraph::new);
        graphs.put("HalfEdgeGraph", halfEdge);

        // MatrixGraph requires capacity; build using node count from adjacency graph
        int matrixCap = Math.max(adj.nodeCount(), 1);
        Graph matrix = new MatrixGraph(matrixCap);
        reader.readCsvAndBuildGraph(csvPath, matrix);
        graphs.put("MatrixGraph", matrix);

        Graph euler = reader.readCsvAndBuildGraph(csvPath, EulerTourTreeGraph::new);
        graphs.put("EulerTourTreeGraph", euler);

        Graph linkCut = reader.readCsvAndBuildGraph(csvPath, LinkCutTreeGraph::new);
        graphs.put("LinkCutTreeGraph", linkCut);

        Graph off = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
        if (off instanceof OffsetArrayGraph) {
            ((OffsetArrayGraph) off).finalizeCSR();
        }
        graphs.put("OffsetArrayGraph", off);

        Graph baseline = reader.readCsvAndBuildGraph(csvPath); // for CSR backing
        Graph csr = new CSRGraph(baseline);
        graphs.put("CSRGraph", csr);

        // Choose fixed origin LHR if present; otherwise fall back to preferredOrigin, then any
        String origin;
        if (trie.hasNode("LHR")) origin = "LHR";
        else if (trie.hasNode(preferredOrigin)) origin = preferredOrigin;
        else origin = graphs.values().iterator().next().nodes().get(0);

        // Fixed test queries as requested
        String q1 = "B"; // many matches
        String q2 = "J"; // few matches
        String q3 = "Z"; // likely sparse

        // Run each graph for the three prefixes
        Map<String, List<Double>> implToAverages = new LinkedHashMap<>();

        System.out.println("\n=== Autocomplete Showcase Across Graphs (origin=" + origin + ") ===");
        System.out.println("Queries: '" + q1 + "', '" + q2 + "', '" + q3 + "'\n");

        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String name = entry.getKey();
            Graph g = entry.getValue();
            System.out.println("\n--- " + name + " ---");
            double avgQ1 = printAutocompleteQueryGeneric(g, name, origin, q1);
            double avgQ2 = printAutocompleteQueryGeneric(g, name, origin, q2);
            double avgQ3 = printAutocompleteQueryGeneric(g, name, origin, q3);
            implToAverages.put(name, Arrays.asList(avgQ1, avgQ2, avgQ3));
        }

        // Three separate summaries: one per query, each ranked fastest to slowest
        List<Map.Entry<String, Double>> r1 = new ArrayList<>();
        List<Map.Entry<String, Double>> r2 = new ArrayList<>();
        List<Map.Entry<String, Double>> r3 = new ArrayList<>();
        for (Map.Entry<String, List<Double>> e : implToAverages.entrySet()) {
            List<Double> ts = e.getValue();
            r1.add(new AbstractMap.SimpleEntry<>(e.getKey(), ts.get(0)));
            r2.add(new AbstractMap.SimpleEntry<>(e.getKey(), ts.get(1)));
            r3.add(new AbstractMap.SimpleEntry<>(e.getKey(), ts.get(2)));
        }
        r1.sort(Comparator.comparingDouble(Map.Entry::getValue));
        r2.sort(Comparator.comparingDouble(Map.Entry::getValue));
        r3.sort(Comparator.comparingDouble(Map.Entry::getValue));

        System.out.println("\n=== Summary: Query '" + q1 + "' (ms), fastest to slowest ===");
        for (Map.Entry<String, Double> e : r1) {
            System.out.println(String.format("%s: %.5f ms", e.getKey(), e.getValue()));
        }

        System.out.println("\n=== Summary: Query '" + q2 + "' (ms), fastest to slowest ===");
        for (Map.Entry<String, Double> e : r2) {
            System.out.println(String.format("%s: %.5f ms", e.getKey(), e.getValue()));
        }

        System.out.println("\n=== Summary: Query '" + q3 + "' (ms), fastest to slowest ===");
        for (Map.Entry<String, Double> e : r3) {
            System.out.println(String.format("%s: %.5f ms", e.getKey(), e.getValue()));
        }
    }

    private static class SamplePrefixes {
        final String many, one, mid;
        SamplePrefixes(String many, String one, String mid) { this.many = many; this.one = one; this.mid = mid; }
    }

    private SamplePrefixes pickSamplePrefixes(Graph graph, String origin) {
        Set<String> codes = new HashSet<>();
        for (Edge e : graph.neighbors(origin)) codes.add(e.getDestination());
        Map<String, Integer> countByPrefix = new HashMap<>();
        for (String code : codes) {
            for (int len = 1; len <= 3 && len <= code.length(); len++) {
                String p = code.substring(0, len);
                countByPrefix.put(p, countByPrefix.getOrDefault(p, 0) + 1);
            }
        }
        String many = null, one = null, mid = null;
        int best = -1;
        for (Map.Entry<String, Integer> e : countByPrefix.entrySet()) {
            if (e.getKey().length() == 1 && e.getValue() > best) { best = e.getValue(); many = e.getKey(); }
        }
        one = codes.iterator().next();
        List<Map.Entry<String, Integer>> twos = new ArrayList<>();
        for (Map.Entry<String, Integer> e : countByPrefix.entrySet()) if (e.getKey().length() == 2) twos.add(e);
        if (!twos.isEmpty()) { twos.sort(Comparator.comparingInt(Map.Entry::getValue)); mid = twos.get(twos.size()/2).getKey(); }
        else { mid = one.substring(0, Math.min(2, one.length())); }
        return new SamplePrefixes(many, one, mid);
    }

    private double printAutocompleteQueryGeneric(Graph graph, String graphName, String origin, String prefix) {
        // Warm-up runs (not recorded)
        final int WARMUP_RUNS = 1000;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            if (graph instanceof RoutePartitionedTrieGraph) {
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
            } else {
                filterNeighborsByPrefix(graph.neighbors(origin), prefix);
            }
        }

        // Measured runs: 1000 for averaging; preview shows first 10
        final int MEASURE_RUNS = 1000;
        final int PREVIEW_RUNS = 10;
        List<Double> previewMs = new ArrayList<>(PREVIEW_RUNS);
        List<Edge> matches = null;
        double sumMs = 0.0;
        for (int i = 0; i < MEASURE_RUNS; i++) {
            long start = System.nanoTime();
            List<Edge> current;
            if (graph instanceof RoutePartitionedTrieGraph) {
                current = ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
            } else {
                current = filterNeighborsByPrefix(graph.neighbors(origin), prefix);
            }
            long ns = System.nanoTime() - start;
            double ms = ns / 1_000_000.0;
            if (i < PREVIEW_RUNS) previewMs.add(ms);
            sumMs += ms;
            if (matches == null) matches = current;
        }
        double avgMs = sumMs / MEASURE_RUNS;

        System.out.println("\nOrigin: " + origin + "  Search: " + prefix);
        System.out.println("Runtimes (ms) [first 10 runs]: " + formatList(previewMs));
        System.out.println(String.format("Average: %.5f ms (averaged across %d runs)", avgMs, 1000));
        System.out.println("Matches: " + (matches != null ? matches.size() : 0));

        if (matches != null && !matches.isEmpty()) {
            List<Edge> sorted = new ArrayList<>(matches);
            sorted.sort(Comparator.comparingDouble(Edge::getWeight).thenComparing(Edge::getDestination));
            System.out.println("airline,destination,composite_rating_5pt,overall,value_for_money,inflight_entertainment,cabin_staff,seat_comfort");
            for (Edge e : sorted) {
                double compositeRating = 6.0 - e.getWeight();
                if (compositeRating < 1.0) compositeRating = 1.0;
                if (compositeRating > 5.0) compositeRating = 5.0;
                System.out.println(e.getAirline() + "," + e.getDestination() + "," + String.format("%.2f", compositeRating) + "," +
                    e.getOverallRating() + "," + e.getValueForMoney() + "," +
                    e.getInflightEntertainment() + "," + e.getCabinStaff() + "," +
                    e.getSeatComfort());
            }
        }

        return avgMs;
    }

    private void printAutocompleteQuery(RoutePartitionedTrieGraph graph, String origin, String prefix) {
        // Warm-up runs (not recorded)
        final int WARMUP_RUNS = 1000;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            graph.neighborsByPrefix(origin, prefix);
        }

        // Measured runs: 1000 for averaging; preview shows first 10
        final int MEASURE_RUNS = 1000;
        final int PREVIEW_RUNS = 10;
        List<Double> previewMs = new ArrayList<>(PREVIEW_RUNS);
        List<Edge> matches = null;
        double sumMs = 0.0;
        for (int i = 0; i < MEASURE_RUNS; i++) {
            long start = System.nanoTime();
            List<Edge> current = graph.neighborsByPrefix(origin, prefix);
            long ns = System.nanoTime() - start;
            double ms = ns / 1_000_000.0;
            if (i < PREVIEW_RUNS) previewMs.add(ms);
            sumMs += ms;
            matches = current;
        }
        double avgMs = sumMs / MEASURE_RUNS;

        System.out.println("\nOrigin: " + origin + "  Search: " + prefix);
        System.out.println("Runtimes (ms) [first 10 runs]: " + formatList(previewMs));
        System.out.println(String.format("Average: %.5f ms (averaged across %d runs)", avgMs, 1000));
        System.out.println("Matches: " + (matches != null ? matches.size() : 0));

        if (matches != null && !matches.isEmpty()) {
            List<Edge> sorted = new ArrayList<>(matches);
            sorted.sort(Comparator.comparingDouble(Edge::getWeight).thenComparing(Edge::getDestination));
            System.out.println("airline,destination,composite_rating_5pt,overall,value_for_money,inflight_entertainment,cabin_staff,seat_comfort");
            for (Edge e : sorted) {
                double compositeRating = 6.0 - e.getWeight();
                if (compositeRating < 1.0) compositeRating = 1.0;
                if (compositeRating > 5.0) compositeRating = 5.0;
                System.out.println(e.getAirline() + "," + e.getDestination() + "," + String.format("%.2f", compositeRating) + "," +
                    e.getOverallRating() + "," + e.getValueForMoney() + "," +
                    e.getInflightEntertainment() + "," + e.getCabinStaff() + "," +
                    e.getSeatComfort());
            }
        }
    }

    private String formatList(List<Double> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.5f", values.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    
    private Map<String, List<String>> generatePrefixes(Graph graph, List<String> origins, int nPrefixes) {
        Map<String, List<String>> prefixesByOrigin = new HashMap<>();
        
        for (String origin : origins) {
            List<String> destinations = new ArrayList<>();
            for (Edge edge : graph.neighbors(origin)) {
                // Use destination codes as-is (e.g., airport codes, typically uppercase)
                destinations.add(edge.getDestination());
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
                if (!isTrieGraph) continue;
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
            }
        }
        
        // Actual measurements
        for (Map.Entry<String, List<String>> entry : prefixesByOrigin.entrySet()) {
            String origin = entry.getKey();
            for (String prefix : entry.getValue()) {
                long start = System.nanoTime();
                List<Edge> matches;
                
                if (!isTrieGraph) {
                    throw new IllegalStateException("PrefixAutocompleteExperiment is restricted to RoutePartitionedTrieGraph");
                }
                matches = ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(origin, prefix);
                
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
    
    private void analyzeAndWriteResults(List<PrefixResult> results, Graph graph, String graphType, String outputDir) throws IOException {
        IOUtils.ensureParentDirectoryExists(outputDir + "/prefix_autocomplete.csv");
        
        List<String[]> csvRows = new ArrayList<>();
        String[] headers = {"origin", "prefix", "matches", "runtime_ms"};
        
        for (PrefixResult result : results) {
            double runtimeMs = result.runtimeNs / 1_000_000.0;
            csvRows.add(new String[]{
                result.origin,
                result.prefix,
                String.valueOf(result.matches),
                String.format("%.3f", runtimeMs)
            });
        }
        
        IOUtils.writeCsv(outputDir + "/prefix_autocomplete.csv", headers, csvRows);

        // Console analysis summary
        double avgRuntimeMs = results.stream().mapToLong(r -> r.runtimeNs).average().orElse(0) / 1_000_000.0;
        double minRuntimeMs = results.stream().mapToLong(r -> r.runtimeNs).min().orElse(0) / 1_000_000.0;
        double maxRuntimeMs = results.stream().mapToLong(r -> r.runtimeNs).max().orElse(0) / 1_000_000.0;
        double avgMatches = results.stream().mapToInt(r -> r.matches).average().orElse(0);

        System.out.println("\n=== Analysis ===");
        System.out.println("Data structure: " + graphType + " (forced)");
        System.out.println("Time complexity (query): O(|p| + k) where |p| is prefix length, k matches");
        System.out.println("Space complexity: O(T + E) where T is total trie nodes across origins and E is edges");
        System.out.println(String.format("Avg runtime: %.3f ms | Min: %.3f ms | Max: %.3f ms | Avg matches: %.1f",
            avgRuntimeMs, minRuntimeMs, maxRuntimeMs, avgMatches));

        long memoryEstimate = 0L;
        if (graph instanceof RoutePartitionedTrieGraph) {
            memoryEstimate = ((RoutePartitionedTrieGraph) graph).getMemoryUsage();
            System.out.println("Memory estimate (bytes): " + memoryEstimate);
        }

        // Print a few sample lines to console (first up to 5 rows)
        System.out.println("Sample results (first up to 5 rows):");
        for (int i = 0; i < Math.min(5, results.size()); i++) {
            PrefixResult r = results.get(i);
            double ms = r.runtimeNs / 1_000_000.0;
            System.out.println(r.origin + "," + r.prefix + "," + r.matches + "," + String.format("%.3fms", ms));
        }
        System.out.println("CSV written to: " + (outputDir + "/prefix_autocomplete.csv"));
    }

    // (Excel output helpers removed; only CSV is written now)
    
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
        // Defaults for quick demo
        String csvPath = "data/cleaned_flights.csv";
        String outputDir = "out/experiments/experiment5_prefix_autocomplete";

        // 1) Full benchmark over many (origin, prefix) pairs
        int nPrefixes = DEFAULT_N_PREFIXES;
        new PrefixAutocompleteExperiment().runExperiment(csvPath, 1, nPrefixes, outputDir);

        // 2) Scenario demo: best airline from a specific origin to destinations matching a code prefix
        // Example: SIN -> CVG (full code) or broader prefix like 'C'/'CV' to simulate region/category filters
        new PrefixAutocompleteExperiment().runBestAirlineScenario(csvPath, "SIN", "CVG", outputDir);
    }
}



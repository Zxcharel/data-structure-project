package src.experiments;

import src.data.CsvReader;
import src.graph.*;
import src.util.IOUtils;
import java.io.IOException;
import java.util.*;

/**
 * Experiment 3: Prefix Autocomplete on Outgoing Routes
 * 
 * Tests when tries beat arrays: prefix autocomplete performance comparing
 * RoutePartitionedTrieGraph against traditional array/list/CSR structures.
 */
public class PrefixAutocompleteExperimentUserInput {
    
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

    // All sample showcase runs removed in this user-input variant.

    /**
     * Console-only showcase: pick three prefixes for a preferred origin (default SIN)
     * - One with many matches (likely 1-letter)
     * - One with ~1 match (full 3-letter code)
     * - One intermediate (2-letter)
     * Prints query time and all results sorted by combined weight.
     */
    /**
     * Interactive run: user chooses origin and destination code or prefix.
     * Builds the trie graph, warms up 1000 runs, then measures 1000 runs and prints results.
     */
    public void runInteractive(String csvPath) throws IOException {
        CsvReader reader = new CsvReader();
        Graph trieGraph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
        if (!(trieGraph instanceof RoutePartitionedTrieGraph)) {
            throw new IllegalStateException("Expected RoutePartitionedTrieGraph");
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter origin code (e.g., LHR): ");
        String origin = scanner.nextLine().trim().toUpperCase();
        if (!trieGraph.hasNode(origin)) {
            System.out.println("Origin not found: " + origin);
            return;
        }
        System.out.print("Enter destination codes or prefixes (comma-separated, e.g., B,J,Z): ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("No prefixes provided.");
            return;
        }
        String[] parts = line.split(",");
        List<String> queries = new ArrayList<>();
        for (String p : parts) {
            String q = p.trim().toUpperCase();
            if (!q.isEmpty()) queries.add(q);
        }
        if (queries.isEmpty()) {
            System.out.println("No valid prefixes provided.");
            return;
        }

        // Build selected graph implementations (restricted set)
        Map<String, Graph> graphs = new LinkedHashMap<>();

        // 1) AdjacencyListGraph — Baseline
        Graph adj = reader.readCsvAndBuildGraph(csvPath);
        graphs.put("AdjacencyListGraph", adj);

        // 2) CSRGraph — Optimized
        Graph baselineForCsr = reader.readCsvAndBuildGraph(csvPath);
        Graph csr = new CSRGraph(baselineForCsr);
        graphs.put("CSRGraph", csr);

        // 3) SortedAdjacencyListGraph — Fastest
        Graph sortedAdj = reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new);
        graphs.put("SortedAdjacencyListGraph", sortedAdj);

        // 4) OffsetArrayGraph — CSR variant
        Graph off = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
        if (off instanceof OffsetArrayGraph) {
            ((OffsetArrayGraph) off).finalizeCSR();
        }
        graphs.put("OffsetArrayGraph", off);

        // 5) MatrixGraph — Comparison
        int matrixCap = Math.max(adj.nodeCount(), 1);
        Graph matrix = new MatrixGraph(matrixCap);
        reader.readCsvAndBuildGraph(csvPath, matrix);
        graphs.put("MatrixGraph", matrix);

        // 6) RoutePartitionedTrieGraph — Specialized
        graphs.put("RoutePartitionedTrieGraph", trieGraph);

        // 7) HalfEdgeGraph — Optional
        Graph halfEdge = reader.readCsvAndBuildGraph(csvPath, HalfEdgeGraph::new);
        graphs.put("HalfEdgeGraph", halfEdge);

        // Run each graph for user-provided queries
        Map<String, List<Double>> implToAverages = new LinkedHashMap<>();
        System.out.println("\n=== Interactive Autocomplete Across Graphs (origin=" + origin + ") ===");
        System.out.println("Queries: " + String.join(", ", queries) + "\n");

        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String name = entry.getKey();
            Graph g = entry.getValue();
            System.out.println("\n--- " + name + " ---");
            List<Double> perGraphAverages = new ArrayList<>();
            for (String q : queries) {
                double avg = printAutocompleteQueryGeneric(g, name, origin, q);
                perGraphAverages.add(avg);
            }
            implToAverages.put(name, perGraphAverages);
        }

        // Summaries per query (fastest to slowest)
        for (int qi = 0; qi < queries.size(); qi++) {
            List<Map.Entry<String, Double>> ranking = new ArrayList<>();
            for (Map.Entry<String, List<Double>> e : implToAverages.entrySet()) {
                if (qi < e.getValue().size()) {
                    ranking.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().get(qi)));
                }
            }
            ranking.sort(Comparator.comparingDouble(Map.Entry::getValue));
            System.out.println("\n=== Summary: Query '" + queries.get(qi) + "' (ms), fastest to slowest ===");
            for (Map.Entry<String, Double> e : ranking) {
                System.out.println(String.format("%s: %.5f ms", e.getKey(), e.getValue()));
            }
        }

        // Random workload: 10000 origin+prefix queries derived from adjacency graph
        List<QueryPair> randomQueries = buildRandomOriginPrefixQueries(adj, 10000);
        List<String[]> randomCsvRows = new ArrayList<>();
        String[] randomHeaders = {"graph_type", "origin", "prefix", "runtime_ms", "memory_kb"};
        List<RandomResult> randomRanking = new ArrayList<>();
        for (Map.Entry<String, Graph> entry : graphs.entrySet()) {
            String name = entry.getKey();
            Graph g = entry.getValue();
            List<Double> perQueryMs = measurePerQueryRuntimes(g, randomQueries);
            double avg = perQueryMs.stream().mapToDouble(d -> d).average().orElse(0.0);
            long memKb = measureGraphMemoryKbByRebuild(csvPath, name, adj.nodeCount());
            randomRanking.add(new RandomResult(name, avg, memKb));
            for (int i = 0; i < randomQueries.size(); i++) {
                QueryPair q = randomQueries.get(i);
                double ms = perQueryMs.get(i);
                randomCsvRows.add(new String[]{
                    name, q.origin, q.prefix, String.format("%.5f", ms), String.valueOf(memKb)
                });
            }
        }
        randomRanking.sort(Comparator.comparingDouble(r -> r.avgMs));
        System.out.println("\n=== Summary: Random workload (10000 origin+prefix queries) (ms), fastest to slowest ===");
        for (RandomResult r : randomRanking) {
            System.out.println(String.format("%s: %.5f ms", r.name, r.avgMs));
        }
        String randomOut = "out/experiments/experiment5_prefix_autocomplete/random_workload_user.csv";
        IOUtils.ensureParentDirectoryExists(randomOut);
        IOUtils.writeCsv(randomOut, randomHeaders, randomCsvRows);
        System.out.println("Random workload CSV written to: " + randomOut);
        List<RandomResult> memoryRank = new ArrayList<>(randomRanking);
        memoryRank.sort(Comparator.comparingLong(r -> r.memoryKb < 0 ? Long.MAX_VALUE : r.memoryKb));
        System.out.println("\n=== Summary: Space usage (KB), smallest to largest ===");
        for (RandomResult r : memoryRank) {
            System.out.println(String.format("%s: %s", r.name, formatMemoryKb(r.memoryKb)));
        }
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

    // Cross-graph showcases removed for this user-input variant.
    private static class RandomResult {
        final String name;
        final double avgMs;
        final long memoryKb;
        RandomResult(String name, double avgMs, long memoryKb) { this.name = name; this.avgMs = avgMs; this.memoryKb = memoryKb; }
    }

    private static class QueryPair {
        final String origin;
        final String prefix;
        QueryPair(String origin, String prefix) { this.origin = origin; this.prefix = prefix; }
    }

    private List<QueryPair> buildRandomOriginPrefixQueries(Graph baseGraph, int n) {
        List<String> origins = baseGraph.nodes();
        Random rng = new Random(42);
        List<QueryPair> queries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String origin;
            List<Edge> neighbors;
            do {
                origin = origins.get(rng.nextInt(origins.size()));
                neighbors = baseGraph.neighbors(origin);
            } while (neighbors.isEmpty());
            Edge e = neighbors.get(rng.nextInt(neighbors.size()));
            String dest = e.getDestination();
            int len = 1 + rng.nextInt(Math.min(3, dest.length()));
            String prefix = dest.substring(0, len);
            queries.add(new QueryPair(origin, prefix));
        }
        return queries;
    }

    private double measureAverageForQueries(Graph graph, List<QueryPair> queries) {
        // Warmup over the random queries (not recorded)
        for (QueryPair q : queries) {
            if (graph instanceof RoutePartitionedTrieGraph) {
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(q.origin, q.prefix);
            } else {
                filterNeighborsByPrefix(graph.neighbors(q.origin), q.prefix);
            }
        }
        // Measured pass
        double sumMs = 0.0;
        for (QueryPair q : queries) {
            long start = System.nanoTime();
            if (graph instanceof RoutePartitionedTrieGraph) {
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(q.origin, q.prefix);
            } else {
                filterNeighborsByPrefix(graph.neighbors(q.origin), q.prefix);
            }
            long ns = System.nanoTime() - start;
            sumMs += ns / 1_000_000.0;
        }
        return sumMs / Math.max(1, queries.size());
    }

    private List<Double> measurePerQueryRuntimes(Graph graph, List<QueryPair> queries) {
        // Warmup
        for (QueryPair q : queries) {
            if (graph instanceof RoutePartitionedTrieGraph) {
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(q.origin, q.prefix);
            } else {
                filterNeighborsByPrefix(graph.neighbors(q.origin), q.prefix);
            }
        }
        // Measured per-query
        List<Double> msList = new ArrayList<>(queries.size());
        for (QueryPair q : queries) {
            long start = System.nanoTime();
            if (graph instanceof RoutePartitionedTrieGraph) {
                ((RoutePartitionedTrieGraph) graph).neighborsByPrefix(q.origin, q.prefix);
            } else {
                filterNeighborsByPrefix(graph.neighbors(q.origin), q.prefix);
            }
            long ns = System.nanoTime() - start;
            msList.add(ns / 1_000_000.0);
        }
        return msList;
    }

    private long estimateMemoryKb(Graph graph) {
        try {
            if (graph instanceof RoutePartitionedTrieGraph) {
                return Math.max(0, ((RoutePartitionedTrieGraph) graph).getMemoryUsage() / 1024);
            }
            if (graph instanceof MatrixGraph) {
                return Math.max(0, ((MatrixGraph) graph).getMemoryUsage() / 1024);
            }
            if (graph instanceof OffsetArrayGraph) {
                try {
                    java.lang.reflect.Method m = graph.getClass().getMethod("getMemoryUsage");
                    Object bytes = m.invoke(graph);
                    if (bytes instanceof Number) return Math.max(0, ((Number) bytes).longValue() / 1024);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        long nodes = graph.nodeCount();
        long edges = graph.edgeCount();
        long estimateBytes = nodes * 50L + edges * 64L;
        return Math.max(0, estimateBytes / 1024);
    }

    private String formatMemoryKb(long kb) {
        return kb >= 0 ? kb + " KB" : "N/A";
    }

    private static Graph MEMORY_PIN; // prevent GC of last built graph during measurement

    private long measureGraphMemoryKbByRebuild(String csvPath, String graphName, int matrixCap) {
        try {
            long bestKb = -1;
            for (int attempt = 0; attempt < 3; attempt++) {
                System.gc();
                long before = usedHeap();
                CsvReader reader = new CsvReader();
                Graph built;
                switch (graphName) {
                    case "AdjacencyListGraph":
                        built = reader.readCsvAndBuildGraph(csvPath);
                        break;
                    case "CSRGraph": {
                        Graph base = reader.readCsvAndBuildGraph(csvPath);
                        built = new CSRGraph(base);
                        break;
                    }
                    case "SortedAdjacencyListGraph":
                        built = reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new);
                        break;
                    case "OffsetArrayGraph": {
                        Graph g = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
                        if (g instanceof OffsetArrayGraph) ((OffsetArrayGraph) g).finalizeCSR();
                        built = g;
                        break;
                    }
                    case "MatrixGraph": {
                        Graph g = new MatrixGraph(Math.max(matrixCap, 1));
                        reader.readCsvAndBuildGraph(csvPath, g);
                        built = g;
                        break;
                    }
                    case "RoutePartitionedTrieGraph":
                        built = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
                        break;
                    case "HalfEdgeGraph":
                        built = reader.readCsvAndBuildGraph(csvPath, HalfEdgeGraph::new);
                        break;
                    default:
                        built = reader.readCsvAndBuildGraph(csvPath);
                }
                MEMORY_PIN = built;
                long after = usedHeap();
                long deltaKb = Math.max(0, (after - before) / 1024);
                if (deltaKb > bestKb) bestKb = deltaKb;
            }
            return bestKb;
        } catch (Exception e) {
            return -1;
        }
    }

    private long usedHeap() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
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

    // Generic print method removed; only interactive path is supported here.

    // Trie-only print helper removed.

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
        String csvPath = "data/cleaned_flights.csv";
        new PrefixAutocompleteExperimentUserInput().runInteractive(csvPath);
    }
}



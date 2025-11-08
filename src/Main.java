package src;

import src.data.CsvReader;
import src.graph.Graph;
import src.graph.AdjacencyListGraph;
import src.graph.SortedAdjacencyListGraph;
import src.graph.MatrixGraph;
import src.graph.CSRGraph;
import src.graph.DoublyLinkedListGraph;
import src.graph.CircularLinkedListGraph;
import src.graph.HalfEdgeGraph;
import src.graph.LinkCutTreeGraph;
import src.graph.EulerTourTreeGraph;
import src.graph.LinearArrayGraph;
import src.graph.DynamicArrayGraph;
import src.graph.OffsetArrayGraph;
import src.graph.RoutePartitionedTrieGraph;
import src.algo.*;
import src.experiments.PathfindingBenchmarkExperiment;
import src.experiments.CacheLocalityExperiment;
import src.experiments.NeighborIterationExperiment;
import src.experiments.ScalingExperiment;
import src.experiments.PrefixAutocompleteExperiment;
import src.experiments.PrefixAutocompleteExperimentUserInput;
import src.analysis.GraphAnalyzer;
import src.analysis.CentralityMetrics;
import src.comparison.DataStructureComparator;
import src.comparison.DataStructureComparator.GraphComparisonResult;
import src.comparison.DataStructureComparator.MemoryComparisonResult;
import src.comparison.DataStructureComparator.AlgorithmComparisonResult;
import src.reports.AnalysisReportGenerator;
import java.io.IOException;
import java.util.*;

/**
 * Main class providing console interface for the airline route finder.
 * Implements the complete menu system as specified in the requirements.
 */
public class Main {
    private static Graph graph = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Best Airline Path Finder ===");
        System.out.println("Find optimal airline routes based on review ratings");
        System.out.println();

        while (true) {
            showMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    buildGraphFromCsv();
                    break;
                case 2:
                    queryBestRoute();
                    break;
                case 3:
                    runExperiments();
                    break;
                case 4:
                    performGraphAnalysis();
                    break;
                case 5:
                    generateAnalysisReport();
                    break;
                case 6:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        }
    }

    /**
     * Displays the main menu
     */
    private static void showMenu() {
        System.out.println("Main Menu:");
        System.out.println("1. Build graph from CSV");
        System.out.println("2. Query best route");
        System.out.println("3. Run experiments (5 experiments available)");
        System.out.println("4. Graph analysis");
        System.out.println("5. Generate analysis report");
        System.out.println("6. Exit");
        System.out.println();
    }

    /**
     * Menu option 1: Build graph from CSV
     */
    private static void buildGraphFromCsv() {
        System.out.println("=== Build Graph from CSV ===");

        String csvPath = getStringInput("Enter CSV path (press Enter for default 'data/cleaned_flights.csv'): ");
        if (csvPath.trim().isEmpty()) {
            csvPath = "data/cleaned_flights.csv";
        }

        try {
            System.out.println("Reading CSV file: " + csvPath);
            CsvReader reader = new CsvReader();
            
            // Choose graph implementation
            System.out.println("\nSelect graph implementation:");
            System.out.println(" 1. AdjacencyListGraph (default)");
            System.out.println(" 2. SortedAdjacencyListGraph");
            System.out.println(" 3. MatrixGraph");
            System.out.println(" 4. CSRGraph");
            System.out.println(" 5. DoublyLinkedListGraph");
            System.out.println(" 6. CircularLinkedListGraph");
            System.out.println(" 7. HalfEdgeGraph");
            System.out.println(" 8. LinearArrayGraph");
            System.out.println(" 9. DynamicArrayGraph");
            System.out.println("10. OffsetArrayGraph");
            System.out.println("11. RoutePartitionedTrieGraph");
            System.out.println("12. LinkCutTreeGraph (adapter)");
            System.out.println("13. EulerTourTreeGraph (adapter)");
            int implChoice = getIntInput("\nEnter choice (1-13): ");
            if (implChoice < 1 || implChoice > 13) implChoice = 1;

            // Build graph based on choice
            switch (implChoice) {
                case 1:
                    // Default: AdjacencyListGraph
                    graph = reader.readCsvAndBuildGraph(csvPath);
                    break;
                case 2:
                    graph = reader.readCsvAndBuildGraph(csvPath, SortedAdjacencyListGraph::new);
                    break;
                case 3:
                    // MatrixGraph needs maxNodes - build temp graph first to get node count
                    Graph tempGraph = reader.readCsvAndBuildGraph(csvPath);
                    int nodeCount = tempGraph.nodeCount();
                    MatrixGraph matrixGraph = new MatrixGraph(nodeCount * 2); // Extra capacity
                    reader.readCsvAndBuildGraph(csvPath, matrixGraph);
                    graph = matrixGraph;
                    break;
                case 4:
                    // CSRGraph needs to be built from another graph
                    Graph tempGraphForCSR = reader.readCsvAndBuildGraph(csvPath);
                    graph = new CSRGraph(tempGraphForCSR);
                    break;
                case 5:
                    graph = reader.readCsvAndBuildGraph(csvPath, DoublyLinkedListGraph::new);
                    break;
                case 6:
                    graph = reader.readCsvAndBuildGraph(csvPath, CircularLinkedListGraph::new);
                    break;
                case 7:
                    graph = reader.readCsvAndBuildGraph(csvPath, HalfEdgeGraph::new);
                    break;
                case 8:
                    graph = reader.readCsvAndBuildGraph(csvPath, LinearArrayGraph::new);
                    break;
                case 9:
                    graph = reader.readCsvAndBuildGraph(csvPath, DynamicArrayGraph::new);
                    break;
                case 10:
                    graph = reader.readCsvAndBuildGraph(csvPath, OffsetArrayGraph::new);
                    break;
                case 11:
                    graph = reader.readCsvAndBuildGraph(csvPath, RoutePartitionedTrieGraph::new);
                    break;
                case 12:
                    graph = reader.readCsvAndBuildGraph(csvPath, LinkCutTreeGraph::new);
                    break;
                case 13:
                    graph = reader.readCsvAndBuildGraph(csvPath, EulerTourTreeGraph::new);
                    break;
                default:
                    graph = reader.readCsvAndBuildGraph(csvPath);
                    break;
            }
            
            System.out.println("Graph built successfully!");
            System.out.println("Graph impl: " + graph.getClass().getSimpleName());
            System.out.println("Nodes: " + graph.nodeCount());
            System.out.println("Edges: " + graph.edgeCount());

            // Show some sample nodes
            List<String> nodes = graph.nodes();
            if (!nodes.isEmpty()) {
                System.out.println("Sample nodes: " + String.join(", ", nodes.subList(0, Math.min(10, nodes.size()))));
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error building graph: " + e.getMessage());
        }
    }

    /**
     * Menu option 2: Query best route
     */
    private static void queryBestRoute() {
        if (graph == null) {
            System.out.println("Error: No graph loaded. Please build graph from CSV first.");
            return;
        }

        System.out.println("=== Query Best Route ===");

        String origin = getStringInput("Enter origin country: ").trim();
        String destination = getStringInput("Enter destination country: ").trim();

        // Validate nodes exist
        if (!graph.hasNode(origin)) {
            System.out.println("Error: Origin country '" + origin + "' not found in graph.");
            suggestSimilarNodes(origin, "origin");
            return;
        }

        if (!graph.hasNode(destination)) {
            System.out.println("Error: Destination country '" + destination + "' not found in graph.");
            suggestSimilarNodes(destination, "destination");
            return;
        }

        // Optional constraints
        Constraints constraints = getConstraints();

        // Run Dijkstra's algorithm
        Dijkstra dijkstra = new Dijkstra();
        PathResult result = dijkstra.findPath(graph, origin, destination, constraints);
        String algorithmName = "Dijkstra";

        // Display results
        System.out.println("\n=== Results ===");
        System.out.println("Algorithm: " + algorithmName);
        System.out.println(result.getDetailedSummary());

        // ASCII route visualization
        if (result.isFound()) {
            System.out.println("\n=== Route Visualization ===");
            displayAsciiRoute(result);
        }
    }

    /**
     * Menu option 3: Run experiments
     */
    private static void runExperiments() {
        System.out.println("=== Experiments: Graph Structure Comparison ===");
        System.out.println("Select an experiment to run:");
        System.out.println("1. Experiment 1: Neighbor Iteration Performance");
        System.out.println("2. Experiment 2: The Graph Size Deception (Scaling)");
        System.out.println("3. Experiment 3: Prefix Autocomplete (Trie vs Arrays)");
        System.out.println("4. Experiment 4: Cache Locality (CSR vs Adjacency Lists)");
        System.out.println("5. Experiment 5: Pathfinding Performance Benchmark");
        System.out.println("6. Run all experiments");
        System.out.println("7. Back to main menu");

        int expChoice = getIntInput("\nEnter choice (1-7): ");
        if (expChoice < 1 || expChoice > 7) {
            System.out.println("Invalid choice.");
            return;
        }

        try {
            switch (expChoice) {
                case 1:
                    runExperiment1();
                    break;
                case 2:
                    runExperiment2();
                    break;
                case 3:
                    runExperiment3();
                    break;
                case 4:
                    runExperiment4();
                    break;
                case 5:
                    runExperiment5();
                    break;
                case 6:
                    System.out.println("\n=== Running All Experiments ===\n");
                    runExperiment1();
                    System.out.println("\n");
                    runExperiment2();
                    System.out.println("\n");
                    runExperiment3();
                    System.out.println("\n");
                    runExperiment4();
                    System.out.println("\n");
                    runExperiment5();
                    System.out.println("\n");
                    System.out.println("\n=== All Experiments Completed ===");
                    break;
                case 7:
                    return;
            }
        } catch (IOException e) {
            System.err.println("Error running experiment: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Runs Experiment 1: Neighbor Iteration Performance
     */
    private static void runExperiment1() throws IOException {
        System.out.println("\n=== Running Experiment 1: Neighbor Iteration Performance ===");
        String csvPath = "data/cleaned_flights.csv";
        String outputDir = "out/experiments/experiment1_neighbor_iteration";

        NeighborIterationExperiment experiment =
            new NeighborIterationExperiment();
        experiment.runExperiment(csvPath, outputDir);

        System.out.println("\n✅ Experiment 1 completed!");
        System.out.println("Results written to: " + outputDir);


        
    }
    
    /**
     * Runs Experiment 2: The Graph Size Deception (Scaling Experiment)
     */
    private static void runExperiment2() throws IOException {
        System.out.println("\n=== Running Experiment 2: The Graph Size Deception ===");
        String csvPath = "data/cleaned_flights.csv";
        String outputDir = "out/experiments/experiment2_scaling";
        
        ScalingExperiment experiment = new ScalingExperiment();
        experiment.run(csvPath, outputDir);

        System.out.println("\n✅ Experiment 2 completed!");
        System.out.println("Results written to: " + outputDir);

    }
    
    /**
     * Runs Experiment 3: Prefix Autocomplete 
     */
    private static void runExperiment3() throws IOException {
        System.out.println("\n=== Running Experiment 3: Prefix Autocomplete ===");
        String csvPath = "data/cleaned_flights.csv";
        
        // Run with defaults (trie forced in the experiment implementation)
        int graphChoice = 1;
        int nPrefixes = 100;
        
        String outputDir = "out/experiments/experiment3_prefix_autocomplete";
        
        PrefixAutocompleteExperiment experiment = new PrefixAutocompleteExperiment();
        experiment.runExperiment(csvPath, graphChoice, nPrefixes, outputDir);
        // Run the same three sample autocomplete queries across all graph types
        experiment.runAllGraphsSampleShowcase(csvPath, "SIN");
        
        System.out.println("\n✅ Experiment 3 completed!");
        System.out.println("Results written to: " + outputDir);
    }
    
    /**
     * Runs Experiment 4: Cache Locality (CSR vs Adjacency Lists) 
     */
    private static void runExperiment4() throws IOException {
        System.out.println("\n=== Running Experiment 4: Cache Locality (CSR vs Adjacency Lists) ===");
        String csvPath = "data/cleaned_flights.csv";
        
        int numQueries = getIntInput("Number of test queries (default 100): ");
        if (numQueries <= 0) {
            numQueries = 100;
        }
        
        String outputDir = "out/experiments/experiment4_cache_locality";
        
        CacheLocalityExperiment experiment = 
            new CacheLocalityExperiment();
        experiment.runExperiment(csvPath, numQueries, outputDir);
        
        System.out.println("\n✅ Experiment 4 completed!");
        System.out.println("Results written to: " + outputDir);
    }
    
    /**
     * Runs Experiment 5: Pathfinding Performance Benchmark 
     */
    private static void runExperiment5() throws IOException {
        System.out.println("\n=== Running Experiment 5: Pathfinding Performance Benchmark ===");
        String csvPath = "data/cleaned_flights.csv";
        
        int numQueries = getIntInput("Number of test queries (default 100): ");
        if (numQueries <= 0) {
            numQueries = 100;
        }
        
        String outputDir = "out/experiments/experiment5_pathfinding_benchmark";
        
        PathfindingBenchmarkExperiment experiment = 
            new PathfindingBenchmarkExperiment();
        experiment.runBenchmark(csvPath, numQueries, outputDir);
        
        System.out.println("\n✅ Experiment 5 completed!");
        System.out.println("Results written to: " + outputDir);
    }

    /**
     * Gets constraints from user input
     */
    private static Constraints getConstraints() {
        System.out.println("\nOptional constraints (press Enter to skip):");

        // Max stops
        String maxStopsInput = getStringInput("Maximum stops (or unlimited): ");
        int maxStops = Integer.MAX_VALUE;
        if (!maxStopsInput.trim().isEmpty()) {
            try {
                maxStops = Integer.parseInt(maxStopsInput.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, using unlimited stops");
            }
        }

        // Airline allowlist
        String allowlistInput = getStringInput("Airline allowlist (comma-separated): ");
        Set<String> allowlist = new HashSet<>();
        if (!allowlistInput.trim().isEmpty()) {
            String[] airlines = allowlistInput.split(",");
            for (String airline : airlines) {
                allowlist.add(airline.trim());
            }
        }

        // Airline blocklist
        String blocklistInput = getStringInput("Airline blocklist (comma-separated): ");
        Set<String> blocklist = new HashSet<>();
        if (!blocklistInput.trim().isEmpty()) {
            String[] airlines = blocklistInput.split(",");
            for (String airline : airlines) {
                blocklist.add(airline.trim());
            }
        }

        return new Constraints(maxStops, allowlist.isEmpty() ? null : allowlist,
                blocklist.isEmpty() ? null : blocklist);
    }

    /**
     * Suggests similar nodes when a node is not found
     */
    private static void suggestSimilarNodes(String target, String type) {
        List<String> nodes = graph.nodes();
        List<String> suggestions = new ArrayList<>();

        String targetLower = target.toLowerCase();
        for (String node : nodes) {
            if (node.toLowerCase().contains(targetLower) || targetLower.contains(node.toLowerCase())) {
                suggestions.add(node);
            }
        }

        if (!suggestions.isEmpty()) {
            System.out.println("Did you mean one of these " + type + " countries?");
            for (String suggestion : suggestions.subList(0, Math.min(5, suggestions.size()))) {
                System.out.println("- " + suggestion);
            }
        }
    }

    /**
     * Displays ASCII route visualization
     */
    private static void displayAsciiRoute(PathResult result) {
        List<String> countries = result.getCountries();
        List<String> airlines = result.getAirlines();

        if (countries.size() < 2) {
            return;
        }

        StringBuilder route = new StringBuilder();
        for (int i = 0; i < countries.size() - 1; i++) {
            if (i > 0) {
                route.append(" ");
            }
            route.append(countries.get(i));
            route.append(" --(").append(airlines.get(i)).append(")--> ");
        }
        route.append(countries.get(countries.size() - 1));

        System.out.println(route.toString());
    }

    /**
     * Gets integer input from user
     */
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Gets string input from user
     */
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Menu option 4: Perform graph analysis
     */
    private static void performGraphAnalysis() {
        if (graph == null) {
            System.out.println("Error: No graph loaded. Please build graph from CSV first.");
            return;
        }

        System.out.println("=== Graph Analysis ===");

        GraphAnalyzer analyzer = new GraphAnalyzer(graph);
        AnalysisReportGenerator reportGenerator = new AnalysisReportGenerator();

        // Show quick summary
        reportGenerator.printQuickSummary(analyzer);

        // Detailed analysis
        System.out.println("Performing detailed analysis...");

        // Structure analysis
        System.out.println("\n--- Graph Structure ---");
        System.out.println(analyzer.analyzeStructure().toString());

        // Airline analysis
        System.out.println("\n--- Airline Analysis ---");
        System.out.println(analyzer.analyzeAirlines().toString());

        // Route analysis
        System.out.println("\n--- Route Analysis ---");
        System.out.println(analyzer.analyzeRoutes().toString());

        // Centrality analysis
        System.out.println("\n--- Centrality Analysis ---");
        Map<String, CentralityMetrics> centrality = analyzer.calculateCentrality();

        // Show top 5 countries by degree centrality
        List<Map.Entry<String, CentralityMetrics>> sortedByDegree = new ArrayList<>(centrality.entrySet());
        sortedByDegree.sort((a, b) -> Integer.compare(b.getValue().getDegree(), a.getValue().getDegree()));

        System.out.println("Top 5 countries by degree centrality:");
        for (int i = 0; i < Math.min(5, sortedByDegree.size()); i++) {
            Map.Entry<String, CentralityMetrics> entry = sortedByDegree.get(i);
            System.out.printf("%d. %s: %s%n", i + 1, entry.getKey(), entry.getValue().toString());
        }
    }


    /**
     * Menu option 5: Generate comprehensive analysis report
     */
    private static void generateAnalysisReport() {
        if (graph == null) {
            System.out.println("Error: No graph loaded. Please build graph from CSV first.");
            return;
        }

        System.out.println("=== Generate Analysis Report ===");

        String outputDir = getStringInput("Enter output directory (press Enter for 'out/analysis'): ");
        if (outputDir.trim().isEmpty()) {
            outputDir = "out/analysis";
        }

        try {
            GraphAnalyzer analyzer = new GraphAnalyzer(graph);
            DataStructureComparator comparator = new DataStructureComparator();
            AnalysisReportGenerator reportGenerator = new AnalysisReportGenerator();

            System.out.println("Generating comprehensive analysis report...");
            reportGenerator.generateCompleteReport(analyzer, comparator, outputDir);

            System.out.println("Analysis report generated successfully!");
            System.out.println("Files created:");
            System.out.println("- " + outputDir + "/analysis_report.md");
            System.out.println("- " + outputDir + "/centrality_data.csv");
            System.out.println("- " + outputDir + "/airline_data.csv");
            System.out.println("- " + outputDir + "/route_data.csv");

        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

}

import data.CsvReader;
import graph.Graph;
import algo.*;
import experiments.ExperimentRunner;
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
        System.out.println("3. Run experiments");
        System.out.println("4. Exit");
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
            graph = reader.readCsvAndBuildGraph(csvPath);
            
            System.out.println("Graph built successfully!");
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
        
        // Algorithm selection
        System.out.println("Select algorithm:");
        System.out.println("1. Dijkstra (default)");
        System.out.println("2. A* (Zero heuristic)");
        System.out.println("3. A* (Hop heuristic)");
        
        int algoChoice = getIntInput("Enter algorithm choice (1-3): ");
        if (algoChoice < 1 || algoChoice > 3) {
            algoChoice = 1; // Default to Dijkstra
        }
        
        // Optional constraints
        Constraints constraints = getConstraints();
        
        // Run the selected algorithm
        PathResult result = null;
        String algorithmName = "";
        
        switch (algoChoice) {
            case 1:
                Dijkstra dijkstra = new Dijkstra();
                result = dijkstra.findPath(graph, origin, destination, constraints);
                algorithmName = "Dijkstra";
                break;
            case 2:
                AStar aStarZero = new AStar();
                result = aStarZero.findPath(graph, origin, destination, new AStar.ZeroHeuristic(), constraints);
                algorithmName = "A* (Zero heuristic)";
                break;
            case 3:
                AStar aStarHop = new AStar();
                result = aStarHop.findPath(graph, origin, destination, new AStar.HopHeuristic(), constraints);
                algorithmName = "A* (Hop heuristic)";
                break;
        }
        
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
        if (graph == null) {
            System.out.println("Error: No graph loaded. Please build graph from CSV first.");
            return;
        }
        
        System.out.println("=== Run Experiments ===");
        
        int numQueries = getIntInput("Number of random queries (default 50): ");
        if (numQueries <= 0) {
            numQueries = 50;
        }
        
        try {
            ExperimentRunner runner = new ExperimentRunner(graph);
            runner.runExperiments(numQueries, "out/experiments");
            
            System.out.println("Experiments completed successfully!");
            System.out.println("Results written to:");
            System.out.println("- out/experiments/algorithms.csv");
            System.out.println("- out/experiments/README.md");
            
        } catch (IOException e) {
            System.err.println("Error running experiments: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
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
}

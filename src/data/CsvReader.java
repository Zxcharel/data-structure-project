package src.data;

import src.graph.AdjacencyListGraph;
import src.graph.Edge;
import src.graph.Graph;
import java.util.function.Supplier;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads CSV files containing flight data and builds a graph representation.
 * Handles case-insensitive header parsing and aggregates routes by airline.
 */
public class CsvReader {
    
    /**
     * Reads a CSV file and builds a graph from the flight data.
     * 
     * @param csvPath Path to the CSV file
     * @return A graph containing all the flight routes
     * @throws IOException if the file cannot be read
     */
    public Graph readCsvAndBuildGraph(String csvPath) throws IOException {
        Map<String, RouteAggregate> routeMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }
            
            // Parse header to find column indices
            String[] headers = parseCsvLine(line);
            ColumnIndices indices = findColumnIndices(headers);
            
            // Read data lines
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    FlightRecord record = parseFlightRecord(line, indices);
                    if (record != null) {
                        String routeKey = record.getRouteKey();
                        routeMap.computeIfAbsent(routeKey, 
                            k -> new RouteAggregate(record.getOriginCountry(), 
                                                  record.getDestinationCountry(), 
                                                  record.getAirline()))
                               .addRecord(record);
                    }
                } catch (Exception e) {
                    System.err.printf("Warning: Skipping malformed line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        }
        
        // Build graph from aggregated routes (default to AdjacencyListGraph)
        return buildGraphFromRoutes(routeMap);
    }

    /**
     * Reads a CSV file and builds a graph using the provided graph factory.
     *
     * @param csvPath Path to the CSV file
     * @param graphFactory Factory to create a Graph implementation
     * @return A graph containing all the flight routes
     * @throws IOException if the file cannot be read
     */
    public Graph readCsvAndBuildGraph(String csvPath, Supplier<Graph> graphFactory) throws IOException {
        Map<String, RouteAggregate> routeMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }

            // Parse header to find column indices
            String[] headers = parseCsvLine(line);
            ColumnIndices indices = findColumnIndices(headers);

            // Read data lines
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    FlightRecord record = parseFlightRecord(line, indices);
                    if (record != null) {
                        String routeKey = record.getRouteKey();
                        routeMap.computeIfAbsent(routeKey, 
                            k -> new RouteAggregate(record.getOriginCountry(), 
                                                  record.getDestinationCountry(), 
                                                  record.getAirline()))
                               .addRecord(record);
                    }
                } catch (Exception e) {
                    System.err.printf("Warning: Skipping malformed line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        }

        Graph graph = graphFactory.get();
        buildIntoGraphFromRoutes(routeMap, graph);
        return graph;
    }

    /**
     * Reads a CSV file and builds the graph into the provided Graph implementation.
     * Returns the same instance for chaining/assignment.
     */
    public Graph readCsvAndBuildGraph(String csvPath, Graph targetGraph) throws IOException {
        Map<String, RouteAggregate> routeMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = parseCsvLine(line);
            ColumnIndices indices = findColumnIndices(headers);
            
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    FlightRecord record = parseFlightRecord(line, indices);
                    if (record != null) {
                        String routeKey = record.getRouteKey();
                        routeMap.computeIfAbsent(routeKey, 
                            k -> new RouteAggregate(record.getOriginCountry(), 
                                                  record.getDestinationCountry(), 
                                                  record.getAirline()))
                               .addRecord(record);
                    }
                } catch (Exception e) {
                    System.err.printf("Warning: Skipping malformed line %d: %s%n", lineNumber, e.getMessage());
                }
            }
        }
        
        buildIntoGraphFromRoutes(routeMap, targetGraph);
        return targetGraph;
    }
    
    /**
     * Parses a CSV line, handling quoted fields
     */
    private String[] parseCsvLine(String line) {
        // Simple CSV parsing - assumes no commas within quoted fields for now
        return line.split(",");
    }
    
    /**
     * Finds the indices of required columns in the header
     */
    private ColumnIndices findColumnIndices(String[] headers) {
        ColumnIndices indices = new ColumnIndices();
        
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim().toLowerCase();
            
            if (header.contains("airline")) {
                indices.airlineIndex = i;
            } else if (header.contains("origin") && !header.contains("code")) {
                indices.originIndex = i;
            } else if (header.contains("destination") && !header.contains("code")) {
                indices.destinationIndex = i;
            } else if (header.contains("overall") && header.contains("rating")) {
                indices.overallRatingIndex = i;
            } else if (header.contains("value") && header.contains("money")) {
                indices.valueForMoneyIndex = i;
            } else if (header.contains("inflight") && header.contains("entertainment")) {
                indices.inflightEntertainmentIndex = i;
            } else if (header.contains("cabin") && header.contains("staff")) {
                indices.cabinStaffIndex = i;
            } else if (header.contains("seat") && header.contains("comfort")) {
                indices.seatComfortIndex = i;
            }
        }
        
        // Validate required columns
        if (indices.airlineIndex == -1) {
            throw new IllegalArgumentException("Required column 'airline' not found");
        }
        if (indices.originIndex == -1) {
            throw new IllegalArgumentException("Required column 'origin' not found");
        }
        if (indices.destinationIndex == -1) {
            throw new IllegalArgumentException("Required column 'destination' not found");
        }
        
        return indices;
    }
    
    /**
     * Parses a single flight record from a CSV line
     */
    private FlightRecord parseFlightRecord(String line, ColumnIndices indices) {
        String[] fields = parseCsvLine(line);
        
        if (fields.length <= Math.max(Math.max(indices.airlineIndex, indices.originIndex), indices.destinationIndex)) {
            return null; // Skip malformed lines
        }
        
        String airline = fields[indices.airlineIndex].trim();
        String origin = fields[indices.originIndex].trim();
        String destination = fields[indices.destinationIndex].trim();
        
        // Skip if essential fields are empty
        if (airline.isEmpty() || origin.isEmpty() || destination.isEmpty()) {
            return null;
        }
        
        int overallRating = parseRating(fields, indices.overallRatingIndex);
        int valueForMoney = parseRating(fields, indices.valueForMoneyIndex);
        int inflightEntertainment = parseRating(fields, indices.inflightEntertainmentIndex);
        int cabinStaff = parseRating(fields, indices.cabinStaffIndex);
        int seatComfort = parseRating(fields, indices.seatComfortIndex);
        
        return new FlightRecord(airline, origin, destination,
                               overallRating, valueForMoney, inflightEntertainment,
                               cabinStaff, seatComfort);
    }
    
    /**
     * Parses a rating field, returning 0 for missing/invalid values
     */
    private int parseRating(String[] fields, int index) {
        if (index == -1 || index >= fields.length) {
            return 0; // Column not found or out of bounds
        }
        
        String value = fields[index].trim();
        if (value.isEmpty()) {
            return 0; // Missing value
        }
        
        try {
            // Handle decimal ratings by rounding to nearest integer
            double rating = Double.parseDouble(value);
            return (int) Math.round(rating);
        } catch (NumberFormatException e) {
            return 0; // Invalid number
        }
    }
    
    /**
     * Builds a graph from the aggregated route data
     */
    private Graph buildGraphFromRoutes(Map<String, RouteAggregate> routeMap) {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        buildIntoGraphFromRoutes(routeMap, graph);
        return graph;
    }

    /**
     * Populates the provided target graph using the aggregated route data.
     */
    private void buildIntoGraphFromRoutes(Map<String, RouteAggregate> routeMap, Graph targetGraph) {
        for (RouteAggregate aggregate : routeMap.values()) {
            // Add nodes
            targetGraph.addNode(aggregate.getOriginCountry());
            targetGraph.addNode(aggregate.getDestinationCountry());
            
            // Add edge with calculated weight
            Edge edge = new Edge(
                aggregate.getDestinationCountry(),
                aggregate.getAirline(),
                aggregate.getAverageOverallRating(),
                aggregate.getAverageValueForMoney(),
                aggregate.getAverageInflightEntertainment(),
                aggregate.getAverageCabinStaff(),
                aggregate.getAverageSeatComfort(),
                aggregate.calculateWeight()
            );
            
            targetGraph.addEdge(aggregate.getOriginCountry(), edge);
        }
    }
    
    /**
     * Helper class to store column indices
     */
    private static class ColumnIndices {
        int airlineIndex = -1;
        int originIndex = -1;
        int destinationIndex = -1;
        int overallRatingIndex = -1;
        int valueForMoneyIndex = -1;
        int inflightEntertainmentIndex = -1;
        int cabinStaffIndex = -1;
        int seatComfortIndex = -1;
    }
}

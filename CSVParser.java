import java.io.*;
import java.util.*;
import common.FlightGraphInterface;

/**
 * CSV Parser for Flight Graph implementations
 * This parser reads flight data from CSV file and populates the graph
 * Used by both HashMap and LinkedList implementations
 */
public class CSVParser {
    
    /**
     * Parse CSV file and populate the flight graph (HashMap implementation)
     * @param filePath path to the CSV file
     * @return populated HashMapFlightGraph object
     * @throws IOException if file cannot be read
     */
    public static HashMapFlightGraph parseCSVToGraph(String filePath) throws IOException {
        HashMapFlightGraph graph = new HashMapFlightGraph();
        return parseCSVToGraph(filePath, graph);
    }
    
    /**
     * Parse CSV file and populate the LinkedList-based flight graph
     * @param filePath path to the CSV file
     * @return populated LinkedListGraph.FlightGraph object
     * @throws IOException if file cannot be read
     */
    public static FlightGraphInterface parseCSVToLinkedListGraph(String filePath) throws IOException {
        // Create LinkedList-based graph using reflection to avoid import issues
        try {
            Class<?> linkedListGraphClass = Class.forName("LinkedListGraph.LinkedListFlightGraph");
            FlightGraphInterface graph = (FlightGraphInterface) linkedListGraphClass.getDeclaredConstructor().newInstance();
            return parseCSVToGraph(filePath, graph);
        } catch (Exception e) {
            throw new IOException("Failed to create LinkedList-based FlightGraph: " + e.getMessage());
        }
    }
    
    /**
     * Parse CSV file and populate the DoublyLinkedList-based flight graph
     * @param filePath path to the CSV file
     * @return populated DoublyLinkedListGraph.DoublyLinkedListFlightGraph object
     * @throws IOException if file cannot be read
     */
    public static FlightGraphInterface parseCSVToDoublyLinkedListGraph(String filePath) throws IOException {
        // Create DoublyLinkedList-based graph using reflection to avoid import issues
        try {
            Class<?> doublyLinkedListGraphClass = Class.forName("DoublyLinkedListGraph.DoublyLinkedListFlightGraph");
            FlightGraphInterface graph = (FlightGraphInterface) doublyLinkedListGraphClass.getDeclaredConstructor().newInstance();
            return parseCSVToGraph(filePath, graph);
        } catch (Exception e) {
            throw new IOException("Failed to create DoublyLinkedList-based FlightGraph: " + e.getMessage());
        }
    }
    
    /**
     * Parse CSV file and populate the TwoDArray-based flight graph
     * @param filePath path to the CSV file
     * @return populated TwoDArrayGraph.TwoDArrayFlightGraph object
     * @throws IOException if file cannot be read
     */
    public static FlightGraphInterface parseCSVToTwoDArrayGraph(String filePath) throws IOException {
        // Create TwoDArray-based graph using reflection to avoid import issues
        try {
            Class<?> twoDArrayGraphClass = Class.forName("TwoDArrayGraph.TwoDArrayFlightGraph");
            FlightGraphInterface graph = (FlightGraphInterface) twoDArrayGraphClass.getDeclaredConstructor().newInstance();
            return parseCSVToGraph(filePath, graph);
        } catch (Exception e) {
            throw new IOException("Failed to create TwoDArray-based FlightGraph: " + e.getMessage());
        }
    }
    
    /**
     * Parse CSV into a provided FlightGraph implementation instance
     * @param filePath path to the CSV file
     * @param graph graph instance to populate
     * @return the populated graph instance
     * @throws IOException if file cannot be read
     */
    public static <T extends FlightGraphInterface> T parseCSVIntoGraph(String filePath, T graph) throws IOException {
        return parseCSVToGraph(filePath, graph);
    }
    
    /**
     * Generic method to parse CSV and populate any FlightGraph implementation
     * @param filePath path to the CSV file
     * @param graph the graph instance to populate
     * @return populated graph object
     * @throws IOException if file cannot be read
     */
    private static <T extends FlightGraphInterface> T parseCSVToGraph(String filePath, T graph) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) continue;
                
                String[] values = parseCSVLine(line);
                
                if (lineNumber == 1) {
                    // First line contains headers
                    headers = values;
                    continue;
                }
                
                try {
                    FlightData flightData = extractFlightDataFromCSV(values, headers);
                    if (flightData != null) {
                        graph.addFlightWithRatings(
                            flightData.sourceAirport,
                            flightData.destinationAirport,
                            flightData.airline,
                            flightData.overallRating,
                            flightData.valueForMoneyRating,
                            flightData.inflightEntertainmentRating,
                            flightData.cabinStaffRating,
                            flightData.seatComfortRating
                        );
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                    // Continue processing other lines
                }
            }
        }
        
        return graph;
    }
    
    /**
     * Parse a CSV line handling quoted values
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }
    
    /**
     * Extract flight data from CSV values
     */
    private static FlightData extractFlightDataFromCSV(String[] values, String[] headers) {
        FlightData data = new FlightData();
        
        // Map headers to indices
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].toLowerCase().trim(), i);
        }
        
        // Extract data based on header names
        data.sourceAirport = getValueByHeader(values, headerMap, "source", "origin", "from", "departure");
        data.destinationAirport = getValueByHeader(values, headerMap, "destination", "dest", "to", "arrival");
        data.airline = getValueByHeader(values, headerMap, "airline", "carrier");
        
        // Extract ratings with default values
        data.overallRating = getDoubleValueByHeader(values, headerMap, "overall", 3.0);
        data.valueForMoneyRating = getDoubleValueByHeader(values, headerMap, "value", 3.0);
        data.inflightEntertainmentRating = getDoubleValueByHeader(values, headerMap, "entertainment", 3.0);
        data.cabinStaffRating = getDoubleValueByHeader(values, headerMap, "staff", 3.0);
        data.seatComfortRating = getDoubleValueByHeader(values, headerMap, "seat", 3.0);
        
        // Validate required fields
        if (data.sourceAirport == null || data.sourceAirport.trim().isEmpty() ||
            data.destinationAirport == null || data.destinationAirport.trim().isEmpty() ||
            data.airline == null || data.airline.trim().isEmpty()) {
            return null; // Skip invalid rows
        }
        
        return data;
    }
    
    /**
     * Get string value by header name (tries multiple variations)
     */
    private static String getValueByHeader(String[] values, Map<String, Integer> headerMap, String... headerVariations) {
        for (String variation : headerVariations) {
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                if (entry.getKey().contains(variation)) {
                    int index = entry.getValue();
                    if (index < values.length) {
                        return values[index].trim();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get double value by header name (tries multiple variations)
     */
    private static double getDoubleValueByHeader(String[] values, Map<String, Integer> headerMap, String headerVariation, double defaultValue) {
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            if (entry.getKey().contains(headerVariation)) {
                int index = entry.getValue();
                if (index < values.length) {
                    try {
                        return Double.parseDouble(values[index].trim());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                }
            }
        }
        return defaultValue;
    }
    
    /**
     * Flight data container class
     */
    private static class FlightData {
        String sourceAirport;
        String destinationAirport;
        String airline;
        double overallRating;
        double valueForMoneyRating;
        double inflightEntertainmentRating;
        double cabinStaffRating;
        double seatComfortRating;
    }
    
    /**
     * Create a sample CSV file for testing
     */
    public static void createSampleCSV(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("Source Airport,Destination Airport,Airline,Overall Rating,Value for Money Rating,Inflight Entertainment Rating,Cabin Staff Rating,Seat Comfort Rating");
            
            // Write sample data
            writer.println("LAX,JFK,Delta Airlines,4.5,3.8,4.2,4.0,3.5");
            writer.println("LAX,JFK,American Airlines,4.2,4.0,3.5,4.1,3.8");
            writer.println("JFK,LAX,Delta Airlines,4.5,3.8,4.2,4.0,3.5");
            writer.println("LAX,ORD,United Airlines,3.8,4.2,3.0,3.9,3.2");
            writer.println("ORD,JFK,American Airlines,4.0,3.9,3.8,4.2,3.6");
            writer.println("JFK,ORD,United Airlines,3.9,4.1,3.2,4.0,3.4");
            writer.println("ORD,LAX,American Airlines,4.1,3.7,3.9,4.3,3.7");
        }
    }
}

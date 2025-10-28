package LinearArrayGraph;

import java.io.*;
import java.util.*;

/**
 * Simple CSV loader specifically for LinearArrayFlightGraph
 * This bypasses the CSVParser reflection issues
 */
public class LinearArrayCSVLoader {

    public static LinearArrayFlightGraph loadFromCSV(String filePath) throws IOException {
        LinearArrayFlightGraph graph = new LinearArrayFlightGraph();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines
                if (line.trim().isEmpty())
                    continue;

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
                                flightData.seatComfortRating);
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
    private static String getValueByHeader(String[] values, Map<String, Integer> headerMap,
            String... headerVariations) {
        for (String variation : headerVariations) {
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                if (entry.getKey().contains(variation)) {
                    int index = entry.getValue();
                    if (index < values.length) {
                        return values[index];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get double value by header name (tries multiple variations)
     */
    private static double getDoubleValueByHeader(String[] values, Map<String, Integer> headerMap,
            String headerVariation, double defaultValue) {
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            if (entry.getKey().contains(headerVariation)) {
                int index = entry.getValue();
                if (index < values.length) {
                    try {
                        return Double.parseDouble(values[index]);
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
}

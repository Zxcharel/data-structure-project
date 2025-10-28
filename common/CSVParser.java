package common;

import java.io.*;
import java.util.*;

/**
 * CSV Parser for Flight Graph implementations
 * Reads flight data from a CSV file and populates any FlightGraph implementation.
 */
public class CSVParser {
    
    /**
     * Parse CSV into a provided FlightGraph implementation instance
     * @param filePath path to the CSV file
     * @param graph graph instance to populate
     * @return the populated graph instance
     * @throws IOException if file cannot be read
     */
    public static <T extends FlightGraphInterface> T parseCSVIntoGraph(String filePath, T graph) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) continue;

                String[] values = parseCSVLine(line);

                if (lineNumber == 1) {
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
                }
            }
        }

        return graph;
    }

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

    private static FlightData extractFlightDataFromCSV(String[] values, String[] headers) {
        FlightData data = new FlightData();

        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].toLowerCase().trim(), i);
        }

        data.sourceAirport = getValueByHeader(values, headerMap, "source", "origin", "from", "departure");
        data.destinationAirport = getValueByHeader(values, headerMap, "destination", "dest", "to", "arrival");
        data.airline = getValueByHeader(values, headerMap, "airline", "carrier");

        data.overallRating = getDoubleValueByHeader(values, headerMap, "overall", 3.0);
        data.valueForMoneyRating = getDoubleValueByHeader(values, headerMap, "value", 3.0);
        data.inflightEntertainmentRating = getDoubleValueByHeader(values, headerMap, "entertainment", 3.0);
        data.cabinStaffRating = getDoubleValueByHeader(values, headerMap, "staff", 3.0);
        data.seatComfortRating = getDoubleValueByHeader(values, headerMap, "seat", 3.0);

        if (data.sourceAirport == null || data.sourceAirport.trim().isEmpty() ||
            data.destinationAirport == null || data.destinationAirport.trim().isEmpty() ||
            data.airline == null || data.airline.trim().isEmpty()) {
            return null;
        }

        return data;
    }

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



package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for I/O operations, particularly CSV writing.
 * Provides methods for writing experiment results and reports.
 */
public class IOUtils {
    
    /**
     * Writes a CSV file with the given headers and data rows
     * 
     * @param filePath Path to the output file
     * @param headers Column headers
     * @param rows Data rows (each row is an array of strings)
     * @throws IOException if the file cannot be written
     */
    public static void writeCsv(String filePath, String[] headers, List<String[]> rows) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write headers
            writeCsvRow(writer, headers);
            
            // Write data rows
            for (String[] row : rows) {
                writeCsvRow(writer, row);
            }
        }
    }
    
    /**
     * Writes a single CSV row to the writer
     */
    private static void writeCsvRow(FileWriter writer, String[] values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(",");
            }
            writer.write(escapeCsvValue(values[i]));
        }
        writer.write("\n");
    }
    
    /**
     * Escapes a CSV value (handles commas and quotes)
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Writes a Markdown file with the given content
     * 
     * @param filePath Path to the output file
     * @param content The markdown content
     * @throws IOException if the file cannot be written
     */
    public static void writeMarkdown(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
    
    /**
     * Ensures the parent directory of the given file path exists
     * 
     * @param filePath The file path
     * @throws IOException if the directory cannot be created
     */
    public static void ensureParentDirectoryExists(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        java.io.File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Formats a double value to a specified number of decimal places
     */
    public static String formatDouble(double value, int decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", value);
    }
    
    /**
     * Formats a percentage value
     */
    public static String formatPercentage(double value) {
        return formatDouble(value * 100, 1) + "%";
    }
}

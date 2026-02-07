package net.biomodels.jummp.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple Java program to fetch MIME types from a GitHub Gist
 * and build bidirectional maps:
 * - MIME type to extension(s) mapping (primary map from JSON)
 * - Extension to MIME type mapping (reverse map)
 *
 * JSON format: {"mime/type": [".ext1", ".ext2", ".ext3"], ...}
 * Example: {"image/jpeg": [".jpg", ".jpeg", ".jpe"], "application/pdf": [".pdf"]}
 *
 * This version uses only standard Java libraries (no external dependencies).
 */
public class SimpleMimeTypeMapper {

    private static final String GIST_ROOT = "https://gist.githubusercontent.com/ntung/";
    private static final String GIST_ID = "7a64db7885e166eb84383f83b8e6aeb2";
    private static final String GIST_URL = GIST_ROOT + GIST_ID +  "/raw/mime-types-to-file-extension.json";

    // MIME type to extension(s) mapping (e.g., "image/jpeg" -> [".jpg", ".jpeg", ".jpe"])
    private final Map<String, List<String>> mimeToExtensionsMap;

    // Extension to MIME type mapping (e.g., ".pdf" -> "application/pdf" or "pdf" -> "application/pdf")
    private final Map<String, String> extensionToMimeMap;

    public SimpleMimeTypeMapper() {
        this.mimeToExtensionsMap = new HashMap<>();
        this.extensionToMimeMap = new HashMap<>();
    }

    /**
     * Fetches the JSON content from the given URL
     */
    private String fetchJsonFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to fetch data. HTTP response code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();
        return response.toString();
    }

    /**
     * Removes surrounding quotes from a string
     */
    private String removeQuotes(String str) {
        if (str == null || str.length() < 2) {
            return str;
        }
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * Parses a JSON array string into a List of strings
     * Expects format: [".item1", ".item2", ".item3"]
     */
    private List<String> parseJsonArray(String arrayStr) {
        List<String> result = new ArrayList<>();

        // Remove surrounding brackets
        arrayStr = arrayStr.trim();
        if (arrayStr.startsWith("[")) {
            arrayStr = arrayStr.substring(1);
        }
        if (arrayStr.endsWith("]")) {
            arrayStr = arrayStr.substring(0, arrayStr.length() - 1);
        }

        if (arrayStr.trim().isEmpty()) {
            return result;
        }

        // Split by comma (handling quoted values)
        String[] items = arrayStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String item : items) {
            item = removeQuotes(item);
            if (!item.isEmpty()) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Simple JSON parser for MIME type to extensions mapping
     * Assumes format: {"mime/type": [".ext1", ".ext2"], "mime/type2": [".ext3"], ...}
     */
    private void parseJson(String json) {
        // Remove opening and closing braces
        json = json.trim();
        if (json.startsWith("{")) {
            json = json.substring(1);
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1);
        }

        // Parse key-value pairs where value is an array
        // We need to find pairs of "key": [...]
        int index = 0;
        while (index < json.length()) {
            // Find the key
            int keyStart = json.indexOf("\"", index);
            if (keyStart == -1) break;

            int keyEnd = json.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;

            String key = json.substring(keyStart + 1, keyEnd);

            // Find the colon
            int colonIndex = json.indexOf(":", keyEnd);
            if (colonIndex == -1) break;

            // Find the array start
            int arrayStart = json.indexOf("[", colonIndex);
            if (arrayStart == -1) break;

            // Find the matching array end
            int arrayEnd = findMatchingBracket(json, arrayStart);
            if (arrayEnd == -1) break;

            String arrayStr = json.substring(arrayStart, arrayEnd + 1);

            // Parse the array
            List<String> extensions = parseJsonArray(arrayStr);

            // Add to MIME -> extensions map
            mimeToExtensionsMap.put(key, extensions);

            // Add to extension -> MIME map (reverse mapping)
            for (String ext : extensions) {
                // Store both with and without dot prefix
                String extWithoutDot = ext.startsWith(".") ? ext.substring(1) : ext;
                extensionToMimeMap.put(ext, key);
                extensionToMimeMap.put(extWithoutDot, key);
            }

            // Move to next pair
            index = arrayEnd + 1;
        }
    }

    /**
     * Finds the matching closing bracket for an opening bracket
     */
    private int findMatchingBracket(String str, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1; // No matching bracket found
    }

    /**
     * Builds the MIME type map from the JSON data
     */
    public void buildMap() throws Exception {
        System.out.println("Fetching MIME types from GitHub Gist...");
        String jsonData = fetchJsonFromUrl(GIST_URL);

        System.out.println("Parsing JSON data...");
        parseJson(jsonData);

        System.out.println("Successfully loaded " + mimeToExtensionsMap.size() + " MIME types!");
        System.out.println("Built reverse map with " + (extensionToMimeMap.size() / 2) + " unique extensions!");
    }

    /**
     * Gets the MIME type for a given file extension
     * Accepts extensions with or without leading dot
     */
    public String getMimeType(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }

        // Try to get directly (handles both .ext and ext)
        String mimeType = extensionToMimeMap.get(extension);
        if (mimeType != null) {
            return mimeType;
        }

        // If not found, try adding/removing dot
        String alternate;
        if (extension.startsWith(".")) {
            alternate = extension.substring(1);
        } else {
            alternate = "." + extension;
        }

        mimeType = extensionToMimeMap.get(alternate);
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * Gets the list of file extensions for a given MIME type
     * Returns empty list if MIME type is not found
     */
    public List<String> getExtensions(String mimeType) {
        List<String> extensions = mimeToExtensionsMap.get(mimeType);
        return extensions != null ? new ArrayList<>(extensions) : new ArrayList<>();
    }

    /**
     * Gets the primary (first) extension for a given MIME type
     * Returns null if MIME type is not found
     */
    public String getPrimaryExtension(String mimeType) {
        List<String> extensions = mimeToExtensionsMap.get(mimeType);
        return (extensions != null && !extensions.isEmpty()) ? extensions.get(0) : null;
    }

    /**
     * Returns the MIME-to-extensions map
     */
    public Map<String, List<String>> getMimeToExtensionsMap() {
        // Create deep copy
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : mimeToExtensionsMap.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Returns the extension-to-MIME map
     */
    public Map<String, String> getExtensionToMimeMap() {
        return new HashMap<>(extensionToMimeMap);
    }

    /**
     * Prints some sample MIME -> extensions mappings
     */
    public void printSamples(int count) {
        System.out.println("\nSample MIME type -> Extensions mappings:");
        System.out.println("----------------------------");
        int i = 0;
        for (Map.Entry<String, List<String>> entry : mimeToExtensionsMap.entrySet()) {
            if (i >= count) break;
            System.out.printf("%-50s -> %s%n", entry.getKey(), entry.getValue());
            i++;
        }
    }

    /**
     * Prints sample reverse mappings (extension -> MIME)
     */
    public void printReverseSamples(int count) {
        System.out.println("\nSample Extension -> MIME type mappings:");
        System.out.println("----------------------------");
        int i = 0;
        boolean[] printed = new boolean[1];
        for (Map.Entry<String, String> entry : extensionToMimeMap.entrySet()) {
            // Only print extensions with dots to avoid duplicates
            if (entry.getKey().startsWith(".")) {
                if (i >= count) break;
                System.out.printf("%-10s -> %s%n", entry.getKey(), entry.getValue());
                i++;
            }
        }
    }

    /**
     * Prints all MIME -> extensions mappings sorted by MIME type
     */
    public void printAllMappings() {
        System.out.println("\nAll MIME type -> Extensions mappings:");
        System.out.println("----------------------------");
        mimeToExtensionsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        System.out.printf("%-50s -> %s%n", entry.getKey(), entry.getValue())
                );
    }

    /**
     * Prints all extension -> MIME mappings sorted by extension
     */
    public void printAllReverseMappings() {
        System.out.println("\nAll Extension -> MIME type mappings:");
        System.out.println("----------------------------");
        extensionToMimeMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("."))  // Only show dotted versions
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        System.out.printf("%-10s -> %s%n", entry.getKey(), entry.getValue())
                );
    }
}
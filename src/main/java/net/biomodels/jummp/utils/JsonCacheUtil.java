package net.biomodels.jummp.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class JsonCacheUtil {

    public static String getOrFetchJson(String urlString, String localCachePath, long maxAgeMinutes)
            throws IOException {
        File cacheFile = new File(System.getProperty("user.home"), localCachePath);

        // Check if cached file exists and is fresh
        if (cacheFile.exists()) {
            long maxAgeMillis = maxAgeMinutes * 60 * 1000;
            long fileAge = System.currentTimeMillis() - cacheFile.lastModified();
            if (fileAge < maxAgeMillis) {
                return new String(Files.readAllBytes(cacheFile.toPath()));
            }
        }

        // Create parent directories if they don't exist
        File parentDir = cacheFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Could not create directory " + parentDir.getAbsolutePath());
            }
        }

        // Fetch from URL
        String json = fetchFromUrl(urlString);

        // Save to cache
        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(json);
        }

        return json;
    }

    private static String fetchFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to fetch JSON: HTTP " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        return response.toString();
    }
}

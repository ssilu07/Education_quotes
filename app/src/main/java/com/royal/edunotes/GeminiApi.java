package com.royal.edunotes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight Gemini API client using HttpURLConnection.
 * Replaces google-genai SDK which is incompatible with Android
 * (uses Apache HttpClient that conflicts with Android's system classes).
 */
public class GeminiApi {

    private static final String TAG = "GeminiApi";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";

    /**
     * Sends a prompt to Gemini and returns the text response.
     * Must be called on a background thread.
     */
    public static String generateContent(String apiKey, String prompt) throws Exception {
        return generateContent(apiKey, DEFAULT_MODEL, prompt);
    }

    public static String generateContent(String apiKey, String model, String prompt) throws Exception {
        String urlStr = BASE_URL + model + ":generateContent?key=" + apiKey;

        // Build JSON request body
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);

        JSONArray partsArray = new JSONArray();
        partsArray.put(textPart);

        JSONObject content = new JSONObject();
        content.put("parts", partsArray);

        JSONArray contentsArray = new JSONArray();
        contentsArray.put(content);

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", contentsArray);

        // Make HTTP request
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(60000);

        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                return extractText(response.toString());
            } else {
                // Read error stream
                StringBuilder error = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line);
                    }
                }
                Log.e(TAG, "API error " + responseCode + ": " + error);
                throw new Exception("Gemini API error (" + responseCode + "): " + error);
            }
        } finally {
            conn.disconnect(); // ✅ connection leak fix - hamesha disconnect karo
        }
    }

    /**
     * Extracts text from Gemini API JSON response.
     */
    private static String extractText(String jsonResponse) throws Exception {
        JSONObject root = new JSONObject(jsonResponse);
        JSONArray candidates = root.getJSONArray("candidates");

        if (candidates.length() == 0) {
            return null;
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        JSONObject contentObj = firstCandidate.getJSONObject("content");
        JSONArray parts = contentObj.getJSONArray("parts");

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.getJSONObject(i);
            if (part.has("text")) {
                text.append(part.getString("text"));
            }
        }

        return text.length() > 0 ? text.toString() : null;
    }
}

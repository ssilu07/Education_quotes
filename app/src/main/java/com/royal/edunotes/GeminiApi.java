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
 *
 * Model fallback chain (highest free quota → lowest):
 *   1. gemini-2.0-flash-lite  (primary — highest free tier limit)
 *   2. gemini-1.5-flash-8b    (fallback 1)
 *   3. gemini-2.0-flash       (fallback 2 — last resort)
 *
 * On 429: auto-waits the retryDelay from the response (max 15 sec cap),
 * retries on the same model once, then moves to the next model in chain.
 */
public class GeminiApi {

    private static final String TAG = "GeminiApi";
    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    // Model chain: gemini-2.0-flash-lite has the most generous free quota
    private static final String[] MODEL_CHAIN = {
            "gemini-2.0-flash-lite",   // Primary — highest free tier
            "gemini-1.5-flash-8b",     // Fallback 1
            "gemini-2.0-flash"         // Fallback 2 — last resort
    };

    private static final int MAX_RETRIES_PER_MODEL = 1;  // 1 retry per model on 429
    private static final long MAX_RETRY_WAIT_MS    = 15_000L; // cap wait at 15 sec

    /**
     * Sends a prompt to Gemini and returns the text response.
     * Tries MODEL_CHAIN in order; on 429 waits retryDelay then retries once per model.
     * Must be called on a background thread.
     */
    public static String generateContent(String apiKey, String prompt) throws Exception {
        Exception lastException = null;

        for (String model : MODEL_CHAIN) {
            Log.d(TAG, "Trying model: " + model);

            for (int attempt = 0; attempt <= MAX_RETRIES_PER_MODEL; attempt++) {
                try {
                    return callApi(apiKey, model, prompt);
                } catch (QuotaExceededException e) {
                    lastException = e;
                    if (attempt < MAX_RETRIES_PER_MODEL) {
                        // Wait the suggested retryDelay (capped) then retry same model
                        long waitMs = Math.min(e.retryDelayMs, MAX_RETRY_WAIT_MS);
                        Log.w(TAG, "429 on " + model + ", waiting " + waitMs + "ms then retrying...");
                        try { Thread.sleep(waitMs); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "429 exhausted retries for " + model + ", trying next model.");
                    }
                }
                // Non-quota exceptions bubble up immediately (network, auth, etc.)
            }
        }

        // All models exhausted
        throw lastException != null ? lastException
                : new Exception("All Gemini models exhausted. Please try again later.");
    }

    /** Makes a single API call. Throws QuotaExceededException on 429, Exception on other errors. */
    private static String callApi(String apiKey, String model, String prompt) throws Exception {
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
                // Read error body
                StringBuilder error = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line);
                    }
                }
                String errorBody = error.toString();
                Log.e(TAG, "API error " + responseCode + ": " + errorBody);

                if (responseCode == 429) {
                    // Parse retryDelay from response (e.g. "3s" or "58s")
                    long retryDelayMs = parseRetryDelayMs(errorBody);
                    throw new QuotaExceededException(model, retryDelayMs);
                }

                throw new Exception("Gemini API error (" + responseCode + "): " + errorBody);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Parses retryDelay from Gemini 429 JSON error body.
     * Example: "retryDelay": "3s" → 3000ms, "retryDelay": "58s" → 58000ms
     */
    private static long parseRetryDelayMs(String errorBody) {
        try {
            JSONObject root = new JSONObject(errorBody);
            JSONObject err = root.optJSONObject("error");
            if (err != null) {
                JSONArray details = err.optJSONArray("details");
                if (details != null) {
                    for (int i = 0; i < details.length(); i++) {
                        JSONObject detail = details.optJSONObject(i);
                        if (detail != null && detail.has("retryDelay")) {
                            String delay = detail.getString("retryDelay"); // e.g. "3s" or "3.5s"
                            // Strip trailing 's' and parse
                            delay = delay.replace("s", "").trim();
                            double seconds = Double.parseDouble(delay);
                            return (long) (seconds * 1000L) + 500L; // +500ms safety buffer
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not parse retryDelay: " + e.getMessage());
        }
        return 5000L; // Default: wait 5 seconds if can't parse
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

    /** Thrown when Gemini returns 429 Resource Exhausted. */
    static class QuotaExceededException extends Exception {
        final long retryDelayMs;
        final String model;

        QuotaExceededException(String model, long retryDelayMs) {
            super("Quota exceeded for model: " + model + ". RetryDelay: " + retryDelayMs + "ms");
            this.model = model;
            this.retryDelayMs = retryDelayMs;
        }
    }
}

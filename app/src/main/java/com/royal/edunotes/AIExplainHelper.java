package com.royal.edunotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIExplainHelper {

    private static final String CACHE_PREF = "ai_explain_cache";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void explain(Context context, String vocabText) {
        // Check cache first
        SharedPreferences prefs = context.getSharedPreferences(CACHE_PREF, Context.MODE_PRIVATE);
        String cacheKey = vocabText.toLowerCase().trim();
        String cached = prefs.getString(cacheKey, null);

        if (cached != null) {
            showExplanationDialog(context, vocabText, cached);
            return;
        }

        // Show loading dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("AI Explaining...");
        builder.setMessage("Please wait...");
        builder.setCancelable(true);
        AlertDialog loadingDialog = builder.create();
        loadingDialog.show();

        executor.execute(() -> {
            try {
                String prompt = "Explain this vocabulary/phrase in simple English. "
                        + "Give: 1) Meaning 2) Three synonyms 3) Use in a sentence. "
                        + "Keep it brief and clear.\n\nVocab: " + vocabText;

                String explanation = GeminiApi.generateContent(
                        BuildConfig.GEMINI_API_KEY, prompt);

                // Cache it
                if (explanation != null && !explanation.isEmpty()) {
                    prefs.edit().putString(cacheKey, explanation).apply();
                }

                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    if (explanation != null && !explanation.isEmpty()) {
                        showExplanationDialog(context, vocabText, explanation);
                    } else {
                        showExplanationDialog(context, vocabText, "No explanation available.");
                    }
                });

            } catch (Exception e) {
                Log.e("AIExplain", "Error", e);
                ((android.app.Activity) context).runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    showExplanationDialog(context, vocabText, "Error: " + e.getMessage());
                });
            }
        });
    }

    private void showExplanationDialog(Context context, String title, String explanation) {
        new AlertDialog.Builder(context)
                .setTitle("AI Explanation")
                .setMessage(explanation)
                .setPositiveButton("OK", null)
                .setNeutralButton("Copy", (dialog, which) -> {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("explanation", explanation);
                    clipboard.setPrimaryClip(clip);
                    android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public void shutdown() {
        executor.shutdown();
    }
}

package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.royal.edunotes.BuildConfig;
import com.royal.edunotes.GeminiApi;
import com.royal.edunotes.R;
import com.royal.edunotes.TTSHelper;
import com.royal.edunotes.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private static final String CACHE_PREF = "word_detail_cache";

    private View layoutLoading;
    private View layoutError;
    private TextView tvError;
    private Button btnRetry;
    private CardView cardWordDetail;
    private TextView tvWordTitle, tvTrickKey, tvTrickExp, tvSynonyms, tvAntonyms;
    private ImageView ivBookmark;
    private LinearLayout btnCopy, btnBookmark, btnShare, btnTts;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TTSHelper ttsHelper;
    private String currentWord = "";
    private String fullContent = "";
    private boolean isBookmarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String query = getIntent().getStringExtra(Utility.SEARCH_KEY);
        currentWord = (query == null || query.isEmpty()) ? "" : query.trim();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentWord.isEmpty() ? "Search" : currentWord);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        layoutLoading   = findViewById(R.id.layout_loading);
        layoutError     = findViewById(R.id.layout_error);
        tvError         = findViewById(R.id.tv_error);
        btnRetry        = findViewById(R.id.btn_retry);
        cardWordDetail  = findViewById(R.id.card_word_detail);
        tvWordTitle     = findViewById(R.id.tv_word_title);
        tvTrickKey      = findViewById(R.id.tv_trick_key);
        tvTrickExp      = findViewById(R.id.tv_trick_exp);
        tvSynonyms      = findViewById(R.id.tv_synonyms);
        tvAntonyms      = findViewById(R.id.tv_antonyms);
        ivBookmark      = findViewById(R.id.iv_bookmark);
        btnCopy         = findViewById(R.id.btn_copy);
        btnBookmark     = findViewById(R.id.btn_bookmark);
        btnShare        = findViewById(R.id.btn_share);
        btnTts          = findViewById(R.id.btn_tts);

        ttsHelper = new TTSHelper(this);

        if (currentWord.isEmpty()) {
            showError("Please enter a word to search.");
            return;
        }

        saveSearchHistory(currentWord);
        fetchWordDetail(currentWord);
    }

    private void fetchWordDetail(String word) {
        // ✅ Bug fix: API key empty hai toh seedha error dikhao
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.isEmpty()) {
            showError("AI API key not configured. Please set GEMINI_API_KEY in local.properties.");
            return;
        }

        // Check cache first
        String cached = getCached(word);
        if (cached != null) {
            parseAndDisplay(word, cached);
            return;
        }

        showLoading();

        executor.execute(() -> {
            try {
                Log.d("SearchActivity", "Calling Gemini for: " + word);
                Log.d("SearchActivity", "API key length: " + BuildConfig.GEMINI_API_KEY.length());
                String prompt = buildPrompt(word);
                String response = GeminiApi.generateContent(BuildConfig.GEMINI_API_KEY, prompt);
                Log.d("SearchActivity", "Response received, length: " + (response != null ? response.length() : 0));
                if (response != null && !response.isEmpty()) {
                    saveCache(word, response);
                    runOnUiThread(() -> parseAndDisplay(word, response));
                } else {
                    runOnUiThread(() -> showError("No response from AI. Please try again."));
                }
            } catch (Exception e) {
                Log.e("SearchActivity", "Gemini call failed: " + e.getMessage(), e);
                runOnUiThread(() -> showError("Error: " + e.getMessage()));
            }
        });
    }

    private String buildPrompt(String word) {
        return "You are an expert in creating desi Hindi memory tricks (mnemonics) for English vocabulary. "
                + "For the word \"" + word + "\", provide the following strictly in JSON format (no extra text, no markdown):\n"
                + "{\n"
                + "  \"hindi_meaning\": \"Hindi meaning of the word\",\n"
                + "  \"trick_key\": \"A related Hindi/Hinglish word that sounds like the English word (Key)\",\n"
                + "  \"trick_exp\": \"A creative Hindi sentence using the trick_key to explain the meaning (Exp)\",\n"
                + "  \"synonyms\": \"Comma separated 2-3 English synonyms\",\n"
                + "  \"antonyms\": \"Comma separated 2-3 English antonyms\"\n"
                + "}";
    }

    private void parseAndDisplay(String word, String jsonText) {
        try {
            // Strip markdown code blocks if present
            String cleaned = jsonText.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
            }

            JSONObject obj = new JSONObject(cleaned);

            String hindiMeaning = obj.optString("hindi_meaning", "");
            String trickKey = obj.optString("trick_key", "");
            String trickExp = obj.optString("trick_exp", "");
            
            // Note: Requested comma separated strings, not JSONArrays.
            String syns = obj.optString("synonyms", "—");
            String ants = obj.optString("antonyms", "—");

            // Format word title
            String capitalizedWord = word.substring(0, 1).toUpperCase(Locale.ENGLISH) + word.substring(1).toLowerCase(Locale.ENGLISH);
            String titleText = capitalizedWord + "- " + hindiMeaning;
            
            String keyText = "Key " + trickKey;
            String expText = "Exp-" + trickExp;
            String synText = "Syn-" + syns;
            String antText = "Ant-" + ants;

            fullContent = titleText + "\n\n"
                    + keyText + "\n\n"
                    + expText + "\n\n"
                    + synText + "\n\n"
                    + antText;

            tvWordTitle.setText(titleText);
            tvTrickKey.setText(keyText);
            tvTrickExp.setText(expText);
            tvSynonyms.setText(synText);
            tvAntonyms.setText(antText);

            showCard();
            setupBottomActions();

        } catch (Exception e) {
            // JSON parse failed — show raw text nicely
            fullContent = jsonText;
            tvWordTitle.setText(word.toUpperCase(Locale.ENGLISH));
            tvTrickKey.setText("—");
            tvTrickExp.setText(jsonText);
            tvSynonyms.setText("—");
            tvAntonyms.setText("—");
            showCard();
            setupBottomActions();
        }
    }

    private void setupBottomActions() {
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("word", fullContent));
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
        });

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            ivBookmark.setImageResource(isBookmarked ? R.drawable.starfilled : R.drawable.star);
            Toast.makeText(this, isBookmarked ? "Bookmarked!" : "Removed bookmark", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, fullContent);
            startActivity(Intent.createChooser(share, "Share via"));
        });

        btnTts.setOnClickListener(v -> ttsHelper.speak(fullContent));
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);      // ✅ error layout bhi hide karo
        cardWordDetail.setVisibility(View.GONE);
    }

    private void showCard() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);       // ✅ error layout bhi hide karo
        cardWordDetail.setVisibility(View.VISIBLE);
    }

    private void showError(String msg) {
        layoutLoading.setVisibility(View.GONE);
        cardWordDetail.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        String display;
        if (msg == null || msg.isEmpty()) {
            display = "Something went wrong. Please try again.";
        } else if (msg.contains("Unable to resolve host") || msg.contains("Network")) {
            display = "No internet connection.\nPlease check your network and retry.";
        } else if (msg.contains("401") || msg.contains("403")) {
            display = "AI service error: Invalid API key.\nPlease contact support.";
        } else if (msg.contains("Quota exceeded") || msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED")) {
            // 429 — all models in fallback chain are also quota-exhausted
            display = "⚠️ AI quota limit reached for today.\n\n"
                    + "The free Gemini API limit has been exceeded. "
                    + "Please wait a few minutes and try again, or try a different word.";
        } else if (msg.contains("API key") && msg.contains("empty")) {
            display = "AI feature not configured. Please set up the Gemini API key.";
        } else {
            display = msg;
        }
        tvError.setText(display);
        btnRetry.setOnClickListener(v -> {
            layoutError.setVisibility(View.GONE);
            fetchWordDetail(currentWord);
        });
    }

    private void saveCache(String word, String response) {
        getSharedPreferences(CACHE_PREF, MODE_PRIVATE)
                .edit().putString(word.toLowerCase(Locale.ENGLISH).trim(), response).apply();
    }

    private String getCached(String word) {
        return getSharedPreferences(CACHE_PREF, MODE_PRIVATE)
                .getString(word.toLowerCase(Locale.ENGLISH).trim(), null);
    }

    private void saveSearchHistory(String query) {
        SharedPreferences prefs = getSharedPreferences("search_history", MODE_PRIVATE);
        String existing = prefs.getString("history", "");
        String[] items = existing.split("\\|\\|");
        StringBuilder sb = new StringBuilder(query);
        int count = 1;
        for (String item : items) {
            if (!item.isEmpty() && !item.equalsIgnoreCase(query) && count < 20) {
                sb.append("||").append(item);
                count++;
            }
        }
        prefs.edit().putString("history", sb.toString()).apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (ttsHelper != null) ttsHelper.shutdown();
        executor.shutdown();
        super.onDestroy();
    }
}

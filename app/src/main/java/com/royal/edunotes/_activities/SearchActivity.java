package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.royal.edunotes.AIExplainHelper;
import com.royal.edunotes.BuildConfig;
import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.ShareUtils;
import com.royal.edunotes.TTSHelper;
import com.royal.edunotes.Utility;
import com.royal.edunotes.VerticalViewPager;
import com.royal.edunotes._adapters.VerticlePagerAdapter;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.royal.edunotes.GeminiApi;


public class SearchActivity extends AppCompatActivity implements VerticlePagerAdapter.ClickInterface {
    String title;
    MyDatabase myDatabase;
    ArrayList<QuoteModel> quoteModels;
    DatabaseHelper db;
    VerticlePagerAdapter verticlePagerAdapter;
    TextView nodata, resultTv;
    VerticalViewPager verticalViewPager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TTSHelper ttsHelper;
    private AIExplainHelper aiExplainHelper;
    private ProgressManager progressManager;

    private static final String GEMINI_CACHE_PREF = "gemini_cache";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        title = intent.getStringExtra(Utility.SEARCH_KEY);

        if (title == null || title.isEmpty()) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            title = Utility.DEFAULT_TITLE;
        } else {
            getSupportActionBar().setTitle(title);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        nodata = findViewById(R.id.nodata);
        resultTv = findViewById(R.id.resultTV);
        verticalViewPager = findViewById(R.id.vPager);

        ttsHelper = new TTSHelper(this);
        aiExplainHelper = new AIExplainHelper();
        progressManager = new ProgressManager(this);

        // Save search to history
        saveSearchHistory(title);

        // Search on background thread
        performSearch(title);
    }

    private void performSearch(String query) {
        resultTv.setText("Searching...");

        executor.execute(() -> {
            String[] filesToSearch = {
                    "life_quotes",
                    "inspirational_quote",
                    "happiness_quotes",
                    "beautiful_quotes",
                    "change_quote",
                    "introvert_quotes",
                    "hope_quotes",
                    "travel_quotes",
                    "trust_quotes",
                    "martin_luther_quotes",
                    "freedom_quotes"
            };

            ArrayList<QuoteModel> results = new ArrayList<>();

            for (String dbName : filesToSearch) {
                MyDatabase myDb = new MyDatabase(SearchActivity.this, dbName, dbName);
                ArrayList<QuoteModel> tempList = myDb.getSearchedData(query);
                for (QuoteModel q : tempList) {
                    q.setCategoryName(dbName);
                }
                results.addAll(tempList);
            }

            Collections.shuffle(results);

            DatabaseHelper dbHelper = new DatabaseHelper(SearchActivity.this);
            ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) dbHelper.getAllNotes();

            runOnUiThread(() -> {
                quoteModels = results;
                db = dbHelper;

                if (quoteModels.isEmpty()) {
                    verticalViewPager.setVisibility(View.INVISIBLE);
                    nodata.setVisibility(View.VISIBLE);
                    resultTv.setText(quoteModels.size() + " Results");
                    fetchGeminiResponse(query);
                } else {
                    resultTv.setText(quoteModels.size() + " Results");
                    verticlePagerAdapter = new VerticlePagerAdapter(SearchActivity.this, quoteModels, this, modelDatabases);
                    verticalViewPager.setAdapter(verticlePagerAdapter);
                }
            });
        });
    }

    private void fetchGeminiResponse(String query) {
        // Check cache first
        String cached = getCachedResponse(query);
        if (cached != null) {
            resultTv.setText("AI: " + cached);
            return;
        }

        resultTv.setText("Trying AI...");

        executor.execute(() -> {
            try {
                String aiText = GeminiApi.generateContent(
                        BuildConfig.GEMINI_API_KEY, query);

                // Cache the response
                if (aiText != null && !aiText.isEmpty()) {
                    cacheGeminiResponse(query, aiText);
                }

                runOnUiThread(() ->
                        resultTv.setText(aiText != null && !aiText.isEmpty()
                                ? "AI: " + aiText
                                : "No response from Gemini.")
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        resultTv.setText("Gemini Error: " + e.getMessage())
                );
                Log.e("Gemini", "fetch failed", e);
            }
        });
    }

    private void cacheGeminiResponse(String query, String response) {
        SharedPreferences prefs = getSharedPreferences(GEMINI_CACHE_PREF, MODE_PRIVATE);
        prefs.edit().putString(query.toLowerCase().trim(), response).apply();
    }

    private String getCachedResponse(String query) {
        SharedPreferences prefs = getSharedPreferences(GEMINI_CACHE_PREF, MODE_PRIVATE);
        return prefs.getString(query.toLowerCase().trim(), null);
    }

    private void saveSearchHistory(String query) {
        SharedPreferences prefs = getSharedPreferences("search_history", MODE_PRIVATE);
        String existing = prefs.getString("history", "");
        // Prepend new query, keep last 20
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
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {
        if (quoteModel.isBookmared()) {
            db = new DatabaseHelper(SearchActivity.this, quoteModel);
            db.deleteNote(quoteModel);
            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
            quoteModel.setBookmared(false);
            verticlePagerAdapter.notifyDataSetChanged();
        } else {
            db = new DatabaseHelper(SearchActivity.this, quoteModel);
            quoteModel.setBookmark("1");
            db.insertNote(quoteModel);
            star.setImageDrawable(getResources().getDrawable(R.drawable.starfilled));
            quoteModel.setBookmared(true);
            verticlePagerAdapter.notifyDataSetChanged();
            if (progressManager != null) progressManager.onWordBookmarked();
        }
    }

    @Override
    public void onCopyClick(QuoteModel quoteModel) {
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", quoteModel.getQuote());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onShareClick(QuoteModel quoteModel) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, quoteModel.getQuote());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    @Override
    public void onTTSClick(QuoteModel quoteModel) {
        ttsHelper.speak(quoteModel.getQuote());
    }

    @Override
    public void onExplainClick(QuoteModel quoteModel) {
        aiExplainHelper.explain(this, quoteModel.getQuote());
    }

    @Override
    public void onShareAsImageClick(View cardView) {
        ShareUtils.shareViewAsImage(this, cardView);
    }

    @Override
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=Hirvasoft")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Hirvasoft")));
        }
    }

    @Override
    protected void onDestroy() {
        if (verticlePagerAdapter != null) {
            verticlePagerAdapter.cleanup();
        }
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        executor.shutdown();
        super.onDestroy();
    }
}

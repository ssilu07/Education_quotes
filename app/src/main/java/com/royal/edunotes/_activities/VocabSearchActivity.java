package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.CategoryDataProvider;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes._adapters.SearchResultAdapter;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Searches every vocab/idiom chapter already bundled in the app and jumps straight to a match. */
public class VocabSearchActivity extends AppCompatActivity {

    private static final int MAX_RESULTS = 60;
    private static final int COLLECT_LIMIT = 500;
    private static final long SEARCH_DEBOUNCE_MS = 300;

    private EditText etQuery;
    private ProgressBar progress;
    private TextView tvNoResults;
    private RecyclerView rvResults;
    private View btnAiLookup;

    private final List<SearchResult> results = new ArrayList<>();
    private SearchResultAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private String currentQuery = "";
    private int searchRequestId = 0;

    public static class SearchResult {
        public final int quoteId;
        public final String word;
        public final String meaning;
        public final String chapterTitle;
        public final String dbName;
        public final String screenCheck;

        SearchResult(int quoteId, String word, String meaning, String chapterTitle, String dbName, String screenCheck) {
            this.quoteId = quoteId;
            this.word = word;
            this.meaning = meaning;
            this.chapterTitle = chapterTitle;
            this.dbName = dbName;
            this.screenCheck = screenCheck;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocab_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Vocab");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etQuery = findViewById(R.id.et_search_query);
        progress = findViewById(R.id.progress_search);
        tvNoResults = findViewById(R.id.tv_no_results);
        rvResults = findViewById(R.id.rv_search_results);
        btnAiLookup = findViewById(R.id.btn_ai_lookup);
        View btnDoSearch = findViewById(R.id.btn_do_search);

        adapter = new SearchResultAdapter(this, results, this::openResult);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        btnDoSearch.setOnClickListener(v -> {
            cancelPendingSearch();
            runSearch(etQuery.getText().toString());
        });
        etQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                cancelPendingSearch();
                runSearch(etQuery.getText().toString());
                return true;
            }
            return false;
        });
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                scheduleSearch(s.toString());
            }
        });

        btnAiLookup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(Utility.SEARCH_KEY, currentQuery);
            startActivity(intent);
        });

        String incomingQuery = getIntent().getStringExtra(Utility.SEARCH_KEY);
        if (incomingQuery != null && !incomingQuery.trim().isEmpty()) {
            etQuery.setText(incomingQuery);
            etQuery.setSelection(etQuery.getText().length());
            cancelPendingSearch(); // setText above queued a debounced search via the watcher; run immediately instead
            runSearch(incomingQuery);
        }
    }

    /** Debounces live typing so we don't re-scan every chapter on every keystroke. */
    private void scheduleSearch(String query) {
        cancelPendingSearch();
        if (query == null || query.trim().isEmpty()) {
            clearResults();
            return;
        }
        pendingSearch = () -> runSearch(query);
        searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private void cancelPendingSearch() {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
            pendingSearch = null;
        }
    }

    private void clearResults() {
        currentQuery = "";
        results.clear();
        adapter.notifyDataSetChanged();
        progress.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
        rvResults.setVisibility(View.GONE);
        btnAiLookup.setVisibility(View.GONE);
    }

    private void runSearch(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            clearResults();
            return;
        }
        currentQuery = trimmed;
        final int requestId = ++searchRequestId;

        progress.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        btnAiLookup.setVisibility(View.GONE);
        rvResults.setVisibility(View.GONE);

        executor.execute(() -> {
            List<SearchResult> found = searchAllChapters(trimmed);
            runOnUiThread(() -> {
                if (requestId != searchRequestId) return; // a newer search superseded this one
                showResults(found);
            });
        });
    }

    private List<SearchResult> searchAllChapters(String query) {
        List<SearchResult> found = new ArrayList<>();
        outer:
        for (CategoryDataProvider.Category cat : CategoryDataProvider.getAllCategories()) {
            for (CategoryDataProvider.SubCategory sub : cat.subCategories) {
                MyDatabase db = new MyDatabase(this, sub.dbName, sub.title);
                ArrayList<QuoteModel> matches = db.getWordMatches(query);
                for (QuoteModel q : matches) {
                    String[] parsed = parseWordAndMeaning(q.getQuote());
                    found.add(new SearchResult(q.getId(), parsed[0], parsed[1], sub.title, sub.dbName, cat.screenCheck));
                    if (found.size() >= COLLECT_LIMIT) break outer;
                }
            }
        }
        rankByRelevance(found, query);
        if (found.size() > MAX_RESULTS) {
            return new ArrayList<>(found.subList(0, MAX_RESULTS));
        }
        return found;
    }

    /** Exact word match first, then prefix/word/substring matches on the word, then matches found only in the meaning. */
    private static void rankByRelevance(List<SearchResult> found, String query) {
        final Map<SearchResult, Integer> scores = new HashMap<>();
        for (SearchResult r : found) {
            scores.put(r, relevanceScore(query, r.word, r.meaning));
        }
        found.sort((a, b) -> {
            int diff = scores.get(a) - scores.get(b);
            if (diff != 0) return diff;
            return a.word.compareToIgnoreCase(b.word);
        });
    }

    private static int relevanceScore(String query, String word, String meaning) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        String w = word == null ? "" : word.toLowerCase(Locale.ROOT).trim();
        String m = meaning == null ? "" : meaning.toLowerCase(Locale.ROOT).trim();

        if (w.equals(q)) return 0;
        if (w.startsWith(q)) return 1;
        if (containsWholeWord(w, q)) return 2;
        if (w.contains(q)) return 3;
        if (containsWholeWord(m, q)) return 4;
        if (m.contains(q)) return 5;
        return 6;
    }

    private static boolean containsWholeWord(String haystack, String needle) {
        if (needle.isEmpty()) return false;
        int idx = haystack.indexOf(needle);
        while (idx >= 0) {
            boolean leftOk = idx == 0 || !Character.isLetterOrDigit(haystack.charAt(idx - 1));
            int endIdx = idx + needle.length();
            boolean rightOk = endIdx == haystack.length() || !Character.isLetterOrDigit(haystack.charAt(endIdx));
            if (leftOk && rightOk) return true;
            idx = haystack.indexOf(needle, idx + 1);
        }
        return false;
    }

    private static String[] parseWordAndMeaning(String quoteText) {
        if (quoteText == null) return new String[]{"", ""};
        String firstLine = quoteText.split("\r\n|\n", 2)[0].trim();
        int dashIdx = firstLine.indexOf('-');
        if (dashIdx > 0) {
            return new String[]{firstLine.substring(0, dashIdx).trim(), firstLine.substring(dashIdx + 1).trim()};
        }
        return new String[]{firstLine, ""};
    }

    private void showResults(List<SearchResult> found) {
        progress.setVisibility(View.GONE);
        btnAiLookup.setVisibility(View.VISIBLE);

        results.clear();
        results.addAll(found);
        adapter.setHighlightQuery(currentQuery);
        adapter.notifyDataSetChanged();

        if (found.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
        }
    }

    private void openResult(SearchResult result) {
        Utility.ScreenCheck = result.screenCheck;
        Intent intent = new Intent(this, HackList.class);
        intent.putExtra(Utility.TITLE_KEY, result.chapterTitle);
        intent.putExtra(Utility.DBNAME_KEY, result.dbName);
        intent.putExtra(Utility.JUMP_TO_ID_KEY, result.quoteId);
        startActivity(intent);
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
        cancelPendingSearch();
        executor.shutdown();
        super.onDestroy();
    }
}

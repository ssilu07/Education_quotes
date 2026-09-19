package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.AIExplainHelper;
import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.ReadingProgressManager;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.ShareUtils;
import com.royal.edunotes.TTSHelper;
import com.royal.edunotes.Utility;
import com.royal.edunotes.VerticalViewPager;
import com.royal.edunotes._adapters.NotesListAdapter;
import com.royal.edunotes._adapters.VerticlePagerAdapter;
import com.royal.edunotes._database.CardProgressDatabase;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.Arrays;


public class HackList extends AppCompatActivity implements VerticlePagerAdapter.ClickInterface {
    MyDatabase myDatabase;
    Intent intent;
    String dbname, category;
    ArrayList<QuoteModel> quoteModels = new ArrayList<>();
    ArrayList<QuoteModel> allQuoteModels = new ArrayList<>();
    VerticlePagerAdapter verticlePagerAdapter;
    DatabaseHelper db;
    TTSHelper ttsHelper;
    AIExplainHelper aiExplainHelper;
    ProgressManager progressManager;
    ReadingProgressManager readingProgressManager;
    SettingsManager settingsManager;
    VerticalViewPager verticalViewPager;

    private RecyclerView rvNotes;
    private NotesListAdapter notesListAdapter;
    private View fabCardsView;
    private boolean isNotesView = false;
    private View toggleActionView;

    private static final String[] FILTER_VALUES = {"ALL", "LEARNED", "NOT_LEARNED"};
    private static final String[] FILTER_LABELS = {"All words", "Learned only", "Not learned yet"};
    private DatabaseHelper databaseHelper;
    private int swipeCount = 0;
    private boolean reviewPrompted = false;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hack_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        com.royal.edunotes.WindowInsetsHelper.applyEdgeToEdge(this, toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        intent = getIntent();

        dbname = intent.getStringExtra(Utility.DBNAME_KEY);
        category = intent.getStringExtra(Utility.TITLE_KEY);

        Log.e("TAG===", "Entity : " + dbname + "   " + category);


        verticalViewPager = (VerticalViewPager) findViewById(R.id.vPager);

        myDatabase = new MyDatabase(HackList.this, dbname, category);

        settingsManager = new SettingsManager(this);
        allQuoteModels = myDatabase.getPoses();
        quoteModels = filterList(allQuoteModels, currentFilter);

//        Collections.shuffle(quoteModels);


        db = new DatabaseHelper(HackList.this);
        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();

        Log.e("TAG1===", "SIZEEEE : " + modelDatabases.size());


        ttsHelper = new TTSHelper(this);
        aiExplainHelper = new AIExplainHelper();
        progressManager = new ProgressManager(this);
        readingProgressManager = new ReadingProgressManager(this);

        verticlePagerAdapter = new VerticlePagerAdapter(HackList.this, quoteModels, this, modelDatabases);
        verticlePagerAdapter.setCategoryName(dbname);

        verticalViewPager.setAdapter(verticlePagerAdapter);

        verticalViewPager.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (progressManager != null) progressManager.onWordRead();

                if (verticlePagerAdapter != null && quoteModels != null) {
                    int dataIndex = verticlePagerAdapter.getDataPosition(position);
                    if (dataIndex >= 0 && dataIndex < quoteModels.size()) {
                        if (readingProgressManager != null && dbname != null) {
                            readingProgressManager.savePosition(dbname, dataIndex, quoteModels.size());
                        }
                        CardProgressDatabase progressDb = new CardProgressDatabase(HackList.this);
                        progressDb.markViewed(dbname, quoteModels.get(dataIndex).getId());
                    }
                }
                
                swipeCount++;
                if (swipeCount == 20 && !reviewPrompted) {
                    showInAppReview();
                    reviewPrompted = true;
                }
            }
        });

        int jumpToId = intent.getIntExtra(Utility.JUMP_TO_ID_KEY, -1);
        if (jumpToId != -1) {
            for (int i = 0; i < quoteModels.size(); i++) {
                if (quoteModels.get(i).getId() == jumpToId) {
                    verticalViewPager.setCurrentItem(verticlePagerAdapter.getPagerPositionForDataIndex(i), false);
                    break;
                }
            }
        } else if (readingProgressManager != null && dbname != null && !quoteModels.isEmpty()) {
            int savedDataIndex = readingProgressManager.getSavedPosition(dbname);
            if (savedDataIndex > 0 && savedDataIndex < quoteModels.size()) {
                int pagerPos = verticlePagerAdapter.getPagerPositionForDataIndex(savedDataIndex);
                verticalViewPager.setCurrentItem(pagerPos, false);
                Toast.makeText(this, "Resuming from Card " + (savedDataIndex + 1) + " of " + quoteModels.size(), Toast.LENGTH_SHORT).show();
            }
        }

        markInitialCardViewed();


        // Initialize Notes List View
        rvNotes = findViewById(R.id.rvNotes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        notesListAdapter = new NotesListAdapter(this, quoteModels, new NotesListAdapter.NoteItemClickListener() {
            @Override
            public void onNoteCardClick(QuoteModel quoteModel, int dataIndex) {
                switchToCardsView(dataIndex);
            }

            @Override
            public void onNoteBookmarkClick(QuoteModel quoteModel, ImageView starView, int position) {
                onBoookmarkClick(quoteModel, starView);
                if (notesListAdapter != null) {
                    notesListAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onNoteCopyClick(QuoteModel quoteModel) {
                HackList.this.onCopyClick(quoteModel);
            }

            @Override
            public void onNoteShareClick(QuoteModel quoteModel, View cardView) {
                ShareUtils.shareText(HackList.this, quoteModel.getQuote());
            }

            @Override
            public void onNoteTTSClick(QuoteModel quoteModel) {
                HackList.this.onTTSClick(quoteModel);
            }

            @Override
            public void onNoteLearnedClick(QuoteModel quoteModel, TextView learnedView, int position) {
                HackList.this.onLearnedClick(quoteModel, learnedView);
                if (notesListAdapter != null) {
                    notesListAdapter.notifyItemChanged(position);
                }
            }
        }, modelDatabases);
        rvNotes.setAdapter(notesListAdapter);

        fabCardsView = findViewById(R.id.fab_cards_view);
        if (fabCardsView != null) {
            fabCardsView.setOnClickListener(v -> switchToCardsView(-1));
        }
    }

    private void toggleViewMode() {
        if (isNotesView) {
            switchToCardsView(-1);
        } else {
            switchToNotesView();
        }
    }

    private void switchToNotesView() {
        isNotesView = true;
        verticalViewPager.setVisibility(View.GONE);
        rvNotes.setVisibility(View.VISIBLE);
        if (fabCardsView != null) {
            fabCardsView.setVisibility(View.VISIBLE);
        }

        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
        if (notesListAdapter != null) {
            notesListAdapter.updateData(quoteModels, modelDatabases);
        }

        int currentPagerPos = verticalViewPager.getCurrentItem();
        int dataIndex = verticlePagerAdapter.getDataPosition(currentPagerPos);
        if (dataIndex >= 0 && dataIndex < quoteModels.size()) {
            rvNotes.scrollToPosition(dataIndex);
            if (readingProgressManager != null && dbname != null) {
                readingProgressManager.savePosition(dbname, dataIndex, quoteModels.size());
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(quoteModels.size() + " Notes");
        }

        updateToggleMenuUI();
        Toast.makeText(this, "Notes View: All content in scrollable list", Toast.LENGTH_SHORT).show();
    }

    private void switchToCardsView(int targetDataIndex) {
        isNotesView = false;
        rvNotes.setVisibility(View.GONE);
        if (fabCardsView != null) {
            fabCardsView.setVisibility(View.GONE);
        }
        verticalViewPager.setVisibility(View.VISIBLE);

        if (targetDataIndex >= 0 && targetDataIndex < quoteModels.size()) {
            int pagerPos = verticlePagerAdapter.getPagerPositionForDataIndex(targetDataIndex);
            verticalViewPager.setCurrentItem(pagerPos, false);
            if (readingProgressManager != null && dbname != null) {
                readingProgressManager.savePosition(dbname, targetDataIndex, quoteModels.size());
            }
        } else {
            LinearLayoutManager lm = (LinearLayoutManager) rvNotes.getLayoutManager();
            if (lm != null) {
                int firstVisible = lm.findFirstVisibleItemPosition();
                if (firstVisible >= 0 && firstVisible < quoteModels.size()) {
                    int pagerPos = verticlePagerAdapter.getPagerPositionForDataIndex(firstVisible);
                    verticalViewPager.setCurrentItem(pagerPos, false);
                    if (readingProgressManager != null && dbname != null) {
                        readingProgressManager.savePosition(dbname, firstVisible, quoteModels.size());
                    }
                }
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }

        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
        verticlePagerAdapter.updateData(quoteModels, modelDatabases);

        updateToggleMenuUI();
    }

    private void updateToggleMenuUI() {
        if (toggleActionView != null) {
            ImageView ivIcon = toggleActionView.findViewById(R.id.iv_mode_icon);
            TextView tvText = toggleActionView.findViewById(R.id.tv_mode_text);
            if (ivIcon != null && tvText != null) {
                if (isNotesView) {
                    ivIcon.setImageResource(R.drawable.ic_cards_mode);
                    tvText.setText("Cards");
                } else {
                    ivIcon.setImageResource(R.drawable.ic_notes_mode);
                    tvText.setText("Notes");
                }
            }
        }
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (isNotesView) {
            switchToCardsView(-1);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hack_list, menu);
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_view);
        if (toggleItem != null) {
            toggleActionView = toggleItem.getActionView();
            if (toggleActionView != null) {
                ImageView ivIcon = toggleActionView.findViewById(R.id.iv_mode_icon);
                TextView tvText = toggleActionView.findViewById(R.id.tv_mode_text);
                if (ivIcon != null && tvText != null) {
                    if (isNotesView) {
                        ivIcon.setImageResource(R.drawable.ic_cards_mode);
                        tvText.setText("Cards");
                    } else {
                        ivIcon.setImageResource(R.drawable.ic_notes_mode);
                        tvText.setText("Notes");
                    }
                }
                toggleActionView.setOnClickListener(v -> toggleViewMode());
            }
            if (isNotesView) {
                toggleItem.setTitle("Cards View");
                toggleItem.setIcon(R.drawable.ic_cards_mode);
            } else {
                toggleItem.setTitle("Notes View");
                toggleItem.setIcon(R.drawable.ic_notes_mode);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_toggle_view) {
            toggleViewMode();
            return true;
        }
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_start_beginning) {
            if (readingProgressManager != null && dbname != null) {
                readingProgressManager.clearPosition(dbname);
            }
            if (verticalViewPager != null) {
                verticalViewPager.setCurrentItem(0, false);
            }
            if (rvNotes != null) {
                rvNotes.scrollToPosition(0);
            }
            Toast.makeText(this, "Started from Card 1", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        int checkedIndex = Arrays.asList(FILTER_VALUES).indexOf(currentFilter);
        new AlertDialog.Builder(this)
                .setTitle("Show")
                .setSingleChoiceItems(FILTER_LABELS, checkedIndex, (dialog, which) -> {
                    currentFilter = FILTER_VALUES[which];
                    applyFilter();
                    dialog.dismiss();
                })
                .show();
    }

    /** Keeps each quote's learned flag fresh and returns only the ones matching the chosen filter. */
    private ArrayList<QuoteModel> filterList(ArrayList<QuoteModel> source, String filter) {
        ArrayList<QuoteModel> result = new ArrayList<>();
        for (QuoteModel quoteModel : source) {
            boolean learned = settingsManager.isWordLearned(quoteModel.getQuote());
            quoteModel.setLearned(learned);
            if ("LEARNED".equals(filter) && !learned) continue;
            if ("NOT_LEARNED".equals(filter) && learned) continue;
            result.add(quoteModel);
        }
        return result;
    }

    private void applyFilter() {
        quoteModels = filterList(allQuoteModels, currentFilter);
        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
        verticlePagerAdapter.updateData(quoteModels, modelDatabases);
        verticalViewPager.setCurrentItem(0, false);

        if (notesListAdapter != null) {
            notesListAdapter.updateData(quoteModels, modelDatabases);
            rvNotes.scrollToPosition(0);
        }

        if (isNotesView && getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(quoteModels.size() + " Notes");
        }

        if (quoteModels.isEmpty()) {
            Toast.makeText(this, "No words in this filter yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {
        if (quoteModel == null || quoteModel.getQuote() == null) return;

        if (db == null) {
            db = new DatabaseHelper(HackList.this);
        }

        boolean isCurrentlyBookmarked = db.isBookmarked(quoteModel.getQuote());

        if (isCurrentlyBookmarked) {
            // Remove from bookmark table
            db.deleteNote(quoteModel);
            quoteModel.setBookmared(false);
            quoteModel.setBookmark("0");
            star.setImageResource(R.drawable.star);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), false);
            }
            if (notesListAdapter != null) {
                notesListAdapter.setBookmarked(quoteModel.getQuote(), false);
            }
        } else {
            // Add to bookmark table
            quoteModel.setBookmark("1");
            quoteModel.setBookmared(true);
            if (quoteModel.getCategoryName() == null || quoteModel.getCategoryName().isEmpty()) {
                quoteModel.setCategoryName(category != null ? category : dbname);
            }
            db.insertNote(quoteModel);
            star.setImageResource(R.drawable.starfilled);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), true);
            }
            if (notesListAdapter != null) {
                notesListAdapter.setBookmarked(quoteModel.getQuote(), true);
            }
            if (progressManager != null) progressManager.onWordBookmarked();
        }
    }
    /*private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }*/

    @Override
    public void onCopyClick(QuoteModel quoteModel) {
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", quoteModel.getQuote());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onShareAsImageClick(View cardView, String text) {
        ShareUtils.shareViewAsImage(this, cardView, text);
    }

    @Override
    public void onTTSClick(QuoteModel quoteModel) {
        ttsHelper.speak(quoteModel.getQuote());
    }

    @Override
    public void onLearnedClick(QuoteModel quoteModel, TextView learnedLabel) {
        boolean newState = !quoteModel.isLearned();
        settingsManager.setWordLearned(quoteModel.getQuote(), newState);
        quoteModel.setLearned(newState);
        learnedLabel.setText(newState ? getString(R.string.learned_on_label) : getString(R.string.learned_off_label));

        if (!"ALL".equals(currentFilter)) {
            applyFilter();
        }
    }

    @Override
    public void onQuizClick() {
        if (dbname != null && dbname.startsWith("grammar_")) {
            Intent quizIntent = new Intent(HackList.this, GrammarQuizActivity.class);
            quizIntent.putExtra("GRAMMAR_TOPIC", dbname);
            startActivity(quizIntent);
        } else {
            Intent quizIntent = new Intent(HackList.this, DailyQuizActivity.class);
            startActivity(quizIntent);
        }
    }

    @Override
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        }
    }

    private void showInAppReview() {
        try {
            com.google.android.play.core.review.ReviewManager manager = com.google.android.play.core.review.ReviewManagerFactory.create(this);
            com.google.android.gms.tasks.Task<com.google.android.play.core.review.ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    com.google.android.play.core.review.ReviewInfo reviewInfo = task.getResult();
                    com.google.android.gms.tasks.Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                    flow.addOnCompleteListener(flowTask -> {
                        // The flow has finished.
                    });
                } else {
                    // There was some problem, log or handle the error code.
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markInitialCardViewed() {
        if (dbname == null || quoteModels == null || quoteModels.isEmpty()) return;
        int currentPagerPos = verticalViewPager != null ? verticalViewPager.getCurrentItem() : 0;
        int dataIndex = verticlePagerAdapter != null ? verticlePagerAdapter.getDataPosition(currentPagerPos) : 0;
        if (dataIndex >= 0 && dataIndex < quoteModels.size()) {
            CardProgressDatabase progressDb = new CardProgressDatabase(this);
            progressDb.markViewed(dbname, quoteModels.get(dataIndex).getId());
        }
    }

    private void saveCurrentPosition() {
        if (readingProgressManager == null || dbname == null || quoteModels == null || quoteModels.isEmpty()) {
            return;
        }
        int currentDataIndex = 0;
        if (isNotesView && rvNotes != null) {
            LinearLayoutManager lm = (LinearLayoutManager) rvNotes.getLayoutManager();
            if (lm != null) {
                int firstVisible = lm.findFirstVisibleItemPosition();
                if (firstVisible >= 0 && firstVisible < quoteModels.size()) {
                    currentDataIndex = firstVisible;
                }
            }
        } else if (verticalViewPager != null && verticlePagerAdapter != null) {
            int pagerPos = verticalViewPager.getCurrentItem();
            currentDataIndex = verticlePagerAdapter.getDataPosition(pagerPos);
        }
        if (currentDataIndex >= 0 && currentDataIndex < quoteModels.size()) {
            readingProgressManager.savePosition(dbname, currentDataIndex, quoteModels.size());
            CardProgressDatabase progressDb = new CardProgressDatabase(this);
            progressDb.markViewed(dbname, quoteModels.get(currentDataIndex).getId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentPosition();
    }

    @Override
    protected void onDestroy() {
        if (verticlePagerAdapter != null) verticlePagerAdapter.cleanup();
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        super.onDestroy();
    }
}

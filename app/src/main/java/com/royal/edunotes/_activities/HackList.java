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

import com.royal.edunotes.AIExplainHelper;
import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
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
    SettingsManager settingsManager;
    VerticalViewPager verticalViewPager;

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

        verticlePagerAdapter = new VerticlePagerAdapter(HackList.this, quoteModels, this, modelDatabases);
        verticlePagerAdapter.setCategoryName(category);

        verticalViewPager.setAdapter(verticlePagerAdapter);

        verticalViewPager.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (progressManager != null) progressManager.onWordRead();
                
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
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hack_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
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

        Log.e("TAGGG ===",quoteModel.getTimestamp());
        Log.e("TAGGG ===", String.valueOf(quoteModel.getId()));
        Log.e("TAGGG ===",quoteModel.getQuote());
        Log.e("TAGGG ===",quoteModel.getCategoryName());


        if (quoteModel.isBookmared()) {
//            Remove from bookmark table


            db = new DatabaseHelper(HackList.this, quoteModel);

            db.deleteNote(quoteModel);

            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
            quoteModel.setBookmared(false);

        } else {

            db = new DatabaseHelper(HackList.this, quoteModel);

            quoteModel.setBookmark("1");

            db.insertNote(quoteModel);

            star.setImageDrawable(getResources().getDrawable(R.drawable.starfilled));
            quoteModel.setBookmared(true);
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
        Intent quizIntent = new Intent(HackList.this, DailyQuizActivity.class);
        startActivity(quizIntent);
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

    @Override
    protected void onDestroy() {
        if (verticlePagerAdapter != null) verticlePagerAdapter.cleanup();
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        super.onDestroy();
    }
}

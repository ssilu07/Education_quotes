package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.royal.edunotes.AIExplainHelper;
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


public class HackList extends AppCompatActivity implements VerticlePagerAdapter.ClickInterface {
    MyDatabase myDatabase;
    Intent intent;
    String dbname, category;
    ArrayList<QuoteModel> quoteModels = new ArrayList<>();
    VerticlePagerAdapter verticlePagerAdapter;
    DatabaseHelper db;
    TTSHelper ttsHelper;
    AIExplainHelper aiExplainHelper;
    ProgressManager progressManager;

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


        VerticalViewPager verticalViewPager = (VerticalViewPager) findViewById(R.id.vPager);

        myDatabase = new MyDatabase(HackList.this, dbname, category);

        quoteModels = myDatabase.getPoses();

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
            }
        });




    }

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
    public void onShareAsImageClick(View cardView) {
        ShareUtils.shareViewAsImage(this, cardView);
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

    @Override
    protected void onDestroy() {
        if (verticlePagerAdapter != null) verticlePagerAdapter.cleanup();
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        super.onDestroy();
    }
}

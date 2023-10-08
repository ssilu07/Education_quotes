package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes.VerticalViewPager;
import com.royal.edunotes._adapters.VerticlePagerAdapter;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.Collections;

public class SearchActivity extends AppCompatActivity implements VerticlePagerAdapter.ClickInterface {
    String title;
    MyDatabase myDatabase;
    ArrayList<QuoteModel> quoteModels;
    DatabaseHelper db;
    VerticlePagerAdapter verticlePagerAdapter;
    TextView nodata,resultTv;
    InterstitialAd mInterstitialAd;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        title = intent.getStringExtra(Utility.SEARCH_KEY);

        Log.e("TAG===", "SEARCH KEY :::::" + title);

        if (title.equals("") || title == null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            title = Utility.DEFAULT_TITLE;
        } else {
            getSupportActionBar().setTitle(title);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        nodata = (TextView) findViewById(R.id.nodata);
        resultTv  =(TextView) findViewById(R.id.resultTV);

        VerticalViewPager verticalViewPager = (VerticalViewPager) findViewById(R.id.vPager);
        myDatabase = new MyDatabase(SearchActivity.this, "alldata");

        quoteModels = myDatabase.getSearchedData(title);

        Collections.shuffle(quoteModels);

        Log.e("TAg===", "FINAL SIZE SEARCHED : " + quoteModels.size());


        db = new DatabaseHelper(SearchActivity.this);
        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();

        Log.e("TAG1===", "SIZEEEE : " + modelDatabases.size());

        if (quoteModels.size() == 0) {

            verticalViewPager.setVisibility(View.INVISIBLE);
            nodata.setVisibility(View.VISIBLE);
            resultTv.setText(quoteModels.size()+" Results");

        } else {

            resultTv.setText(quoteModels.size()+" Results");
            verticlePagerAdapter = new VerticlePagerAdapter(SearchActivity.this, quoteModels, this, modelDatabases);
            verticalViewPager.setAdapter(verticlePagerAdapter);

        }


    }
    @Override
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {

        Log.e("TAGGG ===",quoteModel.getTimestamp());
        Log.e("TAGGG ===", String.valueOf(quoteModel.getId()));
        Log.e("TAGGG ===",quoteModel.getQuote());
        Log.e("TAGGG ===",quoteModel.getCategoryName());


        mInterstitialAd = new InterstitialAd(SearchActivity.this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e("TAG===", "Error ad :" + i);
            }
        });



        if (quoteModel.isBookmared()) {
//            Remove from bookmark table


            db = new DatabaseHelper(SearchActivity.this, quoteModel);

            db.deleteNote(quoteModel);




            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
            quoteModel.setBookmared(false);
            verticlePagerAdapter.notifyDataSetChanged();


        } else {

//           Add in to bookmark table

            db = new DatabaseHelper(SearchActivity.this, quoteModel);

            quoteModel.setBookmark("1");

            db.insertNote(quoteModel);


            star.setImageDrawable(getResources().getDrawable(R.drawable.starfilled));
            quoteModel.setBookmared(true);
            verticlePagerAdapter.notifyDataSetChanged();



        }
    }
    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
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
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=Hirvasoft")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Hirvasoft")));
        }
    }
}

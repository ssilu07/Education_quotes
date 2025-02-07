package com.royal.edunotes._adapters;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._models.QuoteModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by rizvan on 12/13/16.
 */

public class VerticlePagerAdapter extends PagerAdapter {

    private static final String TAG = "VerticlePagerAdapter";
    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<QuoteModel> quoteModels;
    String category;
    ClickInterface clickInterface;
    DatabaseHelper db;

    ArrayList<ModelDatabase> modelDatabases;

    public VerticlePagerAdapter(Context context, ArrayList<QuoteModel> quoteModels, ClickInterface clickInterface, ArrayList<ModelDatabase> modelDatabases) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.quoteModels = quoteModels;
        this.clickInterface = clickInterface;
        this.modelDatabases = modelDatabases;
        Log.d(TAG, "Adapter instantiated");

        // Initialize AdMob
        MobileAds.initialize(mContext, initializationStatus -> {});
    }

    @Override
    public int getCount() {
        return quoteModels.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

        if (Utility.ScreenCheck.equals("Vocab")) {
            CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
            CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
            TextView titleTxt = itemView.findViewById(R.id.title);
            TextView hackTxt = itemView.findViewById(R.id.tv_vocab);
            ImageView hackTxt2 = itemView.findViewById(R.id.iv_vocab);

            LinearLayout copy = itemView.findViewById(R.id.copyLLVocab);
            final LinearLayout starLL = itemView.findViewById(R.id.starLLVocab);
            LinearLayout share = itemView.findViewById(R.id.shareLLVocab);
            LinearLayout otherapp = itemView.findViewById(R.id.ourappLLVocab);

            cardViewVocab.setVisibility(View.VISIBLE);
            cardViewIdiom.setVisibility(View.GONE);

            final ImageView star = itemView.findViewById(R.id.star);

            QuoteModel currentQuote = quoteModels.get(position);
            if (currentQuote == null) {
                Log.e(TAG, "instantiateItem: currentQuote is null at position " + position);
                return itemView;
            }

            hackTxt.setText(currentQuote.getQuote());
            String url = currentQuote.getValue();

            if (url != null && !url.isEmpty()) {
                hackTxt2.setVisibility(View.VISIBLE);
                Picasso.get().load(url).into(hackTxt2);
                Log.d(TAG, "instantiateItem: Image loaded for position " + position);
            } else {
                hackTxt2.setVisibility(View.GONE);
                Log.d(TAG, "instantiateItem: No image for position " + position);
            }

            int[] colors = {Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47), Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120), Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)};
            int randomIndex = (int) (Math.random() * colors.length);
            int randomColor = colors[randomIndex];

            hackTxt.setTextColor(randomColor);

            if (modelDatabases != null) {
                for (ModelDatabase modelDatabase : modelDatabases) {
                    if (modelDatabase != null && currentQuote.getQuote() != null && currentQuote.getQuote().equals(modelDatabase.getNote())) {
                        currentQuote.setBookmark("1");
                        currentQuote.setBookmared(true);
                        Log.d(TAG, "instantiateItem: Quote bookmarked at position " + position);
                        break;
                    } else {
                        currentQuote.setBookmark("0");
                        currentQuote.setBookmared(false);
                    }
                }

                for (ModelDatabase modelDatabase : modelDatabases) {
                    if (modelDatabase != null && currentQuote.getValue() != null && currentQuote.getValue().equals(modelDatabase.getNote())) {
                        currentQuote.setBookmark("1");
                        currentQuote.setBookmared(true);
                        Log.d(TAG, "instantiateItem: Quote value bookmarked at position " + position);
                        break;
                    } else {
                        currentQuote.setBookmark("0");
                        currentQuote.setBookmared(false);
                    }
                }
            } else {
                Log.e(TAG, "instantiateItem: modelDatabases is null");
            }

            if (currentQuote.isBookmared()) {
                star.setImageResource(R.drawable.starfilled);
            } else {
                star.setImageResource(R.drawable.star);
            }

            copy.setOnClickListener(view -> {
                clickInterface.onCopyClick(currentQuote);
            });

            starLL.setOnClickListener(view -> {
                clickInterface.onBoookmarkClick(currentQuote, star);
            });

            share.setOnClickListener(view -> {
                clickInterface.onShareClick(currentQuote);
            });

            otherapp.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Other apps clicked at position " + position);
                clickInterface.onMoreAppsClick();
            });
        } else if (Utility.ScreenCheck.equals("Idiom")){
            CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
            CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
            TextView titleTxt = itemView.findViewById(R.id.title);
            TextView hackTxt = itemView.findViewById(R.id.tv_idiom);
            ImageView hackTxt2 = itemView.findViewById(R.id.iv_idiom);

            LinearLayout copy = itemView.findViewById(R.id.copyLLIdiom);
            final LinearLayout starLL = itemView.findViewById(R.id.starLLIdiom);
            LinearLayout share = itemView.findViewById(R.id.shareLLIdiom);
            LinearLayout otherapp = itemView.findViewById(R.id.ourappLLIdiom);

            cardViewVocab.setVisibility(View.GONE);
            cardViewIdiom.setVisibility(View.VISIBLE);
            final ImageView star = itemView.findViewById(R.id.star);

            QuoteModel currentQuote = quoteModels.get(position);
            if (currentQuote == null) {
                Log.e(TAG, "instantiateItem: currentQuote is null at position " + position);
                return itemView;
            }

            hackTxt.setText(currentQuote.getQuote());
            String url = currentQuote.getValue();

            if (url != null && !url.isEmpty()) {
                hackTxt2.setVisibility(View.VISIBLE);
                Picasso.get().load(url).into(hackTxt2);
                Log.d(TAG, "instantiateItem: Image loaded for position " + position);
            } else {
                hackTxt2.setVisibility(View.GONE);
                Log.d(TAG, "instantiateItem: No image for position " + position);
            }

            int[] colors = {Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47), Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120), Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)};
            int randomIndex = (int) (Math.random() * colors.length);
            int randomColor = colors[randomIndex];

            hackTxt.setTextColor(randomColor);

            if (modelDatabases != null) {
                for (ModelDatabase modelDatabase : modelDatabases) {
                    if (modelDatabase != null && currentQuote.getQuote() != null && currentQuote.getQuote().equals(modelDatabase.getNote())) {
                        currentQuote.setBookmark("1");
                        currentQuote.setBookmared(true);
                        Log.d(TAG, "instantiateItem: Quote bookmarked at position " + position);
                        break;
                    } else {
                        currentQuote.setBookmark("0");
                        currentQuote.setBookmared(false);
                    }
                }

                for (ModelDatabase modelDatabase : modelDatabases) {
                    if (modelDatabase != null && currentQuote.getValue() != null && currentQuote.getValue().equals(modelDatabase.getNote())) {
                        currentQuote.setBookmark("1");
                        currentQuote.setBookmared(true);
                        Log.d(TAG, "instantiateItem: Quote value bookmarked at position " + position);
                        break;
                    } else {
                        currentQuote.setBookmark("0");
                        currentQuote.setBookmared(false);
                    }
                }
            } else {
                Log.e(TAG, "instantiateItem: modelDatabases is null");
            }

            if (currentQuote.isBookmared()) {
                star.setImageResource(R.drawable.starfilled);
            } else {
                star.setImageResource(R.drawable.star);
            }

            copy.setOnClickListener(view -> {
                clickInterface.onCopyClick(currentQuote);
            });

            starLL.setOnClickListener(view -> {
                clickInterface.onBoookmarkClick(currentQuote, star);
            });

            share.setOnClickListener(view -> {
                clickInterface.onShareClick(currentQuote);
            });

            otherapp.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Other apps clicked at position " + position);
                clickInterface.onMoreAppsClick();
            });

        }




        // Load AdMob Banner
    //    AdView adView = itemView.findViewById(R.id.adView); // This is the correct way to get the AdView
     //   adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);

        // Retrieve AdUnitId from strings.xml
        String adUnitId = mContext.getResources().getString(R.string.banner_ad);
     //   adView.setAdUnitId(adUnitId); // Set the AdUnitId from string resource

 /*       AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("AdMob", "Ad Loaded Successfully");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Handle the error and log it
                Log.e("AdMob", "Ad failed to load: " + adError.getMessage());
            }

            @Override
            public void onAdOpened() {
                Log.d("AdMob", "Ad Opened");
            }

            @Override
            public void onAdClosed() {
                Log.d("AdMob", "Ad Closed");
            }
        });*/


        container.addView(itemView);
        Log.d(TAG, "instantiateItem: itemView added for position " + position);



        return itemView;
    }

    public interface ClickInterface {
        void onBoookmarkClick(QuoteModel CategoryModel, ImageView star);

        void onCopyClick(QuoteModel CategoryModel);

        void onShareClick(QuoteModel CategoryModel);

        void onMoreAppsClick();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
        Log.d(TAG, "destroyItem: itemView removed for position " + position);
    }
}

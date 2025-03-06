package com.royal.edunotes._adapters;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
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
        int adCount = quoteModels.size() / 5; // Insert an ad every 5 items
        return quoteModels.size() + adCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;  // ✅ Fix: Remove explicit casting
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        int actualPosition = position - (position / 6); // Adjust position to skip ads
        View itemView = null;  // ✅ Always declare itemView at start

        if (Utility.ScreenCheck.equals("Vocab")) {
            // 🔹 Show Ad Every 6th Position (For "Vocab" Section)
            if (position > 0 && (position + 1) % 6 == 0) {
                itemView = mLayoutInflater.inflate(R.layout.native_ad_layout, container, false);
                if (itemView != null) {
                    loadNativeAd(itemView);
                } else {
                    Log.e(TAG, "instantiateItem: Failed to inflate native_ad_layout at position " + position);
                }
            }
            else {
                itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

            CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
            CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
         //   TextView titleTxt = itemView.findViewById(R.id.title);
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
             /*   for (ModelDatabase modelDatabase : modelDatabases) {
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
                }*/

                ///only check the quote because not required to compare  images and its identifier is "value"
                for (int i = 0; i < modelDatabases.size(); i++) {
                    Log.d("BOOKMARK_CHECK", "Checking Quote: " + quoteModels.get(position).getQuote() + " at position: " + position);
                    Log.d("BOOKMARK_CHECK", "ModelDatabase Note: " + modelDatabases.get(i).getNote() + " at position: " + i);

                    if (quoteModels.get(position).getQuote().equals(modelDatabases.get(i).getNote())) {
                        Log.d("BOOKMARK_CHECK", "MATCH FOUND at position: " + position);
                        quoteModels.get(position).setBookmark("1");
                        quoteModels.get(position).setBookmared(true);
                        break;
                    } else {
                        Log.d("BOOKMARK_CHECK", "No match at position: " + position);
                        quoteModels.get(position).setBookmark("0");
                        quoteModels.get(position).setBookmared(false);
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

        } else if (Utility.ScreenCheck.equals("Idiom")) {

            // 🔹 Show Ad Every 6th Position (For "Vocab" Section)
            if (position > 0 && (position + 1) % 6 == 0) {
                itemView = mLayoutInflater.inflate(R.layout.native_ad_layout, container, false);
                if (itemView != null) {
                    loadNativeAd(itemView);
                } else {
                    Log.e(TAG, "instantiateItem: Failed to inflate native_ad_layout at position " + position);
                }
            }
            else {
                itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);
            CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
            CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
            cardViewVocab.setVisibility(View.GONE);
            cardViewIdiom.setVisibility(View.VISIBLE);
            Log.d(TAG, "instantiateItem: CardView visibility set - Vocab: GONE, Idiom: VISIBLE");

            TextView hackTxt = itemView.findViewById(R.id.tv_idiom);
            ImageView hackTxt2 = itemView.findViewById(R.id.iv_idiom);
            LinearLayout copy = itemView.findViewById(R.id.copyLLIdiom);
            final LinearLayout starLLIdiom = itemView.findViewById(R.id.starLLIdiom);
            LinearLayout share = itemView.findViewById(R.id.shareLLIdiom);
            LinearLayout otherapp = itemView.findViewById(R.id.ourappLLIdiom);
            final ImageView star_idiom = itemView.findViewById(R.id.star_idiom);

            QuoteModel currentQuote = quoteModels.get(position);
            if (currentQuote == null) {
                Log.e(TAG, "instantiateItem: currentQuote is null at position " + position);
                return itemView;
            }

            hackTxt.setText(currentQuote.getQuote());
            Log.d(TAG, "instantiateItem: Quote set at position " + position + " -> " + currentQuote.getQuote());

            String url = currentQuote.getValue();
            if (url != null && !url.isEmpty()) {
                hackTxt2.setVisibility(View.VISIBLE);
                Picasso.get().load(url).into(hackTxt2);
                Log.d(TAG, "instantiateItem: Image loaded for position " + position + " -> " + url);
            } else {
                hackTxt2.setVisibility(View.GONE);
                Log.d(TAG, "instantiateItem: No image for position " + position);
            }

            int[] colors = {Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47), Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120), Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)};
            int randomIndex = (int) (Math.random() * colors.length);
            int randomColor = colors[randomIndex];

            hackTxt.setTextColor(randomColor);

            if (modelDatabases != null) {
             /*   for (ModelDatabase modelDatabase : modelDatabases) {
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
                }*/

                ///only check the quote because not using images and its identifier is "value"

                for (int i = 0; i < modelDatabases.size(); i++) {
                    Log.d("BOOKMARK_CHECK", "Checking Quote: " + quoteModels.get(position).getQuote() + " at position: " + position);
                    Log.d("BOOKMARK_CHECK", "ModelDatabase Note: " + modelDatabases.get(i).getNote() + " at position: " + i);

                    if (quoteModels.get(position).getQuote().equals(modelDatabases.get(i).getNote())) {
                        Log.d("BOOKMARK_CHECK", "MATCH FOUND at position: " + position);
                        quoteModels.get(position).setBookmark("1");
                        quoteModels.get(position).setBookmared(true);
                        break;
                    } else {
                        Log.d("BOOKMARK_CHECK", "No match at position: " + position);
                        quoteModels.get(position).setBookmark("0");
                        quoteModels.get(position).setBookmared(false);
                    }
                }


            } else {
                Log.e(TAG, "instantiateItem: modelDatabases is null at position " + position);
            }



            if (currentQuote.isBookmared()) {
                star_idiom.setImageResource(R.drawable.starfilled);
                Log.d(TAG, "instantiateItem: Star icon set to filled at position " + position);
            } else {
                star_idiom.setImageResource(R.drawable.star);
                Log.d(TAG, "instantiateItem: Star icon set to unfilled at position " + position);
            }

            copy.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Copy clicked at position " + position);
                clickInterface.onCopyClick(currentQuote);
            });

            starLLIdiom.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Bookmark clicked at position " + position);
                clickInterface.onBoookmarkClick(currentQuote, star_idiom);
            });

            share.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Share clicked at position " + position);
                clickInterface.onShareClick(currentQuote);
            });

            otherapp.setOnClickListener(view -> {
                Log.d(TAG, "onClick: Other apps clicked at position " + position);
                clickInterface.onMoreAppsClick();
            });

            }
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

    private void loadNativeAd(View adView) {
        // ✅ Fetch Ad Unit ID from strings.xml
        String adUnitId = mContext.getResources().getString(R.string.native_ad);

        Log.d("AdMob", "Loading Native Ad with Ad Unit ID: " + adUnitId);  // Debugging Log

        AdLoader adLoader = new AdLoader.Builder(mContext, adUnitId)
                .forNativeAd(nativeAd -> {
                    Log.d("AdMob", "✅ Native Ad Loaded Successfully!");
                    NativeAdView adLayout = adView.findViewById(R.id.native_ad_view);
                    populateNativeAdView(nativeAd, adLayout);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("AdMob", "❌ Native Ad Failed to Load: " + adError.getMessage());
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        if (nativeAd.getIcon() != null) {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
        } else {
            adView.getIconView().setVisibility(View.GONE);
        }

        adView.setNativeAd(nativeAd);
    }
    public interface ClickInterface {
        void onBoookmarkClick(QuoteModel CategoryModel, ImageView star);

        void onCopyClick(QuoteModel CategoryModel);

        void onShareClick(QuoteModel CategoryModel);

        void onMoreAppsClick();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView((View) object);  // ✅ No more casting issues!
        } else {
            Log.e(TAG, "destroyItem: Unable to remove view at position " + position + " due to incorrect type.");
        }
    }

}

package com.royal.edunotes._adapters;

import android.app.Activity;
import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.royal.edunotes.BuildConfig;
import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._models.QuoteModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class VerticlePagerAdapter extends PagerAdapter {

    private static final String TAG = "VerticlePager_ADS"; // Fixed: Max 23 characters
    private static final int AD_INTERVAL = 5; // Show interstitial ad every 5 scrolls
    private static final int RETRY_DELAY_MS = 30000; // 30 seconds retry delay
    private static final int AD_SHOW_DELAY_MS = 300; // Small delay before showing ad

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<QuoteModel> quoteModels;
    ClickInterface clickInterface;
    ArrayList<ModelDatabase> modelDatabases;

    // Interstitial ad management
    private InterstitialAd mInterstitialAd;
    private boolean isLoadingAd = false;
    private boolean adsInitialized = false;
    private Handler mainHandler;
    private int lastAdPosition = -1; // Track last position where ad was shown
    private ProgressManager progressManager;

    public VerticlePagerAdapter(Context context, ArrayList<QuoteModel> quoteModels,
                                ClickInterface clickInterface, ArrayList<ModelDatabase> modelDatabases) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.quoteModels = new ArrayList<>(quoteModels);
        this.clickInterface = clickInterface;
        this.modelDatabases = modelDatabases;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.progressManager = new ProgressManager(context);

        Log.d(TAG, "🔧 Adapter created - Quotes: " + this.quoteModels.size());

        // Initialize AdMob
        initializeAds();
    }

    private void initializeAds() {
        try {
            MobileAds.initialize(mContext, initializationStatus -> {
                adsInitialized = true;
                Log.d(TAG, "🔧 AdMob initialized");
                // Preload first interstitial ad
                loadInterstitialAd();
            });
        } catch (Exception e) {
            Log.e(TAG, "🔧 Error initializing AdMob", e);
            adsInitialized = false;
        }
    }

    private void loadInterstitialAd() {
        if (isLoadingAd || !adsInitialized || mContext == null) {
            return;
        }

        isLoadingAd = true;
        Log.d(TAG, "🔧 Loading interstitial ad");

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(mContext, BuildConfig.ADMOB_INTERSTITIAL_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd interstitialAd) {
                        Log.d(TAG, "🔧 Interstitial ad loaded");
                        mInterstitialAd = interstitialAd;
                        isLoadingAd = false;

                        // Set up ad callbacks
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "🔧 Ad dismissed");
                                mInterstitialAd = null;
                                // Preload next ad after small delay
                                mainHandler.postDelayed(() -> loadInterstitialAd(), 2000);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                Log.e(TAG, "🔧 Ad failed to show: " + adError.getMessage());
                                mInterstitialAd = null;
                                // Try to load another ad
                                mainHandler.postDelayed(() -> loadInterstitialAd(), 5000);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.d(TAG, "🔧 Ad showed");
                            }

                            @Override
                            public void onAdClicked() {
                                Log.d(TAG, "🔧 Ad clicked");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        Log.e(TAG, "🔧 Failed to load ad: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                        isLoadingAd = false;

                        // Retry loading after delay
                        mainHandler.postDelayed(() -> loadInterstitialAd(), RETRY_DELAY_MS);
                    }
                });
    }

    @Override
    public int getCount() {
        return quoteModels.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        Log.d(TAG, "🔧 instantiateItem - position: " + position);

        // Check if we should show interstitial ad
        checkAndShowInterstitialAd(position);

        // Safety check
        if (position < 0 || position >= quoteModels.size()) {
            Log.e(TAG, "🔧 Position out of bounds: " + position + "/" + quoteModels.size());
            View dummyView = new View(mContext);
            container.addView(dummyView);
            return dummyView;
        }

        QuoteModel currentQuote = quoteModels.get(position);
        if (currentQuote == null) {
            Log.e(TAG, "🔧 Quote is null at position: " + position);
            View dummyView = new View(mContext);
            container.addView(dummyView);
            return dummyView;
        }

        View itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

        // Track word read for progress
        if (progressManager != null) {
            progressManager.onWordRead();
        }

        if (Utility.ScreenCheck.equals("Vocab")) {
            setupVocabView(itemView, currentQuote, position);
        } else if (Utility.ScreenCheck.equals("Idiom")) {
            setupIdiomView(itemView, currentQuote, position);
        }

        container.addView(itemView);
        return itemView;
    }

    private void checkAndShowInterstitialAd(int position) {
        // Show interstitial ad every 5th scroll, but not too frequently
        boolean shouldShowAd = adsInitialized &&
                (position + 1) % AD_INTERVAL == 0 &&
                position > 0 &&
                position != lastAdPosition &&
                Math.abs(position - lastAdPosition) >= AD_INTERVAL;

        if (shouldShowAd) {
            lastAdPosition = position;
            // Add small delay to ensure smooth transition
            mainHandler.postDelayed(this::showInterstitialAd, AD_SHOW_DELAY_MS);
        }
    }

    private void showInterstitialAd() {
        if (mInterstitialAd != null && mContext instanceof Activity) {
            try {
                Log.d(TAG, "🔧 Showing ad");
                mInterstitialAd.show((Activity) mContext);
            } catch (Exception e) {
                Log.e(TAG, "🔧 Error showing ad", e);
                mInterstitialAd = null;
                loadInterstitialAd();
            }
        } else {
            Log.d(TAG, "🔧 Ad not ready, loading");
            if (!isLoadingAd) {
                loadInterstitialAd();
            }
        }
    }

    private void setupVocabView(View itemView, QuoteModel currentQuote, int position) {
        CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
        CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
        TextView hackTxt = itemView.findViewById(R.id.tv_vocab);
        ImageView hackTxt2 = itemView.findViewById(R.id.iv_vocab);

        LinearLayout copy = itemView.findViewById(R.id.copyLLVocab);
        final LinearLayout starLL = itemView.findViewById(R.id.starLLVocab);
        LinearLayout share = itemView.findViewById(R.id.shareLLVocab);
        LinearLayout ttsLL = itemView.findViewById(R.id.ttsLLVocab);
        LinearLayout explainLL = itemView.findViewById(R.id.explainLLVocab);

        cardViewVocab.setVisibility(View.VISIBLE);
        cardViewIdiom.setVisibility(View.GONE);

        final ImageView star = itemView.findViewById(R.id.star);

        hackTxt.setText(currentQuote.getQuote());
        String url = currentQuote.getValue();

        if (url != null && !url.isEmpty()) {
            hackTxt2.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(url).into(hackTxt2);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                hackTxt2.setVisibility(View.GONE);
            }
        } else {
            hackTxt2.setVisibility(View.GONE);
        }

        // Apply random color
        int[] colors = {
                Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47),
                Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120),
                Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)
        };
        int randomColor = colors[(int) (Math.random() * colors.length)];
        hackTxt.setTextColor(randomColor);

        updateBookmarkStatus(currentQuote, position);

        if (currentQuote.isBookmared()) {
            star.setImageResource(R.drawable.starfilled);
        } else {
            star.setImageResource(R.drawable.star);
        }

        // Set click listeners
        copy.setOnClickListener(view -> clickInterface.onCopyClick(currentQuote));

        starLL.setOnClickListener(view -> {
            clickInterface.onBoookmarkClick(currentQuote, star);
            // Occasionally show ad after bookmark action
            if (position > 0 && (position % 7 == 0)) { // Every 7th bookmark
                mainHandler.postDelayed(this::showInterstitialAd, 1000);
            }
        });

        share.setOnClickListener(view -> {
            clickInterface.onShareClick(currentQuote);
            // Occasionally show ad after share action
            if (position > 0 && (position % 8 == 0)) { // Every 8th share
                mainHandler.postDelayed(this::showInterstitialAd, 1000);
            }
        });

        // Long press share to share as image
        share.setOnLongClickListener(view -> {
            clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_vocab));
            return true;
        });

        ttsLL.setOnClickListener(view -> clickInterface.onTTSClick(currentQuote));
        explainLL.setOnClickListener(view -> clickInterface.onExplainClick(currentQuote));
    }

    private void setupIdiomView(View itemView, QuoteModel currentQuote, int position) {
        CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
        CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
        cardViewVocab.setVisibility(View.GONE);
        cardViewIdiom.setVisibility(View.VISIBLE);

        TextView hackTxt = itemView.findViewById(R.id.tv_idiom);
        ImageView hackTxt2 = itemView.findViewById(R.id.iv_idiom);
        LinearLayout copy = itemView.findViewById(R.id.copyLLIdiom);
        final LinearLayout starLLIdiom = itemView.findViewById(R.id.starLLIdiom);
        LinearLayout share = itemView.findViewById(R.id.shareLLIdiom);
        LinearLayout ttsLL = itemView.findViewById(R.id.ttsLLIdiom);
        LinearLayout explainLL = itemView.findViewById(R.id.explainLLIdiom);
        final ImageView star_idiom = itemView.findViewById(R.id.star_idiom);

        hackTxt.setText(currentQuote.getQuote());

        String url = currentQuote.getValue();
        if (url != null && !url.isEmpty()) {
            hackTxt2.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(url).into(hackTxt2);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image", e);
                hackTxt2.setVisibility(View.GONE);
            }
        } else {
            hackTxt2.setVisibility(View.GONE);
        }

        // Apply random color
        int[] colors = {
                Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47),
                Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120),
                Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)
        };
        int randomColor = colors[(int) (Math.random() * colors.length)];
        hackTxt.setTextColor(randomColor);

        updateBookmarkStatus(currentQuote, position);

        if (currentQuote.isBookmared()) {
            star_idiom.setImageResource(R.drawable.starfilled);
        } else {
            star_idiom.setImageResource(R.drawable.star);
        }

        // Set click listeners
        copy.setOnClickListener(view -> clickInterface.onCopyClick(currentQuote));

        starLLIdiom.setOnClickListener(view -> {
            clickInterface.onBoookmarkClick(currentQuote, star_idiom);
            // Occasionally show ad after bookmark action
            if (position > 0 && (position % 7 == 0)) {
                mainHandler.postDelayed(this::showInterstitialAd, 1000);
            }
        });

        share.setOnClickListener(view -> {
            clickInterface.onShareClick(currentQuote);
            // Occasionally show ad after share action
            if (position > 0 && (position % 8 == 0)) {
                mainHandler.postDelayed(this::showInterstitialAd, 1000);
            }
        });

        // Long press share to share as image
        share.setOnLongClickListener(view -> {
            clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_idiom));
            return true;
        });

        ttsLL.setOnClickListener(view -> clickInterface.onTTSClick(currentQuote));
        explainLL.setOnClickListener(view -> clickInterface.onExplainClick(currentQuote));
    }

    private void updateBookmarkStatus(QuoteModel currentQuote, int position) {
        if (modelDatabases != null) {
            currentQuote.setBookmared(false);

            for (ModelDatabase modelDatabase : modelDatabases) {
                if (modelDatabase != null && currentQuote.getQuote() != null &&
                        currentQuote.getQuote().equals(modelDatabase.getNote())) {
                    currentQuote.setBookmark("1");
                    currentQuote.setBookmared(true);
                    break;
                }
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            container.removeView((View) object);
        }
    }

    public void cleanup() {
        Log.d(TAG, "🔧 Cleanup started");
        if (mInterstitialAd != null) {
            mInterstitialAd = null;
        }
        isLoadingAd = false;
        lastAdPosition = -1;

        // Cancel any pending ad loads
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    public void updateData(ArrayList<QuoteModel> newQuoteModels, ArrayList<ModelDatabase> newModelDatabases) {
        this.quoteModels.clear();
        this.quoteModels.addAll(newQuoteModels);
        this.modelDatabases = newModelDatabases;
        lastAdPosition = -1; // Reset ad position tracking
        notifyDataSetChanged();
        Log.d(TAG, "🔧 Data updated - size: " + newQuoteModels.size());
    }

    // Public methods for external control
    public void showAdNow() {
        showInterstitialAd();
    }

    public void preloadAd() {
        if (!isLoadingAd && mInterstitialAd == null) {
            loadInterstitialAd();
        }
    }

    public boolean isAdReady() {
        return mInterstitialAd != null;
    }

    public void setAdInterval(int interval) {
        // Allow dynamic ad interval adjustment if needed
        // Note: This would require making AD_INTERVAL non-final
    }

    public interface ClickInterface {
        void onBoookmarkClick(QuoteModel CategoryModel, ImageView star);
        void onCopyClick(QuoteModel CategoryModel);
        void onShareClick(QuoteModel CategoryModel);
        void onShareAsImageClick(View cardView);
        void onTTSClick(QuoteModel quoteModel);
        void onExplainClick(QuoteModel quoteModel);
        void onMoreAppsClick();
    }
}
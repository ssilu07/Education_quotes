package com.royal.edunotes._adapters;

import android.app.Activity;
import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.royal.edunotes.BuildConfig;
import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.Utility;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._models.QuoteModel;
import com.squareup.picasso.Picasso;

public class VerticlePagerAdapter extends PagerAdapter {

    private static final String TAG = "VerticlePager_ADS";
    // Every 6th page (index 5, 11, 17...) is a native ad slot
    private static final int AD_INTERVAL = 5;
    private static final int AD_SLOT_SIZE = AD_INTERVAL + 1; // = 6
    private static final int AD_PRELOAD_COUNT = 5;

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<QuoteModel> quoteModels;
    ClickInterface clickInterface;
    ArrayList<ModelDatabase> modelDatabases;
    private java.util.HashSet<String> bookmarkedNotes = new java.util.HashSet<>();

    private final ArrayList<NativeAd> nativeAdList = new ArrayList<>();
    // slotIndex -> active NativeAdView (so we can populate it when ad loads late)
    private final SparseArray<NativeAdView> activeAdViews = new SparseArray<>();
    private boolean nativeAdsLoading = false;
    private boolean adsInitialized = false;
    private Handler mainHandler;
    private ProgressManager progressManager;
    private SettingsManager settingsManager;
    private String categoryName = "";
    private android.util.SparseArray<ArrayList<String>> quizWrongOptionsCache = new android.util.SparseArray<>();

    public VerticlePagerAdapter(Context context, ArrayList<QuoteModel> quoteModels,
                                ClickInterface clickInterface, ArrayList<ModelDatabase> modelDatabases) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.quoteModels = new ArrayList<>(quoteModels);
        this.clickInterface = clickInterface;
        this.modelDatabases = modelDatabases;
        buildBookmarkSet(modelDatabases);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.progressManager = new ProgressManager(context);
        this.settingsManager = new SettingsManager(context);

        Log.d(TAG, "Adapter created - Quotes: " + this.quoteModels.size());

        if (BuildConfig.ENABLE_ADS) {
            MobileAds.initialize(mContext, status -> {
                adsInitialized = true;
                preloadNativeAds();
            });
        }
    }

    public void setCategoryName(String name) {
        this.categoryName = name != null ? name : "";
    }

    private boolean isQuizCategory() {
        return categoryName.toLowerCase().contains("common error");
    }

    // ── Native ad helpers ────────────────────────────────────────────────────

    /** Position is an ad slot if it's the 6th page in every group of 6. */
    private boolean isAdPosition(int position) {
        if (!BuildConfig.ENABLE_ADS) return false;
        return position % AD_SLOT_SIZE == AD_INTERVAL;
    }

    /** Maps a ViewPager position to the real data index (skipping ad slots). */
    private int getDataPosition(int position) {
        return position - position / AD_SLOT_SIZE;
    }

    /** Maps a ViewPager ad-slot position to the sequential ad slot index (0,1,2…). */
    private int getAdSlotIndex(int position) {
        return position / AD_SLOT_SIZE;
    }

    private void preloadNativeAds() {
        if (nativeAdsLoading || !adsInitialized || mContext == null) return;
        nativeAdsLoading = true;

        AdLoader adLoader = new AdLoader.Builder(mContext, BuildConfig.ADMOB_NATIVE_ID)
                .forNativeAd(nativeAd -> {
                    nativeAdList.add(nativeAd);
                    int slotIndex = nativeAdList.size() - 1;
                    // If the view for this slot is already on screen, populate it now
                    mainHandler.post(() -> {
                        NativeAdView view = activeAdViews.get(slotIndex);
                        if (view != null) populateNativeAdView(view, nativeAd);
                    });
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        nativeAdsLoading = false;
                        Log.e(TAG, "Native ad failed: " + error.getMessage());
                    }
                })
                .build();

        adLoader.loadAds(new AdRequest.Builder().build(), AD_PRELOAD_COUNT);
    }

    private void populateNativeAdView(NativeAdView adView, NativeAd nativeAd) {
        TextView headline = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        TextView advertiser = adView.findViewById(R.id.ad_advertiser);
        ImageView icon = adView.findViewById(R.id.ad_icon);
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        Button cta = adView.findViewById(R.id.ad_call_to_action);

        adView.setHeadlineView(headline);
        adView.setBodyView(body);
        adView.setAdvertiserView(advertiser);
        adView.setIconView(icon);
        adView.setMediaView(mediaView);
        adView.setCallToActionView(cta);

        headline.setText(nativeAd.getHeadline());

        if (nativeAd.getBody() != null) {
            body.setText(nativeAd.getBody());
            body.setVisibility(View.VISIBLE);
        } else {
            body.setVisibility(View.GONE);
        }

        if (nativeAd.getAdvertiser() != null) {
            advertiser.setText(nativeAd.getAdvertiser());
            advertiser.setVisibility(View.VISIBLE);
        } else {
            advertiser.setVisibility(View.GONE);
        }

        if (nativeAd.getIcon() != null) {
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }

        if (nativeAd.getCallToAction() != null) {
            cta.setText(nativeAd.getCallToAction());
            cta.setVisibility(View.VISIBLE);
        } else {
            cta.setVisibility(View.GONE);
        }

        if (nativeAd.getMediaContent() != null) {
            mediaView.setMediaContent(nativeAd.getMediaContent());
            mediaView.setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    // ── PagerAdapter overrides ───────────────────────────────────────────────

    @Override
    public int getCount() {
        if (!BuildConfig.ENABLE_ADS) return quoteModels.size();
        // Insert one ad slot after every AD_INTERVAL real cards
        return quoteModels.size() + quoteModels.size() / AD_INTERVAL;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        // Forces ViewPager to rebuild every visible page after updateData() —
        // otherwise notifyDataSetChanged() leaves stale pages on screen (default PagerAdapter behavior).
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (isAdPosition(position)) {
            return instantiateAdItem(container, position);
        }

        int dataPos = getDataPosition(position);

        if (dataPos < 0 || dataPos >= quoteModels.size()) {
            View dummy = new View(mContext);
            container.addView(dummy);
            return dummy;
        }

        QuoteModel currentQuote = quoteModels.get(dataPos);
        if (currentQuote == null) {
            View dummy = new View(mContext);
            container.addView(dummy);
            return dummy;
        }

        View itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

        if (isQuizCategory()) {
            setupQuizView(itemView, currentQuote, dataPos);
        } else if (Utility.ScreenCheck.equals("Vocab") || Utility.ScreenCheck.equals("Grammar")) {
            setupVocabView(itemView, currentQuote, dataPos);
        } else if (Utility.ScreenCheck.equals("Idiom")) {
            setupIdiomView(itemView, currentQuote, dataPos);
        }

        container.addView(itemView);
        return itemView;
    }

    private View instantiateAdItem(ViewGroup container, int position) {
        int slotIndex = getAdSlotIndex(position);
        View adCardView = mLayoutInflater.inflate(R.layout.native_ad_card, container, false);
        NativeAdView nativeAdView = adCardView.findViewById(R.id.native_ad_view);

        activeAdViews.put(slotIndex, nativeAdView);
        adCardView.setTag(slotIndex); // store slotIndex for cleanup in destroyItem

        if (slotIndex < nativeAdList.size()) {
            populateNativeAdView(nativeAdView, nativeAdList.get(slotIndex));
        }
        // else: placeholder shown; will be populated once preloadNativeAds() callback fires

        container.addView(adCardView);
        return adCardView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            View view = (View) object;
            Object tag = view.getTag();
            if (tag instanceof Integer) {
                activeAdViews.remove((Integer) tag);
            }
            container.removeView(view);
        }
    }

    // ── Card setup methods (unchanged) ──────────────────────────────────────

    private void setupVocabView(View itemView, QuoteModel currentQuote, int position) {
        CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
        CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
        TextView hackTxt = itemView.findViewById(R.id.tv_vocab);
        ImageView hackTxt2 = itemView.findViewById(R.id.iv_vocab);

        LinearLayout copy = itemView.findViewById(R.id.copyLLVocab);
        final LinearLayout starLL = itemView.findViewById(R.id.starLLVocab);
        LinearLayout share = itemView.findViewById(R.id.shareLLVocab);
        LinearLayout ttsLL = itemView.findViewById(R.id.ttsLLVocab);
        LinearLayout learnedLL = itemView.findViewById(R.id.learnedLLVocab);
        final TextView learnedLabel = itemView.findViewById(R.id.tv_learned_vocab);

        cardViewVocab.setVisibility(View.VISIBLE);
        cardViewIdiom.setVisibility(View.GONE);

        final ImageView star = itemView.findViewById(R.id.star);

        hackTxt.setText(currentQuote.getQuote());
        hackTxt.setTextSize(settingsManager.getFontSize());
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

        int[] colors = {
                Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47),
                Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120),
                Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)
        };
        hackTxt.setTextColor(colors[(int) (Math.random() * colors.length)]);

        updateBookmarkStatus(currentQuote, position);

        if (currentQuote.isBookmared()) {
            star.setImageResource(R.drawable.starfilled);
        } else {
            star.setImageResource(R.drawable.star);
        }

        updateLearnedStatus(currentQuote);
        learnedLabel.setText(currentQuote.isLearned()
                ? mContext.getString(R.string.learned_on_label)
                : mContext.getString(R.string.learned_off_label));

        copy.setOnClickListener(view -> clickInterface.onCopyClick(currentQuote));
        starLL.setOnClickListener(view -> clickInterface.onBoookmarkClick(currentQuote, star));
        share.setOnClickListener(view -> clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_vocab), currentQuote.getQuote()));
        share.setOnLongClickListener(view -> {
            clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_vocab), currentQuote.getQuote());
            return true;
        });
        ttsLL.setOnClickListener(view -> clickInterface.onTTSClick(currentQuote));
        learnedLL.setOnClickListener(view -> clickInterface.onLearnedClick(currentQuote, learnedLabel));
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
        final ImageView star_idiom = itemView.findViewById(R.id.star_idiom);

        hackTxt.setText(currentQuote.getQuote());
        hackTxt.setTextSize(settingsManager.getFontSize());

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

        int[] colors = {
                Color.rgb(36, 7, 80), Color.rgb(255, 0, 128), Color.rgb(50, 1, 47),
                Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120),
                Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)
        };
        hackTxt.setTextColor(colors[(int) (Math.random() * colors.length)]);

        updateBookmarkStatus(currentQuote, position);

        if (currentQuote.isBookmared()) {
            star_idiom.setImageResource(R.drawable.starfilled);
        } else {
            star_idiom.setImageResource(R.drawable.star);
        }

        copy.setOnClickListener(view -> clickInterface.onCopyClick(currentQuote));
        starLLIdiom.setOnClickListener(view -> clickInterface.onBoookmarkClick(currentQuote, star_idiom));
        share.setOnClickListener(view -> clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_idiom), currentQuote.getQuote()));
        share.setOnLongClickListener(view -> {
            clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_idiom), currentQuote.getQuote());
            return true;
        });
        ttsLL.setOnClickListener(view -> clickInterface.onTTSClick(currentQuote));
    }

    private void setupQuizView(View itemView, QuoteModel currentQuote, int position) {
        CardView cardViewVocab = itemView.findViewById(R.id.card_view_vocab);
        CardView cardViewIdiom = itemView.findViewById(R.id.card_view_idiom);
        CardView cardViewQuiz = itemView.findViewById(R.id.card_view_quiz);

        cardViewVocab.setVisibility(View.GONE);
        cardViewIdiom.setVisibility(View.GONE);
        cardViewQuiz.setVisibility(View.VISIBLE);

        TextView tvQuizNum = itemView.findViewById(R.id.tv_quiz_num);
        TextView tvQuestion = itemView.findViewById(R.id.tv_quiz_question);
        TextView tvHint = itemView.findViewById(R.id.tv_quiz_hint);

        CardView cvA = itemView.findViewById(R.id.cv_option_a);
        CardView cvB = itemView.findViewById(R.id.cv_option_b);
        CardView cvC = itemView.findViewById(R.id.cv_option_c);
        CardView cvD = itemView.findViewById(R.id.cv_option_d);

        TextView tvA = itemView.findViewById(R.id.tv_option_a);
        TextView tvB = itemView.findViewById(R.id.tv_option_b);
        TextView tvC = itemView.findViewById(R.id.tv_option_c);
        TextView tvD = itemView.findViewById(R.id.tv_option_d);

        LinearLayout llExplanation = itemView.findViewById(R.id.ll_explanation);
        TextView tvExplanation = itemView.findViewById(R.id.tv_explanation);

        String quoteText = currentQuote.getQuote();
        String wrongSentence = "";
        String rightSentence = "";
        String rule = "";

        String[] lines = quoteText.split("\r\n|\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase().startsWith("wrong:")) {
                wrongSentence = trimmed.substring(6).trim();
            } else if (trimmed.toLowerCase().startsWith("right:")) {
                rightSentence = trimmed.substring(6).trim();
            } else if (trimmed.toLowerCase().startsWith("rule:")) {
                rule = trimmed.substring(5).trim();
            }
        }

        tvQuizNum.setText("Q." + (position + 1));
        tvQuestion.setText("Choose the CORRECT sentence:");
        tvHint.setText(wrongSentence.isEmpty() ? quoteText : wrongSentence);

        ArrayList<String> options = new ArrayList<>();
        options.add(rightSentence.isEmpty() ? quoteText : rightSentence);

        ArrayList<String> wrongOptions = quizWrongOptionsCache.get(position);
        if (wrongOptions == null) {
            wrongOptions = new ArrayList<>();
            for (int i = 0; i < quoteModels.size(); i++) {
                if (i == position) continue;
                String otherText = quoteModels.get(i).getQuote();
                String[] otherLines = otherText.split("\r\n|\n");
                for (String ol : otherLines) {
                    String ot = ol.trim();
                    if (ot.toLowerCase().startsWith("wrong:")) {
                        wrongOptions.add(ot.substring(6).trim());
                        break;
                    }
                }
            }
            Collections.shuffle(wrongOptions, new Random(position));
            quizWrongOptionsCache.put(position, wrongOptions);
        }
        for (int i = 0; i < Math.min(3, wrongOptions.size()); i++) {
            options.add(wrongOptions.get(i));
        }
        while (options.size() < 4) options.add("Option " + (options.size() + 1));

        String correctAnswer = options.get(0);
        Collections.shuffle(options, new Random(position * 7));

        tvA.setText("A) " + options.get(0));
        tvB.setText("B) " + options.get(1));
        tvC.setText("C) " + options.get(2));
        tvD.setText("D) " + options.get(3));

        cvA.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        cvB.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        cvC.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        cvD.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        tvA.setTextColor(Color.parseColor("#333333"));
        tvB.setTextColor(Color.parseColor("#333333"));
        tvC.setTextColor(Color.parseColor("#333333"));
        tvD.setTextColor(Color.parseColor("#333333"));
        llExplanation.setVisibility(View.GONE);

        String finalRule = rule;
        String finalCorrectAnswer = correctAnswer;
        String finalRightSentence = rightSentence;

        CardView[] cards = {cvA, cvB, cvC, cvD};
        TextView[] texts = {tvA, tvB, tvC, tvD};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            cards[i].setOnClickListener(view -> {
                for (CardView c : cards) c.setClickable(false);

                String selected = options.get(idx).trim();
                boolean isCorrect = selected.equals(finalCorrectAnswer);

                if (isCorrect) {
                    cards[idx].setCardBackgroundColor(Color.parseColor("#4CAF50"));
                    texts[idx].setTextColor(Color.WHITE);
                } else {
                    cards[idx].setCardBackgroundColor(Color.parseColor("#F44336"));
                    texts[idx].setTextColor(Color.WHITE);
                    for (int j = 0; j < 4; j++) {
                        if (options.get(j).trim().equals(finalCorrectAnswer)) {
                            cards[j].setCardBackgroundColor(Color.parseColor("#4CAF50"));
                            texts[j].setTextColor(Color.WHITE);
                        }
                    }
                }

                String explanation = !finalRule.isEmpty()
                        ? "Rule: " + finalRule + "\n\nCorrect: " + finalRightSentence
                        : "Correct: " + finalRightSentence;
                String detailed = currentQuote.getValue();
                if (detailed != null && !detailed.trim().isEmpty()) {
                    explanation += "\n\nExplanation:\n" + detailed.trim();
                }
                tvExplanation.setText(explanation);
                llExplanation.setVisibility(View.VISIBLE);
            });
        }
        updateBookmarkStatus(currentQuote, position);
        final ImageView starQuiz = itemView.findViewById(R.id.star_quiz);
        starQuiz.setImageResource(currentQuote.isBookmared() ? R.drawable.starfilled : R.drawable.star);

        itemView.findViewById(R.id.starLLQuiz).setOnClickListener(v -> clickInterface.onBoookmarkClick(currentQuote, starQuiz));
        itemView.findViewById(R.id.shareLLQuiz).setOnClickListener(v -> clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_quiz), currentQuote.getQuote()));
        itemView.findViewById(R.id.shareLLQuiz).setOnLongClickListener(v -> {
            clickInterface.onShareAsImageClick(itemView.findViewById(R.id.card_view_quiz), currentQuote.getQuote());
            return true;
        });
        itemView.findViewById(R.id.copyLLQuiz).setOnClickListener(v -> clickInterface.onCopyClick(currentQuote));
        itemView.findViewById(R.id.ttsLLQuiz).setOnClickListener(v -> clickInterface.onTTSClick(currentQuote));
    }

    // ── Bookmark helpers ─────────────────────────────────────────────────────

    private void buildBookmarkSet(ArrayList<ModelDatabase> databases) {
        bookmarkedNotes.clear();
        if (databases != null) {
            for (ModelDatabase db : databases) {
                if (db != null && db.getNote() != null) {
                    bookmarkedNotes.add(db.getNote());
                }
            }
        }
    }

    private void updateBookmarkStatus(QuoteModel currentQuote, int position) {
        if (currentQuote.getQuote() != null && bookmarkedNotes.contains(currentQuote.getQuote())) {
            currentQuote.setBookmark("1");
            currentQuote.setBookmared(true);
        } else {
            currentQuote.setBookmared(false);
        }
    }

    private void updateLearnedStatus(QuoteModel currentQuote) {
        currentQuote.setLearned(settingsManager.isWordLearned(currentQuote.getQuote()));
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /** Returns the quote at this pager position, or null if it's an ad slot / out of range. */
    public QuoteModel getItemAt(int pagerPosition) {
        if (isAdPosition(pagerPosition)) return null;
        int dataPos = getDataPosition(pagerPosition);
        if (dataPos < 0 || dataPos >= quoteModels.size()) return null;
        return quoteModels.get(dataPos);
    }

    /** Inverse of getDataPosition(): maps a real data index back to its pager position. */
    public int getPagerPositionForDataIndex(int dataIndex) {
        if (!BuildConfig.ENABLE_ADS) return dataIndex;
        return dataIndex + dataIndex / AD_INTERVAL;
    }

    public void cleanup() {
        if (mainHandler != null) mainHandler.removeCallbacksAndMessages(null);
        for (NativeAd ad : nativeAdList) ad.destroy();
        nativeAdList.clear();
        activeAdViews.clear();
    }

    public void updateData(ArrayList<QuoteModel> newQuoteModels, ArrayList<ModelDatabase> newModelDatabases) {
        this.quoteModels.clear();
        this.quoteModels.addAll(newQuoteModels);
        this.modelDatabases = newModelDatabases;
        buildBookmarkSet(newModelDatabases);
        quizWrongOptionsCache.clear();
        notifyDataSetChanged();
        Log.d(TAG, "Data updated - size: " + newQuoteModels.size());
    }

    public interface ClickInterface {
        void onBoookmarkClick(QuoteModel CategoryModel, ImageView star);
        void onCopyClick(QuoteModel CategoryModel);
        void onShareAsImageClick(View cardView, String text);
        void onTTSClick(QuoteModel quoteModel);
        void onLearnedClick(QuoteModel quoteModel, TextView learnedLabel);
        void onQuizClick();
        void onMoreAppsClick();
    }
}

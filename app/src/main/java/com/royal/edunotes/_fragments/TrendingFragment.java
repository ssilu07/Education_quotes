package com.royal.edunotes._fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.InterstitialAd;
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
import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.CLIPBOARD_SERVICE;

public class TrendingFragment extends Fragment implements VerticlePagerAdapter.ClickInterface {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    ArrayList<QuoteModel> mainQuoteModels;
    private OnFragmentInteractionListener mListener;
    DatabaseHelper db;
    VerticlePagerAdapter verticlePagerAdapter;
    TTSHelper ttsHelper;
    AIExplainHelper aiExplainHelper;
    ProgressManager progressManager;
    SettingsManager settingsManager;
    private ProgressBar progressBar;
    private TextView noDataTxt;
    private VerticalViewPager verticalViewPager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TrendingFragment() {
    }

    public static TrendingFragment newInstance(String param1, String param2) {
        TrendingFragment fragment = new TrendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trending, container, false);

        verticalViewPager = (VerticalViewPager) v.findViewById(R.id.vPager);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        noDataTxt = (TextView) v.findViewById(R.id.noDataTxt);

        ttsHelper = new TTSHelper(getActivity());
        aiExplainHelper = new AIExplainHelper();
        progressManager = new ProgressManager(getActivity());
        settingsManager = new SettingsManager(getActivity());

        loadTrendingData();

        return v;
    }

    private void loadTrendingData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (noDataTxt != null) noDataTxt.setVisibility(View.GONE);
        if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);

        executor.execute(() -> {
            ArrayList<QuoteModel> loadedQuotes = new ArrayList<>();
            List<com.royal.edunotes.CategoryDataProvider.Category> allCats = com.royal.edunotes.CategoryDataProvider.getAllCategories();
            for (com.royal.edunotes.CategoryDataProvider.Category cat : allCats) {
                for (com.royal.edunotes.CategoryDataProvider.SubCategory sub : cat.subCategories) {
                    // Skip quiz categories (they have questions table, not inspiring_life_quote)
                    if (sub.hasSets() || (sub.screenCheck != null && "Quiz".equalsIgnoreCase(sub.screenCheck))) {
                        continue;
                    }
                    try {
                        if (getActivity() == null) return;
                        MyDatabase myDb = new MyDatabase(getActivity(), sub.dbName, sub.title);
                        ArrayList<QuoteModel> quoteModels = myDb.getPoses();
                        if (quoteModels != null && !quoteModels.isEmpty()) {
                            loadedQuotes.addAll(quoteModels);
                        }
                    } catch (Exception e) {
                        Log.e("TrendingFragment", "Error loading DB: " + sub.dbName, e);
                    }
                }
            }

            Collections.shuffle(loadedQuotes);

            ArrayList<ModelDatabase> modelDatabases = new ArrayList<>();
            try {
                if (getActivity() != null) {
                    DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
                    modelDatabases = (ArrayList<ModelDatabase>) dbHelper.getAllNotes();
                }
            } catch (Exception ignored) {}

            final ArrayList<ModelDatabase> finalModelDb = modelDatabases;
            mainHandler.post(() -> {
                if (!isAdded() || getActivity() == null) return;
                mainQuoteModels = loadedQuotes;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (mainQuoteModels.isEmpty()) {
                    if (noDataTxt != null) noDataTxt.setVisibility(View.VISIBLE);
                    if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);
                } else {
                    if (noDataTxt != null) noDataTxt.setVisibility(View.GONE);
                    if (verticalViewPager != null) {
                        verticalViewPager.setVisibility(View.VISIBLE);
                        verticlePagerAdapter = new VerticlePagerAdapter(getActivity(), mainQuoteModels, TrendingFragment.this, finalModelDb);
                        verticalViewPager.setOffscreenPageLimit(0);
                        verticalViewPager.setAdapter(verticlePagerAdapter);
                    }
                }
            });
        });
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {
        if (quoteModel == null || quoteModel.getQuote() == null) return;

        if (db == null) {
            db = new DatabaseHelper(getActivity());
        }

        boolean isCurrentlyBookmarked = db.isBookmarked(quoteModel.getQuote());

        if (isCurrentlyBookmarked) {
            db.deleteNote(quoteModel);
            star.setImageResource(R.drawable.star);
            quoteModel.setBookmared(false);
            quoteModel.setBookmark("0");
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), false);
            }
        } else {
            quoteModel.setBookmark("1");
            quoteModel.setBookmared(true);
            db.insertNote(quoteModel);
            star.setImageResource(R.drawable.starfilled);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), true);
            }
            if (progressManager != null) progressManager.onWordBookmarked();
        }
    }
   /* private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }*/

    @Override
    public void onCopyClick(QuoteModel quoteModel) {
        Toast.makeText(getActivity(), "Copied" , Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", quoteModel.getQuote());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onShareAsImageClick(View cardView, String text) {
        ShareUtils.shareViewAsImage(getActivity(), cardView, text);
    }

    @Override
    public void onTTSClick(QuoteModel quoteModel) {
        if (ttsHelper != null) ttsHelper.speak(quoteModel.getQuote());
    }

    @Override
    public void onLearnedClick(QuoteModel quoteModel, TextView learnedLabel) {
        if (settingsManager == null) return;
        boolean newState = !quoteModel.isLearned();
        settingsManager.setWordLearned(quoteModel.getQuote(), newState);
        quoteModel.setLearned(newState);
        learnedLabel.setText(newState ? getString(R.string.learned_on_label) : getString(R.string.learned_off_label));
    }

    @Override
    public void onQuizClick() {
        startActivity(new Intent(getActivity(), com.royal.edunotes._activities.DailyQuizActivity.class));
    }

    @Override
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        if (executor != null) executor.shutdown();
        if (verticlePagerAdapter != null) verticlePagerAdapter.cleanup();
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        super.onDestroy();
    }
}

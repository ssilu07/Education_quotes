package com.royal.edunotes._fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.royal.edunotes.AIExplainHelper;
import com.royal.edunotes.BuildConfig;
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

import static android.content.Context.CLIPBOARD_SERVICE;

public class BookmarkFragment extends Fragment implements VerticlePagerAdapter.ClickInterface {
    private static final String TAG = "BookmarkFrag_MINIMAL";

    DatabaseHelper db;
    private OnFragmentInteractionListener mListener;
    VerticlePagerAdapter verticlePagerAdapter;
    MyDatabase myDatabase;
    TTSHelper ttsHelper;
    AIExplainHelper aiExplainHelper;
    ProgressManager progressManager;
    TextView nobookmarkTxt;
    VerticalViewPager verticalViewPager;
    InterstitialAd mInterstitialAd;

    private String currentScreenType = "";

    public BookmarkFragment() {
        Log.d(TAG, "🔧 Constructor called");
    }

    public static BookmarkFragment newInstance(String param1, String param2) {
        BookmarkFragment fragment = new BookmarkFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🔧 onCreate - ScreenCheck: " + Utility.ScreenCheck);
        loadInterstitialAd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🔧 onCreateView - ScreenCheck: " + Utility.ScreenCheck);

        View v = inflater.inflate(R.layout.fragment_bookmark2, container, false);
        nobookmarkTxt = (TextView) v.findViewById(R.id.nobookmarkTxt);
        verticalViewPager = (VerticalViewPager) v.findViewById(R.id.vPager);

        // Set initial state immediately
        nobookmarkTxt.setVisibility(View.VISIBLE);
        verticalViewPager.setVisibility(View.GONE);
        nobookmarkTxt.setText("Loading...");

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "🔧 onViewCreated - ScreenCheck: " + Utility.ScreenCheck);

        ttsHelper = new TTSHelper(getActivity());
        aiExplainHelper = new AIExplainHelper();
        progressManager = new ProgressManager(getActivity());
        currentScreenType = Utility.ScreenCheck;
        loadData();
    }

    public void refresh() {
        Log.d(TAG, "🔧 refresh() called - ScreenCheck: " + Utility.ScreenCheck +
                ", Last: " + currentScreenType);

        if (getActivity() == null) {
            Log.d(TAG, "🔧 Activity is null, skipping refresh");
            return;
        }

        // Always reload if screen changed
        if (!currentScreenType.equals(Utility.ScreenCheck)) {
            Log.d(TAG, "🔧 Screen changed, reloading data");
            currentScreenType = Utility.ScreenCheck;

            // Show loading immediately
            if (nobookmarkTxt != null) {
                nobookmarkTxt.setVisibility(View.VISIBLE);
                nobookmarkTxt.setText("Loading " + Utility.ScreenCheck + "...");
            }
            if (verticalViewPager != null) {
                verticalViewPager.setVisibility(View.GONE);
            }

            loadData();
        } else {
            Log.d(TAG, "🔧 No screen change, skipping refresh");
        }
    }

    private void loadData() {
        Log.d(TAG, "🔧 loadData() starting for: " + Utility.ScreenCheck);

        if (getActivity() == null) return;

        try {
            db = new DatabaseHelper(getActivity());
            myDatabase = new MyDatabase(getActivity(), "bookmark_db");

            ArrayList<QuoteModel> allBookmarks = myDatabase.getBookmarkData();
            ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();

            Log.d(TAG, "🔧 Total bookmarks: " + allBookmarks.size() +
                    ", ModelDB: " + modelDatabases.size());

            // Filter for current screen
            ArrayList<QuoteModel> filtered = new ArrayList<>();
            String keyword = Utility.ScreenCheck.equals("Vocab") ? "vocab" : "idiom";

            for (QuoteModel bookmark : allBookmarks) {
                if (bookmark.getCategoryName() != null) {
                    String cat = bookmark.getCategoryName().toLowerCase().trim();
                    if (cat.contains(keyword)) {
                        filtered.add(bookmark);
                    }
                }
            }

            Log.d(TAG, "🔧 Filtered " + filtered.size() + " bookmarks for " + Utility.ScreenCheck);

            // Update UI
            showData(filtered, modelDatabases);

        } catch (Exception e) {
            Log.e(TAG, "🔧 Error loading data: " + e.getMessage());
            showError();
        }
    }

    private void showData(ArrayList<QuoteModel> bookmarks, ArrayList<ModelDatabase> modelDatabases) {
        Log.d(TAG, "🔧 showData() - " + bookmarks.size() + " bookmarks");

        if (getActivity() == null || nobookmarkTxt == null || verticalViewPager == null) {
            Log.e(TAG, "🔧 Views are null, cannot update UI");
            return;
        }

        if (bookmarks.isEmpty()) {
            Log.d(TAG, "🔧 No bookmarks - showing empty state");
            verticalViewPager.setVisibility(View.GONE);
            nobookmarkTxt.setVisibility(View.VISIBLE);
            nobookmarkTxt.setText("No " + Utility.ScreenCheck + " bookmarks");
        } else {
            Log.d(TAG, "🔧 Showing " + bookmarks.size() + " bookmarks");
            nobookmarkTxt.setVisibility(View.GONE);
            verticalViewPager.setVisibility(View.VISIBLE);

            verticlePagerAdapter = new VerticlePagerAdapter(getActivity(), bookmarks, this, modelDatabases);
            verticalViewPager.setOffscreenPageLimit(0);
            verticalViewPager.setAdapter(verticlePagerAdapter);
        }
    }

    private void showError() {
        Log.d(TAG, "🔧 showError()");
        if (nobookmarkTxt != null && verticalViewPager != null) {
            verticalViewPager.setVisibility(View.GONE);
            nobookmarkTxt.setVisibility(View.VISIBLE);
            nobookmarkTxt.setText("Error loading bookmarks");
        }
    }

    private void loadInterstitialAd() {
        if (getActivity() == null) return;

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getActivity(), BuildConfig.ADMOB_INTERSTITIAL_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                            }
                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {}
                            @Override
                            public void onAdShowedFullScreenContent() {}
                        });
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    @Override
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {
        Log.d(TAG, "🔧 Bookmark clicked: " + quoteModel.isBookmared());
        showInterstitial();

        if (quoteModel.isBookmared()) {
            db = new DatabaseHelper(getActivity(), quoteModel);
            db.deleteNote(quoteModel);
            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
            quoteModel.setBookmared(false);
            loadData(); // Refresh after removal
        } else {
            db = new DatabaseHelper(getActivity(), quoteModel);
            quoteModel.setBookmark("1");
            db.insertNote(quoteModel);
            star.setImageDrawable(getResources().getDrawable(R.drawable.starfilled));
            quoteModel.setBookmared(true);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.notifyDataSetChanged();
            }
            if (progressManager != null) progressManager.onWordBookmarked();
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        }
    }

    @Override
    public void onCopyClick(QuoteModel quoteModel) {
        Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
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
        if (ttsHelper != null) ttsHelper.speak(quoteModel.getQuote());
    }

    @Override
    public void onExplainClick(QuoteModel quoteModel) {
        if (getActivity() != null && aiExplainHelper != null) {
            aiExplainHelper.explain(getActivity(), quoteModel.getQuote());
        }
    }

    @Override
    public void onShareAsImageClick(View cardView) {
        if (getActivity() != null) {
            ShareUtils.shareViewAsImage(getActivity(), cardView);
        }
    }

    @Override
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "🔧 onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "🔧 onDetach");
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        if (verticlePagerAdapter != null) verticlePagerAdapter.cleanup();
        if (ttsHelper != null) ttsHelper.shutdown();
        if (aiExplainHelper != null) aiExplainHelper.shutdown();
        super.onDestroy();
    }
}
package com.royal.edunotes._fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.royal.edunotes.AIExplainHelper;
import com.royal.edunotes.BookmarkHelper;
import com.royal.edunotes.BuildConfig;
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
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;

public class BookmarkFragment extends Fragment implements VerticlePagerAdapter.ClickInterface {
    private static final String TAG = "BookmarkFrag_MINIMAL";

    private DatabaseHelper db;
    private OnFragmentInteractionListener mListener;
    private VerticlePagerAdapter verticlePagerAdapter;
    private MyDatabase myDatabase;
    private TTSHelper ttsHelper;
    private AIExplainHelper aiExplainHelper;
    private ProgressManager progressManager;
    private InterstitialAd mInterstitialAd;
    private SettingsManager settingsManager;

    private View cardFilterContainer;
    private Spinner spinnerBookmarkFilter;
    private TextView tvBookmarkCountBadge;
    private VerticalViewPager verticalViewPager;
    private View layoutEmptyBookmarks;
    private TextView nobookmarkTxt;
    private TextView nobookmarkSubTxt;

    private String currentScreenType = "";
    private String currentFilterType = BookmarkHelper.FILTER_ALL;
    private ArrayList<QuoteModel> allBookmarksList = new ArrayList<>();
    private ArrayList<ModelDatabase> allModelDatabases = new ArrayList<>();
    private boolean isUpdatingSpinner = false;

    public static class FilterOption {
        public final String type;
        public final String label;
        public final int count;

        public FilterOption(String type, String label, int count) {
            this.type = type;
            this.label = label;
            this.count = count;
        }

        @Override
        public String toString() {
            return label + " (" + count + ")";
        }
    }

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
        if (BuildConfig.ENABLE_ADS) loadInterstitialAd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🔧 onCreateView - ScreenCheck: " + Utility.ScreenCheck);

        View v = inflater.inflate(R.layout.fragment_bookmark2, container, false);
        cardFilterContainer = v.findViewById(R.id.card_filter_container);
        spinnerBookmarkFilter = v.findViewById(R.id.spinner_bookmark_filter);
        tvBookmarkCountBadge = v.findViewById(R.id.tv_bookmark_count_badge);
        verticalViewPager = v.findViewById(R.id.vPager);
        layoutEmptyBookmarks = v.findViewById(R.id.layout_empty_bookmarks);
        nobookmarkTxt = v.findViewById(R.id.nobookmarkTxt);
        nobookmarkSubTxt = v.findViewById(R.id.nobookmarkSubTxt);

        // Initial state
        if (layoutEmptyBookmarks != null) layoutEmptyBookmarks.setVisibility(View.VISIBLE);
        if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);
        if (nobookmarkTxt != null) nobookmarkTxt.setText("Loading...");

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "🔧 onViewCreated - ScreenCheck: " + Utility.ScreenCheck);

        ttsHelper = new TTSHelper(getActivity());
        aiExplainHelper = new AIExplainHelper();
        progressManager = new ProgressManager(getActivity());
        settingsManager = new SettingsManager(getActivity());
        currentScreenType = Utility.ScreenCheck;

        if (spinnerBookmarkFilter != null) {
            spinnerBookmarkFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isUpdatingSpinner) return;
                    FilterOption selected = (FilterOption) parent.getItemAtPosition(position);
                    if (selected != null && !selected.type.equals(currentFilterType)) {
                        currentFilterType = selected.type;
                        applyFilterAndDisplay();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (verticalViewPager != null) {
            verticalViewPager.addOnPageChangeListener(new androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (verticlePagerAdapter == null || settingsManager == null) return;
                    QuoteModel current = verticlePagerAdapter.getItemAt(position);
                    if (current != null && current.getQuote() != null) {
                        settingsManager.setLastBookmarkNote(currentScreenType, current.getQuote());
                    }
                }
            });
        }

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void refresh() {
        Log.d(TAG, "🔧 refresh() called");
        if (getActivity() == null) return;
        currentScreenType = Utility.ScreenCheck;
        loadData();
    }

    private void loadData() {
        Log.d(TAG, "🔧 loadData() starting");

        if (getActivity() == null) return;

        try {
            db = new DatabaseHelper(getActivity());
            allBookmarksList = db.getAllBookmarkQuotes();
            allModelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();

            Log.d(TAG, "🔧 Total bookmarks: " + allBookmarksList.size() +
                    ", ModelDB: " + allModelDatabases.size());

            updateFilterSpinner();
            applyFilterAndDisplay();

        } catch (Exception e) {
            Log.e(TAG, "🔧 Error loading data: " + e.getMessage(), e);
            showError();
        }
    }

    private void updateFilterSpinner() {
        if (getActivity() == null || spinnerBookmarkFilter == null) return;

        isUpdatingSpinner = true;
        try {
            int totalCount = allBookmarksList.size();
            int vocabCount = BookmarkHelper.countByType(allBookmarksList, BookmarkHelper.FILTER_VOCAB);
            int idiomCount = BookmarkHelper.countByType(allBookmarksList, BookmarkHelper.FILTER_IDIOM);
            int quizCount = BookmarkHelper.countByType(allBookmarksList, BookmarkHelper.FILTER_QUIZ);
            int grammarCount = BookmarkHelper.countByType(allBookmarksList, BookmarkHelper.FILTER_GRAMMAR);

            List<FilterOption> options = new ArrayList<>();
            options.add(new FilterOption(BookmarkHelper.FILTER_ALL, "All Bookmarks", totalCount));
            options.add(new FilterOption(BookmarkHelper.FILTER_VOCAB, "Vocabulary", vocabCount));
            options.add(new FilterOption(BookmarkHelper.FILTER_IDIOM, "Idioms & Phrases", idiomCount));
            options.add(new FilterOption(BookmarkHelper.FILTER_QUIZ, "Quiz Questions", quizCount));
            if (grammarCount > 0) {
                options.add(new FilterOption(BookmarkHelper.FILTER_GRAMMAR, "Grammar Rules", grammarCount));
            }

            FilterSpinnerAdapter adapter = new FilterSpinnerAdapter(getActivity(), options);
            spinnerBookmarkFilter.setAdapter(adapter);

            int selectedIndex = 0;
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).type.equals(currentFilterType)) {
                    selectedIndex = i;
                    break;
                }
            }
            spinnerBookmarkFilter.setSelection(selectedIndex, false);
        } finally {
            isUpdatingSpinner = false;
        }
    }

    private void applyFilterAndDisplay() {
        if (getActivity() == null) return;

        ArrayList<QuoteModel> filtered = BookmarkHelper.filterBookmarks(allBookmarksList, currentFilterType);

        if (tvBookmarkCountBadge != null) {
            tvBookmarkCountBadge.setText(String.valueOf(filtered.size()));
        }

        if (allBookmarksList.isEmpty()) {
            if (cardFilterContainer != null) cardFilterContainer.setVisibility(View.GONE);
            if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);
            if (layoutEmptyBookmarks != null) layoutEmptyBookmarks.setVisibility(View.VISIBLE);
            if (nobookmarkTxt != null) nobookmarkTxt.setText("No bookmarks yet");
            if (nobookmarkSubTxt != null) {
                nobookmarkSubTxt.setText("Tap the star icon on any card or quiz question to bookmark it here.");
            }
        } else if (filtered.isEmpty()) {
            if (cardFilterContainer != null) cardFilterContainer.setVisibility(View.VISIBLE);
            if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);
            if (layoutEmptyBookmarks != null) layoutEmptyBookmarks.setVisibility(View.VISIBLE);

            String filterName = getFilterDisplayName(currentFilterType);
            if (nobookmarkTxt != null) nobookmarkTxt.setText("No " + filterName + " Bookmarks");
            if (nobookmarkSubTxt != null) {
                nobookmarkSubTxt.setText("You haven't bookmarked any " + filterName.toLowerCase() + " yet.\nSelect another category from the dropdown above.");
            }
        } else {
            if (cardFilterContainer != null) cardFilterContainer.setVisibility(View.VISIBLE);
            if (layoutEmptyBookmarks != null) layoutEmptyBookmarks.setVisibility(View.GONE);
            if (verticalViewPager != null) verticalViewPager.setVisibility(View.VISIBLE);

            verticlePagerAdapter = new VerticlePagerAdapter(getActivity(), filtered, this, allModelDatabases);
            verticalViewPager.setOffscreenPageLimit(0);
            verticalViewPager.setAdapter(verticlePagerAdapter);
            restoreLastPosition(filtered);
        }
    }

    private String getFilterDisplayName(String type) {
        if (BookmarkHelper.FILTER_VOCAB.equals(type)) return "Vocabulary";
        if (BookmarkHelper.FILTER_IDIOM.equals(type)) return "Idioms & Phrases";
        if (BookmarkHelper.FILTER_QUIZ.equals(type)) return "Quiz";
        if (BookmarkHelper.FILTER_GRAMMAR.equals(type)) return "Grammar";
        return "Bookmarks";
    }

    /** Jumps back to the bookmark the user was last viewing instead of resetting to the top. */
    private void restoreLastPosition(ArrayList<QuoteModel> bookmarks) {
        if (settingsManager == null || bookmarks == null || bookmarks.isEmpty() || verticlePagerAdapter == null) return;
        String lastNote = settingsManager.getLastBookmarkNote(currentScreenType);
        if (lastNote == null) return;

        for (int i = 0; i < bookmarks.size(); i++) {
            if (lastNote.equals(bookmarks.get(i).getQuote())) {
                int targetPos = verticlePagerAdapter.getPagerPositionForDataIndex(i);
                if (targetPos >= 0 && targetPos < verticlePagerAdapter.getCount()) {
                    verticalViewPager.setCurrentItem(targetPos, false);
                }
                break;
            }
        }
    }

    private void showError() {
        Log.d(TAG, "🔧 showError()");
        if (verticalViewPager != null) verticalViewPager.setVisibility(View.GONE);
        if (layoutEmptyBookmarks != null) layoutEmptyBookmarks.setVisibility(View.VISIBLE);
        if (nobookmarkTxt != null) nobookmarkTxt.setText("Error loading bookmarks");
        if (nobookmarkSubTxt != null) nobookmarkSubTxt.setText("Please try again later.");
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
        if (quoteModel == null) return;
        Log.d(TAG, "🔧 Bookmark clicked: " + quoteModel.isBookmared());
        if (BuildConfig.ENABLE_ADS) showInterstitial();

        if (db == null) db = new DatabaseHelper(getActivity());

        if (quoteModel.isBookmared()) {
            db.deleteNote(quoteModel);
            if (star != null) star.setImageResource(R.drawable.star);
            quoteModel.setBookmared(false);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), false);
            }
            Toast.makeText(getActivity(), "Removed from bookmarks", Toast.LENGTH_SHORT).show();
            loadData(); // Refresh list & counters
        } else {
            quoteModel.setBookmark("1");
            db.insertNote(quoteModel);
            if (star != null) star.setImageResource(R.drawable.starfilled);
            quoteModel.setBookmared(true);
            if (verticlePagerAdapter != null) {
                verticlePagerAdapter.setBookmarked(quoteModel.getQuote(), true);
            }
            if (progressManager != null) progressManager.onWordBookmarked();
            Toast.makeText(getActivity(), "Bookmarked", Toast.LENGTH_SHORT).show();
            loadData();
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        }
    }

    @Override
    public void onCopyClick(QuoteModel quoteModel) {
        if (quoteModel == null || getActivity() == null) return;
        Toast.makeText(getActivity(), "Copied", Toast.LENGTH_SHORT).show();
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", quoteModel.getQuote());
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onShareAsImageClick(View cardView, String text) {
        if (getActivity() == null) return;
        ShareUtils.shareViewAsImage(getActivity(), cardView, text);
    }

    @Override
    public void onTTSClick(QuoteModel quoteModel) {
        if (ttsHelper != null && quoteModel != null && quoteModel.getQuote() != null) {
            ttsHelper.speak(quoteModel.getQuote());
        }
    }

    @Override
    public void onLearnedClick(QuoteModel quoteModel, TextView learnedLabel) {
        if (settingsManager == null || quoteModel == null) return;
        boolean newState = !quoteModel.isLearned();
        settingsManager.setWordLearned(quoteModel.getQuote(), newState);
        quoteModel.setLearned(newState);
        if (learnedLabel != null) {
            learnedLabel.setText(newState ? getString(R.string.learned_on_label) : getString(R.string.learned_off_label));
        }
    }

    @Override
    public void onQuizClick() {
        if (getActivity() == null) return;
        startActivity(new Intent(getActivity(), com.royal.edunotes._activities.DailyQuizActivity.class));
    }

    @Override
    public void onMoreAppsClick() {
        if (getActivity() == null) return;
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

    private class FilterSpinnerAdapter extends ArrayAdapter<FilterOption> {
        public FilterSpinnerAdapter(@NonNull Context context, @NonNull List<FilterOption> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bookmark_spinner, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.tv_spinner_text);
            FilterOption item = getItem(position);
            if (item != null && tv != null) {
                tv.setText(item.label);
            }
            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bookmark_spinner_dropdown, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.tv_spinner_text);
            FilterOption item = getItem(position);
            if (item != null && tv != null) {
                tv.setText(item.label + " (" + item.count + ")");
            }
            return convertView;
        }
    }
}
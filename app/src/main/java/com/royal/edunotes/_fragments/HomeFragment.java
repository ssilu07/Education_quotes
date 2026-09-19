package com.royal.edunotes._fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.royal.edunotes.BuildConfig;
import com.royal.edunotes.Utility;
import com.royal.edunotes._activities.HackList;
import com.royal.edunotes._database.CardProgressDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.CategoryModel;
import com.royal.edunotes._adapters.CategoryAdapter;
import com.royal.edunotes.R;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements CategoryAdapter.CategoryClickInterface {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private List<CategoryModel> movieList;
    private RecyclerView recyclerView;
    ProgressBar progressBar;
    private CategoryAdapter mAdapter;
    GridLayoutManager manager;
    private OnFragmentInteractionListener mListener;
    private Menu menu = null;
    InterstitialAd mInterstitialAd;
    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (BuildConfig.ENABLE_ADS) loadInterstitialAd();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        movieList = new ArrayList<>();
        mAdapter = new CategoryAdapter(getActivity(), movieList, this);

        manager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false);
//        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                return (position % 3 == 0 ? 2 : 1);
//            }
//        });
        recyclerView.setLayoutManager(manager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        prepareMovieData();


        final SwipeRefreshLayout layout = v.findViewById(R.id.swipeRefreshLayout);
        layout.setOnRefreshListener(() -> {
            // start refresh

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layout.setRefreshing(false);
                }
            }, 4000); // Refresh for 4 seconds
        });



        return v;
    }

    private void prepareMovieData() {
        CardProgressDatabase progressDb = new CardProgressDatabase(getActivity());
        List<com.royal.edunotes.CategoryDataProvider.Category> allCats = com.royal.edunotes.CategoryDataProvider.getAllCategories();

        for (int i = 0; i < allCats.size(); i++) {
            com.royal.edunotes.CategoryDataProvider.Category cat = allCats.get(i);
            int totalCount = 0;
            int viewedCount = 0;

            for (com.royal.edunotes.CategoryDataProvider.SubCategory sub : cat.subCategories) {
                if (sub.hasSets()) {
                    for (com.royal.edunotes.CategoryDataProvider.SubCategory set : sub.subSets) {
                        try {
                            MyDatabase myDb = new MyDatabase(getActivity(), set.dbName, set.title);
                            totalCount += myDb.getTotalCount();
                            viewedCount += progressDb.getViewedCount(set.dbName);
                        } catch (Exception e) {
                            Log.e("Tag===", "Error getting progress: " + e.getMessage());
                        }
                    }
                } else {
                    try {
                        MyDatabase myDb = new MyDatabase(getActivity(), sub.dbName, sub.title);
                        totalCount += myDb.getTotalCount();
                        viewedCount += progressDb.getViewedCount(sub.dbName);
                    } catch (Exception e) {
                        Log.e("Tag===", "Error getting progress: " + e.getMessage());
                    }
                }
            }
            
            CategoryModel movie = new CategoryModel(cat.title, String.valueOf(i), viewedCount, totalCount);
            movieList.add(movie);
        }

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (movieList != null) {
            movieList.clear();
            prepareMovieData();
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

    public void categoryClick(CategoryModel categoryModel) {
        int categoryIndex = Integer.parseInt(categoryModel.getDbname());
        List<com.royal.edunotes.CategoryDataProvider.Category> allCats = com.royal.edunotes.CategoryDataProvider.getAllCategories();
        com.royal.edunotes.CategoryDataProvider.Category selected = allCats.get(categoryIndex);

        if (selected.subCategories.size() == 1) {
            com.royal.edunotes.CategoryDataProvider.SubCategory sub = selected.subCategories.get(0);
            Utility.ScreenCheck = selected.screenCheck;
            Intent intent = new Intent(getActivity(), HackList.class);
            intent.putExtra(Utility.TITLE_KEY, sub.title);
            intent.putExtra(Utility.DBNAME_KEY, sub.dbName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), com.royal.edunotes._activities.SubCategoryActivity.class);
            intent.putExtra("CATEGORY_INDEX", categoryIndex);
            intent.putExtra(Utility.TITLE_KEY, selected.title);
            intent.putExtra("SCREEN_CHECK", selected.screenCheck);
            startActivity(intent);
        }

        if (BuildConfig.ENABLE_ADS) {
            // showInterstitial();
        }
    }

   /* private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }*/

    private void loadInterstitialAd() {
        // Use test ad unit ID during development: "ca-app-pub-3940256099942544/1033173712"
        AdRequest adRequestNew = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(), BuildConfig.ADMOB_INTERSTITIAL_ID, adRequestNew, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d("TAG", "Interstitial Ad Loaded");

                // Set FullScreenContentCallback for ad events
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d("TAG", "Ad was dismissed.");
                        // Reset and reload the interstitial ad after it's dismissed
                        mInterstitialAd = null;
                        loadInterstitialAd();  // Immediately reload the ad after dismissal
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        Log.e("TAG", "Ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d("TAG", "Ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("TAG", "Ad failed to load: " + loadAdError.getMessage());
                mInterstitialAd = null; // Reset the ad if it fails to load
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Log.e("TAG", "No interstitial ad loaded");

            // Load a new ad if one is not available
            loadInterstitialAd();
        }
    }




    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

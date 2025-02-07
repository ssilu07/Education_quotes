package com.royal.edunotes._fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes._activities.HackList;
import com.royal.edunotes._adapters.CategoryAdapter;
import com.royal.edunotes._models.CategoryModel;

import java.util.ArrayList;
import java.util.List;


public class HomeIdiomFragment extends Fragment implements CategoryAdapter.CategoryClickInterface {
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
    public HomeIdiomFragment() {
    }

    public static HomeIdiomFragment newInstance(String param1, String param2) {
        HomeIdiomFragment fragment = new HomeIdiomFragment();
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
        loadInterstitialAd();
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

        String[] catdata = getResources().getStringArray(R.array.mycat);
        String[] dbdata = getResources().getStringArray(R.array.mydb);

        Log.e("Tag===", "SIZE : " + catdata.length + "   " + dbdata.length);


        for (int i = 0; i < catdata.length; i++) {
            CategoryModel movie = new CategoryModel(catdata[i], dbdata[i]);
            movieList.add(movie);
        }


        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        mAdapter.notifyDataSetChanged();
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
    public void categoryClick(CategoryModel categoryModel) {
//        Toast.makeText(getActivity(), categoryModel.getTitle(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), HackList.class);
        intent.putExtra(Utility.TITLE_KEY, categoryModel.getTitle());
        intent.putExtra(Utility.DBNAME_KEY, categoryModel.getDbname());
        startActivity(intent);
        showInterstitial();


/*
        mInterstitialAd = new InterstitialAd(getActivity());

     //    set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        AdRequest adRequest = new AdRequest.Builder()
                .build();
*/

        // Load ads into Interstitial Ads
     //   mInterstitialAd.loadAd(adRequest);

     /*   mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e("TAG===", "Error ad :" + i);
            }
        });*/
    }

   /* private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }*/

    private void loadInterstitialAd() {
        // Use test ad unit ID during development: "ca-app-pub-3940256099942544/1033173712"
        AdRequest adRequestNew = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(), getString(R.string.interstitial_full_screen), adRequestNew, new InterstitialAdLoadCallback() {
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

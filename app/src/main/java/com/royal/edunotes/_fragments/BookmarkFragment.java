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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.royal.edunotes.R;
import com.royal.edunotes.VerticalViewPager;
import com.royal.edunotes._adapters.VerticlePagerAdapter;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class BookmarkFragment extends Fragment implements VerticlePagerAdapter.ClickInterface{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    DatabaseHelper db;
    private OnFragmentInteractionListener mListener;
    VerticlePagerAdapter verticlePagerAdapter;
    MyDatabase myDatabase;
    ArrayList<QuoteModel> quoteModels;
    TextView nobookmarkTxt;
    VerticalViewPager verticalViewPager;
    InterstitialAd mInterstitialAd;
    public BookmarkFragment() {
    }
    public static BookmarkFragment newInstance(String param1, String param2) {
        BookmarkFragment fragment = new BookmarkFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_bookmark2, container, false);


        nobookmarkTxt = (TextView) v.findViewById(R.id.nobookmarkTxt);

        quoteModels = new ArrayList<>();

        verticalViewPager= (VerticalViewPager) v.findViewById(R.id.vPager);


        db = new DatabaseHelper(getActivity());
        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
        Log.e("TAG1===", "SIZEEEE : " + modelDatabases.size());


        myDatabase = new MyDatabase(getActivity(),"bookmark_db");

        quoteModels = myDatabase.getBookmarkData();


        if(quoteModels.size()==0){

            verticalViewPager.setVisibility(View.GONE);
            nobookmarkTxt.setVisibility(View.VISIBLE);

        }else{
            verticalViewPager.setVisibility(View.VISIBLE);
            nobookmarkTxt.setVisibility(View.GONE);
            verticlePagerAdapter  = new VerticlePagerAdapter(getActivity(),quoteModels,this,modelDatabases);
            verticalViewPager.setOffscreenPageLimit(0);
            verticalViewPager.setAdapter(verticlePagerAdapter);

        }


        return v;
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



    public void refresh(){

        quoteModels = new ArrayList<>();

        db = new DatabaseHelper(getActivity());
        ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
        Log.e("TAG1===", "SIZEEEE : " + modelDatabases.size());


        myDatabase = new MyDatabase(getActivity(),"bookmark_db");

        quoteModels = myDatabase.getBookmarkData();


        if(quoteModels.size()==0){

            verticalViewPager.setVisibility(View.GONE);
            nobookmarkTxt.setVisibility(View.VISIBLE);

        }else{
            verticalViewPager.setVisibility(View.VISIBLE);
            nobookmarkTxt.setVisibility(View.GONE);
            verticlePagerAdapter  = new VerticlePagerAdapter(getActivity(),quoteModels,this,modelDatabases);
            verticalViewPager.setOffscreenPageLimit(0);
            verticalViewPager.setAdapter(verticlePagerAdapter);
        }
    }

    @Override
    public void onBoookmarkClick(QuoteModel quoteModel, ImageView star) {

        Log.e("TAGGG ===", quoteModel.getTimestamp());
        Log.e("TAGGG ===", String.valueOf(quoteModel.getId()));
        Log.e("TAGGG ===", quoteModel.getQuote());
        Log.e("TAGGG ===", quoteModel.getCategoryName());





        mInterstitialAd = new InterstitialAd(getActivity());

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



            db = new DatabaseHelper(getActivity(), quoteModel);

            db.deleteNote(quoteModel);


            quoteModels.remove(quoteModel);


            db = new DatabaseHelper(getActivity());
            ArrayList<ModelDatabase> modelDatabases = (ArrayList<ModelDatabase>) db.getAllNotes();
            Log.e("TAG1===", "SIZEEEE : " + modelDatabases.size());


            star.setImageDrawable(getResources().getDrawable(R.drawable.star));
            quoteModel.setBookmared(false);



            if(quoteModels.size()==0){

                verticalViewPager.setVisibility(View.GONE);
                nobookmarkTxt.setVisibility(View.VISIBLE);

            }else{
                verticalViewPager.setVisibility(View.VISIBLE);
                nobookmarkTxt.setVisibility(View.GONE);
                verticlePagerAdapter  = new VerticlePagerAdapter(getActivity(),quoteModels,this,modelDatabases);
                verticalViewPager.setOffscreenPageLimit(0);
                verticalViewPager.setAdapter(verticlePagerAdapter);

            }








            verticlePagerAdapter.notifyDataSetChanged();


        } else {

//           Add in to bookmark table

            db = new DatabaseHelper(getActivity(), quoteModel);

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
    public void onMoreAppsClick() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=Hirvasoft")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/search?q=pub:Hirvasoft")));
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

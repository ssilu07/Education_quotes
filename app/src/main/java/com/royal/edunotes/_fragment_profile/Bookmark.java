package com.royal.edunotes._fragment_profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.royal.edunotes.R;
import com.royal.edunotes._activities.BookmarkHackListActivity;
import com.royal.edunotes._adapters.BookMarkAdapter;
import com.royal.edunotes._models.bookmarkModel;

import java.util.ArrayList;
import java.util.List;

public class Bookmark extends Fragment implements BookMarkAdapter.BookmarkClickInterface {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private List<bookmarkModel> movieList = new ArrayList<>();
    private RecyclerView recyclerView;
    ProgressBar progressBar;
    private BookMarkAdapter mAdapter;
    GridLayoutManager manager;

    public Bookmark() {
    }

    public static Bookmark newInstance(String param1, String param2) {
        Bookmark fragment = new Bookmark();
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
        View v = inflater.inflate(R.layout.fragment_bookmark, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);


        mAdapter = new BookMarkAdapter(getActivity(), movieList, this);

        manager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);

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
        bookmarkModel movie = new bookmarkModel("1", "Technology Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Food Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Science Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Daily Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Technology Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Technology Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Technology Hack", getString(R.string.dummyText));
        movieList.add(movie);

        movie = new bookmarkModel("1", "Technology Hack", getString(R.string.dummyText));
        movieList.add(movie);

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void bookmarkClick(bookmarkModel bookmarkModel) {
        startActivity(new Intent(getActivity(), BookmarkHackListActivity.class));
    }
}

package com.royal.edunotes._activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;

import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.royal.edunotes.R;
import com.royal.edunotes.CustomViewPager;
import com.royal.edunotes.NotificationHelper;
import com.royal.edunotes.Utility;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._fragments.BookmarkFragment;
import com.royal.edunotes._fragments.HomeFragment;
import com.royal.edunotes._fragments.LatestFragment;
import com.royal.edunotes._fragments.ProfileFragment;
import com.royal.edunotes._fragments.TrendingFragment;

import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener,
        TrendingFragment.OnFragmentInteractionListener, LatestFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener, BookmarkFragment.OnFragmentInteractionListener, SearchView.OnQueryTextListener {
    private Toolbar mToolbar;
    boolean first = true;
    boolean second = true;
    DatabaseHelper db;
    CustomViewPager viewPager;
    ViewPagerAdapter adapter;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationHelper.scheduleRepeatingRTCNotification(getApplicationContext(), "", "");
        NotificationHelper.enableBootReceiver(getApplicationContext());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initUI();

        if (!isNetworkConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Please turn on internet connection");
            builder.setCancelable(false);
            builder.setPositiveButton("RETRY",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated


                        }
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();
//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;

                    if (isNetworkConnected()) {
                        wantToCloseDialog = true;
                    } else {
                        Toast.makeText(MainActivity.this, "Still internet is not available ...", Toast.LENGTH_SHORT).show();
                        wantToCloseDialog = false;
                    }


                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog) {
                        dialog.dismiss();
//                        Show ads here...............
                    }

                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });


        }


    }

    public void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }


    private void initUI() {
        viewPager = (CustomViewPager) findViewById(R.id.vp_horizontal_ntb);
        setupViewPager(viewPager);

        final String[] colors = getResources().getStringArray(R.array.default_preview);
        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_first),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_sixth))
                        .title("Home")

                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(

                        getResources().getDrawable(R.drawable.ic_second),
                        Color.parseColor(colors[1]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Trending")
                        .badgeTitle("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_third),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.drawable.ic_seventh))
                        .title("Latest")
                        .badgeTitle("")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_fourth),
                        Color.parseColor(colors[3]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Bookmark")
                        .badgeTitle("")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 0);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();

                if (position == 0) {
                    mToolbar.setTitle(getResources().getString(R.string.app_name));
                } else if (position == 1) {
                    mToolbar.setTitle(getResources().getString(R.string.trending));
                } else if (position == 2) {
                    mToolbar.setTitle(getResources().getString(R.string.latest));
                } else if (position == 3) {
                    mToolbar.setTitle(getResources().getString(R.string.bookmark));
                    Fragment activeFragment = adapter.getItem(position);
                    ((BookmarkFragment) activeFragment).refresh();
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }
//
    public void showSearchPrompt() {
        new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText("HELLOOOOOO")
                .setSecondaryText("This is test This is test This is test This is test This is test ")
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setTarget(R.id.action_search)
                .show();
    }
//
//
//    public void showQuotePrompt() {
//        new MaterialTapTargetPrompt.Builder(this)
//                .setPrimaryText("HELLOOOOOO")
//                .setSecondaryText("This is test This is test This is test This is test This is test ")
//                .setAnimationInterpolator(new FastOutSlowInInterpolator())
//                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
//                .setTarget(R.id.action_todayquote)
//                .show();
//    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new TrendingFragment());
        adapter.addFragment(new LatestFragment());
        adapter.addFragment(new BookmarkFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //Todo For Search Icon Commente
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.e("TAG===", "STR : " + query);
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra(Utility.SEARCH_KEY, query);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }


    //Todo For Search Icon Comment

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_todayquote) {
//            if (second) {
//                showQuotePrompt();
//                second = false;
//
//            } else {
//                Toast.makeText(this, "Clickedddd", Toast.LENGTH_SHORT).show();
//            }
//
//            return true;
//        }

        if (id == R.id.action_search) {

            if (first) {
                showSearchPrompt();
                first = false;

            } else {
                Toast.makeText(this, "Clickeddddddd", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();

            return true;
        }


//        if (id == R.id.action_settings) {
//            startActivity(new Intent(getApplicationContext(), Setting.class));
//            return true;
//        }


        return super.onOptionsItemSelected(item);
    }
}



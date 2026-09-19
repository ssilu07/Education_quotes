package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.CategoryDataProvider;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.Utility;
import com.royal.edunotes._adapters.CategoryAdapter;
import com.royal.edunotes._database.CardProgressDatabase;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends AppCompatActivity implements CategoryAdapter.CategoryClickInterface {

    private List<CategoryModel> subList = new ArrayList<>();
    private CategoryAdapter adapter;
    private int categoryIndex;
    private String screenCheck = "Vocab";
    private String parentSubDbName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        categoryIndex = getIntent().getIntExtra("CATEGORY_INDEX", 0);
        String title = getIntent().getStringExtra(Utility.TITLE_KEY);
        String sc = getIntent().getStringExtra("SCREEN_CHECK");
        if (sc != null) screenCheck = sc;
        parentSubDbName = getIntent().getStringExtra("PARENT_SUB_DBNAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        com.royal.edunotes.WindowInsetsHelper.applyEdgeToEdge(this, toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Sub Categories");
        }

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new CategoryAdapter(this, subList, this);
        rv.setAdapter(adapter);

        loadSubCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubCategories();
    }

    private void loadSubCategories() {
        subList.clear();
        CardProgressDatabase progressDb = new CardProgressDatabase(this);
        List<CategoryDataProvider.Category> allCats = CategoryDataProvider.getAllCategories();
        if (categoryIndex < allCats.size()) {
            CategoryDataProvider.Category cat = allCats.get(categoryIndex);

            if (parentSubDbName != null) {
                // Showing sets under a specific subcategory (e.g. Articles -> Set - 1)
                for (CategoryDataProvider.SubCategory sub : cat.subCategories) {
                    if (sub.dbName.equals(parentSubDbName) && sub.hasSets()) {
                        for (CategoryDataProvider.SubCategory set : sub.subSets) {
                            int totalCount = 0;
                            int viewedCount = 0;
                            try {
                                MyDatabase myDb = new MyDatabase(this, set.dbName, set.title);
                                totalCount = myDb.getTotalCount();
                                viewedCount = progressDb.getViewedCount(set.dbName);
                            } catch (Exception e) {
                                // DB might not exist or be empty
                            }
                            subList.add(new CategoryModel(set.title, set.dbName, viewedCount, totalCount));
                        }
                        break;
                    }
                }
            } else {
                // Showing subcategories directly under main category
                for (int i = 0; i < cat.subCategories.size(); i++) {
                    CategoryDataProvider.SubCategory sub = cat.subCategories.get(i);
                    int totalCount = 0;
                    int viewedCount = 0;

                    if (sub.hasSets()) {
                        for (CategoryDataProvider.SubCategory set : sub.subSets) {
                            try {
                                MyDatabase myDb = new MyDatabase(this, set.dbName, set.title);
                                totalCount += myDb.getTotalCount();
                                viewedCount += progressDb.getViewedCount(set.dbName);
                            } catch (Exception e) {
                                // DB might not exist or be empty
                            }
                        }
                    } else {
                        try {
                            MyDatabase myDb = new MyDatabase(this, sub.dbName, sub.title);
                            totalCount = myDb.getTotalCount();
                            viewedCount = progressDb.getViewedCount(sub.dbName);
                        } catch (Exception e) {
                            // DB might not exist or be empty
                        }
                    }
                    subList.add(new CategoryModel(sub.title, sub.dbName, viewedCount, totalCount));
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void categoryClick(CategoryModel categoryModel) {
        List<CategoryDataProvider.Category> allCats = CategoryDataProvider.getAllCategories();
        if (categoryIndex < allCats.size()) {
            CategoryDataProvider.Category cat = allCats.get(categoryIndex);

            if (parentSubDbName != null) {
                // User clicked a set (e.g. Set - 1)
                for (CategoryDataProvider.SubCategory sub : cat.subCategories) {
                    if (sub.dbName.equals(parentSubDbName) && sub.hasSets()) {
                        for (CategoryDataProvider.SubCategory set : sub.subSets) {
                            if (set.dbName.equals(categoryModel.getDbname())) {
                                if ("Quiz".equalsIgnoreCase(set.screenCheck)) {
                                    Intent intent = new Intent(this, QuizActivity.class);
                                    intent.putExtra(Utility.TITLE_KEY, set.title);
                                    intent.putExtra(Utility.DBNAME_KEY, set.dbName);
                                    boolean isDsssb = "DSSSB".equalsIgnoreCase(cat.title) || (cat.title != null && cat.title.toLowerCase().contains("dsssb"));
                                    intent.putExtra("IS_DSSSB", isDsssb);
                                    startActivity(intent);
                                } else {
                                    Utility.ScreenCheck = set.screenCheck != null ? set.screenCheck : screenCheck;
                                    Intent intent = new Intent(this, HackList.class);
                                    intent.putExtra(Utility.TITLE_KEY, set.title);
                                    intent.putExtra(Utility.DBNAME_KEY, set.dbName);
                                    startActivity(intent);
                                }
                                return;
                            }
                        }
                    }
                }
            } else {
                // User clicked a subcategory (e.g. Articles, Synonyms, etc.)
                for (CategoryDataProvider.SubCategory sub : cat.subCategories) {
                    if (sub.dbName.equals(categoryModel.getDbname())) {
                        if (sub.hasSets()) {
                            // Open SubCategoryActivity to display sets (Set - 1, etc.)
                            Intent intent = new Intent(this, SubCategoryActivity.class);
                            intent.putExtra("CATEGORY_INDEX", categoryIndex);
                            intent.putExtra("PARENT_SUB_DBNAME", sub.dbName);
                            intent.putExtra(Utility.TITLE_KEY, sub.title);
                            intent.putExtra("SCREEN_CHECK", sub.screenCheck);
                            startActivity(intent);
                        } else if ("Quiz".equalsIgnoreCase(sub.screenCheck)) {
                            Intent intent = new Intent(this, QuizActivity.class);
                            intent.putExtra(Utility.TITLE_KEY, sub.title);
                            intent.putExtra(Utility.DBNAME_KEY, sub.dbName);
                            boolean isDsssb = "DSSSB".equalsIgnoreCase(cat.title) || (cat.title != null && cat.title.toLowerCase().contains("dsssb"));
                            intent.putExtra("IS_DSSSB", isDsssb);
                            startActivity(intent);
                        } else {
                            Utility.ScreenCheck = sub.screenCheck != null ? sub.screenCheck : screenCheck;
                            Intent intent = new Intent(this, HackList.class);
                            intent.putExtra(Utility.TITLE_KEY, categoryModel.getTitle());
                            intent.putExtra(Utility.DBNAME_KEY, categoryModel.getDbname());
                            startActivity(intent);
                        }
                        return;
                    }
                }
            }
        }

        // Fallback
        Utility.ScreenCheck = screenCheck;
        Intent intent = new Intent(this, HackList.class);
        intent.putExtra(Utility.TITLE_KEY, categoryModel.getTitle());
        intent.putExtra(Utility.DBNAME_KEY, categoryModel.getDbname());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}

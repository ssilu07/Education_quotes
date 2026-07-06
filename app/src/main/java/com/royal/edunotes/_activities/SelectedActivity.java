package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.CategoryDataProvider;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.Utility;
import com.royal.edunotes._adapters.CategoryAdapter;
import com.royal.edunotes._models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class SelectedActivity extends AppCompatActivity implements CategoryAdapter.CategoryClickInterface {

    private List<CategoryModel> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Choose Category");
        }

        RecyclerView rv = findViewById(R.id.rv_categories);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CategoryAdapter(this, categoryList, this);
        rv.setAdapter(adapter);

        loadCategories();
    }

    private void loadCategories() {
        List<CategoryDataProvider.Category> allCats = CategoryDataProvider.getAllCategories();
        for (int i = 0; i < allCats.size(); i++) {
            CategoryDataProvider.Category cat = allCats.get(i);
            // dbname stores the category index so SubCategoryActivity can look it up
            categoryList.add(new CategoryModel(cat.title, String.valueOf(i)));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_selected, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(SelectedActivity.this, SearchActivity.class);
                intent.putExtra(Utility.SEARCH_KEY, query);
                startActivity(intent);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public void categoryClick(CategoryModel categoryModel) {
        int categoryIndex = Integer.parseInt(categoryModel.getDbname());
        List<CategoryDataProvider.Category> allCats = CategoryDataProvider.getAllCategories();
        CategoryDataProvider.Category selected = allCats.get(categoryIndex);

        if (selected.subCategories.size() == 1) {
            // Only 1 sub-category → go directly to content
            CategoryDataProvider.SubCategory sub = selected.subCategories.get(0);
            Utility.ScreenCheck = selected.screenCheck;
            Intent intent = new Intent(this, HackList.class);
            intent.putExtra(Utility.TITLE_KEY, sub.title);
            intent.putExtra(Utility.DBNAME_KEY, sub.dbName);
            startActivity(intent);
        } else {
            // Multiple sub-categories → open SubCategoryActivity
            Intent intent = new Intent(this, SubCategoryActivity.class);
            intent.putExtra("CATEGORY_INDEX", categoryIndex);
            intent.putExtra(Utility.TITLE_KEY, selected.title);
            intent.putExtra("SCREEN_CHECK", selected.screenCheck);
            startActivity(intent);
        }
    }
}

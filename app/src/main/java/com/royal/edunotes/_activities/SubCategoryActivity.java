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
import com.royal.edunotes._models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryActivity extends AppCompatActivity implements CategoryAdapter.CategoryClickInterface {

    private List<CategoryModel> subList = new ArrayList<>();
    private CategoryAdapter adapter;
    private int categoryIndex;
    private String screenCheck = "Vocab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        categoryIndex = getIntent().getIntExtra("CATEGORY_INDEX", 0);
        String title = getIntent().getStringExtra(Utility.TITLE_KEY);
        String sc = getIntent().getStringExtra("SCREEN_CHECK");
        if (sc != null) screenCheck = sc;

        Toolbar toolbar = findViewById(R.id.toolbar);
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

    private void loadSubCategories() {
        List<CategoryDataProvider.Category> allCats = CategoryDataProvider.getAllCategories();
        if (categoryIndex < allCats.size()) {
            CategoryDataProvider.Category cat = allCats.get(categoryIndex);
            for (int i = 0; i < cat.subCategories.size(); i++) {
                CategoryDataProvider.SubCategory sub = cat.subCategories.get(i);
                subList.add(new CategoryModel(sub.title, sub.dbName));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void categoryClick(CategoryModel categoryModel) {
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

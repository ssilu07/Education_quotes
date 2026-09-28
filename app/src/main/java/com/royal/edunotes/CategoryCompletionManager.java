package com.royal.edunotes;

import android.content.Context;
import android.content.SharedPreferences;

import com.royal.edunotes._models.CategoryModel;

public class CategoryCompletionManager {

    private static final String PREF_NAME = "category_completion_prefs";
    private static final String KEY_PREFIX = "cat_done_";

    private static CategoryCompletionManager instance;
    private final SharedPreferences prefs;

    private CategoryCompletionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized CategoryCompletionManager getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryCompletionManager(context);
        }
        return instance;
    }

    public static String getCategoryKey(CategoryModel model) {
        if (model == null) return "";
        String db = model.getDbname();
        if (db != null && !db.trim().isEmpty() && !db.matches("\\d+")) {
            return db.trim();
        }
        if (model.getTitle() != null && !model.getTitle().trim().isEmpty()) {
            return "title_" + model.getTitle().trim();
        }
        return db != null ? db.trim() : "";
    }

    public boolean isCompleted(String categoryKey) {
        if (categoryKey == null || categoryKey.isEmpty()) return false;
        return prefs.getBoolean(KEY_PREFIX + categoryKey, false);
    }

    public void setCompleted(String categoryKey, boolean completed) {
        if (categoryKey == null || categoryKey.isEmpty()) return;
        prefs.edit().putBoolean(KEY_PREFIX + categoryKey, completed).apply();
    }

    public boolean toggleCompleted(String categoryKey) {
        if (categoryKey == null || categoryKey.isEmpty()) return false;
        boolean newState = !isCompleted(categoryKey);
        setCompleted(categoryKey, newState);
        return newState;
    }

    public boolean isCompleted(CategoryModel model) {
        return isCompleted(getCategoryKey(model));
    }

    public void setCompleted(CategoryModel model, boolean completed) {
        setCompleted(getCategoryKey(model), completed);
    }

    public boolean toggleCompleted(CategoryModel model) {
        return toggleCompleted(getCategoryKey(model));
    }
}

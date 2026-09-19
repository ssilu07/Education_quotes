package com.royal.edunotes._database;

/**
 * Created by Admin on 21-06-2018.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.royal.edunotes._models.QuoteModel;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.royal.edunotes.BuildConfig;

import java.util.ArrayList;


public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "life_quotes";
    // Controlled manually from build.gradle
    private static final int DATABASE_VERSION = BuildConfig.DATABASE_VERSION;
    private static final String ID = "id";
    private static final String QUOTE = "quote";
    private static final String VALUE = "value";

    private static final String TIMESTAMP = "timestamp";
    public static final String TABLE_NAME = "inspiring_life_quote";


    public static final String BOOKMARK_TABLE = "bookmarktable";
    public static final String BOOKMARK_DB = "bookmark_db";

    public static final String BOOKMARK = "bookmark";
    public static final String CATEGORY = "category";

    public static final String NOTE = "note";
    public static final String NOTEVALUE = "notevalue";



    String categoryName;
    Context context;

    public MyDatabase(Context context, String dbname, String categoryName) {
        super(context, dbname, null, DATABASE_VERSION);
        setForcedUpgrade(); // Force overwrite of old database with new assets database on upgrade
        this.categoryName = categoryName;
        this.context = context;
    }


    public MyDatabase(Context context, String dbname) {
        super(context, dbname, null, DATABASE_VERSION);
        setForcedUpgrade(); // Force overwrite of old database with new assets database on upgrade
        this.context = context;
    }

    public ArrayList<QuoteModel> getSearchedData(String searchKey) {
        ArrayList<QuoteModel> list = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            // Simulate word-boundary match using LIKE this is use for exact search
            String whereClause = QUOTE + " LIKE ? OR " + QUOTE + " LIKE ? OR " + QUOTE + " LIKE ? OR " + QUOTE + " LIKE ?";
            String[] whereArgs = new String[]{
                    searchKey,                      // exact match
                    searchKey + " %",               // start of sentence
                    "% " + searchKey,               // end of sentence
                    "% " + searchKey + " %"         // word in middle
            };

            cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(ID);
                int quoteIdx = cursor.getColumnIndex(QUOTE);
                int timeIdx = cursor.getColumnIndex(TIMESTAMP);

                while (cursor.moveToNext()) {
                    QuoteModel model = new QuoteModel();
                    if (idIdx != -1) model.id = cursor.getInt(idIdx);
                    if (quoteIdx != -1) model.quote = cursor.getString(quoteIdx);
                    if (timeIdx != -1) model.timestamp = cursor.getString(timeIdx);
                    model.categoryName = categoryName;
                    list.add(model);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MyDatabase", "getSearchedData error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }

    /** Plain case-insensitive substring search across the whole card text (word + meaning + example). */
    public ArrayList<QuoteModel> getWordMatches(String query) {
        ArrayList<QuoteModel> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return list;

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String whereClause = QUOTE + " LIKE ?";
            String[] whereArgs = {"%" + query.trim() + "%"};

            cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(ID);
                int quoteIdx = cursor.getColumnIndex(QUOTE);
                int valIdx = cursor.getColumnIndex(VALUE);
                int timeIdx = cursor.getColumnIndex(TIMESTAMP);

                while (cursor.moveToNext()) {
                    QuoteModel model = new QuoteModel();
                    if (idIdx != -1) model.id = cursor.getInt(idIdx);
                    if (quoteIdx != -1) model.quote = cursor.getString(quoteIdx);
                    if (valIdx != -1) model.value = cursor.getString(valIdx);
                    if (timeIdx != -1) model.timestamp = cursor.getString(timeIdx);
                    model.categoryName = categoryName;
                    list.add(model);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MyDatabase", "getWordMatches error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return list;
    }

    public ArrayList<QuoteModel> getPoses() {
        ArrayList<QuoteModel> questionsArrayList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(ID);
                int quoteIdx = cursor.getColumnIndex(QUOTE);
                int valIdx = cursor.getColumnIndex(VALUE);
                int timeIdx = cursor.getColumnIndex(TIMESTAMP);

                while (cursor.moveToNext()) {
                    QuoteModel questions = new QuoteModel();
                    if (idIdx != -1) questions.id = cursor.getInt(idIdx);
                    if (quoteIdx != -1) questions.quote = cursor.getString(quoteIdx);
                    if (valIdx != -1) questions.value = cursor.getString(valIdx);
                    if (timeIdx != -1) questions.timestamp = cursor.getString(timeIdx);
                    questions.categoryName = categoryName;
                    questionsArrayList.add(questions);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MyDatabase", "getPoses error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return questionsArrayList;
    }

    public int getTotalCount() {
        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            // DB might not exist or table missing
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return count;
    }

    public ArrayList<QuoteModel> getBookmarkData() {
        ArrayList<QuoteModel> questionsArrayList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(BOOKMARK_TABLE, null, null, null, null, null, null);
            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(ID);
                int noteIdx = cursor.getColumnIndex(NOTE);
                int valIdx = cursor.getColumnIndex(NOTEVALUE);
                int timeIdx = cursor.getColumnIndex(TIMESTAMP);
                int bmIdx = cursor.getColumnIndex(BOOKMARK);
                int catIdx = cursor.getColumnIndex(CATEGORY);

                while (cursor.moveToNext()) {
                    QuoteModel questions = new QuoteModel();
                    if (idIdx != -1) questions.id = cursor.getInt(idIdx);
                    if (noteIdx != -1) questions.quote = cursor.getString(noteIdx);
                    if (valIdx != -1) questions.value = cursor.getString(valIdx);
                    if (timeIdx != -1) questions.timestamp = cursor.getString(timeIdx);
                    if (catIdx != -1) questions.categoryName = cursor.getString(catIdx);
                    if (bmIdx != -1) questions.bookmark = cursor.getString(bmIdx);
                    questionsArrayList.add(questions);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MyDatabase", "getBookmarkData error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return questionsArrayList;
    }


}
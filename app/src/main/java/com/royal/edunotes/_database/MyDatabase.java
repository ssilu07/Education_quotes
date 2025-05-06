package com.royal.edunotes._database;

/**
 * Created by Admin on 21-06-2018.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.royal.edunotes._models.QuoteModel;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;


public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "life_quotes";
    private static final int DATABASE_VERSION = 1;
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
        this.categoryName = categoryName;
        this.context = context;
    }


    public MyDatabase(Context context, String dbname) {
        super(context, dbname, null, DATABASE_VERSION);
        this.context = context;
    }

    public ArrayList<QuoteModel> getSearchedData(String searchKey) {
        ArrayList<QuoteModel> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        // Use only columns that are guaranteed to exist in all DBs
        String[] columns = {ID, QUOTE, TIMESTAMP};

/*        String whereClause = QUOTE + " LIKE ?";
        String[] whereArgs = new String[]{"%" + searchKey + "%"};*/

        // Simulate word-boundary match using LIKE this is use for exact search
        String whereClause = QUOTE + " LIKE ? OR " + QUOTE + " LIKE ? OR " + QUOTE + " LIKE ? OR " + QUOTE + " LIKE ?";
        String[] whereArgs = new String[]{
                searchKey,                      // exact match
                searchKey + " %",               // start of sentence
                "% " + searchKey,               // end of sentence
                "% " + searchKey + " %"         // word in middle
        };

        Cursor cursor = db.query(TABLE_NAME, columns, whereClause, whereArgs, null, null, null);

        while (cursor.moveToNext()) {
            QuoteModel model = new QuoteModel();
            model.id = cursor.getInt(cursor.getColumnIndex(ID));
            model.quote = cursor.getString(cursor.getColumnIndex(QUOTE));
            model.timestamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP));

            // set the category name from current DB name passed in constructor
            model.categoryName = categoryName;

            list.add(model);
        }

        cursor.close();
        db.close();

        return list;
    }



    public ArrayList<QuoteModel> getPoses() {


        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {MyDatabase.ID, MyDatabase.QUOTE,MyDatabase.VALUE, MyDatabase.TIMESTAMP};
//        String[] selectionArgs={categoryId+"",subjectId+"",yearId+""};
        Cursor cursor = db.query(MyDatabase.TABLE_NAME, columns, null, null, null, null, null);
//        Cursor cursor=db.query(MyDatabase.TABLE_NAME, columns, null,null, null, null, null);
        ArrayList<QuoteModel> questionsArrayList = new ArrayList<>();

        while (cursor.moveToNext()) {
            QuoteModel questions = new QuoteModel();
            questions.id = cursor.getInt(cursor.getColumnIndex(MyDatabase.ID));
            questions.quote = cursor.getString(cursor.getColumnIndex(MyDatabase.QUOTE));
            questions.value = cursor.getString(cursor.getColumnIndex(MyDatabase.VALUE));
            questions.timestamp = cursor.getString(cursor.getColumnIndex(MyDatabase.TIMESTAMP));
            questions.categoryName = categoryName;
            questionsArrayList.add(questions);
        }
        return questionsArrayList;
    }


    public ArrayList<QuoteModel> getBookmarkData() {


        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {MyDatabase.ID, MyDatabase.NOTE,MyDatabase.NOTEVALUE, MyDatabase.TIMESTAMP, MyDatabase.BOOKMARK, MyDatabase.CATEGORY};
        Cursor cursor = db.query(MyDatabase.BOOKMARK_TABLE, columns, null, null, null, null, null);
        ArrayList<QuoteModel> questionsArrayList = new ArrayList<>();

        while (cursor.moveToNext()) {
            QuoteModel questions = new QuoteModel();
            questions.id = cursor.getInt(cursor.getColumnIndex(MyDatabase.ID));
            questions.quote = cursor.getString(cursor.getColumnIndex(MyDatabase.NOTE));
            questions.value = cursor.getString(cursor.getColumnIndex(MyDatabase.NOTEVALUE));
            questions.timestamp = cursor.getString(cursor.getColumnIndex(MyDatabase.TIMESTAMP));
            questions.categoryName = cursor.getString(cursor.getColumnIndex(MyDatabase.CATEGORY));
            questions.bookmark = cursor.getString(cursor.getColumnIndex(MyDatabase.BOOKMARK));
            questionsArrayList.add(questions);
        }
        return questionsArrayList;
    }


}
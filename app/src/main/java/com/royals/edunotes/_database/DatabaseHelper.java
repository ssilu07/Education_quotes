package com.royals.edunotes._database;

/**
 * Created by Admin on 18-06-2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.royals.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private List<ModelDatabase> databases = new ArrayList<>();


    QuoteModel quoteModel;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "bookmark_db";

    private static final String ALL_DB_NAME = "alldata";


    public DatabaseHelper(Context context, QuoteModel quoteModel) {


        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.quoteModel = quoteModel;

    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String dbname) {
        super(context, ALL_DB_NAME, null, DATABASE_VERSION);
    }


    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(ModelDatabase.CREATE_TABLE);

        Log.e("TAGG===", "OnCreate");


    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + ModelDatabase.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public long insertNote(QuoteModel quoteModel) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them

        values.put(ModelDatabase.COLUMN_ID, quoteModel.getId());
        values.put(ModelDatabase.COLUMN_NOTE, quoteModel.getQuote());
        values.put(ModelDatabase.COLUMN_TIMESTAMP, quoteModel.getTimestamp());
        values.put(ModelDatabase.COLUMN_BOOKMARK, quoteModel.getBookmark());
        values.put(ModelDatabase.COLUMN_CATEGORY, quoteModel.getCategoryName());

        // insert row
        long id = db.insert(ModelDatabase.TABLE_NAME, null, values);

        // close db connection
        db.close();
        // return newly inserted row id
        return id;
    }

    public ModelDatabase getNote(String note1) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ModelDatabase.TABLE_NAME,
                new String[]{ModelDatabase.COLUMN_ID, ModelDatabase.COLUMN_NOTE, ModelDatabase.COLUMN_TIMESTAMP, ModelDatabase.COLUMN_BOOKMARK, ModelDatabase.COLUMN_CATEGORY},
                ModelDatabase.COLUMN_NOTE + "=?",
                new String[]{note1}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        ModelDatabase note = new ModelDatabase(
                cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)),
                cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY))
        );

        // close the db connection
        cursor.close();

        return note;
    }

    public List<ModelDatabase> getSearchedNotes(String keyword) {
        List<ModelDatabase> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + ModelDatabase.TABLE_NAME ;


//        String selectQuery = "SELECT * FROM " + ModelDatabase.TABLE_NAME + " WHERE  " +
//                ModelDatabase.COLUMN_NOTE + " LIKE " + "'%" + keyword + "%'";


        Log.e("TAG===", "QUERY : " + selectQuery);


//        select * from bookmarktable where note LIKE "%Why%"

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ModelDatabase note = new ModelDatabase();
                note.setId(cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY)));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        Log.e("TAG===", "NOTE SIZE : " + notes.size());

        // close db connection
        db.close();

        // return notes list
        return notes;
    }


    public List<ModelDatabase> getAllNotes() {
        List<ModelDatabase> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + ModelDatabase.TABLE_NAME + " ORDER BY " +
                ModelDatabase.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ModelDatabase note = new ModelDatabase();
                note.setId(cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY)));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + ModelDatabase.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateNote(ModelDatabase note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ModelDatabase.COLUMN_NOTE, note.getNote());

        // updating row
        return db.update(ModelDatabase.TABLE_NAME, values, ModelDatabase.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(QuoteModel quoteModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ModelDatabase.TABLE_NAME, ModelDatabase.COLUMN_NOTE + " = ?",
                new String[]{String.valueOf(quoteModel.getQuote())});
        db.close();
    }
}
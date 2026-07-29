package com.royal.edunotes._database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CardProgressDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "card_progress.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "progress";
    private static final String COL_DB = "db_name";
    private static final String COL_CARD = "card_id";

    public CardProgressDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + COL_DB + " TEXT NOT NULL, "
                + COL_CARD + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + COL_DB + ", " + COL_CARD + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void markViewed(String dbName, int cardId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DB, dbName);
        cv.put(COL_CARD, cardId);
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public int getViewedCount(String dbName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, new String[]{"COUNT(*)"}, COL_DB + "=?",
                new String[]{dbName}, null, null, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }
}

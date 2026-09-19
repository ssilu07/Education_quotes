package com.royal.edunotes._database;

/**
 * Created by Admin on 18-06-2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    QuoteModel quoteModel;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "bookmark_db";
    private static final String ALL_DB_NAME = "alldata";

    public DatabaseHelper(Context context, QuoteModel quoteModel) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.quoteModel = quoteModel;
        cleanupDuplicates();
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        cleanupDuplicates();
    }

    public DatabaseHelper(Context context, String dbname) {
        super(context, ALL_DB_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ModelDatabase.CREATE_TABLE);
        Log.e("TAGG===", "OnCreate");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ModelDatabase.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Checks if a given quote text is already bookmarked in the database.
     */
    public boolean isBookmarked(String quoteText) {
        if (quoteText == null || quoteText.trim().isEmpty()) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(ModelDatabase.TABLE_NAME,
                    new String[]{ModelDatabase.COLUMN_ID},
                    "TRIM(" + ModelDatabase.COLUMN_NOTE + ") = ?",
                    new String[]{quoteText.trim()}, null, null, null, "1");
            exists = (cursor != null && cursor.moveToFirst());
        } catch (Exception e) {
            Log.e("DatabaseHelper", "isBookmarked error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    /**
     * Inserts a note into the bookmark table.
     * Prevents inserting duplicates if the note is already bookmarked.
     */
    public long insertNote(QuoteModel quoteModel) {
        if (quoteModel == null || quoteModel.getQuote() == null) return -1;
        String quoteText = quoteModel.getQuote().trim();
        if (quoteText.isEmpty()) return -1;

        if (isBookmarked(quoteText)) {
            return -1; // Prevent duplicate row insertion
        }

        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(ModelDatabase.COLUMN_ID, quoteModel.getId());
            values.put(ModelDatabase.COLUMN_NOTE, quoteText);

            String ts = quoteModel.getTimestamp();
            if (ts == null || ts.trim().isEmpty()) {
                ts = String.valueOf(System.currentTimeMillis());
            }
            values.put(ModelDatabase.COLUMN_TIMESTAMP, ts);
            values.put(ModelDatabase.COLUMN_NOTE_VALUE, quoteModel.getValue() != null ? quoteModel.getValue() : "");
            values.put(ModelDatabase.COLUMN_BOOKMARK, "1");
            values.put(ModelDatabase.COLUMN_CATEGORY, quoteModel.getCategoryName() != null ? quoteModel.getCategoryName() : "");

            id = db.insert(ModelDatabase.TABLE_NAME, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "insertNote error: " + e.getMessage());
        } finally {
            db.close();
        }
        return id;
    }

    /**
     * Deletes any duplicate rows in the bookmark table, keeping only one entry per note text.
     */
    public void cleanupDuplicates() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + ModelDatabase.TABLE_NAME +
                    " WHERE rowid NOT IN (SELECT min(rowid) FROM " + ModelDatabase.TABLE_NAME +
                    " GROUP BY TRIM(" + ModelDatabase.COLUMN_NOTE + "))");
        } catch (Exception ignored) {
        } finally {
            if (db != null) db.close();
        }
    }

    public ModelDatabase getNote(String note1) {
        if (note1 == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(ModelDatabase.TABLE_NAME,
                new String[]{ModelDatabase.COLUMN_ID, ModelDatabase.COLUMN_NOTE, ModelDatabase.COLUMN_NOTE_VALUE,
                        ModelDatabase.COLUMN_TIMESTAMP, ModelDatabase.COLUMN_BOOKMARK, ModelDatabase.COLUMN_CATEGORY},
                "TRIM(" + ModelDatabase.COLUMN_NOTE + ") = ?",
                new String[]{note1.trim()}, null, null, null, "1");

        ModelDatabase note = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note = new ModelDatabase(
                        cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE)),
                        cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE_VALUE)),
                        cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)),
                        cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY))
                );
            }
            cursor.close();
        }
        db.close();
        return note;
    }

    public List<ModelDatabase> getSearchedNotes(String keyword) {
        List<ModelDatabase> notes = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        String selectQuery = "SELECT * FROM " + ModelDatabase.TABLE_NAME + " WHERE " +
                ModelDatabase.COLUMN_NOTE + " LIKE ? ORDER BY " + ModelDatabase.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{"%" + keyword + "%"});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String noteText = cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE));
                if (noteText == null || !seen.add(noteText.trim())) continue;

                ModelDatabase note = new ModelDatabase();
                note.setId(cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)));
                note.setNote(noteText);
                note.setNoteValue(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE_VALUE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)));
                note.setBookmark(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)));
                note.setCategory(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY)));

                notes.add(note);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notes;
    }

    public List<ModelDatabase> getAllNotes() {
        List<ModelDatabase> notes = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        String selectQuery = "SELECT * FROM " + ModelDatabase.TABLE_NAME + " ORDER BY " +
                ModelDatabase.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String noteText = cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE));
                if (noteText == null || !seen.add(noteText.trim())) continue;

                ModelDatabase note = new ModelDatabase();
                note.setId(cursor.getInt(cursor.getColumnIndex(ModelDatabase.COLUMN_ID)));
                note.setNote(noteText);
                note.setNoteValue(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE_VALUE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP)));
                note.setBookmark(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK)));
                note.setCategory(cursor.getString(cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY)));

                notes.add(note);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT COUNT(DISTINCT TRIM(" + ModelDatabase.COLUMN_NOTE + ")) FROM " + ModelDatabase.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    public int updateNote(ModelDatabase note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ModelDatabase.COLUMN_NOTE, note.getNote());
        int updated = db.update(ModelDatabase.TABLE_NAME, values, ModelDatabase.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
        return updated;
    }

    public ArrayList<QuoteModel> getAllBookmarkQuotes() {
        ArrayList<QuoteModel> list = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + ModelDatabase.TABLE_NAME + " ORDER BY " +
                    ModelDatabase.COLUMN_TIMESTAMP + " DESC", null);
            if (cursor != null && cursor.moveToFirst()) {
                int colId = cursor.getColumnIndex(ModelDatabase.COLUMN_ID);
                int colNote = cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE);
                int colVal = cursor.getColumnIndex(ModelDatabase.COLUMN_NOTE_VALUE);
                int colTime = cursor.getColumnIndex(ModelDatabase.COLUMN_TIMESTAMP);
                int colBm = cursor.getColumnIndex(ModelDatabase.COLUMN_BOOKMARK);
                int colCat = cursor.getColumnIndex(ModelDatabase.COLUMN_CATEGORY);

                do {
                    String noteText = colNote != -1 ? cursor.getString(colNote) : null;
                    if (noteText == null || noteText.trim().isEmpty()) continue;
                    String trimmed = noteText.trim();
                    if (!seen.add(trimmed)) {
                        continue; // skip duplicate row
                    }

                    QuoteModel model = new QuoteModel();
                    if (colId != -1) model.setId(cursor.getInt(colId));
                    model.setQuote(noteText);
                    if (colVal != -1) model.setValue(cursor.getString(colVal));
                    if (colTime != -1) model.setTimestamp(cursor.getString(colTime));
                    if (colBm != -1) model.setBookmark(cursor.getString(colBm));
                    if (colCat != -1) model.setCategoryName(cursor.getString(colCat));
                    model.setBookmared(true);
                    list.add(model);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getAllBookmarkQuotes error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }

    public void deleteNote(QuoteModel quoteModel) {
        if (quoteModel == null || quoteModel.getQuote() == null) return;
        deleteNoteByText(quoteModel.getQuote());
    }

    public void deleteNoteByText(String quoteText) {
        if (quoteText == null || quoteText.trim().isEmpty()) return;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(ModelDatabase.TABLE_NAME, "TRIM(" + ModelDatabase.COLUMN_NOTE + ") = ?",
                    new String[]{quoteText.trim()});
        } catch (Exception e) {
            Log.e("DatabaseHelper", "deleteNote error: " + e.getMessage());
        } finally {
            db.close();
        }
    }
}
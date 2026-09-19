package com.royal.edunotes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Dynamically manages saving and restoring reading progress across ALL
 * content categories (Vocabulary, Idioms, English Grammar Rules, Words Often Confused, DSSSB,
 * and any future content added to the app).
 */
public class ReadingProgressManager {

    private static final String PREF_NAME = "reading_resume_progress";
    private static final String KEY_INDEX_PREFIX = "read_index_";
    private static final String KEY_TOTAL_PREFIX = "read_total_";
    private static final String KEY_TIME_PREFIX = "read_time_";

    private final SharedPreferences prefs;

    public ReadingProgressManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Dynamically save the last viewed card index for ANY dbName.
     * Works for all current and future content.
     *
     * @param dbName     The database/topic identifier (e.g. "life_quotes", "grammar_noun", "confused_words_ch1", etc.)
     * @param dataIndex  The 0-based index of the card
     * @param totalCount Total number of cards in this category
     */
    public void savePosition(String dbName, int dataIndex, int totalCount) {
        if (dbName == null || dbName.trim().isEmpty()) return;
        prefs.edit()
                .putInt(KEY_INDEX_PREFIX + dbName, dataIndex)
                .putInt(KEY_TOTAL_PREFIX + dbName, totalCount)
                .putLong(KEY_TIME_PREFIX + dbName, System.currentTimeMillis())
                .apply();
    }

    /**
     * Get the saved card index for ANY dbName.
     * Returns 0 if none saved.
     */
    public int getSavedPosition(String dbName) {
        if (dbName == null) return 0;
        return prefs.getInt(KEY_INDEX_PREFIX + dbName, 0);
    }

    /**
     * Returns the total cards count saved during the last session.
     */
    public int getSavedTotal(String dbName) {
        if (dbName == null) return 0;
        return prefs.getInt(KEY_TOTAL_PREFIX + dbName, 0);
    }

    /**
     * Returns true if there is a saved position for this dbName within a valid range.
     */
    public boolean hasSavedPosition(String dbName, int totalCount) {
        if (dbName == null) return false;
        int pos = getSavedPosition(dbName);
        return pos > 0 && pos < totalCount;
    }

    /**
     * Clear the saved position for a specific dbName (e.g. restart from beginning).
     */
    public void clearPosition(String dbName) {
        if (dbName == null) return;
        prefs.edit()
                .remove(KEY_INDEX_PREFIX + dbName)
                .remove(KEY_TOTAL_PREFIX + dbName)
                .remove(KEY_TIME_PREFIX + dbName)
                .apply();
    }
}

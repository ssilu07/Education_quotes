package com.royal.edunotes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SettingsManager {

    public static final String LANG_HINDI = "hi";
    public static final String LANG_ENGLISH = "en";

    private static final String PREF_NAME = "app_settings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_FONT_SIZE = "font_size_index";
    private static final String KEY_LAST_BOOKMARK_PREFIX = "last_bookmark_note_";
    private static final String KEY_LEARNED_WORDS = "learned_words_set";

    private static final float[] FONT_SIZES = {14f, 17f, 22f};

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyDarkMode(enabled);
    }

    public void applyDarkMode() {
        applyDarkMode(isDarkMode());
    }

    private void applyDarkMode(boolean enabled) {
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public boolean isHindi() {
        return LANG_HINDI.equals(prefs.getString(KEY_LANGUAGE, LANG_ENGLISH));
    }

    public void setLanguage(String lang) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }

    public int getFontSizeIndex() {
        return prefs.getInt(KEY_FONT_SIZE, 1);
    }

    public float getFontSize() {
        return FONT_SIZES[getFontSizeIndex()];
    }

    public void setFontSizeByIndex(int index) {
        if (index >= 0 && index < FONT_SIZES.length) {
            prefs.edit().putInt(KEY_FONT_SIZE, index).apply();
        }
    }

    /** Remembers which bookmarked word the user was last viewing, per screen type (Vocab/Idiom). */
    public String getLastBookmarkNote(String screenType) {
        return prefs.getString(KEY_LAST_BOOKMARK_PREFIX + screenType, null);
    }

    public void setLastBookmarkNote(String screenType, String noteText) {
        prefs.edit().putString(KEY_LAST_BOOKMARK_PREFIX + screenType, noteText).apply();
    }

    /** Tracks which words the user has marked as "learned", keyed by the word's own text. */
    public boolean isWordLearned(String quoteText) {
        return quoteText != null && prefs.getStringSet(KEY_LEARNED_WORDS, Collections.<String>emptySet()).contains(quoteText);
    }

    public void setWordLearned(String quoteText, boolean learned) {
        if (quoteText == null) return;
        Set<String> updated = new HashSet<>(prefs.getStringSet(KEY_LEARNED_WORDS, Collections.<String>emptySet()));
        if (learned) {
            updated.add(quoteText);
        } else {
            updated.remove(quoteText);
        }
        prefs.edit().putStringSet(KEY_LEARNED_WORDS, updated).apply();
    }
}

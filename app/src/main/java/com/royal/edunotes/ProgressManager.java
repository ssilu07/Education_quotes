package com.royal.edunotes;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProgressManager {

    private static final String PREF_NAME = "progress_data";
    private static final String KEY_XP = "total_xp";
    private static final String KEY_WORDS_READ = "words_read";
    private static final String KEY_WORDS_BOOKMARKED = "words_bookmarked";
    private static final String KEY_QUIZZES_TAKEN = "quizzes_taken";
    private static final String KEY_QUIZ_CORRECT = "quiz_correct";
    private static final String KEY_STREAK = "current_streak";
    private static final String KEY_LAST_ACTIVE = "last_active_date";
    private static final String KEY_BEST_STREAK = "best_streak";

    // XP values
    private static final int XP_READ_WORD = 1;
    private static final int XP_BOOKMARK = 5;
    private static final int XP_QUIZ_CORRECT = 10;
    private static final int XP_QUIZ_COMPLETE = 20;
    private static final int XP_DAILY_LOGIN = 15;

    // Badge thresholds
    public static final int BADGE_BOOKWORM = 50;       // 50 words read
    public static final int BADGE_VOCAB_MASTER = 200;   // 200 words read
    public static final int BADGE_STREAK_3 = 3;         // 3 day streak
    public static final int BADGE_STREAK_7 = 7;         // 7 day streak
    public static final int BADGE_STREAK_30 = 30;       // 30 day streak
    public static final int BADGE_QUIZ_10 = 10;         // 10 quizzes
    public static final int BADGE_PERFECT_SCORE = 1;    // 1 perfect quiz

    private final SharedPreferences prefs;

    public ProgressManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkDailyLogin();
    }

    private void checkDailyLogin() {
        String today = getTodayDate();
        String lastActive = prefs.getString(KEY_LAST_ACTIVE, "");

        if (!today.equals(lastActive)) {
            // New day
            if (isYesterday(lastActive)) {
                // Consecutive day - increment streak
                int streak = prefs.getInt(KEY_STREAK, 0) + 1;
                int bestStreak = prefs.getInt(KEY_BEST_STREAK, 0);
                prefs.edit()
                        .putInt(KEY_STREAK, streak)
                        .putInt(KEY_BEST_STREAK, Math.max(streak, bestStreak))
                        .putString(KEY_LAST_ACTIVE, today)
                        .putInt(KEY_XP, getXP() + XP_DAILY_LOGIN)
                        .apply();
            } else if (!lastActive.isEmpty()) {
                // Streak broken
                prefs.edit()
                        .putInt(KEY_STREAK, 1)
                        .putString(KEY_LAST_ACTIVE, today)
                        .putInt(KEY_XP, getXP() + XP_DAILY_LOGIN)
                        .apply();
            } else {
                // First time
                prefs.edit()
                        .putInt(KEY_STREAK, 1)
                        .putString(KEY_LAST_ACTIVE, today)
                        .putInt(KEY_XP, XP_DAILY_LOGIN)
                        .apply();
            }
        }
    }

    public void onWordRead() {
        prefs.edit()
                .putInt(KEY_WORDS_READ, getWordsRead() + 1)
                .putInt(KEY_XP, getXP() + XP_READ_WORD)
                .apply();
    }

    public void onWordBookmarked() {
        prefs.edit()
                .putInt(KEY_WORDS_BOOKMARKED, getWordsBookmarked() + 1)
                .putInt(KEY_XP, getXP() + XP_BOOKMARK)
                .apply();
    }

    public void onQuizCompleted(int correct, int total) {
        int xpEarned = XP_QUIZ_COMPLETE + (correct * XP_QUIZ_CORRECT);
        prefs.edit()
                .putInt(KEY_QUIZZES_TAKEN, getQuizzesTaken() + 1)
                .putInt(KEY_QUIZ_CORRECT, getQuizCorrect() + correct)
                .putInt(KEY_XP, getXP() + xpEarned)
                .apply();
    }

    public int getXP() { return prefs.getInt(KEY_XP, 0); }
    public int getWordsRead() { return prefs.getInt(KEY_WORDS_READ, 0); }
    public int getWordsBookmarked() { return prefs.getInt(KEY_WORDS_BOOKMARKED, 0); }
    public int getQuizzesTaken() { return prefs.getInt(KEY_QUIZZES_TAKEN, 0); }
    public int getQuizCorrect() { return prefs.getInt(KEY_QUIZ_CORRECT, 0); }
    public int getStreak() { return prefs.getInt(KEY_STREAK, 0); }
    public int getBestStreak() { return prefs.getInt(KEY_BEST_STREAK, 0); }

    public int getLevel() {
        // Every 100 XP = 1 level
        return (getXP() / 100) + 1;
    }

    public int getXPForNextLevel() {
        return 100 - (getXP() % 100);
    }

    public String[] getEarnedBadges() {
        java.util.ArrayList<String> badges = new java.util.ArrayList<>();

        if (getWordsRead() >= BADGE_BOOKWORM) badges.add("Bookworm (50 words read)");
        if (getWordsRead() >= BADGE_VOCAB_MASTER) badges.add("Vocab Master (200 words read)");
        if (getStreak() >= BADGE_STREAK_3) badges.add("On Fire (3-day streak)");
        if (getStreak() >= BADGE_STREAK_7) badges.add("Dedicated (7-day streak)");
        if (getBestStreak() >= BADGE_STREAK_30) badges.add("Champion (30-day streak)");
        if (getQuizzesTaken() >= BADGE_QUIZ_10) badges.add("Quiz Pro (10 quizzes)");

        return badges.toArray(new String[0]);
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private boolean isYesterday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            Date yesterday = new Date(System.currentTimeMillis() - 86400000L);
            return sdf.format(date).equals(sdf.format(yesterday));
        } catch (Exception e) {
            return false;
        }
    }
}

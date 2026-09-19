package com.royal.edunotes;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages saving and restoring quiz progress so users can resume
 * quizzes from where they left off if they exit mid-quiz.
 */
public class QuizProgressManager {

    private static final String PREF_NAME = "quiz_resume_progress";
    private static final String KEY_INDEX_PREFIX = "quiz_index_";
    private static final String KEY_SCORE_PREFIX = "quiz_score_";
    private static final String KEY_TOTAL_PREFIX = "quiz_total_";
    private static final String KEY_TIME_PREFIX = "quiz_time_";

    private final SharedPreferences prefs;

    public QuizProgressManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save the user's progress for a specific quiz.
     *
     * @param quizKey            Unique identifier for the quiz (e.g. dbName or grammarTopic)
     * @param nextQuestionIndex  The 0-based index of the question to resume from
     * @param score              Current score accumulated up to this point
     * @param totalQuestions     Total number of questions in this quiz
     */
    public void saveProgress(String quizKey, int nextQuestionIndex, int score, int totalQuestions) {
        if (quizKey == null || quizKey.trim().isEmpty()) return;
        prefs.edit()
                .putInt(KEY_INDEX_PREFIX + quizKey, nextQuestionIndex)
                .putInt(KEY_SCORE_PREFIX + quizKey, score)
                .putInt(KEY_TOTAL_PREFIX + quizKey, totalQuestions)
                .putLong(KEY_TIME_PREFIX + quizKey, System.currentTimeMillis())
                .apply();
    }

    /**
     * Get the saved question index for a specific quiz.
     * Returns 0 if no progress saved.
     */
    public int getSavedIndex(String quizKey) {
        if (quizKey == null) return 0;
        return prefs.getInt(KEY_INDEX_PREFIX + quizKey, 0);
    }

    /**
     * Get the saved score for a specific quiz.
     * Returns 0 if no score saved.
     */
    public int getSavedScore(String quizKey) {
        if (quizKey == null) return 0;
        return prefs.getInt(KEY_SCORE_PREFIX + quizKey, 0);
    }

    /**
     * Returns the total questions count saved during the last session.
     */
    public int getSavedTotal(String quizKey) {
        if (quizKey == null) return 0;
        return prefs.getInt(KEY_TOTAL_PREFIX + quizKey, 0);
    }

    /**
     * Check whether there is a valid in-progress quiz session.
     */
    public boolean hasSavedProgress(String quizKey, int totalQuestions) {
        if (quizKey == null) return false;
        int savedIndex = getSavedIndex(quizKey);
        return savedIndex > 0 && savedIndex < totalQuestions;
    }

    /**
     * Clear the saved progress for a specific quiz (e.g. when completed or restarted).
     */
    public void clearProgress(String quizKey) {
        if (quizKey == null) return;
        prefs.edit()
                .remove(KEY_INDEX_PREFIX + quizKey)
                .remove(KEY_SCORE_PREFIX + quizKey)
                .remove(KEY_TOTAL_PREFIX + quizKey)
                .remove(KEY_TIME_PREFIX + quizKey)
                .apply();
    }
}

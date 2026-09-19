package com.royal.edunotes._activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.TTSHelper;
import com.royal.edunotes.Utility;
import com.royal.edunotes._adapters.QuizListAdapter;
import com.royal.edunotes._database.CardProgressDatabase;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {

    public static class QuizItem {
        public final int id;
        public final String question;
        public final String optA;
        public final String optB;
        public final String optC;
        public final String optD;
        public final String correctAnswer;
        public final String explanation;

        public QuizItem(int id, String question, String optA, String optB, String optC, String optD, String correctAnswer, String explanation) {
            this.id = id;
            this.question = question;
            this.optA = optA;
            this.optB = optB;
            this.optC = optC;
            this.optD = optD;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }
    }

    private TextView tvQuestionCounter, tvScoreBadge, tvQBadge, tvInstruction, tvQuestionText;
    private ProgressBar quizProgressBar;
    private ScrollView scrollQuiz;
    private LinearLayout layoutQuizHeader;
    private RecyclerView rvQuizList;
    private QuizListAdapter quizListAdapter;
    private CardView fabQuizView;
    private boolean isListView = false;
    private View toggleActionView;

    private LinearLayout optCardA, optCardB, optCardC, optCardD;
    private TextView tvBadgeA, tvBadgeB, tvBadgeC, tvBadgeD;
    private TextView tvOptA, tvOptB, tvOptC, tvOptD;
    private ImageView ivIconA, ivIconB, ivIconC, ivIconD;

    private CardView cardExplanation;
    private TextView tvExplanationTitle, tvExplanationText;
    private Button btnNextQuestion;

    private LinearLayout layoutQuizContent, layoutResult;
    private TextView tvResultEmoji, tvResultHeading, tvResultScore, tvResultXP;
    private Button btnReattempt, btnBackToSets;
    private ImageView btnTTS, btnCopy, btnBookmark;

    private final List<QuizItem> quizList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String dbname = "dsssb_articles_set1";
    private String quizTitle = "Articles - Set 1";

    private TTSHelper ttsHelper;
    private ProgressManager progressManager;
    private com.royal.edunotes.QuizProgressManager quizProgressManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        String titleExtra = getIntent().getStringExtra(Utility.TITLE_KEY);
        String dbExtra = getIntent().getStringExtra(Utility.DBNAME_KEY);
        if (titleExtra != null && !titleExtra.isEmpty()) quizTitle = titleExtra;
        if (dbExtra != null && !dbExtra.isEmpty()) dbname = dbExtra;

        Toolbar toolbar = findViewById(R.id.toolbar);
        com.royal.edunotes.WindowInsetsHelper.applyEdgeToEdge(this, toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(quizTitle);
        }

        ttsHelper = new TTSHelper(this);
        progressManager = new ProgressManager(this);
        quizProgressManager = new com.royal.edunotes.QuizProgressManager(this);

        initViews();
        loadQuizFromDatabase();
    }

    private void initViews() {
        tvQuestionCounter = findViewById(R.id.tv_question_counter);
        tvScoreBadge = findViewById(R.id.tv_score_badge);
        quizProgressBar = findViewById(R.id.quiz_progress_bar);
        scrollQuiz = findViewById(R.id.scroll_quiz);
        layoutQuizHeader = findViewById(R.id.layout_quiz_header);
        rvQuizList = findViewById(R.id.rv_quiz_list);
        fabQuizView = findViewById(R.id.fab_quiz_view);

        if (rvQuizList != null) {
            rvQuizList.setLayoutManager(new LinearLayoutManager(this));
            quizListAdapter = new QuizListAdapter(this, quizList, dbname, quizTitle, ttsHelper, position -> {
                currentQuestionIndex = position;
                switchToQuizView();
                showQuestion(currentQuestionIndex);
            });
            rvQuizList.setAdapter(quizListAdapter);
        }

        if (fabQuizView != null) {
            fabQuizView.setOnClickListener(v -> switchToQuizView());
        }

        tvQBadge = findViewById(R.id.tv_q_badge);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvQuestionText = findViewById(R.id.tv_question_text);

        optCardA = findViewById(R.id.opt_card_a);
        optCardB = findViewById(R.id.opt_card_b);
        optCardC = findViewById(R.id.opt_card_c);
        optCardD = findViewById(R.id.opt_card_d);

        tvBadgeA = findViewById(R.id.tv_badge_a);
        tvBadgeB = findViewById(R.id.tv_badge_b);
        tvBadgeC = findViewById(R.id.tv_badge_c);
        tvBadgeD = findViewById(R.id.tv_badge_d);

        tvOptA = findViewById(R.id.tv_opt_a);
        tvOptB = findViewById(R.id.tv_opt_b);
        tvOptC = findViewById(R.id.tv_opt_c);
        tvOptD = findViewById(R.id.tv_opt_d);

        ivIconA = findViewById(R.id.iv_icon_a);
        ivIconB = findViewById(R.id.iv_icon_b);
        ivIconC = findViewById(R.id.iv_icon_c);
        ivIconD = findViewById(R.id.iv_icon_d);

        cardExplanation = findViewById(R.id.card_explanation);
        tvExplanationTitle = findViewById(R.id.tv_explanation_title);
        tvExplanationText = findViewById(R.id.tv_explanation_text);
        btnNextQuestion = findViewById(R.id.btn_next_question);

        layoutQuizContent = findViewById(R.id.layout_quiz_content);
        layoutResult = findViewById(R.id.layout_result);
        tvResultEmoji = findViewById(R.id.tv_result_emoji);
        tvResultHeading = findViewById(R.id.tv_result_heading);
        tvResultScore = findViewById(R.id.tv_result_score);
        tvResultXP = findViewById(R.id.tv_result_xp);
        btnReattempt = findViewById(R.id.btn_reattempt);
        btnBackToSets = findViewById(R.id.btn_back_to_sets);

        btnTTS = findViewById(R.id.btn_tts);
        btnCopy = findViewById(R.id.btn_copy);
        btnBookmark = findViewById(R.id.btn_bookmark);

        optCardA.setOnClickListener(v -> onOptionSelected(0));
        optCardB.setOnClickListener(v -> onOptionSelected(1));
        optCardC.setOnClickListener(v -> onOptionSelected(2));
        optCardD.setOnClickListener(v -> onOptionSelected(3));

        btnNextQuestion.setOnClickListener(v -> nextQuestion());

        btnReattempt.setOnClickListener(v -> {
            if (quizProgressManager != null) {
                quizProgressManager.clearProgress(dbname);
            }
            layoutResult.setVisibility(View.GONE);
            layoutQuizContent.setVisibility(View.VISIBLE);
            score = 0;
            currentQuestionIndex = 0;
            showQuestion(0);
        });

        btnBackToSets.setOnClickListener(v -> finish());

        btnTTS.setOnClickListener(v -> {
            if (currentQuestionIndex < quizList.size()) {
                com.royal.edunotes.QuestionSpanFormatter.FormattedQuestion formatted =
                        com.royal.edunotes.QuestionSpanFormatter.format(this, quizList.get(currentQuestionIndex).question);
                ttsHelper.speak(formatted.plainText);
            }
        });

        btnCopy.setOnClickListener(v -> {
            if (currentQuestionIndex < quizList.size()) {
                QuizItem item = quizList.get(currentQuestionIndex);
                com.royal.edunotes.QuestionSpanFormatter.FormattedQuestion formatted =
                        com.royal.edunotes.QuestionSpanFormatter.format(this, item.question);
                String plainExp = com.royal.edunotes.ExplanationSpanFormatter.toPlainText(item.explanation);
                String copyText = formatted.plainText + "\n\nA) " + item.optA + "\nB) " + item.optB + "\nC) " + item.optC + "\nD) " + item.optD + "\n\nAnswer: " + item.correctAnswer + "\nExplanation:\n" + plainExp;
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("quiz_question", copyText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Question copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        btnBookmark.setOnClickListener(v -> {
            if (currentQuestionIndex < quizList.size()) {
                QuizItem item = quizList.get(currentQuestionIndex);
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                String noteContent = getQuizNoteContent(item);
                boolean isAlready = dbHelper.isBookmarked(noteContent);
                if (isAlready) {
                    dbHelper.deleteNoteByText(noteContent);
                    btnBookmark.setImageResource(R.drawable.star);
                    Toast.makeText(this, "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
                } else {
                    QuoteModel quote = new QuoteModel();
                    quote.setId(item.id);
                    quote.setQuote(noteContent);
                    String plainExp = com.royal.edunotes.ExplanationSpanFormatter.toPlainText(item.explanation);
                    String exp = (!plainExp.isEmpty())
                            ? "Explanation:\n" + plainExp : "Correct Answer: " + item.correctAnswer;
                    quote.setValue(exp);
                    quote.setCategoryName("Quiz: " + (quizTitle != null ? quizTitle : dbname));
                    quote.setBookmark("1");
                    quote.setBookmared(true);
                    quote.setTimestamp(String.valueOf(System.currentTimeMillis()));
                    dbHelper.insertNote(quote);
                    btnBookmark.setImageResource(R.drawable.starfilled);
                    Toast.makeText(this, "Added to Bookmarks", Toast.LENGTH_SHORT).show();
                    if (progressManager != null) progressManager.onWordBookmarked();
                }
            }
        });
    }

    private String getQuizNoteContent(QuizItem item) {
        if (item == null) return "";
        com.royal.edunotes.QuestionSpanFormatter.FormattedQuestion formatted =
                com.royal.edunotes.QuestionSpanFormatter.format(this, item.question);
        return "Quiz Question:\n" + formatted.plainText + "\n\nA) " + item.optA + "\nB) " + item.optB
                + "\nC) " + item.optC + "\nD) " + item.optD + "\n\n✅ Correct Answer: " + item.correctAnswer;
    }

    private void loadQuizFromDatabase() {
        tvQuestionText.setText("Loading questions...");
        setOptionsClickable(false);

        executor.execute(() -> {
            quizList.clear();
            MyDatabase myDb = null;
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                myDb = new MyDatabase(this, dbname, quizTitle);
                db = myDb.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM questions ORDER BY id ASC", null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        String q = cursor.getString(cursor.getColumnIndexOrThrow("question"));
                        String a = cursor.getString(cursor.getColumnIndexOrThrow("option_a"));
                        String b = cursor.getString(cursor.getColumnIndexOrThrow("option_b"));
                        String c = cursor.getString(cursor.getColumnIndexOrThrow("option_c"));
                        String d = cursor.getString(cursor.getColumnIndexOrThrow("option_d"));
                        String ans = cursor.getString(cursor.getColumnIndexOrThrow("correct_answer"));
                        String exp = cursor.getString(cursor.getColumnIndexOrThrow("explanation"));
                        quizList.add(new QuizItem(id, q, a, b, c, d, ans, exp));
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
                if (myDb != null) myDb.close();
            }

            handler.post(() -> {
                if (quizListAdapter != null) {
                    quizListAdapter.setQuizItems(quizList);
                }
                if (!quizList.isEmpty()) {
                    quizProgressBar.setMax(quizList.size());
                    int savedIndex = quizProgressManager != null ? quizProgressManager.getSavedIndex(dbname) : 0;
                    int savedScore = quizProgressManager != null ? quizProgressManager.getSavedScore(dbname) : 0;
                    if (savedIndex > 0 && savedIndex < quizList.size()) {
                        currentQuestionIndex = savedIndex;
                        score = savedScore;
                        showQuestion(currentQuestionIndex);
                        Toast.makeText(this, "Resuming from Question " + (currentQuestionIndex + 1) + " (Score: " + score + ")", Toast.LENGTH_SHORT).show();
                    } else {
                        currentQuestionIndex = 0;
                        score = 0;
                        showQuestion(0);
                    }
                } else {
                    tvQuestionText.setText("No questions available for this set yet.");
                }
            });
        });
    }

    private void showQuestion(int index) {
        if (index >= quizList.size()) {
            showQuizResult();
            return;
        }

        QuizItem item = quizList.get(index);

        tvQuestionCounter.setText("Question " + (index + 1) + " of " + quizList.size());
        tvScoreBadge.setText("Score: " + score);
        quizProgressBar.setProgress(index + 1);

        tvQBadge.setText("Q." + item.id);
        com.royal.edunotes.QuestionSpanFormatter.FormattedQuestion formatted =
                com.royal.edunotes.QuestionSpanFormatter.format(this, item.question);
        if (formatted.instruction != null && !formatted.instruction.isEmpty()) {
            tvInstruction.setVisibility(View.VISIBLE);
            tvInstruction.setText(formatted.instruction);
        } else {
            tvInstruction.setVisibility(View.GONE);
        }
        tvQuestionText.setText(formatted.bodySpan);

        tvOptA.setText(item.optA);
        tvOptB.setText(item.optB);
        tvOptC.setText(item.optC);
        tvOptD.setText(item.optD);

        resetOptionVisuals();
        setOptionsClickable(true);

        cardExplanation.setVisibility(View.GONE);
        btnNextQuestion.setVisibility(View.GONE);
        if (scrollQuiz != null) {
            scrollQuiz.post(() -> scrollQuiz.smoothScrollTo(0, 0));
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean isBm = dbHelper.isBookmarked(getQuizNoteContent(item));
        btnBookmark.setImageResource(isBm ? R.drawable.starfilled : R.drawable.star);
    }

    private void resetOptionVisuals() {
        LinearLayout[] cards = {optCardA, optCardB, optCardC, optCardD};
        TextView[] badges = {tvBadgeA, tvBadgeB, tvBadgeC, tvBadgeD};
        TextView[] texts = {tvOptA, tvOptB, tvOptC, tvOptD};
        ImageView[] icons = {ivIconA, ivIconB, ivIconC, ivIconD};

        for (int i = 0; i < 4; i++) {
            cards[i].setBackgroundResource(R.drawable.bg_quiz_option);
            badges[i].setBackgroundResource(R.drawable.bg_option_badge);
            badges[i].setTextColor(Color.parseColor("#424242"));
            texts[i].setTextColor(Color.parseColor("#222222"));
            icons[i].setVisibility(View.GONE);
        }
    }

    private void setOptionsClickable(boolean clickable) {
        optCardA.setClickable(clickable);
        optCardB.setClickable(clickable);
        optCardC.setClickable(clickable);
        optCardD.setClickable(clickable);
    }

    private void onOptionSelected(int optionIndex) {
        setOptionsClickable(false);
        QuizItem item = quizList.get(currentQuestionIndex);

        String[] options = {item.optA, item.optB, item.optC, item.optD};
        LinearLayout[] cards = {optCardA, optCardB, optCardC, optCardD};
        TextView[] badges = {tvBadgeA, tvBadgeB, tvBadgeC, tvBadgeD};
        TextView[] texts = {tvOptA, tvOptB, tvOptC, tvOptD};
        ImageView[] icons = {ivIconA, ivIconB, ivIconC, ivIconD};

        String selected = options[optionIndex].trim();
        String correct = item.correctAnswer.trim();
        boolean isCorrect = selected.equalsIgnoreCase(correct);

        boolean isDark = false;
        try {
            isDark = new com.royal.edunotes.SettingsManager(this).isDarkMode();
        } catch (Exception ignored) {}

        if (isCorrect) {
            score++;
            tvScoreBadge.setText("Score: " + score);

            cards[optionIndex].setBackgroundResource(R.drawable.bg_quiz_option_correct);
            badges[optionIndex].setBackgroundResource(R.drawable.bg_option_badge_correct);
            badges[optionIndex].setTextColor(Color.WHITE);
            texts[optionIndex].setTextColor(isDark ? Color.parseColor("#4ADE80") : Color.parseColor("#1B5E20"));
            icons[optionIndex].setImageResource(R.drawable.ic_check_circle);
            icons[optionIndex].setVisibility(View.VISIBLE);

            tvExplanationTitle.setText("✅ Correct Answer");
            tvExplanationTitle.setTextColor(isDark ? Color.parseColor("#4ADE80") : Color.parseColor("#2E7D32"));
            cardExplanation.setCardBackgroundColor(isDark ? Color.parseColor("#0F291E") : Color.parseColor("#E8F5E9"));
        } else {
            cards[optionIndex].setBackgroundResource(R.drawable.bg_quiz_option_wrong);
            badges[optionIndex].setBackgroundResource(R.drawable.bg_option_badge_wrong);
            badges[optionIndex].setTextColor(Color.WHITE);
            texts[optionIndex].setTextColor(isDark ? Color.parseColor("#F87171") : Color.parseColor("#B71C1C"));
            icons[optionIndex].setImageResource(R.drawable.ic_close_circle);
            icons[optionIndex].setVisibility(View.VISIBLE);

            // Highlight correct option
            for (int i = 0; i < 4; i++) {
                if (options[i].trim().equalsIgnoreCase(correct)) {
                    cards[i].setBackgroundResource(R.drawable.bg_quiz_option_correct);
                    badges[i].setBackgroundResource(R.drawable.bg_option_badge_correct);
                    badges[i].setTextColor(Color.WHITE);
                    texts[i].setTextColor(isDark ? Color.parseColor("#4ADE80") : Color.parseColor("#1B5E20"));
                    icons[i].setImageResource(R.drawable.ic_check_circle);
                    icons[i].setVisibility(View.VISIBLE);
                }
            }

            tvExplanationTitle.setText("❌ Incorrect. Correct Answer: " + item.correctAnswer);
            tvExplanationTitle.setTextColor(isDark ? Color.parseColor("#F87171") : Color.parseColor("#C62828"));
            cardExplanation.setCardBackgroundColor(isDark ? Color.parseColor("#2D1515") : Color.parseColor("#FFF3E0"));
        }

        tvExplanationText.setText(com.royal.edunotes.ExplanationSpanFormatter.format(this, item.explanation));
        if (isDark) {
            tvExplanationText.setTextColor(Color.parseColor("#E2E8F0"));
        } else {
            tvExplanationText.setTextColor(Color.parseColor("#263238"));
        }
        cardExplanation.setVisibility(View.VISIBLE);
        btnNextQuestion.setVisibility(View.VISIBLE);
        if (scrollQuiz != null) {
            scrollQuiz.post(() -> scrollQuiz.smoothScrollTo(0, cardExplanation.getTop()));
        }

        // Mark this question as viewed in progress db
        CardProgressDatabase progressDb = new CardProgressDatabase(this);
        progressDb.markViewed(dbname, item.id);

        // Save progress pointing to the next question with current score
        if (quizProgressManager != null) {
            quizProgressManager.saveProgress(dbname, currentQuestionIndex + 1, score, quizList.size());
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < quizList.size()) {
            showQuestion(currentQuestionIndex);
            if (quizProgressManager != null) {
                quizProgressManager.saveProgress(dbname, currentQuestionIndex, score, quizList.size());
            }
        } else {
            showQuizResult();
        }
    }

    private void showQuizResult() {
        if (quizProgressManager != null) {
            quizProgressManager.clearProgress(dbname);
        }

        layoutQuizContent.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);

        int total = quizList.size();
        int percent = total > 0 ? (score * 100) / total : 0;
        int xpEarned = 20 + (score * 10);

        tvResultScore.setText("You scored " + score + " out of " + total + " (" + percent + "%)");
        tvResultXP.setText("+" + xpEarned + " XP earned!");

        if (percent == 100) {
            tvResultEmoji.setText("🏆");
            tvResultHeading.setText("Perfect Score! Outstanding!");
            tvResultHeading.setTextColor(Color.parseColor("#FFD700"));
        } else if (percent >= 75) {
            tvResultEmoji.setText("🌟");
            tvResultHeading.setText("Excellent Work! Great Job!");
            tvResultHeading.setTextColor(Color.parseColor("#4CAF50"));
        } else if (percent >= 50) {
            tvResultEmoji.setText("👍");
            tvResultHeading.setText("Good Effort! Keep Learning!");
            tvResultHeading.setTextColor(Color.parseColor("#FF9800"));
        } else {
            tvResultEmoji.setText("💪");
            tvResultHeading.setText("Keep Practicing! You can do it!");
            tvResultHeading.setTextColor(Color.parseColor("#F44336"));
        }

        if (progressManager != null) {
            progressManager.onQuizCompleted(score, total);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (quizProgressManager != null && !quizList.isEmpty() && currentQuestionIndex < quizList.size()) {
            if (layoutResult != null && layoutResult.getVisibility() != View.VISIBLE) {
                int resumeIndex = (cardExplanation != null && cardExplanation.getVisibility() == View.VISIBLE)
                        ? currentQuestionIndex + 1
                        : currentQuestionIndex;
                if (resumeIndex < quizList.size()) {
                    quizProgressManager.saveProgress(dbname, resumeIndex, score, quizList.size());
                } else {
                    quizProgressManager.clearProgress(dbname);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        if (isListView) {
            switchToQuizView();
            return;
        }
        super.onBackPressed();
    }

    private boolean isDsssbQuiz() {
        if (getIntent().getBooleanExtra("IS_DSSSB", false)) {
            return true;
        }
        if (dbname != null && dbname.toLowerCase().contains("dsssb")) {
            return true;
        }
        if (quizTitle != null && quizTitle.toLowerCase().contains("dsssb")) {
            return true;
        }
        return false;
    }

    private void toggleViewMode() {
        if (isListView) {
            switchToQuizView();
        } else {
            switchToListView();
        }
    }

    private void switchToListView() {
        isListView = true;
        if (scrollQuiz != null) scrollQuiz.setVisibility(View.GONE);
        if (layoutQuizHeader != null) layoutQuizHeader.setVisibility(View.GONE);
        if (rvQuizList != null) {
            rvQuizList.setVisibility(View.VISIBLE);
            if (currentQuestionIndex >= 0 && currentQuestionIndex < quizList.size()) {
                rvQuizList.scrollToPosition(currentQuestionIndex);
            }
        }
        if (fabQuizView != null) fabQuizView.setVisibility(View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(quizList.size() + " Questions (List)");
        }
        updateToggleMenuUI();
        supportInvalidateOptionsMenu();
        Toast.makeText(this, "List View: All questions in scrollable list", Toast.LENGTH_SHORT).show();
    }

    private void switchToQuizView() {
        isListView = false;
        if (rvQuizList != null) rvQuizList.setVisibility(View.GONE);
        if (fabQuizView != null) fabQuizView.setVisibility(View.GONE);
        if (layoutQuizHeader != null) layoutQuizHeader.setVisibility(View.VISIBLE);
        if (scrollQuiz != null) scrollQuiz.setVisibility(View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
        updateToggleMenuUI();
        supportInvalidateOptionsMenu();

        if (!quizList.isEmpty() && currentQuestionIndex < quizList.size()) {
            showQuestion(currentQuestionIndex);
        }
    }

    private void updateToggleMenuUI() {
        if (toggleActionView != null) {
            ImageView ivIcon = toggleActionView.findViewById(R.id.iv_mode_icon);
            TextView tvText = toggleActionView.findViewById(R.id.tv_mode_text);
            if (ivIcon != null && tvText != null) {
                if (isListView) {
                    ivIcon.setImageResource(R.drawable.ic_cards_mode);
                    tvText.setText("Quiz");
                } else {
                    ivIcon.setImageResource(R.drawable.ic_notes_mode);
                    tvText.setText("List");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quiz, menu);

        MenuItem toggleItem = menu.findItem(R.id.action_toggle_view);
        MenuItem restartItem = menu.findItem(R.id.action_restart_quiz);

        if (toggleItem != null) {
            if (isDsssbQuiz()) {
                toggleItem.setVisible(true);
                toggleActionView = toggleItem.getActionView();
                if (toggleActionView != null) {
                    updateToggleMenuUI();
                    toggleActionView.setOnClickListener(v -> toggleViewMode());
                }
                if (isListView) {
                    toggleItem.setTitle("Quiz View");
                    toggleItem.setIcon(R.drawable.ic_cards_mode);
                } else {
                    toggleItem.setTitle("List View");
                    toggleItem.setIcon(R.drawable.ic_notes_mode);
                }
            } else {
                toggleItem.setVisible(false);
            }
        }

        if (restartItem != null) {
            restartItem.setVisible(!isListView);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_toggle_view) {
            toggleViewMode();
            return true;
        } else if (item.getItemId() == R.id.action_restart_quiz) {
            showRestartConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRestartConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Restart Quiz")
                .setMessage("Are you sure you want to restart this quiz from Question 1?")
                .setPositiveButton("Restart", (dialog, which) -> {
                    if (quizProgressManager != null) {
                        quizProgressManager.clearProgress(dbname);
                    }
                    score = 0;
                    currentQuestionIndex = 0;
                    if (layoutResult != null && layoutResult.getVisibility() == View.VISIBLE) {
                        layoutResult.setVisibility(View.GONE);
                        layoutQuizContent.setVisibility(View.VISIBLE);
                    }
                    if (!quizList.isEmpty()) {
                        showQuestion(0);
                        Toast.makeText(this, "Quiz restarted from Question 1", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

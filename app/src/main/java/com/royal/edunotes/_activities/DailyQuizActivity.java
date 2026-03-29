package com.royal.edunotes._activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.royal.edunotes.ProgressManager;
import com.royal.edunotes.R;
import com.royal.edunotes.Utility;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyQuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvQuestionNum, tvScore, tvStreak;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private Button btnNext;
    private ProgressBar progressBar;
    private LinearLayout optionsLayout, resultLayout;
    private TextView tvResultTitle, tvResultScore, tvResultXP, tvResultBadges;
    private Button btnPlayAgain, btnGoHome;
    private CardView quizCard;

    private ArrayList<QuizItem> quizItems = new ArrayList<>();
    private int currentQuestion = 0;
    private int score = 0;
    private static final int TOTAL_QUESTIONS = 5;
    private ProgressManager progressManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quiz);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Daily Vocab Quiz");
        }

        progressManager = new ProgressManager(this);

        initViews();
        loadQuiz();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tv_question);
        tvQuestionNum = findViewById(R.id.tv_question_num);
        tvScore = findViewById(R.id.tv_score);
        tvStreak = findViewById(R.id.tv_streak);
        btnOption1 = findViewById(R.id.btn_option1);
        btnOption2 = findViewById(R.id.btn_option2);
        btnOption3 = findViewById(R.id.btn_option3);
        btnOption4 = findViewById(R.id.btn_option4);
        btnNext = findViewById(R.id.btn_next);
        progressBar = findViewById(R.id.quiz_progress);
        optionsLayout = findViewById(R.id.options_layout);
        resultLayout = findViewById(R.id.result_layout);
        tvResultTitle = findViewById(R.id.tv_result_title);
        tvResultScore = findViewById(R.id.tv_result_score);
        tvResultXP = findViewById(R.id.tv_result_xp);
        tvResultBadges = findViewById(R.id.tv_result_badges);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnGoHome = findViewById(R.id.btn_go_home);
        quizCard = findViewById(R.id.quiz_card);

        tvStreak.setText("Streak: " + progressManager.getStreak() + " days");

        btnNext.setOnClickListener(v -> nextQuestion());
        btnPlayAgain.setOnClickListener(v -> {
            resultLayout.setVisibility(View.GONE);
            quizCard.setVisibility(View.VISIBLE);
            currentQuestion = 0;
            score = 0;
            loadQuiz();
        });
        btnGoHome.setOnClickListener(v -> finish());

        View.OnClickListener optionClick = v -> {
            Button clicked = (Button) v;
            checkAnswer(clicked);
        };
        btnOption1.setOnClickListener(optionClick);
        btnOption2.setOnClickListener(optionClick);
        btnOption3.setOnClickListener(optionClick);
        btnOption4.setOnClickListener(optionClick);
    }

    private void loadQuiz() {
        tvQuestion.setText("Loading quiz...");
        setOptionsEnabled(false);

        executor.execute(() -> {
            ArrayList<QuoteModel> allQuotes = new ArrayList<>();

            String[] dbdata;
            if (Utility.ScreenCheck.equals("Idiom")) {
                dbdata = getResources().getStringArray(R.array.myidiomdb);
            } else {
                dbdata = getResources().getStringArray(R.array.mydb);
            }

            for (String db : dbdata) {
                try {
                    MyDatabase myDb = new MyDatabase(DailyQuizActivity.this, db, db);
                    allQuotes.addAll(myDb.getPoses());
                } catch (Exception ignored) {}
            }

            Collections.shuffle(allQuotes);

            // Generate quiz items
            quizItems.clear();
            Random random = new Random();

            int count = Math.min(TOTAL_QUESTIONS, allQuotes.size() / 4);
            for (int i = 0; i < count; i++) {
                QuoteModel correct = allQuotes.get(i);
                String question = correct.getQuote();

                // Get 3 wrong answers
                ArrayList<String> options = new ArrayList<>();
                options.add(getShortAnswer(question));

                int attempts = 0;
                while (options.size() < 4 && attempts < 50) {
                    int randIdx = random.nextInt(allQuotes.size());
                    String wrongAnswer = getShortAnswer(allQuotes.get(randIdx).getQuote());
                    if (!options.contains(wrongAnswer)) {
                        options.add(wrongAnswer);
                    }
                    attempts++;
                }

                // Pad if needed
                while (options.size() < 4) {
                    options.add("Option " + options.size());
                }

                String correctAnswer = options.get(0);
                Collections.shuffle(options);

                quizItems.add(new QuizItem(
                        "Which is the correct vocab?",
                        question,
                        options,
                        correctAnswer
                ));
            }

            handler.post(() -> {
                if (!quizItems.isEmpty()) {
                    showQuestion();
                } else {
                    tvQuestion.setText("Not enough words for quiz. Read more vocab first!");
                }
            });
        });
    }

    private String getShortAnswer(String text) {
        if (text == null) return "";
        // Get first 50 chars or first sentence
        String result = text.length() > 60 ? text.substring(0, 57) + "..." : text;
        return result;
    }

    private void showQuestion() {
        if (currentQuestion >= quizItems.size()) {
            showResult();
            return;
        }

        QuizItem item = quizItems.get(currentQuestion);

        tvQuestionNum.setText("Question " + (currentQuestion + 1) + "/" + quizItems.size());
        tvScore.setText("Score: " + score);
        progressBar.setMax(quizItems.size());
        progressBar.setProgress(currentQuestion);

        tvQuestion.setText(item.hint);

        btnOption1.setText(item.options.get(0));
        btnOption2.setText(item.options.get(1));
        btnOption3.setText(item.options.get(2));
        btnOption4.setText(item.options.get(3));

        // Reset button colors
        resetButtonColors();
        setOptionsEnabled(true);
        btnNext.setVisibility(View.GONE);
    }

    private void checkAnswer(Button selected) {
        setOptionsEnabled(false);
        QuizItem item = quizItems.get(currentQuestion);
        String selectedAnswer = selected.getText().toString();

        if (selectedAnswer.equals(item.correctAnswer)) {
            selected.setBackgroundColor(Color.parseColor("#4CAF50"));
            selected.setTextColor(Color.WHITE);
            score++;
            tvScore.setText("Score: " + score);
        } else {
            selected.setBackgroundColor(Color.parseColor("#F44336"));
            selected.setTextColor(Color.WHITE);

            // Highlight correct answer
            highlightCorrect(item.correctAnswer);
        }

        btnNext.setVisibility(View.VISIBLE);
    }

    private void highlightCorrect(String correctAnswer) {
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (Button btn : buttons) {
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                btn.setTextColor(Color.WHITE);
            }
        }
    }

    private void nextQuestion() {
        currentQuestion++;
        if (currentQuestion >= quizItems.size()) {
            showResult();
        } else {
            showQuestion();
        }
    }

    private void showResult() {
        quizCard.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);

        int total = quizItems.size();
        int xpEarned = 20 + (score * 10);

        progressManager.onQuizCompleted(score, total);

        if (score == total && total > 0) {
            tvResultTitle.setText("PERFECT SCORE!");
            tvResultTitle.setTextColor(Color.parseColor("#FFD700"));
        } else if (score >= total * 0.7) {
            tvResultTitle.setText("Great Job!");
            tvResultTitle.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvResultTitle.setText("Keep Learning!");
            tvResultTitle.setTextColor(Color.parseColor("#FF9800"));
        }

        tvResultScore.setText(score + " / " + total + " correct");
        tvResultXP.setText("+" + xpEarned + " XP earned! (Level " + progressManager.getLevel() + ")");

        String[] badges = progressManager.getEarnedBadges();
        if (badges.length > 0) {
            StringBuilder sb = new StringBuilder("Badges: ");
            for (String badge : badges) {
                sb.append("\n").append(badge);
            }
            tvResultBadges.setText(sb.toString());
            tvResultBadges.setVisibility(View.VISIBLE);
        } else {
            tvResultBadges.setVisibility(View.GONE);
        }
    }

    private void resetButtonColors() {
        Button[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (Button btn : buttons) {
            btn.setBackgroundColor(Color.parseColor("#E0E0E0"));
            btn.setTextColor(Color.parseColor("#333333"));
        }
    }

    private void setOptionsEnabled(boolean enabled) {
        btnOption1.setEnabled(enabled);
        btnOption2.setEnabled(enabled);
        btnOption3.setEnabled(enabled);
        btnOption4.setEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    static class QuizItem {
        String title;
        String hint;
        ArrayList<String> options;
        String correctAnswer;

        QuizItem(String title, String hint, ArrayList<String> options, String correctAnswer) {
            this.title = title;
            this.hint = hint;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }
}

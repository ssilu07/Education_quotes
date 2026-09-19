package com.royal.edunotes._activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GrammarQuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvScore, tvStreak;
    private TextView btnOption1, btnOption2, btnOption3, btnOption4;
    private Button btnNext;
    private ProgressBar progressBar;
    private LinearLayout optionsLayout, resultLayout;
    private TextView tvResultTitle, tvResultScore, tvResultXP, tvResultBadges;
    private TextView tvExplanation, tvExamTag;
    private Button btnPlayAgain, btnGoHome;
    private CardView quizCard, cvExplanation;
    private android.widget.ImageView btnShare, btnCopy, btnBookmark;

    private ArrayList<QuizItem> quizItems = new ArrayList<>();
    private int currentQuestion = 0;
    private int score = 0;
    private ProgressManager progressManager;
    private com.royal.edunotes.QuizProgressManager quizProgressManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String grammarTopic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_quiz);

        grammarTopic = getIntent().getStringExtra("GRAMMAR_TOPIC");
        if (grammarTopic == null) grammarTopic = "grammar_sva";

        Toolbar toolbar = findViewById(R.id.toolbar);
        com.royal.edunotes.WindowInsetsHelper.applyEdgeToEdge(this, toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Grammar Quiz");
        }

        progressManager = new ProgressManager(this);
        quizProgressManager = new com.royal.edunotes.QuizProgressManager(this);

        initViews();
        loadQuiz();
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tv_question);
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
        tvExplanation = findViewById(R.id.tv_explanation);
        tvExamTag = findViewById(R.id.tv_exam_tag);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnGoHome = findViewById(R.id.btn_go_home);
        quizCard = findViewById(R.id.quiz_card);
        cvExplanation = findViewById(R.id.cv_explanation);
        
        btnShare = findViewById(R.id.btn_share);
        btnCopy = findViewById(R.id.btn_copy);
        btnBookmark = findViewById(R.id.btn_bookmark);

        btnNext.setOnClickListener(v -> nextQuestion());
        btnPlayAgain.setOnClickListener(v -> {
            if (quizProgressManager != null) {
                quizProgressManager.clearProgress(grammarTopic);
            }
            resultLayout.setVisibility(View.GONE);
            quizCard.setVisibility(View.VISIBLE);
            currentQuestion = 0;
            score = 0;
            loadQuiz();
        });
        btnGoHome.setOnClickListener(v -> finish());

        View.OnClickListener optionClick = v -> {
            TextView clicked = (TextView) v;
            checkAnswer(clicked);
        };
        btnOption1.setOnClickListener(optionClick);
        btnOption2.setOnClickListener(optionClick);
        btnOption3.setOnClickListener(optionClick);
        btnOption4.setOnClickListener(optionClick);
        
        btnShare.setOnClickListener(v -> {
            if (currentQuestion < quizItems.size()) {
                QuizItem item = quizItems.get(currentQuestion);
                String shareText = item.question + "\n\nOptions:\nA) " + item.options.get(0) + "\nB) " + item.options.get(1) + "\nC) " + item.options.get(2) + "\nD) " + item.options.get(3) + "\n\nShared via EduNotes App";
                com.royal.edunotes.ShareUtils.shareViewAsImage(this, quizCard, shareText);
            }
        });

        btnCopy.setOnClickListener(v -> {
            if (currentQuestion < quizItems.size()) {
                QuizItem item = quizItems.get(currentQuestion);
                String copyText = item.question + "\nOptions: \n" + item.options.get(0) + " | " + item.options.get(1) + " | " + item.options.get(2) + " | " + item.options.get(3);
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("quiz", copyText);
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(this, "Question Copied", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnBookmark.setOnClickListener(v -> {
            if (currentQuestion < quizItems.size()) {
                QuizItem item = quizItems.get(currentQuestion);
                com.royal.edunotes._database.DatabaseHelper dbHelper = new com.royal.edunotes._database.DatabaseHelper(this);
                String noteContent = getGrammarQuizNote(item);
                boolean isAlready = dbHelper.isBookmarked(noteContent);
                if (isAlready) {
                    dbHelper.deleteNoteByText(noteContent);
                    btnBookmark.setImageResource(R.drawable.star);
                    android.widget.Toast.makeText(this, "Removed from Bookmarks", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    String explanationText = (item.explanation != null && !item.explanation.trim().isEmpty())
                            ? "Explanation:\n" + item.explanation.trim() : "Correct Answer: " + item.correctAnswer;
                    com.royal.edunotes._models.QuoteModel mockModel = new com.royal.edunotes._models.QuoteModel();
                    mockModel.setCategoryName("Quiz: Grammar - " + grammarTopic);
                    mockModel.setQuote(noteContent);
                    mockModel.setValue(explanationText);
                    mockModel.setBookmark("1");
                    mockModel.setBookmared(true);
                    mockModel.setTimestamp(String.valueOf(System.currentTimeMillis()));
                    dbHelper.insertNote(mockModel);
                    btnBookmark.setImageResource(R.drawable.starfilled);
                    android.widget.Toast.makeText(this, "Added to Bookmarks", android.widget.Toast.LENGTH_SHORT).show();
                    if (progressManager != null) progressManager.onWordBookmarked();
                }
            }
        });
    }

    private String getGrammarQuizNote(QuizItem item) {
        if (item == null) return "";
        return "[Grammar Quiz: " + grammarTopic.replace("grammar_", "").replace("_", " ") + "]\n"
                + item.question + "\n\n✅ Correct Answer: " + item.correctAnswer;
    }

    private void loadQuiz() {
        tvQuestion.setText("Loading quiz...");
        setOptionsEnabled(false);
        tvExplanation.setVisibility(View.GONE);
        tvExamTag.setVisibility(View.GONE);

        executor.execute(() -> {
            quizItems.clear();
            File dbFile = new File(getFilesDir(), "grammar_quiz_v3.db");
            try {
                if (!dbFile.exists()) {
                    InputStream is = getAssets().open("databases/grammar_quiz.db");
                    OutputStream os = new FileOutputStream(dbFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    os.flush();
                    os.close();
                    is.close();
                }

                SQLiteDatabase db = null;
                Cursor cursor = null;
                try {
                    db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                    cursor = db.rawQuery("SELECT * FROM questions WHERE topic = ? ORDER BY id ASC", new String[]{grammarTopic});
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            int qIdx = cursor.getColumnIndex("question");
                            int aIdx = cursor.getColumnIndex("option_a");
                            int bIdx = cursor.getColumnIndex("option_b");
                            int cIdx = cursor.getColumnIndex("option_c");
                            int dIdx = cursor.getColumnIndex("option_d");
                            int ansIdx = cursor.getColumnIndex("correct_answer");
                            int expIdx = cursor.getColumnIndex("explanation");
                            int exmIdx = cursor.getColumnIndex("exam_tag");

                            String question = cursor.getString(qIdx);
                            String optA = cursor.getString(aIdx);
                            String optB = cursor.getString(bIdx);
                            String optC = cursor.getString(cIdx);
                            String optD = cursor.getString(dIdx);
                            String correctOpt = cursor.getString(ansIdx);
                            String exp = cursor.getString(expIdx);
                            String exam = cursor.getString(exmIdx);
                            
                            String correctText = optA;
                            if(correctOpt.equalsIgnoreCase("B")) correctText = optB;
                            else if(correctOpt.equalsIgnoreCase("C")) correctText = optC;
                            else if(correctOpt.equalsIgnoreCase("D")) correctText = optD;

                            ArrayList<String> options = new ArrayList<>();
                            options.add(optA);
                            options.add(optB);
                            options.add(optC);
                            options.add(optD);
                            Collections.shuffle(options);

                            quizItems.add(new QuizItem(question, options, correctText, exp, exam));
                        } while (cursor.moveToNext());
                    }
                } finally {
                    if (cursor != null) cursor.close();
                    if (db != null) db.close();
                }
                
                // Keep only top 50 questions
                if(quizItems.size() > 50) {
                    quizItems = new ArrayList<>(quizItems.subList(0, 50));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (!quizItems.isEmpty()) {
                    int savedIndex = quizProgressManager != null ? quizProgressManager.getSavedIndex(grammarTopic) : 0;
                    int savedScore = quizProgressManager != null ? quizProgressManager.getSavedScore(grammarTopic) : 0;
                    if (savedIndex > 0 && savedIndex < quizItems.size()) {
                        currentQuestion = savedIndex;
                        score = savedScore;
                        showQuestion();
                        android.widget.Toast.makeText(this, "Resuming from Question " + (currentQuestion + 1) + " (Score: " + score + ")", android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        currentQuestion = 0;
                        score = 0;
                        showQuestion();
                    }
                } else {
                    tvQuestion.setText("No questions available for this topic yet.");
                }
            });
        });
    }

    private void showQuestion() {
        if (currentQuestion >= quizItems.size()) {
            showResult();
            return;
        }

        QuizItem item = quizItems.get(currentQuestion);

        tvStreak.setText("Question " + (currentQuestion + 1) + "/" + quizItems.size());
        tvScore.setText("Score: " + score);
        progressBar.setMax(quizItems.size());
        progressBar.setProgress(currentQuestion);

        tvQuestion.setText(item.question);

        btnOption1.setText(item.options.get(0));
        btnOption2.setText(item.options.get(1));
        btnOption3.setText(item.options.get(2));
        btnOption4.setText(item.options.get(3));

        resetButtonColors();
        setOptionsEnabled(true);
        btnNext.setVisibility(View.GONE);
        cvExplanation.setVisibility(View.GONE);
        tvExplanation.setVisibility(View.GONE);
        tvExamTag.setVisibility(View.GONE);
        
        com.royal.edunotes._database.DatabaseHelper dbHelper = new com.royal.edunotes._database.DatabaseHelper(this);
        boolean isBm = dbHelper.isBookmarked(getGrammarQuizNote(item));
        btnBookmark.setImageResource(isBm ? R.drawable.starfilled : R.drawable.star);
    }

    private void checkAnswer(TextView selected) {
        setOptionsEnabled(false);
        QuizItem item = quizItems.get(currentQuestion);
        String selectedAnswer = selected.getText().toString();

        if (selectedAnswer.equals(item.correctAnswer)) {
            selected.setBackgroundResource(R.drawable.bg_quiz_option_correct);
            selected.setTextColor(Color.parseColor("#333333"));
            score++;
            tvScore.setText("Score: " + score);
        } else {
            selected.setBackgroundResource(R.drawable.bg_quiz_option_wrong);
            selected.setTextColor(Color.parseColor("#333333"));
            highlightCorrect(item.correctAnswer);
        }

        if(item.examTag != null && !item.examTag.isEmpty()) {
            tvExamTag.setText(item.examTag);
            tvExamTag.setVisibility(View.VISIBLE);
        }
        
        if(item.explanation != null && !item.explanation.isEmpty()) {
            tvExplanation.setText("Explanation: " + item.explanation);
            tvExplanation.setVisibility(View.VISIBLE);
        }
        
        cvExplanation.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);

        if (quizProgressManager != null) {
            quizProgressManager.saveProgress(grammarTopic, currentQuestion + 1, score, quizItems.size());
        }
    }

    private void highlightCorrect(String correctAnswer) {
        TextView[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (TextView btn : buttons) {
            if (btn.getText().toString().equals(correctAnswer)) {
                btn.setBackgroundResource(R.drawable.bg_quiz_option_correct);
                btn.setTextColor(Color.parseColor("#333333"));
            }
        }
    }

    private void nextQuestion() {
        currentQuestion++;
        if (currentQuestion >= quizItems.size()) {
            showResult();
        } else {
            showQuestion();
            if (quizProgressManager != null) {
                quizProgressManager.saveProgress(grammarTopic, currentQuestion, score, quizItems.size());
            }
        }
    }

    private void showResult() {
        if (quizProgressManager != null) {
            quizProgressManager.clearProgress(grammarTopic);
        }

        quizCard.setVisibility(View.GONE);
        tvExplanation.setVisibility(View.GONE);
        tvExamTag.setVisibility(View.GONE);
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
        TextView[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        for (TextView btn : buttons) {
            btn.setBackgroundResource(R.drawable.bg_quiz_option);
            btn.setTextColor(Color.parseColor("#222222"));
        }
    }

    private void setOptionsEnabled(boolean enabled) {
        btnOption1.setEnabled(enabled);
        btnOption2.setEnabled(enabled);
        btnOption3.setEnabled(enabled);
        btnOption4.setEnabled(enabled);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (quizProgressManager != null && !quizItems.isEmpty() && currentQuestion < quizItems.size()) {
            if (resultLayout != null && resultLayout.getVisibility() != View.VISIBLE) {
                int resumeIndex = (cvExplanation != null && cvExplanation.getVisibility() == View.VISIBLE)
                        ? currentQuestion + 1
                        : currentQuestion;
                if (resumeIndex < quizItems.size()) {
                    quizProgressManager.saveProgress(grammarTopic, resumeIndex, score, quizItems.size());
                } else {
                    quizProgressManager.clearProgress(grammarTopic);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
                        quizProgressManager.clearProgress(grammarTopic);
                    }
                    score = 0;
                    currentQuestion = 0;
                    if (resultLayout != null && resultLayout.getVisibility() == View.VISIBLE) {
                        resultLayout.setVisibility(View.GONE);
                        quizCard.setVisibility(View.VISIBLE);
                    }
                    if (!quizItems.isEmpty()) {
                        showQuestion();
                        android.widget.Toast.makeText(this, "Quiz restarted from Question 1", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    static class QuizItem {
        String question;
        ArrayList<String> options;
        String correctAnswer;
        String explanation;
        String examTag;

        QuizItem(String question, ArrayList<String> options, String correctAnswer, String explanation, String examTag) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.examTag = examTag;
        }
    }
}

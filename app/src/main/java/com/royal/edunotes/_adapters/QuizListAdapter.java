package com.royal.edunotes._adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.QuestionSpanFormatter;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes.TTSHelper;
import com.royal.edunotes._activities.QuizActivity;
import com.royal.edunotes._database.CardProgressDatabase;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.List;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuestionViewHolder> {

    public interface QuizListActionListener {
        void onOpenInQuiz(int position);
    }

    public static class ItemState {
        public int selectedOptionIndex = -1; // 0=A, 1=B, 2=C, 3=D, -1=none
        public boolean isAnswerRevealed = false;
    }

    private final Context context;
    private final List<QuizActivity.QuizItem> quizList;
    private final String dbname;
    private final String quizTitle;
    private final QuizListActionListener listener;
    private final TTSHelper ttsHelper;
    private final SettingsManager settingsManager;
    private final SparseArray<ItemState> itemStates = new SparseArray<>();

    public QuizListAdapter(Context context, List<QuizActivity.QuizItem> quizList,
                           String dbname, String quizTitle,
                           TTSHelper ttsHelper, QuizListActionListener listener) {
        this.context = context;
        this.quizList = new ArrayList<>(quizList);
        this.dbname = dbname;
        this.quizTitle = quizTitle;
        this.ttsHelper = ttsHelper;
        this.listener = listener;
        this.settingsManager = new SettingsManager(context);
    }

    public void setQuizItems(List<QuizActivity.QuizItem> newItems) {
        this.quizList.clear();
        if (newItems != null) {
            this.quizList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    private ItemState getItemState(int itemId) {
        ItemState state = itemStates.get(itemId);
        if (state == null) {
            state = new ItemState();
            itemStates.put(itemId, state);
        }
        return state;
    }

    public static int getCorrectOptionIndex(QuizActivity.QuizItem item) {
        if (item == null || item.correctAnswer == null) return -1;
        String correct = item.correctAnswer.trim();
        String[] options = {item.optA, item.optB, item.optC, item.optD};
        String[] letters = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            if (options[i] != null && options[i].trim().equalsIgnoreCase(correct)) {
                return i;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (letters[i].equalsIgnoreCase(correct) ||
                    ("Option " + letters[i]).equalsIgnoreCase(correct)) {
                return i;
            }
        }
        return -1;
    }

    public static String getQuizNoteContent(Context context, QuizActivity.QuizItem item) {
        if (item == null) return "";
        QuestionSpanFormatter.FormattedQuestion formatted =
                QuestionSpanFormatter.format(context, item.question);
        return "Quiz Question:\n" + formatted.plainText + "\n\nA) " + item.optA + "\nB) " + item.optB
                + "\nC) " + item.optC + "\nD) " + item.optD + "\n\n✅ Correct Answer: " + item.correctAnswer;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuizActivity.QuizItem item = quizList.get(position);
        if (item == null) return;

        holder.tvQBadge.setText("Q." + (position + 1));

        QuestionSpanFormatter.FormattedQuestion formatted =
                QuestionSpanFormatter.format(context, item.question);

        if (formatted.instruction != null && !formatted.instruction.isEmpty()) {
            holder.tvInstruction.setVisibility(View.VISIBLE);
            holder.tvInstruction.setText(formatted.instruction);
        } else {
            holder.tvInstruction.setVisibility(View.GONE);
        }

        holder.tvQuestionText.setText(formatted.bodySpan);
        holder.tvOptA.setText(item.optA);
        holder.tvOptB.setText(item.optB);
        holder.tvOptC.setText(item.optC);
        holder.tvOptD.setText(item.optD);

        // Bookmark icon state
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        String noteContent = getQuizNoteContent(context, item);
        boolean isBm = dbHelper.isBookmarked(noteContent);
        holder.btnBookmark.setImageResource(isBm ? R.drawable.starfilled : R.drawable.star);

        // Action icon listeners
        holder.btnTTS.setOnClickListener(v -> {
            if (ttsHelper != null) {
                ttsHelper.speak(formatted.plainText);
            }
        });

        holder.btnCopy.setOnClickListener(v -> {
            String plainExp = com.royal.edunotes.ExplanationSpanFormatter.toPlainText(item.explanation);
            String copyText = formatted.plainText + "\n\nA) " + item.optA + "\nB) " + item.optB
                    + "\nC) " + item.optC + "\nD) " + item.optD + "\n\nAnswer: " + item.correctAnswer
                    + "\nExplanation:\n" + plainExp;
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("quiz_question", copyText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Question copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnBookmark.setOnClickListener(v -> {
            boolean currentlyBm = dbHelper.isBookmarked(noteContent);
            if (currentlyBm) {
                dbHelper.deleteNoteByText(noteContent);
                holder.btnBookmark.setImageResource(R.drawable.star);
                Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
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
                holder.btnBookmark.setImageResource(R.drawable.starfilled);
                Toast.makeText(context, "Added to Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener openQuizListener = v -> {
            if (listener != null) {
                listener.onOpenInQuiz(holder.getAdapterPosition());
            }
        };
        holder.btnOpenQuiz.setOnClickListener(openQuizListener);
        holder.tvPracticeInQuiz.setOnClickListener(openQuizListener);

        // Bind Option Visuals & State
        ItemState state = getItemState(item.id);
        boolean isDark = settingsManager.isDarkMode();
        int correctIndex = getCorrectOptionIndex(item);

        LinearLayout[] optCards = {holder.optCardA, holder.optCardB, holder.optCardC, holder.optCardD};
        TextView[] optBadges = {holder.tvBadgeA, holder.tvBadgeB, holder.tvBadgeC, holder.tvBadgeD};
        TextView[] optTexts = {holder.tvOptA, holder.tvOptB, holder.tvOptC, holder.tvOptD};
        ImageView[] optIcons = {holder.ivIconA, holder.ivIconB, holder.ivIconC, holder.ivIconD};

        if (!state.isAnswerRevealed) {
            // Unrevealed state
            for (int i = 0; i < 4; i++) {
                optCards[i].setBackgroundResource(R.drawable.bg_quiz_option);
                optBadges[i].setBackgroundResource(R.drawable.bg_option_badge);
                optBadges[i].setTextColor(isDark ? Color.parseColor("#E0E0E0") : Color.parseColor("#424242"));
                optTexts[i].setTextColor(isDark ? Color.parseColor("#E0E0E0") : Color.parseColor("#222222"));
                optIcons[i].setVisibility(View.GONE);
                final int optIdx = i;
                optCards[i].setOnClickListener(v -> selectOption(item, holder.getAdapterPosition(), optIdx));
            }
            holder.layoutExplanation.setVisibility(View.GONE);
            holder.btnToggleAnswer.setText("Show Answer");
        } else {
            // Answer is revealed (either by user click on option or by tapping 'Show Answer')
            for (int i = 0; i < 4; i++) {
                // Reset defaults first
                optCards[i].setBackgroundResource(R.drawable.bg_quiz_option);
                optBadges[i].setBackgroundResource(R.drawable.bg_option_badge);
                optBadges[i].setTextColor(isDark ? Color.parseColor("#E0E0E0") : Color.parseColor("#424242"));
                optTexts[i].setTextColor(isDark ? Color.parseColor("#E0E0E0") : Color.parseColor("#222222"));
                optIcons[i].setVisibility(View.GONE);

                // Option clicks still allowed to change selection if desired
                final int optIdx = i;
                optCards[i].setOnClickListener(v -> selectOption(item, holder.getAdapterPosition(), optIdx));
            }

            if (state.selectedOptionIndex >= 0) {
                // User picked an option
                if (state.selectedOptionIndex == correctIndex) {
                    // Correct!
                    styleOptionCorrect(optCards[state.selectedOptionIndex],
                            optBadges[state.selectedOptionIndex],
                            optTexts[state.selectedOptionIndex],
                            optIcons[state.selectedOptionIndex]);
                    holder.tvExplanationTitle.setText("✅ Correct Answer");
                    holder.tvExplanationTitle.setTextColor(Color.parseColor("#2E7D32"));
                } else {
                    // Wrong! Highlight selected in red, correct in green
                    styleOptionWrong(optCards[state.selectedOptionIndex],
                            optBadges[state.selectedOptionIndex],
                            optTexts[state.selectedOptionIndex],
                            optIcons[state.selectedOptionIndex]);

                    if (correctIndex >= 0 && correctIndex < 4) {
                        styleOptionCorrect(optCards[correctIndex],
                                optBadges[correctIndex],
                                optTexts[correctIndex],
                                optIcons[correctIndex]);
                    }
                    holder.tvExplanationTitle.setText("❌ Incorrect. Correct: " + item.correctAnswer);
                    holder.tvExplanationTitle.setTextColor(Color.parseColor("#C62828"));
                }
            } else {
                // User just pressed 'Show Answer' without selecting
                if (correctIndex >= 0 && correctIndex < 4) {
                    styleOptionCorrect(optCards[correctIndex],
                            optBadges[correctIndex],
                            optTexts[correctIndex],
                            optIcons[correctIndex]);
                }
                holder.tvExplanationTitle.setText("💡 Correct Answer: " + item.correctAnswer);
                holder.tvExplanationTitle.setTextColor(Color.parseColor("#2E7D32"));
            }

            // Explanation container
            String expText = (item.explanation != null && !item.explanation.trim().isEmpty())
                    ? item.explanation.trim() : "Correct Answer: " + item.correctAnswer;
            holder.tvExplanationText.setText(com.royal.edunotes.ExplanationSpanFormatter.format(context, expText));
            if (isDark) {
                holder.layoutExplanation.setBackgroundColor(Color.parseColor("#1E293B"));
                holder.tvExplanationText.setTextColor(Color.parseColor("#E2E8F0"));
            } else {
                holder.layoutExplanation.setBackgroundResource(R.drawable.bg_explanation_box);
                holder.tvExplanationText.setTextColor(Color.parseColor("#263238"));
            }
            holder.layoutExplanation.setVisibility(View.VISIBLE);
            holder.btnToggleAnswer.setText("Hide Answer");
        }

        // Toggle answer button click
        holder.btnToggleAnswer.setOnClickListener(v -> {
            ItemState curState = getItemState(item.id);
            curState.isAnswerRevealed = !curState.isAnswerRevealed;
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void selectOption(QuizActivity.QuizItem item, int position, int optionIndex) {
        ItemState state = getItemState(item.id);
        state.selectedOptionIndex = optionIndex;
        state.isAnswerRevealed = true;

        try {
            CardProgressDatabase progressDb = new CardProgressDatabase(context);
            progressDb.markViewed(dbname, item.id);
        } catch (Exception ignored) {}

        notifyItemChanged(position);
    }

    private void styleOptionCorrect(LinearLayout card, TextView badge, TextView text, ImageView icon) {
        card.setBackgroundResource(R.drawable.bg_quiz_option_correct);
        badge.setBackgroundResource(R.drawable.bg_option_badge_correct);
        badge.setTextColor(Color.WHITE);
        text.setTextColor(Color.parseColor("#1B5E20"));
        icon.setImageResource(R.drawable.ic_check_circle);
        icon.setVisibility(View.VISIBLE);
    }

    private void styleOptionWrong(LinearLayout card, TextView badge, TextView text, ImageView icon) {
        card.setBackgroundResource(R.drawable.bg_quiz_option_wrong);
        badge.setBackgroundResource(R.drawable.bg_option_badge_wrong);
        badge.setTextColor(Color.WHITE);
        text.setTextColor(Color.parseColor("#B71C1C"));
        icon.setImageResource(R.drawable.ic_close_circle);
        icon.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvQBadge;
        final ImageView btnTTS, btnCopy, btnBookmark, btnOpenQuiz;
        final TextView tvInstruction, tvQuestionText;
        final LinearLayout optCardA, optCardB, optCardC, optCardD;
        final TextView tvBadgeA, tvBadgeB, tvBadgeC, tvBadgeD;
        final TextView tvOptA, tvOptB, tvOptC, tvOptD;
        final ImageView ivIconA, ivIconB, ivIconC, ivIconD;
        final TextView btnToggleAnswer, tvPracticeInQuiz;
        final LinearLayout layoutExplanation;
        final TextView tvExplanationTitle, tvExplanationText;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQBadge = itemView.findViewById(R.id.tv_q_badge);
            btnTTS = itemView.findViewById(R.id.btn_item_tts);
            btnCopy = itemView.findViewById(R.id.btn_item_copy);
            btnBookmark = itemView.findViewById(R.id.btn_item_bookmark);
            btnOpenQuiz = itemView.findViewById(R.id.btn_item_open_quiz);

            tvInstruction = itemView.findViewById(R.id.tv_item_instruction);
            tvQuestionText = itemView.findViewById(R.id.tv_item_question);

            optCardA = itemView.findViewById(R.id.opt_item_a);
            optCardB = itemView.findViewById(R.id.opt_item_b);
            optCardC = itemView.findViewById(R.id.opt_item_c);
            optCardD = itemView.findViewById(R.id.opt_item_d);

            tvBadgeA = itemView.findViewById(R.id.tv_item_badge_a);
            tvBadgeB = itemView.findViewById(R.id.tv_item_badge_b);
            tvBadgeC = itemView.findViewById(R.id.tv_item_badge_c);
            tvBadgeD = itemView.findViewById(R.id.tv_item_badge_d);

            tvOptA = itemView.findViewById(R.id.tv_item_opt_a);
            tvOptB = itemView.findViewById(R.id.tv_item_opt_b);
            tvOptC = itemView.findViewById(R.id.tv_item_opt_c);
            tvOptD = itemView.findViewById(R.id.tv_item_opt_d);

            ivIconA = itemView.findViewById(R.id.iv_item_icon_a);
            ivIconB = itemView.findViewById(R.id.iv_item_icon_b);
            ivIconC = itemView.findViewById(R.id.iv_item_icon_c);
            ivIconD = itemView.findViewById(R.id.iv_item_icon_d);

            btnToggleAnswer = itemView.findViewById(R.id.btn_toggle_answer);
            tvPracticeInQuiz = itemView.findViewById(R.id.tv_practice_in_quiz);

            layoutExplanation = itemView.findViewById(R.id.layout_item_explanation);
            tvExplanationTitle = itemView.findViewById(R.id.tv_item_explanation_title);
            tvExplanationText = itemView.findViewById(R.id.tv_item_explanation_text);
        }
    }
}

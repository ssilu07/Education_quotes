package com.royal.edunotes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats question strings for Quiz display:
 * 1. Instruction is extracted and shown in neutral/black text.
 * 2. Question body (sentence, conversation, context) is shown in another color (e.g. rich blue).
 * 3. Highlighted target words/phrases are styled with a vibrant yellow highlighter background and bold text.
 */
public class QuestionSpanFormatter {

    public static class FormattedQuestion {
        public final String instruction;
        public final CharSequence bodySpan;
        public final String plainText;

        public FormattedQuestion(String instruction, CharSequence bodySpan, String plainText) {
            this.instruction = instruction;
            this.bodySpan = bodySpan;
            this.plainText = plainText;
        }
    }

    private static final Pattern INSTRUCTION_TAG_PATTERN = Pattern.compile(
            "<instruction>(.*?)</instruction>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern HIGHLIGHT_TAG_PATTERN = Pattern.compile(
            "<highlight>(.*?)</highlight>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern AUTO_INSTRUCTION_PATTERN = Pattern.compile(
            "^([A-Z][^\\.\\?\\:]*?(?:sentence below|given sentence|following sentence|sentence|conversation|options given|options|following paragraph|context|bracketed|quotes|below|given word|meaning of|antonym of|synonym of|given proverb)[\\.\\?\\:])\\s+([A-Z].*)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern PREFIX_INSTRUCTION_PATTERN = Pattern.compile(
            "^(Select|Choose|Find|Identify|Read|In the following|Out of|Which|From the|What is|What does|Rectify|Spot|A sentence|Replace|Substitute)\\b[^\\.\\?\\:]*?[\\.\\?\\:]",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern QUOTE_PATTERN = Pattern.compile("['\"\\u201c\\u2018]([^'\"\\u201d\\u2019]{2,})['\"\\u201d\\u2019]");
    private static final Pattern CAP_PATTERN = Pattern.compile("\\b([A-Z]{3,})\\b");

    public static FormattedQuestion format(Context context, String rawQuestion) {
        if (rawQuestion == null || rawQuestion.trim().isEmpty()) {
            return new FormattedQuestion("", "", "");
        }

        boolean isDarkMode = false;
        if (context != null) {
            try {
                isDarkMode = new SettingsManager(context).isDarkMode();
            } catch (Exception ignored) {
            }
        }

        int bodyColor = isDarkMode ? Color.parseColor("#93C5FD") : Color.parseColor("#1E40AF");
        int highlightBg = isDarkMode ? Color.parseColor("#854D0E") : Color.parseColor("#FEF08A");
        int highlightFg = isDarkMode ? Color.parseColor("#FEF08A") : Color.parseColor("#78350F");

        String instruction = null;
        String body = rawQuestion;

        Matcher instMatcher = INSTRUCTION_TAG_PATTERN.matcher(rawQuestion);
        if (instMatcher.find()) {
            instruction = instMatcher.group(1).trim();
            body = rawQuestion.substring(instMatcher.end()).trim();
        } else {
            Matcher autoMatcher = AUTO_INSTRUCTION_PATTERN.matcher(rawQuestion.trim());
            if (autoMatcher.matches()) {
                instruction = autoMatcher.group(1).trim();
                body = autoMatcher.group(2).trim();
            } else {
                Matcher prefMatcher = PREFIX_INSTRUCTION_PATTERN.matcher(rawQuestion.trim());
                if (prefMatcher.find()) {
                    int end = prefMatcher.end();
                    if (end < rawQuestion.trim().length() - 5) {
                        instruction = rawQuestion.trim().substring(0, end).trim();
                        body = rawQuestion.trim().substring(end).trim();
                    }
                }
            }
        }

        SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
        Matcher hlMatcher = HIGHLIGHT_TAG_PATTERN.matcher(body);
        int lastIndex = 0;
        boolean hasExplicitHighlight = false;

        while (hlMatcher.find()) {
            hasExplicitHighlight = true;
            String textBefore = body.substring(lastIndex, hlMatcher.start());
            if (!textBefore.isEmpty()) {
                int start = spanBuilder.length();
                spanBuilder.append(textBefore);
                spanBuilder.setSpan(new ForegroundColorSpan(bodyColor), start, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            String targetWord = hlMatcher.group(1);
            int startHl = spanBuilder.length();
            spanBuilder.append(targetWord);
            spanBuilder.setSpan(new BackgroundColorSpan(highlightBg), startHl, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanBuilder.setSpan(new ForegroundColorSpan(highlightFg), startHl, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanBuilder.setSpan(new StyleSpan(Typeface.BOLD), startHl, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastIndex = hlMatcher.end();
        }

        if (hasExplicitHighlight) {
            if (lastIndex < body.length()) {
                String remaining = body.substring(lastIndex);
                int start = spanBuilder.length();
                spanBuilder.append(remaining);
                spanBuilder.setSpan(new ForegroundColorSpan(bodyColor), start, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            spanBuilder.append(body);
            spanBuilder.setSpan(new ForegroundColorSpan(bodyColor), 0, spanBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            Matcher qm = QUOTE_PATTERN.matcher(body);
            while (qm.find()) {
                int s = qm.start(1);
                int e = qm.end(1);
                spanBuilder.setSpan(new BackgroundColorSpan(highlightBg), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanBuilder.setSpan(new ForegroundColorSpan(highlightFg), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanBuilder.setSpan(new StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            Matcher cm = CAP_PATTERN.matcher(body);
            while (cm.find()) {
                String word = cm.group(1);
                if (!"DSSSB".equalsIgnoreCase(word) && !"MTS".equalsIgnoreCase(word) && !"PGT".equalsIgnoreCase(word)
                        && !"ANTONYM".equalsIgnoreCase(word) && !"SYNONYM".equalsIgnoreCase(word)) {
                    int s = cm.start(1);
                    int e = cm.end(1);
                    spanBuilder.setSpan(new BackgroundColorSpan(highlightBg), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanBuilder.setSpan(new ForegroundColorSpan(highlightFg), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spanBuilder.setSpan(new StyleSpan(Typeface.BOLD), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        String plain = rawQuestion.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();

        return new FormattedQuestion(instruction, spanBuilder, plain);
    }
}

package com.royal.edunotes;

import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;
import java.util.List;

public class BookmarkHelper {

    public static final String FILTER_ALL = "ALL";
    public static final String FILTER_VOCAB = "VOCAB";
    public static final String FILTER_IDIOM = "IDIOM";
    public static final String FILTER_QUIZ = "QUIZ";
    public static final String FILTER_GRAMMAR = "GRAMMAR";

    /**
     * Determines whether a given bookmark item is Vocab, Idiom, Quiz, or Grammar.
     */
    public static String getItemType(QuoteModel item) {
        if (item == null) return FILTER_VOCAB;

        String cat = item.getCategoryName() != null ? item.getCategoryName().toLowerCase().trim() : "";
        String quote = item.getQuote() != null ? item.getQuote().toLowerCase().trim() : "";

        // 1. Quiz detection
        if (cat.startsWith("quiz") || cat.contains("quiz")
                || cat.contains("spot_error") || cat.contains("spot the error")
                || cat.contains("narration_set") || cat.contains("articles_set")
                || cat.startsWith("dsssb_articles") || cat.startsWith("dsssb_spot")
                || cat.startsWith("dsssb_narration") || cat.startsWith("dsssb_fill_in_the_blanks")
                || cat.startsWith("dsssb_fib") || cat.startsWith("dsssb_tense")
                || cat.startsWith("dsssb_adjective")
                || cat.startsWith("dsssb_voice") || cat.startsWith("dsssb_active")
                || cat.startsWith("dsssb_rearrangement") || cat.startsWith("dsssb_sentence")
                || cat.startsWith("dsssb_ows") || cat.startsWith("dsssb_one_word")
                || cat.startsWith("dsssb_synonyms")
                || cat.startsWith("dsssb_antonyms")
                || cat.startsWith("dsssb_idioms")
                || cat.startsWith("dsssb_spelling")
                || quote.startsWith("quiz question") || quote.startsWith("daily quiz")
                || quote.contains("[answer:") || quote.contains("answer:")
                || quote.contains("[grammar quiz") || quote.contains("correct answer:")
                || quote.contains("choose the correct")) {
            return FILTER_QUIZ;
        }

        // 2. Idiom detection
        if (cat.contains("idiom")) {
            return FILTER_IDIOM;
        }

        // 3. Grammar rule detection
        if (cat.startsWith("grammar_") || cat.contains("grammar") || cat.contains("rules")) {
            return FILTER_GRAMMAR;
        }

        // Default to Vocab
        return FILTER_VOCAB;
    }

    /**
     * Filters the given list based on filterType.
     */
    public static ArrayList<QuoteModel> filterBookmarks(List<QuoteModel> list, String filterType) {
        ArrayList<QuoteModel> filtered = new ArrayList<>();
        if (list == null) return filtered;

        if (FILTER_ALL.equals(filterType)) {
            filtered.addAll(list);
            return filtered;
        }

        for (QuoteModel item : list) {
            if (filterType.equals(getItemType(item))) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * Counts how many items match a given filterType.
     */
    public static int countByType(List<QuoteModel> list, String filterType) {
        if (list == null) return 0;
        if (FILTER_ALL.equals(filterType)) return list.size();

        int count = 0;
        for (QuoteModel item : list) {
            if (filterType.equals(getItemType(item))) {
                count++;
            }
        }
        return count;
    }
}

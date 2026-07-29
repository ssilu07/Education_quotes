package com.royal.edunotes;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataProvider {

    public static class SubCategory {
        public final String title;
        public final String dbName;

        public SubCategory(String title, String dbName) {
            this.title = title;
            this.dbName = dbName;
        }
    }

    public static class Category {
        public final String title;
        public final String screenCheck;
        public final List<SubCategory> subCategories;

        public Category(String title, String screenCheck) {
            this.title = title;
            this.screenCheck = screenCheck;
            this.subCategories = new ArrayList<>();
        }

        public Category addSub(String subTitle, String dbName) {
            subCategories.add(new SubCategory(subTitle, dbName));
            return this;
        }
    }

    public static List<Category> getAllCategories() {
        List<Category> cats = new ArrayList<>();

        cats.add(new Category("Vocabulary (Daily Words)", "Vocab")
                .addSub("Vocab Chapter 1", "life_quotes")
                .addSub("Vocab Chapter 2", "inspirational_quote")
                .addSub("Vocab Chapter 3", "happiness_quotes")
                .addSub("Vocab Chapter 4", "beautiful_quotes")
                .addSub("Vocab Chapter 5", "change_quote")
                .addSub("Vocab Chapter 6", "introvert_quotes")
                .addSub("Vocab Chapter 7", "hope_quotes")
                .addSub("Vocab Chapter 8", "travel_quotes")
                .addSub("Vocab Chapter 9", "trust_quotes")
                .addSub("Vocab Chapter 10", "martin_luther_quotes")
                .addSub("Vocab Chapter 11", "chapter_11")
                .addSub("Vocab Chapter 12", "chapter_12"));

        cats.add(new Category("Idioms & Phrases", "Idiom")
                .addSub("Idiom 1", "life_quotes_idiom")
                .addSub("Idiom 2", "inspirational_quote_idiom")
                .addSub("Idiom 3", "happiness_quotes_idiom")
                .addSub("Idiom 4", "beautiful_quotes_idiom")
                .addSub("Idiom 5", "change_quote_idiom")
                .addSub("Idiom 6", "introvert_quotes_idiom")
                .addSub("Idiom 7", "hope_quotes_idiom")
                .addSub("Idiom 8", "travel_quotes_idiom")
                .addSub("Idiom 9", "trust_quotes_idiom")
                .addSub("Idiom 10", "martin_luther_quotes_idiom")
                .addSub("Idiom 11", "freedom_quotes_idiom")
                .addSub("Idiom 12", "humanity_quotes_idiom")
                .addSub("Idiom 13", "simplicity_quotes_idiom")
                .addSub("Idiom 14", "music_quotes_idiom")
                .addSub("Idiom 15", "attitude_quotes_idiom")
                .addSub("Idiom 16", "zen_quotes_idiom")
                .addSub("Idiom 17", "smile_quotes_idiom")
                .addSub("Idiom 18", "art_quotes_idiom")
                .addSub("Idiom 19", "silence_quotes_idiom")
                .addSub("Idiom 20", "gm_quotes_idiom")
                .addSub("Idiom 21", "rumi_quotes_idiom"));

        cats.add(new Category("English Grammar Rules", "Grammar")
                .addSub("Noun Rules", "grammar_noun")
                .addSub("Pronoun Rules", "grammar_pronoun")
                .addSub("Article Rules", "grammar_article")
                .addSub("Verb Rules", "grammar_verb")
                .addSub("Subject-Verb Agreement", "grammar_sva")
                .addSub("Tense Rules", "grammar_tense")
                .addSub("Passive Voice", "grammar_voice")
                .addSub("Narration", "grammar_narration")
                .addSub("Conditional Sentences", "grammar_conditional")
                .addSub("Verb (Advance)", "grammar_verb_adv")
                .addSub("Adjective", "grammar_adjective")
                .addSub("Conjunction", "grammar_conjunction")
                .addSub("Preposition", "grammar_preposition")
                .addSub("Adverb", "grammar_adverb"));

        cats.add(new Category("Words Often Confused", "Vocab")
                .addSub("Chapter 1", "confused_words_ch1")
                .addSub("Chapter 2", "confused_words_ch2")
                .addSub("Chapter 3", "confused_words_ch3"));

        return cats;
    }
}

package com.royal.edunotes;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataProvider {

    public static class SubCategory {
        public final String title;
        public final String dbName;
        public final String screenCheck;
        public final List<SubCategory> subSets;

        public SubCategory(String title, String dbName) {
            this(title, dbName, null);
        }

        public SubCategory(String title, String dbName, String screenCheck) {
            this.title = title;
            this.dbName = dbName;
            this.screenCheck = screenCheck;
            this.subSets = new ArrayList<>();
        }

        public SubCategory addSet(String setTitle, String setDbName, String setScreenCheck) {
            this.subSets.add(new SubCategory(setTitle, setDbName, setScreenCheck));
            return this;
        }

        public boolean hasSets() {
            return subSets != null && !subSets.isEmpty();
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
            subCategories.add(new SubCategory(subTitle, dbName, this.screenCheck));
            return this;
        }

        public Category addSub(String subTitle, String dbName, String screenCheck) {
            subCategories.add(new SubCategory(subTitle, dbName, screenCheck));
            return this;
        }

        public Category addSub(SubCategory subCategory) {
            subCategories.add(subCategory);
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
                .addSub("Vocab Chapter 12", "chapter_12")
                .addSub("Vocab Chapter 13", "chapter_13"));

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

        cats.add(new Category("DSSSB", "Vocab")
                .addSub(new SubCategory("Synonyms", "dsssb_synonyms", "Quiz")
                        .addSet("Set - 1", "dsssb_synonyms_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_synonyms_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_synonyms_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_synonyms_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_synonyms_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_synonyms_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_synonyms_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_synonyms_set8", "Quiz"))
                .addSub(new SubCategory("Antonyms", "dsssb_antonyms", "Quiz")
                        .addSet("Set - 1", "dsssb_antonyms_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_antonyms_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_antonyms_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_antonyms_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_antonyms_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_antonyms_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_antonyms_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_antonyms_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_antonyms_set9", "Quiz")
                        .addSet("Set - 10", "dsssb_antonyms_set10", "Quiz"))
                .addSub(new SubCategory("Idioms", "dsssb_idioms", "Quiz")
                        .addSet("Set - 1", "dsssb_idioms_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_idioms_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_idioms_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_idioms_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_idioms_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_idioms_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_idioms_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_idioms_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_idioms_set9", "Quiz")
                        .addSet("Set - 10", "dsssb_idioms_set10", "Quiz")
                        .addSet("Set - 11", "dsssb_idioms_set11", "Quiz")
                        .addSet("Set - 12", "dsssb_idioms_set12", "Quiz")
                        .addSet("Set - 13", "dsssb_idioms_set13", "Quiz")
                        .addSet("Set - 14", "dsssb_idioms_set14", "Quiz"))
                .addSub(new SubCategory("One Word Substitution", "dsssb_ows", "Quiz")
                        .addSet("Set - 1", "dsssb_ows_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_ows_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_ows_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_ows_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_ows_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_ows_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_ows_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_ows_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_ows_set9", "Quiz")
                        .addSet("Set - 10", "dsssb_ows_set10", "Quiz"))
                .addSub(new SubCategory("Spelling", "dsssb_spelling", "Quiz")
                        .addSet("Set - 1", "dsssb_spelling_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_spelling_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_spelling_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_spelling_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_spelling_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_spelling_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_spelling_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_spelling_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_spelling_set9", "Quiz"))
                .addSub(new SubCategory("Articles", "dsssb_articles", "Quiz")
                        .addSet("Set - 1", "dsssb_articles_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_articles_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_articles_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_articles_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_articles_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_articles_set6", "Quiz"))
                .addSub(new SubCategory("Fill in the Blanks", "dsssb_fill_in_the_blanks", "Quiz")
                        .addSet("Set - 1", "dsssb_fill_in_the_blanks_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_fill_in_the_blanks_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_fill_in_the_blanks_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_fill_in_the_blanks_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_fill_in_the_blanks_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_fill_in_the_blanks_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_fill_in_the_blanks_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_fill_in_the_blanks_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_fill_in_the_blanks_set9", "Quiz")
                        .addSet("Set - 10", "dsssb_fill_in_the_blanks_set10", "Quiz")
                        .addSet("Set - 11", "dsssb_fill_in_the_blanks_set11", "Quiz")
                        .addSet("Set - 12", "dsssb_fill_in_the_blanks_set12", "Quiz")
                        .addSet("Set - 13", "dsssb_fill_in_the_blanks_set13", "Quiz")
                        .addSet("Set - 14", "dsssb_fill_in_the_blanks_set14", "Quiz")
                        .addSet("Set - 15", "dsssb_fill_in_the_blanks_set15", "Quiz")
                        .addSet("Set - 16", "dsssb_fill_in_the_blanks_set16", "Quiz")
                        .addSet("Set - 17", "dsssb_fill_in_the_blanks_set17", "Quiz")
                        .addSet("Set - 18", "dsssb_fill_in_the_blanks_set18", "Quiz")
                        .addSet("Set - 19", "dsssb_fill_in_the_blanks_set19", "Quiz")
                        .addSet("Set - 20", "dsssb_fill_in_the_blanks_set20", "Quiz")
                        .addSet("Set - 21", "dsssb_fill_in_the_blanks_set21", "Quiz")
                        .addSet("Set - 22", "dsssb_fill_in_the_blanks_set22", "Quiz")
                        .addSet("Set - 23", "dsssb_fill_in_the_blanks_set23", "Quiz")
                        .addSet("Set - 24", "dsssb_fill_in_the_blanks_set24", "Quiz"))
                .addSub(new SubCategory("Spot the Error", "dsssb_spot_the_error", "Quiz")
                        .addSet("Set - 1", "dsssb_spot_error_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_spot_error_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_spot_error_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_spot_error_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_spot_error_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_spot_error_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_spot_error_set7", "Quiz"))
                .addSub(new SubCategory("Tense", "dsssb_tense", "Quiz")
                        .addSet("Set - 1", "dsssb_tense_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_tense_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_tense_set3", "Quiz"))
                .addSub(new SubCategory("Preposition", "dsssb_preposition", "Quiz")
                        .addSet("Set - 1", "dsssb_preposition_set1", "Quiz"))
                .addSub(new SubCategory("Narration", "dsssb_narration", "Quiz")
                        .addSet("Set - 1", "dsssb_narration_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_narration_set2", "Quiz"))
                .addSub(new SubCategory("Active & Passive Voice", "dsssb_active_passive", "Quiz")
                        .addSet("Set - 1", "dsssb_active_passive_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_active_passive_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_active_passive_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_active_passive_set4", "Quiz"))
                .addSub(new SubCategory("Sentence Rearrangement", "dsssb_sentence_rearrangement", "Quiz")
                        .addSet("Set - 1", "dsssb_sentence_rearrangement_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_sentence_rearrangement_set2", "Quiz")
                        .addSet("Set - 3", "dsssb_sentence_rearrangement_set3", "Quiz")
                        .addSet("Set - 4", "dsssb_sentence_rearrangement_set4", "Quiz")
                        .addSet("Set - 5", "dsssb_sentence_rearrangement_set5", "Quiz")
                        .addSet("Set - 6", "dsssb_sentence_rearrangement_set6", "Quiz")
                        .addSet("Set - 7", "dsssb_sentence_rearrangement_set7", "Quiz")
                        .addSet("Set - 8", "dsssb_sentence_rearrangement_set8", "Quiz")
                        .addSet("Set - 9", "dsssb_sentence_rearrangement_set9", "Quiz")
                        .addSet("Set - 10", "dsssb_sentence_rearrangement_set10", "Quiz")
                        .addSet("Set - 11", "dsssb_sentence_rearrangement_set11", "Quiz")
                        .addSet("Set - 12", "dsssb_sentence_rearrangement_set12", "Quiz"))
                .addSub(new SubCategory("Adjective", "dsssb_adjective", "Quiz")
                        .addSet("Set - 1", "dsssb_adjective_set1", "Quiz")
                        .addSet("Set - 2", "dsssb_adjective_set2", "Quiz")));

        return cats;
    }
}

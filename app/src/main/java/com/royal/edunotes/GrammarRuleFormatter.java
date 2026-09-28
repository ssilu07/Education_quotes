package com.royal.edunotes;

import androidx.core.text.HtmlCompat;

/**
 * Utility to detect and format English Grammar rules with rich, vibrant styling
 * across Card View (VerticlePagerAdapter) and List View (NotesListAdapter).
 */
public class GrammarRuleFormatter {

    public static boolean isGrammarRule(String quote, String categoryName) {
        if (categoryName != null && categoryName.toLowerCase().startsWith("grammar_")) {
            return true;
        }
        if (quote == null || quote.trim().isEmpty()) {
            return false;
        }
        String trimmed = quote.trim();
        return trimmed.startsWith("**Rule") || trimmed.startsWith("Rule ") || trimmed.startsWith("Rule:");
    }

    public static CharSequence formatGrammarRule(String text, boolean isDarkMode) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        if (!text.contains("Rule") && !text.contains("❌") && !text.contains("✅")) {
            return text;
        }

        String titleColor = isDarkMode ? "#60A5FA" : "#1565C0";
        String red = isDarkMode ? "#F87171" : "#C62828";
        String green = isDarkMode ? "#4ADE80" : "#1B5E20";
        String normalColor = isDarkMode ? "#E2E8F0" : "#1E293B";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                sb.append("<br>");
                continue;
            }

            // Escape ampersands to avoid malformed HTML entities
            line = line.replace("&", "&amp;");

            if (line.startsWith("**Rule") || line.startsWith("Rule")) {
                String clean = line.replace("**", "");
                sb.append("<font color=\"").append(titleColor).append("\"><b><big>").append(clean).append("</big></b></font><br>");
            } else if (line.contains("❌") || line.toLowerCase().contains("incorrect:")) {
                String clean = line.replace("**", "");
                sb.append("<font color=\"").append(red).append("\"><b>").append(clean).append("</b></font><br>");
            } else if (line.contains("✅") || line.toLowerCase().contains("correct:")) {
                String clean = line.replace("**", "");
                sb.append("<font color=\"").append(green).append("\"><b>").append(clean).append("</b></font><br>");
            } else {
                String formatted = line.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
                sb.append("<font color=\"").append(normalColor).append("\">").append(formatted).append("</font><br>");
            }
        }

        String html = sb.toString();
        while (html.endsWith("<br>")) {
            html = html.substring(0, html.length() - 4);
        }

        try {
            return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
        } catch (Exception e) {
            return text;
        }
    }
}

package com.royal.edunotes;

import android.content.Context;
import androidx.core.text.HtmlCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats Quiz explanation strings with rich styling:
 * - Color-coded section headers (Correct Answer, Rule, Options breakdown, Memory Tips)
 * - Highlights and badges for Options (A, B, C, D)
 * - Diagram / Rule Box formatting
 * - Automatic Dark Mode color adaptation for optimal contrast and readability
 */
public class ExplanationSpanFormatter {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<(font|b|i|br|p|div|span|h[1-6]|ul|li)\\b[^>]*>", Pattern.CASE_INSENSITIVE);

    public static CharSequence format(Context context, String rawExplanation) {
        if (rawExplanation == null || rawExplanation.trim().isEmpty()) {
            return "";
        }

        boolean isDarkMode = false;
        if (context != null) {
            try {
                isDarkMode = new SettingsManager(context).isDarkMode();
            } catch (Exception ignored) {
            }
        }

        String content = rawExplanation.trim();

        // Check if content already contains HTML tags
        boolean hasHtml = HTML_TAG_PATTERN.matcher(content).find();

        String htmlFormatted;
        if (hasHtml) {
            htmlFormatted = content;
        } else {
            htmlFormatted = convertPlainTextToHtml(content, isDarkMode);
        }

        if (isDarkMode) {
            htmlFormatted = adaptColorsForDarkMode(htmlFormatted);
        }

        try {
            return HtmlCompat.fromHtml(htmlFormatted, HtmlCompat.FROM_HTML_MODE_LEGACY);
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * Converts plain text structured explanation into colorful, bold, and readable HTML
     */
    private static String convertPlainTextToHtml(String text, boolean isDarkMode) {
        String green = isDarkMode ? "#4ADE80" : "#1B5E20";
        String red = isDarkMode ? "#F87171" : "#C62828";
        String blue = isDarkMode ? "#60A5FA" : "#1565C0";
        String amber = isDarkMode ? "#FBBF24" : "#D97706";
        String purple = isDarkMode ? "#C084FC" : "#6A1B9A";

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\r?\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                sb.append("<br>");
                continue;
            }

            // Headers & Sections
            if (line.contains("🎯") || line.contains("✅ Correct") || line.toLowerCase().startsWith("correct answer")) {
                sb.append("<font color=\"").append(green).append("\"><b>").append(line).append("</b></font><br>");
            } else if (line.contains("❌") || line.toLowerCase().contains("why other options") || line.toLowerCase().contains("incorrect options")) {
                sb.append("<br><font color=\"").append(red).append("\"><b>").append(line).append("</b></font><br>");
            } else if (line.contains("📊") || line.toLowerCase().contains("rule chart") || line.toLowerCase().contains("grammar formula") || line.toLowerCase().contains("rule &")) {
                sb.append("<br><font color=\"").append(blue).append("\"><b>").append(line).append("</b></font><br>");
            } else if (line.contains("💡") || line.toLowerCase().contains("quick tip") || line.toLowerCase().contains("memory trick") || line.toLowerCase().contains("exam tip")) {
                sb.append("<br><font color=\"").append(amber).append("\"><b>").append(line).append("</b></font><br>");
            } else if (line.contains("📚") || line.toLowerCase().contains("all options meaning") || line.toLowerCase().contains("options meaning")) {
                sb.append("<br><font color=\"").append(purple).append("\"><b>").append(line).append("</b></font><br>");
            } else if (line.startsWith("Key -") || line.startsWith("Exp -") || line.startsWith("Syn -") || line.startsWith("Ant -")) {
                // Vocabulary mnemonic tags from existing DBs
                if (line.startsWith("Key -")) {
                    sb.append("<b>💡 Trick / Key:</b> ").append(line.substring(5).trim()).append("<br>");
                } else if (line.startsWith("Exp -")) {
                    sb.append("<b>📖 Story / Context:</b> ").append(line.substring(5).trim()).append("<br>");
                } else if (line.startsWith("Syn -")) {
                    sb.append("<b><font color=\"").append(green).append("\">Synonyms:</font></b> ").append(line.substring(5).trim()).append("<br>");
                } else if (line.startsWith("Ant -")) {
                    sb.append("<b><font color=\"").append(red).append("\">Antonyms:</font></b> ").append(line.substring(5).trim()).append("<br>");
                }
            } else if (line.matches("^[•\\-\\*]?\\s*\\([A-D]\\).*") || line.matches("^[•\\-\\*]?\\s*[A-D]\\).*")) {
                // Option bullet line like: • (A) Word: meaning
                String formattedLine = formatOptionBullet(line, green, red);
                sb.append(formattedLine).append("<br>");
            } else {
                // Regular line: bold keywords before colon or dash
                int colonIdx = line.indexOf(':');
                int dashIdx = line.indexOf(" - ");
                if (colonIdx > 0 && colonIdx < 30) {
                    String title = line.substring(0, colonIdx);
                    String rest = line.substring(colonIdx);
                    sb.append("<b>").append(title).append("</b>").append(rest).append("<br>");
                } else if (dashIdx > 0 && dashIdx < 30 && i == 0) {
                    // Target word definition on first line: e.g. "Trouncing - करारी हार"
                    String word = line.substring(0, dashIdx);
                    String meaning = line.substring(dashIdx + 3);
                    sb.append("<font color=\"").append(blue).append("\"><b><big>").append(word).append("</big></b></font>")
                            .append(" — <b>").append(meaning).append("</b><br>");
                } else {
                    sb.append(line).append("<br>");
                }
            }
        }

        return sb.toString();
    }

    private static String formatOptionBullet(String line, String green, String red) {
        Pattern p = Pattern.compile("([•\\-\\*]?\\s*)(\\([A-D]\\)|[A-D]\\))\\s*(.*)");
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String badge = m.group(2);
            String rest = m.group(3);
            boolean isCorrect = rest.contains("✅") || rest.toLowerCase().contains("[correct") || rest.toLowerCase().contains("correct]");
            String color = isCorrect ? green : red;
            return "• <b><font color=\"" + color + "\">" + badge + "</font></b> " + rest;
        }
        return line;
    }

    /**
     * Replaces light-theme hex colors with high-contrast dark-theme hex colors
     */
    private static String adaptColorsForDarkMode(String html) {
        return html
                .replaceAll("(?i)#1B5E20", "#4ADE80")
                .replaceAll("(?i)#2E7D32", "#4ADE80")
                .replaceAll("(?i)#388E3C", "#4ADE80")
                .replaceAll("(?i)#C62828", "#F87171")
                .replaceAll("(?i)#B71C1C", "#F87171")
                .replaceAll("(?i)#D32F2F", "#F87171")
                .replaceAll("(?i)#1565C0", "#60A5FA")
                .replaceAll("(?i)#0D47A1", "#60A5FA")
                .replaceAll("(?i)#1976D2", "#60A5FA")
                .replaceAll("(?i)#E65100", "#FBBF24")
                .replaceAll("(?i)#D97706", "#FBBF24")
                .replaceAll("(?i)#F57C00", "#FBBF24")
                .replaceAll("(?i)#6A1B9A", "#C084FC")
                .replaceAll("(?i)#4A148C", "#C084FC")
                .replaceAll("(?i)#7B1FA2", "#C084FC")
                .replaceAll("(?i)#263238", "#E2E8F0")
                .replaceAll("(?i)#212121", "#E2E8F0")
                .replaceAll("(?i)#333333", "#E2E8F0");
    }

    /**
     * Converts rich/HTML explanation into clean, readable plain text for clipboard copy or bookmarks
     */
    public static String toPlainText(String rawExplanation) {
        if (rawExplanation == null || rawExplanation.trim().isEmpty()) {
            return "";
        }
        return rawExplanation
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("(?i)<[^>]+>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}

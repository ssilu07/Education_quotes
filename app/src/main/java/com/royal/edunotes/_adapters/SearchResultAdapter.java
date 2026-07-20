package com.royal.edunotes._adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.R;
import com.royal.edunotes._activities.VocabSearchActivity;

import java.util.List;
import java.util.Locale;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnResultClickListener {
        void onResultClick(VocabSearchActivity.SearchResult result);
    }

    private final Context context;
    private final List<VocabSearchActivity.SearchResult> results;
    private final OnResultClickListener listener;
    private String highlightQuery = "";

    public SearchResultAdapter(Context context, List<VocabSearchActivity.SearchResult> results, OnResultClickListener listener) {
        this.context = context;
        this.results = results;
        this.listener = listener;
    }

    /** Call before notifyDataSetChanged() so newly bound rows highlight the term that produced them. */
    public void setHighlightQuery(String query) {
        this.highlightQuery = query == null ? "" : query.trim();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VocabSearchActivity.SearchResult result = results.get(position);
        holder.word.setText(highlight(result.word));
        holder.meaning.setText(highlight(result.meaning));
        holder.chapter.setText(result.chapterTitle);
        holder.itemView.setOnClickListener(v -> listener.onResultClick(result));
    }

    private CharSequence highlight(String text) {
        if (text == null) return "";
        if (highlightQuery.isEmpty()) return text;

        SpannableString spannable = new SpannableString(text);
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerQuery = highlightQuery.toLowerCase(Locale.ROOT);
        int highlightColor = ContextCompat.getColor(context, R.color.searchHighlight);

        int start = lowerText.indexOf(lowerQuery);
        while (start >= 0) {
            int end = start + lowerQuery.length();
            spannable.setSpan(new BackgroundColorSpan(highlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = lowerText.indexOf(lowerQuery, end);
        }
        return spannable;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView word, meaning, chapter;

        ViewHolder(View itemView) {
            super(itemView);
            word = itemView.findViewById(R.id.tv_result_word);
            meaning = itemView.findViewById(R.id.tv_result_meaning);
            chapter = itemView.findViewById(R.id.tv_result_chapter);
        }
    }
}

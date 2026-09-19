package com.royal.edunotes._adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._models.QuoteModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.NoteViewHolder> {

    public interface NoteItemClickListener {
        void onNoteCardClick(QuoteModel quoteModel, int dataIndex);
        void onNoteBookmarkClick(QuoteModel quoteModel, ImageView starView, int position);
        void onNoteCopyClick(QuoteModel quoteModel);
        void onNoteShareClick(QuoteModel quoteModel, View cardView);
        void onNoteTTSClick(QuoteModel quoteModel);
        void onNoteLearnedClick(QuoteModel quoteModel, TextView learnedView, int position);
    }

    private final Context context;
    private ArrayList<QuoteModel> quoteModels;
    private final NoteItemClickListener listener;
    private final SettingsManager settingsManager;
    private final HashSet<String> bookmarkedNotes = new HashSet<>();

    public NotesListAdapter(Context context, ArrayList<QuoteModel> quoteModels,
                            NoteItemClickListener listener, ArrayList<ModelDatabase> modelDatabases) {
        this.context = context;
        this.quoteModels = new ArrayList<>(quoteModels);
        this.listener = listener;
        this.settingsManager = new SettingsManager(context);
        buildBookmarkSet(modelDatabases);
    }

    public void updateData(ArrayList<QuoteModel> newList, ArrayList<ModelDatabase> newBookmarks) {
        this.quoteModels = new ArrayList<>(newList);
        if (newBookmarks != null) {
            buildBookmarkSet(newBookmarks);
        }
        notifyDataSetChanged();
    }

    public void updateBookmarkSet(ArrayList<ModelDatabase> newBookmarks) {
        buildBookmarkSet(newBookmarks);
        notifyDataSetChanged();
    }

    public void setBookmarked(String quote, boolean isBookmarked) {
        if (quote == null) return;
        String trimmed = quote.trim();
        if (isBookmarked) {
            bookmarkedNotes.add(trimmed);
            bookmarkedNotes.add(quote);
        } else {
            bookmarkedNotes.remove(trimmed);
            bookmarkedNotes.remove(quote);
        }
    }

    private void buildBookmarkSet(ArrayList<ModelDatabase> databases) {
        bookmarkedNotes.clear();
        if (databases != null) {
            for (ModelDatabase db : databases) {
                if (db != null && db.getNote() != null) {
                    bookmarkedNotes.add(db.getNote().trim());
                    bookmarkedNotes.add(db.getNote());
                }
            }
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notes_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        QuoteModel item = quoteModels.get(position);
        if (item == null) return;

        holder.tvNoteNumber.setText("Note #" + (position + 1));
        if (item.getQuote() != null && (item.getQuote().contains("<highlight>") || item.getQuote().contains("<instruction>"))) {
            com.royal.edunotes.QuestionSpanFormatter.FormattedQuestion formatted =
                    com.royal.edunotes.QuestionSpanFormatter.format(context, item.getQuote());
            if (formatted.instruction != null && !formatted.instruction.isEmpty()) {
                android.text.SpannableStringBuilder combined = new android.text.SpannableStringBuilder();
                combined.append(formatted.instruction).append("\n\n").append(formatted.bodySpan);
                holder.tvNoteQuote.setText(combined);
            } else {
                holder.tvNoteQuote.setText(formatted.bodySpan);
            }
        } else {
            holder.tvNoteQuote.setText(item.getQuote());
        }
        holder.tvNoteQuote.setTextSize(settingsManager.getFontSize());

        // Handle Image or detailed explanation in 'value' field
        String val = item.getValue();
        if (val != null && (val.startsWith("http://") || val.startsWith("https://"))) {
            holder.ivNoteImage.setVisibility(View.VISIBLE);
            holder.tvNoteExplanation.setVisibility(View.GONE);
            try {
                Picasso.get().load(val).into(holder.ivNoteImage);
            } catch (Exception e) {
                holder.ivNoteImage.setVisibility(View.GONE);
            }
        } else if (val != null && !val.trim().isEmpty()) {
            holder.ivNoteImage.setVisibility(View.GONE);
            holder.tvNoteExplanation.setVisibility(View.VISIBLE);
            holder.tvNoteExplanation.setText(val.trim());
        } else {
            holder.ivNoteImage.setVisibility(View.GONE);
            holder.tvNoteExplanation.setVisibility(View.GONE);
        }

        // Bookmark status
        boolean isBookmarked = (item.getQuote() != null &&
                (bookmarkedNotes.contains(item.getQuote().trim()) || bookmarkedNotes.contains(item.getQuote())))
                || item.isBookmared();
        item.setBookmared(isBookmarked);
        holder.ivNoteStar.setImageResource(isBookmarked ? R.drawable.starfilled : R.drawable.star);

        // Learned status
        boolean isLearned = settingsManager.isWordLearned(item.getQuote());
        item.setLearned(isLearned);
        if (isLearned) {
            holder.tvNoteLearned.setText("Learned ✓");
            holder.tvNoteLearned.setBackgroundResource(R.drawable.bg_learned_badge);
            holder.tvNoteLearned.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvNoteLearned.setText("Mark Learned");
            holder.tvNoteLearned.setBackgroundResource(R.drawable.bg_unlearned_badge);
            holder.tvNoteLearned.setTextColor(Color.parseColor("#666666"));
        }

        // Listeners
        holder.cardViewNote.setOnClickListener(v -> {
            if (listener != null) listener.onNoteCardClick(item, holder.getAdapterPosition());
        });
        holder.ivNoteOpenCard.setOnClickListener(v -> {
            if (listener != null) listener.onNoteCardClick(item, holder.getAdapterPosition());
        });
        holder.btnNoteCardView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteCardClick(item, holder.getAdapterPosition());
        });

        holder.ivNoteStar.setOnClickListener(v -> {
            if (listener != null) listener.onNoteBookmarkClick(item, holder.ivNoteStar, holder.getAdapterPosition());
        });
        holder.tvNoteLearned.setOnClickListener(v -> {
            if (listener != null) listener.onNoteLearnedClick(item, holder.tvNoteLearned, holder.getAdapterPosition());
        });
        holder.btnNoteTts.setOnClickListener(v -> {
            if (listener != null) listener.onNoteTTSClick(item);
        });
        holder.btnNoteCopy.setOnClickListener(v -> {
            if (listener != null) listener.onNoteCopyClick(item);
        });
        holder.btnNoteShare.setOnClickListener(v -> {
            if (listener != null) listener.onNoteShareClick(item, holder.cardViewNote);
        });
    }

    @Override
    public int getItemCount() {
        return quoteModels.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardViewNote;
        TextView tvNoteNumber;
        TextView tvNoteLearned;
        ImageView ivNoteStar;
        ImageView ivNoteOpenCard;
        ImageView ivNoteImage;
        TextView tvNoteQuote;
        TextView tvNoteExplanation;
        View btnNoteTts;
        View btnNoteCopy;
        View btnNoteShare;
        View btnNoteCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewNote = itemView.findViewById(R.id.card_view_note);
            tvNoteNumber = itemView.findViewById(R.id.tv_note_number);
            tvNoteLearned = itemView.findViewById(R.id.tv_note_learned);
            ivNoteStar = itemView.findViewById(R.id.iv_note_star);
            ivNoteOpenCard = itemView.findViewById(R.id.iv_note_open_card);
            ivNoteImage = itemView.findViewById(R.id.iv_note_image);
            tvNoteQuote = itemView.findViewById(R.id.tv_note_quote);
            tvNoteExplanation = itemView.findViewById(R.id.tv_note_explanation);
            btnNoteTts = itemView.findViewById(R.id.btn_note_tts);
            btnNoteCopy = itemView.findViewById(R.id.btn_note_copy);
            btnNoteShare = itemView.findViewById(R.id.btn_note_share);
            btnNoteCardView = itemView.findViewById(R.id.btn_note_card_view);
        }
    }
}

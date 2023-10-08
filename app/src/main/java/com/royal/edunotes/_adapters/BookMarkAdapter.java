package com.royals.edunotes._adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.royals.edunotes.R;
import com.royals.edunotes._models.bookmarkModel;

import java.util.List;

/**
 * Created by Admin on 23-03-2018.
 */

public class BookMarkAdapter extends RecyclerView.Adapter<BookMarkAdapter.MyViewHolder> {

    private List<bookmarkModel> bookmarkList;
    Context context;
    protected BookmarkClickInterface bookmarkClickInterface;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView hackText;
        public LinearLayout mainLL;
        public bookmarkModel bookmarkModel;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            hackText = (TextView) view.findViewById(R.id.hackText);
            mainLL = (LinearLayout) view.findViewById(R.id.mainLL);
            mainLL.setOnClickListener(this);
        }

        public void setData(bookmarkModel bookmarkModel) {
            this.bookmarkModel = bookmarkModel;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.mainLL) {
                bookmarkClickInterface.bookmarkClick(bookmarkModel);
            }
        }
    }

    public BookMarkAdapter(Context context, List<bookmarkModel> bookmarkList, BookmarkClickInterface bookmarkClickInterface) {
        this.bookmarkList = bookmarkList;
        this.context = context;
        this.bookmarkClickInterface = bookmarkClickInterface;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_bookmark_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.setData(bookmarkList.get(position));

        bookmarkModel model = bookmarkList.get(position);
        holder.title.setText(model.getTitle());
        holder.hackText.setText(model.getHack());

    }


    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }


    public interface BookmarkClickInterface {
        void bookmarkClick(bookmarkModel bookmarkModel);
    }
}

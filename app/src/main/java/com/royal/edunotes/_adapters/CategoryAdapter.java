package com.royal.edunotes._adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.royal.edunotes.R;
import com.royal.edunotes._models.CategoryModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private List<CategoryModel> categoryList;
    Context context;

    String[] firstcolor;
    String[] secondcolor;
    protected CategoryClickInterface clickInterface;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title, tvProgress;
        public LinearLayout mainLL;
        public ProgressBar progressBar;
        CategoryModel categoryModel;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.catTitle);
            mainLL = view.findViewById(R.id.mainLL);
            progressBar = view.findViewById(R.id.progressBar);
            tvProgress = view.findViewById(R.id.tvProgress);
            mainLL.setOnClickListener(this);
        }

        public void setData(CategoryModel categoryModel) {
            this.categoryModel = categoryModel;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.mainLL) {
                clickInterface.categoryClick(categoryModel);
            }
        }
    }

    public CategoryAdapter(Context context, List<CategoryModel> categoryList, CategoryClickInterface clickInterface) {
        this.categoryList = categoryList;
        this.context = context;
        this.clickInterface = clickInterface;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setData(categoryList.get(position));

        CategoryModel model = categoryList.get(position);
        holder.title.setText(model.getTitle());

        // Progress display
        int total = model.getTotalCount();
        int viewed = model.getViewedCount();
        if (total > 0) {
            int progressPercent = (viewed * 100) / total;
            holder.progressBar.setMax(100);
            holder.progressBar.setProgress(progressPercent);
            holder.tvProgress.setText(total + " Questions");
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.tvProgress.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setVisibility(View.INVISIBLE);
            holder.tvProgress.setVisibility(View.INVISIBLE);
        }

        // Gradient background
        firstcolor = context.getResources().getStringArray(R.array.firstcolor);
        secondcolor = context.getResources().getStringArray(R.array.secondcolor);

        int colorIndex = position % firstcolor.length;
        int[] colors = {Color.parseColor(firstcolor[colorIndex]), Color.parseColor(secondcolor[colorIndex])};

        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        gd.setCornerRadius(0f);
        holder.mainLL.setBackground(gd);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface CategoryClickInterface {
        void categoryClick(CategoryModel categoryModel);
    }
}

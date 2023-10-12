package com.royal.edunotes._adapters;

/**
 * Created by Admin on 20-03-2018.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.royal.edunotes.R;
import com.royal.edunotes._models.CategoryModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private List<CategoryModel> categoryList;
    Context context;


    String[] firstcolor;
    String[] secondcolor;
    boolean flag = false;
    int i = 0;
    protected CategoryClickInterface clickInterface;
    CategoryModel categoryModel;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
//        public ImageView icon;
        public LinearLayout mainLL;
        CategoryModel categoryModel;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.catTitle);
//            icon = (ImageView) view.findViewById(R.id.catImg);
            mainLL = (LinearLayout) view.findViewById(R.id.mainLL);
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
//        Log.e("TAG==", model.getImageUrl());

//        Picasso.Builder builder = new Picasso.Builder(context);
//        builder.listener(new Picasso.Listener() {
//            @Override
//            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
//                exception.printStackTrace();
//                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.build().load(model.getImageUrl()).into(holder.icon);

        firstcolor = context.getResources().getStringArray(R.array.firstcolor);
        secondcolor = context.getResources().getStringArray(R.array.secondcolor);


        if (categoryList.size() > firstcolor.length) {

            for (int i = 0; i < categoryList.size(); i++) {

                if (position % firstcolor.length == i) {

                    int[] colors = {Color.parseColor(firstcolor[i]), Color.parseColor(secondcolor[i])};

                    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    gd.setCornerRadius(0f);

                    holder.mainLL.setBackgroundDrawable(gd);
                }
            }
        } else {
            String random = (firstcolor[position]);
            String random1 = (secondcolor[position]);

            int[] colors = {Color.parseColor(random), Color.parseColor(random1)};

            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            gd.setCornerRadius(0f);

            holder.mainLL.setBackgroundDrawable(gd);
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface CategoryClickInterface {
        void categoryClick(CategoryModel categoryModel);
    }
}
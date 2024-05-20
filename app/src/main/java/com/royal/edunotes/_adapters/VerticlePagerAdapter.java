package com.royal.edunotes._adapters;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.royal.edunotes.R;
import com.royal.edunotes._database.DatabaseHelper;
import com.royal.edunotes._database.ModelDatabase;
import com.royal.edunotes._models.QuoteModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by rizvan on 12/13/16.
 */

public class VerticlePagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<QuoteModel> quoteModels;
    String category;
    ClickInterface clickInterface;
    DatabaseHelper db;

    ArrayList<ModelDatabase> modelDatabases;

    public VerticlePagerAdapter(Context context, ArrayList<QuoteModel> quoteModels, ClickInterface clickInterface,ArrayList<ModelDatabase> modelDatabases) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.quoteModels = quoteModels;
        this.clickInterface = clickInterface;
        this.modelDatabases = modelDatabases;
    }

    @Override
    public int getCount() {
        return quoteModels.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

        TextView titleTxt = (TextView) itemView.findViewById(R.id.title);
        TextView hackTxt = (TextView) itemView.findViewById(R.id.hackText);
        ImageView hackTxt2 = (ImageView) itemView.findViewById(R.id.hackText2);


        LinearLayout copy = (LinearLayout) itemView.findViewById(R.id.copyLL);
        final LinearLayout starLL = (LinearLayout) itemView.findViewById(R.id.starLL);
        LinearLayout share = (LinearLayout) itemView.findViewById(R.id.shareLL);
        LinearLayout otherapp = (LinearLayout) itemView.findViewById(R.id.ourappLL);

        final ImageView star = (ImageView) itemView.findViewById(R.id.star);

        hackTxt.setText(quoteModels.get(position).getQuote());
 //       titleTxt.setText(quoteModels.get(position).getCategoryName());

        String url = quoteModels.get(position).getValue();

        if (url != null) {
            hackTxt2.setVisibility(View.VISIBLE);
            Picasso.get().load(url).into(hackTxt2);
        } else {
            hackTxt2.setVisibility(View.GONE);

        }

        int[] colors = {Color.rgb(36, 7, 80),Color.rgb(255, 0, 128), Color.rgb(50, 1, 47), Color.rgb(249, 115, 0), Color.rgb(27, 66, 66), Color.rgb(64, 165, 120), Color.rgb(100, 13, 107), Color.rgb(181, 27, 117), Color.rgb(0, 0, 0)};
        int randomIndex = (int) (Math.random() * colors.length);
        int randomColor = colors[randomIndex];


        //       titleTxt.setText(quoteModels.get(position).getCategoryName());



// Set the text color of the TextView to the random color
        hackTxt.setTextColor(randomColor);

        for(int i =0;i<modelDatabases.size();i++){

            if(quoteModels.get(position).getQuote().equals(modelDatabases.get(i).getNote())){
                quoteModels.get(position).setBookmark("1");
                quoteModels.get(position).setBookmared(true);
                break;
            }else{

                quoteModels.get(position).setBookmark("0");
                quoteModels.get(position).setBookmared(false);

            }

        }

        for(int i =0;i<modelDatabases.size();i++){

            if(quoteModels.get(position).getValue().equals(modelDatabases.get(i).getNote())){
                quoteModels.get(position).setBookmark("1");
                quoteModels.get(position).setBookmared(true);
                break;
            }else{

                quoteModels.get(position).setBookmark("0");
                quoteModels.get(position).setBookmared(false);

            }

        }

        if (quoteModels.get(position).isBookmared()) {
            star.setImageResource(R.drawable.starfilled);
        } else {
            star.setImageResource(R.drawable.star);
        }


        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickInterface.onCopyClick(quoteModels.get(position));
            }
        });


        starLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickInterface.onBoookmarkClick(quoteModels.get(position), star);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickInterface.onShareClick(quoteModels.get(position));
            }
        });

        otherapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickInterface.onMoreAppsClick();
            }
        });

        container.addView(itemView);

        return itemView;
    }

    public interface ClickInterface {
        void onBoookmarkClick(QuoteModel CategoryModel, ImageView star);

        void onCopyClick(QuoteModel CategoryModel);

        void
        onShareClick(QuoteModel CategoryModel);

        void onMoreAppsClick();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

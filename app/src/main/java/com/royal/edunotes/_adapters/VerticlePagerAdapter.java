package com.royals.edunotes._adapters;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.royals.edunotes.R;
import com.royals.edunotes._database.DatabaseHelper;
import com.royals.edunotes._database.ModelDatabase;
import com.royals.edunotes._models.QuoteModel;

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


        LinearLayout copy = (LinearLayout) itemView.findViewById(R.id.copyLL);
        final LinearLayout starLL = (LinearLayout) itemView.findViewById(R.id.starLL);
        LinearLayout share = (LinearLayout) itemView.findViewById(R.id.shareLL);
        LinearLayout otherapp = (LinearLayout) itemView.findViewById(R.id.ourappLL);

        final ImageView star = (ImageView) itemView.findViewById(R.id.star);

        hackTxt.setText(quoteModels.get(position).getQuote());
 //       titleTxt.setText(quoteModels.get(position).getCategoryName());


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

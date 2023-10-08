package com.royal.edunotes._adapters;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.royal.edunotes.R;


/**
 * Created by Admin on 26-03-2018.
 */

public class BookmarkPagerAdapter extends PagerAdapter{
    Context mContext;
    LayoutInflater mLayoutInflater;
    public BookmarkPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }



    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.content_main, container, false);

//        TextView titleTxt = (TextView) itemView.findViewById(R.id.title);
//        TextView hackTxt = (TextView) itemView.findViewById(R.id.hackText);
//        TextView senderTxt = (TextView) itemView.findViewById(R.id.senderTxt);
//        TextView bookmarkTxt = (TextView) itemView.findViewById(R.id.bookmarkText);
//        TextView likeTxt = (TextView) itemView.findViewById(R.id.likeTxt);
//
//        ImageView copy = (ImageView) itemView.findViewById(R.id.copyImg);
//        ImageView like = (ImageView) itemView.findViewById(R.id.likeBtn);
//        ImageView bookmark = (ImageView) itemView.findViewById(R.id.bookmark);
//        ImageView share = (ImageView) itemView.findViewById(R.id.share);

        container.addView(itemView);

        return itemView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

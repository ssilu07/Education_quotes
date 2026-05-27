package com.royal.edunotes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class VerticalViewPager extends ViewPager {

    private GestureDetectorCompat gestureDetector;
    private boolean flingConsumed = false;
    private float interceptStartX, interceptStartY;
    private int touchSlop;

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, new VerticalPageTransformer());
        setOverScrollMode(OVER_SCROLL_NEVER);
        setOffscreenPageLimit(3);
        setFastScroller();
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        gestureDetector = new GestureDetectorCompat(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                        if (e1 == null) return false;
                        float diffY = e2.getY() - e1.getY();
                        if (Math.abs(vY) > 300 && Math.abs(diffY) > 30) {
                            flingConsumed = true;
                            int count = getAdapter() != null ? getAdapter().getCount() : 0;
                            int target = diffY < 0 ? getCurrentItem() + 1 : getCurrentItem() - 1;
                            target = Math.max(0, Math.min(target, count - 1));
                            setCurrentItem(target, true);
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void setFastScroller() {
        try {
            Field f = ViewPager.class.getDeclaredField("mScroller");
            f.setAccessible(true);
            f.set(this, new Scroller(getContext(), new DecelerateInterpolator()) {
                @Override
                public void startScroll(int x, int y, int dx, int dy, int duration) {
                    super.startScroll(x, y, dx, dy, 220);
                }
                @Override
                public void startScroll(int x, int y, int dx, int dy) {
                    super.startScroll(x, y, dx, dy, 220);
                }
            });
        } catch (Exception ignored) {}
    }

    // ScrollView andar hai — wo requestDisallowInterceptTouchEvent(true) call karta hai
    // jisse ViewPager ka onInterceptTouchEvent band ho jaata tha. Isko ignore karte hain.
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // intentionally ignored so children can't block our vertical swipe detection
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                interceptStartX = ev.getX();
                interceptStartY = ev.getY();
                // ViewPager ka internal mLastMotionX initialize karo swapped coords se
                super.onInterceptTouchEvent(swapXY(ev));
                swapXY(ev); // restore
                return false;

            case MotionEvent.ACTION_MOVE:
                float diffY = Math.abs(ev.getY() - interceptStartY);
                float diffX = Math.abs(ev.getX() - interceptStartX);
                // Real Y movement check — swapXY se sensitivity halve ho jaati thi
                boolean superWants = super.onInterceptTouchEvent(swapXY(ev));
                swapXY(ev); // restore
                return (diffY > touchSlop && diffY > diffX) || superWants;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                super.onInterceptTouchEvent(swapXY(ev));
                swapXY(ev);
                return false;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            flingConsumed = false;
        }

        gestureDetector.onTouchEvent(ev);

        // GestureDetector ne fling handle kiya — ViewPager ke gesture ko cancel karo
        if (ev.getActionMasked() == MotionEvent.ACTION_UP && flingConsumed) {
            MotionEvent cancel = MotionEvent.obtain(ev);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            super.onTouchEvent(swapXY(cancel));
            cancel.recycle();
            flingConsumed = false;
            return true;
        }

        boolean result = super.onTouchEvent(swapXY(ev));
        swapXY(ev);
        return result;
    }

    private static class VerticalPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            if (position < -1 || position > 1) {
                view.setAlpha(0f);
            } else {
                view.setAlpha(1f);
                view.setTranslationX(view.getWidth() * -position);
                view.setTranslationY(position * view.getHeight());
            }
        }
    }

    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();
        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;
        ev.setLocation(newX, newY);
        return ev;
    }
}

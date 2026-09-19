package com.royal.edunotes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public class VerticalViewPager extends ViewPager {

    private GestureDetectorCompat gestureDetector;
    private boolean flingConsumed = false;
    private float interceptStartX, interceptStartY;
    private int touchSlop;
    private boolean isBeingDragged = false;
    private boolean canChildScrollUpInitially = false;
    private boolean canChildScrollDownInitially = false;

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
                        float startY = (e1 != null) ? e1.getY() : interceptStartY;
                        float endY = (e2 != null) ? e2.getY() : startY;
                        float diffY = endY - startY;

                        // Check velocity or minimum displacement
                        if (Math.abs(vY) > 250 || Math.abs(diffY) > touchSlop * 2) {
                            flingConsumed = true;
                            int count = getAdapter() != null ? getAdapter().getCount() : 0;
                            if (count <= 0) return false;

                            // vY < 0 or diffY < 0: swiping UP (finger moves towards top) -> NEXT CARD (+1)
                            // vY > 0 or diffY > 0: swiping DOWN (finger moves towards bottom) -> PREVIOUS CARD (-1)
                            int target;
                            if (Math.abs(vY) > 250) {
                                target = (vY < 0) ? getCurrentItem() + 1 : getCurrentItem() - 1;
                            } else {
                                target = (diffY < 0) ? getCurrentItem() + 1 : getCurrentItem() - 1;
                            }
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
    // jisse ViewPager ka onInterceptTouchEvent band ho jaata tha. Isko ignore karte hain
    // taaki ScrollView reach top/bottom hone par ViewPager swipe detect kar sake.
    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // intentionally ignored so children can't permanently block our vertical swipe detection
    }

    private View findScrollViewUnder(View view, float x, float y) {
        if (view == null || view.getVisibility() != View.VISIBLE || view.getAlpha() < 0.5f) return null;
        if (view instanceof ScrollView) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                View child = vg.getChildAt(i);
                if (child.getVisibility() == View.VISIBLE && child.getAlpha() > 0.5f) {
                    float childLeft = child.getX();
                    float childTop = child.getY();
                    float childRight = childLeft + child.getWidth();
                    float childBottom = childTop + child.getHeight();
                    if (x >= childLeft && x <= childRight && y >= childTop && y <= childBottom) {
                        View found = findScrollViewUnder(child, x - childLeft, y - childTop);
                        if (found != null) return found;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Feed events to gestureDetector so it always receives ACTION_DOWN with accurate coords
        gestureDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                interceptStartX = ev.getX();
                interceptStartY = ev.getY();
                flingConsumed = false;
                isBeingDragged = false;

                // Check initial scrollability of child ScrollView under pointer
                View scrollView = findScrollViewUnder(this, ev.getX(), ev.getY());
                if (scrollView != null) {
                    canChildScrollUpInitially = scrollView.canScrollVertically(-1);
                    canChildScrollDownInitially = scrollView.canScrollVertically(1);
                } else {
                    canChildScrollUpInitially = false;
                    canChildScrollDownInitially = false;
                }
                return false;

            case MotionEvent.ACTION_MOVE:
                float diffY = ev.getY() - interceptStartY;
                float diffX = ev.getX() - interceptStartX;
                float absDiffY = Math.abs(diffY);
                float absDiffX = Math.abs(diffX);

                if (absDiffY > touchSlop && absDiffY > absDiffX) {
                    // diffY < 0 = dragging UP; diffY > 0 = dragging DOWN
                    if (diffY < 0 && canChildScrollDownInitially) {
                        // Child ScrollView has more content below; let child scroll
                        return false;
                    }
                    if (diffY > 0 && canChildScrollUpInitially) {
                        // Child ScrollView has more content above; let child scroll
                        return false;
                    }

                    // Otherwise child cannot scroll in this direction; intercept for card swiping
                    isBeingDragged = true;
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);

        final int action = ev.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                interceptStartX = ev.getX();
                interceptStartY = ev.getY();
                flingConsumed = false;
                isBeingDragged = false;
                break;

            case MotionEvent.ACTION_UP:
                if (!flingConsumed && isBeingDragged) {
                    float totalDiffY = ev.getY() - interceptStartY;
                    int count = getAdapter() != null ? getAdapter().getCount() : 0;
                    if (count > 0) {
                        int threshold = Math.max(getHeight() / 8, touchSlop * 3);
                        if (Math.abs(totalDiffY) > threshold) {
                            int target = (totalDiffY < 0) ? getCurrentItem() + 1 : getCurrentItem() - 1;
                            target = Math.max(0, Math.min(target, count - 1));
                            setCurrentItem(target, true);
                        }
                    }
                }
                isBeingDragged = false;
                flingConsumed = false;
                return true;

            case MotionEvent.ACTION_CANCEL:
                isBeingDragged = false;
                flingConsumed = false;
                break;
        }

        return true;
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
}

package com.royal.edunotes;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Universal helper to prevent top status bar and bottom navigation bar overlap across all screens.
 *
 * 1. Enables edge-to-edge so the Toolbar background seamlessly fills the status bar area.
 * 2. Applies top padding to the Toolbar equal to the status bar + camera cutout (notch) height,
 *    ensuring title, back button, and menu actions are positioned safely below the status bar.
 * 3. Applies bottom padding to the root content view for the system navigation bar,
 *    preventing content or buttons from being obscured by the Android gesture bar.
 */
public class WindowInsetsHelper {

    public static void applyEdgeToEdge(Activity activity, Toolbar toolbar) {
        if (activity == null) return;
        View content = activity.findViewById(android.R.id.content);
        View root = (content instanceof ViewGroup && ((ViewGroup) content).getChildCount() > 0)
                ? ((ViewGroup) content).getChildAt(0)
                : content;
        applyEdgeToEdge(activity, root, toolbar);
    }

    public static void applyEdgeToEdge(Activity activity, View rootView, Toolbar toolbar) {
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        // Ensure status bar is transparent so toolbar color shows through
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false);

        View targetRoot = rootView != null ? rootView : activity.findViewById(android.R.id.content);
        if (targetRoot == null) return;

        // Disable legacy fitsSystemWindows on root to avoid double-padding
        targetRoot.setFitsSystemWindows(false);

        // Ensure light (white) status bar icons on dark/cyan toolbars
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, targetRoot);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            try {
                boolean isDark = new SettingsManager(activity).isDarkMode();
                controller.setAppearanceLightNavigationBars(!isDark);
            } catch (Exception ignored) {}
        }

        // Ensure Toolbar can expand height with top padding
        if (toolbar != null) {
            ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
            if (lp != null && lp.height > 0) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                toolbar.setLayoutParams(lp);
            }
        }

        final int initTbLeft = toolbar != null ? toolbar.getPaddingLeft() : 0;
        final int initTbRight = toolbar != null ? toolbar.getPaddingRight() : 0;
        final int initTbBottom = toolbar != null ? toolbar.getPaddingBottom() : 0;

        final int initRootLeft = targetRoot.getPaddingLeft();
        final int initRootRight = targetRoot.getPaddingRight();
        final int initRootBottom = targetRoot.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(targetRoot, (v, windowInsets) -> {
            Insets barInsets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            if (toolbar != null) {
                toolbar.setPadding(
                        initTbLeft,
                        barInsets.top,
                        initTbRight,
                        initTbBottom
                );
            }

            v.setPadding(
                    initRootLeft + barInsets.left,
                    toolbar != null ? 0 : barInsets.top,
                    initRootRight + barInsets.right,
                    initRootBottom + barInsets.bottom
            );

            return windowInsets;
        });

        ViewCompat.requestApplyInsets(targetRoot);
    }
}

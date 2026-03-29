package com.royal.edunotes._activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.airbnb.lottie.LottieAnimationView;
import com.royal.edunotes.PrefManager;
import com.royal.edunotes.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;

    private ImageView splashLogo;
    private TextView appName, tagline, features, versionText;
    private View divider, logoGlow, circleTop, circleBottom;
    private LottieAnimationView lottieView;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Fullscreen immersive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0x00000000);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_splash);

        prefManager = new PrefManager(this);

        initViews();
        startAnimations();

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateNext, SPLASH_DURATION);
    }

    private void initViews() {
        splashLogo = findViewById(R.id.splash_logo);
        appName = findViewById(R.id.splash_app_name);
        tagline = findViewById(R.id.splash_tagline);
        features = findViewById(R.id.splash_features);
        divider = findViewById(R.id.splash_divider);
        logoGlow = findViewById(R.id.logo_glow);
        circleTop = findViewById(R.id.circle_top);
        circleBottom = findViewById(R.id.circle_bottom);
        lottieView = findViewById(R.id.splash_lottie);
        versionText = findViewById(R.id.splash_version);
    }

    private void startAnimations() {
        // Phase 1: Decorative circles fade in (background depth)
        animateFadeIn(circleTop, 0, 600, 0.3f);
        animateFadeIn(circleBottom, 200, 600, 0.3f);

        // Phase 2: Logo - bounce in with scale + glow
        animateLogoBounce(400);

        // Phase 3: App name slides up + fades in
        animateSlideUp(appName, 800, 500);

        // Phase 4: Tagline slides up
        animateSlideUp(tagline, 1050, 450);

        // Phase 5: Divider expands from center
        animateDividerExpand(1400);

        // Phase 6: Features text fades up
        animateSlideUp(features, 1650, 400);

        // Phase 7: Lottie animation + version
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            lottieView.animate().alpha(1f).setDuration(400).start();
            lottieView.playAnimation();
        }, 1800);

        animateFadeIn(versionText, 2000, 400, 1f);
    }

    private void animateLogoBounce(long startDelay) {
        // Glow pulse
        ObjectAnimator glowFade = ObjectAnimator.ofFloat(logoGlow, "alpha", 0f, 0.6f);
        glowFade.setDuration(800);
        glowFade.setStartDelay(startDelay);
        glowFade.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator glowScaleX = ObjectAnimator.ofFloat(logoGlow, "scaleX", 0.5f, 1.2f);
        ObjectAnimator glowScaleY = ObjectAnimator.ofFloat(logoGlow, "scaleY", 0.5f, 1.2f);
        glowScaleX.setDuration(800);
        glowScaleY.setDuration(800);
        glowScaleX.setStartDelay(startDelay);
        glowScaleY.setStartDelay(startDelay);

        // Logo bounce in
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(splashLogo, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(splashLogo, "scaleX", 0.3f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(splashLogo, "scaleY", 0.3f, 1f);

        logoAlpha.setDuration(600);
        logoScaleX.setDuration(700);
        logoScaleY.setDuration(700);

        logoAlpha.setStartDelay(startDelay);
        logoScaleX.setStartDelay(startDelay);
        logoScaleY.setStartDelay(startDelay);

        logoScaleX.setInterpolator(new OvershootInterpolator(1.5f));
        logoScaleY.setInterpolator(new OvershootInterpolator(1.5f));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(glowFade, glowScaleX, glowScaleY, logoAlpha, logoScaleX, logoScaleY);
        set.start();

        // Subtle continuous glow pulse after initial animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator pulse = ObjectAnimator.ofFloat(logoGlow, "alpha", 0.6f, 0.3f);
            pulse.setDuration(1200);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.start();
        }, startDelay + 800);
    }

    private void animateSlideUp(View view, long startDelay, long duration) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        // Animate from current translationY (set in XML) to 0
        float startY = view.getTranslationY();
        if (startY == 0) startY = 40 * getResources().getDisplayMetrics().density;
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY",
                startY, 0f);

        alpha.setDuration(duration);
        translateY.setDuration(duration);

        alpha.setStartDelay(startDelay);
        translateY.setStartDelay(startDelay);

        translateY.setInterpolator(new DecelerateInterpolator(1.5f));

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, translateY);
        set.start();
    }

    private void animateDividerExpand(long startDelay) {
        // Post to ensure view is measured
        divider.post(() -> {
            int parentWidth = ((View) divider.getParent()).getWidth();
            int targetWidth = (int) (parentWidth * 0.35f);

            divider.getLayoutParams().width = 0;
            divider.requestLayout();

            ObjectAnimator alpha = ObjectAnimator.ofFloat(divider, "alpha", 0f, 1f);
            alpha.setDuration(300);
            alpha.setStartDelay(startDelay);

            ValueAnimator widthAnim = ValueAnimator.ofInt(0, targetWidth);
            widthAnim.setDuration(500);
            widthAnim.setStartDelay(startDelay);
            widthAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            widthAnim.addUpdateListener(animation -> {
                divider.getLayoutParams().width = (int) animation.getAnimatedValue();
                divider.requestLayout();
            });

            AnimatorSet set = new AnimatorSet();
            set.playTogether(alpha, widthAnim);
            set.start();
        });
    }

    private void animateFadeIn(View view, long startDelay, long duration, float targetAlpha) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, targetAlpha);
        alpha.setDuration(duration);
        alpha.setStartDelay(startDelay);
        alpha.setInterpolator(new DecelerateInterpolator());
        alpha.start();
    }

    private void navigateNext() {
        if (prefManager.isFirstTimeLaunch()) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            startActivity(new Intent(this, SelectedActivity.class));
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}

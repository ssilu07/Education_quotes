package com.royal.edunotes._activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.royal.edunotes.NotificationHelper;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;

public class Setting extends AppCompatActivity {

    private SettingsManager settingsManager;
    private SwitchCompat switchDarkMode, switchLanguage, switchNotifications;
    private TextView tvFontSizeValue, tvLanguageValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        settingsManager = new SettingsManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchLanguage = findViewById(R.id.switch_language);
        switchNotifications = findViewById(R.id.switch_notifications);
        tvFontSizeValue = findViewById(R.id.tv_font_size_value);
        tvLanguageValue = findViewById(R.id.tv_language_value);
    }

    private void loadSettings() {
        switchDarkMode.setChecked(settingsManager.isDarkMode());
        switchNotifications.setChecked(settingsManager.isNotificationsEnabled());
        switchLanguage.setChecked(settingsManager.isHindi());
        tvLanguageValue.setText(settingsManager.isHindi() ? "Hindi" : "English");
        updateFontSizeText();
    }

    private void updateFontSizeText() {
        String[] labels = {"Small (14sp)", "Medium (17sp)", "Large (22sp)"};
        tvFontSizeValue.setText(labels[settingsManager.getFontSizeIndex()]);
    }

    private void setupListeners() {
        // Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setDarkMode(isChecked);
        });

        // Font Size
        findViewById(R.id.ll_font_size).setOnClickListener(v -> showFontSizeDialog());

        // Language Toggle
        switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setLanguage(isChecked ? SettingsManager.LANG_HINDI : SettingsManager.LANG_ENGLISH);
            tvLanguageValue.setText(isChecked ? "Hindi" : "English");
            Toast.makeText(this, "Language changed to " + (isChecked ? "Hindi" : "English"), Toast.LENGTH_SHORT).show();
        });

        // Notifications
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setNotificationsEnabled(isChecked);
            if (isChecked) {
                NotificationHelper.scheduleRepeatingRTCNotification(this, "9", "30");
                NotificationHelper.enableBootReceiver(this);
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                NotificationHelper.cancelAlarmRTC();
                NotificationHelper.disableBootReceiver(this);
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Rate Us
        findViewById(R.id.ll_rate_us).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        // More Apps
        findViewById(R.id.ll_more_apps).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.royals.englishtrickyvocab")));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Play Store", Toast.LENGTH_SHORT).show();
            }
        });

        // Invite Friends
        findViewById(R.id.ll_invite_friends).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out Vocab Tricks Reels - Best app for learning English vocabulary!\n" +
                    "https://play.google.com/store/apps/details?id=" + getPackageName());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Invite Friends"));
        });

        // About Us
        findViewById(R.id.ll_about_us).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("About Us")
                    .setMessage("Vocab Tricks Reels v1.2.2\n\n" +
                            "Learn English vocabulary with tricks, mnemonics, and daily quizzes.\n\n" +
                            "Features:\n" +
                            "- 10+ Vocab chapters\n" +
                            "- 20+ Idiom chapters\n" +
                            "- Daily Quiz with XP\n" +
                            "- AI Explanation\n" +
                            "- Text-to-Speech\n" +
                            "- Bookmarks\n" +
                            "- Dark Mode\n\n" +
                            "Made with love for learners!")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void showFontSizeDialog() {
        String[] options = {"Small (14sp)", "Medium (17sp)", "Large (22sp)"};
        int currentIndex = settingsManager.getFontSizeIndex();

        new AlertDialog.Builder(this)
                .setTitle("Choose Font Size")
                .setSingleChoiceItems(options, currentIndex, (dialog, which) -> {
                    settingsManager.setFontSizeByIndex(which);
                    updateFontSizeText();
                    Toast.makeText(this, "Font size changed. Restart app to apply.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

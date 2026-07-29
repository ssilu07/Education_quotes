package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.royal.edunotes.R;
import com.royal.edunotes.SettingsManager;

public class NotificationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new SettingsManager(this).applyDarkMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvTitle = findViewById(R.id.tv_notification_title);
        TextView tvMessage = findViewById(R.id.tv_notification_message);
        Button btnBack = findViewById(R.id.btn_back_to_app);

        String title = "";
        String message = "";

        if (getIntent() != null) {
            title = getIntent().getStringExtra("title");
            message = getIntent().getStringExtra("message");
        }

        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("Message from App");
        }

        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("");
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationDetailActivity.this, SelectedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Back button on toolbar acts same as button
        Intent intent = new Intent(this, SelectedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        return true;
    }
}

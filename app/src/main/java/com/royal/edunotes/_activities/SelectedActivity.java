package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.royal.edunotes.R;

public class SelectedActivity extends AppCompatActivity implements View.OnClickListener  {

    private CardView box1;
    private CardView box2;
    // private CardView box3;
    // private CardView box4;

    public static String ScreenCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected);

        box1 = findViewById(R.id.vocab);
        box2 = findViewById(R.id.idiom);
        // box3 = findViewById(R.id.box3);
        // box4 = findViewById(R.id.box4);

        // Set click listeners
        box1.setOnClickListener(this);
        box2.setOnClickListener(this);
        // box3.setOnClickListener(this);
        // box4.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.vocab:
                    ScreenCheck = "Vocab";
                    startActivity(new Intent(SelectedActivity.this, MainActivity.class));
                    break;

                case R.id.idiom:
                    ScreenCheck = "Idiom";
                    startActivity(new Intent(SelectedActivity.this, IdiomPhrasesActivity.class));
                    break;

                // case R.id.box3:
                //     showToast("Box 3 Clicked");
                //     break;

                // case R.id.box4:
                //     showToast("Box 4 Clicked");
                //     break;
            }
        }
    }


}
package com.royal.edunotes._activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.royal.edunotes.R;
import com.royal.edunotes.Utility;

public class SelectedActivity extends AppCompatActivity {

    CardView cv_vocab,cv_idiom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        cv_vocab = findViewById(R.id.vocab);
        cv_idiom = findViewById(R.id.idiom);

        cv_vocab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.ScreenCheck = "Vocab";

             Intent intent = new Intent(SelectedActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        cv_idiom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.ScreenCheck = "Idiom";

                Intent intent = new Intent(SelectedActivity.this, IdiomPhrasesActivity.class);
                startActivity(intent);
            }
        });
    }
}



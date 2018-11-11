package com.papamilios.dimitris.cardsagainstfoulis;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity for adding a card.
 */

public class NewCardActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.example.android.wordlistsql.REPLY";

    private EditText mEditCardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);
        mEditCardView = findViewById(R.id.edit_word);

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                if (TextUtils.isEmpty(mEditCardView.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    String cardText = mEditCardView.getText().toString();
                    replyIntent.putExtra(EXTRA_REPLY, cardText);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });
    }
}


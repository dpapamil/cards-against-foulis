package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.papamilios.dimitris.cardsagainstfoulis.R;

/**
 * Activity for adding a card.
 */

public class NewCardActivity extends AppCompatActivity {

    public static final String CARD_TEXT = "com.example.android.wordlistsql.CARD_TEXT";
    public static final String CARD_TO_UPDATE = "com.example.android.wordlistsql.CARD_TO_UPDATE";

    private EditText mEditCardView;
    private int mCardId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);
        mEditCardView = findViewById(R.id.edit_word);

        Intent intent = getIntent();
        mCardId = intent.getIntExtra(CARD_TO_UPDATE, 0);
        String cardText = intent.getStringExtra(CARD_TEXT);
        if (mCardId > 0 && cardText != null) {
            mEditCardView.setText(cardText);
        }

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mEditCardView.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                String cardText = mEditCardView.getText().toString();
                replyIntent.putExtra(CARD_TEXT, cardText);
                replyIntent.putExtra(CARD_TO_UPDATE, mCardId);
                setResult(RESULT_OK, replyIntent);
            }
            finish();
            }
        });
    }
}


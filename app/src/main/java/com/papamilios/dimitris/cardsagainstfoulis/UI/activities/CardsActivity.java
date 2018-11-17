package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardViewModel;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.List;


/*
*  The activity to interact with either the black or the white cards.
 */

public class CardsActivity extends AppCompatActivity {

    public static final int NEW_CARD_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_CARD_ACTIVITY_REQUEST_CODE = 2;

    private CardViewModel mCardViewModel;
    private boolean mIsWhite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.WHITE_CARDS);
        mIsWhite = message.equals("white");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.cards_title);
        String title = mIsWhite? getString(R.string.white_cards) : getString(R.string.black_cards);
        textView.setText(title);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final CardListAdapter adapter = new CardListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mCardViewModel = ViewModelProviders.of(this).get(CardViewModel.class);

        // Add an observer on the LiveData containing the cards.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        LiveData<List<Card>> cards = mIsWhite? mCardViewModel.getAllWhiteCards() : mCardViewModel.getAllBlackCards();
        //adapter.setCards(cards.getValue());
        cards.observe(this, new Observer<List<Card>>() {
            @Override
            public void onChanged(@Nullable final List<Card> cards) {
                // Update the cached copy of the words in the adapter.
                adapter.setCards(cards);
            }
        });

        FloatingActionButton add_card = findViewById(R.id.add_card);
        add_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent = new Intent(CardsActivity.this, NewCardActivity.class);
            startActivityForResult(intent, NEW_CARD_ACTIVITY_REQUEST_CODE);
            }
        });

        FloatingActionButton edit_card = findViewById(R.id.edit_card);
        edit_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check to see if we have a selected card
                Card selectedCard = adapter.getSelectedCard();
                if (selectedCard == null) {
                    return;
                }
                Intent intent = new Intent(CardsActivity.this, NewCardActivity.class);
                intent.putExtra(NewCardActivity.CARD_TO_UPDATE, selectedCard.getId());
                intent.putExtra(NewCardActivity.CARD_TEXT, selectedCard.getText());
                startActivityForResult(intent, EDIT_CARD_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_CARD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Card card = new Card(0, data.getStringExtra(NewCardActivity.CARD_TEXT), mIsWhite);
            mCardViewModel.insert(card);
        } else if (requestCode == EDIT_CARD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Card updated_card = new Card(
                data.getIntExtra(NewCardActivity.CARD_TO_UPDATE, 0),
                data.getStringExtra(NewCardActivity.CARD_TEXT),
                mIsWhite
            );
            mCardViewModel.update(updated_card);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }
}

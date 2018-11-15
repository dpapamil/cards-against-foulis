package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.papamilios.dimitris.cardsagainstfoulis.database.Card;
import com.papamilios.dimitris.cardsagainstfoulis.database.CardRoomDatabase;

import java.util.Collections;
import java.util.List;

public class CardProvider {

    // The black cards
    private List<Card> mBlackCards;
    private int mCurBlackCardPos = 0;

    // The white cards
    private List<Card> mWhiteCards;
    private int mCurWhiteCardPos = 0;

    // Constructor
    public CardProvider(AppCompatActivity activity) {
        CardRoomDatabase cardDatabase = CardRoomDatabase.getDatabase(activity.getApplication());
        cardDatabase.cardDao().getAllBlackCards().observe(activity,  new Observer<List<Card>>() {

            @Override
            public void onChanged(@Nullable final List<Card> cards) {
            mBlackCards = randomizeCards(cards);
            }
        });
        cardDatabase.cardDao().getAllWhiteCards().observe(activity,  new Observer<List<Card>>() {

            @Override
            public void onChanged(@Nullable final List<Card> cards) {
            mWhiteCards = randomizeCards(cards);
            }
        });;
    }

    // Get the next white card
    public Card getNextWhiteCard() {
        mCurWhiteCardPos = getNextCardPosition(mCurWhiteCardPos, mWhiteCards.size());
        return mWhiteCards.get(mCurWhiteCardPos);
    }

    // Get the next black card
    public Card getNextBlackCard() {
        mCurBlackCardPos = getNextCardPosition(mCurBlackCardPos, mBlackCards.size());
        return mBlackCards.get(mCurBlackCardPos);
    }

    // Randomise the given list of cards
    private List<Card> randomizeCards(List<Card> initialCards) {
        List<Card> randomCards = initialCards;
        Collections.shuffle(randomCards);
        return randomCards;
    }

    // Get the next card position. If it is the last one, start over.
    private int getNextCardPosition(int pos, int cardsSize) {
        if (pos == cardsSize - 1) {
            return 0;
        }
        return pos + 1;
    }
}

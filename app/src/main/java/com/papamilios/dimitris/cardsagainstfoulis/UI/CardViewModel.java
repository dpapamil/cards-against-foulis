package com.papamilios.dimitris.cardsagainstfoulis.UI;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.papamilios.dimitris.cardsagainstfoulis.database.Card;
import com.papamilios.dimitris.cardsagainstfoulis.database.CardRepository;

import java.util.List;

/**
 * View Model to keep a reference to the card repository and
 * an up-to-date list of all cards.
 */

public class CardViewModel extends AndroidViewModel {

    private CardRepository mRepository;
    // Using LiveData and caching the cards has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<Card>> mWhiteCards;
    private LiveData<List<Card>> mBlackCards;

    public CardViewModel(Application application) {
        super(application);
        mRepository = new CardRepository(application);
        mWhiteCards = mRepository.getAllWhiteCards();
        mBlackCards = mRepository.getAllBlackCards();
    }

    public LiveData<List<Card>> getAllWhiteCards() {
        return mWhiteCards;
    }
    public LiveData<List<Card>> getAllBlackCards() {
        return mBlackCards;
    }

    public void insert(Card card) {
        mRepository.insert(card);
    }
    public void update(Card card) {
        mRepository.update(card);
    }

    public void deleteAll() { mRepository.deleteAll(); }
}
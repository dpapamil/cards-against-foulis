package com.papamilios.dimitris.cardsagainstfoulis;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/*
 *  The repository of the card database.
 */

class CardRepository {

    private CardDao mCardDao;
    private LiveData<List<Card>> mWhiteCards;
    private LiveData<List<Card>> mBlackCards;

    CardRepository(Application application) {
        CardRoomDatabase db = CardRoomDatabase.getDatabase(application);
        mCardDao = db.cardDao();
        mWhiteCards = mCardDao.getAllWhiteCards();
        mBlackCards = mCardDao.getAllBlackCards();
    }

    LiveData<List<Card>> getAllWhiteCards() {
        return mWhiteCards;
    }

    LiveData<List<Card>> getAllBlackCards() {
        return mBlackCards;
    }

    // You must call this on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    void insert(Card card) {
        new insertAsyncTask(mCardDao).execute(card);
    }

    private static class insertAsyncTask extends AsyncTask<Card, Void, Void> {

        private CardDao mAsyncTaskDao;

        insertAsyncTask(CardDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Card... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}

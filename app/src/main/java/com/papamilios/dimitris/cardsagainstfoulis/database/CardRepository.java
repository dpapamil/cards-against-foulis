package com.papamilios.dimitris.cardsagainstfoulis.database;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/*
 *  The repository of the card database.
 */

public class CardRepository {

    private CardDao mCardDao;
    private LiveData<List<Card>> mWhiteCards;
    private LiveData<List<Card>> mBlackCards;

    public CardRepository(Application application) {
        CardRoomDatabase db = CardRoomDatabase.getDatabase(application);
        mCardDao = db.cardDao();
        mWhiteCards = mCardDao.getAllWhiteCards();
        mBlackCards = mCardDao.getAllBlackCards();
    }

    public LiveData<List<Card>> getAllWhiteCards() {
        return mWhiteCards;
    }

    public LiveData<List<Card>> getAllBlackCards() {
        return mBlackCards;
    }

    // You must call this on a non-UI thread or your app will crash.
    // Like this, Room ensures that you're not doing any long running operations on the main
    // thread, blocking the UI.
    public void insert(Card card) {
        new insertAsyncTask(mCardDao, true).execute(card);
    }

    public void update(Card card) {
        new insertAsyncTask(mCardDao, false).execute(card);
    }

    public void deleteAll() {
        deleteAllAsyncTask deleteTask = new deleteAllAsyncTask(mCardDao);
        deleteTask.execute();
    }

    private static class insertAsyncTask extends AsyncTask<Card, Void, Void> {

        private CardDao mAsyncTaskDao;
        private boolean mInsert;

        insertAsyncTask(CardDao dao, boolean insert) {
            mAsyncTaskDao = dao;
            mInsert = insert;
        }

        @Override
        protected Void doInBackground(final Card... params) {
            if (mInsert) {
                mAsyncTaskDao.insert(params[0]);
            } else {
                mAsyncTaskDao.update(params[0]);
            }
            return null;
        }
    }

    private static class deleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private CardDao mAsyncTaskDao;

        deleteAllAsyncTask(CardDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }
}

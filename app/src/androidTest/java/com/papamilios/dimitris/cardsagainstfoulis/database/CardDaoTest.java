package com.papamilios.dimitris.cardsagainstfoulis.database;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CardDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static final String TAG = "Testing Database";

    private CardDao mCardDao;
    private CardRoomDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        mDb = Room.inMemoryDatabaseBuilder(context, CardRoomDatabase.class)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build();
        mCardDao = mDb.cardDao();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void insertWhiteCards() throws Exception {
        Card card = new Card(0, "testing card", true);
        mCardDao.insert(card);
        List<Card> allWhiteCards = LiveDataTestUtil.getValue(mCardDao.getAllWhiteCards());
        assertEquals(allWhiteCards.get(0).getText(), card.getText());
        assertTrue(allWhiteCards.get(0).isWhite());
    }

    @Test
    public void insertBlackCards() throws Exception {
        Card card = new Card(0, "testing card", false);
        mCardDao.insert(card);
        List<Card> allBlackCards = LiveDataTestUtil.getValue(mCardDao.getAllBlackCards());
        assertEquals(allBlackCards.get(0).getText(), card.getText());
        assertTrue(!allBlackCards.get(0).isWhite());
    }

    @Test
    public void getAllCards() throws Exception {
        Card whiteCard = new Card(0, "test white card", true);
        mCardDao.insert(whiteCard);
        Card blackCard = new Card(0, "test black card", false);
        mCardDao.insert(blackCard);
        List<Card> allWords = LiveDataTestUtil.getValue(mCardDao.getAllCards());
        assertEquals(allWords.get(0).getText(), whiteCard.getText());
        assertTrue(allWords.get(0).isWhite());
        assertEquals(allWords.get(1).getText(), blackCard.getText());
        assertTrue(!allWords.get(1).isWhite());
    }

    @Test
    public void deleteAll() throws Exception {
        Card whiteCard = new Card(0, "test white card", true);
        mCardDao.insert(whiteCard);
        Card blackCard = new Card(0, "test black card", false);
        mCardDao.insert(blackCard);
        mCardDao.deleteAll();
        List<Card> allCards = LiveDataTestUtil.getValue(mCardDao.getAllCards());
        assertTrue(allCards.isEmpty());
    }
}

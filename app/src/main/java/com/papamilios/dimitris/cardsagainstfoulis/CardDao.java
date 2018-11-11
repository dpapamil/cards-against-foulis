package com.papamilios.dimitris.cardsagainstfoulis;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * The class to actually do transactions with the Card table.
 */

@Dao
public interface CardDao {

    @Query("SELECT * from card_table ORDER BY id ASC")
    LiveData<List<Card>> getAllCards();

    @Query("SELECT * from card_table WHERE is_white = 1 ORDER BY id ASC")
    LiveData<List<Card>> getAllWhiteCards();

    @Query("SELECT * from card_table WHERE is_white = 0 ORDER BY id ASC")
    LiveData<List<Card>> getAllBlackCards();

    @Insert
    void insert(Card card);

    @Query("DELETE FROM card_table")
    void deleteAll();
}

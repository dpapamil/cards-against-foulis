package com.papamilios.dimitris.cardsagainstfoulis;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * The representation of a Card. This can be either a black or a white card.
 */

@Entity(tableName = "card_table")
public class Card {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "text")
    private String mText;

    @NonNull
    @ColumnInfo(name = "is_white")
    private boolean mIsWhite;

    public Card(int id, @NonNull String text, @NonNull boolean isWhite) {
        this.id = id;
        this.mText = text;
        this.mIsWhite = isWhite;
    }

    @NonNull
    public String getText() {
        return this.mText;
    }

    @NonNull
    public boolean isWhite() {
        return this.mIsWhite;
    }

    public int getId() {
        return this.id;
    }
}
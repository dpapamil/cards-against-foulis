package com.papamilios.dimitris.cardsagainstfoulis.controller;
/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;

import java.io.Serializable;

public class GamePlayer implements Serializable {
    private String mId = null;
    private String mName = null;

    public GamePlayer(@NonNull String id, @NonNull String name) {
        mId = id;
        mName = name;
    }

    public String getId() { return mId; }
    public String getName() { return mName; }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        GamePlayer otherPlayer = (GamePlayer) other;
        return mId.equals(otherPlayer.getId()) &&
                mName.equals(otherPlayer.getName());
    }
}

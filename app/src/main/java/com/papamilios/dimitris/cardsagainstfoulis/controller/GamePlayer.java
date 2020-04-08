package com.papamilios.dimitris.cardsagainstfoulis.controller;
/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;

public class GamePlayer {
    private String mId = null;
    private String mName = null;

    public GamePlayer(@NonNull String id, @NonNull String name) {
        mId = id;
        mName = name;
    }

    public String getId() { return mId; }
    public String getName() { return mName; }
}

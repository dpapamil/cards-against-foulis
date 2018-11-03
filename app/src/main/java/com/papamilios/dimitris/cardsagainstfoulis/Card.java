package com.papamilios.dimitris.cardsagainstfoulis;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 *
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
    public boolean isWhite() { return this.mIsWhite; }

    public int getId() { return this.id; }
}
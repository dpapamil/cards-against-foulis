package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;

import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardUtils {

    // Get the needed number of answers for the given black card
    public static int numberOfAnswers(@NonNull Card blackCard) {

        String answerPattern = "([^_]*[_]+)";
        Pattern pattern = Pattern.compile(answerPattern);
        Matcher matcher = pattern.matcher(blackCard.getText());
        if (matcher.find()) {
            return matcher.groupCount();
        }

        // The default is one answer
        return 1;
    }
}

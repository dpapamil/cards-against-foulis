package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;

import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardUtils {

    // Get the needed number of answers for the given black card
    public static int numberOfAnswers(@NonNull Card blackCard) {

        String answerPattern = "([^_]*[_]+)";
        Pattern pattern = Pattern.compile(answerPattern);
        Matcher matcher = pattern.matcher(blackCard.getText());
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        // The default is one answer
        if (matches == 0) {
            matches++;
        }

        return matches;
    }

    // Merge white cards into one card
    public static Card mergeWhiteCards(List<Card> cards) {
        if (cards.isEmpty()) {
            throw new AssertionError("Empty card list");
        }
        if (cards.size() == 1) {
            return cards.get(0);
        }

        int count = cards.size();
        String cardText = "";
        for (int i = 0; i < count; i++) {
            cardText += Integer.toString(i+1) + ". " + cards.get(i).getText();
            if (i != count - 1) {
                cardText += "\n";
            }
        }
        return new Card(0, cardText, true);
    }
}

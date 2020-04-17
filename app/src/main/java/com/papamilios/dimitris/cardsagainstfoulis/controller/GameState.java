package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2020 Cards Against Foulis Co.
 */

import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.papamilios.dimitris.cardsagainstfoulis.controller.RoundPhase.CHOOSING_WHITE_CARD;

public class GameState {
    // The current phase we're in
    private RoundPhase mRoundPhase = CHOOSING_WHITE_CARD;
    
    // Whether we are a Czar or not.
    // If we're not Czar, we're a filthy pleb
    private boolean mIsCzar = false;

    // The ID of the current Czar
    private String mCzarName = null;

    // The ID of the winner of the current round
    private String mWinnerName = null;

    // True when we are supposed to be waiting for other players to do something.
    private boolean mWaitingForOthers = false;

    // The list of cards that we should be displaying
    private List<Card> mDisplayedCards = new ArrayList<Card>();

    // The indices of the selected cards
    private List<Integer> mSelectedCards = new ArrayList<Integer>();

    private Integer mNumMaxSelections = 1;

    // The current black card
    private Card mBlackCard = null;

    // The score board
    private Map<String, Integer> mScoreboard = new HashMap<String, Integer>();



    // ====================  FUNCTIONS ======================================
    // Accessors for the round phase
    public RoundPhase getRoundPhase() { return mRoundPhase; }
    public void setRoundPhase(@NonNull RoundPhase phase) { mRoundPhase = phase; }

    // Accessors for the Czar's name
    public String getCzarName() { return mCzarName; }
    public void setCzarName(@NonNull String czarName) { mCzarName = czarName; }
    public boolean isCzar() { return mIsCzar; }
    public void setIsCzar(@NonNull boolean isCzar) { mIsCzar = isCzar; }

    // Accessors for the winner's name
    public String getWinnerName() { return mWinnerName; }
    public void setWinnerName(String winnerName) { mWinnerName = winnerName; }

    // Accessors for waiting for others
    public boolean waitingForOthers() { return mWaitingForOthers; }
    public void setWaitingForOthers(@NonNull boolean waiting) { mWaitingForOthers = waiting; }

    // Accessors for the displayed cards
    public List<Card> getDisplayedCards() { return mDisplayedCards; }
    public void setDisplayedCards(@NonNull List<Card> displayedCards) { mDisplayedCards = displayedCards; }
    public List<Card> getSelectedCards() {
        List<Card> selectedCards = new ArrayList<Card>();
        for (Integer i : mSelectedCards) {
            selectedCards.add(mDisplayedCards.get(i));
        }

        return selectedCards;
    }
    public void setSelectedCards(@NonNull List<Card> selectedCards) {
        mSelectedCards.clear();
        for (Card selectedCard : selectedCards) {
            Integer index = mDisplayedCards.indexOf(selectedCard);
            if (index >= 0) {
                mSelectedCards.add(index);
            }
        }
    }
    public void clearCardSelection() {
        mSelectedCards.clear();
    }

    // Accessors for maximum number of selection allowed
    public Integer getNumMaxSelection() { return mNumMaxSelections; }
    public void setmNumMaxSelections(Integer num) { mNumMaxSelections = num; }

    // Accessors for black card
    public Card getBlackCard() { return mBlackCard; }
    public void setBlackCard(@NonNull Card card) { mBlackCard = card; }

    // Accessors for scoreboard
    public Map<String, Integer> getScoreboard() { return mScoreboard; }
    // Increase the score of the given user.
    public void increaseScore(@NonNull String userName) {
        if (!mScoreboard.containsKey(userName)) {
            return;
        }

        mScoreboard.put(userName, mScoreboard.get(userName) + 1);
    }

    // Initialise scoreboard
    public void initialiseScoreboard(@NonNull List<String> userNames) {
        mScoreboard.clear();
        for (String userName : userNames) {
            mScoreboard.put(userName, 0);
        }
    }
}

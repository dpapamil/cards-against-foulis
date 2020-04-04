package com.papamilios.dimitris.cardsagainstfoulis.controller;

// This represents the 3 phases of one round
public enum RoundPhase {
    CHOOSING_WHITE_CARD,  // The initial phase of the round when players choose their answer cards
    CHOOSING_WINNER_CARD, // The second phase of the round when the Czar chooses the winning card
    WINNER                // The last phase when we display who won the round and the scores
}
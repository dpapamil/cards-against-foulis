package com.papamilios.dimitris.cardsagainstfoulis.UI.LeaderBoard;

import androidx.annotation.NonNull;

import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;

public class PlayerStats {
    private GamePlayer mPlayer = null;
    private int mPoints = 0;
    private int mGamesPlayed = 0;
    private int mWins = 0;

    public PlayerStats(@NonNull GamePlayer player) {
        mPlayer = player;
    }

    public GamePlayer getPlayer() { return mPlayer; }

    public int getPoints() { return mPoints; }
    public void setPoints(int points) { mPoints = points; }

    public int getGamesPlayed() { return mGamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { mGamesPlayed = gamesPlayed; }

    public int getWins() { return mWins; }
    public void setWins(int wins) { mWins = wins; }

    public double getWinPercentage() { return (100.0 * (double) mWins) / (double) mGamesPlayed; }
}

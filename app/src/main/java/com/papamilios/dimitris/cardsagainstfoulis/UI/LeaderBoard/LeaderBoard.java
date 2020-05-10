package com.papamilios.dimitris.cardsagainstfoulis.UI.LeaderBoard;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoard {
    private List<PlayerStats> mPlayerStats = new ArrayList<>();

    public void addPlayerStats(@NonNull PlayerStats stats) {
        // Check if teh given player exists already
        for (PlayerStats playerStats : mPlayerStats) {
            if (playerStats.getPlayer().getId().equals(stats.getPlayer().getId())) {
                return;
            }
        }

        mPlayerStats.add(stats);
        sort();
    }

    public int count() {return mPlayerStats.size(); }

    public PlayerStats getPlayerStats(int index) {
        if (index < 0 || index >= count()) {
            return null;
        }

        return mPlayerStats.get(index);
    }

    // Sort the leaderboard
    private void sort() {
        Collections.sort(mPlayerStats, new Comparator<PlayerStats>() {
            @Override
            public int compare(PlayerStats p1, PlayerStats p2) {
                int pointsDiff = p2.getPoints() - p1.getPoints();
                if (pointsDiff != 0) {
                    return pointsDiff;
                }

                // Check the number of wins
                int winsDiff = p2.getWins() - p1.getWins();
                if (winsDiff != 0) {
                    return winsDiff;
                }

                // If all else fails, check win percentage
                int gamesDiff = p1.getGamesPlayed() - p2.getGamesPlayed();
                return gamesDiff;
            }
        });
    }
}

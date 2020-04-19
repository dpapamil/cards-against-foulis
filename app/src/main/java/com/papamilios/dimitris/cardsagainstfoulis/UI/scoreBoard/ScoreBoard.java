package com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard;

import android.os.Build;

import androidx.annotation.NonNull;

import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreBoard {
    private LinkedHashMap<GamePlayer, Integer> mScoreBoard = new LinkedHashMap<GamePlayer, Integer>();

    public void initialiseBoard(@NonNull List<GamePlayer> players) {
        mScoreBoard.clear();
        for (GamePlayer player : players) {
            mScoreBoard.put(player, 0);
        }
    }

    public void copyFrom(@NonNull final ScoreBoard board) {
        mScoreBoard.clear();
        mScoreBoard.putAll(board.mScoreBoard);
    }

    public int playerCount() {
        return mScoreBoard.size();
    }

    public Integer getScore(int index) {
        return (Integer) mScoreBoard.values().toArray()[index];
    }

    public GamePlayer getPlayer(int index) {
        return (GamePlayer) (mScoreBoard.keySet().toArray())[index];
    }

    // Increase the score of the given player
    public void increasePlayerScore(@NonNull GamePlayer player) {
        mScoreBoard.put(player, mScoreBoard.get(player) + 1);
        sort();
    }

    public int getPlayerPosition(@NonNull String playerName) {
        Object[] playersArray = mScoreBoard.keySet().toArray();
        for (int i = 0; i < playersArray.length; i++) {
            GamePlayer player = (GamePlayer) playersArray[i];
            if (player.getName().equals(playerName)) {
                return i;
            }
        }

        return -1;
    }

    // Sort the score board by the scores
    private void sort() {
        LinkedHashMap<GamePlayer, Integer> sortedBoard = new LinkedHashMap<GamePlayer, Integer>();
        mScoreBoard.
            entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            forEachOrdered(x -> sortedBoard.put(x.getKey(), x.getValue()));

        mScoreBoard.clear();
        mScoreBoard.putAll(sortedBoard);
    }

}

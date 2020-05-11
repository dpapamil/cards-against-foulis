package com.papamilios.dimitris.cardsagainstfoulis;

import com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard.ScoreBoard;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreBoardTest {

    @Test
    public void testScoreBoardSorting() {
        ScoreBoard board = new ScoreBoard();
        assertEquals(board.playerCount(), 0);

        // Initialis the board with some players
        List<GamePlayer> players = Arrays.asList(
                new GamePlayer("1", "player1"),
                new GamePlayer("2", "player2"),
                new GamePlayer("3", "player3")
        );
        board.initialiseBoard(players);
        assertEquals(board.playerCount(), 3);

        // Increase the score of the second player
        board.increasePlayerScore(players.get(1));
        assertEquals(board.getPlayer(0), players.get(1));
        assertEquals(board.getPlayer(1), players.get(0));
        assertEquals(board.getPlayer(2), players.get(2));
        assertEquals(board.getPlayerPosition(players.get(1).getName()), 0);
        assertEquals(board.getPlayerPosition(players.get(0).getName()), 1);
        assertEquals(board.getPlayerPosition(players.get(2).getName()), 2);
        assertEquals(board.getScore(0).intValue(), 1);
        assertEquals(board.getScore(1).intValue(), 0);
        assertEquals(board.getScore(2).intValue(), 0);
        assertEquals(board.getScore(players.get(1).getName()).intValue(), 1);
        assertEquals(board.getScore(players.get(0).getName()).intValue(), 0);
        assertEquals(board.getScore(players.get(2).getName()).intValue(), 0);

        // Increase the score of the third player to 4
        board.increasePlayerScore(players.get(2));
        board.increasePlayerScore(players.get(2));
        board.increasePlayerScore(players.get(2));
        board.increasePlayerScore(players.get(2));
        assertEquals(board.getPlayer(0), players.get(2));
        assertEquals(board.getPlayer(1), players.get(1));
        assertEquals(board.getPlayer(2), players.get(0));
        assertEquals(board.getPlayerPosition(players.get(2).getName()), 0);
        assertEquals(board.getPlayerPosition(players.get(1).getName()), 1);
        assertEquals(board.getPlayerPosition(players.get(0).getName()), 2);
        assertEquals(board.getScore(0).intValue(), 4);
        assertEquals(board.getScore(1).intValue(), 1);
        assertEquals(board.getScore(2).intValue(), 0);
        assertEquals(board.getScore(players.get(2).getName()).intValue(), 4);
        assertEquals(board.getScore(players.get(1).getName()).intValue(), 1);
        assertEquals(board.getScore(players.get(0).getName()).intValue(), 0);
    };
}

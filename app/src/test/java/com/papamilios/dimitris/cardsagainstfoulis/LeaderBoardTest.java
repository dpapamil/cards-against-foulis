package com.papamilios.dimitris.cardsagainstfoulis;

import com.papamilios.dimitris.cardsagainstfoulis.UI.leaderBoard.LeaderBoard;
import com.papamilios.dimitris.cardsagainstfoulis.UI.leaderBoard.PlayerStats;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LeaderBoardTest {

    @Test
    public void testLeaderBoardSorting() {
        LeaderBoard board = new LeaderBoard();
        assertEquals(0, board.count());

        // Create some player stats to add to the board
        List<PlayerStats> stats = createPlayerStatsData();
        board.addPlayerStats(stats.get(0));
        assertEquals(1, board.count());

        board.addPlayerStats(stats.get(1));
        assertEquals(2, board.count());
        assertEquals(stats.get(1), board.getPlayerStats(0));
        assertEquals(stats.get(0), board.getPlayerStats(1));

        board.addPlayerStats(stats.get(2));
        assertEquals(3, board.count());
        assertEquals(stats.get(1), board.getPlayerStats(0));
        assertEquals(stats.get(0), board.getPlayerStats(1));
        assertEquals(stats.get(2), board.getPlayerStats(2));

        board.addPlayerStats(stats.get(3));
        assertEquals(4, board.count());
        assertEquals(stats.get(1), board.getPlayerStats(0));
        assertEquals(stats.get(3), board.getPlayerStats(1));
        assertEquals(stats.get(0), board.getPlayerStats(2));
        assertEquals(stats.get(2), board.getPlayerStats(3));
    }

    // Create a list of player stats for the test
    // The order base on score should be {1, 3, 0, 2}
    private List<PlayerStats> createPlayerStatsData() {
        List<PlayerStats> stats = new ArrayList<>();

        PlayerStats p1Stats = new PlayerStats(new GamePlayer("1", "player1"));
        p1Stats.setPoints(10);
        p1Stats.setWins(2);
        p1Stats.setGamesPlayed(5);
        stats.add(p1Stats);
        
        PlayerStats p2Stats = new PlayerStats(new GamePlayer("2", "player2"));
        p2Stats.setPoints(35);
        p2Stats.setWins(1);
        p2Stats.setGamesPlayed(8);
        stats.add(p2Stats);

        PlayerStats p3Stats = new PlayerStats(new GamePlayer("3", "player3"));
        p3Stats.setPoints(10);
        p3Stats.setWins(2);
        p3Stats.setGamesPlayed(6);
        stats.add(p3Stats);

        PlayerStats p4Stats = new PlayerStats(new GamePlayer("4", "player4"));
        p4Stats.setPoints(10);
        p4Stats.setWins(3);
        p4Stats.setGamesPlayed(5);
        stats.add(p4Stats);

        return stats;
    }
}

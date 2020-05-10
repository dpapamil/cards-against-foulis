package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.LeaderBoard.LeaderBoard;
import com.papamilios.dimitris.cardsagainstfoulis.UI.LeaderBoard.PlayerStats;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;


public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        LeaderBoard leaderboard = new LeaderBoard();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        ValueEventListener userScoreListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapshot) {
                for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.child("displayName").getValue().toString();
                    GamePlayer player = new GamePlayer(userId, userName);
                    if (!userSnapshot.hasChild("allTimeScore")) {
                        continue;
                    }

                    PlayerStats stats = new PlayerStats(player);
                    int points = Integer.parseInt(userSnapshot.child("allTimeScore").getValue().toString());
                    int wins = Integer.parseInt(userSnapshot.child("wins").getValue().toString());
                    int gamesPlayed = Integer.parseInt(userSnapshot.child("gamesPlayed").getValue().toString());
                    stats.setPoints(points);
                    stats.setWins(wins);
                    stats.setGamesPlayed(gamesPlayed);

                    leaderboard.addPlayerStats(stats);
                }

                initialiseTable(leaderboard);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        usersRef.addListenerForSingleValueEvent(userScoreListener);
    }

    // Create the table for the leader board
    private void initialiseTable(@NonNull LeaderBoard board) {
        TableLayout table = (TableLayout) findViewById(R.id.table_leader_board);

        // Create the header row
        TableRow headerRow = new TableRow(this);
        headerRow.setShowDividers(TableRow.SHOW_DIVIDER_MIDDLE);
        TextView headerRank = new TextView(this);
        headerRank.setTextAppearance(R.style.ScoreText);
        headerRank.setText(R.string.rank);
        headerRow.addView(headerRank);
        TextView headerName = new TextView(this);
        headerName.setTextAppearance(R.style.ScoreText);
        headerName.setText(R.string.player_name);
        headerRow.addView(headerName);
        TextView headerPoints = new TextView(this);
        headerPoints.setTextAppearance(R.style.ScoreText);
        headerPoints.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
        headerPoints.setText(R.string.points);
        headerRow.addView(headerPoints);
        TextView headerWins = new TextView(this);
        headerWins.setTextAppearance(R.style.ScoreText);
        headerWins.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
        headerWins.setText(R.string.wins);
        headerRow.addView(headerWins);
        TextView headerGP = new TextView(this);
        headerGP.setTextAppearance(R.style.ScoreText);
        headerGP.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
        headerGP.setText(R.string.games_played);
        headerRow.addView(headerGP);
        TextView headerPercent = new TextView(this);
        headerPercent.setTextAppearance(R.style.ScoreText);
        headerPercent.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        headerPercent.setText(R.string.percentage);
        headerRow.addView(headerPercent);

        table.addView(headerRow);

        // Now add the players from first to last
        for (int i = 0; i < board.count(); i++) {
            PlayerStats stats = board.getPlayerStats(i);

            TableRow row = new TableRow(this);
            row.setShowDividers(TableRow.SHOW_DIVIDER_MIDDLE);

            int textStyle = i == 0? R.style.BoldScoreText : R.style.ScoreText;
            TextView rank = new TextView(this);
            rank.setTextAppearance(textStyle);
            rank.setText(String.format("%d.", i + 1));
            row.addView(rank);
            TextView name = new TextView(this);
            name.setTextAppearance(textStyle);
            name.setText(stats.getPlayer().getName());
            row.addView(name);
            TextView points = new TextView(this);
            points.setTextAppearance(textStyle);
            points.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
            points.setText(Integer.toString(stats.getPoints()));
            row.addView(points);
            TextView wins = new TextView(this);
            wins.setTextAppearance(textStyle);
            wins.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
            wins.setText(Integer.toString(stats.getWins()));
            row.addView(wins);
            TextView gp = new TextView(this);
            gp.setTextAppearance(textStyle);
            gp.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
            gp.setText(Integer.toString(stats.getGamesPlayed()));
            row.addView(gp);
            TextView percent = new TextView(this);
            percent.setTextAppearance(textStyle);
            percent.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
            percent.setText(String.format("%.0f%%", stats.getWinPercentage()));
            row.addView(percent);

            table.addView(row);
        }
    }
}

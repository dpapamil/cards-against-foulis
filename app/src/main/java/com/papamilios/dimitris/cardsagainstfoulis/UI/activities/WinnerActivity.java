package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard.ScoreBoard;
import com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard.ScoreBoardAdapter;

import java.util.List;

public class WinnerActivity extends AppCompatActivity {

    private ScoreBoard mScoreboard = null;
    private ScoreBoardAdapter mScoreBoardAdapter = null;
    private MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        Intent intent = getIntent();
        mScoreboard = (ScoreBoard)intent.getSerializableExtra(GameActivity.SCOREBOARD);
        String gameId = intent.getStringExtra(GameActivity.GAME_ID);

        if (gameId != null) {
            // Remove the game we just finished
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference gameRef = database.getReference("games").child(gameId);
            gameRef.removeValue();
        }

        RecyclerView recyclerScoreBoardView = findViewById(R.id.score_board);
        mScoreBoardAdapter = new ScoreBoardAdapter(this);
        recyclerScoreBoardView.setAdapter(mScoreBoardAdapter);
        recyclerScoreBoardView.setLayoutManager(new LinearLayoutManager(this));
        mScoreBoardAdapter.initialiseScoreBoard(mScoreboard);

        int mediaResource = R.raw.loser;
        String username = getUserName();
        int finishingPosition = mScoreboard.getPlayerPosition(username);
        if (finishingPosition == 0) {
            ImageView image = (ImageView)findViewById(R.id.winnerImage);
            image.setImageResource(R.drawable.winner);
            mediaResource = R.raw.winner;
        }
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), mediaResource);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    // Activity is going to the background. We have to stop the player.
    @Override
    public void onStop() {
        mMediaPlayer.stop();
        super.onStop();
    }

    // Activity is going to the background. We have to stop the player.
    @Override
    public void onPause() {
        mMediaPlayer.stop();
        super.onPause();
    }


    // Activity is being resumed
    @Override
    public void onResume() {
        mMediaPlayer.start();
        super.onResume();
    }

    private String getUserName() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userName = firebaseAuth.getCurrentUser().getDisplayName();
        List<UserInfo> userInfos = (List<UserInfo>) firebaseAuth.getCurrentUser().getProviderData();
        for (UserInfo userInfo : userInfos) {
            if (userInfo.getProviderId().equals("playgames.google.com")) {
                userName = userInfo.getDisplayName();
            }
        }
        return userName;
    }

    public void onGoHome(View view) {
        mMediaPlayer.stop();
        finish();
    }
}

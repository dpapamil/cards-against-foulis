package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardViewModel;
import com.papamilios.dimitris.cardsagainstfoulis.UI.OnSwipeTouchListener;
import com.papamilios.dimitris.cardsagainstfoulis.UI.chat.ChatMessageListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.UI.games.GameInfo;
import com.papamilios.dimitris.cardsagainstfoulis.UI.games.GamesListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard.ScoreBoardAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GameController;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GameState;
import com.papamilios.dimitris.cardsagainstfoulis.controller.RoundPhase;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;
import com.papamilios.dimitris.cardsagainstfoulis.database.FirebaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*
*  The activity to hold the game screen.
 */

public class GameActivity extends AppCompatActivity implements GamesListAdapter.GameSelectionListener {

    final static String TAG = "CardsAgainstFoulis";

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    public static final String SCOREBOARD = "com.papamilios.dimitris.cardsagainstfoulis.SCOREBOARD";
    public static final String GAME_ID = "com.papamilios.dimitris.cardsagainstfoulis.GAME_ID";

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;
    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private GoogleSignInAccount mSignedInAccount = null;

    // The Firebase authentication object
    private FirebaseAuth mFirebaseAuth = null;

    private GameController mController;

    private boolean mJoining = false;
    private String mGameId = null;
    private boolean mGameStarted = false;
    private Button mJoinGameBtn = null;

    // The white card view model
    private CardViewModel mCardViewModel = null;
    private CardListAdapter mCardsAdapter = null;
    private ScoreBoardAdapter mScoreBoardAdapter = null;

    // The games list adapter
    private GamesListAdapter mGamesListAdapter = null;

    // Chat messages
    private ChatMessageListAdapter mChatMessageAdapter = null;
    private boolean mInChat = false;

    // The listener for changing users for the game
    private ValueEventListener mUsersListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get the users
            String joinedUsers = "";
            List<GamePlayer> players = new ArrayList<GamePlayer>();
            List<String> playersNames = new ArrayList<String>();
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                String userName = userSnapshot.child("name").getValue().toString();
                joinedUsers += userName + "\n";
                playersNames.add(userName);
                players.add(new GamePlayer(userSnapshot.getKey(), userName));
            }

            if (mGameStarted) {
                List<GamePlayer> curPlayers = mController.getPlayers();
                if (players.size() > curPlayers.size()) {
                    throw new AssertionError("That probably means that a player was added after the game was started?");
                }
                List<String> losers = new ArrayList<String>();
                for (GamePlayer player : curPlayers) {
                    if (!players.contains(player)) {
                        losers.add(player.getName());
                    }
                }

                if (!losers.isEmpty()) {
                    mController.onPlayersLeft(losers);
                    return;
                }
            } else {
                mController.setPlayers(players);
                if (mJoining) {
                    mGamesListAdapter.updateGamePlayers(mGameId, playersNames);
                } else {
                    setTextToView(R.id.joined_players, joinedUsers);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadUsers:onCancelled", databaseError.toException());
        }
    };

    private ValueEventListener mGamesListener = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        switchToScreen(R.id.screen_wait);

        RecyclerView recyclerView = findViewById(R.id.white_cards);
        mCardsAdapter = new CardListAdapter(this);
        recyclerView.setAdapter(mCardsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mCardViewModel = ViewModelProviders.of(this).get(CardViewModel.class);

        RecyclerView recyclerChatView = findViewById(R.id.reyclerview_message_list);
        mChatMessageAdapter = new ChatMessageListAdapter(this);
        recyclerChatView.setAdapter(mChatMessageAdapter);
        LinearLayoutManager chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setStackFromEnd(true);
        recyclerChatView.setLayoutManager(chatLayoutManager);

        RecyclerView recyclerGamesView = findViewById(R.id.games_to_join);
        mGamesListAdapter = new GamesListAdapter(this);
        recyclerGamesView.setAdapter(mGamesListAdapter);
        recyclerGamesView.setLayoutManager(new LinearLayoutManager(this));
        mGamesListAdapter.addListener(this);

        RecyclerView recyclerScoreBoardView = findViewById(R.id.score_board);
        mScoreBoardAdapter = new ScoreBoardAdapter(this);
        recyclerScoreBoardView.setAdapter(mScoreBoardAdapter);
        recyclerScoreBoardView.setLayoutManager(new LinearLayoutManager(this));

        mController = new GameController(this);

        // Create the client used to sign in.
        GoogleSignInOptions gso = new GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sign in silently
        signInSilently();

        // Check to see if we are starting the game or we're joining a game
        Intent intent = getIntent();
        mJoining = intent.getBooleanExtra(MainActivity.JOINING, false);

        // Hide chat
        showPreservingView(R.id.got_message, false);
        showView(R.id.in_app_chat, false);

        OnSwipeTouchListener swipeListener = new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeRight() {
                hideChat();
            }

            public void onSwipeLeft() {
                showChat();
            }
        };

        findViewById(R.id.in_app_chat).setOnTouchListener(swipeListener);
        findViewById(R.id.screen_game).setOnTouchListener(swipeListener);
        findViewById(R.id.reyclerview_message_list).setOnTouchListener(swipeListener);
    }

    public void onShowChat(View view) {
        showChat();
    }

    private void showChat() {
        if (mInChat) {
            return;
        }

        mInChat = true;
        showPreservingView(R.id.got_message, false);
        View view = findViewById(R.id.in_app_chat);
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
            view.getWidth(),
            0,
            0,
            0);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                showView(R.id.screen_game, false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void hideChat() {
        if (!mInChat) {
            return;
        }

        mInChat = false;
        View view = findViewById(R.id.in_app_chat);
        TranslateAnimation animate = new TranslateAnimation(
            0,
            view.getWidth(),
            0,
            0);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.in_app_chat).clearAnimation();
                showView(R.id.in_app_chat, false);
                showView(R.id.screen_game, true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    @Override
    public void onBackPressed() {
        if (mInChat) {
            hideChat();
        } else {
            new AlertDialog.Builder(GameActivity.this)
                .setTitle("Really?")
                .setMessage("Are you sure you want to exit?\nIf you exit, Foulis will clog your toilet...")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // Remove ourselves from the game
                        if (mGameId != null) {
                            DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("games").child(mGameId);
                            DatabaseReference userRef = gameRef.child("users/" + mController.getMyId());
                            userRef.removeValue();
                            mGameId = null;
                        }
                        GameActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();

        if (mGameStarted) {
            keepScreenOn();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    // Handler for getting the next black card, ie starting the next round
    public void onGetNextRound(View view) {
        mController.endRound();
    }

    // Handler for choosing a white card
    public void onChooseWhiteCard(View view) {
        List<Card> chosenCards = mCardsAdapter.getSelectedCards();
        if (chosenCards.isEmpty()) {
            // TODO: show some kind of error here, or disable button
        } else {
            mController.chooseCards(chosenCards);
        }
    }

    // Handler for swapping all white cards we hold currently
    public void onSwapCards(View view) {
        mController.swapCards();
    }

    // Handler for closing the room event popup
    public void onCloseRoomEventPopup(View view) {
        showView(R.id.room_event_popup, false);
    }

    // Handler for sending a chat message
    public void onSendChatMessage(View view) {
        TextView msgView = (TextView) findViewById(R.id.edittext_chatbox);
        String msg = msgView.getText().toString();
        if (msg != null && !msg.isEmpty()) {
            msgView.setText("");
            mController.sendChatMessage(msg);
        }
    }

    // Event handler for clicking the Sign In button
    public void onSignIn(View view) {
        // start the sign-in flow
        Log.d(TAG, "Sign-in button clicked");
        startSignInIntent();
    }

    public void onStartGame(View view) {
        EditText goalView = (EditText) findViewById(R.id.up_to);
        int goal = Integer.parseInt(goalView.getText().toString());
        mController.setGoalScore(goal);

        HashMap<String, Object> gameState = new HashMap<String, Object>();
        gameState.put("up_to", goal);
        gameState.put("started", true);

        switchToMainScreen();
        keepScreenOn();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference("games").child(mGameId);
        gameRef.child("state").updateChildren(gameState);

        mGameStarted = true;
        mScoreBoardAdapter.initialiseScoreBoard(mController.getPlayers());
        mController.startGame(mGameId);
    }

    public void onJoinGame(View view) {

        GameInfo selectedGame = mGamesListAdapter.getSelectedGame();
        if (selectedGame == null) {
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("games").removeEventListener(mGamesListener);

        mGameId = selectedGame.getId();
        showView(R.id.join_game, false);
        mGamesListAdapter.setGames(Arrays.asList(selectedGame));
        mGamesListAdapter.setSelected(mGameId);
        mGamesListAdapter.enableSelection(false);

        // Add the user to the game object
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("games").child(mGameId);

        String userId = mFirebaseAuth.getCurrentUser().getUid();
        mController.setMyId(userId);

        FirebaseUtils.getData(gameRef.child("host/id"), (data) -> {
            // Get the host
            mController.setHostId(data.getValue().toString());
        });

        gameRef.child("users").addValueEventListener(mUsersListener);

        gameRef.child("users/" + userId + "/name").setValue(getUserName());

        ValueEventListener startedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mGameStarted && dataSnapshot.child("started").getValue().equals(true)) {
                    mController.setGoalScore(Integer.parseInt(dataSnapshot.child("up_to").getValue().toString()));
                    switchToMainScreen();
                    keepScreenOn();
                    mGameStarted = true;
                    mScoreBoardAdapter.initialiseScoreBoard(mController.getPlayers());
                    mController.startGame(mGameId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException());
            }
        };
        gameRef.child("state").addValueEventListener(startedListener);

    }

    public void createGame() {
        switchToScreen(R.id.screen_create_game);

        // Create a game
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = database.getReference("games");

        // Create the new game and push it to the games stack
        DatabaseReference gameRef = gamesRef.push();
        mGameId = gameRef.getKey();
        String userId = mFirebaseAuth.getCurrentUser().getUid();
        HashMap<String, Object> gameInformation = new HashMap<String, Object>();
        gameInformation.put("users/" + userId + "/name", getUserName());
        gameInformation.put("host/id", userId);
        gameInformation.put("host/displayName", getUserName());
        gameInformation.put("state/started", false);
        gameInformation.put("state/up_to", 10);
        gameInformation.put("created", new Date());
        gameRef.updateChildren(gameInformation);
        mController.setHostId(userId);
        mController.setMyId(userId);

        // Populate the game ID edit text
        EditText gameIdTextView = (EditText) findViewById(R.id.game_id);
        gameIdTextView.setText(mGameId);
        gameIdTextView.setInputType(InputType.TYPE_NULL);
        gameIdTextView.setTextIsSelectable(true);

        gameRef.child("users").addValueEventListener(mUsersListener);
    }

    private String getUserName() {
        String userName = mFirebaseAuth.getCurrentUser().getDisplayName();
        List<UserInfo> userInfos = (List<UserInfo>) mFirebaseAuth.getCurrentUser().getProviderData();
        for (UserInfo userInfo : userInfos) {
            if (userInfo.getProviderId().equals("playgames.google.com")) {
                userName = userInfo.getDisplayName();
            }
        }
        return userName;
    }

    public void joinGame() {
        findViewById(R.id.join_game).setEnabled(false);

        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference().child("games");
        mGamesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mGameId != null) {
                    return;
                }
                List<GameInfo> availableGames = new ArrayList<GameInfo>();
                for (DataSnapshot game : dataSnapshot.getChildren()) {
                    if (game.child("state/started").getValue().equals(true)) {
                        continue;
                    }

                    String gameId = game.getKey();
                    String hostId = game.child("host/id").getValue().toString();
                    String hostName = game.child("host/displayName").getValue().toString();
                    Date dateCreated = game.child("created").getValue(Date.class);
                    List<String> currentPlayers = new ArrayList<String>();
                    for (DataSnapshot user : game.child("users").getChildren()) {
                        String userName = user.child("name").getValue().toString();
                        currentPlayers.add(userName);
                    }
                    GameInfo gameInfo = new GameInfo(gameId, hostName, dateCreated);
                    gameInfo.setPlayers(currentPlayers);
                    availableGames.add(gameInfo);
                }
                mGamesListAdapter.setGames(availableGames);

                switchToScreen(R.id.screen_join_game);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "hostLoad:onCancelled", databaseError.toException());
            }
        };
        gamesRef.addValueEventListener(mGamesListener);
    }

    public void addChatMessage(@NonNull ChatMessage chatMsg) {
        mChatMessageAdapter.addMessage(chatMsg);
        showPreservingView(R.id.got_message, !mInChat);
        RecyclerView recyclerChatView = findViewById(R.id.reyclerview_message_list);
        recyclerChatView.smoothScrollToPosition(mChatMessageAdapter.getItemCount() - 1);
    }

    // End the game
    public void endGame(GameState state) {
        Intent intent = new Intent(GameActivity.this, WinnerActivity.class);
        intent.putExtra(SCOREBOARD, state.getScoreboard());
        if (!mJoining) {
            intent.putExtra(GAME_ID, mGameId);
        }
        finish();
        startActivity(intent);
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
            new OnCompleteListener<GoogleSignInAccount>() {
                @Override
                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInSilently(): success");
                    onConnected(task.getResult());
                } else {
                    Log.d(TAG, "signInSilently(): failure", task.getException());
                    onDisconnected();
                }
                }
            });
    }

    public void signOut() {
        Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
            new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "signOut(): success");
                }

                onDisconnected();
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // stop trying to keep the screen on
        stopKeepingScreenOn();


        super.onStop();
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;


            // Call this both in the silent sign-in task's OnCompleteListener and in the
            // Activity's onActivityResult handler.
            Log.d(TAG, "firebaseAuthWithPlayGames:" + googleSignInAccount.getId());

            mFirebaseAuth = FirebaseAuth.getInstance();
            AuthCredential credential = PlayGamesAuthProvider.getCredential(googleSignInAccount.getServerAuthCode());
            mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                            userRef.child("displayName").setValue(getUserName());
                            if (mGameId == null) {
                                if (mJoining) {
                                    joinGame();
                                } else {
                                    createGame();
                                }
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
        }
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        switchToMainScreen();
    }

    // Show error message about game being cancelled and return to main screen.
    public void showGameError() {
        new AlertDialog.Builder(this)
            .setMessage(getString(R.string.game_problem))
            .setNeutralButton(android.R.string.ok, null).create();

        switchToMainScreen();
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    // Update the black card
    public void updateBlackCardView(@NonNull String blackCardText) {
        TextView blackCardView = findViewById(R.id.cur_black_card);
        blackCardView.setText(blackCardText);
    }

    // Add a white card
    public void updateWhiteCardsView(@NonNull List<Card> whiteCards) {
        mCardsAdapter.setCards(whiteCards);
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
        R.id.screen_game,
        R.id.screen_sign_in2,
        R.id.screen_wait,
        R.id.screen_create_game,
        R.id.screen_join_game
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            showView(id, screenId == id);
        }
        mCurScreen = screenId;
    }

    void switchToMainScreen() {
        switchToScreen(R.id.screen_game);
    }

    // Show the next round button
    public void showNextRoundButton(boolean show) {
        showView(R.id.next_round, show);
    }

    // Show the choose white card button
    public void showChooseCard(boolean show) {
        showView(R.id.choose_white_card, show);
    }

    // Show the swap cards button
    private void showSwapCards(boolean show) {
        showPreservingView(R.id.swap_cards, show);
    }

    // Show the white cards
    public void showWhiteCards(boolean show) {
        showView(R.id.white_cards, show);
    }

    // Set the given card as selected
    public void selectWhiteCard(@NonNull String cardText) {
        mCardsAdapter.setSelected(cardText);
    }

    // Show the scoreboard
    public void showScoreboard(boolean show) {
        if (show) {
            View scoreBoardView = findViewById(R.id.score_board);
            if (scoreBoardView.getVisibility() == View.VISIBLE) {
                return;
            }
            scoreBoardView.setVisibility(View.VISIBLE);

            showView(R.id.score_board, true);
            TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                scoreBoardView.getHeight(),
                0
            );
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animate.setDuration(500);
            animate.setFillAfter(true);
            scoreBoardView.startAnimation(animate);
        } else {
            View scoreBoardView = findViewById(R.id.score_board);
            if (scoreBoardView.getVisibility() == View.GONE) {
                return;
            }

            showView(R.id.score_board, true);
            TranslateAnimation animate = new TranslateAnimation(
                    0,
                    0,
                    0,
                    scoreBoardView.getHeight()
            );
            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    findViewById(R.id.score_board).clearAnimation();
                    showView(R.id.score_board, false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animate.setDuration(500);
            animate.setFillAfter(true);
            scoreBoardView.startAnimation(animate);
        }
    }

    // Enable/disable selecting white cards via clicking
    public void setWhiteCardsSelection(int numOfAllowed, boolean enable) {
        mCardsAdapter.setAllowedSelections(numOfAllowed);
        mCardsAdapter.enableSelection(enable);
    }

    // Clear the cards selection
    public void clearWhiteCardsSelection() {
        mCardsAdapter.clearSelection();
    }

    // Show the wait for others text
    public void showWaitForOthers(boolean show) {
        showView(R.id.wait_others, show);
    }

    // Show the message above the black card
    public void showMsgAboveBlackCard(boolean show, String text, String important) {
        setTextToView(R.id.above_card_msg, text);
        showView(R.id.above_card_msg, show);
        if (show && !important.isEmpty()) {
            setTextToView(R.id.above_card_important, important);
            showView(R.id.above_card_important, true);
        } else {
            showView(R.id.above_card_important, false);
        }
    }

    // Show the room event popup with the given text
    public void showRoomEvent(String text) {
        setTextToView(R.id.room_event_popup_text, text);
        showView(R.id.room_event_popup, true);
        showView(R.id.in_app_chat, false);
    }

    // Show/hide the given view
    public void showView(int viewId, boolean show) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setVisibility(show? View.VISIBLE : View.GONE);
    }

    private void showPreservingView(int viewId, boolean show) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setVisibility(show? View.VISIBLE : View.INVISIBLE);
    }

    // Set the given text to the view of the given id
    public void setTextToView(int viewId, String text) {
        TextView view = (TextView) findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setText(text);
    }

    @Override
    public void onSelectionChanged() {
        View joinGameBtn = findViewById(R.id.join_game);
        joinGameBtn.setEnabled(mGamesListAdapter.getSelectedGame() != null);
    }

    // Get the specified resource string
    public String getResourceString(int id) {
        return getResources().getString(id);
    }

    // Update the view
    // This is the control function that does the view update based on the given
    // state of the game
    public void update(GameState gameState) {
        RoundPhase phase = gameState.getRoundPhase();
        switch (phase) {
            case CHOOSING_WHITE_CARD: {
                updateChoosingWhiteCardScreen(gameState);
                break;
            }
            case CHOOSING_WINNER_CARD: {
                updateChoosingWinnerScreen(gameState);
                break;
            }
            case WINNER: {
                updateWinnerScreen(gameState);
                break;
            }
            default: {

            }
        }
    }

    // Update the choosing white card screen. This should be the first phase of the round
    public void updateChoosingWhiteCardScreen(GameState gameState) {
        if (gameState.getRoundPhase() != RoundPhase.CHOOSING_WHITE_CARD) {
            throw new AssertionError("Wrong phase");
        }

        boolean isCzar = gameState.isCzar();
        boolean waiting = gameState.waitingForOthers();

        showNextRoundButton(false);
        showScoreboard(false);

        // Show the black card
        updateBlackCardView(gameState.getBlackCard().getText());
        // Show the cards we are supposed to display
        updateWhiteCardsView(gameState.getDisplayedCards());
        // Update the cards' selection according to the state
        clearWhiteCardsSelection();
        setWhiteCardsSelection(gameState.getNumMaxSelection(), !waiting);

        // Show the cards only for plebs that are not waiting
        // waiting for a pleb means that they made their choice
        boolean showChoices = !isCzar && !waiting;
        showWhiteCards(showChoices);
        showChooseCard(showChoices);
        showSwapCards(showChoices);
        showWaitForOthers(waiting);

        // Show the message above the black card
        String msg = "";
        String important = "";
        if (gameState.isCzar()) {
            // "wait for plebs" for the czar
            msg = getResources().getString(R.string.waiting_for_plebs);
        } else {
            // the current czar for plebs
            msg = getResources().getString(R.string.current_czar);
            important = gameState.getCzarName();
        }
        showMsgAboveBlackCard(true, msg, important);

    }

    // Update the choosing the winner screen. This should be the second phase of the round
    public void updateChoosingWinnerScreen(GameState gameState) {
        if (gameState.getRoundPhase() != RoundPhase.CHOOSING_WINNER_CARD) {
            throw new AssertionError("Wrong phase");
        }

        boolean isCzar = gameState.isCzar();

        showNextRoundButton(false);
        showScoreboard(false);
        showSwapCards(false);

        // Show the black card
        updateBlackCardView(gameState.getBlackCard().getText());
        // Show the cards we are supposed to display
        updateWhiteCardsView(gameState.getDisplayedCards());
        // Enable the cards's selection only for the czar
        clearWhiteCardsSelection();
        setWhiteCardsSelection(gameState.getNumMaxSelection(), isCzar);

        showWhiteCards(true);
        showChooseCard(isCzar);
        showWaitForOthers(false);

        int strId = isCzar? R.string.choose_winner : R.string.waiting_for_czar;
        String msg = getResources().getString(strId);
        showMsgAboveBlackCard(true, msg, "");
    }

    // Update the winner screen. This should be the third phase of the round
    public void updateWinnerScreen(GameState gameState) {
        if (gameState.getRoundPhase() != RoundPhase.WINNER) {
            throw new AssertionError("Wrong phase");
        }

        boolean hasWinner = gameState.getWinnerName() != null;

        // Hide the choose button
        showChooseCard(false);
        showSwapCards(false);
        // Show the next round button only if haven't pressed it already
        showNextRoundButton(!gameState.waitingForOthers());
        // Show the cards and the scoreboard
        showWhiteCards(hasWinner);

        showScoreboard(true);
        if (hasWinner) {
            mScoreBoardAdapter.updateScoreBoard(gameState.getScoreboard());
        }

        // Show the black card
        updateBlackCardView(gameState.getBlackCard().getText());
        if (hasWinner) {
            // Show the cards we are supposed to display
            updateWhiteCardsView(gameState.getDisplayedCards());
        }
        // Select only the winning card and disable selection
        clearWhiteCardsSelection();
        if (hasWinner) {
            setWhiteCardsSelection(gameState.getNumMaxSelection(), false);
            selectWhiteCard(gameState.getSelectedCards().get(0).getText());
        }
        showWaitForOthers(gameState.waitingForOthers());

        String winnerName = hasWinner ? gameState.getWinnerName() : getResourceString(R.string.noone);
        showMsgAboveBlackCard(true, getResourceString(R.string.winner_is), winnerName);
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

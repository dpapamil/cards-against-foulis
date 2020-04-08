package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;
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
import com.papamilios.dimitris.cardsagainstfoulis.controller.GameController;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GamePlayer;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
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

public class GameActivity extends AppCompatActivity {

    final static String TAG = "CardsAgainstFoulis";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // The Firebase authentication object
    private FirebaseAuth mFirebaseAuth = null;

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    private GameController mController;

    String mInviterId = null;
    String mInvitationId = null;

    private boolean mJoining = false;
    private String mGameId = null;
    private boolean mGameStarted = false;

    // The white card view model
    private CardViewModel mCardViewModel = null;
    private CardListAdapter mCardsAdapter = null;

    // Chat messages
    private ChatMessageListAdapter mChatMessageAdapter = null;
    private boolean mInChat = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
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

        mController = new GameController(this);

        // Create the client used to sign in.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Sign in silently
        signInSilently();

        // Check to see if we are starting the game or we're just an invitee
        Intent intent = getIntent();
        String invitationId = intent.getStringExtra(MainActivity.INVITATION_ID);
        mJoining = intent.getBooleanExtra(MainActivity.JOINING, false);
        if (invitationId != null) {
            mInvitationId = invitationId;
        }

        // Hide chat
        findViewById(R.id.got_message).setVisibility(View.INVISIBLE);
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
        findViewById(R.id.game_id).setOnKeyListener(null);
    }

    public void onShowChat(View view) {
        showChat();
    }

    private void showChat() {
        if (mInChat) {
            return;
        }

        mInChat = true;
        findViewById(R.id.got_message).setVisibility(View.INVISIBLE);
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
        switchToMainScreen();
        keepScreenOn();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference("games").child(mGameId);
        gameRef.child("started").setValue(true);
        mController.startGame(mGameId);
    }

    public void onJoinGame(View view) {

        EditText gameIdTextView = (EditText) findViewById(R.id.game_id_edit);
        mGameId = gameIdTextView.getText().toString();

        showView(R.id.game_id_edit, false);
        showView(R.id.join_game, false);
        showView(R.id.current_players, true);

        // Add the user to the game object
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("games").child(mGameId);
        String userId = mFirebaseAuth.getCurrentUser().getUid();
        mController.setMyId(userId);

        ValueEventListener hostListener = new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              // Get the host
              mController.setHostId(dataSnapshot.getValue().toString());
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
              // Getting Post failed, log a message
              Log.w(TAG, "hostLoad:onCancelled", databaseError.toException());
          }
      };
        gameRef.child("host").addListenerForSingleValueEvent(hostListener);

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the users
                String joinedUsers = "";
                List<GamePlayer> players = new ArrayList<GamePlayer>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue().toString();
                    joinedUsers += userName + "\n";
                    players.add(new GamePlayer(userSnapshot.getKey(), userName));
                }

                mController.setPlayers(players);
                setTextToView(R.id.current_players, joinedUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException());
            }
        };
        gameRef.child("users").addValueEventListener(usersListener);

        gameRef.child("users/" + userId + "/name").setValue(mFirebaseAuth.getCurrentUser().getDisplayName());

        ValueEventListener startedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!mGameStarted && dataSnapshot.getValue().equals(true)) {
                    switchToMainScreen();
                    keepScreenOn();
                    mGameStarted = true;
                    mController.startGame(mGameId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException());
            }
        };
        gameRef.child("started").addValueEventListener(startedListener);

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
        gameRef.child("users").child(userId).child("name").setValue(mFirebaseAuth.getCurrentUser().getDisplayName());
        gameRef.child("host").setValue(userId);
        gameRef.child("started").setValue(false);

        mController.setHostId(userId);
        mController.setMyId(userId);

        // Populate the game ID edit text
        EditText gameIdTextView = (EditText) findViewById(R.id.game_id);
        gameIdTextView.setText(mGameId);
        gameIdTextView.setFocusable(false);
        gameIdTextView.setOnKeyListener(null);

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the users
                String joinedUsers = "";
                List<GamePlayer> players = new ArrayList<GamePlayer>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userName = userSnapshot.child("name").getValue().toString();
                    joinedUsers += userName + "\n";
                    players.add(new GamePlayer(userSnapshot.getKey(), userName));
                }

                mController.setPlayers(players);
                setTextToView(R.id.joined_players, joinedUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUsers:onCancelled", databaseError.toException());
            }
        };
        gameRef.child("users").addValueEventListener(usersListener);
    }

    public void joinGame() {
        switchToScreen(R.id.screen_join_game);
    }

    public void addChatMessage(@NonNull ChatMessage chatMsg) {
        mChatMessageAdapter.addMessage(chatMsg);
        findViewById(R.id.got_message).setVisibility(mInChat ? View.INVISIBLE : View.VISIBLE);
        RecyclerView recyclerChatView = findViewById(R.id.reyclerview_message_list);
        recyclerChatView.smoothScrollToPosition(mChatMessageAdapter.getItemCount() - 1);
    }

    // Start a game as a host
    private void startHostGame() {
        // Show the wait screen
        switchToScreen(R.id.screen_wait);

        // show list of invitable players
        mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 7).addOnSuccessListener(
            new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                }
            }
        ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
    }

    private void startGameFromInvitation() {
        // accept the invitation

        switchToScreen(R.id.screen_wait);
        keepScreenOn();
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
                } else {
                    handleException(task.getException(), "signOut() failed!");
                }

                onDisconnected();
                }
            });
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(GameActivity.this)
            .setTitle("Error")
            .setMessage(message + "\n" + errorString)
            .setNeutralButton(android.R.string.ok, null)
            .show();
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
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                switchToMainScreen();
                mController.startGame(mGameId);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != AppCompatActivity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // create the room
        Log.d(TAG, "Creating room...");
        switchToScreen(R.id.screen_wait);
        keepScreenOn();

        mController.createGameRoom(invitees);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToMainScreen();

        super.onStop();
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    public void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                // show waiting room UI
                startActivityForResult(intent, RC_WAITING_ROOM);
                }
            })
            .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private String mPlayerId;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

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
                            userRef.child("displayName").setValue(user.getDisplayName());
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

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;

        switchToMainScreen();
    }


    // Leave the room.
    private void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mController.connectedToRoom()) {
            mController.leaveRoom();
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
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

    // Show the white cards
    public void showWhiteCards(boolean show) {
        showView(R.id.white_cards, show);
    }

    // Set the given card as selected
    public void selectWhiteCard(@NonNull String cardText) {
        mCardsAdapter.setSelected(cardText);
    }

    // Update the text of the scoreboard
    public void updateScoreboard(@NonNull String scoresText) {
        ((TextView)findViewById(R.id.score_board)).setText(scoresText);
    }

    // Show the scoreboard
    public void showScoreboard(boolean show) {
        showView(R.id.score_board, show);
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

    // Set the given text to the view of the given id
    public void setTextToView(int viewId, String text) {
        TextView view = (TextView) findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setText(text);
    }

    // Get the specified resource string
    public String getResourceString(int id) {
        return getResources().getString(id);
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

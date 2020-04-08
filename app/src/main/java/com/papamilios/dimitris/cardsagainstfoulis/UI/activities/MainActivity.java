package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;
import com.papamilios.dimitris.cardsagainstfoulis.database.CardRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/*
*  The main activity of the application.
 */

public class MainActivity extends AppCompatActivity {
    final static String TAG = "CardsAgainstFoulis";
    public static final String WHITE_CARDS = "com.papamilios.dimitris.cardsagainstfoulis.WHITE_CARDS";
    public static final String INVITER_ID = "com.papamilios.dimitris.cardsagainstfoulis.INVITER_ID";
    public static final String INVITATION_ID = "com.papamilios.dimitris.cardsagainstfoulis.INVITATION_ID";
    public static final String JOINING = "com.papamilios.dimitris.cardsagainstfoulis.JOINING";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_INVITATION_INBOX = 10001;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    // The Firebase authentication object
    private FirebaseAuth mFirebaseAuth = null;

    // If non-null, this is the invitation we received via the invitation listener
    Invitation mIncomingInvitation = null;

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
        R.id.screen_main,
        R.id.screen_sign_in,
    };
    int mCurScreen = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the client used to sign in.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //signInSilently();
        switchToMainScreen();
    }

    void switchToMainScreen() {
        if (mInvitationsClient != null) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitation == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    // Event handler for clicking the White Cards button
    public void onShowWhiteCards(View view) {
        Intent intent = new Intent(MainActivity.this, CardsActivity.class);
        intent.putExtra(WHITE_CARDS, "white");
        startActivity(intent);
    }

    // Event handler for clicking the Black Cards button
    public void onShowBlackCards(View view) {
        Intent intent = new Intent(MainActivity.this, CardsActivity.class);
        intent.putExtra(WHITE_CARDS, "black");
        startActivity(intent);
    }

    // Event handler for loading the cards from an excel file
    public void onLoadCards(View view) {
        InputStream stream = getResources().openRawResource(R.raw.cards);
        try {
            CardRepository repo = new CardRepository(getApplication());
            XSSFWorkbook workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                for (int c = 0; c < 2; c++) {
                    Cell cell = row.getCell(c);
                    CellValue cellValue = formulaEvaluator.evaluate(cell);
                    if (cellValue == null) {
                        continue;
                    }
                    String cardText = cellValue.getStringValue();
                    Card card = new Card(0, cardText, c == 1);
                    repo.insert(card);
                }
            }

            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users");

            myRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("name").setValue(mFirebaseAuth.getCurrentUser().getDisplayName());
            myRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("message").setValue("Hello");

        } catch (Exception e) {
            /* proper exception handling to be here */
            Log.d(TAG, e.toString());
        }
    }

    // Event handler for clicking the Start Game button
    public void onStartGame(View view) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(JOINING, false);
        startActivity(intent);
    }

    // Event handler for clicking the Join Game button
    public void onJoinGame(View view) {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(JOINING, true);
        startActivity(intent);
    }

    // Event handler for clicking the Sign In button
    public void onSignIn(View view) {
        // start the sign-in flow
        Log.d(TAG, "Sign-in button clicked");
        startSignInIntent();
    }


    // Event handler for clicking the Sign Out button
    public void onSignOut(View view) {
        // user wants to sign out
        // sign out.
        Log.d(TAG, "Sign-out button clicked");
        signOut();
        switchToScreen(R.id.screen_sign_in);
    }

    // Event handler for clicking the Show Invitations button
    public void onShowInvitations(View view) {
        // show list of pending invitations
        mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
            new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                startActivityForResult(intent, RC_INVITATION_INBOX);
                }
            }
        ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
    }

    // Event handler for clicking the Show Invitations button
    public void onAcceptInvitation(View view) {
        acceptInviteToRoom(mIncomingInvitation);
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

        new AlertDialog.Builder(MainActivity.this)
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

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != AppCompatActivity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation);
        }
    }

    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitation = invitation;
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    invitation.getInviter().getDisplayName() + " " +
                            getString(R.string.is_inviting_you));
            switchToScreen(mCurScreen); // This will show the invitation popup
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitation.getInvitationId().equals(invitationId) && mIncomingInvitation.getInvitationId() != null) {
                mIncomingInvitation = null;
                switchToScreen(mCurScreen); // This will hide the invitation popup
            }
        }
    };

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the invitation client
            mInvitationsClient = Games.getInvitationsClient(MainActivity.this, googleSignInAccount);
        }

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
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                }
            });


        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(MainActivity.this, googleSignInAccount);
        gamesClient.getActivationHint()
            .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                @Override
                public void onSuccess(Bundle hint) {
                    if (hint != null) {
                        Invitation invitation = hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                        if (invitation != null && invitation.getInvitationId() != null) {
                            // retrieve and cache the invitation ID
                            Log.d(TAG, "onConnected: connection hint has a room invite!");
                            acceptInviteToRoom(invitation);
                        }
                    }
                }
            })
            .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));

        switchToMainScreen();
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
        mInvitationsClient = null;

        switchToMainScreen();
    }

    // Accept the given invitation.
    void acceptInviteToRoom(Invitation invitation) {
        // accept the invitation
        String invitationId = invitation.getInvitationId();
        String inviterId = invitation.getInviter().getParticipantId();
        Log.d(TAG, "Accepting invitation: " + invitationId);

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(INVITATION_ID, invitationId);
        intent.putExtra(INVITER_ID, inviterId);
        startActivity(intent);
    }
}

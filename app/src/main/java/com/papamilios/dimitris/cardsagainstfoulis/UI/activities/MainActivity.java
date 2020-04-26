package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PlayGamesAuthProvider;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;
import com.papamilios.dimitris.cardsagainstfoulis.database.CardRepository;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/*
*  The main activity of the application.
 */

public class MainActivity extends AppCompatActivity {
    final static String TAG = "CardsAgainstFoulis";
    public static final String WHITE_CARDS = "com.papamilios.dimitris.cardsagainstfoulis.WHITE_CARDS";
    public static final String JOINING = "com.papamilios.dimitris.cardsagainstfoulis.JOINING";

    private static final String cardsSheetId = "1LCb9xQs3Vt496xXUFAfvPvONvq8qkVFM5pwJPPGmRSc";

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_AUTHORIZE_SHEETS = 9002;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private GoogleSignInAccount mSignedInAccount = null;

    // The Firebase authentication object
    private FirebaseAuth mFirebaseAuth = null;

    // The card repository
    private CardRepository mCardsRepo;

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
        R.id.screen_main,
        R.id.screen_sign_in
    };
    int mCurScreen = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        myToolbar.inflateMenu(R.menu.menu_main);
        setSupportActionBar(myToolbar);
        mCardsRepo = new CardRepository(getApplication());

        // Create the client used to sign in.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestScopes(new Scope(SheetsScopes.SPREADSHEETS))
            .requestEmail()
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        switchToMainScreen();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.sign_out).setVisible(mSignedInAccount != null);
        return true;
    }

    void switchToMainScreen() {
        if (mSignedInAccount != null) {
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.white_cards: {
                onShowWhiteCards(item.getActionView());
                return true;
            }
            case R.id.black_cards: {
                onShowBlackCards(item.getActionView());
                return true;
            }
            case R.id.load_cards: {
                onLoadCards(item.getActionView());
                return true;
            }
            case R.id.sign_out: {
                onSignOut(item.getActionView());
                return true;
            }
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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

    private class LoadCardsTask extends AsyncTask<Void, Void, Void> {

        private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
        private final String cellRange = "Sheet1!A2:B";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                NetHttpTransport transport = new com.google.api.client.http.javanet.NetHttpTransport();
                JsonFactory factory = JacksonFactory.getDefaultInstance();
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), SCOPES);
                credential.setSelectedAccount(mSignedInAccount.getAccount());
                final Sheets sheetsService = new Sheets.Builder(transport, factory, credential)
                        .setApplicationName(getApplication().getApplicationInfo().name)
                        .build();
                ValueRange result = sheetsService.spreadsheets().values().get(cardsSheetId, cellRange).execute();
                int numRows = result.getValues() != null ? result.getValues().size() : 0;

                for (int r = 1; r < numRows; r++) {
                    for (int c = 0; c < 2; c++) {
                        String cardText = result.getValues().get(r).get(c).toString();
                        if (cardText == null || cardText.trim().isEmpty()) {
                            continue;
                        }
                        cardText = cardText.trim();
                        Card card = new Card(0, cardText, c == 1);
                        mCardsRepo.insert(card);
                    }
                }

            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), RC_AUTHORIZE_SHEETS);
            }catch (Exception e) {
                return null;
            } finally {

            }
            return null;
        }
    }

    // Event handler for loading the cards from an excel file
    public void onLoadCards(View view) {
        loadCards();
    }

    private void loadCards() {
        mCardsRepo.deleteAll(Void -> {
            new LoadCardsTask().execute();
            return Void;
        });
    }

    // Event handler for clicking the Start Game button
    public void onStartGame(View view) {
        loadCards();
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

        String errorString = details;

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

        } else if (requestCode == RC_AUTHORIZE_SHEETS) {
            loadCards();
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;
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
        mSignedInAccount = null;
        switchToMainScreen();
    }
}

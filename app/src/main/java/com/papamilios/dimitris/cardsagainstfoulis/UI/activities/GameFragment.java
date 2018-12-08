package com.papamilios.dimitris.cardsagainstfoulis.UI.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.UI.CardViewModel;
import com.papamilios.dimitris.cardsagainstfoulis.UI.OnSwipeTouchListener;
import com.papamilios.dimitris.cardsagainstfoulis.UI.chat.ChatMessageListAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.controller.GameController;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {


    public GameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        RecyclerView recyclerView = getActivity().findViewById(R.id.white_cards);
        mCardsAdapter = new CardListAdapter(getActivity());
        recyclerView.setAdapter(mCardsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Get a new or existing ViewModel from the ViewModelProvider.
        mCardViewModel = ViewModelProviders.of(this).get(CardViewModel.class);

        return rootView;
    }

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */

    final static String TAG = "CardsAgainstFoulis";

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    // The white card view model
    private CardViewModel mCardViewModel = null;
    private CardListAdapter mCardsAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game);

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        //signInSilently();
    }

    // Handler for getting the next black card, ie starting the next round
    public void onGetNextRound(View view) {
        getGameController().endRound();
    }

    // Handler for choosing a white card
    public void onChooseWhiteCard(View view) {
        Card chosenCard = mCardsAdapter.getSelectedCard();
        if (chosenCard == null) {
            // TODO: show some kind of error here, or disable button
        } else {
            getGameController().chooseCard(chosenCard);
        }
    }

    // Handler for closing the room event popup
    public void onCloseRoomEventPopup(View view) {
        showView(R.id.room_event_popup, false);
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        super.onStop();
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */


    // Leave the room.
    private void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (getGameController().connectedToRoom()) {
            getGameController().leaveRoom();
            //switchToScreen(R.id.screen_wait);
        }
    }

    // Show error message about game being cancelled and return to main screen.
    public void showGameError() {
        new AlertDialog.Builder(getActivity())
            .setMessage(getString(R.string.game_problem))
            .setNeutralButton(android.R.string.ok, null).create();
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    // Update the black card
    public void updateBlackCardView(@NonNull String blackCardText) {
        TextView blackCardView = getActivity().findViewById(R.id.cur_black_card);
        blackCardView.setText(blackCardText);
    }

    // Add a white card
    public void updateWhiteCardsView(@NonNull List<Card> whiteCards) {
        mCardsAdapter.setCards(whiteCards);
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

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
        ((TextView) getActivity().findViewById(R.id.score_board)).setText(scoresText);
    }

    // Show the scoreboard
    public void showScoreboard(boolean show) {
        showView(R.id.score_board, show);
    }

    // Enable/disable selecting white cards via clicking
    public void enableWhiteCardsSelection(boolean enable) {
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
    public void showMsgAboveBlackCard(boolean show, String text) {
        setTextToView(R.id.above_card_msg, text);
        showView(R.id.above_card_msg, show);
    }

    // Show the room event popup with the given text
    public void showRoomEvent(String text) {
        setTextToView(R.id.room_event_popup_text, text);
        showView(R.id.room_event_popup, true);
    }

    // Show/hide the given view
    public void showView(int viewId, boolean show) {
        View view = getActivity().findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setVisibility(show? View.VISIBLE : View.GONE);
    }

    // Set the given text to the view of the given id
    public void setTextToView(int viewId, String text) {
        TextView view = (TextView) getActivity().findViewById(viewId);
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
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    GameController getGameController() {
        return ((GameActivity)getActivity()).controller();
    }

}

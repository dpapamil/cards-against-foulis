package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.papamilios.dimitris.cardsagainstfoulis.UI.activities.GameActivity;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {
    // Member Variables

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    // The game activity
    private GameActivity mGameActivity;

    // The message handler
    private MessageHandler mMsgHandler;

    // The message receiver
    private MessageReceiver mMsgReceiver;

    // The room provider
    private RoomProvider mRoomProvider;

    // The cards provider
    private CardProvider mCardProvider;

    // My Ids
    private String mMyId = null;
    private String mPlayerId;

    // The current czar
    private String mCurCzarId = null;

    // The current black card
    private Card mCurBlackCard = null;

    // The current white cards
    private List<Card> mWhiteCards;

    // The answers of all plebs
    private Map<String, String> mPlebsCards;

    // The scoreboard
    private Map<String, Integer> mScoreboard;

    private List<String> mReadyForNextRound = new ArrayList<String>();

    // The invitation id
    private String mInvitationId = null;


    public GameController(GameActivity activity) {
        mGameActivity = activity;
        mMsgHandler = new MessageHandler(this);
        mMsgReceiver = new MessageReceiver(mMsgHandler);
        mRoomProvider = new RoomProvider(this);
        mCardProvider = new CardProvider(activity);
        mWhiteCards = new ArrayList<Card>();
        mPlebsCards = new HashMap<String, String>();
        mCurBlackCard = new Card(0, "", false);
        mScoreboard = new HashMap<String, Integer>();
    }

    // Accept the given invitation. This will join the room that corresponds
    // to the given invitation ID
    public void acceptInvitation(String invitationId, String inviterId) {
        if (mRealTimeMultiplayerClient == null) {
            throw new AssertionError("We need a client to join a  room");
        }
        mRoomProvider.joinRoom(invitationId);
    }

    // Create the game room. This is only when we are the ones that are
    // hosting the game.
    public void createGameRoom(ArrayList<String> invitees) {
        mRoomProvider.createRoom(invitees);
    }

    // Start the game
    public void startGame() {
        mGameActivity.showNextRoundButton(false);
        for (Participant p : mRoomProvider.participants()) {
            mScoreboard.put(p.getParticipantId(), 0);
        }
        getNextCzar();
        sendInitialWhiteCards();
        startRound();
    }

    // Initialise the white cards for everyone
    public void sendInitialWhiteCards() {
        if (!isHost()) {
            return;
        }
        ArrayList<Participant> plebs = mRoomProvider.participants();
        for (Participant pleb : plebs) {
            if (pleb.getParticipantId().equals(mRoomProvider.getHostId())) {
                // For us, the host, we don't need to send messages
                for (int i = 0; i < 10; i++) {
                    mWhiteCards.add(mCardProvider.getNextWhiteCard());
                }
                updateWhiteCardsView(mWhiteCards);
            } else {
                // Send the white cards as messages
                for (int i = 0; i < 10; i++) {
                    sendCard(pleb);
                }
            }
        }
    }

    // ---------------------- ACTIONS --------------------------------------------

    // Start a new round. This will only apply to the host.
    public void startRound() {
        // We'll need to wait for the host to send us all info of the round
        if (!isHost()) {
            return;
        }

        mCurBlackCard = mCardProvider.getNextBlackCard();
        onStartRound(mCurCzarId, mCurBlackCard.getText());
        // Send the card to other players
        mMsgHandler.sendStartRoundMsg(mCurCzarId, mCurBlackCard.getText());
    }

    // End the given round.
    public void endRound() {
        mGameActivity.showNextRoundButton(false);
        if (isHost()) {
            onEndRound(mMyId);
        } else {
            // Send a message to the host to end this round
            mMsgHandler.sendEndRoundMsg();
        }
    }

    public void chooseCard(Card chosenCard) {
        if (isCzar()) {
            chooseWinningCard(chosenCard);
        } else {
            chooseWhiteCard(chosenCard);
        }
    }

    // Choose the white card that answers the question
    public void chooseWhiteCard(Card chosenCard) {
        Card toRemove = null;
        for (Card card : mWhiteCards) {
            if (card.getText().equals(chosenCard.getText())) {
                toRemove = card;
                break;
            }
        }
        if (toRemove != null) {
            mWhiteCards.remove(toRemove);
            updateWhiteCardsView(mWhiteCards);
        }

        mMsgHandler.sendChooseWhiteCardMsg(chosenCard.getText());
        onGetChosenCard(chosenCard.getText(), mMyId);
    }


    // Choose the white card that answers the question
    public void chooseWinningCard(Card chosenCard) {
        mMsgHandler.sendChooseWinnerMsg(chosenCard.getText());
        onGetWinningCard(chosenCard.getText());
    }

    // Send the next white card to the given player
    public void sendCard(Participant player) {
        if (!isHost()) {
            return;
        }
        Card next_card = mCardProvider.getNextWhiteCard();
        mMsgHandler.sendCardMsg(next_card.getText(), player);
    }

    // ---------------------- EVENTS --------------------------------------------

    // Handler for when we are about to start a new round
    public void onStartRound(String czarId, String blackCardText) {
        // Clear the answers
        mPlebsCards.clear();
        // Clear the next round list

        mReadyForNextRound.clear();
        mCurBlackCard.setText(blackCardText);
        updateBlackCardView();
        mCurCzarId = czarId;

        // Show the white cards and the button only if we aren't a czar
        mGameActivity.showWhiteCards(!isCzar());
        mGameActivity.updateWhiteCardsView(mWhiteCards);
        mGameActivity.showChooseCard(!isCzar());
    }

    // Handle ending this round
    public void onEndRound(String senderId) {
        // We'll need to wait for the host to send us all info of the round
        if (!isHost()) {
            return;
        }

        if (mReadyForNextRound.contains(senderId)) {
            throw new AssertionError("Can't have multiple end round messages from the same sender");
        }

        mReadyForNextRound.add(senderId);

        // Check to see if everyone is ready for the next round
        if (mReadyForNextRound.size() == mRoomProvider.participants().size()) {
            // Send a new card to all players except the czar
            for (Participant player : mRoomProvider.getMortalEnemiesOf(mCurCzarId)) {
                if (player.getParticipantId().equals(mMyId)) {
                    mWhiteCards.add(mCardProvider.getNextWhiteCard());
                } else {
                    sendCard(player);
                }
            }
            // Get the next czar
            getNextCzar();
            // Now we should be ready to begin the next round
            startRound();
        }
    }

    // Handler for receiving a white card
    public void onReceiveCard(String cardText) {
        mWhiteCards.add(new Card(0, cardText, true));
        updateWhiteCardsView(mWhiteCards);
    }

    // Handler for receiving the card a pleb has chosen
    public void onGetChosenCard(@NonNull String cardText, @NonNull String plebId) {
        if (mPlebsCards.containsKey(plebId)) {
            throw new AssertionError("We shouldn't get two answers for this pleb");
        }

        mPlebsCards.put(plebId, cardText);
        if (mPlebsCards.size() == mRoomProvider.participants().size() - 1) {
            // Show the answers, if we received all of them
            mGameActivity.showWhiteCards(true);
            showAnswerCards();
            mGameActivity.showChooseCard(isCzar());
        }
    }

    // Handler for receiving the winnong card
    public void onGetWinningCard(String cardText) {
        for (Map.Entry<String, String> pair : mPlebsCards.entrySet()) {
            if (cardText.equals(pair.getValue())) {
                // This is the winner. Increase his score
                mScoreboard.put(pair.getKey(), mScoreboard.get(pair.getKey()) + 1);
                break;
            }
        }
        mGameActivity.selectWhiteCard(cardText);
        mGameActivity.showChooseCard(false);
        mGameActivity.showNextRoundButton(true);
    }

    public void leaveRoom() {
        mRoomProvider.leaveRoom();
    }

    public boolean connectedToRoom() {
        return mRoomProvider.connected();
    }

    public boolean isHost() {
        return mRoomProvider.getHostId().equals(mMyId);
    }
    public boolean isCzar() {
        return mCurCzarId.equals(mMyId);
    }

    public void onConnected(Player player, RealTimeMultiplayerClient client) {
        mPlayerId = player.getPlayerId();
        mRealTimeMultiplayerClient = client;
    }

    public void onRoomCreated(Room room) {
        mGameActivity.showWaitingRoom(room);
    }

    public void onConnectedToRoom() {
        mMyId = mRoomProvider.getParticipantId(mPlayerId);
    }

    public void showGameError() {
        mGameActivity.showGameError();
    }

    public RealTimeMultiplayerClient client() {
        return mRealTimeMultiplayerClient;
    }

    public MessageReceiver msgReceiver() {
        return mMsgReceiver;
    }

    public String roomId() {
        return mRoomProvider.roomId();
    }

    public Participant getParticipant(String id) {
        return mRoomProvider.getParticipant(id);
    }

    public List<Participant> getMortalEnemies() {
        return mRoomProvider.getMortalEnemiesOf(mMyId);
    }

    public Participant getHost() {
        return getParticipant(mRoomProvider.getHostId());
    }

    // Update the current black card
    public void updateBlackCardView() {
        mGameActivity.updateBlackCardView(mCurBlackCard.getText());
    }

    // Update the white cards
    public void updateWhiteCardsView(List<Card> cards) {
        mGameActivity.updateWhiteCardsView(cards);
    }

    // This will show the answer cards
    public void showAnswerCards() {
        // All plebs have given their answers. Show their answers
        List<Card> answerCards = new ArrayList<Card>();
        for (String cardText : mPlebsCards.values()) {
            answerCards.add(new Card(0, cardText, true));
        }
        updateWhiteCardsView(answerCards);
    }

    private void getNextCzar() {
        List<Participant> participants = mRoomProvider.participants();
        int curCzarIndex = -1;
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).getParticipantId().equals(mCurCzarId)) {
                curCzarIndex = i;
                break;
            }
        }
        if (curCzarIndex == participants.size() - 1) {
            curCzarIndex = 0;
        } else {
            curCzarIndex++;
        }
        mCurCzarId = participants.get(curCzarIndex).getParticipantId();
    }
}

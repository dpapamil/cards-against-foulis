package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.activities.GameActivity;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.Collections;
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
        List<Participant> plebs = mRoomProvider.participants();
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
        showEndRoundScreen();
        if (isHost()) {
            onEndRound(mMyId);
        } else {
            // Send a message to the host to end this round
            mMsgHandler.sendEndRoundMsg();
        }
    }

    public void chooseCards(List<Card> chosenCards) {
        if (isCzar()) {
            if (chosenCards.size() != 1) {
                throw new AssertionError("There should be only one selection");
            }
            chooseWinningCard(chosenCards.get(0));
        } else {
            chooseWhiteCards(chosenCards);
        }
    }

    // Choose the white card that answers the question
    public void chooseWhiteCards(List<Card> chosenCards) {
        if (chosenCards.size() < getNumOfAnswers()) {
            return;
        } else if (chosenCards.size() > getNumOfAnswers()) {
            throw new AssertionError("More answers selected thant we expect");
        }

        for (Card card : chosenCards) {
            mWhiteCards.remove(card);
        }

        updateWhiteCardsView(mWhiteCards);
        showWaitOthersToChooseScreen();

        Card chosenCard = CardUtils.mergeWhiteCards(chosenCards);
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

    // Send a chat message to everyone
    public void sendChatMessage(@NonNull String msg) {
        ChatMessage chatMsg = ChatMessage.create(msg);
        chatMsg.setSenderName(mRoomProvider.getParticipant(mMyId).getDisplayName());
        mMsgHandler.sendChatMsg(chatMsg);
        onReceiveChatMessage(chatMsg);
    }

    // ---------------------- EVENTS --------------------------------------------

    // Handler for when we are about to start a new round
    public void onStartRound(String czarId, String blackCardText) {
        // Clear the answers
        mPlebsCards.clear();
        // Clear the next round list

        mReadyForNextRound.clear();
        mCurBlackCard.setText(blackCardText);
        mCurCzarId = czarId;

        showChoosingWhiteCardScreen();
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
            // Send new cards to all players except the czar
            int numCardsToAdd = getNumOfAnswers();
            for (Participant player : mRoomProvider.getMortalEnemiesOf(mCurCzarId)) {
                int numCardsToSend = numCardsToAdd;
                while(numCardsToSend > 0) {
                    if (player.getParticipantId().equals(mMyId)) {
                        mWhiteCards.add(mCardProvider.getNextWhiteCard());
                    } else {
                        sendCard(player);
                    }
                    numCardsToSend--;
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
            showCzarChoosingWinnerScreen();
        }
    }

    // Handler for receiving the winning card
    public void onGetWinningCard(String cardText) {
        String winnerId = null;
        for (Map.Entry<String, String> pair : mPlebsCards.entrySet()) {
            if (cardText.equals(pair.getValue())) {
                // This is the winner. Increase his score
                winnerId = pair.getKey();
                mScoreboard.put(pair.getKey(), mScoreboard.get(pair.getKey()) + 1);
                updateScoreboard();
                break;
            }
        }

        if (winnerId == null) {
            return;
        }

        showWinnerScreen(cardText, winnerId);
    }

    // Called when we receive a chat message
    public void onReceiveChatMessage(@NonNull ChatMessage chatMsg) {
        if (chatMsg.getSenderName() == null) {
            chatMsg.setSenderName(mRoomProvider.getParticipant(chatMsg.senderId()).getDisplayName());
        }
        mGameActivity.addChatMessage(chatMsg);
    }

    // Called when players have left the game
    public void onPlayersLeft(List<String> playersNames) {
        if (playersNames.isEmpty()) {
            return;
        }
        String msg = mGameActivity.getResourceString(R.string.players_left) + "\n";
        for (String name : playersNames) {
            msg += TextUtils.join(", ", playersNames);
        }
        mGameActivity.showRoomEvent(msg);
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

    private void showChoosingWhiteCardScreen() {
        updateBlackCardView();

        mGameActivity.showScoreboard(false);
        mGameActivity.showWaitForOthers(false);

        // Show the white cards and the button only if we aren't a czar
        mGameActivity.clearWhiteCardsSelection();
        mGameActivity.setWhiteCardsSelection(getNumOfAnswers(), true);
        mGameActivity.updateWhiteCardsView(mWhiteCards);
        mGameActivity.showWhiteCards(!isCzar());
        mGameActivity.showChooseCard(!isCzar());

        // Show the message above the black card
        String msg = "";
        if (isCzar()) {
            // "wait for plebs" for the czar
            msg = mGameActivity.getResources().getString(R.string.waiting_for_plebs);
        } else {
            // the current czar for plebs
            msg = mGameActivity.getResources().getString(R.string.current_czar) + mRoomProvider.getParticipant(mCurCzarId).getDisplayName();
        }
        mGameActivity.showMsgAboveBlackCard(true, msg);
    }

    private void showWaitOthersToChooseScreen() {
        showChoosingWhiteCardScreen();
        mGameActivity.setWhiteCardsSelection(getNumOfAnswers(), false);
        mGameActivity.showWhiteCards(false);
        mGameActivity.showChooseCard(false);
        mGameActivity.showWaitForOthers(true);
    }

    private void showAllAnswersScreen() {
        // Show the answers, if we received all of them
        // Enable the selection only for the czars
        showAnswerCards();
        mGameActivity.showWhiteCards(true);
        mGameActivity.setWhiteCardsSelection(1, isCzar());
        mGameActivity.showChooseCard(isCzar());
        mGameActivity.showWaitForOthers(false);

        int strId = isCzar()? R.string.choose_winner : R.string.waiting_for_czar;
        String msg = mGameActivity.getResources().getString(strId);
        mGameActivity.showMsgAboveBlackCard(true, msg);
    }

    private void showCzarScreen() {
        showChoosingWhiteCardScreen();
    }

    private void showCzarChoosingWinnerScreen() {
        showCzarScreen();
        showAllAnswersScreen();
    }

    private void showWinnerScreen(@NonNull String cardText, @NonNull String winnerId) {
        mGameActivity.setWhiteCardsSelection(1, false);
        mGameActivity.selectWhiteCard(cardText);
        mGameActivity.showChooseCard(false);
        mGameActivity.showNextRoundButton(true);
        mGameActivity.showScoreboard(true);

        String msg = mGameActivity.getResources().getString(R.string.winner_is) + mRoomProvider.getParticipant(winnerId).getDisplayName();
        mGameActivity.showMsgAboveBlackCard(true, msg);
    }

    private void showEndRoundScreen() {
        mGameActivity.showNextRoundButton(false);
        mGameActivity.showWaitForOthers(true);
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
        Collections.shuffle(answerCards);
        updateWhiteCardsView(answerCards);
    }

    // Get the number of needed answers for the current black card.
    // -1 means we have no black card
    public int getNumOfAnswers() {
        if (mCurBlackCard == null) {
            return -1;
        }

        return CardUtils.numberOfAnswers(mCurBlackCard);
    }

    // Update the scoreboard
    private void updateScoreboard() {
        // Update the scoreboard before we show it
        String scores = "";
        for (Participant player : mRoomProvider.participants()) {
            scores += player.getDisplayName() + ": " + mScoreboard.get(player.getParticipantId()) + "\n";
        }
        mGameActivity.updateScoreboard(scores);
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

package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;
import android.text.TextUtils;

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

    // The game activity
    private GameActivity mGameActivity;

    // The message handler
    private MessageHandler mMsgHandler;

    // The message receiver
    private MessageReceiver mMsgReceiver;

    // The cards provider
    private CardProvider mCardProvider;

    // My Ids
    private String mMyId = null;
    private String mGameId = null;

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

    private List<GamePlayer> mPlayers = new ArrayList<GamePlayer>();
    private String mHostId = null;


    public GameController(GameActivity activity) {
        mGameActivity = activity;
        mMsgHandler = new MessageHandler(this);
        mMsgReceiver = new MessageReceiver(mMsgHandler);
        mCardProvider = new CardProvider(activity);
        mWhiteCards = new ArrayList<Card>();
        mPlebsCards = new HashMap<String, String>();
        mCurBlackCard = new Card(0, "", false);
        mScoreboard = new HashMap<String, Integer>();
    }

    public String getMyId() { return mMyId; }

    public void setPlayers(@NonNull List<GamePlayer> players) { mPlayers = players; }
    public void setHostId(@NonNull String id) { mHostId = id; }

    // Start the game
    public void startGame(@NonNull String gameId) {
        mGameId = gameId;
        mMsgHandler.setGameId(gameId);
        mMsgReceiver.startListening(mMyId);

        mGameActivity.showNextRoundButton(false);
        for (GamePlayer p : mPlayers) {
            mScoreboard.put(p.getId(), 0);
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

        for (GamePlayer pleb : mPlayers) {
            if (pleb.getId().equals(mHostId)) {
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
    public void sendCard(GamePlayer player) {
        if (!isHost()) {
            return;
        }
        Card next_card = mCardProvider.getNextWhiteCard();
        mMsgHandler.sendCardMsg(next_card.getText(), player);
    }

    // Send a chat message to everyone
    public void sendChatMessage(@NonNull String msg) {
        ChatMessage chatMsg = ChatMessage.create(msg);
        chatMsg.setSenderName(getPlayer(mMyId).getName());
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
        if (mReadyForNextRound.size() == mPlayers.size()) {
            // Send new cards to all players except the czar
            int numCardsToAdd = getNumOfAnswers();
            for (GamePlayer player : getMortalEnemiesOf(mCurCzarId)) {
                int numCardsToSend = numCardsToAdd;
                while(numCardsToSend > 0) {
                    if (player.getId().equals(mMyId)) {
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
        if (mPlebsCards.size() == mPlayers.size() - 1) {
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
            chatMsg.setSenderName(getPlayer(chatMsg.senderId()).getName());
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

    public boolean isHost() {
        return mHostId.equals(mMyId);
    }
    public boolean isCzar() {
        return mCurCzarId.equals(mMyId);
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
        String important = "";
        if (isCzar()) {
            // "wait for plebs" for the czar
            msg = mGameActivity.getResources().getString(R.string.waiting_for_plebs);
        } else {
            // the current czar for plebs
            msg = mGameActivity.getResources().getString(R.string.current_czar);
            important = getPlayer(mCurCzarId).getName();
        }
        mGameActivity.showMsgAboveBlackCard(true, msg, important);
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
        mGameActivity.showMsgAboveBlackCard(true, msg, "");
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

        String msg = mGameActivity.getResources().getString(R.string.winner_is);
        mGameActivity.showMsgAboveBlackCard(true, msg, getPlayer(winnerId).getName());
    }

    private void showEndRoundScreen() {
        mGameActivity.showNextRoundButton(false);
        mGameActivity.showWaitForOthers(true);
    }

    public void setMyId(@NonNull String id) {
        mMyId = id;
    }

    public void showGameError() {
        mGameActivity.showGameError();
    }

    public MessageReceiver msgReceiver() {
        return mMsgReceiver;
    }

    public GamePlayer getPlayer(String id) {
        for (GamePlayer player : mPlayers) {
            if (player.getId().equals(id)) {
                return player;
            }
        }

        return null;
    }

    public List<GamePlayer> getMortalEnemies() { return getMortalEnemiesOf(mMyId); }
    public List<GamePlayer> getMortalEnemiesOf(@NonNull String id) {
        List<GamePlayer> enemies = new ArrayList<GamePlayer>();
        for (GamePlayer player : mPlayers) {
            if (!player.getId().equals(id)) {
                enemies.add(player);
            }
        }

        return enemies;
    }

    public GamePlayer getHost() {
        return getPlayer(mHostId);
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
        for (GamePlayer player : mPlayers) {
            scores += player.getName() + ": " + mScoreboard.get(player.getId()) + "\n";
        }
        mGameActivity.updateScoreboard(scores);
    }

    private void getNextCzar() {
        int curCzarIndex = -1;
        for (int i = 0; i < mPlayers.size(); i++) {
            if (mPlayers.get(i).getId().equals(mCurCzarId)) {
                curCzarIndex = i;
                break;
            }
        }
        if (curCzarIndex == mPlayers.size() - 1) {
            curCzarIndex = 0;
        } else {
            curCzarIndex++;
        }
        mCurCzarId = mPlayers.get(curCzarIndex).getId();
    }
}

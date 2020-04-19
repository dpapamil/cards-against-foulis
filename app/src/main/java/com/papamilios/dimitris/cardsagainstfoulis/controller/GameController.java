package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.papamilios.dimitris.cardsagainstfoulis.R;
import com.papamilios.dimitris.cardsagainstfoulis.UI.activities.GameActivity;
import com.papamilios.dimitris.cardsagainstfoulis.UI.scoreBoard.ScoreBoardAdapter;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SwapCardsMessage;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.Arrays;
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

    // The list of user IDs that are ready for starting the next round
    private List<String> mReadyForNextRound = new ArrayList<String>();

    private List<GamePlayer> mPlayers = new ArrayList<GamePlayer>();
    private List<GamePlayer> mRoundPlayers = new ArrayList<GamePlayer>();

    private String mHostId = null;

    // The game state
    private GameState mGameState = new GameState();


    public GameController(GameActivity activity) {
        mGameActivity = activity;
        mMsgHandler = new MessageHandler(this);
        mMsgReceiver = new MessageReceiver(mMsgHandler);
        mCardProvider = new CardProvider(activity);
        mWhiteCards = new ArrayList<Card>();
        mPlebsCards = new HashMap<String, String>();
        mCurBlackCard = new Card(0, "", false);
    }

    public String getMyId() { return mMyId; }

    public void setPlayers(@NonNull List<GamePlayer> players) {
        mPlayers = players;
    }
    public List<GamePlayer> getPlayers() { return mPlayers; }
    public void setHostId(@NonNull String id) { mHostId = id; }

    // Start the game
    public void startGame(@NonNull String gameId) {
        mGameId = gameId;
        mMsgHandler.setGameId(gameId);
        mMsgReceiver.startListening(mMyId);

        List<String> userNames = new ArrayList<String>();
        mGameState.initialiseScoreboard(mPlayers);

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

                mGameState.setDisplayedCards(mWhiteCards);
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

        updateView();
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

        showWaitOthersToChooseScreen();

        Card chosenCard = CardUtils.mergeWhiteCards(chosenCards);
        mMsgHandler.sendChooseWhiteCardMsg(chosenCard.getText());
        onGetChosenCard(chosenCard.getText(), mMyId);

        updateView();
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

    // Swap all my cards and forfeit this round
    public void swapCards() {
        mWhiteCards.clear();
        mMsgHandler.sendSwapCardsMsg();
        showWaitOthersToChooseScreen();
        onPlayerSwappingCards(getPlayer(mMyId));
        updateView();
    }

    // ---------------------- EVENTS --------------------------------------------

    // Handler for when we are about to start a new round
    public void onStartRound(String czarId, String blackCardText) {
        // Clear the answers
        mPlebsCards.clear();
        // Clear the next round list
        mRoundPlayers.clear();
        mRoundPlayers.addAll(mPlayers);
        mRoundPlayers.remove(getPlayer(czarId));
        mReadyForNextRound.clear();
        mCurBlackCard.setText(blackCardText);
        mCurCzarId = czarId;

        mGameState.setBlackCard(mCurBlackCard);
        mGameState.setCzarName(getPlayer(mCurCzarId).getName());

        showChoosingWhiteCardScreen();

        updateView();
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
            for (GamePlayer player : mRoundPlayers) {
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
    }

    // Handler for receiving the card a pleb has chosen
    public void onGetChosenCard(@NonNull String cardText, @NonNull String plebId) {
        if (mPlebsCards.containsKey(plebId)) {
            throw new AssertionError("We shouldn't get two answers for this pleb");
        }

        mPlebsCards.put(plebId, cardText);

        goToChoosingWinnerPhase();
    }

    // Handler for receiving the winning card
    public void onGetWinningCard(String cardText) {
        String winnerId = null;
        for (Map.Entry<String, String> pair : mPlebsCards.entrySet()) {
            if (cardText.equals(pair.getValue())) {
                // This is the winner. Increase his score
                winnerId = pair.getKey();
                GamePlayer winner = getPlayer(winnerId);
                mGameState.increaseScore(winner.getName());
                break;
            }
        }

        if (winnerId == null) {
            return;
        }

        mGameState.setRoundPhase(RoundPhase.WINNER);
        showWinnerScreen(cardText, winnerId);

        updateView();
    }

    public void onPlayerSwappingCards(@NonNull GamePlayer player) {
        mRoundPlayers.remove(player);
        replaceCards(player);

        if (mRoundPlayers.isEmpty()) {
            goToWinnerPhase();
        } else {
            // Check if we have all answers and go to the next phase if we did
            goToChoosingWinnerPhase();
        }
    }

    // Called when we receive a chat message
    public void onReceiveChatMessage(@NonNull ChatMessage chatMsg) {
        if (chatMsg.getSenderName() == null) {
            chatMsg.setSenderName(getPlayer(chatMsg.senderId()).getName());
        }
        mGameActivity.addChatMessage(chatMsg);
    }

    public void onReceiveSwapCardsMessage(@NonNull SwapCardsMessage msg) {
        GamePlayer strandedPlayer = getPlayer(msg.senderId());
        onPlayerSwappingCards(strandedPlayer);
    }

    // Called when players have left the game
    public void onPlayersLeft(List<String> playersNames) {
        if (playersNames.isEmpty()) {
            return;
        }
        String msg = mGameActivity.getResourceString(R.string.players_left) + "\n";
        msg += TextUtils.join(", ", playersNames);
        mGameActivity.showRoomEvent(msg);
    }

    public boolean isHost() {
        return mHostId.equals(mMyId);
    }
    public boolean isCzar() {
        return mCurCzarId.equals(mMyId);
    }

    private void showChoosingWhiteCardScreen() {
        mGameState.setRoundPhase(RoundPhase.CHOOSING_WHITE_CARD);
        mGameState.setWaitingForOthers(false);
        mGameState.setDisplayedCards(mWhiteCards);
        mGameState.clearCardSelection();
        mGameState.setIsCzar(isCzar());
        mGameState.setmNumMaxSelections(getNumOfAnswers());
    }

    // Replace all game cards for the given player
    // Only for the host
    private void replaceCards(@NonNull GamePlayer player) {
        if (!isHost()) {
            return;
        }

        if (player.getId().equals(mHostId)) {
            mWhiteCards.clear();
            for (int i = 0; i < 10; i++) {
                mWhiteCards.add(mCardProvider.getNextWhiteCard());
            }
        } else {
            for (int i = 0; i < 10; i++) {
                sendCard(player);
            }
        }
    }

    // This will check if we're ready to move onto the second phase
    // of the round, which is the choosing the winner card phase
    private void goToChoosingWinnerPhase() {
        if (mPlebsCards.size() > mRoundPlayers.size()) {
            throw new AssertionError("How can we have more cards than round players?");
        }
        // Only go to the next phase, if we have received answers from all players
        if (mPlebsCards.size() < mRoundPlayers.size()) {
            return;
        }

        mGameState.setRoundPhase(RoundPhase.CHOOSING_WINNER_CARD);
        showCzarChoosingWinnerScreen();
        mGameActivity.update(mGameState);
    }

    private void goToWinnerPhase() {
        mGameState.setRoundPhase(RoundPhase.WINNER);
        List<Card> empty = new ArrayList<Card>();
        mGameState.setDisplayedCards(empty);
        mGameState.setWinnerName(null);
        mGameState.setWaitingForOthers(false);
        updateView();
    }

    private void showWaitOthersToChooseScreen() {
        showChoosingWhiteCardScreen();

        mGameState.setWaitingForOthers(true);
    }

    private void showAllAnswersScreen() {
        // Show the answers, if we received all of them
        // Enable the selection only for the czars
        showAnswerCards();

        mGameState.setmNumMaxSelections(1);
    }

    private void showCzarChoosingWinnerScreen() {
        showAllAnswersScreen();
    }

    private void showWinnerScreen(@NonNull String cardText, @NonNull String winnerId) {
        List<Card> selectedCards = new ArrayList<Card>(Arrays.asList(new Card(0, cardText, true)));
        mGameState.setSelectedCards(selectedCards);
        mGameState.setmNumMaxSelections(0);

        mGameState.setWinnerName(getPlayer(winnerId).getName());
        mGameState.setWaitingForOthers(false);
    }

    private void showEndRoundScreen() {
        mGameState.setWaitingForOthers(true);
    }

    public void setMyId(@NonNull String id) {
        mMyId = id;
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

    // This will show the answer cards
    public void showAnswerCards() {
        // All plebs have given their answers. Show their answers
        List<Card> answerCards = new ArrayList<Card>();
        for (String cardText : mPlebsCards.values()) {
            answerCards.add(new Card(0, cardText, true));
        }
        Collections.shuffle(answerCards);
        mGameState.setDisplayedCards(answerCards);
    }

    // Get the number of needed answers for the current black card.
    // -1 means we have no black card
    public int getNumOfAnswers() {
        if (mCurBlackCard == null) {
            return -1;
        }

        return CardUtils.numberOfAnswers(mCurBlackCard);
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
        
        GamePlayer czar = mPlayers.get(curCzarIndex);
        mCurCzarId = czar.getId();
        mGameState.setCzarName(czar.getName());
    }

    private void updateView() {
        mGameActivity.update(mGameState);
    }
}

package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import com.google.android.gms.games.Player;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.papamilios.dimitris.cardsagainstfoulis.UI.activities.GameActivity;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.util.ArrayList;
import java.util.List;

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

    // The invitation id
    private String mInvitationId = null;


    public GameController(GameActivity activity) {
        mGameActivity = activity;
        mMsgHandler = new MessageHandler(this);
        mMsgReceiver = new MessageReceiver(mMsgHandler);
        mRoomProvider = new RoomProvider(this);
        mCardProvider = new CardProvider(activity);
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
        sendInitialWhiteCards();
        startRound();
    }

    public void sendInitialWhiteCards() {
        if (!isHost()) {
            return;
        }
        ArrayList<Participant> participants = mRoomProvider.participants();
        for (Participant p : participants) {
            if (p.getParticipantId().equals(mRoomProvider.getHostId())) {
                // For us, the host, we don't need to send messages
                for (int i = 0; i < 10; i++) {
                    mGameActivity.addWhiteCard(mCardProvider.getNextWhiteCard());
                }
            } else {
                // Send the white cards as messages
                for (int i = 0; i < 10; i++) {
                    mMsgHandler.sendCardMsg(mCardProvider.getNextWhiteCard().getText(), p);
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

        Card blackCard = mCardProvider.getNextBlackCard();
        onStartRound(mMyId, blackCard.getText());
        // Send the card to other players
        mMsgHandler.sendStartRoundMsg(mMyId, blackCard.getText());
    }

    // End the given round.
    public void endRound() {
        if (isHost()) {
            startRound();
        } else {
            // Send a message to the host to end this round
            mMsgHandler.sendEndRoundMsg();
        }
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
        mGameActivity.updateBlackCardView(blackCardText);
    }

    // Handle ending this round
    public void onEndRound() {
        // We'll need to wait for the host to send us all info of the round
        if (!isHost()) {
            return;
        }
        startRound();
    }

    // Handler for receiving a white card
    public void onReceiveCard(String cardText) {
        mGameActivity.addWhiteCard(new Card(0, cardText, true));
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

    public ArrayList<Participant> getMortalEnemies() {
        return mRoomProvider.getMortalEnemies(mMyId);
    }

    public Participant getHost() {
        return getParticipant(mRoomProvider.getHostId());
    }
}

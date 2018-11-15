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

    public void startGame() {
        startRound();
    }

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

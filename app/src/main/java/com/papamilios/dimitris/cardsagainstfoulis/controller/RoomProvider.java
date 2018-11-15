package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.papamilios.dimitris.cardsagainstfoulis.R;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class RoomProvider {

    // Member Variables
    // The game controller
    private GameController mGameController;

    private RoomStatusUpdateHandler mStatusUpdateHandler;
    private RoomUpdateHandler mUpdateHandler;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    private String mRoomId = null;

    // Holds the configuration of the current room.
    private RoomConfig mRoomConfig = null;

    // The host of the room
    private String mHostId = null;

    // The participants in the currently active game
    private ArrayList<Participant> mParticipants = null;


    // Member Functions
    public RoomProvider(GameController gameController) {
        mGameController = gameController;
        mStatusUpdateHandler = new RoomStatusUpdateHandler(this);
        mUpdateHandler = new RoomUpdateHandler(this);
    };

    // Create a room we are hosting with the given list of invitees
    public void createRoom(ArrayList<String> invitees) {
        if (mRoomConfig != null) {
            return;
        }

        mRoomConfig = RoomConfig.builder(mUpdateHandler)
            .addPlayersToInvite(invitees)
            .setOnMessageReceivedListener(mGameController.msgReceiver())
            .setRoomStatusUpdateCallback(mStatusUpdateHandler)
            .build();
        mGameController.client().create(mRoomConfig);
    }

    // Join a room as an invitee
    public void joinRoom(String invitationId) {
        if (mRoomConfig != null) {
            return;
        }

        mRoomConfig = RoomConfig.builder(mUpdateHandler)
            .setInvitationIdToAccept(invitationId)
            .setOnMessageReceivedListener(mGameController.msgReceiver())
            .setRoomStatusUpdateCallback(mStatusUpdateHandler)
            .build();
        mGameController.client().join(mRoomConfig)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
    }

    // Leave the room
    public void leaveRoom() {
        mGameController.client().leave(mRoomConfig, mRoomId)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                mRoomId = null;
                mRoomConfig = null;
                mHostId = null;
                }
            });
    }

    public boolean connected() {
        return mRoomId != null;
    }

    // Get the room participants
    public ArrayList<Participant> participants() {
        return mParticipants;
    }

    // Get the participant ID that corresponds to the given player ID
    // Return null, if not found
    public String getParticipantId(String playerId) {
        String id = null;
        for (Participant p : mParticipants) {
            if (p.getPlayer().getPlayerId().equals(playerId)) {
                id = p.getParticipantId();
                break;
            }
        }
        return id;
    }

    // Called when the room has been created
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesCallbackStatusCodes.OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            mGameController.showGameError();
            return;
        }

        mHostId = room.getCreatorId();

        // save room ID so we can leave cleanly before the game starts.
        mRoomId = room.getRoomId();

        // Notify the controller we've been connected
        mGameController.onRoomCreated(room);
    }

    // Called when the room has been fully connected
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesCallbackStatusCodes.OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            mGameController.showGameError();
            return;
        }

        updateRoom(room);
    }


    // Called when we have joined to the room
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesCallbackStatusCodes.OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            mGameController.showGameError();
            return;
        }

        // Notify the controller we've been connected
        mHostId = room.getCreatorId();
        mGameController.onRoomCreated(room);
    }


    // Called when we have left the room
    public void onLeftRoom(int statusCode, @NonNull String roomId) {
        // switch to main screen
    }

    // Called when we are connected to the room
    public void onConnectedToRoom(Room room) {
        updateRoom(room);
        mHostId = room.getCreatorId();
        mGameController.onConnectedToRoom();
    }

    // Called when we are disconnected to the room
    public void onDisonnectedFromRoom(Room room) {
        mRoomConfig = null;
        mRoomId = null;
        mParticipants.clear();
        mGameController.showGameError();
    }

    // Update the room
    public void updateRoom(Room room) {
        if (room == null) {
            return;
        }
        mRoomId = room.getRoomId();
        // Update the participants
        mParticipants = room.getParticipants();
    }

    public String roomId() {
        return mRoomId;
    }

    public String getHostId() {
        return mHostId;
    }

    public Participant getParticipant(String id) {
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // Get all the participants except ourselves
    public ArrayList<Participant> getMortalEnemies(String myId) {
        ArrayList<Participant> enemies = mParticipants;
        Participant me = null;
        for (Participant p : enemies) {
            if (p.getParticipantId().equals(myId)) {
                me = p;
                break;
            }
        }
        if (me != null) {
            enemies.remove(me);
        }

        return enemies;
    }
}

package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;

public class RoomUpdateHandler extends RoomUpdateCallback{

    // Member Variables
    // The room provider
    private RoomProvider mRoomProvider;


    public RoomUpdateHandler(RoomProvider roomProvider) {
        mRoomProvider = roomProvider;
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        mRoomProvider.onRoomCreated(statusCode, room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        mRoomProvider.onRoomConnected(statusCode, room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        mRoomProvider.onJoinedRoom(statusCode, room);
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, @NonNull String roomId) {
        mRoomProvider.onLeftRoom(statusCode, roomId);
    }
}

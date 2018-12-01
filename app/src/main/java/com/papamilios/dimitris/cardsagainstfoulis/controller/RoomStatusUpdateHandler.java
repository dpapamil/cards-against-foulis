package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;

import java.util.List;

public class RoomStatusUpdateHandler extends RoomStatusUpdateCallback {

    // The game controller
    private RoomProvider mRoomProvider;


    public RoomStatusUpdateHandler(RoomProvider roomProvider) {
        mRoomProvider = roomProvider;
    }

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        mRoomProvider.onConnectedToRoom(room);
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomProvider.onDisonnectedFromRoom(room);
    }

    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(@NonNull String participant) {
    }

    @Override
    public void onP2PConnected(@NonNull String participant) {
    }

    @Override
    public void onPeerJoined(Room room, @NonNull List<String> arg1) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
        mRoomProvider.onPaylersLeft(room, peersWhoLeft);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, @NonNull List<String> peers) {
        mRoomProvider.updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
        mRoomProvider.onPaylersLeft(room, peers);
    }
}

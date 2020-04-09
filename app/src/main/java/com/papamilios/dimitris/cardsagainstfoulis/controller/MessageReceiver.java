package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;

import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.MessageFactory;

import java.nio.charset.Charset;

public class MessageReceiver {

    // Member Variables
    MessageHandler mMsgHandler;

    DatabaseReference mMessagesRef = null;

    // Constructor
    public MessageReceiver(MessageHandler msgHandler) {
        mMsgHandler = msgHandler;
    };

    public void startListening(@NonNull String userId) {

        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference().child("games").child(mMsgHandler.getGameId());
        mMessagesRef = gameRef.child("users/" + userId + "/message");

        ChildEventListener messageListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String senderId = null;
                GameMessage msg = null;
                if (dataSnapshot.hasChild("buffer")) {
                    String str = dataSnapshot.child("buffer").getValue().toString();
                    msg = MessageFactory.create(str.getBytes(Charset.defaultCharset()));
                }
                if (dataSnapshot.hasChild("sender")) {
                    senderId = dataSnapshot.child("sender").getValue().toString();
                }

                msg.setSenderId(senderId);
                msg.accept(mMsgHandler);

                mMessagesRef.child(dataSnapshot.getKey()).removeValue();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mMessagesRef.addChildEventListener(messageListener);
    }
}


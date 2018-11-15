package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IGameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.MessageFactory;

import java.nio.charset.Charset;

public class MessageReceiver implements OnRealTimeMessageReceivedListener {

    // Member Variables
    MessageHandler mMsgHandler;

    // Constructor
    public MessageReceiver(MessageHandler msgHandler) {
        mMsgHandler = msgHandler;
    };

    // Override from OnRealTimeMessageReceivedListener.
    // This is called when we receive a message from teh multiplayer client
    public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
        byte[] buf = realTimeMessage.getMessageData();

        // Create the message and set its sender
        IGameMessage msg = MessageFactory.create(buf);
        msg.setSenderId(realTimeMessage.getSenderParticipantId());

        // Handle the message
        msg.accept(mMsgHandler);
    };
}


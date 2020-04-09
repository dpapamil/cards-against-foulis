package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public abstract class GameMessage {

    // Member variables
    // The actual message
    private byte[] mMsgBuf;

    // The receiver of the message
    private String mReceiverId;

    // The sender of the message
    private String mSenderId;

    protected GameMessage(byte[] msgBuf) {
        mMsgBuf = msgBuf;
    }

    // Public Functions
    public byte[] bytes() {
        return mMsgBuf;
    }

    // Accessors for the receiver
    public void setReceiverId(String receiverId) {
        mReceiverId = receiverId;
    }
    public String receiverId() {
        return mReceiverId;
    }

    // Accessors for the sender
    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }
    public String senderId() {
        return mSenderId;
    }

    // Abstract function for accepting a visitor
    public abstract void accept(IMessageVisitor visitor);

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("buffer", new String(mMsgBuf, Charset.defaultCharset()));
        map.put("sender", mSenderId);
        return map;
    }
}

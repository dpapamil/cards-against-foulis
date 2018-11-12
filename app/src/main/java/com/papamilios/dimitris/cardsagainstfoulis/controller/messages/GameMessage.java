package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public class GameMessage implements IGameMessage {

    // Member variables
    // The actual message
    private byte[] mMsgBuf;

    // The receiver of the message
    private String mReceiverId;

    // The sender of the message
    private String mSenderId;

    // Public Functions
    public GameMessage(byte[] msgBuf) {
        mMsgBuf = msgBuf;
    }

    public byte[] bytes() {
        return mMsgBuf;
    }

    public void setReceiverId(String receiverId) {
        mReceiverId = receiverId;
    }
    public String receiverId() {
        return mReceiverId;
    }


    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }
    public String senderId() {
        return mSenderId;
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

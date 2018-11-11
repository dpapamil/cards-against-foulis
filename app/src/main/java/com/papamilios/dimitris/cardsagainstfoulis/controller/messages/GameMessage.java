package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public class GameMessage {

    // Member variables

    // The actual message
    private byte[] mMsgBuf;

    // Public Functions
    public GameMessage(byte[] msgBuf) {
        mMsgBuf = msgBuf;
    }

    public byte[] bytes() {
        return mMsgBuf;
    }
}

package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public class AskCardMessage extends GameMessage {

    public AskCardMessage(byte[] buf) {
        super(buf);
    }

    public static AskCardMessage create() {
        byte[] msgBuf = new byte[1];
        msgBuf[0] = (byte) (MessageType.ASK_CARD.value());
        return new AskCardMessage(msgBuf);
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

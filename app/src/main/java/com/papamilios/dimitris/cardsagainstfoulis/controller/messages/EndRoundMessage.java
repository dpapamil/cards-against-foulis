package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public class EndRoundMessage extends GameMessage {

    public EndRoundMessage(byte[] buf) {
        super(buf);
    }

    public static EndRoundMessage create() {
        byte[] msgBuf = new byte[1];
        msgBuf[0] = (byte) (MessageType.END_ROUND.value());
        return new EndRoundMessage(msgBuf);
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

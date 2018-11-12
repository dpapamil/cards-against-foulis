package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

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
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}

package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

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
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}

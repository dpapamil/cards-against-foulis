package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;


public class SwapCardsMessage extends GameMessage {


    public SwapCardsMessage(byte[] buf) {
        super(buf);
    }

    public static SwapCardsMessage create() {
        byte[] msgBuf = new byte[1];
        msgBuf[0] = (byte) (MessageType.SWAP_CARDS.value());
        return new SwapCardsMessage(msgBuf);
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

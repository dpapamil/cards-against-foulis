package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

import java.nio.charset.Charset;

public class ChooseWinnerMessage extends GameMessage {
    // Member variables
    private String mWinnerId;


    public ChooseWinnerMessage(byte[] buf) {
        super(buf);
        mWinnerId = new String(MessageUtils.getActualMsg(buf), Charset.defaultCharset());
    }

    public static ChooseWinnerMessage create(String cardText) {
        byte[] msg = cardText.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] msgBuf = MessageUtils.prependType(MessageType.CHOOSE_WINNER, msg);
        return new ChooseWinnerMessage(msgBuf);
    }

    public String winnerId() {
        return mWinnerId;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}

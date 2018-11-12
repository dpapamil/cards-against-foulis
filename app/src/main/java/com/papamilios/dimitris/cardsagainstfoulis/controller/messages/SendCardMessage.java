package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;


import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import java.nio.charset.Charset;

public class SendCardMessage extends GameMessage {

    // Member variables
    private String mCardText;


    public SendCardMessage(byte[] buf) {
        super(buf);
        mCardText = new String(MessageUtils.getActualMsg(buf), Charset.defaultCharset());
    }

    public static SendCardMessage create(String cardText) {
        byte[] msg = cardText.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] msgBuf = MessageUtils.prependType(MessageType.SEND_CARD, msg);
        return new SendCardMessage(msgBuf);
    }

    public String cardText() {
        return mCardText;
    }

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}

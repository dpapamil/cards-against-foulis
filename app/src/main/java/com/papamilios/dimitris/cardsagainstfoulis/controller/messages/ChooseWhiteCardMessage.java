package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import java.nio.charset.Charset;

public class ChooseWhiteCardMessage extends GameMessage {
    // Member variables
    private String mCardText;


    public ChooseWhiteCardMessage(byte[] buf) {
        super(buf);
        mCardText = new String(MessageUtils.getActualMsg(buf), Charset.defaultCharset());
    }

    public static ChooseWhiteCardMessage create(String cardText) {
        byte[] msg = cardText.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] msgBuf = MessageUtils.prependType(MessageType.CHOOSE_WHITE_CARD, msg);
        return new ChooseWhiteCardMessage(msgBuf);
    }

    public String cardText() {
        return mCardText;
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

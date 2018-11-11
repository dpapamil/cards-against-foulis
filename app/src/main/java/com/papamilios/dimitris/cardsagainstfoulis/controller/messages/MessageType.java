package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

public enum MessageType {
    SEND_CARD_MSG(0);

    private final int mValue;

    private MessageType(int value) {
        mValue = value;
    };

    public int value() {
        return mValue;
    }
}

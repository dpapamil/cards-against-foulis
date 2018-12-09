package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    START_ROUND(0),
    CHOOSE_WHITE_CARD(1),
    CHOOSE_WINNER(2),
    END_ROUND(3),
    SEND_CARD(4),
    ASK_CARD(5),
    CHAT(6);

    private final int mValue;

    // Mapping message type to its value
    private static final Map<Integer, MessageType> mMap = new HashMap<Integer, MessageType>();

    private MessageType(int value) {
        mValue = value;
    };

    public int value() {
        return mValue;
    }

    static
    {
        for (MessageType type : MessageType.values()) {
            // assert for duplicate values
            mMap.put(type.value(), type);
        }
    }

    // Get the type from value
    public static MessageType from(int value)
    {
        return mMap.get(value);
    }
}

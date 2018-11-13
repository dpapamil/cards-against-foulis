package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

class MessageUtils {

    // Prepend the given message type to the bare message
    static byte[] prependType(MessageType msgType, byte[] bareMsg) {
        // Prepend the message type to the actual message
        byte[] type = new byte[1];
        type[0] = (byte) (msgType.value());
        byte[] msgBuf = new byte[type.length + bareMsg.length];
        System.arraycopy(type, 0, msgBuf, 0, type.length);
        System.arraycopy(bareMsg, 0, msgBuf, type.length, bareMsg.length);
        return msgBuf;
    };

    // Get the actual message.
    static byte[] getActualMsg(byte[] fullMsg) {
        // Drop the first byte, which is the message type
        byte[] msg = new byte[fullMsg.length - 1];
        System.arraycopy(fullMsg, 1, msg, 0, fullMsg.length - 1);
        return msg;
    };

    static MessageType getMessageType(byte[] msg) {
        return MessageType.from((int) msg[0]);
    };
}

package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;


import java.nio.charset.Charset;

public class SendCardMessage extends GameMessage {

    public SendCardMessage(byte[] buf) {
        super(buf);
    }

    public static SendCardMessage create(String cardText) {
        byte[] msg = cardText.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] type = new byte[1];
        type[0] = (byte) (MessageType.SEND_CARD_MSG.value());
        byte[] msgBuf = new byte[type.length + msg.length];
        System.arraycopy(type, 0, msgBuf, 0, type.length);
        System.arraycopy(msg, 0, msgBuf, type.length, msg.length);
        return new SendCardMessage(msgBuf);
    }
}

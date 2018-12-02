package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*  * Copyright (C) 2018 Cards Against Foulis Co.  */

import android.support.annotation.NonNull;

import java.nio.charset.Charset;

public class ChatMessage extends GameMessage {

    private final String mText;
    private String mSenderName = null;

    public ChatMessage(byte[] buf) {
        super(buf);
        mText = new String(MessageUtils.getActualMsg(buf), Charset.defaultCharset());
    }

    public static ChatMessage create(@NonNull String msgText) {
        byte[] msg = msgText.getBytes(Charset.defaultCharset());
        // Prepend the message type to the actual message
        byte[] msgBuf = MessageUtils.prependType(MessageType.CHAT, msg);
        return new ChatMessage(msgBuf);
    }

    public String getText() {
        return mText;
    }

    public void setSenderName(@NonNull String name) {
        mSenderName = name;
    }
    public String getSenderName() {
        return mSenderName;
    }

    @Override
    public void accept(IMessageVisitor visitor) {
        visitor.visit(this);
    }
}

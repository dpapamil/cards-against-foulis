package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public interface IGameMessage {
    public void accept(IMessageVisitor visitor);

    // Accessors for the receiver
    public void setReceiverId(String receiverId);
    public String receiverId();

    // Accessors for the sender
    public void setSenderId(String senderId);
    public String senderId();
}

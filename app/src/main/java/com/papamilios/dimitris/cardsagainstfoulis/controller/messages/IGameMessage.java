package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

public interface IGameMessage {
    public void accept(IMessageVisitor visitor);
}

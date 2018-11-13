package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public interface IGameMessage {
    public void accept(IMessageVisitor visitor);
}

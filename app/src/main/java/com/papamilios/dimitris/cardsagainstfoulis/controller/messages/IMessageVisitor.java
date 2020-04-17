package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public interface IMessageVisitor {
    public void visit(StartRoundMessage msg);
    public void visit(ChooseWhiteCardMessage msg);
    public void visit(ChooseWinnerMessage msg);
    public void visit(EndRoundMessage msg);
    public void visit(AskCardMessage msg);
    public void visit(SendCardMessage msg);
    public void visit(ChatMessage msg);
    public void visit(SwapCardsMessage msg);
}

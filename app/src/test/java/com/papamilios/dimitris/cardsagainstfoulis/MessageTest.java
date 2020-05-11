package com.papamilios.dimitris.cardsagainstfoulis;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.AskCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWhiteCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWinnerMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.EndRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IMessageVisitor;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SendCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.StartRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SwapCardsMessage;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/*
 *  Tests for the message classes
 */
public class MessageTest {
    @Mock
    IMessageVisitor messageVisitor;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testStartRoundMessage() {
        // Test the message
        String czarId = "1";
        String blackCardText = "Stupid question";
        StartRoundMessage msg = StartRoundMessage.create(czarId, blackCardText);
        assertEquals(msg.czarId(), czarId);
        assertEquals(msg.blackCardText(), blackCardText);

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testChooseWhiteCardMessage() {
        // Test the message
        String whiteCardText = "Stupid answer";
        ChooseWhiteCardMessage msg = ChooseWhiteCardMessage.create(whiteCardText);
        assertEquals(msg.cardText(), whiteCardText);

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testChooseWinnerMessage() {
        // Test the message
        String cardText = "Tarzanelia";
        ChooseWinnerMessage msg = ChooseWinnerMessage.create(cardText);
        assertEquals(msg.cardText(), cardText);

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testEndRoundMessage() {
        // Test the message
        EndRoundMessage msg = EndRoundMessage.create();

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testAskCardMessage() {
        // Test the message
        AskCardMessage msg = AskCardMessage.create();

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testSendCardMessage() {
        // Test the message
        String cardText = "Mpoumpounea";
        SendCardMessage msg = SendCardMessage.create(cardText);
        assertEquals(msg.cardText(), cardText);

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testChatMessage() {
        // Test the message
        String chat = "Mpoumpounea";
        ChatMessage msg = ChatMessage.create(chat);
        assertEquals(msg.getText(), chat);

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };

    @Test
    public void testSwapCardsMessage() {
        // Test the message
        SwapCardsMessage msg = SwapCardsMessage.create();

        // Test Visitor
        msg.accept(messageVisitor);
        verify(messageVisitor, times(1)).visit(msg);
    };
}

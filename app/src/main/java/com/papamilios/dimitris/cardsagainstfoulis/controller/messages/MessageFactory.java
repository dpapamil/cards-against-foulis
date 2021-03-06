package com.papamilios.dimitris.cardsagainstfoulis.controller.messages;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

public class MessageFactory {
    public static GameMessage create(byte[] msgBuf) {
        if (msgBuf.length <= 0) {
            throw new AssertionError("Invalid message");
        }

        MessageType type = MessageType.from((int) msgBuf[0]);
        switch (type) {
            case START_ROUND: {
                return new StartRoundMessage(msgBuf);
            }
            case CHOOSE_WHITE_CARD: {
                return new ChooseWhiteCardMessage(msgBuf);
            }
            case CHOOSE_WINNER: {
                return new ChooseWinnerMessage(msgBuf);
            }
            case END_ROUND: {
                return new EndRoundMessage(msgBuf);
            }
            case ASK_CARD: {
                return new AskCardMessage(msgBuf);
            }
            case SEND_CARD: {
                return new SendCardMessage(msgBuf);
            }
            case CHAT: {
                return new ChatMessage(msgBuf);
            }
            case SWAP_CARDS: {
                return new SwapCardsMessage(msgBuf);
            }
            default: {
                throw new AssertionError("Message type isn't being handled");
            }
        }
    };
}

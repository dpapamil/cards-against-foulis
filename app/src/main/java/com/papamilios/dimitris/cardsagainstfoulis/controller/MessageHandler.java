package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.util.Log;

import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.AskCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWhiteCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWinnerMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.EndRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IMessageVisitor;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SendCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.StartRoundMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler implements IMessageVisitor {

    // Member Variables
    GameController mGameController;

    public MessageHandler(GameController controller) {
        mGameController = controller;
    }

    public void visit(StartRoundMessage msg) {
        mGameController.onStartRound(msg.czarId(), msg.blackCardText());
    };

    public void visit(ChooseWhiteCardMessage msg) {
        mGameController.onGetChosenCard(msg.cardText(), msg.senderId());
    };

    public void visit(ChooseWinnerMessage msg) {
        mGameController.onGetWinningCard(msg.cardText());
    };

    public void visit(EndRoundMessage msg) {
        mGameController.onEndRound(msg.senderId());
    };

    public void visit(AskCardMessage msg) {

    };

    public void visit(ChatMessage msg) {

    };

    public void visit(SendCardMessage msg) {
        mGameController.onReceiveCard(msg.cardText());
    };

    // Send the Start Round message
    public void sendStartRoundMsg(String czarId, String blackCardText) {
        GameMessage msg = StartRoundMessage.create(czarId, blackCardText);
        sendMsgToAll(msg);
    }

    // Send an End Round message to the host
    public void sendEndRoundMsg() {
        GameMessage msg = EndRoundMessage.create();
        sendMsg(msg, mGameController.getHost());
    }

    // Send a white card to the given participant
    public void sendCardMsg(String cardText, Participant player) {
        GameMessage msg = SendCardMessage.create(cardText);
        sendMsg(msg, player);
    }

    // Send the white card we chose as the answer
    public void sendChooseWhiteCardMsg(String cardText) {
        GameMessage msg = ChooseWhiteCardMessage.create(cardText);
        sendMsgToAll(msg);
    }

    public void sendChooseWinnerMsg(String cardText) {
        GameMessage msg = ChooseWinnerMessage.create(cardText);
        sendMsgToAll(msg);
    }

    // Send the given messag to all participants
    public void sendMsgToAll(GameMessage msg) {
        List<Participant> enemies = mGameController.getMortalEnemies();
        for (Participant p : enemies) {
            sendMsg(msg, p);
        }
    }

    // Send the given message
    public void sendMsg(GameMessage msg, Participant receiver) {
        if (receiver.getStatus() != Participant.STATUS_JOINED) {
            return;
        }

        mGameController.client().sendReliableMessage(msg.bytes(), mGameController.roomId(), receiver.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
            @Override
            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {}
        })
            .addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer tokenId) {

                }
            });
    }
}

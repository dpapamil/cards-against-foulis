package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import android.util.Log;

import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.AskCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWhiteCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWinnerMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.EndRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IMessageVisitor;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SendCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.StartRoundMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;

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

    };

    public void visit(ChooseWinnerMessage msg) {

    };

    public void visit(EndRoundMessage msg) {
        mGameController.onEndRound();
    };

    public void visit(AskCardMessage msg) {

    };

    public void visit(SendCardMessage msg) {

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

    // Send the given messag to all participants
    public void sendMsgToAll(GameMessage msg) {
        ArrayList<Participant> enemies = mGameController.getMortalEnemies();
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

package com.papamilios.dimitris.cardsagainstfoulis.controller;

/*
 * Copyright (C) 2018 Cards Against Foulis Co.
 */

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.AskCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChatMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWhiteCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWinnerMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.EndRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IMessageVisitor;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SendCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.StartRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SwapCardsMessage;

import java.util.List;

public class MessageHandler implements IMessageVisitor {

    // Member Variables
    GameController mGameController;

    private String mGameId = null;

    public MessageHandler(GameController controller) {
        mGameController = controller;
    }

    public void setGameId(@NonNull String gameId) { mGameId = gameId; }
    public String getGameId() { return mGameId; }

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
        mGameController.onReceiveChatMessage(msg);
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
    public void sendCardMsg(String cardText, GamePlayer player) {
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

    public void sendSwapCardsMsg() {
        SwapCardsMessage msg = SwapCardsMessage.create();
        sendMsgToAll(msg);
    }

    public void sendChatMsg(@NonNull ChatMessage chatMsg) {
        sendMsgToAll(chatMsg);
    }

    // Send the given messag to all participants
    public void sendMsgToAll(GameMessage msg) {
        List<GamePlayer> enemies = mGameController.getMortalEnemies();
        for (GamePlayer p : enemies) {
            sendMsg(msg, p);
        }
    }

    // Send the given message
    public void sendMsg(GameMessage msg, GamePlayer receiver) {
        msg.setSenderId(mGameController.getMyId());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference("games").child(mGameId);
        gameRef.child("users").child(receiver.getId()).child("message").push().updateChildren(msg.toMap());
    }
}

package com.papamilios.dimitris.cardsagainstfoulis;

import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.AskCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWhiteCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.ChooseWinnerMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.EndRoundMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.GameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.IGameMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.MessageFactory;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.SendCardMessage;
import com.papamilios.dimitris.cardsagainstfoulis.controller.messages.StartRoundMessage;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class MessageFactoryTest {

    @Test
    public void testStartRoundMessage() {
        StartRoundMessage initialMsg = StartRoundMessage.create("1", "How many tests ...");
        testMessage(initialMsg);
    };

    @Test
    public void testChooseWhiteCardMessage() {
        ChooseWhiteCardMessage initialMsg = ChooseWhiteCardMessage.create("Antetokounmpo's palm");
        testMessage(initialMsg);
    };

    @Test
    public void testChooseWinnerMessage() {
        ChooseWinnerMessage initialMsg = ChooseWinnerMessage.create("Foulis");
        testMessage(initialMsg);
    };

    @Test
    public void testEndRoundMessage() {
        EndRoundMessage initialMsg = EndRoundMessage.create();
        testMessage(initialMsg);
    };

    @Test
    public void testAskCardMessage() {
        AskCardMessage initialMsg = AskCardMessage.create();
        testMessage(initialMsg);
    };

    @Test
    public void testSendCardMessage() {
        SendCardMessage initialMsg = SendCardMessage.create("buttplugs");
        testMessage(initialMsg);
    };

    private void testMessage(GameMessage initialMsg) {
        GameMessage msg = MessageFactory.create(initialMsg.bytes());
        assertThat(msg, instanceOf(initialMsg.getClass()));
    }
}

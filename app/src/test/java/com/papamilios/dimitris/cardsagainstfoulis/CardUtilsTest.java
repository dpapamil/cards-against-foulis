package com.papamilios.dimitris.cardsagainstfoulis;

import com.papamilios.dimitris.cardsagainstfoulis.controller.CardUtils;
import com.papamilios.dimitris.cardsagainstfoulis.database.Card;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardUtilsTest {
    @Test
    public void testFindingNumOfAnswers() {
        Card testCard = new Card(0, "____ + ____ = __", false);
        assertEquals(3, CardUtils.numberOfAnswers(testCard));

        testCard = new Card(0, "My two best friends are __ and__", false);
        assertEquals(2, CardUtils.numberOfAnswers(testCard));

        testCard = new Card(0, "When did you first have sex?", false);
        assertEquals(1, CardUtils.numberOfAnswers(testCard));

        testCard = new Card(0, "My wife told me ____", false);
        assertEquals(1, CardUtils.numberOfAnswers(testCard));
    }
}

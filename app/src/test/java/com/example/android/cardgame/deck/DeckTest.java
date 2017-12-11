package com.example.android.cardgame.deck;

import android.util.Log;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Work on 11/30/2017.
 */
public class DeckTest {
    @Test
    public void seedShuffleTest(){
        Deck deckOne = new Deck();
        Deck deckTwo = new Deck();

        long seed = 123475;
        //Random random = new Random(seed);
        deckOne.getSpecificDeck(new Random(seed));
        deckTwo.getSpecificDeck(new Random(seed));

        assertEquals(deckOne.drawCard(), deckTwo.drawCard());
    }
}
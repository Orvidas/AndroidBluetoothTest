package com.example.android.cardgame;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Work on 11/30/2017.
 */
public class BlackjackTest {
    Blackjack blackjack;
    String[] names = {"One", "Two", "Three", "Four"};
    @Test
    public void blackjackConstructorTestOne(){ //Number of players == number of names
        blackjack = new Blackjack(4, names);

        assertNotNull(blackjack);
    }

    @Test
    public void blackjackConstructorTestTwo(){ //Number of players < number of names
        blackjack = new Blackjack(3, names);

        assertNotNull(blackjack);
    }

    @Test
    public void blackjackConstructorTestThree(){ //Number of players > number of names
        blackjack = new Blackjack(5, names);

        assertNotNull(blackjack);
    }

    @Test
    public void currentPlayerIndexTurnTest(){
        blackjack = new Blackjack(5, names);

        assertEquals(0, blackjack.getCurrentPlayerIndex());
    }

    @Test
    public void endPlayerTurnTest(){
        blackjack = new Blackjack(4, names);

        blackjack.endCurrentPlayerTurn();

        assertEquals(1, blackjack.getCurrentPlayerIndex());
    }

    @Test
    public void isDealersTurn() {
        blackjack = new Blackjack(4, names);

        for (int i = 0; i < 4; i++)
            blackjack.endCurrentPlayerTurn();

        assertTrue(blackjack.isDealerTurn());
    }

    @Test
    public void isNotDealersTurn(){
        blackjack = new Blackjack(5, names);

        for (int i = 0; i < 4; i++) {
            blackjack.endCurrentPlayerTurn();
        }

        assertFalse(blackjack.isDealerTurn());
    }

    @Test
    public void emptyArrayOfNames(){
        String[] mNames = {};
        blackjack = new Blackjack(5, mNames);

        assertNotNull(blackjack);
    }

    @Test
    public void doNotPassNumOfPlayers(){
        blackjack = new Blackjack(1, names);

        blackjack.endCurrentPlayerTurn();
    }

    @Test
    public void getPlayers(){
        blackjack = new Blackjack(5, names);

        assertEquals(5, blackjack.getPlayers().length);
    }

    @Test
    public void getCurrentPlayer(){
        blackjack = new Blackjack(3, names);

        assertEquals(blackjack.getPlayers()[0], blackjack.getCurrentPlayer());
    }

    @Test
    public void hitCurrentPlayer(){
        blackjack = new Blackjack(3, names);

        blackjack.hitCurrentPlayer();
        Player currentPlayer = blackjack.getCurrentPlayer();

        assertEquals(3, currentPlayer.getHand().size());
    }
}
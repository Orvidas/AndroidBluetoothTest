package com.example.android.cardgame;

import com.example.android.cardgame.deck.Deck;

/**
 * Created by Work on 11/30/2017.
 */

public class Blackjack {
    private Deck deck;
    private Player dealer;
    private Player[] players;
    private int numOfPlayers;
    private int currentPlayerIndex;

    public Blackjack(int numOfPlayers, String[] names) {
        deck = new Deck(true);
        this.numOfPlayers = numOfPlayers;
        currentPlayerIndex = 0;
        dealer = new Player("Dealer");
        players = new Player[numOfPlayers];
        for (int i = 0; i < numOfPlayers; i++) {
            players[i] = i < names.length ? new Player(names[i]) : new Player("Player " + i);
        }
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void endCurrentPlayerTurn() {
        currentPlayerIndex++;
    }

    public boolean isDealerTurn() {
        return currentPlayerIndex == numOfPlayers;
    }

    public void hitCurrentPlayer() {
        players[currentPlayerIndex].addCardToHand(deck.drawCard());
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }
}

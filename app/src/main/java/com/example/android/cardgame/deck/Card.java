/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.cardgame.deck;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Objects;

/**
 *
 * @author Orlando
 */
public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    public Suit getSuit(){
        return suit;
    }
    
    public Rank getRank(){
        return rank;
    }

    void print() {
        System.out.println(suit + " of " + rank);
    }
    
    @Override
    public boolean equals(Object other){
        if (other == this) return true;
        
        if (!(other instanceof Card)) return false;
        
        Card card = (Card) other;
        return suit == card.suit && rank == card.rank;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.suit);
        hash = 41 * hash + Objects.hashCode(this.rank);
        return hash;
    }
}



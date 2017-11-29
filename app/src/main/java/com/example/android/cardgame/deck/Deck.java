/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.android.cardgame.deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Orlando
 */
public class Deck {
    private List<Card> deck;
    private List<Card> removedCards;
    
    public Deck(){
        createDeck();
    }

    public Deck(boolean shuffleDeck) {
        createDeck();
        
        if(shuffleDeck)
            shuffleDeck();
    }
    
    private void createDeck(){
        List<Card> newDeck = new ArrayList<>();
        
        for(Suit suit : Suit.values())
            for(Rank rank: Rank.values())
                newDeck.add(new Card(suit, rank));
        
        deck = newDeck;
        removedCards = new ArrayList<>();
    }
    
    public final void shuffleDeck(){
        Collections.shuffle(deck);
    }
    
    public Card drawCard(){
        if(deck.isEmpty())
            return null;
        
        Card drawnCard = deck.remove(0);
        removedCards.add(drawnCard);
        
        return drawnCard;
    }

    public Card[] drawCards(int cardsToDraw) {
        Card[] cards = cardsToDraw > 0 ? new Card[cardsToDraw] : new Card[1];
        
        for(int i = 0; i < cardsToDraw; i++)
            cards[i] = drawCard();
        
        return cards;
    }
    
    public int getDeckSize(){
        return deck.size();
    }
    
    public void printDeck(){
        for(Card card : deck)
            card.print();
    }

    public void returnCards(Card... cards) {
        if(deck.size() + cards.length <= 52){
            for(Card returnedCard : cards) {
                boolean cardReturned = false;
                for(Card removedCard : removedCards) {
                    if(removedCard == returnedCard) {
                        deck.add(returnedCard);
                        cardReturned = true;
                        break;
                    }
                }
                if(cardReturned)
                    removedCards.remove(returnedCard);
            }
        }
    }

    public boolean isNotInDeck(Card... cards) {
        boolean allCardsNotInDeck = false;
        
        for(Card card : cards) {
            for(Card removedCard : removedCards)
                if(removedCard == card) {
                    allCardsNotInDeck = true;
                    break;
                }
                else
                    allCardsNotInDeck = false;
            
            if(!allCardsNotInDeck)
                break;
        }
        return allCardsNotInDeck;
    }
}

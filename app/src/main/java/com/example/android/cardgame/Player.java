package com.example.android.cardgame;

import com.example.android.cardgame.deck.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Work on 11/30/2017.
 */

class Player {
    private String name;
    private int score;
    private List<Card> hand;

    Player() {
        score = 0;
        hand = new ArrayList<>();
    }

    Player(String name){
        this.name = name;
        score = 0;
        hand = new ArrayList<>();
    }

    public int getScore() {
        return score;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCardToHand(Card card) {
        hand.add(card);
        score = getScoreFromHand();
    }

    public void clearHand(){
        hand.clear();
    }

    private int getScoreFromHand() {
        int handScore = 0;
        boolean isAceInHand = false;
        for (Card card : hand){
            switch (card.getRank()){
                case A:
                    if(!isAceInHand)
                        isAceInHand = true;
                    else
                        handScore++;
                    break;
                case TWO:
                    handScore += 2;
                    break;
                case THREE:
                    handScore += 3;
                    break;
                case FOUR:
                    handScore += 4;
                    break;
                case FIVE:
                    handScore += 5;
                    break;
                case SIX:
                    handScore += 6;
                    break;
                case SEVEN:
                    handScore += 7;
                    break;
                case EIGHT:
                    handScore += 8;
                    break;
                case NINE:
                    handScore += 9;
                    break;
                case TEN:
                case JACK:
                case QUEEN:
                case KING:
                    handScore += 10;
                    break;
            }
        }

        if(isAceInHand)
            handScore += handScore + 11 <= 21 ? 11 : 1;

        return handScore;
    }
}

package com.example.android.cardgame;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.cardgame.deck.Card;
import com.example.android.cardgame.deck.Deck;
import com.example.android.cardgame.deck.Rank;
import com.example.android.cardgame.deck.Suit;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Deck deck = new Deck(true);
    LinearLayout dealerLayout;
    LinearLayout playerLayout;

    List<Card> dealerHand = new ArrayList<>();
    List<Card> playerHand = new ArrayList<>();

    int dealerScore;
    int playerScore;

    boolean dealersTurn = false;

    final int[][] allDrawableCards = new int[][] {
            //clubs
            {R.drawable.ic_ace_of_clubs, R.drawable.ic_2_of_clubs, R.drawable.ic_3_of_clubs, R.drawable.ic_4_of_clubs, R.drawable.ic_5_of_clubs, R.drawable.ic_6_of_clubs, R.drawable.ic_7_of_clubs, R.drawable.ic_8_of_clubs, R.drawable.ic_9_of_clubs, R.drawable.ic_10_of_clubs, R.drawable.ic_jack_of_clubs, R.drawable.ic_queen_of_clubs, R.drawable.ic_king_of_clubs },
            //spades
            {R.drawable.ic_ace_of_spades, R.drawable.ic_2_of_spades, R.drawable.ic_3_of_spades, R.drawable.ic_4_of_spades, R.drawable.ic_5_of_spades, R.drawable.ic_6_of_spades, R.drawable.ic_7_of_spades, R.drawable.ic_8_of_spades, R.drawable.ic_9_of_spades, R.drawable.ic_10_of_spades, R.drawable.ic_jack_of_spades, R.drawable.ic_queen_of_spades, R.drawable.ic_king_of_spades },
            //diamonds
            {R.drawable.ic_ace_of_diamonds, R.drawable.ic_2_of_diamonds, R.drawable.ic_3_of_diamonds, R.drawable.ic_4_of_diamonds, R.drawable.ic_5_of_diamonds, R.drawable.ic_6_of_diamonds, R.drawable.ic_7_of_diamonds, R.drawable.ic_8_of_diamonds, R.drawable.ic_9_of_diamonds, R.drawable.ic_10_of_diamonds, R.drawable.ic_jack_of_diamonds, R.drawable.ic_queen_of_diamonds, R.drawable.ic_king_of_diamonds },
            //hearts
            {R.drawable.ic_ace_of_hearts, R.drawable.ic_2_of_hearts, R.drawable.ic_3_of_hearts, R.drawable.ic_4_of_hearts, R.drawable.ic_5_of_hearts, R.drawable.ic_6_of_hearts, R.drawable.ic_7_of_hearts, R.drawable.ic_8_of_hearts, R.drawable.ic_9_of_hearts, R.drawable.ic_10_of_hearts, R.drawable.ic_jack_of_hearts, R.drawable.ic_queen_of_hearts, R.drawable.ic_king_of_hearts }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dealerLayout = findViewById(R.id.dealer_layout);
        playerLayout = findViewById(R.id.player_layout);

        startGame();
    }

    void startGame(){
        dealersTurn = false;
        playerHand.add(deck.drawCard());
        playerHand.add(deck.drawCard());

        dealerHand.add(deck.drawCard());
        dealerHand.add(deck.drawCard());

        updateHandView(playerHand, playerLayout, false);
        updateHandView(dealerHand, dealerLayout, true);
        updateScoreView();
    }

    void resetGameClick(View view){
        resetGame();
    }

    private void resetGame() {
        deck.returnCards(playerHand.toArray(new Card[0]));
        deck.returnCards(dealerHand.toArray(new Card[0]));
        deck.shuffleDeck();

        playerHand.clear();
        dealerHand.clear();

        recreate();
    }

    private void updateScoreView() {
        playerScore = getScoreFromHand(playerHand);
        dealerScore = getScoreFromHand(dealerHand);

        TextView playerScoreTextView = findViewById(R.id.player_score);
        TextView dealerScoreTextView = findViewById(R.id.dealer_score);
        playerScoreTextView.setText("Player Score = " + playerScore);
        if(dealersTurn)
            dealerScoreTextView.setText("Dealer Score = " + dealerScore);
        else
            dealerScoreTextView.setText("Dealer Score = ?");
    }

    private int getScoreFromHand(List<Card> hand) {
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
            handScore += (handScore + 11 <= 21) ? 11 : 1;

        return handScore;
    }

    private int getDpAmount(int sizeOfHand, LinearLayout layout, ImageView image) {
        final int maxLeftMargin = -100;
        int layoutWidthDp = pixelsToDp(layout.getWidth());
        int cardWidthDp = pixelsToDp(image.getWidth());
        int leftMargin = (int) Math.floor((cardWidthDp - layoutWidthDp)/(sizeOfHand-1) + cardWidthDp) * -1;
        leftMargin = leftMargin <= 0 ? leftMargin : 0;
        return leftMargin < maxLeftMargin ? maxLeftMargin : leftMargin;
    }

    private ImageView getCardImage(int dpLeftMargin, Drawable drawableFromCard) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setMargins(dpToPixels(dpLeftMargin), 0, 0, 0);
        imageView.setLayoutParams(layout);
        imageView.setAdjustViewBounds(true);
        imageView.setImageDrawable(drawableFromCard);
        imageView.setBackgroundDrawable((getResources().getDrawable(R.drawable.card_border)));

        return imageView;
    }

    int dpToPixels(int dp){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float pixels = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Math.round(pixels);
    }

    int pixelsToDp(int pixels){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float dp = pixels / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Math.round(dp);
    }

    Drawable getDrawableFromCard(Card cardToDraw){
        int x = 0, y = 0;

        for(int i = 0; i < Suit.values().length; i++){
            if(cardToDraw.getSuit() == Suit.values()[i]){
                x = i;
                break;
            }
        }

        for(int j = 0; j < Rank.values().length; j++){
            if(cardToDraw.getRank() == Rank.values()[j]){
                y = j;
                break;
            }
        }

        return  getResources().getDrawable(allDrawableCards[x][y]);
    }

    void addCardToPlayerHand(View view){
        playerHand.add(deck.drawCard());
        updateHandView(playerHand, playerLayout, false);
        updateScoreView();

        if(playerScore > 21) {
            disableButtons();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playerLoses();
                }
            }, 1000);
        }
    }

    void stayButtonClicked(View view){
        dealersTurn = true;
        disableButtons();
        updateHandView(dealerHand, dealerLayout, false);
        updateScoreView();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(dealerScore < 17){
                    dealerHand.add(deck.drawCard());
                    updateHandView(dealerHand, dealerLayout, false);
                    updateScoreView();
                    handler.postDelayed(this, 1500);
                }
                else if (dealerScore > playerScore && dealerScore <= 21)
                    playerLoses();
                else if (dealerScore == playerScore)
                    displayEndGameMessage("It's a tie!");
                else
                    playerWins();
            }
        }, 1500);
    }

    private void disableButtons() {
        Button hitMeButton = (Button) findViewById(R.id.hit_me_button);
        Button stayButton = (Button) findViewById(R.id.stay_button);
        Button resetButton = (Button) findViewById(R.id.reset_button);

        hitMeButton.setEnabled(false);
        stayButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void displayEndGameMessage(String message) {
        TextView endMessage = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        endMessage.setLayoutParams(textViewParams);
        endMessage.setGravity(Gravity.CENTER);
        endMessage.setTextSize(64);
        endMessage.setBackgroundColor(Color.TRANSPARENT);
        endMessage.setText(message + "\nPlayer: " + playerScore + " - Dealer: " + dealerScore);

        ViewGroup rootLayout = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        rootLayout.addView(endMessage, 0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetGame();
            }
        }, 3000);
    }

    private void playerLoses() {
        TextView loseMessage = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        loseMessage.setLayoutParams(textViewParams);
        loseMessage.setGravity(Gravity.CENTER);
        loseMessage.setTextSize(64);
        loseMessage.setBackgroundColor(Color.TRANSPARENT);
        loseMessage.setText("You Lose!\nPlayer: " + playerScore + " - Dealer: " + dealerScore);

        ViewGroup rootLayout = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        rootLayout.addView(loseMessage, 0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetGame();
            }
        }, 3000);
    }

    private void playerWins() {
        TextView winMessage = new TextView(this);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        winMessage.setLayoutParams(textViewParams);
        winMessage.setGravity(Gravity.CENTER);
        winMessage.setTextSize(64);
        winMessage.setBackgroundColor(Color.TRANSPARENT);
        winMessage.setText("You Win!\nPlayer: " + playerScore + " - Dealer: " + dealerScore);

        ViewGroup rootLayout = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        rootLayout.addView(winMessage, 0);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetGame();
            }
        }, 3000);
    }

    private void updateHandView(final List<Card> hand, final LinearLayout layout, boolean hideFirstCard) {
        Drawable backCard = getResources().getDrawable(R.drawable.ic_back);
        layout.removeAllViews();

        final ImageView defaultImage = hideFirstCard ? getCardImage(0, backCard) : getCardImage(0, getDrawableFromCard(hand.get(0)));
        layout.addView(defaultImage);

        final ViewTreeObserver viewTreeObserver = defaultImage.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                defaultImage.getViewTreeObserver().removeOnPreDrawListener(this);

                int dpAmount = getDpAmount(hand.size(), layout, defaultImage);
                for (int i = 1; i < hand.size(); i++){
                    layout.addView(getCardImage(dpAmount, getDrawableFromCard(hand.get(i))));
                }
                return true;
            }
        } );
    }
}
package com.example.android.cardgame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.cardgame.deck.*;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class TwoPlayerBlackjackActivity extends AppCompatActivity {
    private BluetoothGameService gameService;
    private boolean isHost;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            finish();
            return;
        }
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        gameService = new BluetoothGameService(this, gameHandler);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String status = extras.getString("player_status");
            isHost = status.equals("Host");

            if(isHost) {
                setContentView(R.layout.activity_host_menu);
                startHosting();
            } else {
                setContentView(R.layout.activity_bluetooth);
                showPairedDevices();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(gameService != null) {
            gameService.stopThreads();
        }

        super.onBackPressed();
    }

    private void showPairedDevices() {
        final ListView deviceListView = findViewById(R.id.device_list_view);
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayAdapter<String> itemsToShow = new ArrayAdapter<String>(this, R.layout.device_name_text_view);
        for (BluetoothDevice device : pairedDevices) {
            itemsToShow.add(device.getName() + "\n" + device.getAddress());
        }
        deviceListView.setAdapter(itemsToShow);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nameAndAddress = ((TextView) view).getText().toString();
                for (BluetoothDevice device : pairedDevices) {
                    if (nameAndAddress.equals(device.getName() + "\n" + device.getAddress())) {
                        gameService.searchForOpenGame(device);
                        break;
                    }
                }
            }
        });
    }

    Button startHostingButton;
    Button cancelHostingButton;

    private void startHosting() {
        startHostingButton = findViewById(R.id.start_host_button);
        cancelHostingButton = findViewById(R.id.cancel_host_button);

        startHostingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHostingButton.setEnabled(false);
                cancelHostingButton.setEnabled(true);
                gameService.startHostingGame();
            }
        });

        cancelHostingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHostingButton.setEnabled(true);
                cancelHostingButton.setEnabled(false);
                gameService.stopThreads();
            }
        });
    }

    private final Handler gameHandler = new Handler() {
        boolean receivedDeckSeed = false;

        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case BluetoothGameService.STATE_CONNECTED:
                    startGame();
                    break;
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    if(!receivedDeckSeed) {
                        receivedDeckSeed = true;
                        String readMsg = new String(readBuffer, 0, msg.arg1);
                        long deckSeed = Long.parseLong(readMsg);
                        clientSetupBlackjack(deckSeed);
                    }
                    break;
            }
        }
    };

    int myTurn;

    String[] names = {};
    Deck deck;
    Player player;
    Player opponent;
    Player dealer;

    LinearLayout opponentLayout;
    LinearLayout playerLayout;

    private void clientSetupBlackjack(long deckSeed) {
        playerLayout = findViewById(R.id.current_player_layout);
        opponentLayout = findViewById(R.id.opponent_layout);

        deck = new Deck(deckSeed);
        player = new Player("You");
        opponent = new Player("Host");
        dealer = new Player("Dealer");

        opponent.addCardToHand(deck.drawCard());
        opponent.addCardToHand(deck.drawCard());

        player.addCardToHand(deck.drawCard());
        player.addCardToHand(deck.drawCard());

        dealer.addCardToHand(deck.drawCard());
        dealer.addCardToHand(deck.drawCard());

        updateHandView(player.getHand(), playerLayout, false);
        updateHandView(opponent.getHand(), opponentLayout, false);
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

    private int getDpAmount(int sizeOfHand, LinearLayout layout, ImageView image) {
        final int maxLeftMargin = -100;
        int layoutWidthDp = pixelsToDp(layout.getWidth());
        int cardWidthDp = pixelsToDp(image.getWidth());
        int leftMargin = (int) Math.floor((cardWidthDp - layoutWidthDp)/(sizeOfHand-1) + cardWidthDp) * -1;
        leftMargin = leftMargin <= 0 ? leftMargin : 0;
        return leftMargin < maxLeftMargin ? maxLeftMargin : leftMargin;
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

    private void startGame() {
        setContentView(R.layout.activity_two_player_blackjack);

        myTurn = isHost ? 0 : 1;

        opponentLayout = findViewById(R.id.opponent_layout);
        playerLayout = findViewById(R.id.current_player_layout);

        if(isHost)
            startHostBlackjack();
    }

    private void startHostBlackjack() {
        long seed = new Random().nextLong();
        gameService.writeString(String.valueOf(seed));

        deck = new Deck(seed);
        player = new Player("You");
        opponent = new Player("Client");
        dealer = new Player("Dealer");

        player.addCardToHand(deck.drawCard());
        player.addCardToHand(deck.drawCard());

        opponent.addCardToHand(deck.drawCard());
        opponent.addCardToHand(deck.drawCard());


        dealer.addCardToHand(deck.drawCard());
        dealer.addCardToHand(deck.drawCard());

        updateHandView(player.getHand(), playerLayout, false);
        updateHandView(opponent.getHand(), opponentLayout, false);
    }

    Drawable getDrawableFromCard(Card cardToDraw){
        int x = 0, y = 0;

        for(int i = 0; i < Suit.values().length; i++){
            if(cardToDraw.getSuit() == Suit.values()[i]){
                x = i;
                break;
            }
        }

        for(int j = 0; j < Rank.values().length; j++) {
            if (cardToDraw.getRank() == Rank.values()[j]) {
                y = j;
                break;
            }
        }
        return  getResources().getDrawable(allDrawableCards[x][y]);
    }
}

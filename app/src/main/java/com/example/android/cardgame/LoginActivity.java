package com.example.android.cardgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    TextView onePlayerOption;
    TextView twoPlayerOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        onePlayerOption = findViewById(R.id.one_player_game);
        twoPlayerOption = findViewById(R.id.two_player_game);

        onePlayerOption.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOnePlayerGame();
            }
        });

        twoPlayerOption.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v){
                selectTwoPlayerGame();
            }
        });
    }

    private void selectTwoPlayerGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View layout = inflater.inflate(R.layout.bluetooth_host_client_option, null);
        final RadioButton hostRadioButton = layout.findViewById(R.id.host_radio_button);
        final RadioButton clientRadioButton = layout.findViewById(R.id.client_radio_button);

        builder.setView(layout)
                .setTitle("Host or find game")
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(hostRadioButton.isChecked()){
                            hostANewGame();
                        }
                        else if(clientRadioButton.isChecked()){
                            searchForAnOpenGame();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Select an option", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
        });
        builder.create().show();
    }

    void startOnePlayerGame(){
        final Intent onePlayerGame = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(onePlayerGame);
    }

    void searchForAnOpenGame(){
        final Intent twoPlayerGame = new Intent(LoginActivity.this, TwoPlayerBlackjackActivity.class);
        twoPlayerGame.putExtra("player_status", "Client");
        startActivity(twoPlayerGame);
    }

    private void hostANewGame() {
        final Intent hostGame = new Intent(LoginActivity.this, TwoPlayerBlackjackActivity.class);
        hostGame.putExtra("player_status", "Host");
        startActivity(hostGame);
    }

    private void testToolbarActivity(){
        final Intent toolbar = new Intent(LoginActivity.this, ToolbarTestActivity.class);
        startActivity(toolbar);
    }
}

package com.example.android.cardgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class HostMenuActivity extends AppCompatActivity {
    private BluetoothGameService bluetoothGameService;

    private Button startHostingButton;
    private Button cancelHostingButton;

    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_menu);

        bluetoothGameService = new BluetoothGameService(mHandler);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            finish();
            return;
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        startHostingButton = findViewById(R.id.start_host_button);
        cancelHostingButton = findViewById(R.id.cancel_host_button);

        startHostingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHostingButton.setEnabled(false);
                cancelHostingButton.setEnabled(true);
                bluetoothGameService.startHostingGame();
                //setUpHosting();
            }
        });

        cancelHostingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHostingButton.setEnabled(true);
                cancelHostingButton.setEnabled(false);
                bluetoothGameService.stopThreads();
            }
        });
    }

    private void setUpHosting(){
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case BluetoothGameService.STATE_CONNECTED:
                    startMultiplayer();
                    break;
                case MessageConstants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readMsg = new String(readBuffer, 0, msg.arg1);
                    Toast.makeText(HostMenuActivity.this, "Read message: " + readMsg, Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMsg = new String(writeBuffer);
                    Toast.makeText(HostMenuActivity.this, "Message send: " + writeMsg, Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.MESSAGE_TOAST:
                    Toast.makeText(HostMenuActivity.this, "Possible error", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    Button readButton;
    Button sendButton;

    private void startMultiplayer() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

//        setContentView(R.layout.host_button_test);
//
//        sendButton = findViewById(R.id.send_button);
//        readButton = findViewById(R.id.read_button);
//
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bluetoothGameService.writeString("Greetings guest!");
//            }
//        });
    }
}

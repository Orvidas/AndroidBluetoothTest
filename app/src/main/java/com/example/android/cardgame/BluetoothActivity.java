package com.example.android.cardgame;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothGameService bluetoothGameService;

    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> itemsToShow;
    ListView deviceListView;
    Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        itemsToShow = new ArrayAdapter<String>(this, R.layout.device_name_text_view);
        deviceListView = findViewById(R.id.device_list_view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            finish();
            return;
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        bluetoothGameService = new BluetoothGameService(this, mHandler);

        queryPairedDevices();
    }

    @Override
    public void onBackPressed() {
        if(bluetoothGameService != null) {
            bluetoothGameService.stopThreads();
        }

        super.onBackPressed();
    }

    private void queryPairedDevices() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices)
            itemsToShow.add(device.getName() + "\n" + device.getAddress());
        deviceListView.setAdapter(itemsToShow);
        deviceListView.setOnItemClickListener(selectDeviceListener);
    }

    private AdapterView.OnItemClickListener selectDeviceListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String nameAndAddress = ((TextView) view).getText().toString();
            Toast.makeText(BluetoothActivity.this, "Selected " + nameAndAddress, Toast.LENGTH_SHORT).show();
            for(BluetoothDevice device : pairedDevices){
                if(nameAndAddress.equals(device.getName() + "\n" + device.getAddress())){
                    setupClient(device);
                    break;
                }
            }
        }
    };

    private void setupClient(BluetoothDevice device) {
        bluetoothGameService.searchForOpenGame(device);
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
                    Toast.makeText(BluetoothActivity.this, "Read message: " + readMsg, Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMsg = new String(writeBuffer);
                    Toast.makeText(BluetoothActivity.this, "Message send: " + writeMsg, Toast.LENGTH_SHORT).show();
                    break;
                case MessageConstants.MESSAGE_TOAST:
                    Toast.makeText(BluetoothActivity.this, "Possible error", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void startMultiplayer() {
        setContentView(R.layout.host_button_test);
        Button sendButton = findViewById(R.id.send_button);
        Button readButton = findViewById(R.id.read_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothGameService.writeString("Hello host");
            }
        });
    }
}

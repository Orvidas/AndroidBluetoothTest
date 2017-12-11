package com.example.android.cardgame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.android.cardgame.MessageConstants.*;

/**
 * Created by Work on 11/25/2017.
 */

class BluetoothGameService {
    private ConnectedThread connectedThread = null;
    private HostThread hostThread = null;
    private ClientThread clientThread = null;

    private static final UUID mUUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String mName = "Blackjack";
    private static final String TAG = "BluetoothGameService";

    private final Handler gameHandler;
    private final BluetoothAdapter bluetoothAdapter;

    //private BluetoothSocket bluetoothSocket;

    private int currentState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1; //Exclusive to host thread
    public static final int STATE_CONNECTING = 2; //Exclusive to client thread
    public static final int STATE_CONNECTED = 3;

    BluetoothGameService(Handler handler) {
        currentState = STATE_NONE;
        gameHandler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    synchronized void startHostingGame(){
        if(hostThread != null) {
            hostThread.cancel();
            hostThread = null;
        }
        hostThread = new HostThread();
        hostThread.start();
    }

    synchronized void searchForOpenGame(BluetoothDevice device) {
        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }
        clientThread = new ClientThread(device);
        clientThread.start();
    }

    private synchronized void establishConnection(BluetoothSocket mBluetoothSocket) {
        if(hostThread != null) {
            hostThread.cancel();
            hostThread = null;
        }
        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }
        if(connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        connectedThread = new ConnectedThread(mBluetoothSocket);
        connectedThread.start();
    }

    private void closeIndividualThreads(){
        if(hostThread != null) {
            hostThread.cancel();
            hostThread = null;
        }
    }

    void writeString(String message) {
        byte[] msgByte = message.getBytes();

        if (connectedThread != null) {
            connectedThread.write(msgByte);
        }
    }

    void writeString(String message, int messageType){
        byte[] msgByte = message.getBytes();

        if(connectedThread != null) {
            connectedThread.write(msgByte, messageType);
        }
    }

    private void connectionFailed() {
        Message message = gameHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect to host");
        message.setData(bundle);
        gameHandler.sendMessage(message);

        currentState = STATE_NONE;
    }

    private void connectionLost(){
        Message message = gameHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Connection to the other device was lost");
        message.setData(bundle);
        gameHandler.sendMessage(message);

        currentState = STATE_NONE;

//        BluetoothGameService.this.start();
    }

    void stopThreads() {
        if(connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if(hostThread != null) {
            hostThread.cancel();
            hostThread = null;
        }
        if(clientThread != null){
            clientThread.cancel();
            clientThread = null;
        }

        currentState = STATE_NONE;
    }

    //Uses bluetoothAdapter, currentState, and establishConnection method.
    private class HostThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        HostThread(){
            BluetoothServerSocket tempServerSocket = null;
            try{
                tempServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(mName, mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }

            currentState = STATE_LISTEN;
            serverSocket = tempServerSocket;
        }

        public void run(){
            BluetoothSocket tempSocket;

            while(currentState != STATE_CONNECTED){
                try{
                    tempSocket = serverSocket.accept();
                } catch (IOException e){
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if(tempSocket != null) {
                    //Manage host connections on a separate
                    synchronized (BluetoothGameService.this) {
                        if (currentState == STATE_LISTEN) {
                            establishConnection(tempSocket);
                        }
                        break;
                    }
                }
            }
        }

        void cancel(){
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the server socket connection", e);
            }
        }
    }
    //uses currentState, bluetoothAdapter, connectionFailed method, and establishConnection method.
    private class ClientThread extends Thread {
        private final BluetoothDevice bluetoothDevice;
        private BluetoothSocket tempSocket = null;

        ClientThread(BluetoothDevice device){
            try {
                tempSocket = device.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }

            currentState = STATE_CONNECTING;
            bluetoothDevice = device;
        }

        public void run(){
            bluetoothAdapter.cancelDiscovery();

            try{
                tempSocket.connect();
            } catch (IOException connectException) {
                cancel();
                connectionFailed();
                return;
            }

            //Manage client connection in separate thread
            synchronized (BluetoothGameService.this) {
                clientThread = null;
            }
            establishConnection(tempSocket);
        }

        void cancel(){
            try {
                tempSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    //closeIndividualThreads, gameHandler, currentState, and gameHandler method
    private class ConnectedThread extends Thread{
        private BluetoothSocket mBluetoothSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private byte[] mBuffer;

        ConnectedThread(BluetoothSocket theSocket){
            InputStream tempInput = null;
            OutputStream tempOutput = null;
            mBluetoothSocket = theSocket;

            try{
                tempInput = mBluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try{
                tempOutput = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mInputStream = tempInput;
            mOutputStream = tempOutput;
            currentState = STATE_CONNECTED;

            closeIndividualThreads();

            gameHandler.obtainMessage(STATE_CONNECTED).sendToTarget();
        }

        public void run(){ //Also the read method. Constantly reading
            mBuffer = new byte[1024];
            int numBytes;

            while(currentState == STATE_CONNECTED){
                try {
                    numBytes = mInputStream.read(mBuffer);
                    gameHandler.obtainMessage(MESSAGE_READ, numBytes, -1, mBuffer).sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        void write(byte[] bytes){
            try{
                mOutputStream.write(bytes);

                gameHandler.obtainMessage(MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                Message writeErrorMessage = gameHandler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMessage.setData(bundle);
                gameHandler.sendMessage(writeErrorMessage);
            }
        }

        void write(byte[] bytes, int messageType) {
            try{
                mOutputStream.write(bytes);

                gameHandler.obtainMessage(messageType, -1, -1, bytes).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                Message writeErrorMessage = gameHandler.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMessage.setData(bundle);
                gameHandler.sendMessage(writeErrorMessage);
            }
        }

        void cancel(){
            try{
                mBluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

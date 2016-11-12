package com.lakomy.tomasz.androidpingclient;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class UdpSocketRequestTask extends AsyncTask<Void, Void, Void> {
    DatagramSocket datagramSocket;
    DatagramPacket senderPacket;
    DatagramPacket receiverPacket;

    String message;
    InetAddress ipAddress;
    byte[] receiveBuffer;
    TextView textView;
    int dstPort;
    int packetSize;
    RandomDataGenerator generator;

    UdpSocketRequestTask(String addr, int port, int pSize, TextView txtView) throws UnknownHostException, SocketException {
        datagramSocket = new DatagramSocket();
        ipAddress =  InetAddress.getByName(addr);
        dstPort = port;
        packetSize = pSize;
        generator = new RandomDataGenerator();
        textView = txtView;
    }

    protected void sendData() throws IOException {
        message = generator.generateRandomData(packetSize);
        message = message.trim();

        senderPacket = new DatagramPacket(message.getBytes(), message.length(), ipAddress, dstPort);
        datagramSocket.setBroadcast(true);

        // Store time before request:
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();

        Log.d("aping", "Sending pingTimesEntries through UDP: " + message);
        datagramSocket.send(senderPacket);
    }

    protected void receiveData() throws IOException {
        receiveBuffer = new byte[packetSize];
        receiverPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        // Receive the UDP-Packet
        datagramSocket.receive(receiverPacket);
        publishProgress();
        Log.d("aping", "Received pingTimesEntries through UDP: " + new String(receiverPacket.getData()));
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        try {
            PingServerActivity.updateRequestStatistics();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PingServerActivity.updateCurrentResults(textView);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try
        {
            while (!PingServerActivity.shouldCancelNextRequest()) {
                sendData();
                receiveData();
                Thread.sleep(PingServerActivity.requestInterval);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (datagramSocket != null)
            {
                datagramSocket.close();
            }
        }
        return null;
    }
}

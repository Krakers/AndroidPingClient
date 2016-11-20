package com.lakomy.tomasz.androidpingclient;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

class TcpSocketRequestTask extends AsyncTask<Void, Void, Void> {
    String dstAddress;
    int dstPort;
    String response = "";
    int packetSize;
    TextView textView;
    Socket socket;
    RandomDataGenerator generator;
    PrintWriter outputStream;

    TcpSocketRequestTask(String addr, int port, int pSize, TextView txtView) {
        Log.d("aping", "TcpSocketRequestTask: " + addr + " port:" + port);
        dstAddress = addr;
        dstPort = port;
        packetSize = pSize;
        textView = txtView;
        generator = new RandomDataGenerator();
    }

    protected void closeSocket() {
        if(socket != null){
            try {
                // We're closing the socket only if the request interval is higher than a minute:
//                if (PingServerActivity.requestInterval > 60 * 1000) {
                    Log.d("aping", "Closing TCP socket on client side");
                    socket.close();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void sendData() {
        String str = generator.generateRandomData(packetSize);
        str = str.trim();

        // Store time before request:
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();

        if (PingServerActivity.numberOfRequests == 0) {
            // Set packet size on the server side in a first request
            outputStream.println("PACKET_SIZE:" + packetSize);
        }

        // Send pingTimesEntries:
        Log.d("aping", "Sending pingTimesEntries: " + str);
        outputStream.println(str);
    }

    protected void receiveData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(packetSize);
        byte[] buffer = new byte[packetSize];
        response = "";

        int bytesRead;
        InputStream inputStream = socket.getInputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            response += byteArrayOutputStream.toString("UTF-8");
        }
        publishProgress();
        Log.d("aping", "Received pingTimesEntries: " + response);
    }

    protected void resetPacketSize() throws IOException {
        if (PingServerActivity.shouldCancelNextRequest()) {
            // Reset PACKET_SIZE on server-side
            if (socket.isClosed()) {
                socket = new Socket(dstAddress, dstPort);
                outputStream = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
            }
            Log.d("aping", "Resetting PACKET_SIZE");
            outputStream.println("PACKET_SIZE:" + 65000);
            closeSocket();
        }
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
    protected Void doInBackground(Void... arg0) {
        try {
            socket = new Socket(dstAddress, dstPort);
            outputStream = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);

            while (!PingServerActivity.shouldCancelNextRequest()) {
                if (socket.isClosed()) {
                    socket = new Socket(dstAddress, dstPort);
                    outputStream = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())), true);
                }

                Thread.sleep(PingServerActivity.requestInterval);
                sendData();
                receiveData();
                closeSocket();
            }

            resetPacketSize();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d("aping", "UnknownHostException: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("aping", "IOException: " + e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}

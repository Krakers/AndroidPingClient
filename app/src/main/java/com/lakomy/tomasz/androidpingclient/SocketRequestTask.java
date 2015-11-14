package com.lakomy.tomasz.androidpingclient;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

class SocketRequestTask extends AsyncTask<Void, Void, Void> {
    String dstAddress;
    int dstPort;
    String response = "";
    int packetSize;

    SocketRequestTask(String addr, int port, int pSize) {
        Log.d("aping", "SocketRequestTask: " + addr + " port:" + port);
        dstAddress = addr;
        dstPort = port;
        packetSize = pSize;
    }

    public String getResponse() {
        return response;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Socket socket = null;

        try {
            socket = new Socket(dstAddress, dstPort);
            RandomDataGenerator generator = new RandomDataGenerator();
            String str = generator.generateRandomData(packetSize);
            str = str.trim();
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);

            // Set packet size on the server side:
            out.println("PACKET_SIZE:" + packetSize);
            out.println(str);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(packetSize);
            byte[] buffer = new byte[packetSize];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d("aping", response);
    }
}

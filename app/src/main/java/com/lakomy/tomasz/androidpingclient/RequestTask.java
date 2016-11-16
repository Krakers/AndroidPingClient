package com.lakomy.tomasz.androidpingclient;

import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import java.util.TimerTask;

class RequestTask extends TimerTask {
    StringRequest request;
    RequestQueue queue;
    String type;
    String ipAddress;
    int portNumber;
    int packetSize;
    TextView textView;
    TcpSocketRequestTask tcpRequest;
    int requestNumber;

    public RequestTask(String addr, int port, int pSize, TextView txtView) {
        type = "tcp";
        ipAddress = addr;
        portNumber = port;
        packetSize = pSize;
        textView = txtView;
    }

    public RequestTask(StringRequest stringRequest, RequestQueue requestQueue) {
        request = stringRequest;
        queue = requestQueue;
        type = "http";
        requestNumber = 0;
    }

    public void run() {
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();

        if (type.equals("http")) {
            requestNumber++;
            Log.d("aping", "sending http request " + requestNumber);
            queue.add(request);
        } else {
            TcpSocketRequestTask tcpRequest = new TcpSocketRequestTask(ipAddress, portNumber, packetSize, textView);
            tcpRequest.execute();
        }
    }
}
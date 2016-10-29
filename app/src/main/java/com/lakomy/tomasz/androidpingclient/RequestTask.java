package com.lakomy.tomasz.androidpingclient;

import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.w3c.dom.Text;

import java.util.TimerTask;

class RequestTask extends TimerTask {
    StringRequest request;
    RequestQueue queue;
    String type;
    String ipAddress;
    int portNumber;
    int packetSize;
    TextView textView;

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
    }

    public void run() {
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();

        if (type.equals("http")) {
            queue.add(request);
        } else {
            SocketRequestTask tcpRequest = new SocketRequestTask(ipAddress, portNumber, packetSize, textView);
            tcpRequest.execute();
        }
    }
}
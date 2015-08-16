package com.lakomy.tomasz.androidpingclient;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import java.util.TimerTask;

class RequestTask extends TimerTask {
    StringRequest request;
    RequestQueue queue;
    public RequestTask(StringRequest jsonRequest, RequestQueue requestQueue) {
        request = jsonRequest;
        queue = requestQueue;
    }
    public void run() {
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();
        queue.add(request);
    }
}
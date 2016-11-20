package com.lakomy.tomasz.androidpingclient;

import android.os.AsyncTask;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestTask extends AsyncTask<Void, Void, Void> {
    final Map<String, String> params = new HashMap<>();
    final RandomDataGenerator generator = new RandomDataGenerator();
    final Calendar calendar = Calendar.getInstance();
    TextView pingInfo;
    String url;
    int packetSize;
    RequestQueue queue;

    final Response.Listener successHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) throws IOException {
            PingServerActivity.updateRequestStatistics();
            PingServerActivity.updateCurrentResults(pingInfo);
        }
    };

    final Response.ErrorListener errorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            pingInfo.setText("HTTP request error: " + error.toString());
        }
    };

    HttpRequestTask(String urlAddr, int pSize, RequestQueue requestQueue, TextView pInfo) {
        pingInfo = pInfo;
        url = urlAddr;
        packetSize = pSize;
        queue = requestQueue;
    }

    public CustomStringRequest getStringRequest() {
        CustomStringRequest stringRequest = new CustomStringRequest(url, successHandler, errorHandler) {
            protected Map<String, String> getParams()
            {
                String data = generator.generateRandomData(packetSize);
                params.put("data", data);
                params.put("timestamp", "" + calendar.getTimeInMillis());
                return params;
            }
        };
        stringRequest.setPriority(Request.Priority.IMMEDIATE);
        stringRequest.setShouldCache(false);
        return stringRequest;
    }

    public void sendHttpRequest() {
        PingServerActivity.timeBeforeRequest = System.currentTimeMillis();
        queue.add(getStringRequest());
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (!PingServerActivity.shouldCancelNextRequest()) {
            sendHttpRequest();
            try {
                Thread.sleep(PingServerActivity.requestInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

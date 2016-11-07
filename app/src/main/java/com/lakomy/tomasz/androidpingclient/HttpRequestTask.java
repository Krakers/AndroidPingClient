package com.lakomy.tomasz.androidpingclient;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestTask extends FragmentActivity {
    final Map<String, String> params = new HashMap<>();
    final RandomDataGenerator generator = new RandomDataGenerator();
    final Calendar calendar = Calendar.getInstance();
    TextView pingInfo;
    String url;
    int packetSize;

    final Response.Listener successHandler = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            PingServerActivity.updateRequestStatistics();
            PingServerActivity.updateCurrentResults(pingInfo);
        }
    };

    final Response.ErrorListener errorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            pingInfo.setText("Http request error: " + error.toString());
        }
    };

    HttpRequestTask(String urlAddr, int pSize, TextView pInfo) {
        pingInfo = pInfo;
        url = urlAddr;
        packetSize = pSize;
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
}

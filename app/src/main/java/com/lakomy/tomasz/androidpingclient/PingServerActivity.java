package com.lakomy.tomasz.androidpingclient;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class PingServerActivity extends ActionBarActivity {


    int numberOfRequests;
    int sumOfRequestTimes;
    int averageRequestTime;
    static public long timeBeforeRequest = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_server);
    }

    @Override
    protected void onStart() {
        super.onStart();
        numberOfRequests = 0;
        sumOfRequestTimes = 0;
        averageRequestTime = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ping_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pingServer(final View view) {
        // Instantiate the RequestQueue.
        final TextView mTextView = (TextView) findViewById(R.id.ping_info);
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://2102cb16.ngrok.io";
        Timer timer = new Timer();

        final Response.Listener successHandler = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                long deltaTime = System.currentTimeMillis() - timeBeforeRequest;
                sumOfRequestTimes += deltaTime;
                numberOfRequests++;
                averageRequestTime = sumOfRequestTimes / numberOfRequests;
                mTextView.setText("Request time is: " + deltaTime + " number of requests: " + numberOfRequests
                        + " average request time: " + averageRequestTime);
            }
        };

        final Response.ErrorListener errorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!" + error.toString());
            }
        };
        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                successHandler, errorHandler);

        RequestTask task = new RequestTask(stringRequest, queue);

        // Add the request to the RequestQueue.
        timer.scheduleAtFixedRate(task, new Date(), 1000);
    }
}

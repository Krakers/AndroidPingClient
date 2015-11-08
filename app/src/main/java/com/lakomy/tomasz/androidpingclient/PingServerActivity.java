package com.lakomy.tomasz.androidpingclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class PingServerActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    int numberOfRequests;
    int sumOfRequestTimes;
    int averageRequestTime;
    long lastKnownDeltaTime;
    Timer timer;
    static public long timeBeforeRequest = System.currentTimeMillis();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private double currentLatitude;
    private double currentLongitude;
    RequestQueue queue;
    List<Long> results;

    int packetSize;
    int numberOfPackets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            packetSize = extras.getInt("packet_size", 16);
            numberOfPackets = extras.getInt("number_of_packets", 10);
        }
        Log.d("aping", "packetSize: " + packetSize);
        Log.d("aping", "numberOfPackets: " + numberOfPackets);
        setContentView(R.layout.activity_ping_server);
        setUpMapIfNeeded();
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        numberOfRequests = 0;
        sumOfRequestTimes = 0;
        averageRequestTime = 0;
        lastKnownDeltaTime = 0;
        results = new ArrayList<>();

        queue = Volley.newRequestQueue(this);
        queue.start();
        timer = new Timer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
            final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String generateRandomData(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        Log.d("aping", "randomData: " + sb.toString());

        return sb.toString();
    }

    public boolean shouldCancelNextRequest() {
        return numberOfRequests == numberOfPackets;
    }

    public void pingServer(final View view) {
        final TextView mTextView = (TextView) findViewById(R.id.ping_info);
        String url = "http://192.168.0.10:8000";

        final Response.Listener successHandler = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                lastKnownDeltaTime = System.currentTimeMillis() - timeBeforeRequest;
                results.add(lastKnownDeltaTime);
                sumOfRequestTimes += lastKnownDeltaTime;
                numberOfRequests++;
                averageRequestTime = sumOfRequestTimes / numberOfRequests;

                if (shouldCancelNextRequest()) {
                    mTextView.setText("Measurement finished!\n" +
                            "Average request time: " + averageRequestTime);
                    timer.cancel();
                } else {
                    mTextView.setText("Sending " + numberOfPackets + " packets"
                            + "\nRequest number: #" + numberOfRequests
                            + "\nAverage request time: " + averageRequestTime);
                }
            }
        };

        final Response.ErrorListener errorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!" + error.toString());
            }
        };

        // Request a string response from the provided URL.
        final CustomStringRequest stringRequest = new CustomStringRequest(url, successHandler, errorHandler)
        {
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                String data = generateRandomData(packetSize);
                params.put("data", data);
                params.put("timestamp", "" + Calendar.getInstance().getTimeInMillis());
                return params;
            }
        };

        stringRequest.setPriority(Request.Priority.IMMEDIATE);
        stringRequest.setShouldCache(false);
        RequestTask task = new RequestTask(stringRequest, queue);

        // Add the request to the RequestQueue.
        timer.scheduleAtFixedRate(task, new Date(), 2000);
    }


    public void showResults(View view) {
        Intent intent = new Intent(this, ResultsActivity.class);
        long[] resultsArray = new long[results.size()];
        for (int i = 0; i < results.size(); i++) {
            resultsArray[i] = results.get(i);
        }
        intent.putExtra("results", resultsArray);
        startActivity(intent);
    }


    /**
     * GOOGLE MAPS
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        setUpMapIfNeeded(); // Just in case
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
        }

        updateCamera(currentLatitude, currentLongitude, true);
    }

    private static double normalize(double min, double max, double value) {
        return (value - min) / (max - min);
    }

    private int getColorBasedOnDeltaTime() {
        double normalizedDeltaTime = normalize(0, 500, lastKnownDeltaTime);
        normalizedDeltaTime = 1 - Math.max(0, Math.min(1, normalizedDeltaTime));

        return Color.HSVToColor( new float[]{ 1.f, 0, (float)normalizedDeltaTime } );
    }

    public void updateCamera(double latitude, double longitude, boolean doNotMark) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);

        mMap.moveCamera(center);
        if (!doNotMark) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(12)
                    .fillColor(getColorBasedOnDeltaTime())
                    .strokeWidth(0));
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' section.
    }

    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        // Set up location listener:
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                updateCamera(location.getLatitude(), location.getLongitude(), false);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        String locationProvider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }
}

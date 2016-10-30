package com.lakomy.tomasz.androidpingclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.primitives.Longs;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;


public class PingServerActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static public int numberOfRequests;
    static public int sumOfRequestTimes;
    static public double averageRequestTime;
    static public double medianRequestTime;
    static public double quartileDeviation;
    static public long maxRequestTime;
    static public long minRequestTime;
    static public long lastKnownDeltaTime;
    static public String currentNetworkType;
    static public Timer timer;
    static public long timeBeforeRequest;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private double currentLatitude;
    private double currentLongitude;
    RequestQueue queue;
    static public List<Long> results;

    int packetSize;
    static public int numberOfPackets;
    String url;
    String protocol;
    String ipAddress;
    String intervalUnit;
    int port;
    int requestInterval;
    boolean isInProgress;
    Button pingButton;
    TelephonyManager telephonyManager;
    PingPhoneStateListener pingPhoneStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        resetResults();

        setContentView(R.layout.activity_ping_server);
        setUpMapIfNeeded();
        setRequestInterval();
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        pingButton = (Button)findViewById(R.id.ping_button);
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        queue = Volley.newRequestQueue(this);
        queue.start();
        timer = new Timer();
        pingPhoneStateListener = new PingPhoneStateListener();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(pingPhoneStateListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                PhoneStateListener.LISTEN_CALL_STATE |
                PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_DATA_ACTIVITY |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                PhoneStateListener.LISTEN_SERVICE_STATE |
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTransmission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTransmission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        pingButton.setText("START MEASURING");
    }

    protected void getExtras () {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            packetSize = extras.getInt("packet_size", 16);
            numberOfPackets = extras.getInt("number_of_packets", 10);
            url = extras.getString("url");
            ipAddress = extras.getString("ip_address");
            port = extras.getInt("port", 10);
            protocol = extras.getString("protocol");
            intervalUnit = extras.getString("interval_unit");
            requestInterval = extras.getInt("request_interval", 10);
        }
    }

    protected void resetResults() {
        numberOfRequests = 0;
        sumOfRequestTimes = 0;
        averageRequestTime = 0;
        lastKnownDeltaTime = 0;
        medianRequestTime = 0;
        quartileDeviation = 0;
        maxRequestTime = 0;
        minRequestTime = 0;
        results = new ArrayList<>();
        isInProgress = false;
    }

    protected void cancelTransmission() {
        isInProgress = false;
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ping_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRequestInterval() {
        switch (intervalUnit) {
            case "Seconds":
                requestInterval *= 1000;
                break;
            case "Minutes":
                requestInterval *= 60000;
                break;
        }
    }

    public boolean shouldCancelNextRequest() {
        return numberOfRequests == numberOfPackets;
    }

    public static void calculateQuartileDeviation(double[] results) {
        DescriptiveStatistics da = new DescriptiveStatistics(results);
        quartileDeviation = (da.getPercentile(75) - da.getPercentile(25)) / 2;
    }

    public static void calculateStatistics(List<Long> results) {
        long[] resultsArray = Longs.toArray(results);
        double[] resultsDoubleArray = new double[resultsArray.length];
        for (int i = 0 ; i < resultsArray.length; i++)
        {
            resultsDoubleArray[i] = (double) resultsArray[i];
        }

        Median median = new Median();
        medianRequestTime = median.evaluate(resultsDoubleArray);

        maxRequestTime = Collections.max(results);
        minRequestTime = Collections.min(results);
        calculateQuartileDeviation(resultsDoubleArray);
    }

    public static void updateRequestStatistics() {
        lastKnownDeltaTime = System.currentTimeMillis() - timeBeforeRequest;
        results.add(lastKnownDeltaTime);
        sumOfRequestTimes += lastKnownDeltaTime;
        numberOfRequests++;
        averageRequestTime = sumOfRequestTimes / numberOfRequests;
        calculateStatistics(results);
    }

    public void updateCurrentResults(TextView textView) {
        getCurrentNetworkData();
        if (shouldCancelNextRequest()) {
            textView.setText("Measurement finished!\n" +
                    "\nMedian request time: \t" + medianRequestTime
                    + "\nQuartile deviation: \t" + quartileDeviation
                    + "\nMinimum request time: \t" + minRequestTime
                    + "\nMaximum request time: \t" + maxRequestTime
                    + "\nAverage request time: \t" + averageRequestTime);
            pingButton.setText("Start measuring");
            isInProgress = false;
            timer.cancel();
        } else {
            pingButton.setText("Please wait...");
            textView.setText("Sending " + numberOfPackets + " packets"
                    + "\nCurrent network: \t" + currentNetworkType
                    + "\nRequest number: \t#" + numberOfRequests
                    + "\nCurrent request time: \t" + lastKnownDeltaTime
                    + "\nMedian request time: \t" + medianRequestTime
                    + "\nQuartile deviation: \t" + quartileDeviation
                    + "\nMinimum request time: \t" + minRequestTime
                    + "\nMaximum request time: \t" + maxRequestTime
                    + "\nAverage request time: \t" + averageRequestTime);
        }
    }

    public String getCurrentNetworkType() {
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown network";
        }
        return "Unknown network";
    }

    public void getCurrentNetworkData() {
        currentNetworkType =  getCurrentNetworkType();

        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
        if (allCellInfo != null) {
            Log.d("aping", allCellInfo.toString());
        } else {
            Log.d("aping", "allCellInfo is null");
        }


//        if (all != null) {
//            CellInfoGsm cellinfogsm = (CellInfoGsm) all.get(0);
//            CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
//
//            int strengthDbm = cellSignalStrengthGsm.getDbm();
//            TextView strength = (TextView) findViewById(R.id.signal_info);
//            strength.setText(strengthDbm + "dBm ");
//        }
    }

    public void performHttpRequests() {
        final TextView pingInfo = (TextView) findViewById(R.id.ping_info);
        final Map<String, String> params = new HashMap<>();
        final RandomDataGenerator generator = new RandomDataGenerator();
        final Calendar calendar = Calendar.getInstance();

        final Response.Listener successHandler = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
            updateRequestStatistics();
            updateCurrentResults(pingInfo);
            }
        };

        final Response.ErrorListener errorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            pingInfo.setText("That didn't work!" + error.toString());
            }
        };

        // Request a string response from the provided URL.
        final CustomStringRequest stringRequest = new CustomStringRequest(url, successHandler, errorHandler)
        {

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
        RequestTask task = new RequestTask(stringRequest, queue);

        isInProgress = true;
        // Add the request to the RequestQueue.
        timer.scheduleAtFixedRate(task, 0, requestInterval);
    }

    public void performTcpRequests() {
        final TextView mTextView = (TextView) findViewById(R.id.ping_info);
        RequestTask task = new RequestTask(ipAddress, port, packetSize, mTextView);
        isInProgress = true;
        timer.scheduleAtFixedRate(task, 0, requestInterval);
    }

    public void pingServer(final View view) {
        if (!isInProgress) {
            resetResults();
            if (protocol.equals("http")) {
                performHttpRequests();
            } else {
                performTcpRequests();
            }

            pingButton.setText("Please wait...");
        }
    }


    public void showResults(View view) {
        if (!isInProgress) {
            Intent intent = new Intent(this, ResultsActivity.class);
            long[] resultsArray = new long[results.size()];
            for (int i = 0; i < results.size(); i++) {
                resultsArray[i] = results.get(i);
            }
            intent.putExtra("results", resultsArray);
            intent.putExtra("protocol", protocol);
            intent.putExtra("requestInterval", requestInterval);
            intent.putExtra("packetSize", packetSize);
            intent.putExtra("numberOfPackets", numberOfPackets);
            intent.putExtra("averageRequestTime", averageRequestTime);
            intent.putExtra("medianRequestTime", medianRequestTime);
            intent.putExtra("minRequestTime", minRequestTime);
            intent.putExtra("maxRequestTime", maxRequestTime);
            intent.putExtra("quartileDeviation", quartileDeviation);
            startActivity(intent);
        }
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
                if (isInProgress) {
                    updateCamera(location.getLatitude(), location.getLongitude(), false);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        String locationProvider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }
}

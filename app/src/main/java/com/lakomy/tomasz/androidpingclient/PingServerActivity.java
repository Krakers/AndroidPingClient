package com.lakomy.tomasz.androidpingclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class PingServerActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static public int numberOfRequests;
    static public int sumOfRequestTimes;
    static public double averageRequestTime;
    static public double medianRequestTime;
    static public double quartileDeviation;
    static public long maxRequestTime;
    static public long minRequestTime;
    static public long lastKnownDeltaTime;
    static public long signalStrength;
    static public String currentNetworkType;
    static public long timeBeforeRequest;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    static public double currentLatitude;
    static public double currentLongitude;
    RequestQueue queue;
    static public List<Long> pingTimes;
    static public List<Long> signalStrengths;
    static public List<Float> latitudes;
    static public List<Float> longitudes;

    static int packetSize;
    static public int numberOfPackets;
    String url;
    static String protocol;
    String ipAddress;
    String intervalUnit;
    int port;
    static public int requestInterval;
    static public boolean isInProgress;
    static Button pingButton;
    static ResultsSaver resultsSaver;
    static TelephonyManager telephonyManager;
    PingPhoneStateListener pingPhoneStateListener;
    MemoryBoss mMemoryBoss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        resetResults();
        getActionBar().setDisplayHomeAsUpEnabled(false);
        mMemoryBoss = new MemoryBoss();
        registerComponentCallbacks(mMemoryBoss);

        setContentView(R.layout.activity_ping_server);
        setUpMapIfNeeded();
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

        pingButton = (Button)findViewById(R.id.ping_button);
        telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        queue = Volley.newRequestQueue(this);
        queue.start();
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
        pingTimes = new ArrayList<>();
        signalStrengths = new ArrayList<>();
        longitudes = new ArrayList<>();
        latitudes = new ArrayList<>();
        isInProgress = false;
    }

    public void displayAlert(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(PingServerActivity.this);
        builder1.setMessage(message);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog alertDialog = builder1.create();
        alertDialog.show();
    }

    public static void cancelTransmission() {
        isInProgress = false;
    }

    public static void restartTransmission() {
        isInProgress = true;
    }

    public static boolean shouldCancelNextRequest() {
        return numberOfRequests == numberOfPackets || !isInProgress;
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

        sumOfRequestTimes += lastKnownDeltaTime;
        numberOfRequests++;
        averageRequestTime = sumOfRequestTimes / numberOfRequests;

        maxRequestTime = Collections.max(results);
        minRequestTime = Collections.min(results);
        calculateQuartileDeviation(resultsDoubleArray);
    }

    public static void appendCurrentResultsToFile() throws IOException {
        resultsSaver.setExtras(createShowResultsBundle());
        resultsSaver.addCurrentResultsToFile();
    }

    public static void updateRequestStatistics() throws IOException {
        lastKnownDeltaTime = System.currentTimeMillis() - timeBeforeRequest;

        pingTimes.add(lastKnownDeltaTime);
        signalStrengths.add(signalStrength);
        longitudes.add((float)currentLongitude);
        latitudes.add((float)currentLatitude);

        calculateStatistics(pingTimes);
        appendCurrentResultsToFile();
    }



    public static void updateCurrentResults(TextView textView) {
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
        } else {
            pingButton.setText("Please wait...");
            textView.setText("Sending packet " + numberOfRequests +
                    " out of " + numberOfPackets + " packets"
                    + "\nCurrent network: \t" + currentNetworkType + " (" + signalStrength + " dBm)"
                    + "\nCurrent request time: \t" + lastKnownDeltaTime
                    + "\nMedian request time: \t" + medianRequestTime
                    + "\nQuartile deviation: \t" + quartileDeviation
                    + "\nMinimum request time: \t" + minRequestTime
                    + "\nMaximum request time: \t" + maxRequestTime
                    + "\nAverage request time: \t" + averageRequestTime);
        }
    }

    public static String getCurrentNetworkType() {
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        return "Unknown network";
    }

    public static void getCurrentNetworkData() {
        currentNetworkType = getCurrentNetworkType();
    }

    public void performHttpRequests() {
        final TextView pingInfo = (TextView) findViewById(R.id.ping_info);
        isInProgress = true;
        HttpRequestTask httpRequestTask = new HttpRequestTask(url, packetSize, queue, pingInfo);
        httpRequestTask.execute();
    }

    public void performTcpRequests() {
        final TextView pingInfo = (TextView) findViewById(R.id.ping_info);
        isInProgress = true;
        TcpSocketRequestTask tcpRequest = new TcpSocketRequestTask(ipAddress, port, packetSize, pingInfo);
        tcpRequest.execute();
    }

    public void performUdpRequests() throws SocketException, UnknownHostException {
        final TextView pingInfo = (TextView) findViewById(R.id.ping_info);
        isInProgress = true;
        UdpSocketRequestTask udpRequest = new UdpSocketRequestTask(ipAddress, port, packetSize, pingInfo);
        udpRequest.execute();
    }

    public void pingServer(final View view) throws SocketException, UnknownHostException {
        Log.d("aping", "current protocol: " + protocol);
        resultsSaver = new ResultsSaver(createShowResultsBundle());
        resultsSaver.createFilePath();
        if (!isInProgress) {
            resetResults();
            switch (protocol) {
                case "HTTP":
                    performHttpRequests();
                    break;
                case "TCP":
                    performTcpRequests();
                    break;
                case "UDP":
                    performUdpRequests();
                    break;
            }
            pingButton.setText("Please wait...");
        }
    }

    public static float[] convertFloatListToArray(List<Float> list) {
        float[] longitudesArray = new float[list.size()];
        int i = 0;

        for (Float f : list) {
            longitudesArray[i++] = (f != null ? f : Float.NaN);
        }

        return longitudesArray;
    }

    public static Bundle createShowResultsBundle() {
        Bundle bundle = new Bundle();
        long[] pingTimesArray = Longs.toArray(pingTimes);
        long[] signalStrengthsArray = Longs.toArray(signalStrengths);
        float[] longitudesArray = convertFloatListToArray(longitudes);
        float[] latitudesArray = convertFloatListToArray(latitudes);

        bundle.putLongArray("pingTimes", pingTimesArray);
        bundle.putLongArray("signalStrengths", signalStrengthsArray);
        bundle.putFloatArray("longitudes", longitudesArray);
        bundle.putFloatArray("latitudes", latitudesArray);
        bundle.putString("protocol", protocol);
        bundle.putString("currentNetworkType", currentNetworkType);
        bundle.putInt("requestInterval", requestInterval);
        bundle.putInt("packetSize", packetSize);
        bundle.putInt("numberOfPackets", numberOfPackets);
        bundle.putDouble("averageRequestTime", averageRequestTime);
        bundle.putDouble("medianRequestTime", medianRequestTime);
        bundle.putLong("minRequestTime", minRequestTime);
        bundle.putLong("maxRequestTime", maxRequestTime);
        bundle.putDouble("quartileDeviation", quartileDeviation);

        return bundle;
    }

    Intent createShowResultsIntent() {
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtras(createShowResultsBundle());

        return intent;
    }

    public void showResults(View view) {
        if (!isInProgress) {
            Intent intent = createShowResultsIntent();
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
                googleApiClient);
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
        CameraUpdate center =
                CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);
        googleMap.moveCamera(center);
        if (!doNotMark) {
            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .radius(6)
                    .fillColor(getColorBasedOnDeltaTime())
                    .strokeWidth(0));
        }
    }

    public void captureScreen(final View view)
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
        {
            @Override
            public void onSnapshotReady(Bitmap snapshot)
            {
                try {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
                    String currentTime = simpleDateFormat.format(calendar.getTime());
                    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    String filePath = baseDir + File.separator
                            + "AndroidPingResults" + currentTime
                            + ".png";
                    String filePathMap = baseDir + File.separator
                            + "AndroidPingMap" + currentTime
                            + ".png";

                    FileOutputStream mapOutputStream = new FileOutputStream(filePathMap);
                    snapshot.compress(Bitmap.CompressFormat.JPEG, 90, mapOutputStream);
                    mapOutputStream.flush();
                    mapOutputStream.close();

                    // create bitmap screen capture
                    View v1 = getWindow().getDecorView().getRootView();
                    v1.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                    v1.setDrawingCacheEnabled(false);

                    File imageFile = new File(filePath);

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    displayAlert("Screenshot and map saved to " + filePath + " & " + filePathMap);

                } catch (Throwable e) {
                    // Several error may come out with file handling or OOM
                    e.printStackTrace();
                }
            }
        };

        googleMap.snapshot(callback);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("aping", "Google maps - onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("aping", "Google maps - onConnectionFailed");
    }

    public synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        LocationManager locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
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

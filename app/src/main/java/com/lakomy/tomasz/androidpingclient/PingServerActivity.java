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

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        TextView mTextView = (TextView) findViewById(R.id.ping_info);
        super.onResume();
        setUpMapIfNeeded();
        mTextView.setText("Android Ping!");
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

    public void changeNetworkSettings(final View view) {
        final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(intent);
    }

    public void pingServer(final View view) {
        // Instantiate the RequestQueue.
        final TextView mTextView = (TextView) findViewById(R.id.ping_info);
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://2cdc75fa.ngrok.io";
        timer = new Timer();

        final Response.Listener successHandler = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                lastKnownDeltaTime = System.currentTimeMillis() - timeBeforeRequest;
                sumOfRequestTimes += lastKnownDeltaTime;
                numberOfRequests++;
                averageRequestTime = sumOfRequestTimes / numberOfRequests;
                mTextView.setText("Request time is: " + lastKnownDeltaTime + " number of requests: " + numberOfRequests
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
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                successHandler, errorHandler)
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                String data = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse iaculis justo ut quam ultrices, a venenatis nibh vestibulum. Morbi vitae erat non dolor imperdiet tincidunt at quis sapien. Pellentesque fermentum erat quis odio mattis auctor. Integer convallis nulla nec luctus egestas. Aenean mollis id purus pretium volutpat. Maecenas vel odio odio. Nam velit nisl, varius ac egestas at, placerat eu leo. Nulla elementum eros eget orci sagittis, non pulvinar est interdum. Suspendisse tincidunt libero sed sagittis ultrices. Curabitur sed urna porttitor, dictum augue sit amet, tristique risus. Vivamus id augue mi. Cras vitae lectus in nisi congue tempor. Integer sagittis pretium elit, vel hendrerit massa. Aenean elementum mi turpis, in euismod risus hendrerit a. Pellentesque vel ante et nibh euismod convallis. Vestibulum ex justo, dignissim non arcu sit amet, mollis condimentum nunc. Fusce pellentesque vitae nunc et accumsan. Suspendisse tristique condimentum ligula egestas placerat. Etiam vel lacus sed turpis porttitor bibendum. Praesent placerat elementum dui, sit amet interdum ipsum porttitor semper. Cras pellentesque, felis et sollicitudin dapibus, libero quam ultricies libero, in fringilla augue dolor ut mauris. Proin luctus a massa at hendrerit. Phasellus enim tortor, sodales a mauris ac, tincidunt hendrerit velit. Interdum et malesuada fames ac ante ipsum primis in faucibus. Mauris efficitur orci placerat suscipit varius. Nulla scelerisque arcu sed sollicitudin lacinia. Integer sem nibh, vehicula non blandit a, maximus vel nulla. Nunc consectetur turpis velit, facilisis dignissim mi tristique quis. Nullam interdum gravida sapien, in convallis arcu ultrices ut. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Fusce id ex nisi. Vivamus a risus tortor. Integer id rhoncus magna. Sed egestas ut orci sed posuere. Donec vitae tortor sed erat accumsan ornare ac nec turpis. Nunc a erat feugiat, aliquet arcu a, dignissim tellus. Mauris sit amet gravida odio, quis elementum leo. Suspendisse tellus ipsum, tincidunt posuere mattis non, convallis non elit. Curabitur sed felis faucibus, sagittis nulla a, aliquet lacus. Vivamus mattis libero mauris. Mauris justo ipsum, lacinia id blandit a, tristique id mi. Sed sagittis faucibus luctus. Sed et ante gravida ipsum vehicula sagittis. Praesent turpis elit, eleifend dictum vulputate ac, consectetur nec magna. Etiam eget odio volutpat ex tempor ornare ac eu diam. Phasellus ac ex nunc. Mauris a purus eu lectus dictum volutpat. Nulla facilisi. Phasellus id commodo sem, ut lacinia ante. Curabitur vehicula, lectus nec efficitur convallis, metus nulla commodo massa, a pulvinar lorem ipsum vel libero. Aenean ut venenatis ante. Vestibulum sagittis quam id tempus auctor. Proin eget purus vehicula, mattis lacus vel, posuere nisl. Vivamus sit amet lectus nec ligula sollicitudin posuere. Quisque laoreet tortor nec erat cursus, a maximus magna molestie. Interdum et malesuada fames ac ante ipsum primis in faucibus. Mauris aliquet bibendum vehicula. Duis tincidunt lobortis nibh et cursus. Sed congue porttitor mi vitae dignissim. Sed consequat tincidunt nisl, ac pharetra magna sodales at. Cras fringilla justo facilisis pulvinar venenatis. Fusce orci enim, vulputate at felis id, pulvinar ultricies purus. Maecenas fermentum turpis ac lacinia maximus. Maecenas pretium ipsum a magna luctus, nec accumsan ligula tempus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Donec vel risus dapibus, convallis risus sed, rutrum velit. Fusce rhoncus elit diam. Sed tristique neque vitae ornare consectetur. Nunc facilisis nunc sed porttitor faucibus. Cras sit amet tortor eu odio volutpat fringilla. Duis dolor dui, viverra vitae scelerisque eu, feugiat ac quam. Maecenas quis quam rhoncus, sagittis magna ut, sodales libero. In eget leo augue. Vivamus malesuada pharetra urna id mattis. Mauris at iaculis libero, ac posuere augue. In nec libero at nulla volutpat feugiat. In hac habitasse platea dictumst. Nulla pellentesque, urna quis malesuada sed.";
                params.put("data", data);
                params.put("timestamp", "" + Calendar.getInstance().getTimeInMillis());
                return params;
            }
        };

        RequestTask task = new RequestTask(stringRequest, queue);

        // Add the request to the RequestQueue.
        timer.scheduleAtFixedRate(task, new Date(), 1000);
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

        updateCamera(currentLatitude, currentLongitude);
    }

    private static double normalize(double min, double max, double value) {
        return (value - min) / (max - min);
    }

    private int getColorBasedOnDeltaTime() {
        double normalizedDeltaTime = normalize(0, 700, lastKnownDeltaTime);
        normalizedDeltaTime = 1 - Math.max(0, Math.min(1, normalizedDeltaTime));

        Log.d("dupa", "" + normalizedDeltaTime);

        return Color.HSVToColor( new float[]{ 0, (float)normalizedDeltaTime, 1.f } );
    }

    public void updateCamera(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16);

        mMap.moveCamera(center);
        mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(12)
                .fillColor(getColorBasedOnDeltaTime())
                .strokeWidth(0));
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
                updateCamera(location.getLatitude(), location.getLongitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        String locationProvider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }
}

package com.lakomy.tomasz.androidpingclient;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private int getDataFromEditTextView(int id) {
        EditText edit = (EditText)findViewById(id);
        String strValue = edit.getText().toString();
        int retVal = 0;

        if (!strValue.equals("")) {
            retVal = Integer.parseInt(strValue);
        }

        return retVal;
    }

    /** Called when the user clicks the Send button **/
    public void sendMessage(View view) {
        Intent intent = new Intent(this, PingServerActivity.class);
        int packetSize = getDataFromEditTextView(R.id.packet_size);
        int numberOfPackets = getDataFromEditTextView(R.id.number_of_packets);
        String ipAddress = ((EditText)findViewById(R.id.ip_address)).getText().toString();
        Spinner intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);
        Spinner protocolSpinner = (Spinner)findViewById(R.id.protocol_spinner);
        String intervalUnit = intervalSpinner.getSelectedItem().toString();
        String protocol = protocolSpinner.getSelectedItem().toString();

        int port = getDataFromEditTextView(R.id.port_number);
        int requestInterval = getDataFromEditTextView(R.id.request_interval);
        String url;


        // Set default values:
        if (packetSize == 0) {
            packetSize = 16;
        }

        if (requestInterval == 0) {
            requestInterval = 2;
        }

        if (numberOfPackets == 0) {
            numberOfPackets = 10;
        }

        if (ipAddress.isEmpty() || port == 0) {
            ipAddress = "192.168.0.10";
            port = 8000;
        }

        url = "http://" + ipAddress + ":" + port;
//        url = "https://thawing-castle-69711.herokuapp.com/";

        intent.putExtra("packet_size", packetSize);
        intent.putExtra("number_of_packets", numberOfPackets);
        intent.putExtra("ip_address", ipAddress);
        intent.putExtra("port", port);
        intent.putExtra("url", url);
        intent.putExtra("protocol", protocol);
        intent.putExtra("interval_unit", intervalUnit);
        intent.putExtra("request_interval", requestInterval);

        startActivity(intent);
    }

}

package com.lakomy.tomasz.androidpingclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;


public class MainActivity extends Activity {
    int packetSize;
    int numberOfPackets;
    String ipAddress;
    Spinner intervalSpinner;
    Spinner protocolSpinner;
    String intervalUnit;
    String protocol;

    int port;
    int requestInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddress = ((EditText) findViewById(R.id.ip_address)).getText().toString();
        intervalSpinner = (Spinner) findViewById(R.id.interval_spinner);
        protocolSpinner = (Spinner) findViewById(R.id.protocol_spinner);
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

    public void displayWrongInputAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

    public boolean verifyInputData() {
        boolean isInputDataCorrect = true;

        if (packetSize <= 0) {
            displayWrongInputAlert("Packet size must be greater than 0");
            isInputDataCorrect = false;
        }

        if (packetSize <= 20 && protocol.equals("TCP")) {
            displayWrongInputAlert("TCP packet size must be greater than 20 bytes");
            isInputDataCorrect = false;
        }

        if (packetSize > 65000) {
            displayWrongInputAlert("Maximum packet size allowed is 65000");
            isInputDataCorrect = false;
        }

        if (requestInterval < 500) {
            displayWrongInputAlert("Request interval must be greater than 500ms");
            isInputDataCorrect = false;
        }

        if (numberOfPackets <= 0) {
            displayWrongInputAlert("Number of packets must be greater than 0");
            isInputDataCorrect = false;
        }

        return isInputDataCorrect;
    }

    /** Called when the user clicks the Send button **/
    public void sendMessage(View view) {
        Intent intent = new Intent(this, PingServerActivity.class);
        packetSize = getDataFromEditTextView(R.id.packet_size);
        numberOfPackets = getDataFromEditTextView(R.id.number_of_packets);
        intervalUnit = intervalSpinner.getSelectedItem().toString();
        protocol = protocolSpinner.getSelectedItem().toString();
        port = getDataFromEditTextView(R.id.port_number);
        requestInterval = getDataFromEditTextView(R.id.request_interval);
        String url;

        Log.d("aping", "Current protocol: " + protocol);

        setRequestInterval();
        verifyInputData();

        // Default pingTimesEntries, remove it later
        if (ipAddress.isEmpty() || port == 0) {
            ipAddress = "192.168.0.19";
            port = 8000;
        }

//        url = "http://" + ipAddress + ":" + port;
        url = "https://thawing-castle-69711.herokuapp.com/";


        if (verifyInputData()) {
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
}

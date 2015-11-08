package com.lakomy.tomasz.androidpingclient;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {
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

        // Set default values:
        if (packetSize == 0) {
            packetSize = 20;
        }

        if (numberOfPackets == 0) {
            numberOfPackets = 10;
        }

        intent.putExtra("packet_size", packetSize);
        intent.putExtra("number_of_packets", numberOfPackets);

        startActivity(intent);
    }

}

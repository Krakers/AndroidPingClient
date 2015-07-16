package com.lakomy.tomasz.androidpingclient;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.List;

public class CheckSignalStrength extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        class myPhoneStateListener extends PhoneStateListener {
            public int SignalStrength = 0;
            public int counter = 0;

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                counter++;
                SignalStrength = signalStrength.getGsmSignalStrength();
                TextView strength = (TextView) findViewById(R.id.signal_strength);
                strength.setText(SignalStrength + "dBm " + counter);
            }

            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                super.onCellInfoChanged(cellInfo);
                TextView info = (TextView) findViewById(R.id.cell_info);
                info.setText("CellInfo: " + cellInfo.toString());
            }

            public void onDataActivity(int direction) {
                super.onDataActivity(direction);
                TextView networkInfo = (TextView) findViewById(R.id.network_info);
                switch (direction) {
                    case TelephonyManager.DATA_ACTIVITY_NONE:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_NONE");
                        break;
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_IN");
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_OUT");
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_INOUT");
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_DORMANT");
                        break;
                    default:
                        networkInfo.setText("onDataActivity: DATA_ACTIVITY_NONE");
                        break;
                }
            }
        }

        TelephonyManager TelephonManager;
        myPhoneStateListener pslistener;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_signal_strength);
        try {
            pslistener = new myPhoneStateListener();
            TelephonManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            TelephonManager.listen(pslistener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                    PhoneStateListener.LISTEN_CALL_STATE |
                    PhoneStateListener.LISTEN_CELL_LOCATION |
                    PhoneStateListener.LISTEN_DATA_ACTIVITY |
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                    PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                    PhoneStateListener.LISTEN_SERVICE_STATE |
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_signal_strength, menu);
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
}

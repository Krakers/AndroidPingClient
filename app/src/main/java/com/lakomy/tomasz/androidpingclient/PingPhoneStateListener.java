package com.lakomy.tomasz.androidpingclient;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

class PingPhoneStateListener extends PhoneStateListener {

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (PingServerActivity.telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
            Log.d("signal info", signalStrength.toString());
            String[] signalInfo = signalStrength.toString().split(" ");

            PingServerActivity.signalStrength = Integer.parseInt(signalInfo[8]) - 120;
        } else {
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();

            PingServerActivity.signalStrength =
                    gsmSignalStrength != 99 ? -113 + 2 * gsmSignalStrength : -120;
        }
    }
}

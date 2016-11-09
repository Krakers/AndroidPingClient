package com.lakomy.tomasz.androidpingclient;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

class PingPhoneStateListener extends PhoneStateListener {

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int gsmSignalStrength = signalStrength.getGsmSignalStrength();

        PingServerActivity.signalStrength =
                gsmSignalStrength != 99 ? -113 + 2 * gsmSignalStrength : -120;
    }
}

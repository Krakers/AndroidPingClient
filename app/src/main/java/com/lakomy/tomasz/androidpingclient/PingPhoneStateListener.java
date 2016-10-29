package com.lakomy.tomasz.androidpingclient;

import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

class PingPhoneStateListener extends PhoneStateListener {
    public int signalStrengthValue = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        Log.d("aping", "isGSM: " + signalStrength.isGsm());
        Log.d("aping", "getCdmaDbm: " + signalStrength.getCdmaDbm() + " dBm");
        Log.d("aping", "getEvdoDbm: " + signalStrength.getEvdoDbm() + " dBm");
        Log.d("aping", "getGsmSignalStrength: " + signalStrength.getGsmSignalStrength() + " dBm");
        Log.d("aping", "getGsmSignalStrength: " + signalStrength.getGsmBitErrorRate());
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo) {
        super.onCellInfoChanged(cellInfo);
        Log.d("aping", "CellInfo: " + cellInfo.toString());
    }
}

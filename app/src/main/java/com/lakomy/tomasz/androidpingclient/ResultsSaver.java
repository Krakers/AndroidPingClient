package com.lakomy.tomasz.androidpingclient;


import android.os.Bundle;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ResultsSaver {
    Bundle extras;
    int packetSize;
    int numberOfPackets;
    String protocol;
    int requestInterval;
    double averageRequestTime;
    double medianRequestTime;
    long minRequestTime;
    long maxRequestTime;
    double quartileDeviation;
    long[] pingTimes;
    long[] signalStrengths;
    String filePath;
    CSVWriter writer;
    Calendar calendar;
    String fileName;
    SimpleDateFormat simpleDateFormat;

    ResultsSaver(Bundle _extras) {
        extras = _extras;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
    }

    public String getFilePath() {
        return filePath;
    }

    public void setExtras(Bundle _extras) {
        extras = _extras;
    }

    public void createFilePath() {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String currentTime = simpleDateFormat.format(calendar.getTime());
        fileName = "AnalysisData " + currentTime + ".csv";

        filePath = baseDir + File.separator + fileName;
    }

    public boolean createFile(boolean shouldAppend) throws IOException {
        boolean isSuccessful = true;
        createFilePath();

        try {
            writer = new CSVWriter(new FileWriter(filePath, shouldAppend), ',');
        } catch (IOException e) {
            Log.d("aping", "Creating file failed");
            isSuccessful = false;
        }

        return isSuccessful;
    }

    public boolean addCurrentResultsToFile() throws IOException {
        writer = new CSVWriter(new FileWriter(filePath, false), ',');
        writeDataToFile();
        return true;
    }

    public boolean saveAllResultsToNewFile() throws IOException {
        assignCurrentValues();
        createFile(true);
        writeDataToFile();
        return true;
    }

    void assignCurrentValues() {
        packetSize = extras.getInt("packet_size");
        numberOfPackets = extras.getInt("numberOfPackets");
        protocol = extras.getString("protocol");
        requestInterval = extras.getInt("requestInterval");
        averageRequestTime = extras.getDouble("averageRequestTime");
        medianRequestTime = extras.getDouble("medianRequestTime");
        minRequestTime = extras.getLong("minRequestTime");
        maxRequestTime = extras.getLong("maxRequestTime");
        quartileDeviation = extras.getDouble("quartileDeviation");
        pingTimes = extras.getLongArray("pingTimes");
        signalStrengths = extras.getLongArray("signalStrengths");
    }

    void writeDataToFile() {
        assignCurrentValues();
        String[] statisticsArray = new String[9];
        String[] pingResultsArray = new String[pingTimes.length + 1];
        String[] signalStrengthsArray = new String[signalStrengths.length + 1];

        try {
            statisticsArray[0] = "Protocol: " + protocol;
            statisticsArray[1] = " Packet size: " + packetSize;
            statisticsArray[2] = " Number of packets: " + numberOfPackets;
            statisticsArray[3] = " Request interval: " + requestInterval;
            statisticsArray[4] = " Average request time: " + averageRequestTime;
            statisticsArray[5] = " Median request time: " + medianRequestTime;
            statisticsArray[6] = " Minimum request time: " + minRequestTime;
            statisticsArray[7] = " Maximum request time: " + maxRequestTime;
            statisticsArray[8] = " Quartile deviation: " + quartileDeviation;

            pingResultsArray[0] = "Response time results: ";
            for(int i = 1; i < pingTimes.length; i++){
                pingResultsArray[i + 1] = String.valueOf(pingTimes[i]);
            }

            signalStrengthsArray[0] = "Signal strengths results: ";
            for(int i = 1; i < signalStrengths.length; i++){
                signalStrengthsArray[i + 1] = String.valueOf(signalStrengths[i]);
            }

            writer.writeNext(statisticsArray, false);
            writer.writeNext(pingResultsArray, false);
            writer.writeNext(signalStrengthsArray, false);
            writer.close();

        } catch (IOException e) {
            Log.d("aping", "Save failed");
        }
    }
}

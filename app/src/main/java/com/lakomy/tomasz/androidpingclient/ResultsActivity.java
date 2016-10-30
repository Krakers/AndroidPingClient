package com.lakomy.tomasz.androidpingclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.File;
import java.util.Calendar;



public class ResultsActivity extends FragmentActivity {
    LineChart chart;
    ArrayList<Entry> data = new ArrayList<>();
    long[] results;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        extras = getIntent().getExtras();
        setUpChart();
    }

    void setUpChart() {
        ArrayList<String> xVals = new ArrayList<>();
        chart = (LineChart) findViewById(R.id.chart);

        if (extras != null) {
            results = extras.getLongArray("results");
        }


        for (int i = 0; i < results.length; i++) {
            Entry entry = new Entry(results[i], i);
            data.add(entry);
            xVals.add("Packet " + i);
        }

        LineDataSet dataSet = new LineDataSet(data, "Response time");
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData dataLineData = new LineData(xVals, dataSets);

        chart.setData(dataLineData);
    }

    public void displaySaveResultAlert(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ResultsActivity.this);
        builder1.setMessage(message);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void saveResults(final View view) {
        Log.d("aping", "Save results");
        int packetSize = extras.getInt("packet_size");
        int numberOfPackets = extras.getInt("numberOfPackets");
        String protocol = extras.getString("protocol");
        int requestInterval = extras.getInt("requestInterval");
        double averageRequestTime = extras.getDouble("averageRequestTime");
        double medianRequestTime = extras.getDouble("medianRequestTime");
        long minRequestTime = extras.getLong("minRequestTime");
        long maxRequestTime = extras.getLong("maxRequestTime");
        double quartileDeviation = extras.getDouble("quartileDeviation");

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        String currentTime = sdf.format(c.getTime());

        String fileName = "AnalysisData " + currentTime + ".csv";
        String filePath = baseDir + File.separator + fileName;
        Log.d("aping", "Saving data to" + filePath);
        CSVWriter writer;
        String userMessage;

        try {
            writer = new CSVWriter(new FileWriter(filePath), ',');
            String[] resultsArray = new String[results.length + 10];
            resultsArray[0] = "Measurement results:";
            resultsArray[1] = "Protocol: " + protocol;
            resultsArray[2] = "Packet size: " + packetSize;
            resultsArray[3] = "Number of packets: " + numberOfPackets;
            resultsArray[4] = "Request interval: " + requestInterval;
            resultsArray[5] = "Average request time: " + averageRequestTime;
            resultsArray[6] = "Median request time: " + medianRequestTime;
            resultsArray[7] = "Minimum request time: " + minRequestTime;
            resultsArray[8] = "Maximum request time: " + maxRequestTime;
            resultsArray[9] = "Quartile deviation: " + quartileDeviation;
            for(int i = 1; i < results.length; i++){
                resultsArray[i + 10] = String.valueOf(results[i]);
            }

            writer.writeNext(resultsArray);
            writer.close();
            userMessage = "Results saved to " + filePath;

        } catch (IOException e) {
            Log.d("aping", "Save failed");
            userMessage = "Saving results failed!";
        }
        displaySaveResultAlert(userMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
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

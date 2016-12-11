package com.lakomy.tomasz.androidpingclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.io.File;
import java.util.Calendar;

class yAxisValueFormatterLeft implements YAxisValueFormatter {

    private DecimalFormat mFormat;

    public yAxisValueFormatterLeft() {
        mFormat = new DecimalFormat("###,###,##0"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value) + " ms"; // e.g. append a dollar-sign
    }
}

class yAxisValueFormatterRight implements YAxisValueFormatter {

    private DecimalFormat mFormat;

    public yAxisValueFormatterRight() {
        mFormat = new DecimalFormat("###,###,##0"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value) + " dBm"; // e.g. append a dollar-sign
    }
}

public class ResultsActivity extends Activity {
    LineChart chart;
    ArrayList<Entry> pingTimesEntries = new ArrayList<>();
    ArrayList<Entry> signalStrengthEntries = new ArrayList<>();
    long[] pingTimes;
    long[] signalStrengths;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        extras = getIntent().getExtras();
        setUpChart();
    }

    void setUpChart() {
        ArrayList<String> xVals = new ArrayList<>();
        chart = (LineChart) findViewById(R.id.chart);

        if (extras != null) {
            pingTimes = extras.getLongArray("pingTimes");
            signalStrengths = extras.getLongArray("signalStrengths");
        }

        for (int i = 0; i < pingTimes.length; i++) {
            Entry entry = new Entry(pingTimes[i], i);
            pingTimesEntries.add(entry);
            xVals.add("Packet " + i);
        }

        for (int i = 0; i < signalStrengths.length; i++) {
            Entry entry = new Entry(signalStrengths[i], i);
            signalStrengthEntries.add(entry);
        }

        LineDataSet pingTimesDataSet = new LineDataSet(pingTimesEntries, "Response time [ms]");
        LineDataSet signalStrengthsDataSet = new LineDataSet(signalStrengthEntries, "Signal strength [dBm]");
        signalStrengthsDataSet.setColor(Color.RED);
        signalStrengthsDataSet.setCircleColor(Color.RED);
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(pingTimesDataSet);
        dataSets.add(signalStrengthsDataSet);

        chart.animateXY(1500, 1500);
        chart.setDescription("");
        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setValueFormatter(new yAxisValueFormatterLeft());
        YAxis axisRight = chart.getAxisRight();
        axisRight.setValueFormatter(new yAxisValueFormatterRight());

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

        AlertDialog alertDialog = builder1.create();
        alertDialog.show();
    }

    public void saveResults(final View view) throws IOException {
        ResultsSaver resultsSaver = new ResultsSaver(extras);
        boolean saveSuccessful = resultsSaver.saveAllResultsToNewFile();
        String userMessage = saveSuccessful ?
                "Results saved to " + resultsSaver.getFilePath() :
                "Saving results failed!";
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

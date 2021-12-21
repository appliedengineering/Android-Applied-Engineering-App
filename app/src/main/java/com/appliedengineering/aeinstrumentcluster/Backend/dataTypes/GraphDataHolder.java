package com.appliedengineering.aeinstrumentcluster.Backend.dataTypes;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.appliedengineering.aeinstrumentcluster.Backend.DataManager;
import com.appliedengineering.aeinstrumentcluster.Backend.LogUtil;
import com.appliedengineering.aeinstrumentcluster.R;
import com.appliedengineering.aeinstrumentcluster.UI.HomeActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GraphDataHolder {

    private static final String TAG = "GraphDataHolder";

    private final String keyValue;
    private final transient LineChart chart;
    private List<DataPoint> dataPoints;
    private final transient List<LineChart> chartsToUpdate = new ArrayList<>();

    public GraphDataHolder(String keyValue, LineChart chart) {

        this.keyValue = keyValue;
        this.chart = chart;
        this.dataPoints = new ArrayList<>();
        chartsToUpdate.add(chart);

        // initial init

//        // hide the grid
//        chart.getAxisLeft().setDrawGridLines(false);
//        chart.getXAxis().setDrawGridLines(false);
    }

    public void updateGraphView() {
        // update indicators
        HomeActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                date.setTime(DataManager.START_TIME);
                HomeActivity.snapshotTimeIndicator.setText(date.toString());
            }
        });

        for (LineChart chart : chartsToUpdate) {
            LogUtil.add(getEntriesFormatted().toString());
            LineDataSet lineDataSet = new LineDataSet(getEntriesFormatted(), keyValue);
            LineData lineData = new LineData(lineDataSet);

            if (chartsToUpdate.indexOf(chart) == 0) {
                // Hide labels
                lineData.setDrawValues(false);
                lineData.setHighlightEnabled(false);

                // auto scroll the graph view
                // limit the number of visible entries
                chart.setVisibleXRange(10, 20);

                chart.getAxisLeft().setDrawGridLines(false);
                chart.getXAxis().setDrawGridLines(false);
                chart.getXAxis().setDrawLabels(false);
            } else {
                chart.setVisibleXRange(1, 20);
            }

            boolean isOutOfRange = false;

            // check whether monitor is set
            SharedPreferences sharedPreferences = chart.getContext().getSharedPreferences("GraphViewData", 0);
            String maxString = sharedPreferences.getString(keyValue+"_MAX", null);
            String minString = sharedPreferences.getString(keyValue+"_MIN", null);
            if(maxString != null) {
                float maxValue = Float.parseFloat(maxString);
                for (DataPoint d :
                        dataPoints) {
                    if(d.getY() > maxValue){
                        isOutOfRange = true;
                    }
                }
            }
            if(minString != null) {
                float minValue = Float.parseFloat(minString);
                for (DataPoint d :
                        dataPoints) {
                    if(d.getY() < minValue){
                        isOutOfRange = true;
                    }
                }
            }

            // make the chart smooth
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setCubicIntensity(.1f);
            lineDataSet.setDrawCircles(false);
            Description description = new Description();
            description.setText(keyValue);
            chart.setDescription(description);


            Drawable drawable = ContextCompat.getDrawable(chart.getContext(), R.drawable.fade_cool_blue);
            if(isOutOfRange) {
                lineDataSet.setColor(Color.RED);
                lineDataSet.setLabel(lineDataSet.getLabel()+" (OUT OF RANGE)");
                drawable = ContextCompat.getDrawable(chart.getContext(), R.drawable.fade_cool_red);
            }
            lineDataSet.setDrawFilled(true);
            lineDataSet.setLineWidth(3f);
            lineDataSet.setFillDrawable(drawable);
            chart.setData(lineData);

            // update graphview
            chart.notifyDataSetChanged();
            chart.invalidate();

            // move to the latest entry
            if (dataPoints.size() > 20) {
                chart.moveViewToX(dataPoints.size() - 20);
            }
        }
    }

    private List<Entry> getEntriesFormatted() {
        // Format the data relative to the time elapsed
        List<Entry> newEntryList = new ArrayList<>();
        int i = 0;
        for (DataPoint e : dataPoints) {
            i++;
            float x, y;
            Log.d(TAG, "getEntriesFormatted: " + e.getX() + " , " + DataManager.START_TIME);
            x = (e.getX() - DataManager.START_TIME) / 1000f; // divide by 1000 to convert to seconds
            y = e.getY();
            Entry newEntry = new Entry(x, y);

            newEntryList.add(newEntry);
        }

        // The entries MUST be sorted
        Collections.sort(newEntryList, new EntryXComparator());

        return newEntryList;

    }

    // GETTERS AND SETTERS

    public void addEntry(long x, float y) {
        dataPoints.add(new DataPoint(x, y));
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
        updateGraphView();
        chart.notifyDataSetChanged();
        updateGraphView();
        // the view needs to be refreshed two times
        // first time is for the chart to realize that new data has been added
        // second time is for the chart to re-render the view with proper scaling
    }

    public void deRegister(LineChart lineChart) {
        chartsToUpdate.remove(lineChart);
    }

    public void register(LineChart lineChart) {
        chartsToUpdate.add(lineChart);
    }
}

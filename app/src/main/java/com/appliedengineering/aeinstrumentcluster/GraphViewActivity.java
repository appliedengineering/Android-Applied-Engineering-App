package com.appliedengineering.aeinstrumentcluster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.appliedengineering.aeinstrumentcluster.Backend.DataManager;
import com.appliedengineering.aeinstrumentcluster.Backend.dataTypes.GraphDataHolder;
import com.appliedengineering.aeinstrumentcluster.UI.HomeContentScroll;
import com.github.mikephil.charting.charts.LineChart;

public class GraphViewActivity extends AppCompatActivity {

    public static final String DATA_INDEX = "DATA_INDEX";
    private DataManager dataManager;
    private EditText max, min;
    private LineChart chart;
    private GraphDataHolder graphDataHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);

        // get the proper id
        String chartId = getIntent().getStringExtra(DATA_INDEX);

        // set the title
        TextView title = findViewById(R.id.graph_title_view);
        title.setText(HomeContentScroll.formatTitle(chartId));

        // set up the monitor
        max = findViewById(R.id.max_edit_text);
        min = findViewById(R.id.min_edit_text);

        // restore saved values
        SharedPreferences sharedPreferences = getSharedPreferences("GraphViewData", 0);
        String maxString = sharedPreferences.getString(chartId+"_MAX", null);
        String minString = sharedPreferences.getString(chartId+"_MIN", null);

        if(maxString != null) {
            max.setText(maxString);
        }
        if(minString != null) {
            min.setText(minString);
        }

        max.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences sharedPreferences = getSharedPreferences("GraphViewData", 0);
                sharedPreferences.edit().putString(chartId+"_MAX", editable.toString()).commit();
            }
        });

        min.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences sharedPreferences = getSharedPreferences("GraphViewData", 0);
                sharedPreferences.edit().putString(chartId+"_MIN", editable.toString()).commit();
            }
        });

        // retrieve the data
        dataManager = DataManager.dataManager;
        graphDataHolder = dataManager.getGraphDataHolderRef(chartId);
        if (graphDataHolder != null) {
            chart = findViewById(R.id.lineChartId);
            graphDataHolder.register(chart);
            graphDataHolder.updateGraphView();
            graphDataHolder.updateGraphView();
            // need to update two times, for reasons explained in another class
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        graphDataHolder.deRegister(chart);
    }
}
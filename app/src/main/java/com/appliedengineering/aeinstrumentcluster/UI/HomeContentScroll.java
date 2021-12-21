package com.appliedengineering.aeinstrumentcluster.UI;

import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appliedengineering.aeinstrumentcluster.Backend.DataManager;
import com.appliedengineering.aeinstrumentcluster.Backend.dataTypes.GraphDataHolder;
import com.appliedengineering.aeinstrumentcluster.GraphViewActivity;
import com.appliedengineering.aeinstrumentcluster.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.msgpack.value.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeContentScroll extends Fragment {

    private final DataManager dataManager;

    public HomeContentScroll(){
        super(R.layout.home_content_scroll_layout);
        this.dataManager = DataManager.dataManager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.content_linear_layout);

        for (int i = 0; i < DataManager.GRAPH_KEY_VALUES.length; i++){
            layout.addView(createNewChartView(DataManager.GRAPH_KEY_VALUES[i]));
        }

        return view;
    }

    public View createNewChartView(String keyValue){
        // inflate the graph layout
        @SuppressLint("InflateParams")
        View graphView = getLayoutInflater().inflate(R.layout.graph_layout, null, false);
        LineChart chart = graphView.findViewById(R.id.line_chart);
        TextView chartTitle = graphView.findViewById(R.id.line_chart_title);

        // Set the title
        chartTitle.setText(formatTitle(keyValue));

        // Add the graph view to the right place in the indexed map
        // The string key
        GraphDataHolder graphDataHolder = dataManager.registerForDataManager(keyValue, chart); // gives the chart a "room" in the data manager
        List<Entry> entries = new ArrayList<>();
        LineDataSet lineDataSet = new LineDataSet(entries, keyValue);

        // Set properties and styling
        lineDataSet.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        chart.setData(new LineData(dataSets));

        // After initializing an empty graph, we store the reference to the entries var so that we can update it later
        // also store the reference to the linechart, we will need it later
        // these refs are stored with the data manager
        // the data manager will manage everything, this class just need to create the graphs and give them to the data manager
        // data manager will update the graphs and refresh them


        // Set an onclick listener for the graph
        TextView lineChartTitle = graphView.findViewById(R.id.line_chart_title);

        lineChartTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent graphViewIntent = new Intent(view.getContext(), GraphViewActivity.class);
                graphViewIntent.putExtra(GraphViewActivity.DATA_INDEX, keyValue);
                startActivity(graphViewIntent);
            }
        });

        return graphView;
    }

    private String formatTitle(String keyValue) {
        String[] tokens = keyValue.split("(?=\\p{Upper})");
        String returnString = "";
        for(String token : tokens) {
            returnString += token.substring(0, 1).toUpperCase() + token.substring(1) + " ";
        }
        return returnString;
    }


}

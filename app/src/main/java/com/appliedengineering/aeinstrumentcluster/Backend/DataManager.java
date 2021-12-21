package com.appliedengineering.aeinstrumentcluster.Backend;

import android.util.Log;

import com.appliedengineering.aeinstrumentcluster.Backend.dataTypes.DataPoint;
import com.appliedengineering.aeinstrumentcluster.Backend.dataTypes.GraphDataHolder;
import com.appliedengineering.aeinstrumentcluster.UI.HomeActivity;
import com.appliedengineering.aeinstrumentcluster.UI.HomeContentScroll;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.msgpack.value.Value;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    public static long START_TIME = System.currentTimeMillis(); // we want current time in seconds
    public static final String SERIALIZATION_DELIMITER = "d>d<d>d<d";

    // The data manager needs to be static in order to access it from another activity
    public static DataManager dataManager;

    private HashMap<String, GraphDataHolder> graphsMap = new HashMap<>();

    public static String serializeData(){
        // format {startTime}delimiter{GSON encoded data}
        // delimiter = $>$<$>$<$
        return START_TIME
                + SERIALIZATION_DELIMITER
                + new Gson().toJson(dataManager.graphsMap);
    }

    public static final String[] GRAPH_KEY_VALUES = new String[] {
            "psuMode",
            "throttlePercent",
            "dutyPercent",
            "pwmFrequency",
            "rpm",
            "torque",
            "tempC",
            "sourceVoltage",
            "pwmCurrent",
            "powerChange",
            "voltageChange"
    };

    public GraphDataHolder registerForDataManager(String keyValue, LineChart chart) {
        GraphDataHolder dataHolder = new GraphDataHolder(keyValue, chart);
        graphsMap.put(keyValue, dataHolder);
        return dataHolder;
    }

    public void addData(Map<Value, Value> map, long timeStamp) {
        try {
            // First convert the format of the map, make it more friendly
            List<String> keyValues = Arrays.asList(DataManager.GRAPH_KEY_VALUES);
            timeStamp = getTimestamp(map)*1000L;
            for (Map.Entry<Value, Value> entry : map.entrySet()) {
                Value key = entry.getKey();
                Value value = entry.getValue();

                String keyValue = key.toString();
                if(keyValues.contains(keyValue)) {
                    float entryValue = 0;
                    try {
                        entryValue = Float.parseFloat(value.toString());
                    } catch (NumberFormatException e) {
                        LogUtil.addc("Could not parse number! Corrupted data?");
                    }

                    graphsMap.get(keyValue).addEntry(timeStamp, entryValue);
                    graphsMap.get(keyValue).updateGraphView();
                }


            }
        } catch (Exception e) {
            Log.e("DataManager", e.toString());
            LogUtil.addc("Serious error when parsing data! Corrupted data?");
        }
    }

    private long getTimestamp(Map<Value, Value> map) {
        long entryValue = 0;
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            Value key = entry.getKey();
            Value value = entry.getValue();
            if(key.toString().equals("timeStamp")){
                try {
                    LogUtil.add("Value:" + value.toString());
                    entryValue = (long) (Double.parseDouble(value.toString().split("E")[0]) * Math.pow(10, 9));
                } catch (NumberFormatException e) {
                    LogUtil.addc("Could not parse number! Corrupted data?");
                }
            }
        }
        LogUtil.add("Calc Time"+entryValue);
        return entryValue;
    }

    public GraphDataHolder getGraphDataHolderRef(String keyValue) {
        return graphsMap.get(keyValue);
    }

    public void addDebugData(Map<String, Float> map, long currentTimeMillis) {
        List<String> keyValues = Arrays.asList(DataManager.GRAPH_KEY_VALUES);
        for (Map.Entry<String, Float> entry : map.entrySet()) {
            String key = entry.getKey();
            float value = entry.getValue();
            if(keyValues.contains(key)) {
                graphsMap.get(key).addEntry(currentTimeMillis, value);
                graphsMap.get(key).updateGraphView();
            }
        }
    }

    public void loadDataFromString(String snapshot) {
        Type typeOfHashMap = new TypeToken<Map<String, GraphDataHolder>>() { }.getType();

        String[] tokens = snapshot.split(SERIALIZATION_DELIMITER);
        String extraData = tokens[0];
        String gsonData = tokens[1];

        // get timestamp from extra data
        long timestamp = Long.parseLong(extraData);
        START_TIME = timestamp;

        LinkedTreeMap<String, GraphDataHolder> newGraphDataHolder = new Gson().fromJson(gsonData, typeOfHashMap);

        for(String key : graphsMap.keySet()) {
            // get the graph and update it
            GraphDataHolder graphDataHolder = graphsMap.get(key);
            graphDataHolder.setDataPoints(newGraphDataHolder.get(key).getDataPoints());
        }

        HomeActivity.isSnapshotLoaded = true;

    }

    public void reset() {
        for(String key : graphsMap.keySet()) {
            // get the graph and reset it
            GraphDataHolder graphDataHolder = graphsMap.get(key);
            graphDataHolder.getDataPoints().clear();
            graphDataHolder.updateGraphView();
        }

        HomeActivity.isSnapshotLoaded = false;
    }


    // These following properties need to be handled differently
//            "mddStatus" : True,
//            "ocpStatus" : True,
//            "ovpStatus" : True,
//            "timeStamp" // Determines the x coordinate


}

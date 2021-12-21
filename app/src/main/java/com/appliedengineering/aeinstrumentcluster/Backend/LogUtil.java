package com.appliedengineering.aeinstrumentcluster.Backend;

import android.util.Log;

public class LogUtil {
    static public void add(String s){
        Log.d("log", "Status: " + s);
    }

    static public void addc(String s){
        Log.d("log", "Critical: " + s);
    }
}

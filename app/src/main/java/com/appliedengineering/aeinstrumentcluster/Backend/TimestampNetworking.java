package com.appliedengineering.aeinstrumentcluster.Backend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.appliedengineering.aeinstrumentcluster.R;
import com.appliedengineering.aeinstrumentcluster.UI.HomeTopBar;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.Map;


public class TimestampNetworking extends AsyncTask<Void, Void, Void>{

    private volatile boolean isRunning = true;

    public TimestampNetworking(Activity activity) {
        Communication.init();

        SharedPreferences settings = activity.getSharedPreferences("SettingsInfo", 0);
        String ipAddress = "192.168.3.2";
        String port = "5546";

        LogUtil.add("Communication setup: " + Communication.connectToTimestampSocket("tcp://"+ipAddress+":5546"));
    }

    @Override
    protected void onCancelled(){
        isRunning = false;
        Communication.deinit();
    }

    @Override
    protected Void doInBackground(Void... params) {

        loadPreferences();

        while (isRunning){
            try {

                Communication.sendTimestamp(Long.toString(System.currentTimeMillis()).getBytes());
                byte[] data = Communication.recv();
                if (data != null) {
                    handleData(data);
                }
                else{
                    LogUtil.add("No msg to be received");
                }
            }
            catch (ZMQException e){
                if (!e.equals(ZMQ.Error.EAGAIN)) {
                    LogUtil.addc("Error - " + e.getMessage());
                }
            } catch (IOException e) {
                LogUtil.addc(e.getStackTrace().toString());
            }
            // SystemClock.sleep(1000);
        }

        return null;
    }



    private void handleData(byte[] data) throws IOException {
        LogUtil.add(new String(data));
    }


    // preferences
    private void loadPreferences(){

    }

    private void savePreferences(){

    }
    // end preferences

}

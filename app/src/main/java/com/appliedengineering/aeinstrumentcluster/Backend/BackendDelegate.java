package com.appliedengineering.aeinstrumentcluster.Backend;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.appliedengineering.aeinstrumentcluster.R;
import com.appliedengineering.aeinstrumentcluster.UI.HomeTopBar;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.FloatValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
//import org.zeromq.EmbeddedLibraryTools;


// Class design found here: https://stackoverflow.com/a/36155334/
public class BackendDelegate extends AsyncTask<Void, Void, Void>{

    private static final int NO_MESSAGE_COUNT_THRESHOLD = 5;
    private final DataManager dataManager;
    private final HomeTopBar homeTopBar;
    private volatile boolean isRunning = true;
    private volatile boolean isNetworkEnabled = true;
    private volatile boolean generateDebugData = false;
    private Thread debugThread;

    private volatile int noMessageCount = NO_MESSAGE_COUNT_THRESHOLD;

    // options
    private String connectionPort = "";
    private String connectionIPAddress = "";


    // not used
//    private String connectionAddress = "";
//    private String connectionGroup = "";
//    private int receiveReconnect = -1;
//    private int receiveTimeout = -1;
//    private int reconnectTimeout = -1;
//    private int receiveBuffer = 60;

    // end options


    private final Activity activity;

    public BackendDelegate(HomeTopBar homeTopBar, Activity activity) {
        this.dataManager = DataManager.dataManager;
        this.homeTopBar = homeTopBar;
        this.activity = activity;
        Communication.init();

        connect();

        setUpDebugThread();
    }

    private void setUpDebugThread() {
        // the debug thread is a thread dedicated to debugging
        debugThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isRunning) {
                    if (generateDebugData) {
                        if(isNetworkEnabled) {
                            return;
                        }
                        dataManager.addDebugData(generateDebugData(), System.currentTimeMillis());
                    }
                    SystemClock.sleep(1000);
                }
            }
        });
        debugThread.start();
    }

    private Map<String, Float> generateDebugData() {
        Map<String, Float> map = new HashMap<>();
        for (String key :
                DataManager.GRAPH_KEY_VALUES) {
            map.put(key, (float) (Math.random()*100f));
        }
        return map;
    }

    private void connect() {
        loadPreferences(activity);

        String connectionString = "tcp://"+connectionIPAddress+":"+connectionPort;

        LogUtil.add("Communication setup: " + Communication.connect(connectionString));
    }

    public void reconnect() {
        Communication.disconnect();
        connect();
    }

    @Override
    protected void onCancelled(){
        isRunning = false;
        Communication.deinit();
    }

    @Override
    protected Void doInBackground(Void... params) {

        while (isRunning){
            try {
                byte[] data = Communication.recv();
                if (data != null) {
                    noMessageCount = 0;
                    handleData(data);
                }
                else{
                    noMessageCount++;
                    LogUtil.add("No msg to be received");
                }
                updateHomeTopBar();
            }
            catch (ZMQException e){
                if (!e.equals(ZMQ.Error.EAGAIN)) {
                    LogUtil.addc("Error - " + e.getMessage());
                }
            } catch (IOException e) {
                LogUtil.addc(e.getStackTrace().toString());
            }
            SystemClock.sleep(500);
        }

        return null;
    }

    private void updateHomeTopBar() {
        // show connected indicator
        String textToDisplay;
        if(noMessageCount >= NO_MESSAGE_COUNT_THRESHOLD) {
            // offline
            textToDisplay = "Status: offline";
        } else {
            // online
            textToDisplay = "Status: online";
        }
        if(homeTopBar.getView() != null) {
            if(!isNetworkEnabled) {
                textToDisplay = "Status: DISABLED";
            }
            if(generateDebugData) {
                textToDisplay = "Status: DEBUG";
            }
            String finalTextToDisplay = textToDisplay;
            homeTopBar.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    TextView indicator = homeTopBar.getView().findViewById(R.id.status_indicator_text);

                    indicator.setText(finalTextToDisplay);
                }
            });
        }
    }

    private void handleData(byte[] data) throws IOException {
        if(!isNetworkEnabled) return;

        LogUtil.add("Received data: " + new String(data));

        // unpack the data into readable format
        MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(data);
        MapValue mv = (MapValue) messageUnpacker.unpackValue();

        Map<Value, Value> map = mv.map();

        // bypass for now
        long timeStamp = System.currentTimeMillis();

        dataManager.addData(map, timeStamp);


    }


    // preferences
    private void loadPreferences(Activity activity){
        SharedPreferences settings = activity.getSharedPreferences("SettingsInfo", 0);
        connectionIPAddress =  settings.getString("ipAddress", "192.168.137.1");
        connectionPort = settings.getString("port", "5556");
    }



    public void setNetworkEnabled(boolean isEnabled) {
        isNetworkEnabled = isEnabled;
    }

    public void setGenerateDebugData(boolean generate) {
        generateDebugData = generate;
    }

}

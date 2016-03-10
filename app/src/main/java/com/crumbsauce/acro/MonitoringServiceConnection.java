package com.crumbsauce.acro;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MonitoringServiceConnection implements ServiceConnection {

    private static final String LOG_TAG = "SERVICE_CONN";
    private HomeScreen callback;

    public MonitoringServiceConnection(HomeScreen callback) {
        this.callback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AccidentMonitor boundMonitor = ((AccidentMonitor.LocalBinder) service).getService();
        boundMonitor.setNotificationCallback(callback);
        Log.d(LOG_TAG, "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LOG_TAG, "Service disconnected");
    }
}

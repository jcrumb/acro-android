package com.crumbsauce.acro;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class MonitorServiceConnection implements ServiceConnection {

    private static final String LOG_TAG = "SERVICE_CONN";

    private HomeScreen callback;
    private AccidentMonitor boundMonitor;

    public MonitorServiceConnection(HomeScreen callback) {
        this.callback = callback;
    }

    public AccidentMonitor getBoundMonitor() {
        return boundMonitor;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        boundMonitor = ((AccidentMonitor.LocalBinder)service).getService();
        boundMonitor.setNotificationCallback(callback);
        Log.d(LOG_TAG, "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LOG_TAG, "Service disconnected");
    }
}

package com.crumbsauce.acro;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

public class AccidentMonitor extends Service implements SensorEventListener {
    private static final String LOG_TAG = "BACKGROUND_THREAD";

    private final IBinder mBinder = new LocalBinder();
    private HomeScreen callback;

    private SensorManager sensorManager;

    private ArrayBlockingQueue<Float> deltaXhistory = new ArrayBlockingQueue<>(3);
    private ArrayBlockingQueue<Float> deltaYhistory = new ArrayBlockingQueue<>(3);
    private ArrayBlockingQueue<Float> deltaZhistory = new ArrayBlockingQueue<>(3);

    private float lastX = 0.0f;
    private float lastY = 0.0f;
    private float lastZ = 0.0f;

    private boolean onCooldown = false;
    private int cooldownCountRemaining = 3;


    private float calculateAverageChange(ArrayBlockingQueue<Float> deltaHistory, float delta) {
        if (deltaHistory.remainingCapacity() != 0) {
            deltaHistory.offer(delta);
            return 0.0f;
        }

        deltaHistory.poll();
        deltaHistory.offer(delta);

        float total = 0.0f;

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        Float[] values = deltaHistory.toArray(new Float[0]);
        for (Float f : values) {
            total += f;
        }

        return total / values.length;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (linearAccelerometer == null) {
            Log.d(LOG_TAG, "No default linear accelerometer found");
        }
        Log.d(LOG_TAG, "Service starting");
        sensorManager.registerListener(this, linearAccelerometer, 330000);
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        Log.d(LOG_TAG, "Service stopped");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float forceX = event.values[0];
        float forceY = event.values[1];
        float forceZ = event.values[2];

        Log.v(LOG_TAG, String.format("X Force: %f", forceX));
        Log.v(LOG_TAG, String.format("Y Force: %f", forceY));
        Log.v(LOG_TAG, String.format("Z Force: %f", forceZ));

        float deltaX = Math.abs(lastX - forceX);
        float deltaY = Math.abs(lastY - forceY);
        float deltaZ = Math.abs(lastZ - forceZ);

        lastX = forceX;
        lastY = forceY;
        lastZ = forceZ;

        float avgDeltaX = calculateAverageChange(deltaXhistory, deltaX);
        float avgDeltaY = calculateAverageChange(deltaYhistory, deltaY);
        float avgDeltaZ = calculateAverageChange(deltaZhistory, deltaZ);

        final float forceThreshold = 3.0f;
        if (avgDeltaX > forceThreshold || avgDeltaY > forceThreshold || avgDeltaZ > forceThreshold) {
            if (!onCooldown) {
                onCooldown = true;
                cooldownCountRemaining = 3;

                Log.d(LOG_TAG, "Alert generated");

                deltaXhistory.clear();
                deltaYhistory.clear();
                deltaZhistory.clear();

                callback.promptForAlert();
            } else {
                cooldownCountRemaining--;
                if (cooldownCountRemaining == 0) {
                    onCooldown = false;
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start command received");
        return START_STICKY;
    }

    public void setNotificationCallback(HomeScreen callback) {
        this.callback = callback;
    }

    public class LocalBinder extends Binder {
        AccidentMonitor getService() {
            return AccidentMonitor.this;
        }
    }
}

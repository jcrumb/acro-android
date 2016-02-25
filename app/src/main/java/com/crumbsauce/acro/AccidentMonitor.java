package com.crumbsauce.acro;

import android.os.Process;
import android.util.Log;

public class AccidentMonitor implements Runnable {
    private HomeScreen UI;

    public AccidentMonitor(HomeScreen caller) {
        UI = caller;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        UI.setAccidentMonitorThread(Thread.currentThread());

        int num = 0;

        while (true) {
            try {
                num++;
                Log.d("BACKGROUND_THREAD", String.format("Loops: %d", num));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }

        Log.d("BACKGROUND_THREAD", "Exiting...");
    }
}

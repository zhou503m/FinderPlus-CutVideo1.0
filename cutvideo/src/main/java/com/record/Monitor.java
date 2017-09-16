package com.record;

import com.VideoMain;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A thread to maintain the recorder threads
 * Created by Wangke on 2017/9/14.
 */
public class Monitor extends Thread {

    private Map<Runnable, Thread> monitoredThread;

    public Monitor(Map<Runnable, Thread> monitoredThread) {
        this.monitoredThread = monitoredThread;
    }

    public void run() {
        while (true) {
            try {
                for (Map.Entry<Runnable, Thread> entry: monitoredThread.entrySet()) {
                    Thread thread = entry.getValue();
                    Thread.State state = thread.getState();
                    if (Thread.State.TERMINATED.equals(state) && VideoMain.recording) {
                        Runnable runnable = entry.getKey();
                        Thread newThread = new Thread(runnable);
                        newThread.start();
                        entry.setValue(newThread);
                    }
                }

                TimeUnit.MILLISECONDS.sleep(30000);
            } catch (InterruptedException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
        }
    }
}
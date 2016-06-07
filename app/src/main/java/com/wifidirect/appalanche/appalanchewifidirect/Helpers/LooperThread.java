package com.wifidirect.appalanche.appalanchewifidirect.Helpers;


import android.os.Handler;
import android.os.Looper;

import com.wifidirect.appalanche.appalanchewifidirect.Handlers.IncomingHandler;

public class LooperThread extends Thread {
    public Handler mHandler;

    public void run() {
        Looper.prepare();

        mHandler = new IncomingHandler();

        Looper.loop();
    }

    public Handler getHandler(){
        return mHandler;
    }

}
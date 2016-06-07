package com.wifidirect.appalanche.appalanchewifidirect;

import android.app.Application;
import android.content.Intent;

import com.wifidirect.appalanche.appalanchewifidirect.Services.BroadcastReceiverService;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, BroadcastReceiverService.class));
    }
}

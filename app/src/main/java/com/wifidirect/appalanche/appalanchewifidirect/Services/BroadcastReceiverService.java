package com.wifidirect.appalanche.appalanchewifidirect.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.WiFiDirectBroadcastReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class BroadcastReceiverService extends Service {
        BroadcastReceiver mReceiver;

    Thread bgThread;
    @Subscribe
    public void onEvent(boolean event){

    }

    @Override
    public void onCreate() {
        super.onCreate();


        EventBus.getDefault().register(this);

        bgThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // get an instance of the receiver in your service
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

                intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                mReceiver = new WiFiDirectBroadcastReceiver();
                registerReceiver(mReceiver, intentFilter);
            }
        });
        bgThread.start();
    }
    @Override
    public void onDestroy(){
        bgThread.interrupt();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onRebind(Intent intent) {
        Log.v("Service BR", "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v("Service BR", "in onUnbind");
        return true;
    }
}


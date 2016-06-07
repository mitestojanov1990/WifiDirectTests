package com.wifidirect.appalanche.appalanchewifidirect;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Events.ConnectionInfoEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.IsDisconnectedEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.MyDeviceNameEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.PeersEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.WifiInfoEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.WifiStatusEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;

import org.greenrobot.eventbus.EventBus;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    public WiFiDirectBroadcastReceiver(){

    }

    private String getDeviceStatus(int status) {
        switch(status) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return Integer.toString(status);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo be = intent.getParcelableExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED);
            if(netInfo != null) {
                if (netInfo.isConnected()) {
                    EventBus.getDefault().post(new WifiInfoEvent(true));
                } else {
                    EventBus.getDefault().post(new WifiStatusEvent(false));
                }
            }
        }

        WifiP2pDevice dev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        if(dev != null) {
            String thisDeviceName = dev.deviceName;
            EventBus.getDefault().post(new MyDeviceNameEvent(thisDeviceName));
        }


        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d(Constants.TAG_LOG, action);
            WifiP2pInfo p2pInfo = (WifiP2pInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection info to find group owner IP
                Log.d(Constants.TAG_LOG, "Connected to p2p network. Requesting network details");
                EventBus.getDefault().post(new ConnectionInfoEvent(true));
            } else {
                // It's a disconnect
                Log.d(Constants.TAG_LOG, "It is a disconnect");
                //if (!WifiGroupManager.IsServer) {
                    //WifiGroupManager.IsDisconnected = true;
                EventBus.getDefault().post(new IsDisconnectedEvent(true));
                    //((FragmentChangeListener) activity).OnChangeToSubview(Constants.ID_MAIN_PAGE);
                //}
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(Constants.TAG_LOG, action);

            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(Constants.TAG_LOG, "Device status -" + getDeviceStatus(device.status) + " name: " + device.deviceName + " adr: " + device.deviceAddress);

        } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(Constants.TAG_LOG, "WFD enabled");
            } else {
                Log.d(Constants.TAG_LOG, "WFD NOT enabled");
            }
        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            EventBus.getDefault().post(new PeersEvent(true));
            //((FragmentChangeListener)activity).OnChangeToSubview(Constants.PEERS_AVAILABLE);
        }
    }
}

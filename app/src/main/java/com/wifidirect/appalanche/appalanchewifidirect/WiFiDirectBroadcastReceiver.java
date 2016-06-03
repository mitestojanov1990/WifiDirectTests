package com.wifidirect.appalanche.appalanchewifidirect;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.FragmentChangeListener;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.WifiManagerListener;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiStatusEvent;

import org.greenrobot.eventbus.EventBus;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private Activity activity = null;

    public WiFiDirectBroadcastReceiver(){

    }
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager,Channel channel, Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
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

    private void GetWifiInfo(WifiManager wifiManager){
        if(wifiManager != null) {
            ((WifiManagerListener) activity).SendMessage("Connected");

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            // Convert little-endian to big-endianif needed
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                ipAddress = Integer.reverseBytes(ipAddress);
            }
            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
            String ipAddressString;
            try {
                ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
            } catch (UnknownHostException ex) {
                ((WifiManagerListener) activity).SendMessage("Unable to get host address.");
                ipAddressString = null;
            }
            int tmpRssi = wifiInfo.getRssi();
            int signalLevel = wifiManager.calculateSignalLevel(tmpRssi, 10);

            ((WifiManagerListener) activity).SendMessage("Ip Address: " + ipAddressString + " Signal Level: " + signalLevel);

            ((WifiManagerListener) activity).SendMessage("Error, Net ID is -1");

            EventBus.getDefault().post(new WifiStatusEvent(true));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WifiManager wifiManager = null;
        if (activity != null) {
            wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        }
//        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
//            if (activity != null) {
//                ((WifiManagerListener) activity).scanReturnedResults();
//            }
//        }
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo be = intent.getParcelableExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED);
            if(netInfo != null) {
                if (netInfo.isConnected()) {
                    if (activity != null) {
                        GetWifiInfo(wifiManager);
                    }
                } else {
                    if (activity != null) {
                        ((WifiManagerListener) activity).SendMessage("Not connected");
                    }
                }
            }
        }

        WifiP2pDevice dev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        if(dev != null) {
            String thisDeviceName = dev.deviceName;
            ((WifiManagerListener) activity).GetMyDeviceName(thisDeviceName);
        }


        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            Log.d(Constants.TAG_LOG, action);
            WifiP2pInfo p2pInfo = (WifiP2pInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection info to find group owner IP
                Log.d(Constants.TAG_LOG, "Connected to p2p network. Requesting network details");
                manager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) activity);
            } else {
                // It's a disconnect
                Log.d(Constants.TAG_LOG, "It is a disconnect");
                if (!WifiGroupListing.IsServer) {
                    WifiGroupListing.IsDisconnected = true;
                    ((FragmentChangeListener) activity).OnChangeToSubview(Constants.ID_MAIN_PAGE);
                }
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
            manager.requestPeers(channel, (WifiP2pManager.PeerListListener) activity);
            ((FragmentChangeListener)activity).OnChangeToSubview(Constants.PEERS_AVAILABLE);
        }
    }
}
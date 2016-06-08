
package com.wifidirect.appalanche.appalanchewifidirect.Events;


import android.net.wifi.p2p.WifiP2pInfo;

public class ConnectionInfoAvailable {
    public WifiP2pInfo p2pInfo;

    public ConnectionInfoAvailable(){

    }
    public ConnectionInfoAvailable(WifiP2pInfo p2pInfo){
        this.p2pInfo = p2pInfo;
    }

    public void setP2pInfo(WifiP2pInfo p2pInfo){ this.p2pInfo = p2pInfo; }
    public WifiP2pInfo getP2pInfo(){ return this.p2pInfo; }
}

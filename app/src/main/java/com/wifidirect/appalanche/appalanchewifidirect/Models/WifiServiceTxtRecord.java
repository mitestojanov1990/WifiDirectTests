
package com.wifidirect.appalanche.appalanchewifidirect.Models;

import android.net.wifi.p2p.WifiP2pDevice;


public class WifiServiceTxtRecord {
    public WifiP2pDevice device;
    public String TxtRecord = null;
    public String SSID = null;
    public String PassPhrase = null;
    public String ServerIp = null;
    public int UserID = 0;

    public WifiServiceTxtRecord(){

    }
    public WifiServiceTxtRecord(WifiP2pDevice device, String TxtRecord, String SSID, String PassPhrase, String ServerIp, int UserID){
        this.device = device;
        this.TxtRecord = TxtRecord;
        this.SSID = SSID;
        this.PassPhrase = PassPhrase;
        this.ServerIp = ServerIp;
        this.UserID = UserID;
    }
    public WifiServiceTxtRecord(WifiP2pDevice device){
        this.device = device;
    }

    public WifiP2pDevice getDevice() { return this.device; }
    public String getTxtRecord() { return this.TxtRecord; }
    public String getSSID() { return this.SSID; }
    public String getPassPhrase() { return this.PassPhrase;}
    public String getServerIp() { return this.ServerIp;}
    public void setServerIp(String ServerIp){ this.ServerIp = ServerIp; }

    public int getUserID(){ return this.UserID; }
    public void setUserID(int UserID){ this.UserID = UserID; }
}

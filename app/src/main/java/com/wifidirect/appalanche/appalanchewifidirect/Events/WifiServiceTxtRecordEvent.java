
package com.wifidirect.appalanche.appalanchewifidirect.Events;


import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;

public class WifiServiceTxtRecordEvent {
    public WifiServiceTxtRecord wifiServiceTxtRecord;

    public WifiServiceTxtRecordEvent(){

    }
    public WifiServiceTxtRecordEvent(WifiServiceTxtRecord wifiServiceTxtRecord){
        this.wifiServiceTxtRecord = wifiServiceTxtRecord;
    }

    public void setWifiServiceTxtRecord(WifiServiceTxtRecord wifiServiceTxtRecord){ this.wifiServiceTxtRecord = wifiServiceTxtRecord; }
    public WifiServiceTxtRecord getWifiServiceTxtRecord(){ return this.wifiServiceTxtRecord; }
}

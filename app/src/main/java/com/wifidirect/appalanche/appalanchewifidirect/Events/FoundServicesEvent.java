
package com.wifidirect.appalanche.appalanchewifidirect.Events;


import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;

import java.util.ArrayList;

public class FoundServicesEvent {
    public ArrayList<WifiServiceTxtRecord> FoundServices;

    public FoundServicesEvent(){

    }
    public FoundServicesEvent(ArrayList<WifiServiceTxtRecord> FoundServices){
        this.FoundServices = FoundServices;
    }

    public void setIsConnected(ArrayList<WifiServiceTxtRecord> FoundServices){ this.FoundServices = FoundServices; }
    public ArrayList<WifiServiceTxtRecord> getAll(){ return this.FoundServices; }
}

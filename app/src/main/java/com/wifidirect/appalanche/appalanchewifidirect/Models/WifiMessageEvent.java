
package com.wifidirect.appalanche.appalanchewifidirect.Models;

import android.net.wifi.p2p.WifiP2pDevice;


public class WifiMessageEvent {
    public String message = null;

    public WifiMessageEvent(){

    }
    public WifiMessageEvent(String message){
        this.message = message;
    }

    public void setMessage(String message){ this.message = message; }
    public String getMessage(){ return this.message; }
}

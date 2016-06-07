
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class WifiStatusEvent {
    public boolean isConnected = false;

    public WifiStatusEvent(){

    }
    public WifiStatusEvent(boolean isConnected){
        this.isConnected = isConnected;
    }

    public void setIsConnected(boolean isConnected){ this.isConnected = isConnected; }
    public boolean getIsConnected(){ return this.isConnected; }
}

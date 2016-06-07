
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class IsDisconnectedEvent {
    public boolean isDisconnected = false;

    public IsDisconnectedEvent(){

    }
    public IsDisconnectedEvent(boolean isDisconnected){
        this.isDisconnected = isDisconnected;
    }

    public void setIsConnected(boolean isConnected){ this.isDisconnected = isConnected; }
    public boolean getIsConnected(){ return this.isDisconnected; }
}

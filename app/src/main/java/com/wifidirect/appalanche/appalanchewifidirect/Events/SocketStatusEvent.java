
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class SocketStatusEvent {
    public boolean isConnected = false;

    public SocketStatusEvent(){

    }
    public SocketStatusEvent(boolean message){
        this.isConnected = isConnected;
    }

    public void setIsConnected(boolean isConnected){ this.isConnected = isConnected; }
    public boolean getIsConnected(){ return this.isConnected; }
}

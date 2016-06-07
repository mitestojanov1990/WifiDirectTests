
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class SocketProblemEvent {
    public boolean isConnected = false;

    public SocketProblemEvent(){

    }
    public SocketProblemEvent(boolean isConnected){
        this.isConnected = isConnected;
    }

    public void setIsConnected(boolean isConnected){ this.isConnected = isConnected; }
    public boolean getIsConnected(){ return this.isConnected; }
}

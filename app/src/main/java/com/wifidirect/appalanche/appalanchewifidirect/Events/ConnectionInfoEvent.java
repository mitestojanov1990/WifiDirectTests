
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class ConnectionInfoEvent {
    public boolean ConnectionInfo;

    public ConnectionInfoEvent(){

    }
    public ConnectionInfoEvent(boolean ConnectionInfo){
        this.ConnectionInfo = ConnectionInfo;
    }

    public void setConnectionInfo(boolean ConnectionInfo){ this.ConnectionInfo = ConnectionInfo; }
    public boolean getConnectionInfo(){ return this.ConnectionInfo; }
}

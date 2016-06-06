
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class ServerIpEvent {
    public String ipAddress;

    public ServerIpEvent(){

    }
    public ServerIpEvent(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public void setIpAddress(String ipAddress){ this.ipAddress = ipAddress; }
    public String getIpAddress(){ return this.ipAddress; }
}

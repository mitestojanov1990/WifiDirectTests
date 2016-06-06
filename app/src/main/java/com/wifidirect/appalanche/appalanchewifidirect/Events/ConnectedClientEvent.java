
package com.wifidirect.appalanche.appalanchewifidirect.Events;


import com.wifidirect.appalanche.appalanchewifidirect.MessageManager;

public class ConnectedClientEvent {
    public MessageManager mgr;

    public ConnectedClientEvent(){

    }
    public ConnectedClientEvent(MessageManager mgr){
        this.mgr = mgr;
    }

    public void setMgr(MessageManager mgr){ this.mgr = mgr; }
    public MessageManager getMgr(){ return this.mgr; }
}

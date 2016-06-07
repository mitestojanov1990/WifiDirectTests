
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class PeersEvent {
    public boolean Peers;

    public PeersEvent(){

    }
    public PeersEvent(boolean Peers){
        this.Peers = Peers;
    }

    public void setPeers(boolean ConnectionInfo){ this.Peers = Peers; }
    public boolean getPeers(){ return this.Peers; }
}

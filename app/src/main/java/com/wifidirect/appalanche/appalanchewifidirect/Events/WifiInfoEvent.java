
package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class WifiInfoEvent {
    public boolean infoAvailable = false;

    public WifiInfoEvent(){

    }
    public WifiInfoEvent(boolean infoAvailable){
        this.infoAvailable = infoAvailable;
    }

    public void setInfoAvailable(boolean infoAvailable){ this.infoAvailable = infoAvailable; }
    public boolean getInfoAvailable(){ return this.infoAvailable; }
}

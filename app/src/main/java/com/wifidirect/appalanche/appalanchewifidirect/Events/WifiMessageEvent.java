
package com.wifidirect.appalanche.appalanchewifidirect.Events;

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

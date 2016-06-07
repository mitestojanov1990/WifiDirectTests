
package com.wifidirect.appalanche.appalanchewifidirect.Events;

import com.wifidirect.appalanche.appalanchewifidirect.Models.AppMessage;

public class ClientMessageEvent {
    public AppMessage message = null;

    public ClientMessageEvent(){

    }
    public ClientMessageEvent(AppMessage message){
        this.message = message;
    }

    public void setMessage(AppMessage message){ this.message = message; }
    public AppMessage getMessage(){ return this.message; }
}


package com.wifidirect.appalanche.appalanchewifidirect.Events;


public class MyDeviceNameEvent {
    public String deviceName;

    public MyDeviceNameEvent(){

    }
    public MyDeviceNameEvent(String deviceName){
        this.deviceName = deviceName;
    }

    public void setDeviceName(String deviceName){ this.deviceName = deviceName; }
    public String getDeviceName(){ return this.deviceName; }
}

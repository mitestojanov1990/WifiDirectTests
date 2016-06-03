package com.wifidirect.appalanche.appalanchewifidirect.Events;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ReceivedMeasurementEvent {
    private double x;
    private double y;
    private double rssi;
    private boolean send = false;

    public ReceivedMeasurementEvent(double x, double y, double rssi, boolean send) {
        this.x = x;
        this.y = y;
        this.rssi = rssi;
        this.send = send;
    }
    public ReceivedMeasurementEvent(String data){
        parseData(data);
    }

    public void parseData(String json){
        try {
            JSONObject jObj = new JSONObject(json);
            this.rssi = jObj.getDouble("rssi");
            this.x = jObj.getDouble("x");
            this.y = jObj.getDouble("y");
        }
        catch (JSONException ex) {
            Log.d("EventBus", "Problem parsing Measurement Obj");
        }
    }

    public String stringifyPair(){
        JSONObject jMsg = new JSONObject();
        try {
            jMsg.put("x", this.x);
            jMsg.put("y", this.y);
            jMsg.put("rssi", this.rssi);
            return jMsg.toString();
        }
        catch (JSONException ex) {
            Log.d("EventBus", "Problem stringify Measurement Obj");
            return "";
        }
    }
    public boolean isSend(){
        return this.send;
    }
}
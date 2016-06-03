package com.wifidirect.appalanche.appalanchewifidirect.Models;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppMessage {

    public String DeviceID;
    public Date TimeStamp;
    public double Latitude;
    public double Longitude;

    public String msgTxt;

    public AppMessage() {
        TimeStamp = new Date();
    }
    public void PopulateFromJSON(String json) {
        try {
            JSONObject jObj = new JSONObject(json);
            this.DeviceID = jObj.getString("id");
//            this.Longitude = jObj.getDouble("long");
//            this.Latitude = jObj.getDouble("lat");
            this.msgTxt = jObj.getString("msgTxt");
        }
        catch (JSONException ex) {

        }
    }
    public String ToJSONString() {
        JSONObject jMsg = new JSONObject();
        try {
            jMsg.put("id", this.DeviceID);
//            jMsg.put("lat", Double.toString(this.Latitude));
//            jMsg.put("long", Double.toString(this.Longitude));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String ts = sdf.format(this.TimeStamp);
            jMsg.put("ts", ts);
            jMsg.put("msgTxt", this.msgTxt);
            return jMsg.toString();
        }
        catch (JSONException ex) {
            return "";
        }
    }

}

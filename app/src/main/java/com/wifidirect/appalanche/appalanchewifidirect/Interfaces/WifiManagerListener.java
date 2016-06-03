package com.wifidirect.appalanche.appalanchewifidirect.Interfaces;

public interface WifiManagerListener {
    public void SendMessage(String msg);
    public void SetServerIpAddress(String addr);
    public void ConnectToSocket();
    public void GetMyDeviceName(String deviceName);
    public void GetWifiStatus(boolean isConnected);
    public void GetSocketStatus(boolean isConnected);
}

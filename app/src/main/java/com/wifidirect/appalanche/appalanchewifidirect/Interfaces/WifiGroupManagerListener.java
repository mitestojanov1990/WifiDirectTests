package com.wifidirect.appalanche.appalanchewifidirect.Interfaces;

import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;

public interface WifiGroupManagerListener {
    public void GetSocketStatus(boolean isSocketConnected);
    public void SocketProblemDisconnect(boolean isConnected);
    public void GetWifiStatus(boolean isConnected);
    public void SendMessage(String msg);

    public void SetServerIpAddress(String addr);
    public void ConnectToSocket();
    public void GetMyDeviceName(String deviceName);

    public void onClickConnectWifi(WifiServiceTxtRecord record);
}

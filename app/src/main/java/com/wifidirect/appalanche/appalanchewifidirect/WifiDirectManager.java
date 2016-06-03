package com.wifidirect.appalanche.appalanchewifidirect;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.WifiManagerListener;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiDirectManager {
    private Context context;
    private Activity activity;

    private final IntentFilter intentFilter = new IntentFilter();

    private WifiManager wifi;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel _channel;

    private WifiP2pDnsSdServiceRequest serviceRequest;

    public static ArrayList<WifiServiceTxtRecord> FoundServices = new ArrayList<WifiServiceTxtRecord>();
    private ArrayList<String> _foundServiceNames = new ArrayList<String>();

    private String SSID = "not set";
    private String PassPhrase = "not set";
    private String ServerIp;
    private String UserID;

    public WifiDirectManager(Context context, Activity activity, IntentFilter intentFilter){

        this.context = context;
        this.activity = activity;
        intentFilter = intentFilter;

        wifi = (WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);

        manager = (WifiP2pManager) this.context.getSystemService(Context.WIFI_P2P_SERVICE);


        Init();
    }

    private static WifiDirectManager instance = null;
    public static WifiDirectManager getInstance(Context context, Activity activity, IntentFilter intentFilter) {
        if(instance == null) {
            instance = new WifiDirectManager(context, activity, intentFilter);
        }
        return instance;
    }

    public WifiP2pManager getWifiP2pManager(){
        return manager;
    }
    public WifiP2pManager.Channel getChannel(){
        return _channel;
    }

    public void Init(){
        _channel = manager.initialize(this.context, this.context.getMainLooper(), null);
    }

    public void ToggleWifiMode(boolean enabled){
        wifi.setWifiEnabled(enabled);
    }
    public boolean IsWifiEnabled(){
        return wifi.isWifiEnabled();
    }
    public String getScanResultSecurity(ScanResult scanResult) {

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }
    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration.SSID = "\"" + networkSSID + "\"";

        if (securityMode.equalsIgnoreCase("OPEN")) {

            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        } else if (securityMode.equalsIgnoreCase("WEP")) {

            wifiConfiguration.wepKeys[0] = "\"" + networkPasskey + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        } else if (securityMode.equalsIgnoreCase("PSK")) {

            wifiConfiguration.preSharedKey = "\"" + networkPasskey + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        } else {
            return null;
        }

        return wifiConfiguration;

    }
    public int connectToAP() {
        List<ScanResult> scanResultList = wifi.getScanResults();
        for (ScanResult result : scanResultList) {

            if (result.SSID.equals(tmpSSID)) {

                String securityMode = getScanResultSecurity(result);

                WifiConfiguration wifiConfiguration = createAPConfiguration(tmpSSID, tmpPass, securityMode);

                int res = wifi.addNetwork(wifiConfiguration);

                boolean b = wifi.enableNetwork(res, true);

                wifi.setWifiEnabled(true);

                boolean changeHappen = wifi.saveConfiguration();

                if (res != -1 && changeHappen) {
                } else {
                }

                return res;
            }
        }

        return -1;
    }

    int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            //return SECURITY_PSK;
            return 1;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            //return SECURITY_EAP;
            return 2;
        }
        return (config.wepKeys[0] != null) ? 0 : 3; //SECURITY_WEP : SECURITY_NONE;
    }

    private String tmpSSID;
    private String tmpPass;
    public void ConnectToWifi(String SSID, String PassPhrase){
//        wifi.startScan();
//        tmpSSID  = SSID;
//        tmpPass = PassPhrase;
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = String.format("\"%s\"", SSID);
        wfc.preSharedKey = String.format("\"%s\"", PassPhrase);
        wfc.status = WifiConfiguration.Status.ENABLED;

        /*
        * RSN = WPA2/IEEE 802.11i
        * WPA = WPA/IEEE 802.11i/D3.0
        * taken from WifiConfiguration DOCS
        * */
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        /*
        * WPA pre-shared key (requires preSharedKey to be specified).
        * */
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        /*
        * CCMP = AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
        * TKIP = Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
        * NONE - depricated
        * taken from WifiConfiguration DOCS
        * */
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        /*
        * WEP40 = WEP (Wired Equivalent Privacy) with 40-bit key (original 802.11)
        * WEP104 = WEP (Wired Equivalent Privacy) with 104-bit key
        * CCMP = AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
        * TKIP = Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
        * */
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

        //remember id
        int netId = wifi.addNetwork(wfc);

        boolean changeHappen = wifi.saveConfiguration();
        boolean hasPing = wifi.pingSupplicant();

        WifiConfiguration tmp = null;
        List<WifiConfiguration> list = wifi.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                tmp = i;
                break;
            }
        }
//        int tmpSecurity = getSecurity(tmp);

        ((WifiManagerListener) activity).SendMessage("Net ID = " + netId);

        if (netId != -1) {
            wifi.disconnect();
            wifi.enableNetwork(netId, true);
            wifi.reconnect();
            wifi.reassociate();
//            if(wifi.disconnect()){
//                if(wifi.enableNetwork(netId, true)){
//                    if(wifi.reconnect()){
//                        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//                        if (networkInfo.isConnected()) {
//                            ((WifiManagerListener) activity).SendMessage("Connected");
//                        }else{
//                            ((WifiManagerListener) activity).SendMessage("Not connected " + networkInfo.getReason());
//                        }
//                    }else{
//                        ((WifiManagerListener) activity).SendMessage("Problem connecting to wifi network");
//                    }
//                }else{
//                    ((WifiManagerListener) activity).SendMessage("Problem enabling wifi network");
//                }
//            }else{
//                ((WifiManagerListener) activity).SendMessage("Cannot disconnect from previous network");
//            }

        }else{
            ((WifiManagerListener) activity).SendMessage("Cannot connect");
        }
    }

    public void DisconnectFromWifi(){
        wifi.disconnect();
        ((WifiManagerListener) activity).SendMessage("Disconnected");
    }

    public void ConnectToDevice(WifiP2pConfig config, WifiP2pManager.ActionListener listener){
        manager.connect(_channel, config, listener);
    }
    public void RemoveServiceRequest(WifiP2pDnsSdServiceRequest serviceRequest, WifiP2pManager.ActionListener listener){
        manager.removeServiceRequest(_channel, serviceRequest,listener);
    }
    public void AddServiceRequest(WifiP2pDnsSdServiceRequest serviceRequest, WifiP2pManager.ActionListener listener){
        manager.addServiceRequest(_channel, serviceRequest, listener);

    }

    public void AddLocalService(WifiP2pManager.ActionListener listener){
        Map record = new HashMap();
        record.put("SSID", this.SSID);
        record.put("PassPhrase", this.PassPhrase);
        record.put("ServerIp", this.ServerIp);
        record.put("UserID", this.UserID);
        record.put("available", "visible");
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);
        manager.addLocalService(_channel, serviceInfo, listener);
    }
    public void ClearLocalServices(WifiP2pManager.ActionListener listener){
        manager.clearLocalServices(_channel, listener);
    }

    public void RequestPeers(WifiP2pManager.PeerListListener listener){
        manager.requestPeers(_channel, listener);
    }

    public void RequestGroupInfo(WifiP2pManager.GroupInfoListener listener){
        manager.requestGroupInfo(_channel, listener);
    }
    public void CreateGroup(WifiP2pManager.ActionListener listener){
        manager.createGroup(_channel, listener);
    }
    public void RemoveGroup(WifiP2pManager.ActionListener listener){
        manager.removeGroup(_channel, listener);
    }
    public void StopPeerDiscovery(WifiP2pManager.ActionListener listener){
        manager.stopPeerDiscovery(_channel, listener);
    }
    public void DiscoverPeers(WifiP2pManager.ActionListener listener){
        manager.discoverPeers(_channel, listener);
    }
    public void DiscoverServices(WifiP2pManager.ActionListener listener){
        manager.discoverServices(_channel, listener);
    }
    public void SetDnsSdResponseListeners(WifiP2pManager.DnsSdServiceResponseListener responseListener, WifiP2pManager.DnsSdTxtRecordListener recordListener){
        manager.setDnsSdResponseListeners(_channel, responseListener, recordListener);
    }
    public void RequestConnectionInfo(WifiP2pManager.ConnectionInfoListener listener){
        manager.requestConnectionInfo(_channel, listener);
    }

    public void AddFoundService(WifiServiceTxtRecord service, String devName){
        FoundServices.add(service);
        _foundServiceNames.add(devName);
    }
    public WifiServiceTxtRecord GetByName(String name){
        for (int i = 0; i < FoundServices.size(); i++) {
             WifiServiceTxtRecord tmp = FoundServices.get(i);
            if(tmp.getSSID() + tmp.getPassPhrase() == name){
                return tmp;
            }
        }
        return null;
    }

    public WifiServiceTxtRecord GetByHighestPriority(){
        int priority = 0;
        WifiServiceTxtRecord record = null;
        for (int i = 0; i < FoundServices.size(); i++) {
            WifiServiceTxtRecord tmp = FoundServices.get(i);
            if(tmp.getUserID() > priority){
                record = tmp;
            }
        }
        return record;
    }

    public void ClearFoundServices(){
        _foundServiceNames.clear();
        FoundServices.clear();
    }

    public ArrayList<String> GetFoundServiceNames(){
        return _foundServiceNames;
    }

    public void setSSID(String SSID){
        this.SSID = SSID;
    }
    public void setPassPhrase(String PassPhrase){
        this.PassPhrase = PassPhrase;
    }
    public void setServerIp(String ServerIp)
    {
        this.ServerIp = ServerIp;
    }
    public String getSSID(){return this.SSID; }
    public String getPassPhrase(){return this.PassPhrase; }
    public String getUserID(){return this.UserID;};
    public void setUserID(String UserID){this.UserID = UserID;}

}

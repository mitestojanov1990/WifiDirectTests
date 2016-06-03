package com.wifidirect.appalanche.appalanchewifidirect;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wifidirect.appalanche.appalanchewifidirect.Adapters.ServiceTxtRecordAdapter;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.ClientSocketHandler;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.GroupOwnerSocketHandler;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.FragmentChangeListener;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.MessageForwarder;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.MessageTarget;
import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.WifiManagerListener;
import com.wifidirect.appalanche.appalanchewifidirect.Models.AppMessage;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiMessageEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiStatusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WifiGroupListing extends AppCompatActivity implements
        android.os.Handler.Callback,
        MessageTarget,
        MessageForwarder,
        WifiP2pManager.ChannelListener,
        WifiP2pManager.ConnectionInfoListener,
        FragmentChangeListener,
        WifiP2pManager.PeerListListener,
        WifiManagerListener{


    public static EventBus eventBus;
    public static Activity curActivity;

    public WifiGroupListing(){

    }
    public WifiGroupListing(Activity activity){
        curActivity = activity;
    }

    public void RegisterEvent(EventBus eb){
        eventBus = eb;
    }

    public WifiMessageEvent getWifiMessageEvent(String message){
        return new WifiMessageEvent(message);
    }

    public WifiStatusEvent getWifiStatusEvent(boolean isConnected){
        return new WifiStatusEvent(isConnected);
    }

    public class Tuple<E, F> {
        public E DeviceName;
        public F UserID;
    }
    public List<Tuple<String, Integer>> fixedUsers = new java.util.ArrayList<>();
    public Tuple<String, Integer> myDevice = null;

    public static WifiDirectManager wifiDirectManager = null;
    public static boolean IsDisconnected = false;
    public static boolean IsServer = false;

    private final IntentFilter intentFilter = new IntentFilter();

    public static ArrayList<MessageManager> ConnectedClientManagers = new ArrayList<MessageManager>(); // holds all message managers (connected clients)

    public static WifiServiceTxtRecord curRecord = null;

    static RecyclerView mRecyclerView;                           // Declaring RecyclerView
    static RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;


    private MessageManager messageManager;
    private boolean isMassageManagerSet = false;

    private boolean GroupCreated = false;
    private boolean retryChannel = false;
    private boolean _IsInitiated = false;
    private boolean _isServer = false;
    private boolean _serverThreadCreated = false;
    private Handler handler = new Handler(this);
    private Handler requestGroupInfoHandler = new Handler(this);
    private Handler checkForAvailableConnectionsHandler = new Handler(this);
    private Handler reDisocverHandler = new Handler(this);
    private BroadcastReceiver _receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;


    public Button discoveryStartBtn;
    public Button setServiceRequest;
    public Button StartLocalService;
    public Button startListeners;
    public Button sendMsg;
    public Button broadcastMsg;
    public Button StartWifiManager;
    public Button StopWifiManager;
    public Button DisconnectBtn;
    public Button discoverPeersBtn;
    public Button clearAll;
    public Button StartServerSocket;

    public Button RequestConnectionInfoBtn;
    public Button RequestGroupInfoBtn;
    public Button RemoveGroupBtn;
    public Button CreateGroupBtn;

    private boolean IsConnected = false;
    public boolean IsSocketConnected = false;

    private boolean canYouBeServer = true;

    private TextView statusTxtView;

    public static ArrayList<String> DataList = new ArrayList<String>(); // holds all collected JSON data

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_devices_list);
        curActivity = this;
        Init();
    }

    private void PopulateTempUsers(){
        Tuple<String, Integer> tmp = new Tuple<String, Integer>();
        tmp.DeviceName = "Telly TesterMM's G2 mini";
        tmp.UserID = -1;
        fixedUsers.add(tmp);
        tmp = new Tuple<String, Integer>();
        tmp.DeviceName = "G4";
        tmp.UserID = 2;
        fixedUsers.add(tmp);
        tmp = new Tuple<String, Integer>();
        tmp.DeviceName = "Android_4f60";
        tmp.UserID = 3;
        fixedUsers.add(tmp);
        tmp = new Tuple<String, Integer>();
        tmp.DeviceName = "Android_bfe2";
        tmp.UserID = 1;
        fixedUsers.add(tmp);
    }

    private void ClearAll(){
        statusTxtView.setText("");
    }

    public void Init(){

        PopulateTempUsers();
      statusTxtView = (TextView)findViewById(R.id.status_text);
        sendMsg = (Button) findViewById(R.id.sendMsg);

        sendMsg = (Button) findViewById(R.id.sendMsg);
        broadcastMsg = (Button) findViewById(R.id.broadcastMsg);

        clearAll = (Button) findViewById(R.id.clearAll);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClearAll();
            }
        });

        StartServerSocket = (Button)  findViewById(R.id.StartServerSocket);
        StartServerSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateServerSocket();
            }
        });
        DisconnectBtn = (Button)  findViewById(R.id.DisconnectBtn);
        DisconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisconnectFromWifi();
            }
        });

        StartWifiManager = (Button) findViewById(R.id.StartWifiManager);
        StartWifiManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiDirectManager = WifiDirectManager.getInstance(getApplicationContext(), getParent(), intentFilter);
                updateItems(WifiDirectManager.FoundServices);
                appendStatus("Start Wifi Direct Manager");
            }
        });
        StopWifiManager = (Button) findViewById(R.id.StopWifiManager);
        StopWifiManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiDirectManager = null;
                appendStatus("Stop Wifi Direct Manager");
            }
        });


        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //messageFragment.SendMessageToServer("Client says hello!");
                HandleMessageToServer("Client says hello!");
            }
        });
        broadcastMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //messageFragment.SendBroadcastMessage("Server says hello!");
                HandleBroadcastMessage("Server says hello!");
            }
        });

        StartLocalService = (Button)findViewById(R.id.StartLocalService);
        StartLocalService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartLocalService();
            }
        });
        startListeners = (Button)findViewById(R.id.startListeners);
        startListeners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetDnsSdListeners();
            }
        });
        setServiceRequest = (Button)findViewById(R.id.setServiceRequest);
        setServiceRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetServiceRequest();
            }
        });

        discoveryStartBtn = (Button)findViewById(R.id.discoveryStartBtn);
        discoveryStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData(false);
                DiscoverServices();
            }
        });

        discoverPeersBtn = (Button)findViewById(R.id.discoverPeersBtn);
        discoverPeersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetData(false);
                DiscoverPeers();
            }
        });


        CreateGroupBtn = (Button)findViewById(R.id.CreateGroupBtn);
        CreateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateGroup();
            }
        });

        RemoveGroupBtn = (Button)findViewById(R.id.RemoveGroupBtn);
        RemoveGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveGroup();
            }
        });
        RequestGroupInfoBtn = (Button)findViewById(R.id.RequestGroupInfoBtn);
        RequestGroupInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestGroupInfo(false);
            }
        });
        RequestConnectionInfoBtn = (Button)findViewById(R.id.RequestConnectionInfoBtn);
        RequestConnectionInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestConnectionInfo();
            }
        });

        SetRecyclerView();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiDirectManager = WifiDirectManager.getInstance(getApplicationContext(), this, intentFilter);
        updateItems(WifiDirectManager.FoundServices);
        appendStatus("Start Wifi Direct Manager");

       // SetDnsSdListeners();
        //StartAutomaticSearch();
    }

    private void DisconnectFromWifi(){
        wifiDirectManager.DisconnectFromWifi();
        IsConnected = false;
        curRecord = null;
        appendStatus("Disconnect from Wifi");
    }

    private void StartAutomaticSearch(){
        SetServiceRequest();

        checkForAvailableConnectionsHandler.postDelayed(checkForAvailableConnections, 5000);
    }

    private void reDiscoverServices(boolean repeat){
        if(repeat)
            reDisocverHandler.postDelayed(reDiscoverRecords, 20000);
    }

    private void CheckIfConnectionsAvailable(){
        if (WifiDirectManager.FoundServices.size() > 0) {
            //if(!IsConnected) {
                FindHighestPriorityConnection();
            //}
        } else {
            // Create Group
            if (!GroupCreated)
                CreateGroup();
        }
        reDiscoverServices(true);
    }

    private void FindHighestPriorityConnection(){
        WifiServiceTxtRecord tmp = wifiDirectManager.GetByHighestPriority();
        if(tmp != null) {
            appendStatus("Priority: " + tmp.getUserID());
            if (curRecord == null) {
                CheckAndConnect(tmp);
            } else if (tmp.getUserID() > curRecord.getUserID()) {
                CheckAndConnect(tmp);
            }else{
                if(!IsSocketConnected){
                    ConnectToSocket();
                }
            }
        }
    }
    private void CheckAndConnect(WifiServiceTxtRecord tmp){
        // Disconnected if connected
        if(IsConnected){
            wifiDirectManager.DisconnectFromWifi();
            IsConnected = false;
        }
        // Connect
        curRecord = tmp;
        ConnectToWifi(curRecord.getSSID(), curRecord.getPassPhrase());
    }

    private void SetRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.recordsView);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator(  ));
    }

    public void updateItems(ArrayList<WifiServiceTxtRecord> FoundServices){
        mAdapter = new ServiceTxtRecordAdapter(FoundServices, getParent());
        mRecyclerView.setAdapter(mAdapter);
    }
    public static void onConnectToSocket(String ServerIp){
        ((WifiManagerListener)curActivity).SetServerIpAddress(ServerIp);
    }
    public static void WifiTxtRecordOnClick(WifiServiceTxtRecord record){
        EventBus.getDefault().post(record);
    }
    @Subscribe
    public void onEvent(WifiServiceTxtRecord event){
        ConnectToWifi(event.getSSID(), event.getPassPhrase());
    }
    @Subscribe
    public void onEvent(WifiStatusEvent event){
        ((WifiManagerListener)curActivity).SetServerIpAddress(curRecord.getServerIp());
    }

    private void StopLocalService(){
        wifiDirectManager.ClearLocalServices(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Test", "ClearLocalServices success");
                appendStatus("ClearLocalServices success");
            }
            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
                Log.i("Test","Failed clearing local service " + getErrorStatusByCode(error));
                appendStatus("Failed clearing local service " + getErrorStatusByCode(error));

            }
        });
    }

    private void StartLocalService() {
        wifiDirectManager.ClearLocalServices(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Test","ClearLocalServices success");
                appendStatus("ClearLocalServices success");
                wifiDirectManager.AddLocalService(new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // service broadcasting started
//                        mServiceBroadcastingHandler
//                                .postDelayed(mServiceBroadcastingRunnable,
//                                        3000);
                        //discoverService();
                        Log.i("Testing","AddLocalService success");
                        appendStatus("AddLocalService success");
                    }

                    @Override
                    public void onFailure(int error) {
                        Log.i("Test","Failed to add a service " + getErrorStatusByCode(error));
                        appendStatus("Failed to add a service " + getErrorStatusByCode(error));
                    }
                });
            }
            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
                Log.i("Test","Failed clearing local service " + getErrorStatusByCode(error));
                appendStatus("Failed clearing local service " + getErrorStatusByCode(error));
            }
        });
    }

    private void stopRegistrationAndDiscovery(){
        StopDiscovery(new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.i("Test","Failed stopPeerDiscovery :" + getErrorStatusByCode(reasonCode));
                appendStatus("Failed stopPeerDiscovery :" + getErrorStatusByCode(reasonCode));
            }
            @Override
            public void onSuccess() {
                wifiDirectManager.ClearLocalServices(new WifiP2pManager.ActionListener(){
                    @Override
                    public void onSuccess() {
                        Log.i("Testing","stopRegistrationAndDiscovery success");
                        appendStatus("stopRegistrationAndDiscovery success");
                        if(_isServer) {
                            GroupOwnerSocketHandler.CloseSocket();
                            _serverThreadCreated = false;
                        }
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Log.i("Test","Failed to remove a service :" + getErrorStatusByCode(reasonCode));
                        appendStatus("Failed to remove a service :" + getErrorStatusByCode(reasonCode));
                    }
                });
            }
        });
    }


    private void Disconnect(final WifiP2pManager.ActionListener listener){
        wifiDirectManager.RequestGroupInfo(new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    // clients require these
                    String ssid = group.getNetworkName();
                    String passphrase = group.getPassphrase();

                    wifiDirectManager.RemoveGroup(listener);
                    if(IsServer)
                    ConnectedClientManagers.clear();
                }else{
                    listener.onSuccess();
                    appendStatus("Group not found/dc");
                }
            }
        });
    }

    public void StopDiscovery(final WifiP2pManager.ActionListener listener){
        if (wifiDirectManager.getManager() != null && wifiDirectManager.getChannel() != null) {
            Disconnect(new WifiP2pManager.ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.i("Testing","Disconnect failed. Reason :" + getErrorStatusByCode(reasonCode));
                    appendStatus("Disconnect failed. Reason :" + getErrorStatusByCode(reasonCode));
                }

                @Override
                public void onSuccess() {
                    //wifiDirectManager.StopPeerDiscovery(listener);
                    Log.i("Testing","StopDiscovery success");
                    appendStatus("StopDiscovery success");
                }
            });
        }else{
            listener.onFailure(10);
        }
    }

    private void SetDnsSdListeners() {
        wifiDirectManager.SetDnsSdResponseListeners(
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {


                            appendStatus("DnsSd Service found!");
                        //}
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {

                        String devName = device.deviceAddress.trim().toLowerCase();
                        String SSID = "not set";
                        String PassPhrase = "not set";
                        String ServerIp = "0.0.0.0";
                        int UserID = 0;
                        if(record.get("SSID") != null)
                            SSID = record.get("SSID");
                        if(record.get("PassPhrase") != null)
                            PassPhrase = record.get("PassPhrase");
                        if(record.get("ServerIp") != null)
                            ServerIp = record.get("ServerIp");
                        if(record.get("UserID") != null)
                            UserID = Integer.parseInt(record.get("UserID"));
                        if (wifiDirectManager.GetFoundServiceNames().indexOf(devName) == -1 && device.status == WifiP2pDevice.AVAILABLE) {
                            appendStatus("Txt Record: " + device.deviceName);
                            WifiServiceTxtRecord txtrecord = new WifiServiceTxtRecord();
                            txtrecord.device = device;
                            txtrecord.TxtRecord = SSID + " p:" + PassPhrase;
                            txtrecord.SSID = SSID;
                            txtrecord.PassPhrase = PassPhrase;
                            txtrecord.setServerIp(ServerIp);
                            txtrecord.setUserID(UserID);
                            wifiDirectManager.AddFoundService(txtrecord, devName);
                            appendStatus(devName);
                            updateItems(WifiDirectManager.FoundServices);
                        }
                        else {
                            Log.i("Testing", "Txt Record exists in list: " + SSID + " status: " + getDeviceStatusByCode(device.status));
                            appendStatus("Service exists in list: " + SSID + " status: " + getDeviceStatusByCode(device.status));

                            updateItems(WifiDirectManager.FoundServices);
                        }
                    }
                });
        Log.i("Testing","Set Dns Listeners");
        appendStatus("Set Dns Listeners");
    }

    private void RemoveServiceRequest(){
        if(serviceRequest != null) {
            wifiDirectManager.RemoveServiceRequest(serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    appendStatus("RemoveServiceRequest Success");
                }

                @Override
                public void onFailure(int arg0) {
                    Log.i("Testing", "Failed removing service request :" + getErrorStatusByCode(arg0));
                    appendStatus("Failed removing service request :" + getErrorStatusByCode(arg0));
                }
            });
        }
    }

    private void SetServiceRequest(){
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        wifiDirectManager.RemoveServiceRequest(serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                wifiDirectManager.AddServiceRequest(serviceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i("Testing","AddServiceRequest Success");
                        appendStatus("AddServiceRequest Success");

                        //resetData(true);
                        DiscoverServices();
                    }
                    @Override
                    public void onFailure(int arg0) {
                        Log.i("Testing","Failed addServiceRequest :" + getErrorStatusByCode(arg0));
                        appendStatus("Failed addServiceRequest :" + getErrorStatusByCode(arg0));
                    }
                });
            }
            @Override
            public void onFailure(int arg0) {
                Log.i("Testing","Failed removing service request :" + getErrorStatusByCode(arg0));
                appendStatus("Failed removing service request :" + getErrorStatusByCode(arg0));
            }
        });
    }

    private void DiscoverServices(){

        wifiDirectManager.DiscoverServices(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Testing","DiscoverServices success");
                appendStatus("DiscoverServices success");
//                                mServiceDiscoveringHandler.postDelayed(
//                                        mServiceDiscoveringRunnable,
//                                        Constants.SERVICE_DISCOVERING_INTERVAL);
                updateItems(WifiDirectManager.FoundServices);
            }
            @Override
            public void onFailure(int arg0) {
                Log.i("Testing","Failed discoverServices :"  + getErrorStatusByCode(arg0));
                appendStatus("Failed discoverServices :" + getErrorStatusByCode(arg0));
                canYouBeServer = false;
                RemoveGroup();
            }
        });

    }

    private void StopDiscoverPeers(){
        wifiDirectManager.StopPeerDiscovery(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Testing","StopDiscoverPeers success");
                appendStatus("StopDiscoverPeers success");
            }

            @Override
            public void onFailure(int error) {
                Log.i("Testing","Failed discoverServices :" + getErrorStatusByCode(error));
                appendStatus("Failed discoverServices :" + getErrorStatusByCode(error));
            }
        });
    }

    private void DiscoverPeers(){
        wifiDirectManager.DiscoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("Testing","DiscoverPeers initiated");
                appendStatus("DiscoverPeers initiated");
            }

            @Override
            public void onFailure(int error) {
                Log.i("Testing","Failed DiscoverPeers :" + getErrorStatusByCode(error));
                appendStatus("Failed DiscoverPeers :" + getErrorStatusByCode(error));
            }
        });
    }

    private Runnable reDiscoverRecords = new Runnable() {
        @Override
        public void run() {
            StartAutomaticSearch();
        }
    };

    private Runnable checkForAvailableConnections = new Runnable(){

        @Override
        public void run() {
            CheckIfConnectionsAvailable();
        }
    };

    private Runnable requestGroupInfoRunnable = new Runnable() {
        @Override
        public void run() {
            RequestGroupInfo(false);
        }
    };

    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            wifiDirectManager.DiscoverPeers(new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
//                    mServiceBroadcastingHandler
//                            .postDelayed(mServiceDiscoveringRunnable, 3000);
                }

                @Override
                public void onFailure(int error) {
                    appendStatus("Failed DiscoverPeers :" + getErrorStatusByCode(error));
                }
            });
        }
    };

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            DiscoverServices();
        }
    };

    private void ConnectToWifi(String SSID, String PassPhrase){
        appendStatus("Connecting to wifi...");
        wifiDirectManager.ConnectToWifi(SSID, PassPhrase);
    }

    private void ConnectToDevice(final String deviceAddress, int ConnectionType){
        Log.i("Testing","Connect to gendevice: " + deviceAddress);
        appendStatus("Connect to gendevice: " + deviceAddress);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = ConnectionType;

        //config.groupOwnerIntent = groupOwner;

//        if (chkServer.isChecked())
//            config.groupOwnerIntent = 15;

        wifiDirectManager.ConnectToDevice(config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess(){

            }
            @Override
            public void onFailure(int errorCode) {
                Log.i("Testing","Failed connecting to service: " + getErrorStatusByCode(errorCode));
                appendStatus("Failed connecting to service:" + getErrorStatusByCode(errorCode));
            }
        });
    }
    private void genericConnectToDevice(final String deviceAddress, int ConnectionType) {
        Log.i("Testing","Connecting...");
        appendStatus("Connecting...");
        ConnectToDevice(deviceAddress, ConnectionType);
//        if (serviceRequest != null){
//            Log.i("Testing","serviceRequest is alive");
//            wifiDirectManager.RemoveServiceRequest(serviceRequest, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    ConnectToDevice(deviceAddress);
//                }
//                @Override
//                public void onFailure(int arg0) {
//                    Log.i("Testing","Failed removeServiceRequest: " + getErrorStatusByCode(arg0));
//                    appendStatus("Failed removeServiceRequest:" + getErrorStatusByCode(arg0));
//                }
//            });
//        }else{
//            ConnectToDevice(deviceAddress);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(wifiDirectManager != null){
            _receiver = new WiFiDirectBroadcastReceiver(wifiDirectManager.getManager(), wifiDirectManager.getChannel(), this);
            registerReceiver(_receiver, intentFilter);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            RegisterEvent(EventBus.getDefault());

        }
    }

    @Override
    public void onDestroy(){
        resetData(false);
        DisconnectFromWifi();
        RemoveServiceRequest();
        StopLocalService();
        super.onDestroy();

    }
    @Override
    public void onPause() {
        super.onPause();
        if(_receiver != null)
            unregisterReceiver(_receiver);
        EventBus.getDefault().unregister(this);
    }
    @Override
    protected void onRestart() {
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler = null;

        if (p2pInfo.isGroupOwner) {
            _isServer = true;
            Log.d(Constants.TAG_LOG, "Connected as server (group owner) MAC");
            appendStatus("Connected as server (group owner) MAC");
            if (!_serverThreadCreated) {
                try {
                    Log.d(Constants.TAG_LOG, "Create GroupOwnerSocketHandler");
                    appendStatus("Create GroupOwnerSocketHandler");
                    handler = new GroupOwnerSocketHandler(this.getHandler(), this);
                    handler.start();
                    appendStatus("Group Owner Address: " + p2pInfo.groupOwnerAddress);
                    wifiDirectManager.setServerIp(p2pInfo.groupOwnerAddress.getHostAddress());
//                    String curName = wifiDirectManager.getSSID()+wifiDirectManager.getPassPhrase();
//                    WifiServiceTxtRecord tmp = wifiDirectManager.GetByName(curName);
//                    tmp.setServerIp(p2pInfo.groupOwnerAddress.getHostAddress());
//                    updateItems(wifiDirectManager.FoundServices);
                    IsConnected = true;
                } catch (IOException e) {
                    Log.d(Constants.TAG_LOG, "Failed to create a server thread - " + e.getMessage());
                    appendStatus("Failed to create a server thread - " + e.getMessage());
                    IsConnected = false;
                    return;
                }
                _serverThreadCreated = true;
            }
        }
        else {
            _isServer = false;
            Log.d(Constants.TAG_LOG, "Connected as client (peer)");
            appendStatus("Connected as client (peer)");
            handler = new ClientSocketHandler(this.getHandler(), p2pInfo.groupOwnerAddress, this);
            handler.start();
            Log.d(Constants.TAG_LOG, "ClientHandlerStart");
        }
        IsServer = _isServer;
        //if (!messageFragmentSet) {
        //    messageFragment = new MessageFragment();
        //    messageFragment.MessageForwarder = this;
        //    getFragmentManager().beginTransaction().replace(R.id.container_root, messageFragment, "messages").commit();
            //getFragmentManager().beginTransaction().commit();
        //    messageFragmentSet = true;
        //}
        //if(_isServer) {
//            mServiceBroadcastingHandler
//                    .postDelayed(mServiceBroadcastingRunnable, 3000);
        //}

    }

    private void resetData(boolean reset) {
        if(reset) {
            _IsInitiated = false;
            wifiDirectManager.ClearFoundServices();
            //ConnectedClientManagers.clear();
            updateItems(WifiDirectManager.FoundServices);
        }
    }

    public void appendStatus(String status) {
        if(eventBus != null)
            eventBus.post(new WifiMessageEvent(status));
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
        Log.d(Constants.TAG_LOG, status);
    }

    public String getErrorStatusByCode(int code){
        String strError = "";
        switch(code){
            case Constants.ERROR:
                strError = "The operation failed due to an internal error";
                break;
            case Constants.P2P_UNSUPPORTED:
                strError = "The operation failed because p2p is unsupported on the device.";
                break;
            case Constants.BUSY:
                strError = "The operation failed because the framework is busy and unable to service the request";
                break;
            case Constants.NO_SERVICE_REQUESTS:
                strError = "No service requests are added.";
                break;
            default:
                strError = String.valueOf(code);
                break;
        }
        return strError;
    }

    public String getDeviceStatusByCode(int code){
        String strStatus = "";
        switch(code){
            case WifiP2pDevice.CONNECTED:
                strStatus = "CONNECTED";
                break;
            case WifiP2pDevice.INVITED:
                strStatus = "INVITED";
                break;
            case WifiP2pDevice.FAILED:
                strStatus = "FAILED";
                break;
            case WifiP2pDevice.AVAILABLE:
                strStatus = "AVAILABLE";
                break;
            case WifiP2pDevice.UNAVAILABLE:
                strStatus = "UNAVAILABLE";
                break;
            default:
                strStatus = String.valueOf(code);
                break;
        }
        return strStatus;
    }

    @Override
    public void onChannelDisconnected() {
        if (wifiDirectManager.getManager() != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData(false);
            retryChannel = true;
            wifiDirectManager.Init();
        } else {
            Toast.makeText(this,
                    "Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }


    private void forwardMessageToClients(String message) {
        for (MessageManager mgr : ConnectedClientManagers) {
            mgr.write(message.getBytes());
        }
    }

    @Override
    public void ForwardMessage(String msg) {
        forwardMessageToClients(msg);
    }
    private void SendMessageToServer(String message) {
        //if(messageFragmentSet)
        //    messageFragment.SendMessageToServer(message);

        HandleMessageToServer(message);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                DataList.add(readMessage);
                EventBus.getDefault().post(readMessage);
                //(messageFragment).pushMessage(readMessage);
                //forwardMessageToClients(readMessage);
                AppMessage tmp = new AppMessage();
                tmp.PopulateFromJSON(readMessage);

                if(tmp.msgTxt == null)
                    appendStatus("Message received: " + readMessage);
                else
                    appendStatus("Message received: " + tmp.msgTxt);

                break;

            case Constants.MY_HANDLE:
                _IsInitiated = true;
                IsDisconnected = false;
                Log.d(Constants.TAG_LOG, "MY_HANDLE");
                Object obj = msg.obj;
                messageManager = (MessageManager)obj;
                //(messageFragment).setMessageManager((MessageManager) obj);

            case Constants.HANDLER_TIMEOUT:
                Log.d(Constants.TAG_LOG, "HANDLER_TIMEOUT");
        }
        return true;
    }


    public void HandleBroadcastMessage(String msgText){
        AppMessage msg = new AppMessage();
        msg.DeviceID = "device id";
        msg.msgTxt = msgText;
        String dataLine = msg.ToJSONString();

        if (WifiGroupListing.IsServer) {
            WifiGroupListing.DataList.add(dataLine);
            if (messageManager != null) {
                ((MessageForwarder)curActivity).ForwardMessage(dataLine);
            }
        }
    };

    public void HandleMessageToServer(String msgText){
        AppMessage msg = new AppMessage();
        msg.DeviceID = "device id";
        msg.msgTxt = msgText;
        String dataLine = msg.ToJSONString();

        if (!WifiGroupListing.IsServer) {
            if (messageManager != null) {
                messageManager.write(dataLine.getBytes());
            }
        }
    }


    @Override
    public void OnChangeToSubview(int viewId) {
        if(viewId == Constants.PEERS_AVAILABLE){
        }
        if (viewId == Constants.ID_MAIN_PAGE) {
            if (_IsInitiated) {
                //messageFragmentSet = false;
                //Fragment frag = getFragmentManager().findFragmentByTag("messages");
                //if (frag != null) {
                //    getFragmentManager().beginTransaction().remove(frag).commit();
                //}
                resetData(false);
                Log.d(Constants.TAG_LOG, "Kill service");
                Disconnect(new WifiP2pManager.ActionListener() {
                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d(Constants.TAG_LOG, "Disconnect failed. Reason :" + getErrorStatusByCode(reasonCode));
                        appendStatus("Disconnect failed. Reason :" + getErrorStatusByCode(reasonCode));
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(Constants.TAG_LOG, "Succeeded disconnect");
                        appendStatus("Succeeded disconnect");
                    }
                });

                Log.d(Constants.TAG_LOG, "Restart discovery");
                appendStatus("Disconnected!");
                wifiDirectManager.Init();
            }
        }
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        appendStatus("Peers Available total: " + peerList.getDeviceList().size());
    }

    public void CreateClientSocket(String addr){
        Thread handler = null;
        InetAddress tmpAdd = null;
        try {
            tmpAdd = InetAddress.getByName(addr);

            handler = new ClientSocketHandler(this.getHandler(), tmpAdd, this);
            handler.start();

            //if (!messageFragmentSet) {
            //    messageFragment = new MessageFragment();
            //    messageFragment.MessageForwarder = this;
            //    getFragmentManager().beginTransaction().replace(R.id.container_root, messageFragment, "messages").commit();
                //getFragmentManager().beginTransaction().commit();
            //    messageFragmentSet = true;
            //}
            IsSocketConnected = true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
            appendStatus("error creating client socket ");
            IsSocketConnected = false;
            // TODO: retry to connect
        }
    }

    public void CreateServerSocket(){
        Thread handler = null;
        try {
            handler = new GroupOwnerSocketHandler(this.getHandler(), this);
            handler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //if (!messageFragmentSet) {
        //    messageFragment = new MessageFragment();
        //    messageFragment.MessageForwarder = this;
        //    getFragmentManager().beginTransaction().replace(R.id.container_root, messageFragment, "messages").commit();
            //getFragmentManager().beginTransaction().commit();
        //    messageFragmentSet = true;
        //}

    }


    public void CreateGroup(){
        if(canYouBeServer) {
                wifiDirectManager.RequestGroupInfo(new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if (group != null) {
                            if (!group.isGroupOwner()){
                                wifiDirectManager.RemoveGroup(new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(Constants.TAG_LOG, "Succeeded RemoveGroup");
                                        appendStatus("Succeeded RemoveGroup");
                                        GroupCreated = false;

                                        wifiDirectManager.CreateGroup(new WifiP2pManager.ActionListener() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d(Constants.TAG_LOG, "Succeeded CreateGroup");
                                                appendStatus("Succeeded CreateGroup");
                                                RequestGroupInfo(false);
                                            }

                                            @Override
                                            public void onFailure(int reason) {
                                                Log.d(Constants.TAG_LOG, "CreateGroup failed. Reason :" + getErrorStatusByCode(reason));
                                                appendStatus("CreateGroup failed. Reason :" + getErrorStatusByCode(reason));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d(Constants.TAG_LOG, "RemoveGroup failed. Reason :" + getErrorStatusByCode(reason));
                                        appendStatus("RemoveGroup failed. Reason :" + getErrorStatusByCode(reason));
                                    }
                                });
                            }else{
                                RequestGroupInfo(false);
                            }
                        } else {
                            wifiDirectManager.CreateGroup(new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Log.d(Constants.TAG_LOG, "Succeeded CreateGroup");
                                    appendStatus("Succeeded CreateGroup");
                                    RequestGroupInfo(false);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Log.d(Constants.TAG_LOG, "CreateGroup failed. Reason :" + getErrorStatusByCode(reason));
                                    appendStatus("CreateGroup failed. Reason :" + getErrorStatusByCode(reason));
                                }
                            });
                        }
                    }
                });
        }
    }

    public void RemoveGroup(){
        wifiDirectManager.RemoveGroup(new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                Log.d(Constants.TAG_LOG, "Succeeded RemoveGroup");
                appendStatus("Succeeded RemoveGroup");
                GroupCreated = false;
                IsConnected = false;
            }

            @Override
            public void onFailure(int reason) {
                Log.d(Constants.TAG_LOG, "RemoveGroup failed. Reason :" + getErrorStatusByCode(reason));
                appendStatus("RemoveGroup failed. Reason :" + getErrorStatusByCode(reason));
            }
        });
    }

    public void RequestGroupInfo(final boolean retry){
        wifiDirectManager.RequestGroupInfo(new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    wifiDirectManager.setSSID(group.getNetworkName());
                    wifiDirectManager.setPassPhrase(group.getPassphrase());
                    if(myDevice != null)
                        wifiDirectManager.setUserID(myDevice.UserID.toString());
                    appendStatus("SSID: " + group.getNetworkName());
                    appendStatus("PassPhrase: " + group.getPassphrase());
                    Log.w("Group Info", group.toString());
                    GroupCreated = true;
                    StartLocalService();
                }else{
                    appendStatus("Group not found");
                    if(!retry) {
                        appendStatus("Try again in 3 seconds");
                        requestGroupInfoHandler.postDelayed(requestGroupInfoRunnable, 3000);
                    }
                }
            }
        });
    }

    public void RequestConnectionInfo(){
        wifiDirectManager.RequestConnectionInfo(new WifiP2pManager.ConnectionInfoListener(){

            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info != null){
                    appendStatus("Connection info: " + info.toString());
                }else{
                    appendStatus("Connection info problem");
                }
            }
        });
    }

    @Override
    public void SendMessage(String msg) {
        appendStatus(msg);
    }

    @Override
    public void SetServerIpAddress(String addr){
        CreateClientSocket(addr);
    }

    @Override
    public void ConnectToSocket(){
        if(curRecord != null)
            CreateClientSocket(curRecord.getServerIp());
    }

    @Override
    public void GetMyDeviceName(String deviceName) {
        if(myDevice == null) {
            for (int i = 0; i < fixedUsers.size(); i++) {
                Tuple tmp = fixedUsers.get(i);
                if (deviceName.equals(tmp.DeviceName)) {
                    myDevice = tmp;
                    break;
                }
            }
        }
    }

    @Override
    public void GetSocketStatus(boolean isConnected){
        if(!isConnected)
            IsConnected = false;
    }
}
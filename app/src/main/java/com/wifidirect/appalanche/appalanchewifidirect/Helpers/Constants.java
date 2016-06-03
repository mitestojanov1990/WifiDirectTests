package com.wifidirect.appalanche.appalanchewifidirect.Helpers;

public class Constants {
    public static int ID_MAIN_PAGE = 0;
    public static int ID_DEVICE_LIST_PAGE = 1;
    public static int ID_RECONNECT = 2;
    public static int DISCOVERY_START = 3;
    public static int PEERS_AVAILABLE = 4;

    public static final String DEVNAME_FILENAME = "/sdcard/appalanchedev.txt";
    public static final String DATA_FILENAME = "/sdcard/appalanche-data.txt";

    public static final int SERVER_PORT = 4545;
    public static final String SERVER_ADDR = "192.168.49.171";

    public static final int UPDATE_INTERVAL = 1500;
    public static final int SERVICE_BROADCASTING_INTERVAL = 10000;
    public static final int SERVICE_DISCOVERING_INTERVAL = 10000;

    public static final String TAG_LOG = "appalanche";

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_appalanche";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final String MY_INFO = "my_info";

    // Messages
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int HANDLER_TIMEOUT = 0x400 + 3;

    // Statuses
    public static final int INIT = -1;
    public static final int STARTED = 0;
    public static final int STOPPED = 1;
    public static final int FOUND = 2;
    public static final int CONNECTED = 3;

    // WifiP2pManager Connect Errors
    public static final int ERROR = 0;
    public static final int P2P_UNSUPPORTED = 1;
    public static final int BUSY = 2;
    public static final int NO_SERVICE_REQUESTS = 3;
}

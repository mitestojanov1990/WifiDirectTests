package com.wifidirect.appalanche.appalanchewifidirect.Helpers;

import android.os.Handler;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Interfaces.WifiGroupManagerListener;
import com.wifidirect.appalanche.appalanchewifidirect.MessageManager;
import com.wifidirect.appalanche.appalanchewifidirect.WifiGroupManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

    private Handler handler;
    private MessageManager _messageHandler;
    private InetAddress _address;
    static private WifiGroupManager activity;

    Thread t = null;

    private boolean isAlive = true;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, WifiGroupManager activity) {
        this.handler = handler;
        this._address = groupOwnerAddress;
        this.activity = activity;
    }

    public boolean IsSocketConnected(){
        return t.isAlive();
    }

    public void CheckConnection(){
        while(true){
            if(!IsSocketConnected()){
                ((WifiGroupManagerListener)activity).GetSocketStatus(false);
                break;
            }
        }
    }

    @Override
    public void run() {
        //while(isAlive) {
            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(_address.getHostAddress(), Constants.SERVER_PORT), 5000);
                Log.d(Constants.TAG_LOG, "Launching the I/O handler. host: " + _address.getHostAddress().toString());

                SendStatusMessage("Socket connected");
                _messageHandler = new MessageManager(socket, handler);
                t = new Thread(_messageHandler);
                t.start();
                CheckConnection();
                t.interrupt();
                t.join();

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("CSH run", e.getMessage());
                SendStatusMessage("Socket failed :" + e.getMessage());
                try {
                    socket.close();
                    activity.IsSocketConnected = false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.i("CSH close run", e.getMessage());
                    SendStatusMessage("Socket closing failed :" + e1.getMessage());
                }
                if(e.getMessage().contains("ENETUNREACH")){
                    ((WifiGroupManagerListener)activity).SocketProblemDisconnect(false);
                }
                if(e.getMessage().contains("ECONNREFUSED")){
                    ((WifiGroupManagerListener)activity).SocketProblemDisconnect(false);
                }
                return;
            }
            catch (InterruptedException e) {
                Log.i("CSH InterruptedEx", e.getMessage());
                e.printStackTrace();
            }
//            try {
//                sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                Log.i("Client Socket", e.toString());
//                return;
//            }
        //}
    }

    private void SendStatusMessage(final String msg){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.appendStatus(msg);
            }
        });
    }
}

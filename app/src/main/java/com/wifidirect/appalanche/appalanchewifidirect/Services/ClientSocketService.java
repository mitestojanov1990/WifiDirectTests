package com.wifidirect.appalanche.appalanchewifidirect.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Events.ServerIpEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.SocketProblemEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.SocketStatusEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.WifiMessageEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.LooperThread;
import com.wifidirect.appalanche.appalanchewifidirect.MessageManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocketService extends Service {
    PrintWriter out;
    Socket socket;
    private InetAddress _address;

    Thread t = null;
    LooperThread looperThread = null;

    private boolean isAlive = true;
    private MessageManager _messageHandler;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ClientSocketService getService() {
            System.out.println("I am in Localbinder ");
            return ClientSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("I am in on create");
    }

    public void IsBoundable() {
        System.out.println("I am in on create");
        //Toast.makeText(this,"I bind like butter", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();

        looperThread = new LooperThread();

        ServerIpEvent stickyEvent = EventBus.getDefault().removeStickyEvent(ServerIpEvent.class);
        if(stickyEvent != null) {
            // Now do something with it
            try {
                _address = InetAddress.getByName(stickyEvent.getIpAddress());
                InitializeSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return START_STICKY;
    }

    private void InitializeSocket(){
        Runnable connect = new connectSocket();
        new Thread(connect).start();
    }

    public boolean IsSocketConnected(){
        return t.isAlive();
    }
    public void CheckConnection(){
        while(true){
            if(!IsSocketConnected()){
                EventBus.getDefault().post(new SocketStatusEvent(false));
                break;
            }
        }
    }
    class connectSocket implements Runnable {
        @Override
        public void run() {
            Looper.prepare();
            //while(isAlive) {
            Socket socket = new Socket();
            try {
                socket.bind(null);
                socket.connect(new InetSocketAddress(_address.getHostAddress(), Constants.SERVER_PORT), 5000);
                Log.d(Constants.TAG_LOG, "Launching the I/O handler. host: " + _address.getHostAddress().toString());

                SendStatusMessage("Socket connected");
                _messageHandler = new MessageManager(socket, looperThread.getHandler());

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
                    EventBus.getDefault().post(new SocketStatusEvent(false));
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.i("CSH close run", e.getMessage());
                    SendStatusMessage("Socket closing failed :" + e1.getMessage());
                }
                if(e.getMessage().contains("ENETUNREACH")){
                    EventBus.getDefault().post(new SocketProblemEvent(false));
                }
                if(e.getMessage().contains("ECONNREFUSED")){
                    EventBus.getDefault().post(new SocketProblemEvent(false));
                }
                return;
            }
            catch (InterruptedException e) {
                Log.i("CSH InterruptedEx", e.getMessage());
                e.printStackTrace();
                try {
                    socket.close();
                    EventBus.getDefault().post(new SocketStatusEvent(false));
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.i("CSH close run", e.getMessage());
                    SendStatusMessage("Socket closing failed :" + e1.getMessage());
                }
            }

        }
    }

    private void SendStatusMessage(final String msg){
        EventBus.getDefault().post(new WifiMessageEvent(msg));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //TODO: see how to stop handler
        try {
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }

}

package com.wifidirect.appalanche.appalanchewifidirect.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Events.BroadcastMessageEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.ConnectedClientEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.WifiMessageEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;
import com.wifidirect.appalanche.appalanchewifidirect.Helpers.LooperThread;
import com.wifidirect.appalanche.appalanchewifidirect.MessageManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerSocketService extends Service {
    PrintWriter out;

    static ServerSocket socket = null;
    private final int THREAD_COUNT = 10;

    Thread serverThread = null;
    LooperThread looperThread = null;


    Handler serverHandler = null;

    public static ArrayList<MessageManager> ConnectedClientManagers = new ArrayList<MessageManager>(); // holds all message managers (connected clients)

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ServerSocketService getService() {
            System.out.println("I am in Localbinder ");
            return ServerSocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        System.out.println("I am in on create");
    }

    public void IsBoundable(){
        System.out.println("I am in on create");
        //Toast.makeText(this,"I bind like butter", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();

        looperThread = new LooperThread();
        looperThread.start();

        InitializeSocket();

        return START_STICKY;
    }


    private void InitializeSocket(){
        Runnable connect = new connectSocket();
        serverThread = new Thread(connect);
        serverThread.start();
    }

    private void createServerSocket(){
        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            Log.d(Constants.TAG_LOG, "Server Socket Started");
            SendStatusMessage("Server Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            SendStatusMessage("Server Socket failed :" + e.toString());
            Log.i("GroupOwnerSocketHandler", e.toString());
            pool.shutdownNow();
            try {
                throw e;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    ///
    /// A ThreadPool for client sockets.
    ///
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    class connectSocket implements Runnable {
        @Override
        public void run() {
            createServerSocket();
            while (true) {
                try {
                    // A blocking operation. Initiate a MessageManager instance when there is a new connection
                    Log.d(Constants.TAG_LOG, "Before create MessageManager");
                    //if(!activity.IsServer) {
                    if(!socket.isClosed()) {
                        Socket tmp = socket.accept();
                        MessageManager mgr = new MessageManager(tmp, looperThread.getHandler());
                        EventBus.getDefault().post(new ConnectedClientEvent(mgr));
                        ConnectedClientManagers.add(mgr);
                        pool.execute(mgr);

                        //looperThread.getHandler().sen
                        //}
                        Log.d(Constants.TAG_LOG, "Launching the I/O handler (server)");
                        SendStatusMessage("Launching the I/O handler (server)");
                    }
                }catch(ConnectException exc){
                    SendStatusMessage("MessageManager socket error :" + exc.toString());
                }
                catch(SocketException ex){
                    SendStatusMessage("MessageManager socket error :" + ex.toString());
                }
                catch (IOException e) {

                    Log.i("GOS IOException", e.toString());
                    SendStatusMessage("MessageManager socket error :" + e.toString());
                    try {
                        if (socket != null && !socket.isClosed())
                            socket.close();
                    } catch (IOException ioe) {
                        Log.i("GOSClose IOException", ioe.toString());
                        SendStatusMessage("Server Socket close failed :" + ioe.toString());
                    }
                    e.printStackTrace();
                    pool.shutdownNow();
                    break;
                }
            }
        }
    }

    private void SendStatusMessage(String msg){
        EventBus.getDefault().post(new WifiMessageEvent(msg));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //TODO: see how to stop handler
        //serverHandler.remove
        serverThread.interrupt();
        try {
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }

    @Subscribe
    public void onEventBackgroundThread(BroadcastMessageEvent event){
        for (MessageManager mgr : ConnectedClientManagers) {
            String msg = event.getMessage().ToJSONString();
            mgr.write(msg.getBytes());
        }
    }

}

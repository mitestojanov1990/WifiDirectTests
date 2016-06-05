package com.wifidirect.appalanche.appalanchewifidirect.Helpers;

import android.os.Handler;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.MessageManager;
import com.wifidirect.appalanche.appalanchewifidirect.WifiGroupManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GroupOwnerSocketHandler extends Thread {

    static ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private Handler handler;
    private static WifiGroupManager activity;

    private SocketAddress myIP;

    public GroupOwnerSocketHandler(Handler handler, WifiGroupManager activity) throws IOException {

        try {
            socket = new ServerSocket(Constants.SERVER_PORT);
            myIP = socket.getLocalSocketAddress();
            this.handler = handler;
            this.activity = activity;
            Log.d(Constants.TAG_LOG, "Server Socket Started");
            SendStatusMessage("Server Socket Started");
            //socket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();
            SendStatusMessage("Server Socket failed :" + e.toString());
            Log.i("GroupOwnerSocketHandler", e.toString());
            pool.shutdownNow();
            throw e;
        }

    }

    public static void CloseSocket(){
        if(socket != null && socket.isClosed() == false){
            try {
                socket.close();
                SendStatusMessage("Server Socket closed");
            } catch (IOException e) {
                SendStatusMessage("Server Socket close failed :" + e.toString());
                Log.i("GOSClose CloseSocket", e.toString());
                e.printStackTrace();
            }
        }
    }

    ///
    /// A ThreadPool for client sockets.
    ///
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a MessageManager instance when there is a new connection
                Log.d(Constants.TAG_LOG, "Before create MessageManager");
                //if(!activity.IsServer) {
                    Socket tmp = socket.accept();
                    MessageManager mgr = new MessageManager(tmp, handler);
                    WifiGroupManager.ConnectedClientManagers.add(mgr);
                    pool.execute(mgr);
                //}
                Log.d(Constants.TAG_LOG, "Launching the I/O handler (server)");
                SendStatusMessage("Launching the I/O handler (server)");
            } catch (IOException e) {

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


    private static void SendStatusMessage(final String msg){
        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.appendStatus(msg);
                }
            });
        }
    }

}

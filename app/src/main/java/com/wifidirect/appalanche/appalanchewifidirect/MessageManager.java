package com.wifidirect.appalanche.appalanchewifidirect;

import android.os.Handler;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

///
/// Handles reading and writing of messages with socket buffers. Uses a Handler
/// to post messages to UI thread for UI updates.
///
public class MessageManager implements Runnable {

    private Socket socket = null;
    private Handler _handler;
    private Boolean _threadIsAlive = true;

    public MessageManager(Socket socket, Handler handler) {
        this.socket = socket;
        this._handler = handler;

        // register client connections on server
        /*
        if (MainActivity.IsServer) {
            String devIp = socket.getInetAddress().toString();
            MainActivity.ConnectedDevices.add(devIp.replace("/", ""));
            for(String ip : MainActivity.ConnectedDevices) {
                Log.d(Constants.TAG_LOG, "Registered client: " + ip);
            }
        }*/
        Log.d(Constants.TAG_LOG, "New MessageManager. Host:" + socket.getInetAddress().toString());
    }

    private InputStream iStream;
    private OutputStream oStream;

    public Socket getSocket(){
        return this.socket;
    }
    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            _handler.obtainMessage(Constants.MY_HANDLE, this).sendToTarget();

            while (_threadIsAlive) {
                bytes = iStream.read(buffer);
                if (bytes == -1) {
                    break;
                }

                _handler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            }
        } catch (IOException e) {
            Log.i("IOException", e.toString());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                Log.i("FinallyIOException", e.toString());
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            oStream.write(buffer);
        } catch (IOException e) {
            Log.i("MM write", e.getMessage());
            Log.e(Constants.TAG_LOG, "Exception during write", e);
        }
    }
}

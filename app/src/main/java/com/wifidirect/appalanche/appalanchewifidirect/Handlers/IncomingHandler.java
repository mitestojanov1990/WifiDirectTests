package com.wifidirect.appalanche.appalanchewifidirect.Handlers;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wifidirect.appalanche.appalanchewifidirect.Helpers.Constants;
import com.wifidirect.appalanche.appalanchewifidirect.Models.AppMessage;

public class IncomingHandler extends Handler {



    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                AppMessage tmp = new AppMessage();
                tmp.PopulateFromJSON(readMessage);

//                    if(tmp.msgTxt == null)
//                        appendStatus("Message received: " + readMessage);
//                    else
//                        appendStatus("Message received: " + tmp.msgTxt);

                break;

            case Constants.MY_HANDLE:
                //_IsInitiated = true;
                //IsDisconnected = false;
                Log.d(Constants.TAG_LOG, "MY_HANDLE");
                Object obj = msg.obj;
                //messageManager = (MessageManager)obj;
                //(messageFragment).setMessageManager((MessageManager) obj);

            case Constants.HANDLER_TIMEOUT:
                Log.d(Constants.TAG_LOG, "HANDLER_TIMEOUT");
        }
    }
}
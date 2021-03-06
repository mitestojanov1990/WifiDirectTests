package com.wifidirect.appalanche.appalanchewifidirect.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wifidirect.appalanche.appalanchewifidirect.Events.ServerIpEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Events.WifiServiceTxtRecordEvent;
import com.wifidirect.appalanche.appalanchewifidirect.Models.WifiServiceTxtRecord;
import com.wifidirect.appalanche.appalanchewifidirect.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class ServiceTxtRecordAdapter extends RecyclerView.Adapter<ServiceTxtRecordAdapter.MyViewHolder> {

    private ArrayList<WifiServiceTxtRecord> userGroupDataSet;

    private EventBus eventBus;

    private ServiceTxtRecordAdapter instance;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        int Holderid;

        TextView TxtRecord;
        TextView SSID;
        TextView PassPhrase;
        String ServerIP;
        Button connect;
        Button connectSocket;

        public MyViewHolder(View itemView,int ViewType) {
            super(itemView);
            this.TxtRecord = (TextView) itemView.findViewById(R.id.TxtRecord);
            this.SSID = (TextView) itemView.findViewById(R.id.SSID);
            this.PassPhrase = (TextView) itemView.findViewById(R.id.PassPhrase);
            this.connect = (Button) itemView.findViewById(R.id.connect);
            this.connectSocket  = (Button) itemView.findViewById(R.id.connectSocket);
        }
    }

    public ServiceTxtRecordAdapter(ArrayList<WifiServiceTxtRecord> userGroups, EventBus eventBus) {
        this.userGroupDataSet = userGroups;
        this.eventBus = eventBus;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_row, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view, viewType);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        TextView TxtRecord = holder.TxtRecord;
        TextView SSID = holder.SSID;
        TextView PassPhrase = holder.PassPhrase;
        Button connect = holder.connect;
        Button connectSocket = holder.connectSocket;

        TxtRecord.setText(userGroupDataSet.get(listPosition).getTxtRecord());
        SSID.setText(userGroupDataSet.get(listPosition).getSSID());
        PassPhrase.setText(userGroupDataSet.get(listPosition).getPassPhrase());
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userGroupDataSet.size() > 0) {
                    WifiServiceTxtRecord tmp = userGroupDataSet.get(listPosition);

                    //WifiGroupManager.WifiTxtRecordOnClick(tmp);
                    eventBus.post(new WifiServiceTxtRecordEvent(tmp));
                    //tmp.setIsConnected(true);
                    notifyDataSetChanged();
                }
            }
        });
        connectSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userGroupDataSet.size() > 0) {
                    WifiServiceTxtRecord tmp = userGroupDataSet.get(listPosition);
                    //WifiGroupManager.onConnectToSocket(tmp.getServerIp());

                    eventBus.postSticky(new ServerIpEvent(tmp.getServerIp()));
                    //eventBus.post(new ServerIpEvent(tmp.getServerIp()));
                    //tmp.setIsConnected(true);
                    notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return userGroupDataSet.size();
    }
}
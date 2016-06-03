package com.wifidirect.appalanche.appalanchewifidirect.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wifidirect.appalanche.appalanchewifidirect.R;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<String> {

    Activity _activity;
    List<String> _messages = null;

    public MessageAdapter(Context context, int textViewResourceId, List<String> items, Activity activity) {
        super(context, textViewResourceId, items);
        this._messages = items;
        this._activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(android.R.layout.simple_list_item_1, null);
        }
        String message = _messages.get(position);
        if (message != null && !message.isEmpty()) {
            TextView nameText = (TextView) v.findViewById(android.R.id.text1);

            if (nameText != null) {
                nameText.setText(message);
                if (message.startsWith("Me: ")) {
                    nameText.setTextAppearance(_activity, R.style.normalText);
                } else {
                    nameText.setTextAppearance(_activity, R.style.boldText);
                }
            }
        }
        return v;
    }
}

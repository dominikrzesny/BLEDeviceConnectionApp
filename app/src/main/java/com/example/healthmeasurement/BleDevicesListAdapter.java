package com.example.healthmeasurement;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;



public class BleDevicesListAdapter extends BaseAdapter {

    private Context context;
    public ArrayList<BluetoothDevice> leDevices = new ArrayList<>();

    public BleDevicesListAdapter(Context context){
        this.context = context;
    }

    public void addDevice(BluetoothDevice device){
        if(leDevices.contains(device)){
            leDevices.add(device);
        }
    }

    public void clear(){
        if(!leDevices.isEmpty())
            leDevices.clear();
    }

    @Override
     public int getCount(){
         return leDevices.size();
     }

    @Override
    public Object getItem(int position) {
        return leDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView  = inflater.inflate(R.layout.le_devices_list_view,null,true);
        TextView titleText = rowView.findViewById(R.id.title);
        TextView subtitleText = rowView.findViewById(R.id.description);

        titleText.setText(leDevices.get(position).getName());
        subtitleText.setText(leDevices.get(position).getAddress());
        return rowView;
    }


}

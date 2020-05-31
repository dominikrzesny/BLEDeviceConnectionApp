package com.example.healthmeasurement;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.widget.Button;
import android.os.Handler;


public class ScanLeDevices extends ListActivity {

    public ScanLeDevices(BluetoothAdapter bluetoothAdapter, Handler handler){
        this.bluetoothAdapter = bluetoothAdapter;
        this.handler= handler;
    }

    private static final long SCAN_PERIOD = 5000;
    private BluetoothAdapter bluetoothAdapter;
    public Handler handler;
    public boolean mScanning = false;


    void scanLeDevice(boolean enable, Button button, final ScanCallback mScanCallback){
        if(enable){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("elo po delayu");
                    mScanning = false;
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        }
        else{
            mScanning = true;
            bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
    }

    public void stopScanLeDevice(final ScanCallback mScanCallback){
        bluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        mScanning=false;
    }

}

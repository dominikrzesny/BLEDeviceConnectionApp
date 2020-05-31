package com.example.healthmeasurement;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BleDevicesListAdapter leDevicesListAdapter = new BleDevicesListAdapter(this);
    private ScanLeDevices bleScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            System.out.println("BLE// onScanResults:");
            System.out.println(result.getRssi());
            BluetoothDevice searchedDevice = result.getDevice();

            if(!leDevicesListAdapter.leDevices.contains(searchedDevice)){
                leDevicesListAdapter.leDevices.add(searchedDevice);
            }

            ListView listView = findViewById(R.id.listView1);
            listView.setAdapter(leDevicesListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    BluetoothDevice chosenDevice = (BluetoothDevice) leDevicesListAdapter.getItem(position);
                    System.out.println("Po kliknieciu na liste. ID:" + parent.getItemIdAtPosition(position));
                    System.out.println("Nazwa urzadzenia wybranego:" + chosenDevice.getName());
                    final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, chosenDevice.getName());
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, chosenDevice.getAddress());

                    if (bleScanner.mScanning) {
                        bleScanner.stopScanLeDevice(mScanCallback);
                    }

                    startActivity(intent);
                    //Intent serviceIntent = Intent(this,Blue)

                }
            });

        }

        @Override
        public void onScanFailed(int errorCode) {
            System.out.println("BLE// onScanFailed");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            System.out.println("BLE// onBatchScanResults");
        }

    };



    public void onClickMethod(View view){
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else
            {
                // Permission already has been granted
        }


        final Button button = findViewById(R.id.button1);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        if(bluetoothAdapter.isEnabled()==true){
            bleScanner = new ScanLeDevices(bluetoothAdapter, new Handler());
            leDevicesListAdapter.clear();
            leDevicesListAdapter.notifyDataSetChanged();
            bleScanner.scanLeDevice(true,button,mScanCallback);
            button.setText("Wyszukiwanie");
            Toast.makeText(this, "Wyszukiwanie... Czekaj na zakończenie", Toast.LENGTH_LONG).show();
        }
        else{
            return;
        }

    }

}

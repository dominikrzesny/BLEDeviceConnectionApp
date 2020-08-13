package com.example.healthmeasurement;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceControlActivity extends Activity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public boolean onCreate;
    private BluetoothLEService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView mConnectionState;
    private TextView pulse;
    private GraphView graph;
    private BluetoothGattCharacteristic blunoCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private int i;
    private ArrayList<DataPoint> pointsArray= new ArrayList(){};
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLEService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate=true;
        setContentView(R.layout.activity_device_control);
        mConnectionState = findViewById(R.id.mConnectionState);
        pulse = findViewById(R.id.myImageViewText);
        graph = findViewById(R.id.graph);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE );
        graph.getGridLabelRenderer().setGridColor(Color.RED);
        graph.getViewport().setDrawBorder(true);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().reloadStyles();
        graph.getViewport().setMinY(20);
        graph.getViewport().setMaxY(200);
        graph.getViewport().setYAxisBoundsManual(true);
        //graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value,boolean isValueX){
                if(isValueX){
                    System.out.println("DATA!!!!!");
                    System.out.println(value);
                    return sdf.format(new Date((long) value));
                }
                else{
                    return super.formatLabel(value,isValueX);
                }
            }
        });
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);
        //graph.getViewport().setMaxX(new Date().getTime());

        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
        registerReceiver(connectionStateReceiver,makeStateConnectionUpdateIntentFilter());

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        registerReceiver(connectionStateReceiver,makeStateConnectionUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        if(onCreate){
            onCreate=false;
            return;
        }
        drawGraph();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(connectionStateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void displayData(String data) {
        if (data != null) {
            pulse.setText(data);
        }
    }

    public void clicknij(View view){
        mBluetoothLeService.load();
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private static IntentFilter makeStateConnectionUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mConnectionState.setText(" Connected");

            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                pulse.setText("0");
                mConnectionState.setText(" Disconnected");

            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
                for (BluetoothGattService gattService : gattServices) {
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        String uuid = gattCharacteristic.getUuid().toString();
                        System.out.println(uuid);
                        if(uuid.equals(SampleGattAttributes.BLUNO)){
                            System.out.println("Pobrało cos");
                            blunoCharacteristic = gattService.getCharacteristic(mBluetoothLeService.UUID_HEART_RATE_MEASUREMENT);
                        }
                    }
                }

                mBluetoothLeService.setCharacteristicNotification(blunoCharacteristic,true);
                mBluetoothLeService.readCharacteristic(blunoCharacteristic);


            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String data = intent.getStringExtra(BluetoothLEService.EXTRA_DATA);
                if(data.length()>1){
                    displayData(data);
                    drawGraph();
                }
            }
        }
    };

    private final BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras() != null) {
                final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                if (ni != null && ni.isConnectedOrConnecting()) {
                    Log.i(TAG, "Network " + ni.getTypeName() + " connected");

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothLeService.sendPulseToServer("");
                            //mBluetoothLeService.sendAllOldMeasurements();
                        }
                    }, 100);


                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    Log.d(TAG, "There's no network connectivity");
                }
            }

        }
    };

    public void drawGraph(){

        pointsArray = mBluetoothLeService.getPointsArray();
        DataPoint[] dataPoints = new DataPoint[pointsArray.size()];
        for(int z =0;z<pointsArray.size();z++){
            dataPoints[z]=pointsArray.get(z);
        }
        LineGraphSeries<DataPoint> pa = new LineGraphSeries<>(dataPoints);
        pa.setColor(Color.RED);
        //graph.getViewport().setMaxX(new Date().getTime());
        graph.addSeries(pa);


    }

}




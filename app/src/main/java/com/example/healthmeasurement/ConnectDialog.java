package com.example.healthmeasurement;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

public class ConnectDialog extends AppCompatDialogFragment {

    private EditText ipText;
    private EditText portText;
    BluetoothLEService mBluetoothLeService;

    ConnectDialog(BluetoothLEService mBluetoothLeService){
        this.mBluetoothLeService = mBluetoothLeService;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.mosquito_connect_dialog,null);
        ipText = view.findViewById(R.id.ipv4_address);
        portText = view.findViewById(R.id.port);
        builder.setView(view)
                .setTitle("Connect")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                    }
                }).setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (ipText.getText().toString().matches("") || portText.getText().toString().matches("") ) {
                    Toast.makeText(getContext(), "You did not enter a username", Toast.LENGTH_SHORT).show();
                return;
                }

                if(mBluetoothLeService.isMqttServerConnected==false){

                    mBluetoothLeService.connectToMqttServer(ipText.getText().toString(),portText.getText().toString());
                }
            }
        });


        return builder.create();
    }
}
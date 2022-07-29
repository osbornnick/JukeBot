package com.osbornnick.jukebot1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTooth;
    TextView mPairedDevices;
    Button mbtnOn, mbtnOff, mbtnDiscover, mbtnPaired;

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        mStatusBlueTooth = findViewById(R.id.txt_bluetoothstatus);
        mPairedDevices = findViewById(R.id.txt_pairdDevices);
        mbtnOn = findViewById(R.id.btn_on);
        mbtnOff = findViewById(R.id.btn_off);
        mbtnPaired = findViewById(R.id.btn_pairdDevices);
        mbtnDiscover = findViewById(R.id.btn_discoverable);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            mStatusBlueTooth.setText("Bluetooth is not available");
        } else {
            mStatusBlueTooth.setText("Bluetooth is available");
        }
        Log.d(TAG, "onCreate: " + mBluetoothAdapter);

        mbtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Toast.makeText(BluetoothActivity.this, "Turning on Bluetooth...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //openBluetoothActivityForResult(intent);

                } else {
                    Toast.makeText(BluetoothActivity.this, "Bluetooth is already on", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mbtnOff.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    Log.d(TAG, "onClick: btnoff turning off");
                    Toast.makeText(BluetoothActivity.this, "Turning Bluetooth off", Toast.LENGTH_SHORT).show();

                }
            }
        });

        mbtnDiscover.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isDiscovering()) {
                    Log.d(TAG, "onClick: discovering");
                    Toast.makeText(BluetoothActivity.this, "Making your device discoverable", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }

            }
        });

        mbtnPaired.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mPairedDevices.setText("Paired Devices");
//                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
                    @SuppressLint("MissingPermission") Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        mPairedDevices.append("\nDevice" + device.getName() + "," + device);
                    }
                } else {
                    Toast.makeText(BluetoothActivity.this, "Please turn on bluetooth to get paired devices", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK){
                    Toast.makeText(BluetoothActivity.this, "Bluetooth is on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BluetoothActivity.this, "Couldn't turn on bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openBluetoothActivityForResult(Intent intent){
        BluetoothActivityResultLauncher.launch(intent);
    }


    ActivityResultLauncher<Intent> BluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                    }
                }
            });

}
package com.osbornnick.jukebot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WiFiActivity extends AppCompatActivity {

    private static final String TAG = "WiFiActivity";
    Button btnDiscover, btnWiFi;
    TextView txtStatus;
    ListView listView;

    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi);

        btnDiscover = findViewById(R.id.btn_discover);
        btnWiFi = findViewById(R.id.btn_wifi);
        txtStatus = findViewById(R.id.txt_connection_status);
        listView = findViewById(R.id.list_wifi);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager, mChannel, this);

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        listener();
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers)){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    Log.d(TAG, "onPeersAvailable: " + deviceNameArray[index]);
                    deviceArray[index] = device;
                }
                String[] test = {"test"};

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(WiFiActivity.this, android.R.layout.simple_list_item_1, test);
                listView.setAdapter(adapter);


                if (peers.size() == 0){
                    txtStatus.setText("No device found");
                    return;
                }
            }
        }
    };

    public void listener(){
        btnWiFi.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(intent, 1);
        });

        btnDiscover.setOnClickListener(v -> mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                txtStatus.setText("Discovery started");
            }

            @Override
            public void onFailure(int i) {
                txtStatus.setText("Discovery not started");
            }
        }));


    }


    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}
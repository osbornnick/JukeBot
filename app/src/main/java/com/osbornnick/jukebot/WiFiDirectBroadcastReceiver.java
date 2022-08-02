package com.osbornnick.jukebot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mWifiP2pManager, WifiP2pManager.Channel mChannel, WiFiActivity mActivity) {
        this.mWifiP2pManager = mWifiP2pManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // check if wifi is enabled
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){

            // requestpeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if (mWifiP2pManager != null){
                mWifiP2pManager.requestPeers(mChannel, mActivity.peerListListener);
            }
            // responds to new connection or disconneciton
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        }
    }
}

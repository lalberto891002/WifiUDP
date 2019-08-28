package com.dominando.android.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.nio.channels.Channel;
import java.util.ArrayList;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;


    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            Log.d("p2p", "Action: " + action);

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.d("p2p", "Requesting for peers");

            if (mManager != null) {
                mManager.requestPeers((WifiP2pManager.Channel) mChannel, myPeerListListener);
            }



            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device'cliente_udp wifi state changing
        }
    }

    private  WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(final WifiP2pDeviceList peers) {

            mActivity.conecciones=new ArrayList<String>();
            for(WifiP2pDevice peer:peers.getDeviceList())
            {
                mActivity.conecciones.add(peer.deviceName);



            }

            mActivity.miAdapter = new ArrayAdapter<String>(mActivity,R.layout.support_simple_spinner_dropdown_item,mActivity.conecciones);
            mActivity.spinner.setAdapter(mActivity.miAdapter);
            mActivity.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if(selectedItem.equals("Add new category"))
                    {
                        // do your stuff
                    }

                    final WifiP2pDevice device = peers.get(mActivity.miAdapter.getItem(position));
                    final WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    mManager.connect((WifiP2pManager.Channel) mChannel, config, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            //success logic
                            Toast.makeText(mActivity,"conectado con exito a"+config.deviceAddress,Toast.LENGTH_LONG);
                        }

                        @Override
                        public void onFailure(int reason) {
                            //failure logic
                            Toast.makeText(mActivity,"Error conectando a"+config.deviceAddress,Toast.LENGTH_LONG);
                        }
                    });



                } // to close the onItemSelected
                public void onNothingSelected(AdapterView<?> parent)
                {

                }
            });

        }
    };
}

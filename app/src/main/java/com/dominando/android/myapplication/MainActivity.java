package com.dominando.android.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button buscar,buscarArquivo,enviarArquivo;
    public Spinner spinner;
    WifiP2pManager  manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    public ArrayList<String> conecciones;
    private WifiManager mainWifiObj ;
    public ArrayAdapter<String> miAdapter;
    scanReceiver wifiReciever;
    BroadcastReceiver wifiScanReceiver;
    List<ScanResult> wifiScanList;
    public static boolean conectado;
    ArrayList<String> lista;
    public static boolean SEND;
    tarea hilo;
    Thread udpConnect;
    static boolean FREE;
    Runnable thread;
    Handler handler;
    String path;
    File arq;
    StringBuilder textoEnviar;
    public ProgramarWifi programarWifi;
    BufferedReader br;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainWifiObj =(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        buscar=findViewById(R.id.button);
        buscarArquivo=findViewById(R.id.button2);
        enviarArquivo=findViewById(R.id.button3);
        spinner=findViewById(R.id.spinner);
        manager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this,getMainLooper(),null);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiReciever=new scanReceiver();
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean success = mainWifiObj.startScan();
                if (!success) {
                    // scan failure handling
                    scanFailure();
                }
                else
                    scanSuccess();

            }
        });
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };
        FREE=true;

        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        boolean success = mainWifiObj.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
        else
            scanSuccess();
        enviarArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                programarWifi=new ProgramarWifi(Environment.getExternalStorageDirectory().toString());
                SEND=true;
                if(handler!=null)
                    handler.removeCallbacks(thread);
                if(FREE) {
                    hilo = new tarea(3333, "192.168.0.2", MainActivity.this);
                    hilo.execute();
                }


                if(handler==null) {
                    handler = new Handler();
                    thread = new Runnable() {
                        @Override
                        public void run() {
                            SEND = true;
                            hilo = new tarea(3333, "192.168.0.2", MainActivity.this);
                            hilo.execute();

                            handler.postDelayed(this, 500);
                        }
                    };
                }
                handler.postDelayed(thread,3000);
            }
        });
       /*udpConnect = new Thread(new ClientListen(MainActivity.this));
        udpConnect.start();*/

    buscarArquivo.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            path= Environment.getExternalStorageDirectory().getAbsolutePath();

        }
    });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
    }


    private void scanSuccess() {
        if(MainActivity.conectado)
            return;

        wifiScanList = mainWifiObj.getScanResults();
        lista = new ArrayList<>();
        for (ScanResult result : wifiScanList) {
                if(result.toString().contains("SN-"))
                    lista.add(result.SSID.toString());
        }
        //String data = wifiScanList.get(0).toString();
        miAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, lista);
        spinner.setAdapter(miAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String result=lista.get(position);
                String networkSSID = result;
                String networkPass = "152152153";
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + networkSSID + "\"";
                conf.preSharedKey = "\""+ networkPass +"\"";
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                mainWifiObj.addNetwork(conf);
                List<WifiConfiguration> list = mainWifiObj.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.toString().equals('"'+networkSSID+'"')) {
                        mainWifiObj.disconnect();
                        mainWifiObj.enableNetwork(i.networkId, true);
                        mainWifiObj.reconnect();
                        MainActivity.conectado=true;
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
    }
}



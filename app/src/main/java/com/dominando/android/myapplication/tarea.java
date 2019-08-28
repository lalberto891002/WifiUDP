package com.dominando.android.myapplication;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class tarea extends AsyncTask {
    String IP;
    int PORT;
    MainActivity context;
    DatagramSocket cliente_udp = null;
    InetAddress local = null;
   public tarea(int port,String ip,MainActivity c)
    {
        PORT=port;
        IP=ip;
        context=c;
        try {
            local = InetAddress.getByName(IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Object doInBackground(Object[] objects) {
       if(MainActivity.SEND) {
           //String messageStr = "$CONNECT\r\n";
           String messageStr= null;

           messageStr = context.programarWifi.programar();

           if (cliente_udp != null) {
               cliente_udp.close();
               cliente_udp = null;
           }
           try {
               cliente_udp = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
               cliente_udp.setBroadcast(true);
           } catch (Exception e) {
               e.printStackTrace();
           }

           int msg_length = messageStr.length();
           byte[] message = messageStr.getBytes();
           DatagramPacket p = null;
           try {
               p = new DatagramPacket(message, msg_length, getBroadcastAddress(), PORT);
           } catch (IOException e) {
               e.printStackTrace();
           }

           try {
               cliente_udp.setBroadcast(true);
               cliente_udp.send(p);
           } catch (Exception e) {
               e.printStackTrace();
           }
           MainActivity.SEND=false;
       }
       else
       {
           if (cliente_udp != null) {
               cliente_udp.close();
               cliente_udp = null;
           }
           try {
               cliente_udp = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
               cliente_udp.setBroadcast(true);
           } catch (Exception e) {
               e.printStackTrace();
           }

           byte[] recvBuf = new byte[15000];
           DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
           try {
               cliente_udp.receive(packet);
               final String data = new String(packet.getData()).trim();
               new Handler(Looper.getMainLooper()).post(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(context,data,Toast.LENGTH_LONG).show();
                   }
                   // execute code that must be run on UI thread
               });

           } catch (Exception e) {
               e.printStackTrace();
           }


       }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if(cliente_udp!=null) {
            cliente_udp.close();
        }
        MainActivity.FREE=true;
    }


    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}

package com.dominando.android.myapplication;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Handler;

public class ClientListen implements Runnable {
    MainActivity mainActivity;
  public  ClientListen(MainActivity activity)
    {
        mainActivity=activity;
    }
    @SuppressLint("LongLogTag")
    @Override
    public void run() {
        boolean run = true;
        while (run) {
            try {
                DatagramSocket udpSocket = new DatagramSocket(3333);
                byte[] message = new byte[8000];
                DatagramPacket packet = new DatagramPacket(message,message.length);
                Log.i("UDP client: ", "about to wait to receive");
                udpSocket.receive(packet);
                String text = new String(message, 0, packet.getLength());
                Log.d("Received data", text);
                Toast.makeText(mainActivity,text,Toast.LENGTH_LONG).show();
            }catch (IOException e) {
                Log.e("UDP client has IOException", "error: ", e);
                run = false;
            }
        }
    }
}
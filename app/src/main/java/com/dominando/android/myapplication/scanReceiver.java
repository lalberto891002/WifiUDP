package com.dominando.android.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

class scanReceiver extends BroadcastReceiver {
    public void onReceive(Context c, Intent intent) {
        Toast.makeText(c,"receivng",Toast.LENGTH_LONG).show();

    }



}


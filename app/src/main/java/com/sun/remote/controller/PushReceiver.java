package com.sun.remote.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * =====================================================================================
 * Summary:
 *
 * File: PushReceiver.java
 * Author: Yanpeng.Sun
 * Create: 2019/5/13 9:17
 * =====================================================================================
 */
public class PushReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            Log.e("PushHandler", intent.getExtras().toString());
        }
    }
}

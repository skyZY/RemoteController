package com.sun.remote.controller.remote;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/*
 * =====================================================================================
 * Summary:
 *
 * File: MyServiceConnection.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/13 15:01
 * =====================================================================================
 */
public class MyServiceConnection implements ServiceConnection{

    private IGetMessageCallBack IGetMessageCallBack;
    private RemoteControlService mqttService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mqttService = ((RemoteControlService.CustomBinder)service).getService();
        mqttService.setIGetMessageCallBack(IGetMessageCallBack);
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public RemoteControlService getMqttService(){
        return mqttService;
    }
    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack){
        this.IGetMessageCallBack = IGetMessageCallBack;
    }
}

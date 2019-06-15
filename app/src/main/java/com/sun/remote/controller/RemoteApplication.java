package com.sun.remote.controller;

import android.app.Application;
import android.content.Context;

import com.sun.remote.controller.remote.RemoteControlService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * =====================================================================================
 * Summary:
 *
 * File: RemoteApplication.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/15 12:58
 * =====================================================================================
 */
public class RemoteApplication extends Application{
    private static Context mContextApp;
    public static ExecutorService mExecutors;
    @Override
    public void onCreate() {
        super.onCreate();
        mContextApp = this;
        mExecutors = Executors.newFixedThreadPool(4);
        RemoteControlService.start(mContextApp);
    }

    public static Context getInstance(){
        return mContextApp;
    }
}

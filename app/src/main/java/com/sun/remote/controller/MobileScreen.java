package com.sun.remote.controller;

import android.app.Activity;
import android.util.DisplayMetrics;

/*
 * =====================================================================================
 * Summary:
 *
 * File: MobileScreen.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/15 14:54
 * =====================================================================================
 */
public class MobileScreen{

    public static int mScreenWidth;
    public static int mScreenHeight;

    public static void initScreen(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    public static int getmScreenWidth() {
        if(mScreenWidth == 0)
            throw new RuntimeException("please call initScreen ");
        return mScreenWidth;
    }

    public static int getmScreenHeight() {
        if(mScreenHeight == 0)
            throw new RuntimeException("please call initScreen ");
        return mScreenHeight;
    }

}

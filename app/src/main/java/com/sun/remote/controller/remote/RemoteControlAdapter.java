package com.sun.remote.controller.remote;

import java.io.IOException;

/*
 * =====================================================================================
 * Summary:
 *
 * File: RemoteControlAdapter.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/11 17:23
 * =====================================================================================
 */
public interface RemoteControlAdapter{

    abstract boolean initSocket();

    abstract boolean isRunning();

    abstract String accept() throws IOException;

    abstract void close() throws IOException;


}

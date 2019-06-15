package com.sun.remote.controller;

public interface IPushHandler{
    void onRecvMessage(int id, String method, String msg);
}

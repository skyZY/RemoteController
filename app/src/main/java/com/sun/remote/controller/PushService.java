package com.sun.remote.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PushService{

    // {"cmd":"active"}
    // mqtt topic : id/cmd
    // https://mqttfx.jensd.de/index.php/download
    // 35.240.197.132:1883
    public static final String INTENT_ACTIVE = "carota.intent.action.ACTIVE";
    private final static String TAG = "PushService";
    //private static String SERVER_URL = "ws://192.168.95.38:8686/mqtt";
    private static final String SERVER_URL = "tcp://192.168.0.103:7788"/*"tcp://35.240.197.132:1883"*//*"tcp://192.192.192.10:1883"*/;
    private static PushClient sPushClient = null;
    private static Context sContext = null;

    public static void init(Context context, String id, final BroadcastReceiver pushHandler) {
        if(null != sPushClient) {
            try{
                sPushClient.stop();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        sContext = context.getApplicationContext();
        sPushClient = new PushClient(sContext, SERVER_URL, id);
        sPushClient.setHandler("/cmd", new IPushHandler(){
            @Override
            public void onRecvMessage(int id, String method, String msg) {
                if(null != pushHandler) {
                    JSONObject joMsg = parseJson(msg);
                    if(null != joMsg) {
                        Bundle extra = new Bundle();
                        Iterator<String> it = joMsg.keys();
                        while (it.hasNext()) {
                            String key = it.next();
                            extra.putString(key, joMsg.optString(key));
                        }
                        pushHandler.onReceive(sContext, new Intent(joMsg.optString("action"))
                                .putExtras(extra).putExtra("method", method));
                    } else {
                        Log.e(TAG, "Invalid Message @ Topic - " + method);
                    }
                } else {
                    Log.e(TAG, "Invalid Handler @ Topic - " + method);
                }
            }
        });
    }

    public static void start() throws Exception {
        Log.i(TAG, "start service");
        sPushClient.start();
    }

    public static void stop() throws Exception {
        Log.i(TAG, "stop service");
        sPushClient.stop();

    }

    public static boolean isRunning() {
        return sPushClient.isRunning();
    }


    private static JSONObject parseJson(String json) {
        if(json == null) return null;
        JSONTokener tokener = new JSONTokener(json);
        Object root;
        try{
            root = tokener.nextValue();
            return (JSONObject) root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void SocketTest() {
        try{
            ServerSocket server = new ServerSocket(7788);       //步骤一
            ExecutorService mExecutorService = Executors.newCachedThreadPool();
            System.out.println("服务器已启动...");
            Socket client = null;
            while (true) {
                client = server.accept();         //步骤二，每接受到一个新Socket连接请求，就会新建一个Thread去处理与其之间的通信
//                mList.add(client);
                mExecutorService.execute(new MyServerThread(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String receiveMsg;
    private static String sendMsg;

    static class MyServerThread implements Runnable{

        Socket mSocket;
        private BufferedReader in = null;
        private PrintWriter printWriter = null;

        public MyServerThread(Socket socket) {
            mSocket = socket;
            try{
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), "UTF-8"));
                printWriter.println("成功连接服务器" + "（服务器发送）");
                System.out.println("成功连接服务器");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                while (true) {                                   //循环接收、读取 Client 端发送过来的信息
                    if ((receiveMsg = in.readLine())!=null) {
                        System.out.println("receiveMsg:"+receiveMsg);
                        if (receiveMsg.equals("0")) {
                            System.out.println("客户端请求断开连接");
                            printWriter.println("服务端断开连接"+"（服务器发送）");
//                            mList.remove(socket);
                            in.close();
                            mSocket.close();                         //接受 Client 端的断开连接请求，并关闭 Socket 连接
                            break;
                        } else {
                            sendMsg = "我已接收：" + receiveMsg + "（服务器发送）";
                            printWriter.println(sendMsg);           //向 Client 端反馈、发送信息
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}

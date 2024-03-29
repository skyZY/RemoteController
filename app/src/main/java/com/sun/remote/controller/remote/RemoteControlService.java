package com.sun.remote.controller.remote;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.sun.remote.controller.IScreenController;
import com.sun.remote.controller.R;
import com.sun.remote.controller.SharedPreferencesUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import androidx.core.app.NotificationCompat;

/*
 * =====================================================================================
 * Summary:
 *
 * File: RemoteControlService.java
 * Author: Yanpeng.Sun
 * Create: 2019/6/11 17:25
 * =====================================================================================
 */
public class RemoteControlService extends Service implements RemoteControlAdapter{
    private Socket mSocket;

    public static final String TAG = RemoteControlService.class.getSimpleName();

    private static MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    private String host = "tcp://127.0.0.1";
    private String userName = "u001";
    private String passWord = "p001";
    private static String myTopic = "ForTest";      //要订阅的主题
    private String clientId = "c001";//客户端标识
    private IGetMessageCallBack IGetMessageCallBack;
    private static IScreenController mConttoller;
    private static Socket socket;

    public static void start(Context context) {
        context.startService(new Intent(context, RemoteControlService.class).setPackage(context.getPackageName()));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getClass().getName(), "onCreate");
//        init();
//        initSocket();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        connectServer(mConttoller);
        return START_NOT_STICKY;
    }

    @Override
    public boolean initSocket() {
        try{
            mSocket = new Socket("127.0.0.1", 2001);
            //构建IO
            InputStream is = mSocket.getInputStream();
            OutputStream os = mSocket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            //向服务器端发送一条消息
            bw.write("connect");
            bw.flush();
            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String mess = br.readLine();
            System.out.println("服务器发来的消息：" + mess);
            if(mess.equals("connected")) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void publish(String msg) {
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        Log.d("syp", "msg:" + msg + " ; client:" + client);
        try{
            if(client != null) {
                client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    private void init() {
        // 服务器地址（协议+地址+端口号）
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(true);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(20);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());     //将字符串转换为字符串数组

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + clientId + "\"}";
        Log.e(getClass().getName(), "message是:" + message);
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;
        if((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            // MQTT本身就是为信号不稳定的网络设计的，所以难免一些客户端会无故的和Broker断开连接。
            //当客户端连接到Broker时，可以指定LWT，Broker会定期检测客户端是否有异常。
            //当客户端异常掉线时，Broker就往连接时指定的topic里推送当时指定的LWT消息。

            try{
                conOpt.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }

        if(doConnect) {
            doClientConnection();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if(!client.isConnected() && isConnectIsNormal()) {
            try{
                client.connect(conOpt, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onDestroy() {
        stopSelf();
        try{
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    @Override
    public boolean isRunning() {
        return mSocket.isConnected();
    }

    @Override
    public String accept() throws IOException {
        if(mSocket.isConnected()) {
            //读取服务器返回的消息
            BufferedReader br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            String mess = br.readLine();
            return mess;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener(){

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try{
                // 订阅myTopic话题
                client.subscribe(myTopic, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            // 连接失败，重连
        }
    };


    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback(){

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            if(IGetMessageCallBack != null) {
                IGetMessageCallBack.setMessage(str1);
            }
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "messageArrived:" + str1);
            Log.i(TAG, str2);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
        }
    };


    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return new CustomBinder();
    }

    public void setIGetMessageCallBack(IGetMessageCallBack IGetMessageCallBack) {
        this.IGetMessageCallBack = IGetMessageCallBack;
    }


    public class CustomBinder extends Binder{
        public RemoteControlService getService() {
            return RemoteControlService.this;
        }
    }

    public void toCreateNotification(String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, RemoteControlService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);//3、创建一个通知，属性太多，使用构造器模式

        Notification notification = builder
                .setTicker("测试标题")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("")
                .setContentText(message)
                .setContentInfo("")
                .setContentIntent(pendingIntent)//点击后才触发的意图，“挂起的”意图
                .setAutoCancel(true)        //设置点击之后notification消失
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(0, notification);
        notificationManager.notify(0, notification);

    }

    public static void connectServer(IScreenController controller) {
        try{
            String ip = SharedPreferencesUtils.getInstance().getValue(SharedPreferencesUtils.KEY_IP, "");
            String host = TextUtils.isEmpty(ip) ? "192.168.0.101" : ip;
            mConttoller = controller;
            SocketAddress remoteAddr = new InetSocketAddress(host, 7788); //获取sockaddress对象
            socket = new Socket();
            socket.connect(remoteAddr, 5000);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setReuseAddress(true);

            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装成打印流
            JSONObject jsonCmd = new JSONObject();
            jsonCmd.put("action", "cmd");
            while (true) {
                pw.write(jsonCmd.toString());
                pw.flush();
                Thread.sleep(200);
                String msg = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                if(controller != null) controller.process(msg);
            }
//            br.close();
//            socket.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Log.e("UnknownHost", "来自服务器的数据:" + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("IOException", "来自服务器的数据:" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void closeSocket() {
        try{
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

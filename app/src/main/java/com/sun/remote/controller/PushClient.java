package com.sun.remote.controller;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PushClient implements MqttCallbackExtended {

    private final static String TAG = PushClient.class.getSimpleName();
    private MqttAndroidClient mClient;
    private MqttConnectOptions mConnectOptions;
    private IPushHandler mPushHandler;
    private String mTopic;
    public final String ID;

    public PushClient(Context context, String url, String id) {
        ID = id;
        mTopic = null;
        mClient = new MqttAndroidClient(context, url,  id);
        Log.i("", "mClient=" + mClient + " ;DEV:" + id);
        mClient.setCallback(this);
        mConnectOptions = new MqttConnectOptions();
        mConnectOptions.setAutomaticReconnect(true);
        mConnectOptions.setUserName("u001");
        mConnectOptions.setCleanSession(false);
    }

    public void setLoginInfo(String usr, String psw) {
        if(null != usr && null != psw) {
            mConnectOptions.setUserName(usr);
            mConnectOptions.setPassword(psw.toCharArray());
        }
    }

    public void setHandler(String method, IPushHandler handler) {
        mTopic = ID + method;
        mPushHandler = handler;
        Log.d(TAG, "Topic : " + mTopic);
    }

    public void start() throws MqttException {
        if(!mClient.isConnected()) {
            Log.i(TAG, "connect start ; clientId:" + mClient.getClientId());
            mClient.connect(mConnectOptions, null, new IMqttActionListener(){
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "connect onSuccess!");
                    mClient.setBufferOpts(createBufferOptions());
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "connect onFailure!", exception);
                }
            });
        }
    }

    public void stop() throws MqttException {
        Log.i(TAG, "mClient:" + mClient);
        if(mClient != null && mClient.isConnected())
            mClient.disconnect();
    }

    public boolean isRunning() {
        return mClient != null && mClient.isConnected();
    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(TAG, "connectComplete--" + reconnect + "-" + serverURI);
        if(reconnect) {
            subscribeToTopic();
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "connectionLost", cause);
    }


    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        Log.d(TAG, "messageArrived @ " + topic + " - " + msg);
        if(null != mPushHandler) {
            mPushHandler.onRecvMessage(message.getId(),
                    new StringBuilder(topic).delete(0, ID.length()).toString(), msg);
        } else {
            Log.e(TAG, "messageArrived ： No handler");
        }
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete--");
    }


    private void subscribeToTopic() {
        if(null == mTopic) {
            return;
        }
        try{
            mClient.subscribe(mTopic, 0, null, new IMqttActionListener(){
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to subscribe");
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private DisconnectedBufferOptions createBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }
}

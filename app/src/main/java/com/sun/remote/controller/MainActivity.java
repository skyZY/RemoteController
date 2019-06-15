package com.sun.remote.controller;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.sun.remote.controller.remote.RemoteControlService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class MainActivity extends Activity implements IScreenController, IMouseMove, View.OnClickListener{
    private Button btn_connect;
    private TextView tv_hello;
    private ImageView mImageView;
    private EditText et_ip;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        MobileScreen.initScreen(this);
        btn_connect = findViewById(R.id.btn_connect);
        mImageView = findViewById(R.id.img_mouse);
        findViewById(R.id.btn_setIp).setOnClickListener(this);
        et_ip = findViewById(R.id.et_ip);
        String ip = SharedPreferencesUtils.getInstance().getValue(SharedPreferencesUtils.KEY_IP,"");
        if(!TextUtils.isEmpty(ip)) et_ip.setText(ip);
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        RemoteControlService.closeSocket();
                    }
                }).start();

            }
        });
//        moveMouse(20,600,30,900);
//        PushService.init(mContext, "c001", new PushReceiver());

        btn_connect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try{
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            RemoteControlService.connectServer(MainActivity.this);
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
//        unbindService(serviceConnection);
        super.onDestroy();
    }

    public void test() {
        try{
            SocketAddress remoteAddr = new InetSocketAddress("192.168.0.101", 7788); //获取sockaddress对象
            Socket socket = new Socket();
            socket.connect(remoteAddr, 5000);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setReuseAddress(true);

            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装成打印流
//            JSONObject json = new JSONObject();
//            json.put("action","screen");
//            pw.write(json.toString());
//            pw.flush();

//            InputStream is = socket.getInputStream();
//            Log.i("syp", "resp："+new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
////            InputStreamReader iReader = new InputStreamReader(is);
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String info = null;
            JSONObject jsonCmd = new JSONObject();
            jsonCmd.put("action", "cmd");
            while (true) {
                pw.write(jsonCmd.toString());
                pw.flush();
                Thread.sleep(500);
                Log.i("syp", "cmd:" + jsonCmd.toString() + "，resp：" + new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
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

    @Override
    public void process(String action) {
        Log.i("syp", "action:" + action.toString());
        updateUI(action, this);
    }

    private void updateUI(final String action, final IMouseMove iMouseMove) {
        RemoteApplication.mExecutors.execute(new Runnable(){
            @Override
            public void run() {
                try{
                    ScreenAction screenAction = JSON.parseObject(action, ScreenAction.class);
                    Log.i("syp", "screenAction:" + screenAction);
                    float widthRate = (float) MobileScreen.getmScreenWidth() / screenAction.getScreen().getWidth();
                    float heightRate = (float) MobileScreen.getmScreenHeight() / screenAction.getScreen().getHeight();
                    Log.d("syp", "widthRate:" + widthRate + " ; heigthRate=" + heightRate + " ; mWidht=" + MobileScreen.getmScreenWidth());
                    float x = widthRate * screenAction.getMouse().getStartLocationX();
                    float y = heightRate * screenAction.getMouse().getStartLocationY();
                    int keys = screenAction.getMouse().getKeys();
                    if(null != iMouseMove) iMouseMove.move((int) x, (int) y, keys);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private float fromX;
    private float fromY;

    @Override
    public void move(final int x, final int y, int key) {
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                moveMouse(fromX, x, fromY, y);
            }
        });

        fromX = x;
        fromY = y;
    }
    TranslateAnimation translateAnimation;
    private void moveMouse(float fromX, float toX, float fromY, float toY) {
        Log.i("syp", "fromX:" + fromX + "toX:" + toX + " ; fromY:" + fromY + ";toY:" + toY);
        translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
        translateAnimation.setDuration(100);// 设置动画时间 
        translateAnimation.setRepeatCount(1);//设置重复次数
        mImageView.setVisibility(View.VISIBLE);
        mImageView.startAnimation(translateAnimation);
        translateAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        translateAnimation.setFillAfter(true);//不回到起始位置
        translateAnimation.startNow();
        translateAnimation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
              /*  mImageView.clearAnimation();
                int left = mImageView.getLeft();
                int top = mImageView.getTop() - 60;
                int width = mImageView.getWidth();
                int height = mImageView.getHeight();
                mImageView.layout(left, top, left + width, top + height);
                Log.i("syp", "left:" + left + " top:" + top + " ; width:" + width + " ; height:" + height);*/

            }
        });
        Log.i("syp", "start translate" );

    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_setIp:
                String ip = et_ip.getText().toString();
                if(!TextUtils.isEmpty(ip)){
                    SharedPreferencesUtils.getInstance().setValue(SharedPreferencesUtils.KEY_IP,ip);
                    Toast.makeText(this,"ip保存成功",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}

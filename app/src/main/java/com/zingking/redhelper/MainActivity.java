package com.zingking.redhelper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.zingking.redhelper.service.RedPacketService;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Button btnStart;
    private Notification notification;
    private NotificationManager nm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    private void setListener() {
        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            initNotificationBar();
            bindService(new Intent(this, RedPacketService.class), connection, Service.BIND_AUTO_CREATE);
        });
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_start);
    }


    public void initNotificationBar() {
        String service = Context.NOTIFICATION_SERVICE;
        nm = (NotificationManager) getSystemService(service); // get system

        notification = new Notification();
        //初始化通知
        notification.icon = R.mipmap.ic_launcher;
        RemoteViews contentView = new RemoteViews(getPackageName(),
                R.layout.activity_main_1);
        notification.contentView = contentView;
//
//        Intent intentPlay = new Intent("play");//新建意图，并设置action标记为"play"，用于接收广播时过滤意图信息
//        PendingIntent pIntentPlay = PendingIntent.getBroadcast(this, 0,
//                intentPlay, 0);
//        contentView.setOnClickPendingIntent(R.id.bt_notic_play, pIntentPlay);//为play控件注册事件
//
//        Intent intentPause = new Intent("pause");
//        PendingIntent pIntentPause = PendingIntent.getBroadcast(this, 0,
//                intentPause, 0);
//        contentView.setOnClickPendingIntent(R.id.bt_notic_pause, pIntentPause);
//
//        Intent intentNext = new Intent("next");
//        PendingIntent pIntentNext = PendingIntent.getBroadcast(this, 0,
//                intentNext, 0);
//        contentView.setOnClickPendingIntent(R.id.bt_notic_next, pIntentNext);
//
//        Intent intentLast = new Intent("last");
//        PendingIntent pIntentLast = PendingIntent.getBroadcast(this, 0,
//                intentLast, 0);
//        contentView.setOnClickPendingIntent(R.id.bt_notic_last, pIntentLast);
//
//        Intent intentCancel = new Intent("cancel");
//        PendingIntent pIntentCancel = PendingIntent.getBroadcast(this, 0,
//                intentCancel, 0);
//        contentView
//                .setOnClickPendingIntent(R.id.bt_notic_cancel, pIntentCancel);





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "com.zingking.redhelper",
                    TAG,
                    NotificationManager.IMPORTANCE_DEFAULT

            );

            nm.createNotificationChannel(channel);

        }




        notification.flags = notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除
        nm.notify(1, notification);//开启通知

    }

}

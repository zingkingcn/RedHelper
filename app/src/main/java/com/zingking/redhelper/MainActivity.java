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
    // https://www.cnblogs.com/roccheung/p/5797270.html
    // https://www.cnblogs.com/huolongluo/p/6120946.html
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    private void setListener() {
        btnStart.setOnClickListener(v -> {
            initNotificationBar();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            bindService(new Intent(this, RedPacketService.class), connection, Service.BIND_AUTO_CREATE);
        });
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_start);
    }


    public void initNotificationBar() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(false)
                .setContentText("通知消息")
                .setContentTitle("通知标题");
        Notification notification = builder.build();
        //初始化通知
//        notification.icon = R.mipmap.ic_launcher;
//        RemoteViews contentView = new RemoteViews(getPackageName(),
//                R.layout.activity_main_1);
//        notification.contentView = contentView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.zingking.redhelper",
                    TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("com.zingking.redhelper");
        }
        notification.flags = notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除
        notificationManager.notify(1, notification);//开启通知
    }

}

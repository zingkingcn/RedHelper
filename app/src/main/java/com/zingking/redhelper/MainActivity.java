package com.zingking.redhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;
import com.zingking.redhelper.appinfo.IPackageInfo;
import com.zingking.redhelper.appinfo.PackageInfoHelper;
import com.zingking.redhelper.appinfo.WechatPackageInfo703;
import com.zingking.redhelper.appinfo.WechatPackageInfo704;
import com.zingking.redhelper.databinding.ActivityMainBinding;
import com.zingking.redhelper.service.RedPacketService;

import info.hoang8f.android.segmented.SegmentedGroup;

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
    private SwitchButton swWechat;
    private IPackageInfo iPackageInfo;
    private SegmentedGroup sgVersionList;
    private TextView tvChooseVersion;
    private RadioButton rb703;
    private RadioButton rb704;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.setConfig(new BuildConfig());
        initView();
        setListener();
        iPackageInfo = new WechatPackageInfo704();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        btnStart.setOnClickListener(v -> {
            initNotificationBar();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            bindService(new Intent(this, RedPacketService.class), connection, Service.BIND_AUTO_CREATE);
        });
        swWechat.setOnTouchListener((v, event) -> !hadChooseVersion());
        swWechat.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                PackageInfoHelper.Companion.getInstance().addPackageInfo(RedHelper.WECHAT_PACKAGE_NAME, iPackageInfo);
            } else {
                PackageInfoHelper.Companion.getInstance().removePackageInfo(RedHelper.WECHAT_PACKAGE_NAME);
            }
        });
        View.OnTouchListener rbTouchListener = (v, event) -> grabPacketIng();
        rb703.setOnTouchListener(rbTouchListener);
        rb704.setOnTouchListener(rbTouchListener);
        sgVersionList.setOnCheckedChangeListener((group, checkedId) -> {
            tvChooseVersion.clearAnimation();
            switch (checkedId) {
                case R.id.rb_704:
                    iPackageInfo = new WechatPackageInfo704();
                    break;
                case R.id.rb_703:
                    iPackageInfo = new WechatPackageInfo703();
                    break;
                default:
            }
        });
    }

    /**
     * 抢红包中
     */
    private boolean grabPacketIng() {
        boolean checked = swWechat.isChecked();
        if (checked) {
            Toast.makeText(this, "正在抢红包中，不能切换微信版本！", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

    private boolean hadChooseVersion() {
        int radioButtonId = sgVersionList.getCheckedRadioButtonId();
        if (radioButtonId == -1) {
            Toast.makeText(this, "请选择微信版本", Toast.LENGTH_SHORT).show();
            tvChooseVersion.clearAnimation();
            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
            animation.setRepeatCount(-1);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            tvChooseVersion.startAnimation(animation);
            return false;
        } else {
            tvChooseVersion.clearAnimation();
            return true;
        }
    }

    private void initView() {
        btnStart = (Button) findViewById(R.id.btn_start);
        swWechat = (SwitchButton) findViewById(R.id.sw_wechat);
        sgVersionList = (SegmentedGroup) findViewById(R.id.sg_version_list);
        tvChooseVersion = (TextView) findViewById(R.id.tv_choose_version);
        rb703 = findViewById(R.id.rb_703);
        rb704 = findViewById(R.id.rb_704);
        String buildVersion = getString(R.string.build_revision);
        TextView tvGitVersion = findViewById(R.id.tv_git_version);
        tvGitVersion.setText("VC" + BuildConfig.VERSION_CODE + "_" + buildVersion);
    }

    public void initNotificationBar() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.logo3)
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

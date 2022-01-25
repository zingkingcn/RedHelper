package com.zingking.redhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.suke.widget.SwitchButton;
import com.zingking.redhelper.appinfo.*;
import com.zingking.redhelper.databinding.ActivityMainBinding;
import com.zingking.redhelper.events.ReceiveRedPackageEvent;
import com.zingking.redhelper.events.ReturnHomeEvent;
import com.zingking.redhelper.service.RedPacketService;
import info.hoang8f.android.segmented.SegmentedGroup;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.List;

import static com.zingking.redhelper.Utils.isServiceRunning;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks{
    // https://www.cnblogs.com/roccheung/p/5797270.html
    // https://www.cnblogs.com/huolongluo/p/6120946.html
    private static final String TAG = "MainActivity";
    private final int PERMISSION_CODE_STORAGE = 0x0001;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
        }
    };
    private SwitchButton swWechat;
    private IPackageInfo iPackageInfo;
    private SegmentedGroup sgVersionList;
    private TextView tvChooseVersion, tvCheck, tvStart, tvServiceState;
    private RadioButton rb703, rb704, rb705, rb706, rb7010, rb7018, rb7021, rb8000, rb8018;
    private CheckBox cbHome, cbLock;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        ActivityMainBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBinding.setConfig(new BuildConfig());
        initView();
        setListener();
        iPackageInfo = new WechatPackageInfo704();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAccess();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        tvServiceState.setOnClickListener(v -> {
            if (checkAccess()) {
                Toast.makeText(this, "已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未开启", Toast.LENGTH_SHORT).show();
            }
        });
        tvCheck.setOnClickListener(v -> {
            if (checkAccess()) {
                Toast.makeText(this, "已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未开启", Toast.LENGTH_SHORT).show();
            }
        });
        tvStart.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Intent service = new Intent(this, RedPacketService.class);
            bindService(service, connection, Service.BIND_AUTO_CREATE);
        });
        cbLock.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (EasyPermissions.hasPermissions(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        return false;
                    } else {
                        EasyPermissions.requestPermissions(
                                MainActivity.this,
                                "需要存储权限才能锁屏抢红包",
                                PERMISSION_CODE_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        );
                        return true;
                    }
                } else {
                    return false;
                }
            }
            return false;
        });
        swWechat.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return !hadChooseVersion();
            }
            return false;
        });
        swWechat.setOnCheckedChangeListener((view, isChecked) -> {
            boolean isRunning = isServiceRunning(this, RedPacketService.class.getName());
            Log.i(TAG, "RedPacketService isRunning = " + isRunning);
            if (isChecked) {
                PackageInfoHelper.Companion.getInstance().addPackageInfo(RedHelper.WECHAT_PACKAGE_NAME, iPackageInfo);
                initNotificationBar();
            } else {
                PackageInfoHelper.Companion.getInstance().removePackageInfo(RedHelper.WECHAT_PACKAGE_NAME);
                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
            }
        });
        View.OnTouchListener rbTouchListener = (v, event) -> grabPacketIng();
        rb703.setOnTouchListener(rbTouchListener);
        rb704.setOnTouchListener(rbTouchListener);
        rb705.setOnTouchListener(rbTouchListener);
        rb706.setOnTouchListener(rbTouchListener);
        rb7010.setOnTouchListener(rbTouchListener);
        rb7018.setOnTouchListener(rbTouchListener);
        rb7021.setOnTouchListener(rbTouchListener);
        rb8000.setOnTouchListener(rbTouchListener);
        rb8018.setOnTouchListener(rbTouchListener);
        sgVersionList.setOnCheckedChangeListener((group, checkedId) -> {
            tvChooseVersion.clearAnimation();
            switch (checkedId) {
                case R.id.rb_8018:
                    iPackageInfo = new WechatPackageInfo8018();
                    break;
                case R.id.rb_8000:
                    iPackageInfo = new WechatPackageInfo8000();
                    break;
                case R.id.rb_7021:
                    iPackageInfo = new WechatPackageInfo7021();
                    break;
                case R.id.rb_7018:
                    iPackageInfo = new WechatPackageInfo7018();
                    break;
                case R.id.rb_7010:
                    iPackageInfo = new WechatPackageInfo7010();
                    break;
                case R.id.rb_706:
                    iPackageInfo = new WechatPackageInfo706();
                    break;
                case R.id.rb_705:
                    iPackageInfo = new WechatPackageInfo705();
                    break;
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

    private boolean checkAccess() {
        boolean accessibilitySettingsOn = Utils.isAccessibilitySettingsOn(this);
        Log.i(TAG, "accessibilitySettingsOn = " + accessibilitySettingsOn);
        if (!accessibilitySettingsOn) {
            tvCheck.setText("未开启或开启失败，助手无效！");
            tvCheck.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return false;
        } else {
            tvCheck.setText("已开启");
            tvCheck.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
            return true;
        }
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
        boolean accessibilitySettingsOn = Utils.isAccessibilitySettingsOn(this);
        if (!accessibilitySettingsOn) {
            Toast.makeText(this, "请开启无障碍服务", Toast.LENGTH_LONG).show();
            return false;
        }
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
        cbHome = (CheckBox) findViewById(R.id.cb_home);
        cbLock = (CheckBox) findViewById(R.id.cb_lock);
        tvStart = (TextView) findViewById(R.id.tv_start);
        tvServiceState = (TextView) findViewById(R.id.tv_service_state);
        swWechat = (SwitchButton) findViewById(R.id.sw_wechat);
        sgVersionList = (SegmentedGroup) findViewById(R.id.sg_version_list);
        tvChooseVersion = (TextView) findViewById(R.id.tv_choose_version);
        tvCheck = (TextView) findViewById(R.id.tv_check);
        rb703 = findViewById(R.id.rb_703);
        rb704 = findViewById(R.id.rb_704);
        rb705 = findViewById(R.id.rb_705);
        rb706 = findViewById(R.id.rb_706);
        rb7010 = findViewById(R.id.rb_7010);
        rb7018 = findViewById(R.id.rb_7018);
        rb7021 = findViewById(R.id.rb_7021);
        rb8000 = findViewById(R.id.rb_8000);
        rb8018 = findViewById(R.id.rb_8018);
        String buildVersion = getString(R.string.build_revision);
        TextView tvGitVersion = findViewById(R.id.tv_git_version);
        tvGitVersion.setText("VC" + BuildConfig.VERSION_CODE + "_" + buildVersion);
    }

    public void initNotificationBar() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.logo3)
                .setAutoCancel(false)
                .setContentText("在下奋力抢红包中...")
                .setContentTitle("主上");
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.zingking.redhelper",
                    TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId("com.zingking.redhelper");
        }
        notification.flags = notification.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除
        notificationManager.notify(1, notification);//开启通知
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 权限请求成功，easyPermissions 回调
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }
    /**
     * 权限请求失败，easyPermissions 回调
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        String title = "权限被拒绝";
        String rationale = "请打开存储权限以保证应用正常运行";
        if (perms.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
                || perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            title = "存储权限已经被您拒绝";
            rationale = "请打开存储权限以保证锁屏抢红包正常使用";
        }
        //如果有一些权限被永久的拒绝, 就需要转跳到 设置-->应用-->对应的App下去开启权限
        new AppSettingsDialog.Builder(this)
                .setTitle(title)
                .setRationale(rationale)
                .build()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openHome(ReturnHomeEvent event) {
        if (cbHome.isChecked()) {
            //模拟Home键操作
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveRedPackage(ReceiveRedPackageEvent event) {
        if (cbLock.isChecked()){
            KeyguardManager keyManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyManager.isKeyguardLocked()) {
                Intent intent = new Intent(this, MessageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("msg", "微信收到一个红包，点我抢");
                startActivity(intent);
            }
        }
    }

}

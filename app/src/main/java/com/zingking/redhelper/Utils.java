package com.zingking.redhelper;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.zingking.redhelper.service.RedPacketService;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright © www.zingking.cn
 *
 * @auth Z.kai
 * @date 2019/7/18
 * @descrption
 */
public class Utils {
    private static final String TAG = "Utils";

    public static boolean isAccessibilityEnabled(Context context) throws RuntimeException {
        if (context == null) {
            return false;
        }

        // 检查AccessibilityService是否开启
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(android.content.Context.ACCESSIBILITY_SERVICE);
        boolean isAccessibilityEnabled_flag = am.isEnabled();

        boolean isExploreByTouchEnabled_flag = false;

        // 检查无障碍服务是否以语音播报的方式开启
        isExploreByTouchEnabled_flag = isScreenReaderActive(context);

        return (isAccessibilityEnabled_flag/* && isExploreByTouchEnabled_flag*/);

    }

    private final static String SCREEN_READER_INTENT_ACTION = "android.accessibilityservice.AccessibilityService";
    private final static String SCREEN_READER_INTENT_CATEGORY = "android.accessibilityservice.category.FEEDBACK_SPOKEN";

    private static boolean isScreenReaderActive(Context context) {

        // 通过Intent方式判断是否存在以语音播报方式提供服务的Service，还需要判断开启状态
        Intent screenReaderIntent = new Intent(SCREEN_READER_INTENT_ACTION);
        screenReaderIntent.addCategory(SCREEN_READER_INTENT_CATEGORY);
        List<ResolveInfo> screenReaders = context.getPackageManager().queryIntentServices(screenReaderIntent, 0);
        // 如果没有，返回false
        if (screenReaders == null || screenReaders.size() <= 0) {
            return false;
        }

        boolean hasActiveScreenReader = false;
        if (Build.VERSION.SDK_INT <= 15) {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = null;
            int status = 0;

            for (ResolveInfo screenReader : screenReaders) {
                cursor = cr.query(Uri.parse("content://" + screenReader.serviceInfo.packageName
                        + ".providers.StatusProvider"), null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    status = cursor.getInt(0);
                    cursor.close();
                    // 状态1为开启状态，直接返回true即可
                    if (status == 1) {
                        return true;
                    }
                }
            }
        } else if (Build.VERSION.SDK_INT >= 26) {
            // 高版本可以直接判断服务是否处于开启状态
            for (ResolveInfo screenReader : screenReaders) {
                hasActiveScreenReader |= isAccessibilitySettingsOn(context, screenReader.serviceInfo.packageName + "/" + screenReader.serviceInfo.name);
            }

        } else {
            // 判断正在运行的Service里有没有上述存在的Service
            List<String> runningServices = new ArrayList<String>();

            android.app.ActivityManager manager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                runningServices.add(service.service.getPackageName());
            }

            for (ResolveInfo screenReader : screenReaders) {
                if (runningServices.contains(screenReader.serviceInfo.packageName)) {
                    hasActiveScreenReader |= true;
                }
            }
        }

        return hasActiveScreenReader;
    }

    // To check if service is enabled
    private static boolean isAccessibilitySettingsOn(Context context, String service) {

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        String settingValue = Settings.Secure.getString(
                context.getApplicationContext().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            mStringColonSplitter.setString(settingValue);
            while (mStringColonSplitter.hasNext()) {
                String accessibilityService = mStringColonSplitter.next();
                if (accessibilityService.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * 检测辅助功能是否开启<br>
     * 方 法 名：isAccessibilitySettingsOn <br>
     * 创 建 人 <br>
     * 创建时间：2016-6-22 下午2:29:24 <br>
     * 修 改 人： <br>
     * 修改日期： <br>
     *
     * @param mContext
     * @return boolean
     */
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        // TestService为对应的服务
        final String service = mContext.getPackageName() + "/" + RedPacketService.class.getCanonicalName();
        Log.i(TAG, "service:" + service);
        // com.z.buildingaccessibilityservices/android.accessibilityservice.AccessibilityService
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            // com.z.buildingaccessibilityservices/com.z.buildingaccessibilityservices.TestService
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++) {
            Log.i("服务运行1：",""+runningService.get(i).service.getClassName().toString());
            if (runningService.get(i).service.getClassName().toString().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }
}

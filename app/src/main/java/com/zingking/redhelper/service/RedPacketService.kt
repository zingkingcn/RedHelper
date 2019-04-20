package com.zingking.redhelper.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zingking.redhelper.RedHelper.WECHAT_PACKAGE_NAME
import com.zingking.redhelper.appinfo.INodeInfoListener
import com.zingking.redhelper.appinfo.IPackageInfo
import com.zingking.redhelper.appinfo.PackageInfoHelper

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2019/4/7
 * @description
 */
class RedPacketService : AccessibilityService(), INodeInfoListener {

    private val TAG = "RedPacketService"
    override fun onInterrupt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i(TAG, "onAccessibilityEvent = " + event)
        event?.let {
            val eventType: Int = event.eventType
            val packageInfo: IPackageInfo? = PackageInfoHelper.instance.getPackageInfo(WECHAT_PACKAGE_NAME)
            packageInfo?.let {
                packageInfo.event = event
                packageInfo.iNodeInfoListener = this
                when (eventType) {
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                        val powerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                        var screennOn = false

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                            screennOn = powerManager.isInteractive
                        } else {
                            screennOn = powerManager.isScreenOn
                        }
                        if (!screennOn){
                            val wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "redHelper:RedPacketService")
                            wakeLock.acquire(3000)
                            wakeLock.release()
                        }
                        packageInfo.openApp()
                    }
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                        packageInfo.grabPacket()
                    }
                    AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                        packageInfo.dealLastMsg()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun getNodeInfo(): AccessibilityNodeInfo? {
        return rootInActiveWindow
    }

}
package com.zingking.redhelper.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zingking.redhelper.MessageActivity
import com.zingking.redhelper.RedHelper.WECHAT_PACKAGE_NAME
import com.zingking.redhelper.appinfo.IAppListener
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
                        packageInfo.openApp(IAppListener {
                            val keyManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyManager.isKeyguardLocked) {
                                var intent: Intent = Intent(this, MessageActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("msg", "微信收到一个红包，点我抢")
                                startActivity(intent)
                            }
                        })
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
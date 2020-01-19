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
        Log.e(TAG,"onInterrupt 服务中断")
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
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> { // 1.接收到通知：打开微信app
                        packageInfo.openApp(IAppListener {
                            // TODO event bus 发送收到红包消息，由首页去判断和打开 MessageActivity
                            val keyManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyManager.isKeyguardLocked) { // 锁屏打开app
                                var intent: Intent = Intent(this, MessageActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("msg", "微信收到一个红包，点我抢")
                                startActivity(intent)
                            }
                        })
                    }
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> { // 2.打开了微信app：执行了上面的 1 后触发
                        packageInfo.grabPacket()
                    }
                    AccessibilityEvent.TYPE_VIEW_SCROLLED -> { // 3.在聊天页面：聊天信息滚动时触发
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
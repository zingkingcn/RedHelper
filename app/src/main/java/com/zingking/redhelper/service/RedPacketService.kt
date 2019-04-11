package com.zingking.redhelper.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2019/4/7
 * @description
 */
class RedPacketService : AccessibilityService() {
    private val TAG = "RedPacketService"
    override fun onInterrupt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i(TAG, "onAccessibilityEvent = " + event)
        event?.let {
            val packageName: String = event.packageName.toString()
            val eventType: Int = event.eventType
            val texts: List<CharSequence> = event.text
            when (packageName) {
                "com.tencent.mm" -> {
                    when (eventType) {
                        AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                            for (text in texts) {
                                if (text.contains("微信红包")) {
                                    val parcelableData = event.parcelableData
                                    if (parcelableData is Notification) {
                                        val pendingIntent = parcelableData.contentIntent
                                        pendingIntent.send()
                                        Log.i(TAG, "打开微信")
                                    }
                                    break
                                }
                            }
                        }
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                            val className = event.className.toString()
                            when (className) {
                                "com.tencent.mm.ui.LauncherUI" -> {
                                    Log.i(TAG, "点击红包")
                                    clickPacket(rootInActiveWindow)
                                }
//                                com.tencent.mm/.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI
                                "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI" -> {
                                    inputClick("com.tencent.mm:id/cyf")
                                }
//                                "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" -> {
//                                    inputClick("com.tencent.mm:id/kb")
//                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun inputClick(s: String) {
        rootInActiveWindow?.let {
            val infosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(s)
            for (node: AccessibilityNodeInfo in infosByViewId) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    private fun clickPacket(nodeInfo: AccessibilityNodeInfo) {
        if (nodeInfo.childCount == 0) {
            nodeInfo.text?.let {
                if ("微信红包" == (nodeInfo.text.toString())) {
                    //这里有一个问题需要注意，就是需要找到一个可以点击的View
                    Log.i("demo", "Click" + ",isClick:" + nodeInfo.isClickable())
//                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    var parent: AccessibilityNodeInfo? = nodeInfo.parent
                    while (parent != null) {
                        Log.i("demo", "parent isClick:" + parent.isClickable())
                        if (parent.isClickable) {
                            if (!hasFinish(parent)) {
                                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                break
                            }
                        }
                        parent = parent.parent
                    }
                }
            }
        } else {
            for (i in 0 until nodeInfo.childCount) {
                val child = nodeInfo.getChild(i)
                if (child != null) {
                    clickPacket(child)
                }
            }
        }
    }

    fun hasFinish(parent: AccessibilityNodeInfo): Boolean {
        if (parent.childCount > 0) {
            for (i in 0..parent.childCount - 1) {
                if (hasFinish(parent.getChild(i))) {
                    return true
                }
            }
        }
        parent.text?.let {
            if ("已领取" == (parent.text.toString())) {
                Log.i(TAG, "已领取")
                return true
            }
        }
        return false
    }


}
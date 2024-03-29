package com.zingking.redhelper.appinfo

import android.app.Notification
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.zingking.redhelper.events.ReturnHomeEvent
import org.greenrobot.eventbus.EventBus

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2019/4/12
 * @description 适配 微信7.0.3 版本
 */

open class WechatPackageInfo703 : IPackageInfo {
    open val CHAT_UI_CLASS = "com.tencent.mm.ui.LauncherUI" // 微信聊天界面
    open val CHAT_UI_CLASS_2 = "com.tencent.mm/.ui.LauncherUI" // 微信聊天界面
    open val MONEY_UI_CLASS = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI" // 抢红包界面,有开字的那个弹框
    open val MONEY_UI_CLASS_2 = "com.tencent.mm/.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI" // 抢红包界面,有开字的那个弹框
    open val OPEN_BUTTON_ID = "com.tencent.mm:id/cyf" // "开"按钮
    open val MONEY_UI_DETAIL_CLASS = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" // 红包详情页面
    /**
     * 这个值是停留在在聊天页面(不是聊天列表)是监听消息用的，区别于[grabPacket]
     */
    open val MESSAGE_GROUP_ID = "com.tencent.mm:id/alc" // 消息树(ListView)的父控件名
    val SCREEN_LOCK_CLICK_INTERVAL = 3000L // 锁屏状态下弹出带“开”字的框时，不会触发自动点“开”的逻辑，使用延迟处理
    val SCREEN_LOCK_CLICK_TAG = 0x22 // 锁屏状态下弹出带“开”字的框时，不会触发自动点“开”的逻辑，使用延迟处理
    val handler: Handler = Handler {
        if (it.what == SCREEN_LOCK_CLICK_TAG) {
            clickViewById(OPEN_BUTTON_ID)
        }
        false
    }

    override var event: AccessibilityEvent? = null
    override var iNodeInfoListener: INodeInfoListener? = null
    val TAG = "WechatPackageInfo703"

    init {
        println("Init block")
    }

    /**
     * 这个是点击"微信红包"通知跳转到微信聊天界面用的区别于[MESSAGE_GROUP_ID]
     */
    override fun grabPacket() {
        val rootInActiveWindow: AccessibilityNodeInfo = iNodeInfoListener!!.getNodeInfo() ?: return
        val className: CharSequence? = event!!.className
        Log.i(TAG, "grabPacket: $className")
        if (TextUtils.isEmpty(className)){
            return
        }
        when (className.toString()) {
            CHAT_UI_CLASS_2, CHAT_UI_CLASS -> { // 微信聊天页面
                val redPacket = filterRedPacket(rootInActiveWindow)
                redPacket?.let {
                    // 点击红包
                    clickPacket(redPacket)
                }
            }
            MONEY_UI_CLASS_2, MONEY_UI_CLASS -> { // 开红包页面
                clickViewById(OPEN_BUTTON_ID)
            }
            MONEY_UI_DETAIL_CLASS -> { // 红包详情页面
                EventBus.getDefault().post(ReturnHomeEvent())
//
//                //模拟Home键操作
//                var intent: Intent = Intent(Intent.ACTION_MAIN);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(intent);
            }

            else -> {
            }
        }
    }

    private fun clickPacket(redPacket: AccessibilityNodeInfo) {
        Log.i(TAG, "clickPacket -> " + "点击红包")
        redPacket.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        val message = Message.obtain()
        message.what = SCREEN_LOCK_CLICK_TAG
        handler.sendMessageDelayed(message, SCREEN_LOCK_CLICK_INTERVAL)
    }

    /**
     * 过滤聊天信息页面中的红包
     */
    private fun filterRedPacket(nodeInfo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d(TAG, "filterRedPacket() called with: nodeInfo = $nodeInfo")
        var result: AccessibilityNodeInfo? = null
        if (nodeInfo.childCount == 0) {
            nodeInfo.text?.let {
                if ("微信红包" == (nodeInfo.text.toString())) {
                    //注意，需要找到一个可以点击的View
                    Log.i("${TAG}demo", "Click" + ",isClick:" + nodeInfo.isClickable())
                    var parent: AccessibilityNodeInfo? = nodeInfo.parent
                    while (parent != null) {
                        Log.i("${TAG}demo", "parent isClick:" + parent.isClickable())
                        if (parent.isClickable) {
                            if (!hasFinish(parent)) {
                                result = parent
                                break
                            }
                        }
                        parent = parent.parent
                    }
                }
            }
        } else {
            for (i in 0 until nodeInfo.childCount) {
                val child:AccessibilityNodeInfo? = nodeInfo.getChild(i)
                if (child != null) {
                    result = filterRedPacket(child)
                    if (result != null) {
                        break
                    }
                }
            }
        }
        return result
    }

    override fun openApp(iAppListener: IAppListener?) {
        val texts: List<CharSequence> = event!!.text
        for (text in texts) {
            Log.i(TAG, "消息" + text)

            if (text.contains("微信红包")) {
                val parcelableData = event!!.parcelableData
                if (parcelableData is Notification) {
                    val pendingIntent = parcelableData.contentIntent
                    pendingIntent.send()
                    Log.i(TAG, "openApp -> " + "打开微信")
                    iAppListener?.onOpenApp()
                }
                break
            }
        }
    }

    override fun dealLastMsg() {
        val className = event!!.className.toString()
//        when (className) {
//            CHAT_UI_CLASS -> { // 微信聊天页面
        val rootInActiveWindow = iNodeInfoListener!!.getNodeInfo() ?: return
        val messageGroup: List<AccessibilityNodeInfo>? = rootInActiveWindow.findAccessibilityNodeInfosByViewId(MESSAGE_GROUP_ID)
        if (messageGroup == null || messageGroup.isEmpty()) {
            return
        }
        val frameLayout: AccessibilityNodeInfo = messageGroup.get(0) // 聊天树中的listView
        if (frameLayout.childCount == 0) {
            return
        }
        val listView: AccessibilityNodeInfo? = frameLayout.getChild(0) // 聊天树中的listView
        if (listView?.childCount == 0) {
            return
        }
        val lastChild:AccessibilityNodeInfo? = listView?.getChild(listView.childCount - 1)
        if (lastChild == null) {
            return
        }
        val redPacket = filterRedPacket(lastChild)
        redPacket?.let {
            clickPacket(redPacket)
        }
    }

    private var currTryCount = 0
    // 有"开"的那个小弹窗，点击“开”的重试次数
    private val OPEN_BUTTON_TRY_COUNT = 10
    // 有"开"的那个小弹窗，点击“开”的重试间隔毫秒数
    private var OPEN_BUTTON_TRY_INTERVAL = 300L

    private fun clickViewById(sId: String) {
        currTryCount++
        Log.d(TAG, "clickViewById() called with: sId = $sId")
        handler.removeMessages(SCREEN_LOCK_CLICK_TAG)
        val rootInActiveWindow:AccessibilityNodeInfo? = iNodeInfoListener!!.getNodeInfo()
        Log.d(TAG, "clickViewById() called with: rootInActiveWindow = $rootInActiveWindow")
        val infosByViewId = rootInActiveWindow?.findAccessibilityNodeInfosByViewId(sId)
        Log.d(TAG, "clickViewById() called with: infosByViewId = $infosByViewId")
        if (rootInActiveWindow == null || infosByViewId == null || infosByViewId.isEmpty()) {
            Log.d(TAG, "clickViewById() called with: currTryCount = $currTryCount")
            if (currTryCount > OPEN_BUTTON_TRY_COUNT) {
                return
            }
            Thread.sleep(OPEN_BUTTON_TRY_INTERVAL)
            clickViewById(sId)
            return
        }
        for (node: AccessibilityNodeInfo in infosByViewId) {
            Log.i(TAG, "clickViewById -> " + "开红包")
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
//        recycle(rootInActiveWindow)
//        Thread.sleep(1000)
//        recycle(iNodeInfoListener!!.getNodeInfo())
    }

    /**
     * 测试方法
     */
    private fun recycle(nodeInfo: AccessibilityNodeInfo?){
        Log.d(TAG, "recycle() called with: nodeInfo = $nodeInfo")
        if (nodeInfo == null) {
            return
        }
        if (nodeInfo.childCount == 0) {
            Log.d(TAG, "recycle() called with: nodeInfo = $nodeInfo")
        }else{
            for (index in 0 until  nodeInfo.childCount) {
                val child:AccessibilityNodeInfo? = nodeInfo.getChild(index)
                if (child != null) {
                    recycle(child)
                }
            }
        }

    }

    /**
     * 判断红包是否已经领取过
     */
    private fun hasFinish(parent: AccessibilityNodeInfo): Boolean {
        if (parent.childCount > 0) {
            for (i in 0..parent.childCount - 1) {
                val child: AccessibilityNodeInfo? = parent.getChild(i)
                if (child == null) {
                    return false
                }
                if (hasFinish(child)) {
                    return true
                }
            }
        }
        parent.text?.let {
            if ("已领取" == (parent.text.toString())
                || "已被领完" == (parent.text.toString())
                || "已领取" in parent.text.toString()
                || "已过期" == (parent.text.toString())) {
                Log.i(TAG, "已领取")
                return true
            }
        }
        return false
    }

}
package com.zingking.redhelper.appinfo

import android.view.accessibility.AccessibilityNodeInfo

interface INodeInfoListener {
    fun getNodeInfo(): AccessibilityNodeInfo?
}
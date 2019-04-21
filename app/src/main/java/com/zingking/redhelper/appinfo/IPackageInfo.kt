package com.zingking.redhelper.appinfo

import android.view.accessibility.AccessibilityEvent

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2019/4/12
 * @description
 */
interface IPackageInfo {

    var event: AccessibilityEvent?
    var iNodeInfoListener: INodeInfoListener?

    fun openApp(iAppListener: IAppListener?)

    fun grabPacket()

    fun dealLastMsg()
}
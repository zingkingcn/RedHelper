package com.zingking.redhelper.appinfo

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2020/1/19
 * @description 适配 微信7.0.18 版本
 */

open class WechatPackageInfo7018 : WechatPackageInfo703() {
    override var OPEN_BUTTON_ID = "com.tencent.mm:id/den" // "开"按钮
    override val MESSAGE_GROUP_ID = "com.tencent.mm:id/am4" // 消息树(ListView)的父控件名，是一个framelayout

}
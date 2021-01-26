package com.zingking.redhelper.appinfo

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2020/1/19
 * @description 适配 微信7.0.18 版本
 */

open class WechatPackageInfo8000 : WechatPackageInfo703() {
    override var OPEN_BUTTON_ID = "com.tencent.mm:id/f4f" // "开"按钮
    override val MESSAGE_GROUP_ID = "com.tencent.mm:id/avl" // 消息树(ListView)的父控件名，是一个framelayout

}
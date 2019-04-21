package com.zingking.redhelper

import android.content.Context
import android.os.Build
import android.os.PowerManager

/**
 * 判断是否亮屏
 * @param context 上下文
 * @return true：亮屏<p>false：息屏</p>
 */
fun isScreenOn(context: Context): Boolean {
    val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        powerManager.isInteractive
    } else {
        powerManager.isScreenOn
    }
}

/**
 * 唤醒屏幕
 */

fun weakScreen(context: Context) {
    if (!isScreenOn(context)) {
        val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "redHelper:RedPacketService")
        wakeLock.acquire(1000)
        wakeLock.release()
    }
}
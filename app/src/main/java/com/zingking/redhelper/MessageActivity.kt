package com.zingking.redhelper

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.app.KeyguardManager
import android.content.Context
import android.util.Log
import kotlinx.android.synthetic.main.activity_message.*
import android.app.WallpaperManager
import android.graphics.drawable.Drawable


class MessageActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weakScreen(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val wallPaper: Drawable = WallpaperManager.getInstance(this).drawable
        window.setBackgroundDrawable(wallPaper)
        setContentView(R.layout.activity_message)
        val msg: String? = intent.extras?.get("msg") as String
        tv_msg.text = msg ?: "收到一个红包"
        tv_msg.setOnClickListener {
            Log.i("kai ", "onclick")
            //先解锁系统自带锁屏服务，放在锁屏界面里面
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.newKeyguardLock("unclock").disableKeyguard() //解锁
            finish()
        }
    }
}

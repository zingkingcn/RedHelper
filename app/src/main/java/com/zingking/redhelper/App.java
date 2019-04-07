package com.zingking.redhelper;

import android.app.Application;
import android.content.Context;

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 *
 * @author Z.kai
 * @date 2019/4/7
 * @description
 */

public class App extends Application{

    private  static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static Context getAppContext(){
        return context.getApplicationContext();
    }
}

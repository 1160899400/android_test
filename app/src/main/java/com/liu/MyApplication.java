package com.liu;

import android.app.Application;
import android.content.Context;

import com.liu.helper.GreenDaoHelper;

/**
 * @author HZLI02
 * @date 2018/8/21
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";


    @Override
    public void onCreate(){
        super.onCreate();
        GreenDaoHelper greenDaoManager = new GreenDaoHelper();
        greenDaoManager.initDatabase(getApplicationContext());

    }

    public Context getApplicationCtx(){
        return getApplicationContext();
    }
}

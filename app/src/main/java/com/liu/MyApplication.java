package com.liu;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.liu.helper.GreenDaoHelper;
import com.liu.log.LogConstant;
import com.liu.log.MyLogService;

import java.util.logging.Logger;

/**
 * @author HZLI02
 * @date 2018/8/21
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";


    @Override
    public void onCreate(){
        super.onCreate();
        Intent intent = new Intent(this,MyLogService.class);

        startService(intent);
        Log.i(TAG,"has start service");

        //        intent.setPackage("com.liu");
//        intent.setAction("com.liu.log.MyLogService");

//        GreenDaoHelper greenDaoManager = new GreenDaoHelper();
//        greenDaoManager.initDatabase(getApplicationContext());

    }


    @Override
    public void onTerminate(){
        super.onTerminate();
    }

    public Context getApplicationCtx(){
        return getApplicationContext();
    }
}

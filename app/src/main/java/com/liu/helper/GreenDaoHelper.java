package com.liu.helper;

import android.content.Context;
import android.util.Log;

import com.liu.dao.DaoMaster;
import com.liu.dao.DaoSession;

/**
 * @author HZLI02
 * @date 2018/8/21
 */

public class GreenDaoHelper {
    private static DaoMaster.DevOpenHelper helper;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    public static final String DB_NAME = "test.db";

    public void initDatabase(Context context){
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        helper = new DaoMaster.DevOpenHelper(context,DB_NAME,null);
        daoMaster = new DaoMaster(helper.getWritableDb());
        daoSession = daoMaster.newSession();
        Log.i("###","init database");
    }

    public static DaoSession getDaoSession(){
        return daoSession;
    }


}

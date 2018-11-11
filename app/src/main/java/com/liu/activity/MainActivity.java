package com.liu.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.liu.R;
import com.liu.dao.DaoMaster;
import com.liu.dao.DaoSession;
import com.liu.dao.StudentDao;
import com.liu.entity.Student;
import com.liu.helper.GreenDaoHelper;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.rx.RxDao;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.internal.schedulers.ImmediateScheduler;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindView();

//        addData();
//        showData();

        //DevOpenHelper indirectly extends to DatabaseOpenHelper of SQLite.
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, null);
        //get writable database.WritableDatabase is both readable and writable while ReadableDatabase is only readable
        Database database = helper.getWritableDb();
        //DaoMaster is entrance of database operation in GreenDao
        DaoMaster daoMaster = new DaoMaster(database);


        DaoSession daoSession = daoMaster.newSession();
        Long studentId = 3L;
        daoSession.load(Student.class, studentId);


        StudentDao studentDao = daoSession.getStudentDao();
        StudentDao studentDao1 = (StudentDao) daoSession.getDao(Student.class);

        DaoConfig daoConfig = new DaoConfig(database, StudentDao.class);
        daoConfig.initIdentityScope(IdentityScopeType.Session);
        StudentDao studentDao2 = new StudentDao(daoConfig);


//        AsyncSession asyncSession = new AsyncSession();


//        testDir();
//        mkDirFile("log_test", "test");
//        FileUtils.createFile(getExternalFilesDir("Log").getAbsolutePath(),"MyLog.log");

    }

    public void bindView() {

    }


    private void addData() {
        AbstractDao<Student, Long> mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        mStudentDao.insert(new Student());
        RxDao<Student, Long> rxDao = new RxDao<Student, Long>(mStudentDao);
        rxDao.loadAll().subscribeOn(Schedulers.newThread())
                .observeOn(ImmediateScheduler.INSTANCE)
                .subscribe(new Observer<List<Student>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Student> o) {

                    }
                });
    }

    private void showData() {
        StudentDao mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        Student student = mStudentDao.load(3L);
        Log.i(TAG, "student age: " + student.getAge());

    }



    /**
     * 用于测试文件内外部存储的路径
     */
    private void testDir() {
        //内置sdcard路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d(TAG, "MEDIA_MOUNTED  : getExternalStorageDirectory:" + Environment.getExternalStorageDirectory());
        } else {
            Log.d(TAG, "!MEDIA_MOUNTED  : getExternalStorageDirectory:" + Environment.getExternalStorageDirectory());
        }
        Log.d(TAG, "getExternalCacheDir  getAbsolutePath:" + Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.d(TAG, "getExternalCacheDir" + getExternalCacheDir());
        Log.d(TAG, "getExternalFilesDir" + getExternalFilesDir(null));


//        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filename");
//        if (!file.mkdirs()) {
//            Log.e(Tag, "Directory not created");
//        }
    }

    /**
     * 在指定目录下创建
     *
     * @param dirName
     * @param fileName
     */
    private void mkDirFile(String dirName, String fileName) {
        String pathFileDir = getExternalFilesDir(null).getAbsolutePath();
        File dirLog = new File(pathFileDir + "/" + "Log");
        if (!dirLog.exists()) {
            dirLog.mkdirs();
            Log.d(TAG, "mk dir result:  success to mk dir");
        } else {
            Log.d(TAG, "mk dir result:  dir exist");
        }
        File logFile = new File(dirLog.getAbsolutePath(), "Log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Log.log exist");
        }
    }

}

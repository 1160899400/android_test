package com.liu.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.liu.R;
import com.liu.dao.StudentDao;
import com.liu.entity.Student;
import com.liu.helper.GreenDaoHelper;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        addData();
        showData();

//        AsyncSession asyncSession = new AsyncSession();

//        testDir();
//        mkDirFile("log_test", "test");
//        FileUtils.createFile(getExternalFilesDir("Log").getAbsolutePath(),"MyLog.log");

    }
    public void bindView(){
        btn = findViewById(R.id.btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Btn click");
            }
        });
    }


    private void addData() {
        StudentDao mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        mStudentDao.insert(new Student(null, "23"));
    }

    private void showData() {
        StudentDao mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        Student student = mStudentDao.load(1L);
        Log.i("###", "student age: " + student.getAge());

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
     * @param dirName
     * @param fileName
     */
    private void mkDirFile(String dirName, String fileName){
        String pathFileDir = getExternalFilesDir(null).getAbsolutePath();
        File dirLog = new File(pathFileDir + "/" + "Log");
        if (!dirLog.exists()){
            dirLog.mkdirs();
            Log.d(TAG,"mk dir result:  success to mk dir" );
        }else {
            Log.d(TAG,"mk dir result:  dir exist" );
        }
        File logFile = new File(dirLog.getAbsolutePath() , "Log.txt");
        if (!logFile.exists()){
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Log.d(TAG,"Log.log exist" );
        }
    }

}

package com.liu.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.liu.dao.StudentDao;
import com.liu.entity.Student;
import com.liu.helper.GreenDaoHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addData();
        showData();

    }


    private void addData(){
        StudentDao mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        mStudentDao.insert(new Student(null,"23"));
    }

    private void showData(){
        StudentDao mStudentDao = GreenDaoHelper.getDaoSession().getStudentDao();
        Student student = mStudentDao.load(1L);
        Log.i("###","student age: "+student.getAge());

    }
}

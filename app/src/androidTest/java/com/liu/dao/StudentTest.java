package com.liu.dao;

import org.greenrobot.greendao.test.AbstractDaoTestLongPk;

import com.liu.entity.Student;

public class StudentTest extends AbstractDaoTestLongPk<StudentDao, Student> {

    public StudentTest() {
        super(StudentDao.class);
    }

    @Override
    protected Student createEntity(Long key) {
        Student entity = new Student();
        entity.set_id(key);
        entity.setAge();
        return entity;
    }

}

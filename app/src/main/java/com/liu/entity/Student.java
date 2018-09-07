package com.liu.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

/**
 * @author HZLI02
 * @date 2018/8/21
 */


@Entity
public class Student {

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private String age;

    @Transient
    private String info;

    @Generated(hash = 943511856)
    public Student(Long _id, @NotNull String age) {
        this._id = _id;
        this.age = age;
    }

    @Generated(hash = 1556870573)
    public Student() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }


    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}

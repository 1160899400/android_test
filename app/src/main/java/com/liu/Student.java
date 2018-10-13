package com.liu;

import android.provider.Settings;

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

    /**
     * @Transient 类似于transient关键字
     * 该变量不会被序列化，也意味着不会生成对应的数据库字段
     */
    @Transient
    private String info;

    

    public Student() {
    }

    @Generated(hash = 943511856)
    public Student(Long _id, @NotNull String age) {
        this._id = _id;
        this.age = age;
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

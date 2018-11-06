package com.liu.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * @author HZLI02
 * @date 2018/8/21
 */

@Entity(active = false)
public class Student {

    @Id(autoincrement = true)
    public Long id;

    @NotNull
    private String age;

    /**
     * @Transient 类似于transient关键字
     * 该变量不会被序列化，也意味着不会生成对应的数据库字段
     */
//    @Transient
    private String info;


    @Generated(hash = 2089105256)
    public Student(Long id, @NotNull String age, String info) {
        this.id = id;
        this.age = age;
        this.info = info;
    }

    @Generated(hash = 1556870573)
    public Student() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

}

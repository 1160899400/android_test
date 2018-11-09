package com.liu.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Unique;

import com.liu.dao.DaoSession;
import com.liu.dao.StudentDao;

/**
 * @author HZLI02
 * @date 2018/8/21
 */

@Entity(active = true)
public class Student {

    @Id(autoincrement = true)
//    @NotNull
//    @Unique
    public Long id;

    @NotNull
    private String age;

    /**
     * @Transient 类似于transient关键字
     * 该变量不会被序列化，也意味着不会生成对应的数据库字段
     */
//    @Transient
    @Property(nameInDb = "info")
    private String te;

    @Property()
    @Convert(converter = Converter1.class, columnType = Integer.class)
    private TestConvert testConvert;

    @NotNull
    @Property(nameInDb = "teacher_id")
    private Long teacherId;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1943931642)
    private transient StudentDao myDao;

    @Generated(hash = 520296122)
    public Student(Long id, @NotNull String age, String te, TestConvert testConvert,
            @NotNull Long teacherId) {
        this.id = id;
        this.age = age;
        this.te = te;
        this.testConvert = testConvert;
        this.teacherId = teacherId;
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

    public String getTe() {
        return this.te;
    }

    public void setTe(String te) {
        this.te = te;
    }

    public Long getTeacherId() {
        return this.teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public TestConvert getTestConvert() {
        return this.testConvert;
    }

    public void setTestConvert(TestConvert testConvert) {
        this.testConvert = testConvert;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1701634981)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getStudentDao() : null;
    }

}

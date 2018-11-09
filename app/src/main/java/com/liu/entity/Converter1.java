package com.liu.entity;

import org.greenrobot.greendao.converter.PropertyConverter;

/**
 * @author Hongzhi.Liu
 * @date 2018/11/8
 */
public class Converter1 implements PropertyConverter<TestConvert,Integer> {


    @Override
    public TestConvert convertToEntityProperty(Integer databaseValue) {
        return new TestConvert();
    }

    @Override
    public Integer convertToDatabaseValue(TestConvert entityProperty) {
        return 10;
    }
}

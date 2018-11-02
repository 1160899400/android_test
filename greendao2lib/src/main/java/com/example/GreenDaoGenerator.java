package com.example;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * test GreenDao 2.x manual generation of code
 */
public class GreenDaoGenerator {

    //version of database
    private static int version = 1;

    //path of generated entity
    private static String outputEntityPackage = "com.liu.entity";

    //path of generated dao/session
    private static String outputDaoPackage = "com.liu.dao";

    //relative path of java source directory of root project
    private static String outUri = "./app/src/main/java";

    public static void main(String[] args) throws Exception{
        Schema schema = new Schema(version,outputEntityPackage);
        schema.setDefaultJavaPackageDao(outputDaoPackage);
        addEntity(schema);
        new DaoGenerator().generateAll(schema,outUri);
    }

    public static void addEntity(Schema schema){
        //it will create entity class with class name 'Student'
        Entity entity = schema.addEntity("Student");
        //without setTableName(tableName),the table name will be the name of entity class name
        entity.setTableName("student");
        //add id property to table,id increase automatically
        entity.addIdProperty().autoincrement();
        entity.addStringProperty("name").notNull();

    }
}

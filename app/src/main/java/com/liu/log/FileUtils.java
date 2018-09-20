package com.liu.log;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Hongzhi.Liu 2014302580200@whu.edu.cn
 * @date 2018/9/19
 */
public class FileUtils {


    private static final String TAG = "FileUtils";

    /**
     * 在父目录下创建一个文件
     * 若父目录不存在，则创建该目录
     * 若文件存在则不作处理，不存在则创建
     * @return 创建成功则返回true，否则返回false
     */
    public static boolean createFile(String parentPath, String fileName) {
        File parentDirPath = new File(parentPath);
        File file = new File(parentPath, fileName);
        if (!parentDirPath.exists()) {
            boolean mkDirResult = parentDirPath.mkdirs();
            Log.d(TAG, "init dir:   init dir result: " + mkDirResult);
        } else {
            Log.d(TAG, "init dir:   dir already exist");
        }
        if (file.exists() && !file.isDirectory()) {
            Log.d(TAG, "init file:   file already exist");
        } else {
            try {
                boolean mkFileResult = file.createNewFile();
                Log.d(TAG, "init file:   int file result:  " + mkFileResult);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 在父目录下创建子目录
     * 若父目录不存在，则创建父目录
     *
     * @return 创建成功则返回true，否则返回false
     */
    public static boolean createDirectory(String parentPath, String dirName) {
        File dir = new File(parentPath, dirName);
        return dir.mkdirs();
    }

    /**
     * 返回写的结果
     *
     * @param is
     * @return
     */
//    public static boolean writeFile(InputStream is, String filePath) {
//
//
//    }
}

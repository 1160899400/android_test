package com.liu.log;

/**
 * @author Hongzhi.Liu 2014302580200@whu.edu.cn
 * @date 2018/9/17
 */
public interface LogConstant {

    /**
     * 当前logService
     */
    public final String LOG_SERVICE_TAG = "LogService";

    /**
     * sdcard/Android/data/package/files下的Log总目录
     */
    public final String LOG_DIR = "Log";

    /**
     * Log总目录下的子目录，存放所有log
     */
    public final String ALL_LOG_DIR= "AllLog";

    /**
     * Service的log
     */
    public final String SERVICE_LOG = "MyLogService.log";

    public static final int BUFFER_SIZE = 8192;
    public static final byte[] BUFFER = new byte[BUFFER_SIZE];

}

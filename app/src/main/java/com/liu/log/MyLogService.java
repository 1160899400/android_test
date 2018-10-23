package com.liu.log;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Hongzhi.Liu
 * @date 2018/9/17
 */
public class MyLogService extends IntentService {
    private static final String TAG = "MyLogService";

    /**
     * Log和AllLog目录
     */
    private String logPath;
    private String allLogPath;
    private boolean writingLog = true;

    /**
     * log文件名
     */
    private String logFileName;

    /**
     * 记录logService服务报错log文件路径
     */
    private String logServiceLogPath;

    private OutputStreamWriter serviceLogOutput;
    private Process process;

    /**
     * 日志名称格式
     */
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat logFileNameFormat = new SimpleDateFormat("yyyy-MM-dd");

    //唤醒
    private PowerManager.WakeLock wakeLock;


    //切换日志文件action
    private static String SWITCH_LOG_FILE_ACTION = "SWITCH_LOG_FILE_ACTION";

    public MyLogService() {
        super(TAG);
    }

    /**
     * 每次开启service时，都会调用一次该方法，用于初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Log start");
        init();
        new LogCollectorThread().start();
    }

    /**
     * 开始将log输出到file
     * 开始收集日志信息
     */
    private void beginTrace() {
        List<String> commandList = new ArrayList<>();
        commandList.add("logcat");
//        commandList.add("-d");
        //-f参数指明输出文件的路径
        commandList.add("-f");

        commandList.add(getLogPath());
        commandList.add("-b");
        commandList.add("main");
        commandList.add("-b");
        commandList.add("system");
        //-v参数为time，表明log格式为time格式
        commandList.add("-v");
        commandList.add("time");
        //过滤条件
        commandList.add("*:V");
        // 过滤所有的错误信息
        //commandList.add("*:E");
        // 过滤指定TAG的信息
        // commandList.add("MyAPP:V");
        // commandList.add("*:S");
        try {
            process = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
            recordLogServiceLog("start collecting the log,and log path is:" + getLogPath());
//            startWriteLogFile(process.getInputStream(), getLogPath());
        } catch (Exception e) {
            e(TAG, "CollectorThread == >" + e.getMessage());
            recordLogServiceLog("CollectorThread == >" + e.getMessage());
        }
        
    }


    public void startWriteLogFile(InputStream is, String filePath) {
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            byte[] buffer = new byte[LogConstant.BUFFER_SIZE];
            bufferedInputStream = new BufferedInputStream(is);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath, true));
            while (!(bufferedInputStream.read(buffer) == -1)) {
                bufferedInputStream.read(buffer);
                bufferedOutputStream.write(buffer);
            }
            bufferedOutputStream.flush();
            bufferedInputStream.close();
            bufferedOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                bufferedInputStream.close();
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 手动停止log输出
     */
    public void stopWriteLogFile() {
        writingLog = false;
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


    /**
     * 销毁时调用该方法
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWriteLogFile();
        recordLogServiceLog("LogService onDestroy");
        if (serviceLogOutput != null) {
            try {
                serviceLogOutput.close();
                serviceLogOutput = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (process != null) {
            process.destroy();
        }
    }


    private void init() {
        //尝试在sdcard中创建log文件夹
        createLogDir();
        //本服务产生的日志的路径，记录日志服务开启失败信息
        logServiceLogPath = logPath + File.separator + LogConstant.SERVICE_LOG;
        try {
            //true 表示可以接着写入
            OutputStream outputStream = new FileOutputStream(logServiceLogPath, true);
            serviceLogOutput = new OutputStreamWriter(outputStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        //获取PowerManager管理者
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }
        //当前的日志记录类型
        Log.i(TAG, "LogService onCreate");
    }


    /**
     * 创建存放所有log的文件夹
     */
    private void createLogDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //parentPath为sdcard/Android/data下本应用的Files目录
            String parentDir = getExternalFilesDir(null).getAbsolutePath();
            FileUtils.createDirectory(parentDir, LogConstant.LOG_DIR);
            logPath = parentDir + File.separator + LogConstant.LOG_DIR;
            FileUtils.createDirectory(logPath, LogConstant.ALL_LOG_DIR);
            allLogPath = logPath + File.separator + LogConstant.ALL_LOG_DIR;
            logFileName = logFileNameFormat.format(new Date()) + ".log";
//            FileUtils.createFile(allLogPath, logFileName);
        } else {
            Log.e(TAG, "create directory fail");
        }
    }


    /**
     * 日志收集
     * 1.清除日志缓存
     * 2.杀死应用程序已开启的Logcat进程，防止多个进程写入一个日志文件
     * 3.开启日志收集进程
     */
    class LogCollectorThread extends Thread {
        LogCollectorThread() {
            super("LogCollectorThread");
            Log.d(TAG, "LogCollectorThread is create");
        }

        @SuppressLint("WakelockTimeout")
        @Override
        public void run() {
            try {
                //唤醒手机
                wakeLock.acquire();
                clearLogCache();
                List<String> orgProcessList = getAllProcess();
                Log.d(TAG,"collect log");
                List<ProcessInfo> processInfoList = getProcessInfoList(orgProcessList);
                //关闭由本程序开启的logcat进程
                killLogcatPro(processInfoList);
                beginTrace();
                //释放
                wakeLock.release();
            } catch (Exception e) {
                e.printStackTrace();
                recordLogServiceLog(Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 每次记录日志之前先清除日志的缓存, 不然会在两个日志文件中记录重复的日志
     */
    private void clearLogCache() {
        Process pro = null;
        List<String> commandList = new ArrayList<>();
        commandList.add("logcat");
        commandList.add("-c");
        try {
            pro = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
            StreamConsumer errorGobbler = new StreamConsumer(pro.getErrorStream());
            StreamConsumer outputGobbler = new StreamConsumer(pro.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            if (pro.waitFor() != 0) {
                Log.e(TAG, " clearLogCache proc.waitFor() != 0");
                recordLogServiceLog("clearLogCache clearLogCache proc.waitFor() != 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "clearLogCache failed", e);
            recordLogServiceLog("clearLogCache failed");
        } finally {
            try {
                if (pro != null) {
                    pro.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "clearLogCache failed", e);
                recordLogServiceLog("clearLogCache failed");
            }
        }
    }

    /**
     * 关闭由本程序开启的logcat进程：
     * 根据用户名称杀死进程(如果是本程序进程开启的Logcat收集进程那么两者的USER一致)
     * 如果不关闭会有多个进程读取logcat日志缓存信息写入日志文件
     * @param allPro allPro
     */
    private void killLogcatPro(List<ProcessInfo> allPro) {
        if (process != null) {
            process.destroy();
        }
        String packName = this.getPackageName();
        //获取本程序的用户名称
        String myUser = getAppUser(packName, allPro);
        for (ProcessInfo processInfo : allPro) {
            if (processInfo.name.toLowerCase().equals("logcat") && processInfo.user.equals(myUser)) {
                android.os.Process.killProcess(Integer.parseInt(processInfo.pid));
            }
        }
    }

//    /**
//     * 获取本程序的用户名称
//     *
//     * @param packName   packName
//     * @param allProList allProList
//     * @return 程序名称
//     */
    private String getAppUser(String packName, List<ProcessInfo> allProList) {
        for (ProcessInfo processInfo : allProList) {
            if (processInfo.name.equals(packName)) {
                return processInfo.user;
            }
        }
        return null;
    }

    /**
     * 根据ps命令得到的内容获取PID，User，name等信息
     *
     * @param orgProcessList orgProcessList
     * @return 集合
     */
    private List<ProcessInfo> getProcessInfoList(List<String> orgProcessList) {
        List<ProcessInfo> proInfoList = new ArrayList<>();
        for (int i = 1; i < orgProcessList.size(); i++) {
            String processInfo = orgProcessList.get(i);
            String[] proStr = processInfo.split(" ");
            // USER PID PPID VSIZE RSS WCHAN PC NAME
            // root 1 0 416 300 c00d4b28 0000cd5c S /init
            List<String> orgInfo = new ArrayList<>();
            for (String str : proStr) {
                if (!"".equals(str)) {
                    orgInfo.add(str);
                }
            }
            if (orgInfo.size() == 9) {
                ProcessInfo pInfo = new ProcessInfo();
                pInfo.user = orgInfo.get(0);
                pInfo.pid = orgInfo.get(1);
                pInfo.ppid = orgInfo.get(2);
                pInfo.name = orgInfo.get(8);
                proInfoList.add(pInfo);
            }
        }
        return proInfoList;
    }

    /**
     * 运行PS命令得到进程信息
     *
     * @return USER PID PPID VSIZE RSS WCHAN PC NAME
     * root 1 0 416 300 c00d4b28 0000cd5c S /init
     */
    private List<String> getAllProcess() {
        List<String> orgProList = new ArrayList<>();
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec("ps");
            StreamConsumer outputConsumer = new StreamConsumer(pro.getInputStream(), orgProList);
            outputConsumer.start();
            if (pro.waitFor() != 0) {
                Log.e(TAG, "getAllProcess pro.waitFor() != 0");
                recordLogServiceLog("getAllProcess pro.waitFor() != 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllProcess failed", e);
            recordLogServiceLog("getAllProcess failed");
        } finally {
            try {
                if (pro != null) {
                    pro.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "getAllProcess failed", e);
                recordLogServiceLog("getAllProcess failed");
            }
        }
        return orgProList;
    }


    /**
     * 获得今日Log的绝对路径
     *
     * @return 路径
     */
    public String getLogPath() {
        Log.d(TAG, "Log stored in SDcard, the path is:" + allLogPath + File.separator + logFileName);
        return allLogPath + File.separator + logFileName;
    }

    /**
     * 这个注意是记录错误的日志
     * 记录日志服务的基本信息 防止日志服务有错，在LogCat日志中无法查找
     * 此日志名称为Log.log
     *
     * @param msg msg
     */
    private void recordLogServiceLog(String msg) {
        if (serviceLogOutput != null) {
            try {
                serviceLogOutput.write((msg + "\n"));
                serviceLogOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }


    class ProcessInfo {
        public String user;
        private String pid;
        private String ppid;
        public String name;

        @Override
        public String toString() {
            return "ProcessInfo{" +
                    "user='" + user + '\'' +
                    ", pid='" + pid + '\'' +
                    ", ppid='" + ppid + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }


    class StreamConsumer extends Thread {
        InputStream is;
        List<String> list;

        StreamConsumer(InputStream is) {
            this.is = is;
        }

        StreamConsumer(InputStream is, List<String> list) {
            this.is = is;
            this.list = list;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    if (list != null) {
                        list.add(line);
                    }
                }
                is.close();
                isr.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    private static void v(String tag, String string) {
        Log.v(tag, string);
    }

    private static void d(String tag, String string) {
        Log.d(tag, string);
    }

    private static void i(String tag, String string) {
        Log.i(tag, string);
    }

    private static void w(String tag, String string) {
        Log.w(tag, string);
    }

    private static void e(String tag, String string) {
        Log.e(tag, string);
    }

    private static void wtf(String tag, String string) {
        Log.wtf(tag, string);
    }
}

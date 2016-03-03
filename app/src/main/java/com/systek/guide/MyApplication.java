package com.systek.guide;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloader;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.receiver.NetworkStateChangedReceiver;

import java.util.Iterator;


/**
 * Created by Qiang on 2015/12/30.
 *
 *
 */
public class MyApplication extends Application implements IConstants{

    private static MyApplication myApplication;
    /*软件是否开发完毕*/
    public static final boolean isRelease = false;

    public MediaServiceManager getmServiceManager() {
        return mServiceManager;
    }

    public void setmServiceManager(MediaServiceManager mServiceManager) {
        this.mServiceManager = mServiceManager;
    }

    public MediaServiceManager mServiceManager;

    public static int getCurrentNetworkType() {
        return currentNetworkType;
    }

    public static void setCurrentNetworkType(int currentNetworkType) {
        MyApplication.currentNetworkType = currentNetworkType;
    }

    /*当前网络状态*/
    public static int currentNetworkType= INTERNET_TYPE_NONE;
    private BluetoothManager bluetoothManager;


    @Override
    public void onCreate() {
        super.onCreate();
        //初始化下载框架
        FileDownloader.init(this);
        if (!isSameAppName()) {return;}
        // 防止重启两次,非相同名字的则返回
        mServiceManager = MediaServiceManager.getInstance(getApplicationContext());
        mServiceManager.connectService();
        myApplication = this;
        registerNetWorkReceiver();
        initBlueTooth();
        //初始化检查内存泄露
        //LeakCanary.install(this);

    }




    private void initBlueTooth() {
        bluetoothManager =BluetoothManager.newInstance(this);
        bluetoothManager.initBeaconSearcher();
    }
    public  static Context getAppContext(){
        return myApplication.getApplicationContext();
    }

    public void registerNetWorkReceiver(){
        NetworkStateChangedReceiver networkStateChangedReceiver = new NetworkStateChangedReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkStateChangedReceiver,intentFilter);
    }

    /**
     * 判断是否为相同app名
     *
     * @return
     */
    private boolean isSameAppName() {
        int pid = android.os.Process.myPid();
        String processAppName = getProcessAppName(pid);
        return !(TextUtils.isEmpty(processAppName) || !processAppName.equalsIgnoreCase(getPackageName()));
    }
    /**
     * 获取processAppName
     *
     * @param pid
     * @return
     */
    private String getProcessAppName(int pid) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        Iterator<ActivityManager.RunningAppProcessInfo> iterator = activityManager.getRunningAppProcesses().iterator();
        while (iterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo =iterator.next();
            try {
                if (runningAppProcessInfo.pid == pid) {
                    return runningAppProcessInfo.processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    /*退出程序*/
    public  void exit() {
        bluetoothManager.disConnectBluetoothService();
        bluetoothManager=null;
        mServiceManager.disConnectService();
        DataBiz.clearTempValues(getAppContext());
        System.exit(0);
    }

    /**
     * 获取application对象
     *
     * @return JApplication
     */
    public static MyApplication get() {
        return myApplication;
    }

}

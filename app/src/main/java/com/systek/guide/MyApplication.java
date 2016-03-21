package com.systek.guide;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloader;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.receiver.NetworkStateChangedReceiver;
import com.systek.guide.utils.ExceptionUtil;

import java.lang.reflect.Field;


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
        //setTypeface();
        //初始化下载框架
        FileDownloader.init(this);
        // 防止重启两次,非相同名字的则返回
        if (!isSameAppName()) {return;}
        myApplication = this;
        registerNetWorkReceiver();
        initBlueTooth();
        //初始化检查内存泄露
        //LeakCanary.install(this);
        //FontUtils.getInstance().replaceSystemDefaultFontFromAsset(this, "fonts/aaa.ttf");
    }

    public void initMediaService() {
        mServiceManager = MediaServiceManager.getInstance(getApplicationContext());
        mServiceManager.connectService();
    }


    public void setTypeface(){
        Typeface  typeFace = Typeface.createFromAsset(getAssets(), "fonts/aaa.ttf");
        try
        {
            Field field = Typeface.class.getDeclaredField("SERIF");
            field.setAccessible(true);
            field.set(null, typeFace);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }




    public void initBlueTooth() {
        if(bluetoothManager==null){
            bluetoothManager =BluetoothManager.newInstance(this);
        }
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
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
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
        if(bluetoothManager!=null){
            bluetoothManager.disConnectBluetoothService();
        }
        if(mServiceManager!=null){
            mServiceManager.disConnectService();
        }
        bluetoothManager=null;
        //ImageLoader.getInstance().clearMemoryCache();
        DataBiz.clearTempValues(getAppContext());
        android.os.Process.killProcess(android.os.Process.myPid());
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

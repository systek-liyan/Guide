package com.systek.guide;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloader;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.receiver.NetworkStateChangedReceiver;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;

import java.io.File;
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

    /*当前网络状态*/
    public static int currentNetworkType= INTERNET_TYPE_NONE;
    private BluetoothManager bluetoothManager;

    @Override
    public void onCreate() {
        super.onCreate();
        //setTypeface();
        //初始化下载框架
        FileDownloader.init(this);
        ImageLoader.getInstance().init(newConfig());
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

    private  ImageLoaderConfiguration newConfig(){
        // See the sample project how to use ImageLoader correctly.
        Context context= getApplicationContext();
        //File cacheDir = StorageUtils.getCacheDirectory(context);
        String  cacheDirPath = getFilesDir().getAbsolutePath()+"/guide";
        File cacheDir=new File(cacheDirPath);
        if(!cacheDir.isDirectory()){
            if(cacheDir.mkdirs()) ;
        }
        LogUtil.i("ZHANG", "cacheDirPath==" + cacheDir.getAbsolutePath());
        return new ImageLoaderConfiguration
                .Builder(context)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                        //.diskCacheExtraOptions(480, 800, null)// Can slow ImageLoader, use it carefully (Better don't use it)/设置缓存的详细信息，最好不要设置这个
                        //.taskExecutor(...)
                        //.taskExecutorForCachedImages(...)
                .threadPoolSize(3) // 线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiskCache(cacheDir)) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .imageDecoder(new BaseImageDecoder(true)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                        //.writeDebugLogs()
                .build();
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

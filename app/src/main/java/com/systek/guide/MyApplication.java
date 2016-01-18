package com.systek.guide;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.systek.guide.biz.DataBiz;
import com.systek.guide.manager.MediaServiceManager;
import com.systek.guide.receiver.NetworkStateChangedReceiver;
import com.systek.guide.utils.ExceptionUtil;

import java.util.Iterator;


/**
 * Created by Qiang on 2015/12/30.
 */
public class MyApplication extends Application implements IConstants{

    private static MyApplication myApplication;
    /*软件是否开发完毕*/
    public static final boolean isRelease = false;
    public MediaServiceManager mServiceManager;
    /*当前网络状态*/
    public static int currentNetworkType= INTERNET_TYPE_NONE;
    //public List<ExhibitBean> totalExhibitBeanList; /**展品总集合*/
   // public List<ExhibitBean> currentExhibitBeanList;/**当前要加入展品（蓝牙扫描周边等）集合*/
   // public String currentMuseumId;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        if (!isSameAppName()) {return;}
        // 防止重启两次,非相同名字的则返回
        mServiceManager = MediaServiceManager.getInstance(getApplicationContext());
        mServiceManager.connectService();
        //initDrawerImageLoader();
        registerNetWorkReceiver();
    }

    /*private void initDrawerImageLoader() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }
            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                return super.placeholder(ctx, tag);
            }
        });
    }*/
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
    private void initBaiduSDK() {
        try {
            // 初始化百度地图
            //SDKInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }
    /*退出程序*/
    public  void exit() {
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

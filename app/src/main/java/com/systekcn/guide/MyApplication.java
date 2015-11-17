package com.systekcn.guide;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.baidu.mapapi.SDKInitializer;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.NetworkUtil;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.manager.MediaServiceManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Qiang on 2015/10/22.
 */
public class MyApplication extends Application implements IConstants{
    /*所有的activity都放入此集合中*/
    public static ArrayList<Activity> listActivity = new ArrayList<>();
    /*软件是否开发完毕*/
    public static final boolean isRelease = false;
    /*当前网络状态*/
    public static int currentNetworkType= INTERNET_TYPE_NONE;
    public static final int GUIDE_MODEL_AUTO=2;
    public static final int GUIDE_MODEL_HAND=3;
    public static int guideModel=GUIDE_MODEL_HAND;
    public  MediaServiceManager mServiceManager;

    /**当前展品*/
    public ExhibitBean currentExhibitBean;
    /**展品总集合*/
    public List<ExhibitBean> totalExhibitBeanList;
    /**当前要加入展品（蓝牙扫描周边等）集合*/
    public List<ExhibitBean> currentExhibitBeanList;
    /**看过的展品集合*/
    public List<ExhibitBean> everSeenExhibitBeanList;
    /**专题展品集合*/
    public List<ExhibitBean> topicExhibitBeanList;
    /***/
    public List<ExhibitBean> recordExhibitBeanList;
    /**附近展品框中显示的展品*/
    public List<ExhibitBean> nearlyExhibitBeanList;

    public String currentMuseumId;
    public String currentBeaconId;
    public String currentExhibitId;

    private MyApplication application;

    public final int  DATA_FROM_HOME =1;
    public final int  DATA_FROM_COLLECTION =2;
    public final int DATA_FROM_BEACON =3;
    public int dataFrom;

    public boolean isTopicOpen=false;

    @Override
    public void onCreate() {
        super.onCreate();
        application=this;
        // 防止重启两次,非相同名字的则返回
        if (!isSameAppName()) {
            return;
        }
        try{
            initConfig();
            initBaiduSDK();
            currentExhibitBeanList=new ArrayList<>();
            totalExhibitBeanList=new ArrayList<>();
            everSeenExhibitBeanList=new ArrayList<>();
            topicExhibitBeanList=new ArrayList<>();
            recordExhibitBeanList=new ArrayList<>();
            nearlyExhibitBeanList=new ArrayList<>();
            mServiceManager = new MediaServiceManager(getApplicationContext());
            mServiceManager.connectService();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    public void refreshData(){
        if(currentExhibitBean!=null){
            currentExhibitId=currentExhibitBean.getId();
        }
        currentMuseumId=currentExhibitBean.getMuseumId();
        currentBeaconId=currentExhibitBean.getBeaconId();
    }

    public String getCurrentBeaconId(){
        if(currentExhibitBean!=null){
            currentBeaconId=currentExhibitBean.getBeaconId();
            return currentBeaconId;
        }else{
            return "";
        }
    }

    private void initBaiduSDK() {
        try {
            // 初始化百度地图
            SDKInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    public  String getCurrentLyricDir(){
        return LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_LYRIC+"/";
    }
    public  String getCurrentMuseumId(){
        if(currentExhibitBean!=null){
            return currentExhibitBean.getMuseumId();
        }else{
            return "";
        }
    }
    public  String getCurrentImgDir(){
        return LOCAL_ASSETS_PATH+currentExhibitBean.getMuseumId()+"/"+LOCAL_FILE_TYPE_IMAGE+"/";
    }
    private void initConfig() {
        NetworkUtil.checkNet(this);
        SharedPreferences settings = getSharedPreferences(APP_SETTING, 0);
        String mGuideModel=settings.getString(GUIDE_MODEL_KEY, IConstants.GUIDE_MODEL_HAND);
        LogUtil.i("ZHANG", mGuideModel);
        if(mGuideModel.equals(IConstants.GUIDE_MODEL_HAND)){
            guideModel=GUIDE_MODEL_HAND;
        }else if(mGuideModel.equals(IConstants.GUIDE_MODEL_AUTO)){
            guideModel=GUIDE_MODEL_AUTO;
        }
        LogUtil.i("测试数据模式", "guideModel----" + guideModel);
    }

    /*退出程序 */
    public  void exit() {
        for (Activity activity : listActivity) {
            if(activity!=null){
                try {
                    activity.finish();
                    LogUtil.i("退出", activity.toString() + "退出了");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }
        mServiceManager.disConnectService();
        System.exit(0);
    }

    /**
     * 获取application对象
     *
     * @return JApplication
     */
    public  MyApplication get() {
        return application;
    }

    /**
     * 判断是否为相同app名
     *
     * @return
     */
    private boolean isSameAppName() {
        int pid = android.os.Process.myPid();
        String processAppName = getProcessAppName(pid);
        if (TextUtils.isEmpty(processAppName) || !processAppName.equalsIgnoreCase(getPackageName())) {
            return false;
        }
        return true;
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
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo = iterator
                    .next();
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

    public static Context getContext(){
        return getContext();
    }

}

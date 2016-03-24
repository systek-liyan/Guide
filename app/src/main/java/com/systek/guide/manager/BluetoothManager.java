package com.systek.guide.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.systek.beacon.core.BeaconSearcher;
import com.systek.beacon.listener.BeaconsListener;
import com.systek.beacon.model.SystekBeacon;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Qiang on 2015/12/11.
 *
 * 蓝牙管理类
 */
public class BluetoothManager implements IConstants {


    public static final boolean NOT_IN_GUIDE_VIEW=true;

    private boolean isInGuide=NOT_IN_GUIDE_VIEW;

    public void setIsInGuide(boolean isInGuide) {
        this.isInGuide = isInGuide;
    }

    private Context context;
    private static BluetoothManager bluetoothManager;
    /*蓝牙扫描对象*/
    private BeaconSearcher mBeaconSearcher;

    public String getCurrentMuseumId() {
        return currentMuseumId;
    }

    private String currentMuseumId;

    public BluetoothManager(Context context) {
        this.context = context;
    }

    public static BluetoothManager newInstance(Context c){
        if(bluetoothManager==null){
            synchronized (MyApplication.class){
                if(bluetoothManager==null){
                    bluetoothManager=new BluetoothManager(c.getApplicationContext());
                }
            }
        }
        return bluetoothManager;
    }

    public void disConnectBluetoothService(){

        if (mBeaconSearcher != null) {
            mBeaconSearcher.closeSearcher();
            mBeaconSearcher=null;
        }
        context=null;
        bluetoothManager=null;
    }

    public void initBeaconSearcher() {
        if (mBeaconSearcher == null) {
            // 设定用于展品定位的最小停留时间(ms)
            mBeaconSearcher = BeaconSearcher.getInstance(context);
            // NearestBeacon.GET_EXHIBIT_BEACON：展品定位beacon
            // NearestBeacon.GET_EXHIBIT_BEACON：游客定位beacon。可以不用设置上述的最小停留时间和最小距离
            //mBeaconSearcher.setMin_stay_milliseconds(1500);
            // 设定用于展品定位的最小距离(m)
            // mBeaconSearcher.setExhibit_distance(0.5);
            // 设置获取距离最近的beacon类型
            // 当蓝牙打开时，打开beacon搜索器，开始搜索距离最近的Beacon
            // 设置beacon监听器
            mBeaconSearcher.setNearestBeaconListener(onNearestBeaconListener);
            // 添加导游模式切换监听
            if (mBeaconSearcher != null && mBeaconSearcher.checkBLEEnable()) {
                mBeaconSearcher.openSearcher();
            } else {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                        mBeaconSearcher.openSearcher();
                    }
                }
            }
        } else {
            if (mBeaconSearcher.checkBLEEnable()) {
                mBeaconSearcher.openSearcher();
            }
        }
    }


    /**实现beacon搜索监听，或得BeaconSearcher搜索到的beacon对象*/

    private BeaconsListener onNearestBeaconListener=new BeaconsListener(){

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        @Override
        public void getNearestBeacon(Beacon beacon) {

        }

        @Override
        public void getBeacons(List<SystekBeacon> list) {

        }


        /**
         * 获得 beacon列表 解析beacon像播放控制发送数据
         * @param isSwitch switch
         * @param refresh refresh
         * @param list SystekBeacon
         */
        @Override
        public void getBeacons(boolean isSwitch, boolean refresh, List<SystekBeacon> list) {
            if(list!=null){
                DataBiz.saveTempValue(MyApplication.get(), SP_IS_IN_MUSEUM, true);
            }else{
                return;
            }
            if(!isInGuide&&!refresh){
                return;
            }
            executor.execute(new MyBeaconTask(isSwitch,list));
        }

        @Override
        public void getBeacons(boolean b, boolean b1, List<SystekBeacon> list, String s) {

        }


    };

    /**
     * 根据beacon集合找出展品集合
     * @param beaconBeans
     * @return
     */
    private static List<ExhibitBean> searchExhibitByBeacon(List<BeaconBean> beaconBeans) {

        List<ExhibitBean> exhibitList= new ArrayList<>();

        if(beaconBeans==null||beaconBeans.size()==0){return exhibitList;}
        String museumId= beaconBeans.get(0).getMuseumId();
            /*遍历beacon结合，获得展品列表*/
        for(int i=0;i<beaconBeans.size();i++){
            BeaconBean beacon=beaconBeans.get(i);
            if(beacon==null){continue;}
            String beaconId=beacon.getId();
            List<ExhibitBean> tempList= DataBiz.getExhibitListByBeaconId(museumId, beaconId);
            if(tempList==null||tempList.size()==0){continue;}
            for(ExhibitBean beaconBean:tempList){
                beaconBean.setDistance(beaconBeans.get(i).getDistance());
            }
                /*去重复*/
            exhibitList.removeAll(tempList);
            exhibitList.addAll(tempList);
        }
        return exhibitList;
    }

    /**
     * 根据beaconforsort集合找出beacon集合
     * @param systekBeacons beacon结合
     * @param dis 规定距离内beacon
     * @return
     */
    private static List<BeaconBean> changeToBeaconList(List<SystekBeacon> systekBeacons,double dis) {
        List <BeaconBean> beaconBeans=new ArrayList<>();
        for (int i = 0; i < systekBeacons.size(); i++) {
            SystekBeacon systekBeacon=systekBeacons.get(i);

            String major = systekBeacon.getMajor();
            String minor = systekBeacon.getMinor();
            //根据beacon的minor和major参数获得beacon对象
            BeaconBean beaconBean= DataBiz.getBeaconMinorAndMajor(minor, major);
            if(beaconBean==null){continue;}
            beaconBean.setDistance(systekBeacon.getDistance());
            //设定距离范围，暂定小于1米则放入列表
          /* if(systekBeacon.getDistance()<dis){
            }*/
            beaconBeans.add(beaconBean);
        }
        return beaconBeans;
    }

    private static class MyBeaconTask implements Runnable{

        boolean isSwitch;
        List<SystekBeacon> list;

        public MyBeaconTask(boolean isSwitch, List<SystekBeacon> list) {
            this.isSwitch = isSwitch;
            this.list = list;
        }


        @Override
        public void run() {
            DataBiz.saveTempValue(MyApplication.get(), SP_IS_IN_MUSEUM, true);
            List<BeaconBean> beaconBeanList=changeToBeaconList(list,20.0);
            if(beaconBeanList.size()==0){return;}
            BeaconBean beacon =beaconBeanList.get(0);
            if(beacon==null){return;}
            //currentMuseumId =beacon.getMuseumId();
            List<ExhibitBean> exhibitBeansList=searchExhibitByBeacon(beaconBeanList);
            if (exhibitBeansList==null||exhibitBeansList.size()==0) {return;}
            //将最近列表转为json发送广播
            String json= JSON.toJSONString(exhibitBeansList);
            Intent intent =new Intent();
            intent.setAction(INTENT_EXHIBIT_LIST);
            intent.putExtra(INTENT_EXHIBIT_LIST, json);
            intent.putExtra(INTENT_SWITCH_FLAG,isSwitch);
            MyApplication.get().sendBroadcast(intent);
        }
    }


}

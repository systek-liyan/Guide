package com.systekcn.guide.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.systekcn.guide.IConstants;
import com.systekcn.guide.beacon.BeaconForSort;
import com.systekcn.guide.beacon.BeaconSearcher;
import com.systekcn.guide.beacon.NearestBeacon;
import com.systekcn.guide.beacon.NearestBeaconListener;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.DataBiz;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/12/11.
 */
public class BluetoothManager implements IConstants {

    private Context context;
    //private MyApplication application;
    private static BluetoothManager bluetoothManager;
    private NearestBeaconListener nearestBeaconListener;

    public void setNearestBeaconListener(NearestBeaconListener nearestBeaconListener) {
        this.nearestBeaconListener = nearestBeaconListener;
    }

    public void setGetBeaconCallBack(GetBeaconCallBack getBeaconCallBack) {
        this.getBeaconCallBack = getBeaconCallBack;
    }

    private GetBeaconCallBack getBeaconCallBack;

    /*蓝牙扫描对象*/
    private BeaconSearcher mBeaconSearcher;

    private BluetoothManager(Context context) {
        this.context = context;
        //application=MyApplication.get();
    }

    public static BluetoothManager newInstance(Context c){
        if(bluetoothManager==null){
            bluetoothManager=new BluetoothManager(c);
        }
        return bluetoothManager;
    }

    public void disConnectBluetoothService(){

        if (mBeaconSearcher != null) {
            mBeaconSearcher.closeSearcher();
            mBeaconSearcher=null;
        }
        bluetoothManager=null;
    }

    public void initBeaconSearcher() {
        if (mBeaconSearcher == null) {
            // 设定用于展品定位的最小停留时间(ms)
            mBeaconSearcher = BeaconSearcher.getInstance(context);
            // NearestBeacon.GET_EXHIBIT_BEACON：展品定位beacon
            // NearestBeacon.GET_EXHIBIT_BEACON：游客定位beacon。可以不用设置上述的最小停留时间和最小距离
            mBeaconSearcher.setMin_stay_milliseconds(3000);
            // 设定用于展品定位的最小距离(m)
            mBeaconSearcher.setExhibit_distance(1.5);
            // 设置获取距离最近的beacon类型
            mBeaconSearcher.setNearestBeaconType(NearestBeacon.GET_EXHIBIT_BEACON);
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

    private BeaconSearcher.OnNearestBeaconListener onNearestBeaconListener=new BeaconSearcher.OnNearestBeaconListener(){

        BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(context);

        /*此方法为自动切换展品，暂时已不用*/
        @Override
        public void getNearestBeacon(int type,Beacon beacon) {
         /*   if (beacon != null) {
                LogUtil.i("ZHANG", beacon.getId2() + "   " + beacon.getId3());
                try{
                    Identifier major = beacon.getId2();
                    Identifier minor = beacon.getId3();
                    LogUtil.i("ZHANG","major===="+major+","+"minor==="+minor);
                    BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(context);
                    BeaconBean b = biz.getBeaconMinorAndMajor(minor, major);
                    if (b != null) {
                        if(getBeaconCallBack!=null){
                            getBeaconCallBack.getMuseumByBeaconCallBack(b);
                        }
                        String beaconId = b.getId();
                        LogUtil.i("ZHANG", beaconId);
                        if (beaconId != null && !(application.getCurrentBeaconId().equals(""))) {
                            if(!beaconId.equals(application.getCurrentBeaconId())){
                                List<ExhibitBean> nearlyExhibitsList = biz.getExhibitListByBeaconId(application.getCurrentMuseumId(), beaconId);
                                if (nearlyExhibitsList != null && nearlyExhibitsList.size() > 0) {
                                    //LogUtil.i("ZHANG", nearlyExhibitsList.toString());
                                    application.currentExhibitBeanList=nearlyExhibitsList;
                                    application.currentExhibitBean=nearlyExhibitsList.get(0);
                                    application.refreshData();
                                    //application.dataFrom=application.DATA_FROM_BEACON;// TODO: 2015/11/3
                                    Intent intent =new Intent();
                                    intent.setAction(ACTION_NOTIFY_CURRENT_EXHIBIT_CHANGE);
                                    context.sendBroadcast(intent);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }
            }*/
        }

        List<BeaconBean> beaconBeanList;
        List<ExhibitBean> exhibitBeansList;
        long recordTime=0;
        //int count=0;
        /**当接受到多个beacon时，根据beacon查找展品，更新附近列表*/
        @Override
        public void getNearestBeacons(int type, List<BeaconForSort> beaconsForSortList) {
            if(System.currentTimeMillis()-recordTime<2000){return;}
            recordTime=System.currentTimeMillis();
            if (beaconsForSortList == null||beaconsForSortList.size()<=0) {return;}
            beaconBeanList=new ArrayList<>();
            exhibitBeansList=new ArrayList<>();
            for (int i = 0; i < beaconsForSortList.size(); i++) {
                Beacon beacon = beaconsForSortList.get(i).getBeacon();//if(i==0){LogUtil.i("ZHANG", beacon.getId3());}
                double distance=beaconsForSortList.get(i).getDistance();
                Identifier major = beacon.getId2();
                Identifier minor = beacon.getId3();
                BeaconBean beaconBean=biz.getBeaconMinorAndMajor(minor, major);
                if(beaconBean!=null){
                    if(i==0&&nearestBeaconListener!=null){
                        nearestBeaconListener.nearestBeaconCallBack(beaconBean);
                        if(getBeaconCallBack!=null){
                            getBeaconCallBack.getMuseumByBeaconCallBack(beaconBean);
                        }
                    }
                    if(distance<2.0){// TODO: 2016/1/3
                        beaconBean.setDistance(distance);
                        beaconBeanList.add(beaconBean);
                    }
                }
            }
            if(beaconBeanList.size()==0){return;}
            String museumId= DataBiz.getCurrentMuseumId();
            if(TextUtils.isEmpty(museumId)){return;}
            for(int i=0;i<beaconBeanList.size();i++){
                String beaconId=beaconBeanList.get(i).getId();
                List<ExhibitBean> list= DataBiz.getExhibitListByBeaconId(museumId, beaconId);
                if(list==null||list.size()==0){continue;}
                for(ExhibitBean beaconBean:list){
                    beaconBean.setDistance(beaconBeanList.get(i).getDistance());
                }
                exhibitBeansList.removeAll(list);
                exhibitBeansList.addAll(list);
            }
            if(exhibitBeansList.size()==0){return;}
            String json=JSON.toJSONString(exhibitBeansList);
            Intent intent =new Intent();
            intent.setAction(INTENT_EXHIBIT_LIST);
            intent.putExtra(INTENT_EXHIBIT_LIST,json);
            context.sendBroadcast(intent);
        }
    };

    public interface GetBeaconCallBack{
        String getMuseumByBeaconCallBack(BeaconBean beaconBean);
    }
}

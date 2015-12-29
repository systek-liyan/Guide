package com.systekcn.guide.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.beacon.BeaconForSort;
import com.systekcn.guide.beacon.BeaconSearcher;
import com.systekcn.guide.beacon.NearestBeacon;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.listener.NearestBeaconListener;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/12/11.
 */
public class BluetoothManager implements IConstants{

    private Context context;
    private MyApplication application;
    private static BluetoothManager bluetoothManager;
    private NearestBeaconListener nearestBeaconListener;

    public void setNearestBeaconListener(NearestBeaconListener nearestBeaconListener) {
        this.nearestBeaconListener = nearestBeaconListener;
    }

    public void setGetBeaconCallBack(GetBeaconCallBack getBeaconCallBack) {
        this.getBeaconCallBack = getBeaconCallBack;
    }

    private GetBeaconCallBack getBeaconCallBack;

    /**蓝牙扫描对象*/
    private  BeaconSearcher mBeaconSearcher;

    private BluetoothManager(Context context) {
        this.context = context;
        application=MyApplication.get();
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
    /**
     * 实现beacon搜索监听，或得BeaconSearcher搜索到的beacon对象
     */
    long recordTime=0;
    private BeaconSearcher.OnNearestBeaconListener onNearestBeaconListener=new BeaconSearcher.OnNearestBeaconListener(){

        BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(context);

        /**此方法为自动切换展品，暂时已不用*/
        @Override
        public void getNearestBeacon(int type,Beacon beacon) {
            if (beacon != null) {
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
                                    application.dataFrom=application.DATA_FROM_BEACON;// TODO: 2015/11/3
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
            }
        }

        List<BeaconBean> beaconBeanList;
        List<ExhibitBean> exhibitBeansList;
        /**当接受到多个beacon时，根据beacon查找展品，更新附近列表*/
        @Override
        public void getNearestBeacons(int type, List<BeaconForSort> beaconsForSortList) {

                beaconBeanList=new ArrayList<>();
                exhibitBeansList=new ArrayList<>();
                if (beaconsForSortList != null&&beaconsForSortList.size()>0) {
                    for (int i = 0; i < beaconsForSortList.size(); i++) {
                        Beacon beacon = beaconsForSortList.get(i).getBeacon();
                        double distance=beaconsForSortList.get(i).getDistance();
                        Identifier major = beacon.getId2();
                        Identifier minor = beacon.getId3();
                        BeaconBean beaconBean=biz.getBeaconMinorAndMajor(minor, major);
                        if(beaconBean!=null){//&&distance<1.5
                            beaconBean.setDistance(distance);
                            beaconBeanList.add(beaconBean);
                        }
                    }
                    if(beaconBeanList.size()>0){
                        BeaconBean b=beaconBeanList.get(0);
                        if(nearestBeaconListener!=null){
                            nearestBeaconListener.nearestBeaconCallBack(b);
                        }
                        if(getBeaconCallBack!=null){
                            getBeaconCallBack.getMuseumByBeaconCallBack(b);
                        }
                        for(int i=0;i<beaconBeanList.size();i++){
                            String beaconId=beaconBeanList.get(i).getId();
                            List<ExhibitBean> list= biz.getExhibitListByBeaconId(application.getCurrentMuseumId(), beaconId);
                            if(list!=null&&list.size()>0){
                                for(ExhibitBean beaconBean:list){
                                    beaconBean.setDistance(beaconBeanList.get(i).getDistance());
                                }
                                exhibitBeansList.removeAll(list);
                                exhibitBeansList.addAll(list);
                            }
                        }
                    }

                    if(System.currentTimeMillis()-recordTime>3000){
                        recordTime=System.currentTimeMillis();
                    if(application.currentExhibitBeanList.size()==0){
                        application.currentExhibitBeanList=exhibitBeansList;
                        Intent intent =new Intent();
                        intent.setAction(ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE);
                        context.sendBroadcast(intent);
                    }else {
                        if(!exhibitBeansList.equals(application.nearlyExhibitBeanList)){
                            application.currentExhibitBeanList=exhibitBeansList;
                            Intent intent =new Intent();
                            intent.setAction(ACTION_NOTIFY_NEARLY_EXHIBIT_LIST_CHANGE);
                            context.sendBroadcast(intent);
                        }
                    }}
                }
        }
    };

    public interface GetBeaconCallBack{
        String getMuseumByBeaconCallBack(BeaconBean beaconBean);
    }

}

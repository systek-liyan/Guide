package com.systek.guide.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.beacon.BeaconForSort;
import com.systek.guide.beacon.BeaconSearcher;
import com.systek.guide.beacon.NearestBeacon;
import com.systek.guide.beacon.NearestBeaconListener;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang on 2015/12/11.
 */
public class BluetoothManager implements IConstants {

    private Context context;
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
            synchronized (MyApplication.class){
                if(bluetoothManager==null){
                    bluetoothManager=new BluetoothManager(c);
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
        bluetoothManager=null;
    }

    public void initBeaconSearcher() {
        if (mBeaconSearcher == null) {
            // 设定用于展品定位的最小停留时间(ms)
            mBeaconSearcher = BeaconSearcher.getInstance(context);
            // NearestBeacon.GET_EXHIBIT_BEACON：展品定位beacon
            // NearestBeacon.GET_EXHIBIT_BEACON：游客定位beacon。可以不用设置上述的最小停留时间和最小距离
            mBeaconSearcher.setMin_stay_milliseconds(1500);
            // 设定用于展品定位的最小距离(m)
            mBeaconSearcher.setExhibit_distance(0.5);
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

        /*此方法为自动切换展品，暂时已不用*/
        @Override
        public void getNearestBeacon(int type,Beacon beacon) {

        }

        long recordTime=0;
        //int count=0;
        /**当接受到多个beacon时，根据beacon查找展品，更新附近列表*/
        @Override
        public void getNearestBeacons(int type, List<BeaconForSort> beaconsForSortList) {
            /*如果返回扫面beacon列表为空，返回*/
            if (beaconsForSortList == null||beaconsForSortList.size()<=0) {return;}
            List<BeaconBean> beaconBeanList=null;
            List<ExhibitBean> exhibitBeansList=null;
            /*遍历BeaconForSort集合*/
            beaconBeanList=changeToBeaconList(beaconsForSortList);
            /*获得最近的beacon给地图回调和首页跳转回调*/
            if(beaconBeanList==null||beaconBeanList.size()==0){return;}
            BeaconBean nearestBeacon=beaconBeanList.get(0);
            if(nearestBeacon==null){return;}
            if(nearestBeaconListener!=null){
                nearestBeaconListener.nearestBeaconCallBack(nearestBeacon);
            }
            if(getBeaconCallBack!=null){
                getBeaconCallBack.getMuseumByBeaconCallBack(nearestBeacon);
            }
            /*设定刷新列表周期，小于699毫秒不做处理*/
            if(System.currentTimeMillis()-recordTime<699){return;}
            recordTime=System.currentTimeMillis();
            /*列表为空不做处理*/// TODO: 2016/1/13 附近列表为空是否要显示空的列表？
            if(beaconBeanList.size()==0){return;}
            /*根据beacon集合查询展品*/
            exhibitBeansList=searchExhibitByBeacon(beaconBeanList);
            if (exhibitBeansList==null||exhibitBeansList.size()==0) {return;}
            /*将最近列表转为json发送广播*/
            String json=JSON.toJSONString(exhibitBeansList);
            Intent intent =new Intent();
            intent.setAction(INTENT_EXHIBIT_LIST);
            intent.putExtra(INTENT_EXHIBIT_LIST,json);
            context.sendBroadcast(intent);
        }

    };

    private List<ExhibitBean> searchExhibitByBeacon(List<BeaconBean> beaconBeans) {

        List<ExhibitBean> exhibitList= new ArrayList<>();

        if(beaconBeans==null||beaconBeans.size()==0){return exhibitList;}
        String museumId= beaconBeans.get(0).getMuseumId();
            /*遍历beacon结合，获得展品列表*/
        for(int i=0;i<beaconBeans.size();i++){
            String beaconId=beaconBeans.get(i).getId();
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

    private List<BeaconBean> changeToBeaconList(List<BeaconForSort> beaconsForSortList) {
        List <BeaconBean> beaconBeans=new ArrayList<>();
        for (int i = 0; i < beaconsForSortList.size(); i++) {
            Beacon beacon = beaconsForSortList.get(i).getBeacon();
            double distance=beaconsForSortList.get(i).getDistance();
            Identifier major = beacon.getId2();
            Identifier minor = beacon.getId3();
                /*根据beacon的minor和major参数获得beacon对象*/
            BeaconBean beaconBean= DataBiz.getBeaconMinorAndMajor( minor, major);
            if(beaconBean==null){continue;}
                /*设定距离范围，暂定小于1米则放入列表*/
            if(distance<1.5){// TODO: 2016/1/3
                beaconBean.setDistance(distance);
                beaconBeans.add(beaconBean);
            }
        }
        return beaconBeans;

    }


    public interface GetBeaconCallBack{
        String getMuseumByBeaconCallBack(BeaconBean beaconBean);
    }
}

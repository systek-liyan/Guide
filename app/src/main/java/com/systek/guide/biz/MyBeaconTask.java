package com.systek.guide.biz;

import com.systek.guide.IConstants;
import com.systek.guide.callback.BeaconChangeCallback;
import com.systek.guide.entity.BeaconBean;
import com.systek.guide.entity.ExhibitBean;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Qiang on 2016/5/18.
 */
public class MyBeaconTask implements Runnable,IConstants {

    boolean isSwitch;
    Collection<Beacon> beacons;
    BeaconChangeCallback callback;

    public MyBeaconTask(Collection<Beacon> beacons){
        this.beacons = beacons;
    }

    public MyBeaconTask(boolean isSwitch, Collection<Beacon> beacons) {
        this.isSwitch = isSwitch;
        this.beacons = beacons;
    }
    public MyBeaconTask( Collection<Beacon> beacons,BeaconChangeCallback callback) {
        this.beacons = beacons;
        this.callback=callback;
    }
    public MyBeaconTask(boolean isSwitch, Collection<Beacon> beacons,BeaconChangeCallback callback) {
        this.isSwitch = isSwitch;
        this.beacons = beacons;
        this.callback=callback;
    }
    @Override
    public void run() {
        //DataBiz.saveTempValue(MyApplication.get(), SP_IS_IN_MUSEUM, true);
        if(beacons==null){return;}

        NearestBeacon.getInstance().calculateDistance(beacons);
        List<SystekBeacon> exhibitLocateBeacons = NearestBeacon.getInstance().getExhibitLocateBeacons();
        if (callback==null){return;}
        List<BeaconBean> beaconList=changeToBeaconList(exhibitLocateBeacons,20.0);
        if(beaconList==null||beaconList.size()==0){return;}
        List<ExhibitBean> exhibitBeansList=searchExhibitByBeacon(beaconList);
        if (exhibitBeansList==null||exhibitBeansList.size()==0) {return;}
        callback.getNearestBeacon(beaconList.get(0));
        callback.getExhibits(exhibitBeansList);
        callback.getNearestExhibit(exhibitBeansList.get(0));

       /* Beacon beacon=DisCount.getInstance().dis(beacons);
        if(beacon!=null){
            LogUtil.i("ZHANG",System.currentTimeMillis()+"  beacon= "+beacon.getId3());
        }
        List<Beacon> beaconList=DisCount.getInstance().disArray(beacons);
        List<BeaconBean> mBeaconList=changeToBeaconList(beaconList,20.0);
        if(beaconList==null||beaconList.size()==0){return;}
        List<ExhibitBean> exhibitBeansList=searchExhibitByBeacon(mBeaconList);
        if (exhibitBeansList==null||exhibitBeansList.size()==0) {return;}
        if(callback==null){return;}
        List<ExhibitBean> tempList=searchExhibitByBeacon(beacon);
        callback.getExhibits(exhibitBeansList);
        callback.getNearestExhibits(tempList);
        callback.getNearestExhibit(exhibitBeansList.get(0));*/

    }

    /**
     * 根据beaconforsort集合找出beacon集合
     * @param beacons beacon结合
     * @param dis 规定距离内beacon
     * @return
     */
    private static List<BeaconBean> changeToBeaconList( List<SystekBeacon> beacons,double dis) {
        List <BeaconBean> beaconBeans=new ArrayList<>();
        if(beacons==null||beacons.size()==0){
            return null;
        }

        for (SystekBeacon beacon:beacons) {
            String major = beacon.getMajor();
            String minor = beacon.getMinor();
            //根据beacon的minor和major参数获得beacon对象
            BeaconBean beaconBean= DataBiz.getBeaconMinorAndMajor(minor, major);
            if(beaconBean==null){continue;}
            beaconBean.setDistance(beacon.getDistance());
            //设定距离范围，暂定小于1米则放入列表
          /* if(systekBeacon.getDistance()<dis){
            }*/
            beaconBeans.add(beaconBean);
        }
        return beaconBeans;
    }
    /**
     * 根据beaconforsort集合找出beacon集合
     * @param beacons beacon结合
     * @param dis 规定距离内beacon
     * @return
     */
    private static List<BeaconBean> changeToBeaconList( Collection<Beacon> beacons,double dis) {
        List <BeaconBean> beaconBeans=new ArrayList<>();
        if(beacons==null||beacons.size()==0){
            return null;
        }

        for (Beacon beacon:beacons) {
            Identifier major = beacon.getId2();
            Identifier minor = beacon.getId3();
            //根据beacon的minor和major参数获得beacon对象
            BeaconBean beaconBean= DataBiz.getBeaconMinorAndMajor(minor, major);
            if(beaconBean==null){continue;}
            beaconBean.setDistance(beacon.getDistance());
            //设定距离范围，暂定小于1米则放入列表
          /* if(systekBeacon.getDistance()<dis){
            }*/
            beaconBeans.add(beaconBean);
        }
        return beaconBeans;
    }


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
     * 根据beacon集合找出展品集合
     * @param beacon
     * @return
     */
    private static List<ExhibitBean> searchExhibitByBeacon(Beacon beacon) {

        List<ExhibitBean> exhibitList= new ArrayList<>();
        if(beacon==null){return exhibitList;}

        BeaconBean bean= DataBiz.getBeaconMinorAndMajor(beacon.getId3(), beacon.getId2());
        if(bean==null){return null;}

        return DataBiz.getExhibitListByBeaconId(bean.getMuseumId(), bean.getId());
    }


}

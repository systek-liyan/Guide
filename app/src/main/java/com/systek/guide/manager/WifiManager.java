package com.systek.guide.manager;

import android.net.wifi.ScanResult;
import android.text.TextUtils;

import com.systek.guide.IConstants;
import com.systek.guide.MyApplication;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumNetInfo;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.WifiAdmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Qiang on 2016/3/21.
 */
public class WifiManager implements IConstants{

    public static MuseumNetInfo connectWifi(String museumId){
        long startTime=System.currentTimeMillis();
        String url=BASE_URL+URL_LOCAL_HOSTS+museumId;
        List<MuseumNetInfo> netInfoList= DataBiz.getEntityListFromNet(MuseumNetInfo.class, url);
        //当博物馆中没有WiFi，不去下载
        if(netInfoList==null||netInfoList.size()==0){return null;}
        WifiAdmin wifiAdmin = new WifiAdmin(MyApplication.get());
        //若WiFi没有打开，打开WiFi
        if(!wifiAdmin.isWifiEnable()){
            wifiAdmin.openWifi();
            while(!wifiAdmin.isWifiEnable()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LogUtil.i("Zhang", "wifi已经打开用时" + (System.currentTimeMillis() - startTime));
        }
        //如果有连接，断开连接
        if(wifiAdmin.isWifiConnected()){wifiAdmin.disconnectNetWork();}
        //如果WiFi没有连接，需要连接WiFi
        //开始扫描
        wifiAdmin.startScan();
        //获取WiFi扫描结果
        List<ScanResult> scanWifiResultList= wifiAdmin.getWifiList();
        //没有扫描到WiFi信息，不去下载
        if(scanWifiResultList==null||scanWifiResultList.size()==0){return null;}
        List<String> scanWifiSSIDList=new ArrayList<>();
        //遍历扫描结果，将结果中WiFi名称加入集合
        for(ScanResult result:scanWifiResultList){
            if(!TextUtils.isEmpty(result.SSID)){
                scanWifiSSIDList.add(result.SSID);
            }
        }
        //遍历获取服务器中存储所在博物馆的WiFi名称至集合
        List<String> serviceSsidList=new ArrayList<>();
        for(MuseumNetInfo info:netInfoList){
            serviceSsidList.add(info.getWifiName());
        }
        int size=serviceSsidList.size();

        List<String> existSSIDList=new ArrayList<>();
        for(int i=0;i<size;i++){
            String ssid=serviceSsidList.get(i);
            if(!TextUtils.isEmpty(ssid)&&scanWifiSSIDList.contains(ssid)){
                existSSIDList.add(ssid);
            }
        }
        //没有WiFi，不去下载
        if(existSSIDList.size()==0){
            return null;
        }
        //将扫描中包含服务器中存储博物馆的WiFi信息提取
        List<ScanResult> canUseWifiList=new ArrayList<>();
        for(ScanResult result:scanWifiResultList){
            for(int j=0;j<existSSIDList.size();j++){
                String ssid=existSSIDList.get(j);
                String scanSsid=result.SSID;
                if(scanSsid!=null&&ssid!=null&&scanSsid.equals(ssid)){
                    canUseWifiList.add(result);
                }
            }

        }
        if(canUseWifiList.size()>1){
            Collections.sort(canUseWifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return rhs.level - lhs.level;// TODO: 2016/2/23
                }
            });

        }
        ScanResult scanResult=canUseWifiList.get(0);
        String ssid=scanResult.SSID;
        MuseumNetInfo toUseWifi=null;
        for(MuseumNetInfo wifi:netInfoList){
            String name=wifi.getWifiName();
            if(name!=null&&name.equals(ssid)){
                toUseWifi=wifi;
            }
        }
        if(toUseWifi==null){return null;}
        startTime=System.currentTimeMillis();
        LogUtil.i("ZHANG", "要连接的wifi=" + toUseWifi.getWifiName() + "密码=" + toUseWifi.getWifiPass());
        wifiAdmin.connectWifi(toUseWifi.getWifiName(),toUseWifi.getWifiPass());
        while(!wifiAdmin.isWifiConnected()) {
            try {
                if (System.currentTimeMillis() - startTime > 150000) {
                    LogUtil.i("ZHANG", "连接WiFi超时");
                    break;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtil.i("ZHANG", "连接WiFi用时；" + (System.currentTimeMillis()-startTime));
        int ip= wifiAdmin.getIPAddress();
        LogUtil.i("ZHANG", "ip地址" + wifiAdmin.intToIp(ip));
        return toUseWifi;
    }

}

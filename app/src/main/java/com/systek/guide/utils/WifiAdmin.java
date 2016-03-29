package com.systek.guide.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by Qiang on 2015/12/11.
 */
public class WifiAdmin {

    // 定义WifiManager对象
    private WifiManager mWifiManager;

    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock
    WifiManager.WifiLock mWifiLock;

    private Context context;

    // 构造器
    public WifiAdmin(Context context) {
        this.context=context.getApplicationContext();
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }


    public WifiManager getmWifiManager() {
        return mWifiManager;
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    // 检查当前WIFI状态
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    public boolean isWifiEnable(){
        return mWifiManager.isWifiEnabled();
    }


    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    // 创建一个WifiLock
    public void creatWifiLock(String lock) {
        mWifiLock = mWifiManager.createWifiLock(lock);
    }

    // 得到配置好的网络
    public List<WifiConfiguration> getConfiguration() {
        return mWifiConfiguration;
    }

    public void startScan() {
        mWifiManager.startScan();
        // 得到扫描结果
        //mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    // 得到网络列表
    public List<ScanResult> getWifiList() {
        mWifiList = mWifiManager.getScanResults();
        return mWifiList;
    }

    // 指定配置好的网络进行连接
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        // 连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

    // 查看扫描结果
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++) {
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }



    public String getConnectedWifiSSID(){

        // 判断用户是打开还是关闭
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
       // manager.getAllNetworks();
        if(activeNetworkInfo==null){return null;}
        NetworkInfo wifiNetworkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            return  info != null ? info.getSSID() : null;
        }
        return null;
    }


    // 得到MAC地址
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    // 得到接入点的BSSID
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    // 得到IP地址
    public int getIPAddress() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    // 得到连接的ID
    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }


    //是否连接WIFI
    public boolean isWifiConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }
        return false ;
    }


    public void disconnectNetWork(){
        if(isWifiConnected()){
            mWifiManager.disconnect();
        }
    }

    // 添加一个网络并连接
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b =  mWifiManager.enableNetwork(wcgID, true);
        System.out.println("a--" + wcgID);
        System.out.println("b--" + b);
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    public  void connectWifi(String ssid,String password){

        WifiConfiguration wifiConfig = new WifiConfiguration();

        wifiConfig . status =  WifiConfiguration . Status . DISABLED ;
        wifiConfig . priority =  40 ;

        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", password);

        wifiConfig . allowedProtocols . set ( WifiConfiguration . Protocol . RSN );
        wifiConfig . allowedProtocols . set ( WifiConfiguration . Protocol . WPA );
        wifiConfig . allowedKeyManagement . set ( WifiConfiguration . KeyMgmt . WPA_PSK );
        wifiConfig . allowedPairwiseCiphers . set ( WifiConfiguration . PairwiseCipher . CCMP );
        wifiConfig . allowedPairwiseCiphers . set ( WifiConfiguration . PairwiseCipher . TKIP );
        wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . WEP40 );
        wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . WEP104 );
        wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . CCMP );
        wifiConfig . allowedGroupCiphers . set ( WifiConfiguration . GroupCipher . TKIP );

        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);//remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    public String intToIp(int i) {
        return ( i & 0xFF) + "." +  ((i >> 8 ) & 0xFF) + "." +  ((i >> 16 ) & 0xFF) + "." +  ((i >> 24 ) & 0xFF ) ;
    }
}

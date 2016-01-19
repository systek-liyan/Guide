package com.systek.guide.activity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.systek.guide.R;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.utils.NetworkUtil;
import com.systek.guide.utils.Tools;
import com.systek.guide.utils.WifiAdmin;

public class BeginActivity extends BaseActivity {

    private View view;
    private Class<?> targetClass;
    private BluetoothManager bluetoothManager;

    @Override
    protected void initialize(Bundle savedInstanceState) {
        //connectWIFI();
        initBlueTooth();
        view = View.inflate(this, R.layout.activity_begin, null);
        NetworkUtil.checkNet(this);
        setContentView(view);
        boolean isFirstLogin= (boolean) Tools.getValue(this, SP_NOT_FIRST_LOGIN, false);
        if(!isFirstLogin){
            Tools.saveValue(this,SP_NOT_FIRST_LOGIN,true);
            targetClass=WelcomeActivity.class;
        }else{
            /*默认跳转界面为城市选择*/
            targetClass=MuseumListActivity.class;
        }
    }

    private void initBlueTooth() {
        bluetoothManager = BluetoothManager.newInstance(this);
        bluetoothManager.initBeaconSearcher();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
    private void connectWIFI() {

        new Thread(){
            @Override
            public void run() {
                WifiAdmin wifiAdmin=new WifiAdmin(BeginActivity.this);
                if(wifiAdmin.checkState()== WifiManager.WIFI_STATE_DISABLED){
                    wifiAdmin.openWifi();
                }
                WifiAdmin.connectWifi(BeginActivity.this,WIFI_SSID,WIFI_PASSWORD);
            }
        }.start();
    }

    private void initData() {
        AlphaAnimation startAnimation = new AlphaAnimation(1.0f, 1.0f);
        startAnimation.setDuration(2500);
        view.startAnimation(startAnimation);
        startAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                redirectTo();
            }
        });
    }
    protected void redirectTo() {
        startActivity(new Intent(getApplicationContext(),targetClass));
        finish();
    }
}

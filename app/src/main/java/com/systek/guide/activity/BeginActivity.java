package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.entity.MuseumBean;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.utils.NetworkUtil;
import com.systek.guide.utils.Tools;

import java.util.List;

public class BeginActivity extends BaseActivity implements IConstants{

    private View view;//开始界面的view
    private Class<?> targetClass;
    private boolean isFirstLogin;
    private BluetoothManager bluetoothManager;
    @Override
    protected void initialize(Bundle savedInstanceState) {
        view = View.inflate(this, R.layout.activity_begin, null);
        NetworkUtil.checkNet(this);
        setContentView(view);
        bluetoothManager=BluetoothManager.newInstance(this);
        initData();
    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    private void initData() {
        //判断是否是第一次进入APP，是则进入导航界面，否则进入博物馆列表页
        AlphaAnimation startAnimation = new AlphaAnimation(1.0f, 1.0f);
        startAnimation.setDuration(1000);
        view.startAnimation(startAnimation);
        startAnimation.setAnimationListener(animationListener);
    }

    private Animation.AnimationListener animationListener=new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
        @Override
        public void onAnimationEnd(Animation animation) {
            new Thread(){
                @Override
                public void run() {
                    isFirstLogin= (boolean) Tools.getValue(BeginActivity.this, SP_NOT_FIRST_LOGIN, true);
                    if(isFirstLogin){
                        Tools.saveValue(BeginActivity.this,SP_NOT_FIRST_LOGIN,false);
                        targetClass=WelcomeActivity.class;
                    }else{
                            /*默认跳转界面为城市选择*/
                        targetClass=MuseumListActivity.class;
                    }
                    Intent intent=new Intent();
                    if(!isFirstLogin){
                        String museumId=bluetoothManager.getCurrentMuseumId();
                        MuseumBean currentMuseum=null;
                        String url=BASE_URL+URL_GET_MUSEUM_BY_ID+museumId;
                        List<MuseumBean> museumBeanList= DataBiz.getEntityListFromNet(MuseumBean.class,url);
                        if(museumBeanList!=null&&museumBeanList.size()>0){
                            currentMuseum=museumBeanList.get(0);
                        }
                        if(currentMuseum!=null){
                            String json= JSON.toJSONString(currentMuseum);
                            targetClass=MuseumHomeActivity.class;
                            intent.putExtra(INTENT_MUSEUM, json);
                        }
                    }
                    intent.setClass(BeginActivity.this, targetClass);
                    startActivity(intent);
                    finish();
                }
            }.start();
        }
    };

}

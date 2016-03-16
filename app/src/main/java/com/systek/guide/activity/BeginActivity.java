package com.systek.guide.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.manager.BluetoothManager;
import com.systek.guide.utils.NetworkUtil;
import com.systek.guide.utils.Tools;

import java.lang.ref.WeakReference;

public class BeginActivity extends BaseActivity implements IConstants{


    private Class<?> targetClass;
    private boolean isFirstLogin;
    private String currentMuseumId;
    private Handler handler;


    @Override
    protected void setView() {
        View view = View.inflate(this, R.layout.activity_begin, null);
        setContentView(view);
        handler=new MyHandler(this);
    }

    @Override
    void initView() {

    }

    @Override
    void addListener() {

    }

    @Override
    void initData() {
        new Thread(){
            @Override
            public void run() {
                NetworkUtil.checkNet(BeginActivity.this);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isFirstLogin= (boolean) Tools.getValue(BeginActivity.this, SP_NOT_FIRST_LOGIN, true);
                if(isFirstLogin){
                    Tools.saveValue(BeginActivity.this,SP_NOT_FIRST_LOGIN,false);
                    targetClass=WelcomeActivity.class;
                }else{
                            /*默认跳转界面为城市选择*/
                    targetClass=MuseumListActivity.class;
                    //targetClass=CityChooseActivity.class;
                }
                if(!isFirstLogin){
                    BluetoothManager bluetoothManager=BluetoothManager.newInstance(BeginActivity.this);
                    currentMuseumId=bluetoothManager.getCurrentMuseumId();
                    if(!TextUtils.isEmpty(currentMuseumId)){
                        targetClass=MuseumHomeActivity.class;
                    }
                }
                handler.sendEmptyMessage(MSG_WHAT_UPDATE_DATA_SUCCESS);
            }
        }.start();
    }

    @Override
    void registerReceiver() {

    }

    private void goToNextActivity(){
        Intent intent=new Intent();
        if(!TextUtils.isEmpty(currentMuseumId)){
            intent.putExtra(INTENT_MUSEUM_ID, currentMuseumId);
        }
        intent.setClass(BeginActivity.this, targetClass);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        WeakReference<BeginActivity> activity;
        public MyHandler(BeginActivity activity){
            this.activity=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(activity==null){return;}
            BeginActivity beginActivity=activity.get();
            if(beginActivity==null){return;}
            switch (msg.what){
                case MSG_WHAT_UPDATE_DATA_SUCCESS:
                    beginActivity.goToNextActivity();
                    break;
                default:break;
            }
        }
    }


}

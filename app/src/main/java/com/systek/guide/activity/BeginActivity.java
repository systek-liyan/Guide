package com.systek.guide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.systek.guide.R;
import com.systek.guide.biz.DataBiz;
import com.systek.guide.utils.Tools;

import java.lang.ref.WeakReference;

public class BeginActivity extends BaseActivity{


    private Class<?> targetClass;
    private boolean isFirstLogin;
    private String currentMuseumId;
    private static final int MSG_WHAT_CHANGE_ACTIVITY=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LinearLayout.inflate(this,R.layout.activity_begin,null);
        switch (theme){
            case R.style.AppTheme:
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case R.style.BlueAppTheme:
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                break;
        }
        setContentView(view);
        handler=new MyHandler(this);
        initData();

    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
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
                    currentMuseumId= (String) DataBiz.getTempValue(BeginActivity.this, SP_MUSEUM_ID, "");
                    if(!TextUtils.isEmpty(currentMuseumId)){
                        targetClass=MuseumHomeActivity.class;
                    }
                }
                handler.sendEmptyMessage(MSG_WHAT_CHANGE_ACTIVITY);
            }
        }.start();
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

    static class MyHandler extends Handler {

        WeakReference<BeginActivity> activityWeakReference;
        MyHandler(BeginActivity activity){
            this.activityWeakReference=new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            BeginActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_CHANGE_ACTIVITY:
                    activity.goToNextActivity();
                    break;
               default:break;
            }
        }
    }


}

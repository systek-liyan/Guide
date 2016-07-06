package com.systek.guide.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.systek.guide.R;
import com.systek.guide.entity.base.VersionBean;
import com.systek.guide.manager.UpdateManager;
import com.systek.guide.utils.Tools;

import java.lang.ref.WeakReference;

public class SettingActivity extends BaseActivity {


    private Button btn_update;
    private static final int MSG_WHAT_CURRENT_VERSION_IS_NEAREST=1;
    private static final int MSG_WHAT_CURRENT_VERSION_NOT_NEAREST=2;
    private UpdateManager updateManager;
    private Button themeNormal;
    private Button themeBlue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        handler=new MyHandler(this);
        //initDrawer();
        initView();
        addListener();

    }
    private void addListener() {
        updateManager=new UpdateManager(SettingActivity.this);
        btn_update.setOnClickListener(onClickListener);
        themeNormal.setOnClickListener(onClickListener);
        themeBlue.setOnClickListener(onClickListener);

    }

    public  void initView() {
        setTitleBar();
        setTitleBarTitle("设置");
        setHomeIcon();
        setHomeClickListener(backOnClickListener);
        btn_update=(Button)findViewById(R.id.btn_update);
        themeNormal=(Button)findViewById(R.id.themeNormal);
        themeBlue=(Button)findViewById(R.id.themeBlue);
    }


    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_update:
                    checkUpdate();
                    break;
                case R.id.titleBarDrawer:
                    finish();
                    break;
                case R.id.themeBlue:
                    Tools.saveValue(getApplicationContext(),THEME,R.style.BlueAppTheme);
                    getApplication().setTheme(R.style.BlueAppTheme);
                    recreate();
                    break;
                case R.id.themeNormal:
                    Tools.saveValue(getApplicationContext(),THEME,R.style.AppTheme);
                    getApplication().setTheme(R.style.AppTheme);
                    recreate();
                    break;
            }
        }
    };

    private void checkUpdate() {
        new Thread(){
            @Override
            public void run() {
                String currentVersion=updateManager.getVersion(SettingActivity.this);
                VersionBean version=updateManager.checkVersion();
                if(version==null){return;}
                String nearestVersionName=version.getVersion();
                if(nearestVersionName.equals(currentVersion)){
                    handler.sendEmptyMessage(MSG_WHAT_CURRENT_VERSION_IS_NEAREST);
                }else{
                    updateManager.setApkUrl(version.getUrl());
                    handler.sendEmptyMessage(MSG_WHAT_CURRENT_VERSION_NOT_NEAREST);
                }
            }
        }.start();

    }

    public void alreadyNewest(){
        Toast.makeText(SettingActivity.this,"当前已是最新版本！",Toast.LENGTH_SHORT).show();
    }

    public void checkUpdataInfo(){
        updateManager.checkUpdateInfo();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

   static class MyHandler extends Handler{

       WeakReference<SettingActivity> activityWeakReference;
       MyHandler(SettingActivity activity){
           this.activityWeakReference=new WeakReference<>(activity);
       }

        @Override
        public void handleMessage(Message msg) {

            if(activityWeakReference==null){return;}
            SettingActivity activity=activityWeakReference.get();
            if(activity==null){return;}
            switch (msg.what){
                case MSG_WHAT_CURRENT_VERSION_IS_NEAREST:
                    activity.alreadyNewest();
                    break;
                case MSG_WHAT_CURRENT_VERSION_NOT_NEAREST:
                    activity.checkUpdataInfo();
                    break;
            }
        }
    }

}

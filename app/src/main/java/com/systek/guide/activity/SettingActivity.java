package com.systek.guide.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.systek.guide.R;
import com.systek.guide.entity.base.VersionBean;
import com.systek.guide.manager.UpdateManager;

public class SettingActivity extends BaseActivity {


    private Button btn_update;
    private final int MSG_WHAT_CURRENT_VERSION_IS_NEAREST=1;
    private final int MSG_WHAT_CURRENT_VERSION_NOT_NEAREST=2;
    private Handler handler;
    private UpdateManager updateManager;

    @Override
    protected void setView() {
        setContentView(R.layout.activity_setting);
        initDrawer();
    }
    @Override
    void addListener() {
        handler=new MyHandler();
        updateManager=new UpdateManager(SettingActivity.this);
        btn_update.setOnClickListener(onClickListener);
    }

    @Override
    void initData() {

    }

    @Override
    void registerReceiver() {

    }
    @Override
    void initView() {

        setTitleBar();
        setTitleBarTitle("设置");
        setHomeIcon();
        setHomeClickListener(backOnClickListener);
        btn_update=(Button)findViewById(R.id.btn_update);
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

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_WHAT_CURRENT_VERSION_IS_NEAREST:
                    Toast.makeText(SettingActivity.this,"当前已是最新版本！",Toast.LENGTH_SHORT).show();
                    break;
                case MSG_WHAT_CURRENT_VERSION_NOT_NEAREST:
                    updateManager.checkUpdateInfo();
                    break;
            }
        }
    }

}

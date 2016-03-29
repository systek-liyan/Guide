package com.systek.guide.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.systek.guide.R;
import com.systek.guide.entity.base.VersionBean;
import com.systek.guide.manager.UpdateManager;

import java.lang.ref.WeakReference;

public class SettingActivity extends BaseActivity {


    private Button btn_update;
    private static final int MSG_WHAT_CURRENT_VERSION_IS_NEAREST=1;
    private static final int MSG_WHAT_CURRENT_VERSION_NOT_NEAREST=2;
    private UpdateManager updateManager;

    @Override
    protected void setView() {
        setContentView(R.layout.activity_setting);
        initDrawer();
    }
    @Override
    void addListener() {
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
    void unRegisterReceiver() {

    }

    @Override
    void refreshView() {

    }

    @Override
    void refreshExhibit() {

    }

    @Override
    void refreshTitle() {

    }

    @Override
    void refreshViewBottomTab() {

    }

    @Override
    void refreshProgress() {

    }

    @Override
    void refreshIcon() {

    }

    @Override
    void refreshState() {

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

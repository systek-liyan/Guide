package com.systekcn.guide.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.systekcn.guide.R;
import com.systekcn.guide.adapter.DownloadAdapter;
import com.systekcn.guide.adapter.DownloadProgressListener;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.widget.DrawerView;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity implements DownloadProgressListener{

    private ImageView iv_download_drawer;
    private SlidingMenu side_drawer;
    private ListView lv_download;
    private TextView progressText;
    private  ProgressBar progressBar;
    private ImageView download_ctrl_btn;
    private DownloadBroadcastReceiver downloadBroadcastReceiver;
    private List<MuseumBean> museumBeanList;
    private DownloadAdapter downloadAdapter;
    private final int MSG_WHAT_UPDATE_DOWNLOAD_DATA=1;
    private Handler  handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if(msg.what==MSG_WHAT_UPDATE_DOWNLOAD_DATA){
                try{
                    downloadAdapter.updateData(museumBeanList);
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initialize();
    }

    private void initialize() {
        try{
            initViews();
            addListener();
            registReceiver();
            initSlidingMenu();
            initData();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void initData() {
        try{
            new Thread(){
                @Override
                public void run() {
                    BeansManageBiz biz= (BeansManageBiz) BizFactory.getBeansManageBiz(DownloadActivity.this);
                    museumBeanList = biz.getAllBeans(URL_TYPE_GET_MUSEUM_LIST, MuseumBean.class, "");
                    // TODO: 2015/11/2
                    while (museumBeanList == null) {
                    }
                    handler.sendEmptyMessage(MSG_WHAT_UPDATE_DOWNLOAD_DATA);
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }

    }

    private void registReceiver() {
        /*注册广播*/
        downloadBroadcastReceiver = new DownloadBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PROGRESS);
        registerReceiver(downloadBroadcastReceiver, filter);
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void addListener() {
        iv_download_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (side_drawer.isMenuShowing()) {
                        side_drawer.showContent();
                    } else {
                        side_drawer.showMenu();
                    }
                }catch (Exception e){
                    ExceptionUtil.handleException(e);
                }

            }
        });

    }

    private void initViews() {
        iv_download_drawer=(ImageView)findViewById(R.id.iv_download_drawer);
        lv_download=(ListView)findViewById(R.id.lv_download);
        museumBeanList=new ArrayList<>();
        downloadAdapter=new DownloadAdapter(this,museumBeanList);
        lv_download.setAdapter(downloadAdapter);
    }

    @Override
    public void onProgressChanged(ProgressBar progressBar, TextView textView,ImageView iv_download_trl) {
        progressText=textView;
        this.progressBar=progressBar;
        this.download_ctrl_btn=iv_download_trl;
    }

    /**用于接收下载中需要的数据的广播接收器*/
    class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try{
				/*如果广播是下载进度，则更新进度条，下载完毕则隐藏相关控件*/
                if (intent.getAction().equals(ACTION_PROGRESS)) {
                    int progress = intent.getIntExtra(ACTION_PROGRESS, -1);
                    if (progressBar != null&&progress!=-1) {
                        if (progress == 100) {
                            download_ctrl_btn.setVisibility(View.GONE);
                            String museumId=(String) download_ctrl_btn.getTag();
                            SharedPreferences setting = getSharedPreferences(museumId,0);
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putBoolean(HAS_DOWNLOAD, true);
                        }
                        progressBar.setProgress(progress);
                        progressText.setText(progress+" %");
                    }
                }
            }catch (Exception e){
                ExceptionUtil.handleException(e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(downloadBroadcastReceiver!=null){
            unregisterReceiver(downloadBroadcastReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(side_drawer.isMenuShowing() ||side_drawer.isSecondaryMenuShowing()){
                side_drawer.showContent();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}

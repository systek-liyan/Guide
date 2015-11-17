package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.magic.mapdemo.R;
import com.systekcn.guide.adapter.DownloadAdapter;
import com.systekcn.guide.adapter.DownloadAdapter.DownloadProgressListener;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.service.MuseumDownloadService;
import com.systekcn.guide.widget.DrawerView;
import com.systekcn.guide.widget.slidingmenu.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    private ImageView iv_download_drawer;
    private SlidingMenu side_drawer;
    private ListView lv_download;
    private DownloadBroadcastReceiver downloadBroadcastReceiver;
    private List<MuseumBean> museumBeanList;
    private DownloadAdapter downloadAdapter;
    private final int MSG_WHAT_UPDATE_DOWNLOAD_DATA=1;
    private AlertDialog progressDialog;
    private DownloadAdapter.ViewHolder mViewHolder;


    private Handler  handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if(msg.what==MSG_WHAT_UPDATE_DOWNLOAD_DATA){
                try{
                    downloadAdapter.updateData(museumBeanList);
                    if(progressDialog!=null&&progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
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
            initData();
            initViews();
            addListener();
            registerReceiver();
            initSlidingMenu();
            showProgressDialog();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void showProgressDialog() {
        progressDialog = new AlertDialog.Builder(DownloadActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.alert_dialog_progress);
        TextView dialog_title=(TextView)window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");
    }


    private void initData() {
        try{
            new Thread(){
                @Override
                public void run() {

                    BeansManageBiz biz= (BeansManageBiz) BizFactory.getBeansManageBiz(DownloadActivity.this);
                    museumBeanList = biz.getAllBeans(URL_TYPE_GET_MUSEUM_LIST, MuseumBean.class, "");
                    long time=System.currentTimeMillis();
                    while(museumBeanList == null) {
                        if(System.currentTimeMillis()-time<30000){
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                ExceptionUtil.handleException(e);
                            }
                        }else{
                            break;
                        }
                    }
                    if(museumBeanList!=null){
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DOWNLOAD_DATA);
                    }else{
                        Toast.makeText(DownloadActivity.this,"数据加载失败，请检查网络",Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }

    }

    private void registerReceiver() {
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
                try {
                    if (side_drawer.isMenuShowing()) {
                        side_drawer.showContent();
                    } else {
                        side_drawer.showMenu();
                    }
                } catch (Exception e) {
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
        downloadAdapter.setDownloadProgressListener(downloadProgressListener);
    }

    private DownloadProgressListener downloadProgressListener=new DownloadProgressListener() {
        @Override
        public void onProgressChanged(DownloadAdapter.ViewHolder viewHolder) {
            mViewHolder=viewHolder;
        }
    };

    /**用于接收下载中需要的数据的广播接收器*/
    class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            try{
				/*如果广播是下载进度，则更新进度条，下载完毕则隐藏相关控件*/
                if (intent.getAction().equals(ACTION_PROGRESS)) {
                    int progress = intent.getIntExtra(ACTION_PROGRESS, -1);
                    if (mViewHolder != null) {
                        if (progress == 100) {
                            mViewHolder.ivCtrl.setVisibility(View.GONE);
                            MuseumDownloadService.isDownloadOver=true;
                        }
                        mViewHolder.progressBar.setProgress(progress);
                        mViewHolder.tvProgress.setText(progress+" %");
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

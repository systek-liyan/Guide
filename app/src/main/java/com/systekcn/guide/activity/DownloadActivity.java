package com.systekcn.guide.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.systekcn.guide.MyApplication;
import com.systekcn.guide.R;
import com.systekcn.guide.activity.base.BaseActivity;
import com.systekcn.guide.adapter.DownloadAdapter;
import com.systekcn.guide.adapter.DownloadAdapter.DownloadProgressListener;
import com.systekcn.guide.biz.BeansManageBiz;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.NetworkUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.common.utils.ViewUtils;
import com.systekcn.guide.custom.DrawerView;
import com.systekcn.guide.custom.slidingmenu.SlidingMenu;
import com.systekcn.guide.entity.MuseumBean;
import com.systekcn.guide.service.MuseumDownloadService;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity implements IConstants{


    private ImageView iv_download_drawer;/**侧边栏按钮*/
    private SlidingMenu side_drawer;/**侧滑菜单*/
    private ListView lv_download;/**下载列表*/
    private DownloadBroadcastReceiver downloadBroadcastReceiver;/**下载状态监听广播接收器*/
    private List<MuseumBean> museumBeanList;/**可下载博物馆集合*/
    private DownloadAdapter downloadAdapter;/**适配器*/
    private final int MSG_WHAT_UPDATE_DOWNLOAD_DATA=1;
    private AlertDialog progressDialog;/**对话框*/
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
    private TextView tv_download_btn_refresh;/*无网络时刷新按钮*/


    @Override
    protected void initialize() {
        ViewUtils.setStateBarColor(this, R.color.myOrange);
        setContentView(R.layout.activity_download);
        init();
    }


    private void init() {
        try{
            initViews();//加载视图
            addListener();//添加监听器
            registerReceiver();/*注册广播*/
            initSlidingMenu();/*初始侧边栏*/
            if(MyApplication.currentNetworkType==INTERNET_TYPE_NONE){
                tv_download_btn_refresh.setVisibility(View.VISIBLE);
                Toast.makeText(this,"当前无网络连接，请检查网络！",Toast.LENGTH_SHORT).show();
            }else {
                showDialog();
                initData();//加载数据
            }
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    private void showDialog() {
         /*数据未获得前显示对话框*/
        progressDialog = new AlertDialog.Builder(DownloadActivity.this).create();
        progressDialog.show();
        Window window = progressDialog.getWindow();
        window.setContentView(R.layout.dialog_progress);
        TextView dialog_title = (TextView) window.findViewById(R.id.dialog_title);
        dialog_title.setText("正在加载...");
    }

    private void initData() {
        try {
            new Thread() {
                @Override
                public void run() {
                    BeansManageBiz biz = (BeansManageBiz) BizFactory.getBeansManageBiz(DownloadActivity.this);
                    museumBeanList = biz.getAllBeansByNet(URL_TYPE_GET_MUSEUM_LIST, MuseumBean.class, "");
                    long time = System.currentTimeMillis();
                    while (museumBeanList == null) {
                        if (System.currentTimeMillis() - time < 30000) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                ExceptionUtil.handleException(e);
                            }
                        } else {
                            break;
                        }
                    }
                    if (museumBeanList != null) {
                        handler.sendEmptyMessage(MSG_WHAT_UPDATE_DOWNLOAD_DATA);
                    } else {
                        Toast.makeText(DownloadActivity.this, "数据加载失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
        }
    }

    private void registerReceiver() {
        /*注册广播*/
        downloadBroadcastReceiver = new DownloadBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PROGRESS);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");/*监听网络状态*/
        registerReceiver(downloadBroadcastReceiver, filter);
    }

    private void initSlidingMenu() {
        DrawerView dv =new DrawerView(this);
        side_drawer = dv.initSlidingMenu();
    }

    private void addListener() {

        tv_download_btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyApplication.currentNetworkType!=INTERNET_TYPE_NONE){
                    v.setVisibility(View.GONE);
                    showDialog();
                    initData();
                }else{
                    Tools.showMessage(DownloadActivity.this,"当前无网络连接，请检查网络!");
                }
            }
        });

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
        tv_download_btn_refresh=(TextView)findViewById(R.id.tv_download_btn_refresh);
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

    @Override
    protected void onDestroy() {
        if(downloadBroadcastReceiver!=null){
            unregisterReceiver(downloadBroadcastReceiver);
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
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
    /**用于接收下载中需要的数据的广播接收器*/
    class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {

            try {
				/*如果广播是下载进度，则更新进度条，下载完毕则隐藏相关控件*/
                if (intent.getAction().equals(ACTION_PROGRESS)) {
                    int progress = intent.getIntExtra(ACTION_PROGRESS, -1);
                    if (mViewHolder != null) {
                        if (progress == 100) {
                            mViewHolder.ivCtrl.setVisibility(View.GONE);
                            mViewHolder.tvState.setVisibility(View.VISIBLE);
                            mViewHolder.tvState.setText("已下载");
                            MuseumDownloadService.isDownloadOver = true;
                            mViewHolder.ivIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent1 = new Intent(DownloadActivity.this, MuseumHomePageActivity.class);
                                    String mId = String.valueOf(mViewHolder.ivIcon.getTag());
                                    intent1.putExtra(INTENT_MUSEUM_ID, mId);
                                    startActivity(intent1);
                                }
                            });
                        }
                        mViewHolder.progressBar.setProgress(progress);
                        mViewHolder.tvProgress.setText(progress + " %");
                    } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        /**检查网络，当网络中断时，暂停下载并提示*/
                        int netType = NetworkUtil.checkNet(DownloadActivity.this);
                        if (netType == MyApplication.INTERNET_TYPE_NONE) {
                            Intent intent1 = new Intent();
                            intent.setAction(ACTION_DOWNLOAD_PAUSE);
                            mViewHolder.ivCtrl.setBackgroundResource(R.mipmap.btn_download_pause);
                            sendBroadcast(intent1);
                            Toast.makeText(DownloadActivity.this, "网络已中断，请检查网络", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                ExceptionUtil.handleException(e);
            }
        }
    }

}

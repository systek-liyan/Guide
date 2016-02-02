package com.systek.guide.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.systek.guide.IConstants;
import com.systek.guide.biz.DownloadBiz;
import com.systek.guide.biz.DownloadTask;
import com.systek.guide.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/10/28.
 *
 * 博物馆数据下载类，暂已不用
 */
public class MuseumDownloadService  extends IntentService implements IConstants {


    private String museumId;
    private DownloadStateReceiver downloadStateReceiver;
    private Handler handler;
    List<DownloadTask> downloadTaskList;
    private DownloadBiz downloadBiz;
    private Vector<String> assetsList;
    private int totalCount;
    private int downloadCount;
    //判断assets是否下载完毕
    public boolean isDownloadOver;
    private int progress;

    public MuseumDownloadService() {
        super("download");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler=new MyHandler();
        downloadTaskList=new ArrayList<>();
        // 注册广播
        downloadStateReceiver = new DownloadStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_CONTINUE);
        filter.addAction(ACTION_DOWNLOAD_PAUSE);
        registerReceiver(downloadStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        // 取消广播
        unregisterReceiver(downloadStateReceiver);
        LogUtil.i("ZHANG", "MuseumDownloadService执行了onDestroy");
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.i("ZHANG", "已启动下载服务");
        museumId = intent.getStringExtra(INTENT_MUSEUM_ID);
        if(TextUtils.isEmpty(museumId)){return;}
        //sendProgress();
        /*DownloadTask downloadTask=new DownloadTask();
        downloadTaskList.add(downloadTask);
        downloadTask.download(museumId);*/
    }

    //广播接收器，用于接收用户操控下载状态
    private class DownloadStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 继续
            if (action.equals(ACTION_DOWNLOAD_CONTINUE)) {
                String id=intent.getStringExtra(INTENT_MUSEUM_ID);
                //继续
                if(downloadTaskList==null||downloadTaskList.size()==0){return;}
                for(DownloadTask task:downloadTaskList){
                    if(!task.getMuseumId().equals(museumId)){continue;}
                    downloadBiz.toContinue();
                }
                //downloadBiz.downloadPause =false;
            } else if (action.equals(ACTION_DOWNLOAD_PAUSE)) {
                // 暂停
                for(DownloadTask task:downloadTaskList){
                    if(!task.getMuseumId().equals(museumId)){continue;}
                    downloadBiz.pause();
                }
            }
        }
    }

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_WHAT_UPDATE_DATA_SUCCESS){
            }
        }
    }

}

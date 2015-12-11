package com.systekcn.guide.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.biz.BizFactory;
import com.systekcn.guide.biz.DownloadBiz;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.common.utils.Tools;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.LabelBean;
import com.systekcn.guide.entity.MapBean;

import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/10/28.
 */
public class MuseumDownloadService extends IntentService implements IConstants{

    /* 资源集合 */
    private Vector<String> assetsList;
    /* 判断assets是否下载完毕 */
    public static boolean isDownloadOver;
    /* 下载状态监听器 */
    DownloadStateReceiver downloadStateReceiver;
    /** 详细信息资源集合 */
    private List<BeaconBean> beaconList;
    private List<LabelBean> labelList;
    private List<ExhibitBean> exhibitList;
    private List<MapBean> mapList;
    private DownloadBiz downloadBiz;
    private String museumId;

    public MuseumDownloadService() {
        super("download");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册广播
        downloadStateReceiver = new DownloadStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_CONTINUE);
        filter.addAction(ACTION_DOWNLOAD_PAUSE);
        registerReceiver(downloadStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("ZHANG", "MuseumDownloadService执行了onDestroy, unregisterReceiver");
        // 取消广播
        unregisterReceiver(downloadStateReceiver);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.i("ZHANG", "已启动下载服务");
        try {
            String assetsJson = intent.getStringExtra(DOWNLOAD_ASSETS_KEY);
            museumId = intent.getStringExtra(DOWNLOAD_MUSEUMID_KEY);
            /**将展品详细信息保存至数据库*/
            saveAllJson(museumId);
			/** 创建下载业务对象，并开始下载 */
            downloadBiz = (DownloadBiz) BizFactory.getDownloadBiz(getApplicationContext());
            assetsList = downloadBiz.parseAssetsJson(assetsJson);
            downloadBiz.count = assetsList.size();
            downloadBiz.downloadAssets(assetsList, 0, assetsList.size(),museumId);
            sendProgress();
        } catch (Exception e) {
            ExceptionUtil.handleException(e);
            Toast.makeText(getApplicationContext(), "数据获取异常", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveAllJson(String museumId) {
        getJsonForDetailMuseum(museumId);
        while(beaconList==null||beaconList.size()<=0
                ||labelList==null||labelList.size()<=0
                ||exhibitList==null||exhibitList.size()<=0
                ||mapList==null||mapList.size()<=0
                ){}
        saveAllAssetsList();
        LogUtil.i("ZHANG","所有json数据存储成功");
    }
    private void sendProgress() {
        while(!isDownloadOver){
            /* 当下载未完成时，每秒发送一条广播以更新进度条 */
            int progress;
            progress = (assetsList.size() + 1 - downloadBiz.count) * 100 / assetsList.size();
            Intent in = new Intent();
            in.setAction(ACTION_PROGRESS);
            in.putExtra(ACTION_PROGRESS, progress);// currentSize*100/totalSize
            sendBroadcast(in);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ExceptionUtil.handleException(e);
            }
        }
        /**下载完毕，存储状态*/
        Tools.saveValue(this, museumId, "true");
        LogUtil.i("ZHANG","下载状态已保存");
    }

    /** 获取博物馆所有展品的详细信息的json */
    private void getJsonForDetailMuseum(String museumId) {

        HttpUtils http = new HttpUtils();
        DbUtils db=DbUtils.create(this);
        try {
            beaconList=db.findAll(BeaconBean.class);
            if(beaconList!=null&&beaconList.size()>0){
                db.deleteAll(beaconList);
            }
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        try {
            labelList=db.findAll(LabelBean.class);
            if(labelList!=null&&labelList.size()>0){
                db.deleteAll(labelList);
            }
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        try {
            exhibitList=db.findAll(ExhibitBean.class);
            if(exhibitList!=null&&exhibitList.size()>0){
                db.deleteAll(exhibitList);
            }
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        try {
            mapList=db.findAll(MapBean.class);
            if(mapList!=null&&mapList.size()>0){
                db.deleteAll(mapList);
            }
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }finally {
            if(db!=null){
                db.close();
            }
        }

        http.send(HttpRequest.HttpMethod.GET,BEACON_URL + museumId , new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    beaconList = JSON.parseArray(responseInfo.result, BeaconBean.class);
                    LogUtil.i("ZHANG","beaconList获取成功");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }
            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("下载JSON-BEACON_URL-获取失败" + error.toString(), msg);
            }
        });
        http.send(HttpRequest.HttpMethod.GET, LABELS_URL + museumId, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    labelList = JSON.parseArray(responseInfo.result, LabelBean.class);
                    LogUtil.i("ZHANG", "labelList获取成功");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("下载JSON-LABELS_URL-获取失败" + error.toString(), msg);
            }
        });
        http.send(HttpRequest.HttpMethod.GET, EXHIBIT_LIST_URL + museumId, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    exhibitList = JSON.parseArray(responseInfo.result, ExhibitBean.class);
                    LogUtil.i("ZHANG", "exhibitList获取成功");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("下载JSON-EXHIBIT_URL-获取失败" + error.toString(), msg);
            }
        });
        http.send(HttpRequest.HttpMethod.GET, MUSEUM_MAP_URL + museumId, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                try {
                    mapList = JSON.parseArray(responseInfo.result, MapBean.class);
                    LogUtil.i("ZHANG", "mapList获取成功");
                } catch (Exception e) {
                    ExceptionUtil.handleException(e);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("下载JSON-MUSEUM_MAP_URL-获取失败" + error.toString(), msg);
            }
        });
    }

    /** 保存所有详细信息至数据库 */
    private void saveAllAssetsList() {

        DbUtils db = DbUtils.create(this);
        try {
            db.saveAll(beaconList);
            db.saveAll(labelList);
            db.saveAll(exhibitList);
            db.saveAll(mapList);
        } catch (DbException e) {
            ExceptionUtil.handleException(e);
        }
        if (db != null) {
            db.close();
        }
        LogUtil.i("ZHANG", "json已保存至数据库");
    }

    /* 广播接收器，用于接收用户操控下载状态 */
    private class DownloadStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
			/* 继续 */
            if (action.equals(ACTION_DOWNLOAD_CONTINUE)) {
                String id=intent.getStringExtra(ACTION_DOWNLOAD_CONTINUE);
                downloadBiz.downloadAssets(assetsList, assetsList.size() - downloadBiz.count, assetsList.size(),id);
				/* 暂停 */
            } else if (action.equals(ACTION_DOWNLOAD_PAUSE)) {
                for (int i = 0; i < downloadBiz.httpHandlerList.size(); i++) {
                    if (downloadBiz.httpHandlerList.get(i) != null
                            && !downloadBiz.httpHandlerList.get(i).isCancelled()) {
                        downloadBiz.httpHandlerList.get(i).cancel();
                    }
                }
            }
        }
    }
}

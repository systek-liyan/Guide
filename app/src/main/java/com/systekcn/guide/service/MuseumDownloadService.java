package com.systekcn.guide.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.MyApplication;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.ExceptionUtil;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.entity.BeaconBean;
import com.systekcn.guide.entity.ExhibitBean;
import com.systekcn.guide.entity.LabelBean;
import com.systekcn.guide.entity.MapBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/10/28.
 */
public class MuseumDownloadService extends IntentService implements IConstants{

    /**
     * 当前下载博物馆ID
     */
    String museumId="deadccf89ef8412a9c8a2628cee28e18";
    /**
     * 资源路径JSON
     */
    private String assetsJson;
    /**
     * 判断JSON请求是否完毕
     */
    boolean isJsonGetOver;
    /**
     * 下载资源总大小
     */
    int totalSize;
    /**
     * 下载资源集合
     */
    Vector<String> assetsVector;
    /**
     * 下载开始时间
     */
    private long startTime;
    /**
     * 控制下载工具的handler
     */
    private ArrayList<HttpHandler<File>> httpHandlerList;
    /**
     * 启动下载的线程数
     */
    private int maxDownloadThread = 3;
    /**
     * 下载资源个数
     */
    private int fileCount;
    /**
     * 下载状态监听器
     */
    private DownloadStateReceiver downloadStateReceiver;
    private boolean isDownloadOver;
    private ArrayList<BeaconBean> beaconList;
    private ArrayList<LabelBean> labelList;
    private ArrayList<ExhibitBean> exhibitList;
    private ArrayList<MapBean> mapList;
    private MyApplication application;

    private int fileTotalcount;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public MuseumDownloadService() {
        super("MuseumDownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

       /* application= (MyApplication) getApplication();
        museumId = intent.getStringExtra(INTENT_STRING_DATA_MUSEUM_ID);*/// TODO: 2015/11/1
        getDownloadJson();
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10000 || !isJsonGetOver || assetsJson == null) {
        }
        if (assetsJson == null) {
            Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
            onDestroy();// TODO: 2015/11/2
        } else {
            JSONObject jsonObj = JSON.parseObject(assetsJson);
            totalSize = jsonObj.getInteger("size");
            String jsonString = jsonObj.getString("url");
            List<String> list = JSON.parseArray(jsonString, String.class);
            fileCount = list.size();
            fileTotalcount=fileCount;
            assetsVector = new Vector<>(list);
            downloadAssets(assetsVector, 0, fileCount,museumId);
            sendProgress();
            while(!isDownloadOver){}
            saveAllJson();
        }
    }

    private void sendProgress() {
        new Thread(){
            @Override
            public void run() {
               /* 当下载未完成时，每秒发送一条广播以更新进度条 */
                int progress;
                while (!isDownloadOver) {
                    progress = (fileTotalcount + 1 - fileCount) * 100 / fileTotalcount;
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
            }
        }.start();
    }

    private void saveAllJson() {
        getJsonforDetailMuseum(museumId);
        while(beaconList==null||beaconList.size()<=0
                ||labelList==null||labelList.size()<=0
                ||exhibitList==null||exhibitList.size()<=0
                ||mapList==null||mapList.size()<=0
                ){}
        saveAllAssetsList(museumId);

    }

    /** 保存所有详细信息至数据库 */
    private void saveAllAssetsList(String id) {

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
        LogUtil.i("json已保存至数据库", "-------------------------------");
    }

    private void getJsonforDetailMuseum(String museumId) {
        {
            HttpUtils http = new HttpUtils();
            http.send(HttpRequest.HttpMethod.GET,BEACON_URL + museumId , new RequestCallBack<String>() {

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
                        beaconList = (ArrayList<BeaconBean>) JSON.parseArray(responseInfo.result, BeaconBean.class);
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
                        labelList = (ArrayList<LabelBean>) JSON.parseArray(responseInfo.result, LabelBean.class);
                        LogUtil.i("ZHANG","labelList获取成功");
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
                        exhibitList = (ArrayList<ExhibitBean>) JSON.parseArray(responseInfo.result, ExhibitBean.class);
                        LogUtil.i("ZHANG","exhibitList获取成功");
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
                        mapList = (ArrayList<MapBean>) JSON.parseArray(responseInfo.result, MapBean.class);
                        LogUtil.i("ZHANG","mapList获取成功");
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
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册广播
        downloadStateReceiver = new DownloadStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONTINUE);
        filter.addAction(ACTION_PAUSE);
        registerReceiver(downloadStateReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消广播
        unregisterReceiver(downloadStateReceiver);
    }

    /* 下载assets中数据 */
    private void downloadAssets(Vector<String> assetsList, int start, int end, String museumId) {

        startTime = System.currentTimeMillis();
        httpHandlerList = new ArrayList<>();
        LogUtil.i("downloadAssets开始执行", "------当前时间为" + startTime + "文件个数" + fileCount);
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(getMaxDownloadThread());
        String imgPath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_IMAGE + "/";
        String lyricPath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_LYRIC + "/";
        String audioPath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_AUDIO + "/";
        String str = null;
        String fileName = null;
        String savePath = null;
        String url = null;
        /* 遍历集合并下载 */
        for (int i = start; i < end; i++) {
            str = assetsList.get(i);
            if (str.endsWith(".jpg") || str.endsWith(".png")) {
                fileName = str.replaceAll("/", "_");
                savePath = imgPath + fileName;
                url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else if (str.endsWith(".lrc")) {
                fileName = str.replaceAll("/", "_");
                savePath = lyricPath + fileName;
                url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else if (str.endsWith(".mp3") || str.endsWith(".wav")) {
                fileName = str.replaceAll("/", "_");
                savePath = audioPath + fileName;
                url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else {
                LogUtil.i("文件后缀异常", "------------------------------------------");
                fileCount--;
            }
        }
    }

    private void downloadFile(HttpUtils http, String savePath, final String url) {

        HttpHandler<File> httpHandler = http.download(url, savePath, true, true, new RequestCallBack<File>() {

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                fileCount--;
                if (url.endsWith(".jpg")) {
                    LogUtil.i("jpg文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + fileCount);
                } else if (url.endsWith(".png")) {
                    LogUtil.i("png文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + fileCount);
                } else if (url.endsWith(".lrc")) {
                    LogUtil.i("lrc文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + fileCount);
                } else if (url.endsWith(".mp3")) {
                    LogUtil.i("mp3文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + fileCount);
                } else if (url.endsWith(".wav")) {
                    LogUtil.i("wav文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + fileCount);
                }
                if (fileCount <= 0) {
                    long cost = System.currentTimeMillis() - startTime;
                    LogUtil.i("下载执行完毕", "用时----------------" + cost / 1000 + "秒");
                    isDownloadOver = true;
                    Intent in = new Intent();
                    in.setAction(ACTION_PROGRESS);
                    in.putExtra(ACTION_PROGRESS, 100);// currentSize*100/totalSize
                    sendBroadcast(in);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("文件下载失败" + error.toString(), msg);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                return;
            }

        });
        httpHandlerList.add(httpHandler);
    }


    private int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    private void getDownloadJson() {
        HttpUtils http = new HttpUtils();
        // TODO: 2015/10/28  
        String url = URL_ALL_MUSEUM_ASSETS + "deadccf89ef8412a9c8a2628cee28e18";//+ museumId;
        http.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                assetsJson = responseInfo.result;
                isJsonGetOver = true;
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("下载所需JSON获取失败" + error.toString(), msg);
                isJsonGetOver = true;
            }
        });
    }

    /* 广播接收器，用于接收用户操控下载状态 */
    private class DownloadStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
			/* 继续 */
            if (action.equals(ACTION_CONTINUE)) {
                String id = intent.getStringExtra(ACTION_CONTINUE);
                downloadAssets(assetsVector, assetsVector.size() - fileCount, assetsVector.size(), id);
				/* 暂停 */
            } else if (action.equals(ACTION_PAUSE)) {
                for (int i = 0; i < httpHandlerList.size(); i++) {
                    if (httpHandlerList.get(i) != null
                            && !httpHandlerList.get(i).isCancelled()) {
                        httpHandlerList.get(i).cancel();
                    }
                }
            }
        }

    }

}

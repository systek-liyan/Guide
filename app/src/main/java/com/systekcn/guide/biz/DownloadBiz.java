package com.systekcn.guide.biz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.systekcn.guide.common.IConstants;
import com.systekcn.guide.common.utils.LogUtil;
import com.systekcn.guide.service.MuseumDownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/11/17.
 */
public class DownloadBiz implements IConstants{

    private Context context;
    /* 控制下载的handler集合 */
    public ArrayList<HttpHandler<File>> httpHandlerList;
    /**下载文件个数*/
    public  int count;
    /* 下载开始时间 */
    long startTime;
    private String assetsJson;
    /* 启动下载的线程数 */
    private int maxDownloadThread = 3;
    private int fileCount;
    private int totalSize;
    private int fileTotalCount;
    private String mMuseumId;


    public DownloadBiz(Context context) {
        this.context = context;
    }

    public void download(final String museumId) {

        new Thread(){
            public void run() {
				/* 获取所资源地址 */
                mMuseumId=museumId;
                assetsJson = getAssetsJSON(museumId);
				/* 下载资源 */
                if(assetsJson!=null&&!assetsJson.equals("")){
                    downloadAssets(assetsJson,museumId);
                }else{
                    Toast.makeText(context,"下载失败，请检查网络状态",Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    public int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    public void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }


    /** 获得assets资源json */
    private String getAssetsJSON(String id) {

        LogUtil.i("ZHANG", "getAssetsJSO开始执行");
        HttpUtils http = new HttpUtils();
        long startTime=System.currentTimeMillis();
        http.send(HttpRequest.HttpMethod.GET, URL_ALL_MUSEUM_ASSETS + id, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                assetsJson = responseInfo.result;
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("getAssetsJSON失败" + error.toString(), msg);
            }
        });
        while (assetsJson == null) {
            if(System.currentTimeMillis()-startTime>15000){
                break;
            }
        }
        return assetsJson;
    }

    /** 下载assets的方法，启动下载服务 */
    private void downloadAssets(String assetsJson2,String id) {
        Intent intent = new Intent(context, MuseumDownloadService.class);
        intent.putExtra(DOWNLOAD_ASSETS_KEY, assetsJson2);
        intent.putExtra(DOWNLOAD_MUSEUMID_KEY, id);
        context.startService(intent);
    }

    /** 解析assets资源json */
    public Vector<String> parseAssetsJson(String assetsJson2) {
        JSONObject jsonObj = JSON.parseObject(assetsJson2);
        totalSize = jsonObj.getInteger("size");
        String jsonString = jsonObj.getString("url");
        List<String> list = JSON.parseArray(jsonString, String.class);
        fileCount = list.size();
        fileTotalCount =fileCount;
        Vector<String>  vector= new Vector<>(list);
        return vector;
    }

    /* 下载assets中数据 */
    public void downloadAssets(Vector<String> assetsList, int start, int end,String id) {

        startTime = System.currentTimeMillis();
        httpHandlerList = new ArrayList<>();
        LogUtil.i("downloadAssets开始执行", "------当前时间为" + startTime + "文件个数" + count);

        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(getMaxDownloadThread());
        String str = "";
        String savePath = "";
		/* 遍历集合并下载 */
        for (int i = start; i < end; i++) {
            str = assetsList.get(i);
            if (str.endsWith(".jpg")||str.endsWith(".png")) {
                savePath = LOCAL_ASSETS_PATH +id+"/"+LOCAL_FILE_TYPE_IMAGE+ str.substring(str.lastIndexOf("/"));
                final String url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else if (str.endsWith(".lrc")) {
                savePath = LOCAL_ASSETS_PATH+id+"/" +LOCAL_FILE_TYPE_LYRIC+ str.substring(str.lastIndexOf("/"));
                final String url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else if (str.endsWith(".mp3") || str.endsWith(".wav")) {
                savePath = LOCAL_ASSETS_PATH+id+"/" +LOCAL_FILE_TYPE_AUDIO+ str.substring(str.lastIndexOf("/"));
                final String url = BASEURL + assetsList.get(i);
                downloadFile(http, savePath, url);
            } else {
                LogUtil.i("文件后缀异常", "------------------------------------------");
                count--;
            }
        }
    }

    private void downloadFile(HttpUtils http, String savePath, final String url) {

        HttpHandler<File> httpHandler = http.download(url, savePath, true, true, new RequestCallBack<File>() {

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                count--;
                if (url.endsWith(".jpg")) {
                    LogUtil.i("jpg文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
                }else if (url.endsWith(".png")) {
                    LogUtil.i("png文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
                }else if (url.endsWith(".lrc")) {
                    LogUtil.i("lrc文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
                } else if (url.endsWith(".mp3")) {
                    LogUtil.i("mp3文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
                } else if (url.endsWith(".wav")) {
                    LogUtil.i("wav文件下载成功", url.substring(url.lastIndexOf("/") + 1) + "剩余个数" + count);
                }
                if (count <= 0) {
                    long cost = System.currentTimeMillis() - startTime;
                    LogUtil.i("下载执行完毕", "用时----------------" + cost / 1000 + "秒");
                    Intent in = new Intent();
                    in.setAction(ACTION_PROGRESS);
                    in.putExtra(ACTION_PROGRESS, 100);// currentSize*100/totalSize
                    context.sendBroadcast(in);
                    SharedPreferences setting = context.getSharedPreferences("setting",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = setting.edit();
                    editor.putBoolean(mMuseumId,true);
                    editor.commit();
                    LogUtil.i("ZHANG","下载状态已保存");
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                LogUtil.i("文件下载失败" + error.toString(), msg);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
            }

        });
        httpHandlerList.add(httpHandler);
    }
}

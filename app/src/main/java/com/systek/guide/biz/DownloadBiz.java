package com.systek.guide.biz;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systek.guide.IConstants;
import com.systek.guide.utils.MyHttpUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Qiang on 2015/11/17.
 *
 * 下载业务类
 */
public class DownloadBiz implements IConstants {

    private int totalSize;
    public  boolean downloadPause;
    /**下载文件个数*/
    private int count;
    public boolean downloadOver=false;


    public void setAssetsList(List<String> assetsList) {
        this.assetsList = assetsList;
    }

    private List<String> assetsList;


    public int getProgress(){
        if(totalSize==0){return 0;}
        return count*100/totalSize>100?100:count;
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id) {
        String url=BASE_URL+URL_ALL_MUSEUM_ASSETS + id;
        return MyHttpUtil.doGet(url);
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id,String baseUrl) {
        String url=baseUrl+URL_ALL_MUSEUM_ASSETS + id;
        return MyHttpUtil.sendGet(url);
    }


    /** 解析assets资源json */
    public List<String> parseAssetsJson(String assetsJson) {
        if(TextUtils.isEmpty(assetsJson)){return null;}
        JSONObject jsonObj = JSON.parseObject(assetsJson);
        String jsonString = jsonObj.getString("url");
        if(TextUtils.isEmpty(jsonString)){return null;}
        return JSON.parseArray(jsonString, String.class);
    }

    /** 解析assets资源json */
    public long parseAssetsSize(String assetsJson) {
        JSONObject jsonObj = JSON.parseObject(assetsJson);
        String size = jsonObj.getString("size");
        if(size==null){return 0;}
        return Long.valueOf(size);
    }


    public void pause(){
        downloadPause=true;
    }
    public void toContinue(){
        downloadPause=false;
    }

    public synchronized String getDownloadUrl(){
        if(assetsList!=null&&assetsList.size()>0){
            String url=assetsList.get(0);
            assetsList.remove(0);
            return url;
        }
        return null;
    }



    /**
     * 现在资源文件
     * @param assetsList 资源列表
     * @param museumId 博物馆id
     */
    public void downloadAssets(List<String> assetsList, String museumId) {

        setAssetsList(assetsList);

        ThreadPoolExecutor poolExecutor  = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        DownloadAssetsFileTask task1=new DownloadAssetsFileTask(this,museumId);
        DownloadAssetsFileTask task2=new DownloadAssetsFileTask(this,museumId);
        DownloadAssetsFileTask task3=new DownloadAssetsFileTask(this,museumId);
        DownloadAssetsFileTask task4=new DownloadAssetsFileTask(this,museumId);

        poolExecutor.execute(task1);
        poolExecutor.execute(task2);
        poolExecutor.execute(task3);
        poolExecutor.execute(task4);


    }


    public void downloadAssets(List<String> assetsList, String museumId,String baseUrl) {

        setAssetsList(assetsList);

        ThreadPoolExecutor poolExecutor  = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        DownloadAssetsFileTask task1=new DownloadAssetsFileTask(this,museumId,baseUrl);
        DownloadAssetsFileTask task2=new DownloadAssetsFileTask(this,museumId,baseUrl);
        DownloadAssetsFileTask task3=new DownloadAssetsFileTask(this,museumId,baseUrl);
        DownloadAssetsFileTask task4=new DownloadAssetsFileTask(this,museumId,baseUrl);

        poolExecutor.execute(task1);
        poolExecutor.execute(task2);
        poolExecutor.execute(task3);
        poolExecutor.execute(task4);

    }

}

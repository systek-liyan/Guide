package com.systek.guide.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.systek.guide.IConstants;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.MyHttpUtil;
import com.systek.guide.utils.Tools;

import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2015/11/17.
 *
 * 下载业务类
 */
public class DownloadBiz implements IConstants {

    /* 下载开始时间 */
    long startTime;
    private String assetsJson;
    private int totalSize;
    public  boolean downloadPause;
    /**下载文件个数*/
    private int count;
    public boolean downloadOver=false;

    public int getProgress(){
        if(totalSize==0){return 0;}
        return count*100/totalSize>100?100:count;
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id) {
        long startTime=System.currentTimeMillis();
        String url=BASE_URL+URL_ALL_MUSEUM_ASSETS + id;

        //assetsJson=MyHttpUtil.sendGet(url);
        assetsJson=MyHttpUtil.doGet(url);
        while (assetsJson == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                ExceptionUtil.handleException(e);
            }
            if(System.currentTimeMillis()-startTime>8000){
                LogUtil.i("ZHANG","getAssetsJSON失败");
                break;
            }
        }
        return assetsJson;
    }

    /** 获得assets资源json */
    public String getAssetsJSON(String id,String baseUrl) {
        long startTime=System.currentTimeMillis();
        String url=baseUrl+URL_ALL_MUSEUM_ASSETS + id;
        assetsJson=MyHttpUtil.sendGet(url);
        while (assetsJson == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                ExceptionUtil.handleException(e);
            }
            if(System.currentTimeMillis()-startTime>8000){
                LogUtil.i("ZHANG","getAssetsJSON失败");
                break;
            }
        }
        return assetsJson;
    }


    /** 解析assets资源json */
    public Vector<String> parseAssetsJson(String assetsJson) {
        JSONObject jsonObj = JSON.parseObject(assetsJson);
        String jsonString = jsonObj.getString("url");
        List<String> list = JSON.parseArray(jsonString, String.class);
        return new Vector<>(list);
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

    String str = "";
    String savePath = "";

    /**
     * 现在资源文件
     * @param assetsList 资源列表
     * @param start 开始下表
     * @param end 结束下标
     * @param museumId 博物馆id
     */
    public void downloadAssets(Vector<String> assetsList, int start, int end,String museumId) {
        startTime = System.currentTimeMillis();
        totalSize=assetsList.size();
        LogUtil.i("ZHANG", "downloadAssets开始执行------当前时间为" + startTime + "文件个数" + (totalSize-count));
        String url="";
        String name="";
		/* 遍历集合并下载 */
        for (int i = start; i < end; i++) {
            str = assetsList.get(i);
            if (str.endsWith(".jpg")||str.endsWith(".png")) {
                savePath = LOCAL_ASSETS_PATH +museumId+"/"+LOCAL_FILE_TYPE_IMAGE;
                url = BASE_URL + assetsList.get(i);
            } else if (str.endsWith(".lrc")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_LYRIC;
                url = BASE_URL + assetsList.get(i);
            } else if (str.endsWith(".mp3") || str.endsWith(".wav")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_AUDIO;
                url = BASE_URL + assetsList.get(i);
            } else {
                LogUtil.i("ZHANG", "文件后缀异常-----------------");
                continue;
            }
            String fileName=Tools.changePathToName(str);
            if(Tools.isFileExist(savePath+"/"+fileName)){
                count++;
                continue;
            }
            while(downloadPause){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ExceptionUtil.handleException(e);
                }
            }
            downloadFile(savePath,fileName, url);
//            DataBiz.saveTempValue(MyApplication.get(),SP_DOWNLOAD_MUSEUM_COUNT,totalSize-count);
        }
        downloadOver=true;
    }


    public void downloadAssets(Vector<String> assetsList, int start, int end,String museumId,String baseUrl) {
        startTime = System.currentTimeMillis();
        totalSize=assetsList.size();
        LogUtil.i("ZHANG", "downloadAssets开始执行-当前时间为" + startTime + "文件个数" + (totalSize-count));
        String url="";
        String name="";
		/* 遍历集合并下载 */
        for (int i = start; i < end; i++) {
            str = assetsList.get(i);
            if (str.endsWith(".jpg")||str.endsWith(".png")) {
                savePath = LOCAL_ASSETS_PATH +museumId+"/"+LOCAL_FILE_TYPE_IMAGE;
                url = baseUrl + assetsList.get(i);
            } else if (str.endsWith(".lrc")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_LYRIC;
                url = baseUrl + assetsList.get(i);
            } else if (str.endsWith(".mp3") || str.endsWith(".wav")) {
                savePath = LOCAL_ASSETS_PATH+museumId+"/" +LOCAL_FILE_TYPE_AUDIO;
                url = baseUrl + assetsList.get(i);
            } else {
                LogUtil.i("ZHANG", "文件后缀异常-----------------");
                continue;
            }
            String fileName=Tools.changePathToName(str);
            if(Tools.isFileExist(savePath+"/"+fileName)){
                count++;
                continue;
            }
            while(downloadPause){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ExceptionUtil.handleException(e);
                }
            }
            downloadFile(savePath,fileName, url);
//            DataBiz.saveTempValue(MyApplication.get(),SP_DOWNLOAD_MUSEUM_COUNT,totalSize-count);
        }
        downloadOver=true;
    }




    /**
     * 下载文件
     * @param savePath 保存路径
     * @param name 文件名称
     * @param url
     */
    private void downloadFile(String savePath,String name, final String url) {
        try {

            /*HttpUtils httpUtils=new HttpUtils();
            httpUtils.download(url,savePath,null);*/
            MyHttpUtil.downloadFile(url, savePath ,name);
            //MyHttpUtil.downLoadFromUrl(url,name,savePath);
            LogUtil.i("ZHANG","已下载文件"+name);
            count++;
        } catch (Exception e) {
            LogUtil.i("zhang","下载失败");
            ExceptionUtil.handleException(e);
        }
       /* if (url.endsWith(".jpg")) {
            LogUtil.i("ZHANG","jpg文件下载成功"+ url.substring(url.lastIndexOf("/") + 1) + "下载个数" + count);
        }else if (url.endsWith(".png")) {
            LogUtil.i("ZHANG","png文件下载成功"+ url.substring(url.lastIndexOf("/") + 1) + "下载个数" + count);
        }else if (url.endsWith(".lrc")) {
            LogUtil.i("ZHANG","lrc文件下载成功"+ url.substring(url.lastIndexOf("/") + 1) + "下载个数" + count);
        } else if (url.endsWith(".mp3")) {
            LogUtil.i("ZHANG","mp3文件下载成功"+ url.substring(url.lastIndexOf("/") + 1) + "下载个数" + count);
        } else if (url.endsWith(".wav")) {
            LogUtil.i("ZHANG","wav文件下载成功"+ url.substring(url.lastIndexOf("/") + 1) + "下载个数" + count);
        }*/
    }
}

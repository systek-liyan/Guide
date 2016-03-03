package com.systek.guide.biz;

import android.text.TextUtils;

import com.systek.guide.IConstants;
import com.systek.guide.utils.ExceptionUtil;

import java.util.List;
import java.util.Vector;

/**
 * Created by Qiang on 2016/1/5.
 *
 * 下载任务类
 */
public class MyDownloadTask extends Thread implements  IConstants {

    //下载状态监听器
    private String museumId;
    private TaskListener listener;
    private Vector<TaskListener> taskListeners;
    private DownloadBiz downloadBiz;
    private List<String> assetsList;
    private int totalCount;
    private int downloadCount;
    private boolean breakByUser;

    public boolean isDownloadState() {
        return downloadState;
    }

    private boolean downloadState;


    public String getMuseumId() {
        return museumId;
    }

    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public MyDownloadTask(String museumId){
        this.museumId=museumId;
        taskListeners=new Vector<>();
    }


    /*public synchronized String getDownloadUrl(){
        if(assetsList!=null&&assetsList.size()>0){
            String url=assetsList.get(0);
            assetsList.remove(0);
            return url;
        }
        return null;
    }
*/

    public void addListener(TaskListener l) {
        taskListeners.add(l);
    }
    public void removeListener(TaskListener l){
        taskListeners.remove(l);
    }
    public TaskListener getListener() {
        return listener;
    }
    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        if(TextUtils.isEmpty(museumId)){return;}
        /*/*//* 创建下载业务对象，并开始下载
        downloadBiz = (DownloadBiz) BizFactory.getDownloadBiz();
        String assetsJson=null;
        if(baseUrl!=null){
            assetsJson=downloadBiz.getAssetsJSON(museumId,"http://"+baseUrl);
        }else{
            assetsJson=downloadBiz.getAssetsJSON(museumId);
        }
        if(TextUtils.isEmpty(assetsJson)){return;}// TODO: 2016/1/20 获取资源数据失败
        long size=downloadBiz.parseAssetsSize(assetsJson);
        if(StorageUtil.getAvailableInternalMemorySize()<size){return;}// TODO: 2016/1/20 内部存储空间不足 下载失败
        assetsList = downloadBiz.parseAssetsJson(assetsJson);
        if(assetsList==null||assetsList.size()==0){return;}// TODO: 2016/1/20 解析后没有资源数据
        totalCount=assetsList.size();
        downloadCount = totalCount;
        sendProgress();
        if(TextUtils.isEmpty(baseUrl)){
            downloadBiz.downloadAssets(assetsList, museumId);
        }else{
            downloadBiz.downloadAssets(assetsList, museumId,"http://"+baseUrl);
        }
        downloadState=true;
        /*//*下载完毕，存储状态
        Tools.saveValue(MyApplication.get(), museumId, true);
        LogUtil.i("ZHANG", "下载状态已保存");
        if(!taskListeners.isEmpty()){
            for(TaskListener l:taskListeners){
                l.onProgressChanged(downloadBiz.getProgress());
            }
        }*/
    }


    public void pause(){
        downloadBiz.pause();
    }

    public void toContinue(){
        downloadBiz.toContinue();
    }

    public  interface TaskListener {
        void onProgressChanged(final int progress);
    }

    public void sendProgress() {
        new Thread(){
            @Override
            public void run() {
                while(downloadBiz!=null&&!downloadBiz.downloadOver){
                    if(breakByUser){break;}
                    //当下载未完成时，每秒发送一条广播以更新进度条
                    if(!taskListeners.isEmpty()){
                        for(TaskListener l:taskListeners){
                            l.onProgressChanged(downloadBiz.getProgress());
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        ExceptionUtil.handleException(e);
                    }
                }
            }
        }.start();
    }

    public void disSendProgress(){
        breakByUser=true;
    }

}

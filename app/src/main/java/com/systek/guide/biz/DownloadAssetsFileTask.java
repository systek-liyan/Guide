package com.systek.guide.biz;

import android.text.TextUtils;

import com.systek.guide.IConstants;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.LogUtil;
import com.systek.guide.utils.MyHttpUtil;
import com.systek.guide.utils.Tools;

import java.io.IOException;

/**
 * Created by Qiang on 2016/3/1.
 *
 */
public class DownloadAssetsFileTask implements Runnable,IConstants {


    DownloadBiz downloadBiz;
    String baseUrl;
    String museumId;

    public DownloadAssetsFileTask(DownloadBiz downloadBiz, String museumId) {
        this(downloadBiz,museumId, BASE_URL);
    }

    public DownloadAssetsFileTask(DownloadBiz downloadBiz, String museumId, String baseUrl) {
        this.museumId = museumId;
        this.downloadBiz = downloadBiz;
        this.baseUrl = baseUrl;
    }


    @Override
    public void run() {

        while (downloadBiz != null ) {
            String url=null;
            try {
                url=downloadBiz.getDownloadUrl();
                if(TextUtils.isEmpty(url)){break;}
                downloadAndSaveFile(url);
            } catch (Exception e) {
                ExceptionUtil.handleException(e);
                LogUtil.i("ZHANG","文件下载失败，url--"+url);
            }
        }

        LogUtil.i("ZHANG","assets文件下载完毕");

    }

    /**
     * 下载保存文件
     * @param url 下载url
     * @return 是否下载成功
     * @throws IOException IO异常
     */
    private boolean downloadAndSaveFile(String url) throws IOException {
        String savePath = null;
        if (url.endsWith(".jpg") || url.endsWith(".png")) {
            savePath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_IMAGE;
        } else if (url.endsWith(".lrc")) {
            savePath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_LYRIC;
        } else if (url.endsWith(".mp3") || url.endsWith(".wav")) {
            savePath = LOCAL_ASSETS_PATH + museumId + "/" + LOCAL_FILE_TYPE_AUDIO;
        } else {
            LogUtil.i("ZHANG", "文件后缀异常-----------------");
            return false;
        }
        String absoluteUrl = baseUrl + url;
        String fileName = Tools.changePathToName(url);
        if (!Tools.isFileExist(savePath + "/" + fileName)) {
            MyHttpUtil.downLoadFromUrl(absoluteUrl, savePath, fileName);
        }
        return true;
    }


}
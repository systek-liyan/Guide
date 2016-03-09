package com.systek.guide.download;

import android.text.TextUtils;

import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

/**
 * Created by Qiang on 2016/3/7.
 *
 * 下载列队
 */
public class MyFileDownloadQueueSet extends FileDownloadQueueSet {


    private int downloadId;
    private String url;
    private String path;

    public TaskItemViewHolder getHolder() {
        return holder;
    }

    public void setHolder(TaskItemViewHolder holder) {
        this.holder = holder;
    }

    private TaskItemViewHolder holder;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



    /**
     * Get download id (generate by url & path)
     * id生成与url和path相关
     *
     * @return 获得有效的对应当前download task的id
     */
    public int getDownloadId() {
        // TODO 这里和savePah有关，但是savePath如果为空在start以后会重新生成因此有坑
        if (downloadId != 0) {
            return downloadId;
        }

        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(url)) {
            return downloadId = FileDownloadUtils.generateId(url, path);
        }

        return 0;
    }


    /**
     * @param target for all tasks callback status change
     */
    public MyFileDownloadQueueSet(FileDownloadListener target) {
        super(target);
    }




}

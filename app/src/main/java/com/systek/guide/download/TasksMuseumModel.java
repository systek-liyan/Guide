package com.systek.guide.download;

import android.content.ContentValues;

/**
 * Created by Qiang on 2016/3/8.
 *
 * 下载任务实体类
 */
public class TasksMuseumModel {

    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String URL = "url";
    public final static String PATH = "path";
    public final static String MUSEUM_ID = "museumId";
    public final static String ICON_URL = "iconUrl";
    public final static String STATUS = "status";
    public final static String PROGRESS = "progress";
    public final static String TOTAL = "total";

    private int id;
    private int downloadId;
    private String name;
    private String url;
    private String path;
    private String iconUrl;
    private String museumId;

    private int status;
    private float progress;
    private int total;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getMuseumId() {
        return museumId;
    }

    public void setMuseumId(String museumId) {
        this.museumId = museumId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(NAME, name);
        cv.put(URL, url);
        cv.put(PATH, path);
        cv.put(MUSEUM_ID,museumId);
        cv.put(ICON_URL,iconUrl);
        cv.put(STATUS,status);
        cv.put(PROGRESS,progress);
        cv.put(TOTAL,total);
        return cv;
    }
}

package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * 博物馆实体类
 */

public class MuseumBean extends BaseEntity {



    //@Column(column = "id")
	private String id;
    //@Column(column = "name")
	private String name;// 博物馆名称
    //@Column(column = "longitudX")
    private double longitudX;// 表示博物馆纬度坐标
    //@Column(column = "longitudY")
    private double longitudY;// 表示博物馆经度坐标
    //@Column(column = "iconUrl")
    private String iconUrl;// icon的Url地址
    //@Column(column = "address")
    private String address;// 博物馆地址
    //@Column(column = "opentime")
    private String opentime;// 博物馆开放时间
   // @Column(column = "isOpen")
    private String isOpen;// 当前博物馆是否开放
   // @Column(column = "textUrl")
    private String textUrl;//博物馆简介
    //@Column(column = "floorCount")
    private int floorCount;//楼层数
    //@Column(column = "imgUrl")
    private String imgUrl;//图片url
    //@Column(column = "audioUrl")
    private String audioUrl;//简介url
    //@Column(column = "city")
    private String city;//所在城市
   // @Column(column = "version")
    private int version;//版本
    //@Column(column = "priority")
    private int priority ;//优先级
   // @Column(column = "isDownload")
    private boolean isDownload;//是否已经下载

    private int mState;//下载状态


    private int downloadId;//下载状态

    private long progress;

    private long fileLength;

    private boolean autoResume;

    private boolean autoRename;

    public String getId() {
        return id;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitudX() {
        return longitudX;
    }

    public void setLongitudX(double longitudX) {
        this.longitudX = longitudX;
    }

    public double getLongitudY() {
        return longitudY;
    }

    public void setLongitudY(double longitudY) {
        this.longitudY = longitudY;
    }
    public String getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(String isOpen) {
        this.isOpen = isOpen;
    }
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpentime() {
        return opentime;
    }

    public void setOpentime(String opentime) {
        this.opentime = opentime;
    }


    public String getTextUrl() {
        return textUrl;
    }

    public void setTextUrl(String textUrl) {
        this.textUrl = textUrl;
    }

    public int getFloorCount() {
        return floorCount;
    }

    public void setFloorCount(int floorCount) {
        this.floorCount = floorCount;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setIsDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }

    public int getmState() {
        return mState;
    }

    public void setmState(int mState) {
        this.mState = mState;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public boolean isAutoResume() {
        return autoResume;
    }

    public void setAutoResume(boolean autoResume) {
        this.autoResume = autoResume;
    }

    public boolean isAutoRename() {
        return autoRename;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }

    @Override
    public String toString() {
        return "MuseumBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", longitudX=" + longitudX +
                ", longitudY=" + longitudY +
                ", iconUrl='" + iconUrl + '\'' +
                ", address='" + address + '\'' +
                ", opentime='" + opentime + '\'' +
                ", isOpen=" + isOpen +
                ", textUrl='" + textUrl + '\'' +
                ", floorCount=" + floorCount +
                ", imgUrl='" + imgUrl + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", city='" + city + '\'' +
                ", version=" + version +
                ", priority=" + priority +
                ", isDownload=" + isDownload +
                ", progress=" + progress +
                ", fileLength=" + fileLength +
                ", autoResume=" + autoResume +
                ", autoRename=" + autoRename +
                '}';
    }

    @Override
	public void parseData(String data) {
	}
	@Override
	public String getDataStr() {
		return null;
	}
}

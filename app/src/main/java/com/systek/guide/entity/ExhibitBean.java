package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * 展品实体类
 */


public class ExhibitBean extends BaseEntity {

    private String id;//id
    private String name;//名字
    private String museumId;//博物馆id
    private String beaconId;//信标id
    private String introduce;//简介
    private String address;//位置
    private float mapx;//地图x坐标
    private float mapy;//地图y坐标
    private int floor;//所在楼层
    private String iconurl;//icon 地址
    private String imgsurl;//多角度图片地址
    private String audiourl;//音频地址
    private String texturl;//介绍地址
    private String labels;//标签-->年代材质等
    private String lexhibit;
    private String rexhibit;
    private String number;//博物馆内id号码
    private String content;//详细介绍内容
    private int version;//版本
    private int priority;//优先级
    private double distance;//距离
    private boolean saveForPerson;

    public ExhibitBean(){}


    public String getId() {
        return id;
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

    public String getMuseumId() {
        return museumId;
    }

    public void setMuseumId(String museumId) {
        this.museumId = museumId;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getMapx() {
        return mapx;
    }

    public void setMapx(float mapx) {
        this.mapx = mapx;
    }

    public float getMapy() {
        return mapy;
    }

    public void setMapy(float mapy) {
        this.mapy = mapy;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getIconurl() {
        return iconurl;
    }

    public void setIconurl(String iconurl) {
        this.iconurl = iconurl;
    }

    public String getImgsurl() {
        return imgsurl;
    }

    public void setImgsurl(String imgsurl) {
        this.imgsurl = imgsurl;
    }

    public String getAudiourl() {
        return audiourl;
    }

    public void setAudiourl(String audiourl) {
        this.audiourl = audiourl;
    }

    public String getTexturl() {
        return texturl;
    }

    public void setTexturl(String texturl) {
        this.texturl = texturl;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getLexhibit() {
        return lexhibit;
    }

    public void setLexhibit(String lexhibit) {
        this.lexhibit = lexhibit;
    }

    public String getRexhibit() {
        return rexhibit;
    }

    public void setRexhibit(String rexhibit) {
        this.rexhibit = rexhibit;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isSaveForPerson() {
        return saveForPerson;
    }

    public void setSaveForPerson(boolean saveForPerson) {
        this.saveForPerson = saveForPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExhibitBean)) return false;

        ExhibitBean that = (ExhibitBean) o;

        if (floor != that.floor) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (museumId != null ? !museumId.equals(that.museumId) : that.museumId != null)
            return false;
        if (beaconId != null ? !beaconId.equals(that.beaconId) : that.beaconId != null)
            return false;
        if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
        return !(number != null ? !number.equals(that.number) : that.number != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (museumId != null ? museumId.hashCode() : 0);
        result = 31 * result + (beaconId != null ? beaconId.hashCode() : 0);
        result = 31 * result + floor;
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        return result;
    }

    @Override
    public void parseData(String data) {

    }

    @Override
    public String getDataStr() {
        return null;
    }
}

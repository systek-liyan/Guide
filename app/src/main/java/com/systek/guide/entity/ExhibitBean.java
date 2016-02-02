package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * 展品实体类
 */
public class ExhibitBean extends BaseEntity {

    private String id;
    private String name;
    private String museumId;
    private String beaconId;
    private String introduce;
    private String address;
    private float mapx;
    private float mapy;
    private int floor;
    private String iconurl;
    private String imgsurl;
    private String audiourl;
    private String texturl;
    private String labels;
    private String lexhibit;
    private String rexhibit;
    private String number;
    private int version;
    private int priority;
    private double distance;
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

package com.systekcn.guide.entity;

import com.systekcn.guide.entity.base.BaseEntity;

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
    private int version;
    private int priority;

    public ExhibitBean() {
    }

    public ExhibitBean(String id, String name, String museumId,
                       String beaconId, String introduce, String address,
                       float mapx, float mapy, int floor, String iconurl,
                       String imgsurl, String audiourl, String texturl,
                       String labels, String lexhibit, String rexhibit,
                       int version, int priority) {
        this.id = id;
        this.name = name;
        this.museumId = museumId;
        this.beaconId = beaconId;
        this.introduce = introduce;
        this.address = address;
        this.mapx = mapx;
        this.mapy = mapy;
        this.floor = floor;
        this.iconurl = iconurl;
        this.imgsurl = imgsurl;
        this.audiourl = audiourl;
        this.texturl = texturl;
        this.labels = labels;
        this.lexhibit = lexhibit;
        this.rexhibit = rexhibit;
        this.version = version;
        this.priority = priority;
    }

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

    @Override
    public String toString() {
        return "ExhibitBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", museumId='" + museumId + '\'' +
                ", beaconId='" + beaconId + '\'' +
                ", introduce='" + introduce + '\'' +
                ", address='" + address + '\'' +
                ", mapx=" + mapx +
                ", mapy=" + mapy +
                ", floor=" + floor +
                ", iconurl='" + iconurl + '\'' +
                ", imgsurl='" + imgsurl + '\'' +
                ", audiourl='" + audiourl + '\'' +
                ", texturl='" + texturl + '\'' +
                ", labels='" + labels + '\'' +
                ", lexhibit='" + lexhibit + '\'' +
                ", rexhibit='" + rexhibit + '\'' +
                ", version=" + version +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExhibitBean)) return false;
        ExhibitBean that = (ExhibitBean) o;
        if (Float.compare(that.mapx, mapx) != 0) return false;
        if (Float.compare(that.mapy, mapy) != 0) return false;
        if (floor != that.floor) return false;
        if (version != that.version) return false;
        if (priority != that.priority) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (museumId != null ? !museumId.equals(that.museumId) : that.museumId != null)
            return false;
        if (beaconId != null ? !beaconId.equals(that.beaconId) : that.beaconId != null)
            return false;
        if (introduce != null ? !introduce.equals(that.introduce) : that.introduce != null)
            return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (iconurl != null ? !iconurl.equals(that.iconurl) : that.iconurl != null) return false;
        if (imgsurl != null ? !imgsurl.equals(that.imgsurl) : that.imgsurl != null) return false;
        if (audiourl != null ? !audiourl.equals(that.audiourl) : that.audiourl != null)
            return false;
        if (texturl != null ? !texturl.equals(that.texturl) : that.texturl != null) return false;
        if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
        if (lexhibit != null ? !lexhibit.equals(that.lexhibit) : that.lexhibit != null)
            return false;
        return !(rexhibit != null ? !rexhibit.equals(that.rexhibit) : that.rexhibit != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (museumId != null ? museumId.hashCode() : 0);
        result = 31 * result + (beaconId != null ? beaconId.hashCode() : 0);
        result = 31 * result + (introduce != null ? introduce.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (mapx != +0.0f ? Float.floatToIntBits(mapx) : 0);
        result = 31 * result + (mapy != +0.0f ? Float.floatToIntBits(mapy) : 0);
        result = 31 * result + floor;
        result = 31 * result + (iconurl != null ? iconurl.hashCode() : 0);
        result = 31 * result + (imgsurl != null ? imgsurl.hashCode() : 0);
        result = 31 * result + (audiourl != null ? audiourl.hashCode() : 0);
        result = 31 * result + (texturl != null ? texturl.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (lexhibit != null ? lexhibit.hashCode() : 0);
        result = 31 * result + (rexhibit != null ? rexhibit.hashCode() : 0);
        result = 31 * result + version;
        result = 31 * result + priority;
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

package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * Created by Qiang on 2016/2/23.
 */
public class MuseumNetInfo extends BaseEntity{

    private String id;
    private String museumId;
    private String museumAreaId;
    private String ip;
    private String wifiName;
    private String wifiPass;
    private String encryption;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMuseumId() {
        return museumId;
    }

    public void setMuseumId(String museumId) {
        this.museumId = museumId;
    }

    public String getMuseumAreaId() {
        return museumAreaId;
    }

    public void setMuseumAreaId(String museumAreaId) {
        this.museumAreaId = museumAreaId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPass() {
        return wifiPass;
    }

    public void setWifiPass(String wifiPass) {
        this.wifiPass = wifiPass;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MuseumNetInfo)) return false;

        MuseumNetInfo that = (MuseumNetInfo) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (museumId != null ? !museumId.equals(that.museumId) : that.museumId != null)
            return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (wifiName != null ? !wifiName.equals(that.wifiName) : that.wifiName != null)
            return false;
        return !(wifiPass != null ? !wifiPass.equals(that.wifiPass) : that.wifiPass != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (museumId != null ? museumId.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (wifiName != null ? wifiName.hashCode() : 0);
        result = 31 * result + (wifiPass != null ? wifiPass.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MuseumNetInfo{" +
                "id='" + id + '\'' +
                ", museumId='" + museumId + '\'' +
                ", museumAreaId='" + museumAreaId + '\'' +
                ", ip='" + ip + '\'' +
                ", wifiName='" + wifiName + '\'' +
                ", wifiPass='" + wifiPass + '\'' +
                ", encryption='" + encryption + '\'' +
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

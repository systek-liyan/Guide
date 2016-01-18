package com.systek.guide.entity.base;

/**
 * Created by Qiang on 2016/1/18.
 */
public class VersionBean extends BaseEntity {

    private int id;
    private String version;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionBean)) return false;
        VersionBean that = (VersionBean) o;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
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

package com.systek.guide.entity;

/**
 * Created by Qiang on 2015/12/25.
 */
public class MultiAngleImg {
    int time;
    String url;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiAngleImg)) return false;

        MultiAngleImg that = (MultiAngleImg) o;

        if (time != that.time) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);

    }

    @Override
    public int hashCode() {
        int result = time;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MultiAngleImg{" +
                "time=" + time +
                ", url='" + url + '\'' +
                '}';
    }
}

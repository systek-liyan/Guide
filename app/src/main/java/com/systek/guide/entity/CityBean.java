package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * Created by Qiang on 2015/11/26.
 *
 * 城市实体类
 */
public class CityBean extends BaseEntity {


    private int id;
    private String name;//城市名称
    private String alpha;//城市名称首字母

    public CityBean(){}

    public CityBean(int id, String name, String alpha) {
        this.id = id;
        this.name = name;
        this.alpha = alpha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    @Override
    public void parseData(String data) {

    }

    @Override
    public String toString() {
        return "CityBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", alpha='" + alpha + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CityBean)) return false;

        CityBean cityBean = (CityBean) o;

        if (id != cityBean.id) return false;
        if (name != null ? !name.equals(cityBean.name) : cityBean.name != null) return false;
        return !(alpha != null ? !alpha.equals(cityBean.alpha) : cityBean.alpha != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (alpha != null ? alpha.hashCode() : 0);
        return result;
    }

    @Override
    public String getDataStr() {
        return null;
    }

}

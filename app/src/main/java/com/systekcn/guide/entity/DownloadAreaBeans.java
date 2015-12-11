package com.systekcn.guide.entity;

import com.systekcn.guide.entity.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

public class DownloadAreaBeans extends BaseEntity implements Serializable {
	
	/**
	 * 序列化默认版本ID
	 */
	private static final long serialVersionUID = 1L;

	int id;

	/** 外层(组,城市)*/
	String city; 
	/** 内层(子层,博物馆列表)*/
	List<DownloadInfoBean> list;
	
	public DownloadAreaBeans() {
		super();
	}

	public DownloadAreaBeans(String city, List<DownloadInfoBean> list) {
		super();
		this.city = city;
		this.list = list;
	}
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getInfoCount() {
            return list .size();
        }

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<DownloadInfoBean> getList() {
		return list;
	}

	public void setList(List<DownloadInfoBean> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "DownloadTargetModels [id=" + id + ", city=" + city + ", list=" + list + "]";
	}


	@Override
	public void parseData(String data) {

	}

	@Override
	public String getDataStr() {
		return null;
	}
}

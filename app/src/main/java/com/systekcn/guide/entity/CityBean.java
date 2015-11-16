package com.systekcn.guide.entity;

public class CityBean implements BeanInterface{
	
	private int id;
	private String name;//城市名称
	private String alpha;//城市名称首字母
	
	
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
	public String toString() {
		return "CityBean [id=" + id + ", name=" + name + ", alpha=" + alpha + "]";
	}

}

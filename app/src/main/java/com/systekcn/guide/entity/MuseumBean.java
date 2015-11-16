package com.systekcn.guide.entity;

public class MuseumBean implements BeanInterface {
	
	private String id;
	private String name;// 博物馆名称
	private double longitudX;// 表示博物馆纬度坐标
	private double longitudY;// 表示博物馆经度坐标
	private String iconUrl;// icon的Url地址
	private String address;// 博物馆地址
	private String opentime;// 博物馆开放时间
	private boolean isOpen;// 当前博物馆是否开放
	private String textUrl;
	private int floorCount;
	private String imgUrl;
	private String audioUrl;
	private String city;
	private int version;
	private int priority ;
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
	public boolean isOpen() {
		return isOpen;
	}
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
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
	@Override
	public String toString() {
		return "MuseumBean [id=" + id + ", name=" + name + ", longitudX=" + longitudX + ", longitudY=" + longitudY
				+ ", iconUrl=" + iconUrl + ", address=" + address + ", opentime=" + opentime + ", isOpen=" + isOpen
				+ ", textUrl=" + textUrl + ", floorCount=" + floorCount + ", imgUrl=" + imgUrl + ", audioUrl="
				+ audioUrl + ", city=" + city + ", version=" + version + ", priority=" + priority + "]";
	}
	
}

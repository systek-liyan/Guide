package com.systekcn.guide.entity;

public class BeaconBean implements BeanInterface{

	private String id;
	private String uuid;
	private float personx;
	private float persony;
	private int type;
	private String major;
	private String minor;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public float getPersonx() {
		return personx;
	}

	public void setPersonx(float personx) {
		this.personx = personx;
	}

	public float getPersony() {
		return persony;
	}

	public void setPersony(float persony) {
		this.persony = persony;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

}

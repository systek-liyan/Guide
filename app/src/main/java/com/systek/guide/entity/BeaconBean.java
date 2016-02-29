package com.systek.guide.entity;

import com.systek.guide.entity.base.BaseEntity;

/**
 * beacon实体类
 */

public class BeaconBean extends BaseEntity {

	private String id;
	private String uuid;
	private float personx;
	private float persony;
	private int type;
	private String major;
	private String minor;
	private String museumId;
	private String museumAreaId;
	private double distance;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	@Override
	public void parseData(String data) {

	}

	@Override
	public String getDataStr() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BeaconBean)) return false;

		BeaconBean that = (BeaconBean) o;

		if (Float.compare(that.personx, personx) != 0) return false;
		if (Float.compare(that.persony, persony) != 0) return false;
		if (type != that.type) return false;
		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
		if (major != null ? !major.equals(that.major) : that.major != null) return false;
		if (minor != null ? !minor.equals(that.minor) : that.minor != null) return false;
		if (museumId != null ? !museumId.equals(that.museumId) : that.museumId != null)
			return false;
		if (museumAreaId != null ? !museumAreaId.equals(that.museumAreaId) : that.museumAreaId != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
		result = 31 * result + (personx != +0.0f ? Float.floatToIntBits(personx) : 0);
		result = 31 * result + (persony != +0.0f ? Float.floatToIntBits(persony) : 0);
		result = 31 * result + type;
		result = 31 * result + (major != null ? major.hashCode() : 0);
		result = 31 * result + (minor != null ? minor.hashCode() : 0);
		result = 31 * result + (museumId != null ? museumId.hashCode() : 0);
		result = 31 * result + (museumAreaId != null ? museumAreaId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "BeaconBean{" +
				"id='" + id + '\'' +
				", uuid='" + uuid + '\'' +
				", personx=" + personx +
				", persony=" + persony +
				", type=" + type +
				", major='" + major + '\'' +
				", minor='" + minor + '\'' +
				", museumId='" + museumId + '\'' +
				", museumAreaId='" + museumAreaId + '\'' +
				", distance=" + distance +
				'}';
	}
}

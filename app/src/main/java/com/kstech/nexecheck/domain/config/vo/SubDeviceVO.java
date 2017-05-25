package com.kstech.nexecheck.domain.config.vo;


/**
 * 子机型
 *
 */
public class SubDeviceVO {
	/**
	 * 子机型ID
	 */
	private String subDevId;
	/**
	 * 子机型名字
	 */
	private String subDevName;

	public String getSubDevId() {
		return subDevId;
	}

	public void setSubDevId(String subDevId) {
		this.subDevId = subDevId;
	}

	public String getSubDevName() {
		return subDevName;
	}

	public void setSubDevName(String subDevName) {
		this.subDevName = subDevName;
	}

	public SubDeviceVO(String subDevId, String subDevName) {
		super();
		this.subDevId = subDevId;
		this.subDevName = subDevName;
	}

}

package com.kstech.nexecheck.domain.config.vo;

import java.io.Serializable;

/**
 *
 */
public class RealTimeParamVO implements Serializable{
	private static final long serialVersionUID = 4943630941521202379L;
	private String name;
	private String unit;
	private String value;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}

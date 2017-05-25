package com.kstech.nexecheck.domain.db.dbenum;

public enum UserStatusEnum {
	/**
	 * 停用
	 */
	DISABLE("0","停用"),
	/**
	 * 启用
	 */
	ENABLE("1","启用");

	UserStatusEnum(String code,String name){
		this.name = name;
		this.code = code;
	}

	private String code;
	private String name;

	public String getCode(){
		return this.code;
	}

	public String getName(){
		return this.name;
	}
}


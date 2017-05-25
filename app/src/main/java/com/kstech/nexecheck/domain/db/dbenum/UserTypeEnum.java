package com.kstech.nexecheck.domain.db.dbenum;

public enum UserTypeEnum {
	/**
	 * 管理员
	 */
	MANAGER("0","管理员"),
	/**
	 * 检验员
	 */
	CHECKER("1","检验员");

	UserTypeEnum(String code,String name){
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
	// 普通方法
	public static String getName(String code) {
		for (UserTypeEnum c : UserTypeEnum.values()) {
			if (c.getCode() .equals(code) ) {
				return c.name;
			}
		}
		return null;
	}
	// 普通方法
	public static String getCode(String name) {
		for (UserTypeEnum c : UserTypeEnum.values()) {
			if (c.getName() .equals(name) ) {
				return c.code;
			}
		}
		return null;
	}
}

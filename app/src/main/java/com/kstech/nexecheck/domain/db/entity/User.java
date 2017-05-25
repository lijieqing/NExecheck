package com.kstech.nexecheck.domain.db.entity;

import com.kstech.nexecheck.domain.db.dbenum.UserTypeEnum;

/**
 * 用户实体
 *
 */
public class User {
	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工号
	 */
	private String code;

	/**
	 * 员工类型
	 */
	private UserTypeEnum type;
	/**
	 * 密码
	 */
	private String pwd;
	public void setType(int type) {
		this.type = UserTypeEnum.values()[type];
	}

	public UserTypeEnum getType() {
		return type;
	}

	public void setType(UserTypeEnum type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}

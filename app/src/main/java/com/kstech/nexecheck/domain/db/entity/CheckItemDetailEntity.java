package com.kstech.nexecheck.domain.db.entity;


/**
 * 检验项目明细表对象
 *
 */
public class CheckItemDetailEntity extends BaseCheckInfo {
	private static final long serialVersionUID = 1753978318880537305L;
	private String checkTime;
	private String checkError;
	private String checkStatus;
	private String checkerCode;
	private String checkerName;

	public String getCheckerCode() {
		return checkerCode;
	}

	public void setCheckerCode(String checkerCode) {
		this.checkerCode = checkerCode;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public String getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}

	public String getCheckError() {
		return checkError;
	}

	public void setCheckError(String checkError) {
		this.checkError = checkError;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

}

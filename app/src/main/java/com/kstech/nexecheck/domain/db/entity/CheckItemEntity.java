package com.kstech.nexecheck.domain.db.entity;

import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;

import java.util.List;


/**
 *
 */
public class CheckItemEntity extends BaseCheckInfo {
	private static final long serialVersionUID = -8762628534496897081L;
	/**
	 * 项目名称
	 */
	private String itemName;
	/**
	 * 检查状态
	 */
	private String checkStatus;
	/**
	 * 检查次数
	 */
	private int sumTimes;
	/**
	 * 检查不合格次数
	 */
	private int sumTimesNoPass;
	/**
	 * 描述
	 */
	private String checkDesc;

	/**
	 * 检验员名字
	 */
	private String checkerName;

	/**
	 * 检查项详情
	 * @return
	 */
	private List<CheckItemDetailEntity> checkItemDetailList;

	public List<CheckItemDetailEntity> getCheckItemDetailList() {
		return checkItemDetailList;
	}

	public void setCheckItemDetailList(
			List<CheckItemDetailEntity> checkItemDetailList) {
		this.checkItemDetailList = checkItemDetailList;
	}

	public String getCheckerName() {
		return checkerName;
	}

	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}

	public int getSumTimes() {
		return sumTimes;
	}
	public void setSumTimes(int sumTimes) {
		this.sumTimes = sumTimes;
	}
	public String getCheckDesc() {
		return checkDesc;
	}
	public void setCheckDesc(String checkDesc) {
		this.checkDesc = checkDesc;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getCheckStatus() {
		return checkStatus;
	}

	public String getCheckStatusName() {
		return CheckItemStatusEnum.getName(checkStatus);
	}
	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public int getSumTimesNoPass() {
		return sumTimesNoPass;
	}

	public void setSumTimesNoPass(int sumTimesNoPass) {
		this.sumTimesNoPass = sumTimesNoPass;
	}
}

package com.kstech.nexecheck.domain.db.entity;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CheckRecordEntity implements Serializable {
	private static final long serialVersionUID = 6893295341186440326L;
	/**
	 * 挖机出厂编号
	 */
	private String excId;
	/**
	 * 机型ID
	 */
	private String deviceId;
	/**
	 * 机型名
	 */
	private String deviceName;
	/**
	 * 子机型ID
	 */
	private String subdeviceId;
	/**
	 * 子机型名
	 */
	private String subdeviceName;
	/**
	 * 检验状态：0：未完成1：合格2：未合格 3：强制合格
	 */
	private String checkStatus;
	/**
	 * 创建时间
	 */
	private String createTime;
	/**
	 * 检验完成日期（最终判定合格或不合格的时间）
	 */
	private String finishTime;
	/**
	 * 管理员工号，取自USER表
	 */
	private String managerCode;
	/**
	 * 管理员名字，取自USER表
	 */
	private String managerName;
	/**
	 * 检验员工号，取自USER表
	 */
	private String checkerCode;
	/**
	 * 检验员名字，取自USER表
	 */
	private String checkerName;
	/**
	 * 检验说明，用大文本存储，暂时不限制字数
	 */
	private String desc;
	/**
	 * 检线名字
	 */
	private String checklineName;
	/**
	 * 检线IP
	 */
	private String checklineIp;
	/**
	 * 复选框状态
	 */
	private boolean checkBoxState;
	/**
	 * 整机检验总次数
	 */
	private int sumTimes;
	/**
	 * 整机检验总次数,不合格
	 */
	private int sumTimesNoPass;

	private List<CheckItemEntity> checkItemList = new ArrayList<>();

	public CheckItemEntity getCheckItem(String itemId) {
		for (CheckItemEntity item : checkItemList) {
			if (item.getItemId().equals(itemId)) {
				return item;
			}
		}
		return null;
	}

	public void setCheckItemList(List<CheckItemEntity> checkItemVOs) {
		this.checkItemList = checkItemVOs;
	}

	public List<CheckItemEntity> getCheckItemList() {
		return checkItemList;
	}

	public int getSumTimes() {
		return sumTimes;
	}

	public void setSumTimes(int sumTimes) {
		this.sumTimes = sumTimes;
	}

	public String getExcId() {
		return excId;
	}

	public void setExcId(String excId) {
		this.excId = excId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getSubdeviceId() {
		return subdeviceId;
	}

	public void setSubdeviceId(String subdeviceId) {
		this.subdeviceId = subdeviceId;
	}

	public String getSubdeviceName() {
		return subdeviceName;
	}

	public void setSubdeviceName(String subdeviceName) {
		this.subdeviceName = subdeviceName;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public String getManagerCode() {
		return managerCode;
	}

	public void setManagerCode(String managerCode) {
		this.managerCode = managerCode;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getChecklineName() {
		return checklineName;
	}

	public void setChecklineName(String checklineName) {
		this.checklineName = checklineName;
	}

	public String getChecklineIp() {
		return checklineIp;
	}

	public void setChecklineIp(String checklineIp) {
		this.checklineIp = checklineIp;
	}

	public CheckRecordEntity() {
	}

	public CheckRecordEntity(String excId, String deviceId, String deviceName,
							 String subdeviceId, String subdeviceName) {
		this.excId = excId;
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.subdeviceId = subdeviceId;
		this.subdeviceName = subdeviceName;
	}

	public CheckRecordEntity(String excId, String deviceId, String deviceName,
							 String subdeviceId, String subdeviceName, String checkStatus,
							 String createTime, String finishTime, String managerCode,
							 String managerName, String checkerCode, String checkerName,
							 String desc, String checklineName, String checklineIp) {
		super();
		this.excId = excId;
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.subdeviceId = subdeviceId;
		this.subdeviceName = subdeviceName;
		this.checkStatus = checkStatus;
		this.createTime = createTime;
		this.finishTime = finishTime;
		this.managerCode = managerCode;
		this.managerName = managerName;
		this.checkerCode = checkerCode;
		this.checkerName = checkerName;
		this.desc = desc;
		this.checklineName = checklineName;
		this.checklineIp = checklineIp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public boolean getCheckBoxState() {
		return checkBoxState;
	}

	public void setCheckBoxState(boolean checkBoxState) {
		this.checkBoxState = checkBoxState;
	}

	public int getSumTimesNoPass() {
		return sumTimesNoPass;
	}

	public void setSumTimesNoPass(int sumTimesNoPass) {
		this.sumTimesNoPass = sumTimesNoPass;
	}
}

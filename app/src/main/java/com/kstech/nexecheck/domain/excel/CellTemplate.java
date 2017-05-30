package com.kstech.nexecheck.domain.excel;

/**
 * excel模板，配置的填充区域JSON内容
 * @author wanghaibin
 * @created 2016-11-14 下午4:56:05
 * @since v1.0
 */
public class CellTemplate {
	// 项目ID
	private String itemId;
	// 参数名称
	private String paramName;
	// 倒数第几次
	private int times;
	// 需要多少次能够检验合格
	private int totalTimes;
	// 类型	item、detail、dateTime、checkerName
	private String dataType;
	public CellTemplate(){}
	public CellTemplate(String itemId, String paramName, int times,int totalTimes,String dataType) {
		super();
		this.itemId = itemId;
		this.paramName = paramName;
		this.times = times;
		this.totalTimes = totalTimes;
		this.dataType = dataType;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getTotalTimes() {
		return totalTimes;
	}
	public void setTotalTimes(int totalTimes) {
		this.totalTimes = totalTimes;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}

}

package com.kstech.nexecheck.domain.config.vo;


import com.kstech.nexecheck.exception.ExcException;

import java.io.Serializable;

/**
 * 检查项目参数值对象
 */
public class CheckItemParamValueVO implements Comparable<CheckItemParamValueVO>,Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 数据库存储内容如下
	 */
//	[{"param":"主溢流压力","value":"30","unit":"Mpa",type:"主参数"},
//	 {"param":"液压油温度","value":"30","unit":"℃",type:"环境参数"}]
	/**
	 * 配置文件配置如下
	 */
//	<QCParam Param="主溢流压力" ValidMin="" ValidMax="" ValidAvg=""/>
//  <ENVParam Param="液压油温度" ValidMin="" ValidMax="" ValidAvg=""/>
	private String BelongTo;
	private String param;
	private String value="";
	private String unit="";
	private String type;
	private String validMin;
	private String validMax;
	private String validAvg;
	public void setBelongTo(String itemname){
		this.BelongTo = itemname;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValidMin() {
		return validMin;
	}
	public void setValidMin(String validMin) {
		this.validMin = validMin;
		try {
			Float.valueOf(validMin);
		}catch (NumberFormatException e){
			throw new ExcException(e,"配置信息异常！\n检测项目:"+BelongTo+"\n 参数"+param+"最小值 "+validMin+" 为非数字");
		}
	}
	public String getValidMax() {
		return validMax;
	}
	public void setValidMax(String validMax) {
		this.validMax = validMax;
		try {
			Float.valueOf(validMax);
		}catch (NumberFormatException e){
			throw new ExcException(e,"配置信息异常！\n检测项目:"+BelongTo+"\n 参数"+param+"最大值 "+validMax+" 为非数字");
		}
	}
	public String getValidAvg() {
		return validAvg;
	}
	public void setValidAvg(String validAvg) {
		this.validAvg = validAvg;
	}
	@Override
	public int compareTo(CheckItemParamValueVO arg0) {
		int i = this.type.compareTo(arg0.type);
		if (i==0) {
			i = this.param.compareTo(arg0.param);
		}
		return i;
	}
}

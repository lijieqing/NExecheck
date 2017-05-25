/**
 * 
 */
package com.kstech.nexecheck.domain.db.entity;

import com.kstech.nexecheck.domain.config.vo.CheckItemParamValueVO;
import com.kstech.nexecheck.utils.JsonUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 *
 */
public class BaseCheckInfo implements Serializable{
	private static final long serialVersionUID = 9020509016588545393L;

	/**
	 * 项目ID
	 */
	protected String itemId;

	/**
	 * 挖掘机出厂编号
	 */
	protected String excId;

	/**
	 * 结果参数值
	 */
	protected String paramValue;

	// 将字符串解析后，存入到集合中
	protected List<CheckItemParamValueVO> param;

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
		param = JsonUtils.fromArrayJson(paramValue, CheckItemParamValueVO.class);
		// 将集合中的参数进行排序
		Collections.sort(param);
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getExcId() {
		return excId;
	}

	public void setExcId(String excId) {
		this.excId = excId;
	}

	public List<CheckItemParamValueVO> getParam() {
		return param;
	}

	public void setParam(List<CheckItemParamValueVO> param) {
		this.param = param;
	}

	public String getParamValue() {
		return paramValue;
	}

}

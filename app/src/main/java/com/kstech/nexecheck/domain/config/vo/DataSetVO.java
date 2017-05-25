/**
 * 
 */
package com.kstech.nexecheck.domain.config.vo;

import android.util.Log;

import java.util.List;
import java.util.Map;

import J1939.J1939_DataVar_ts;

/**
 *
 */
public class DataSetVO {
	/**
	 * J1939配置的变量数组，通过解析配置文件的<DataSet>标记及其子标记形成
	 */
	private J1939_DataVar_ts[] j1939_DataVarCfg;

	/**
	 * key：DSItem的Name，
	 * value：J1939_DataVar_ts对象在数组中的位置
	 */
	private Map<String, Short> itemIntexMap ;



	public short getItemIndex(String itemName){
		return itemIntexMap.get(itemName);
	}

	public J1939_DataVar_ts[] getJ1939_DataVarCfg() {
		return j1939_DataVarCfg;
	}

	public void setJ1939_DataVarCfg(List<J1939_DataVar_ts> j1939_DataVarCfg) {
		this.j1939_DataVarCfg = j1939_DataVarCfg
				.toArray(new J1939_DataVar_ts[0]);
	}

	public void setItemIntexMap(Map<String, Short> itemIntexMap) {
		this.itemIntexMap = itemIntexMap;
	}

	public J1939_DataVar_ts getDSItem(String name){
		Log.e("--------------", name);
		return j1939_DataVarCfg[itemIntexMap.get(name)];
	}
	// 发送区承载数据对象
	public J1939_DataVar_ts getCheckItemDSItem(){
		return j1939_DataVarCfg[itemIntexMap.get("当前检测项目")];
	}

	public J1939_DataVar_ts getCheckTimesDSItem() {
		return j1939_DataVarCfg[itemIntexMap.get("当前检测次序")];
	}

	public J1939_DataVar_ts getCommandDSItem() {
		return j1939_DataVarCfg[itemIntexMap.get("检测命令")];
	}
	//　应答区承载数据对象
	public J1939_DataVar_ts getCheckItemDSItemResp() {
		return j1939_DataVarCfg[itemIntexMap.get("应答的当前检测项目")];
	}
	public J1939_DataVar_ts getCheckTimesDSItemResp() {
		return j1939_DataVarCfg[itemIntexMap.get("应答的当前检测次序")];
	}
	public J1939_DataVar_ts getCommandDSItemResp() {
		return j1939_DataVarCfg[itemIntexMap.get("应答的检测命令")];
	}
	public J1939_DataVar_ts getCheckStatusDSItemResp() {
		return j1939_DataVarCfg[itemIntexMap.get("检测应答状态")];
	}
	public J1939_DataVar_ts getCheckCodeDSItemResp() {
		return j1939_DataVarCfg[itemIntexMap.get("检测指示码")];
	}
	public J1939_DataVar_ts getLastCheckValueDSItem() {
		return j1939_DataVarCfg[itemIntexMap.get("最新接收的测量值PGN")];
	}

}

package com.kstech.nexecheck.domain.config.vo;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.Map;

import J1939.J1939_Cfg_ts;
import J1939.J1939_DTCfg_ts;
import J1939.J1939_PGCfg_ts;
import J1939.J1939_SPCfg_ts;

/**
 *
 */
@SuppressLint("UseSparseArrays")
public class J1939PgSetVO extends J1939_Cfg_ts {

	/**
	 * key：PG的PGN值， value：PG象在数组中的位置
	 */
	private Map<Integer, Short> pgIndexMap = new HashMap<Integer, Short>();

	/**
	 * key：SP的Ref值， value：SP对象
	 */
	private Map<String, J1939_SPCfg_ts> spRefMap = new HashMap<String, J1939_SPCfg_ts>();

	/**
	 * 根据变量值判断是否能找到对应错误DTC对象，如果找到则返回该DTC，否则返回null
	 * @param varName 变量名字
	 * @return
	 */
	public J1939_DTCfg_ts getLastErrorDtc(String varName) {
		J1939_SPCfg_ts sp = spRefMap.get(varName);
		if (sp == null) {
			return null;
		}
		return sp.pLastDTC;
	}

	public void putSpRef(String spRef, J1939_SPCfg_ts sp) {
		spRefMap.put(spRef, sp);

	}

	public void putPgIndex(int pgn, short index) {
		pgIndexMap.put(pgn, index);
	}

	public void setPgIndexMap(Map<Integer, Short> pgIndexMap) {
		this.pgIndexMap = pgIndexMap;
	}

	/**
	 * 根据PGN值获取PG对象
	 *
	 * @param pgn
	 * @return
	 */
	public J1939_PGCfg_ts getPg(int pgn) {
		return pPGCfg[pgIndexMap.get(pgn)];
	}
}

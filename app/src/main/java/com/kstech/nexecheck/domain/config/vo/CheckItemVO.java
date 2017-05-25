package com.kstech.nexecheck.domain.config.vo;


import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CheckItemVO implements Serializable {
	private static final long serialVersionUID = 530906464911285765L;

	private String id;

	private String name;

	/**
	 * true：必检项目,false：非必检项目
	 */
	private boolean require;

	/**
	 * 连续检验times次都合格才算总结果合格
	 */
	private int times;

	/**
	 * 就绪超时时间, 发出准备检测命令到收到准备就绪应答之间的最大时长
	 */
	private int readyTimeout;

	/**
	 * 检测超时时间，发出开始检测命令到收至检测完成应答之间的最大时长
	 */
	private int qcTimeout;

	private Msgs msgs = new Msgs();

	private List<CheckItemParamValueVO> paramList = new ArrayList<CheckItemParamValueVO>();

	private List<RealTimeParamVO> rtParamList = new ArrayList<RealTimeParamVO>();
	public String getJsonParams(){
		return JsonUtils.toJson(paramList);
	}

	public void sortParamList(){
		Collections.sort(paramList);
	}

	public List<CheckItemParamValueVO> getParamNameList(){
		return paramList;
	}
	public void addRtParam(RealTimeParamVO param) {
		rtParamList.add(param);
	}

	public List<RealTimeParamVO> getRtParamList() {
		return rtParamList;
	}

	public void addQcParam(String param, String validMin, String validMax,
						   String validAvg) throws ExcException {
		CheckItemParamValueVO vo = new CheckItemParamValueVO();
		vo.setBelongTo(name);
		vo.setParam(param);
		vo.setValidAvg(validAvg);
		vo.setValidMax(validMax);
		vo.setValidMin(validMin);
		vo.setType("主参数");
		paramList.add(vo);
	}

	public void addEnvParam(String param, String validMin, String validMax,
							String validAvg) throws ExcException{
		CheckItemParamValueVO vo = new CheckItemParamValueVO();
		vo.setBelongTo(name);
		vo.setParam(param);
		vo.setValidAvg(validAvg);
		vo.setValidMax(validMax);
		vo.setValidMin(validMin);
		vo.setType("环境参数");
		paramList.add(vo);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequire() {
		return require;
	}

	public void setRequire(String strRequire) {
		this.require = Boolean.valueOf(strRequire);
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(String strTimes) {
		this.times = Integer.valueOf(strTimes);
	}

	public int getReadyTimeout() {
		return readyTimeout;
	}

	public void setReadyTimeout(String readyTimeout) {
		this.readyTimeout = Integer.valueOf(readyTimeout);
	}

	public int getQcTimeout() {
		return qcTimeout;
	}

	public void setQcTimeout(String qcTimeout) {
		this.qcTimeout = Integer.valueOf(qcTimeout);
	}

	public String getNotReadyMsg() {
		return msgs.notReadyMsg;
	}

	public void setNotReadyMsg(String notReadyMsg) {
		msgs.notReadyMsg = notReadyMsg;
	}

	public String getAbortMsg() {
		return msgs.abortMsg;
	}

	public void setAbortMsg(String abortMsg) {
		msgs.abortMsg = abortMsg;
	}

	public String getOkMsg() {
		return msgs.okMsg;
	}

	public void setOkMsg(String okMsg) {
		msgs.okMsg = okMsg;
	}

	public String getReadyMsg() {
		return msgs.readyMsg;
	}

	public void setReadyMsg(String readyMsg) {
		msgs.readyMsg = readyMsg;
	}

	public void putProgressMsg(String code, String msg) {
		msgs.progressMap.put(code, msg);
	}

	public String getProgressMsg(String code) {
		return msgs.progressMap.get(code);
	}

	public void putErrorMsg(String code, String msg) {
		msgs.errMap.put(code, msg);
	}

	public String getErrorMsg(String code) {
		return msgs.errMap.get(code);
	}

	private static class Msgs {
		/**
		 * 检测准备就绪提示,在发出“准备检测”命令并收到“准备就绪”应答后显示
		 */
		String readyMsg;
		/**
		 * 检测准备未就绪（未达检测条件）提示，在发出“准备检测”命令并收到“传感器故障”应答时显示
		 */
		String notReadyMsg;
		/**
		 * 检测中止提示，在检测过程中选择“中止检测”发出中止命令且收到“本次测量中止”时显示
		 */
		String abortMsg;
		/**
		 * 检测完成提示，在收到“检测完成”应答时显示
		 */
		String okMsg;

		/**
		 * 当收到“正在检测”应答时显示的提示信息，具体内容根据应答帧中的“检测指示码”而定
		 */
		Map<String, String> progressMap = new HashMap<String, String>();

		/**
		 * 当收到“检测失败”应答时显示的提示信息，具体内容根据应答帧中的“检测指示码”而定
		 */
		Map<String, String> errMap = new HashMap<String, String>();

	}

}

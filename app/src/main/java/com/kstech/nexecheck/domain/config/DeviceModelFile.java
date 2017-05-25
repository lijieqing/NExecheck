package com.kstech.nexecheck.domain.config;

import android.content.Context;
import android.util.Log;

import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.config.vo.DataSetVO;
import com.kstech.nexecheck.domain.config.vo.J1939PgSetVO;
import com.kstech.nexecheck.domain.config.vo.RealTimeParamVO;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.Globals;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import J1939.J1939_Context;
import J1939.J1939_DTCfg_ts;
import J1939.J1939_DataVar_ts;
import J1939.J1939_PGCfg_ts;
import J1939.J1939_SPCfg_ts;

/**
 * 挖掘机机型文件对应的值对象，封装了一个机型文件的完整数据
 *
 */
public class DeviceModelFile {
	/**
	 * 机型Id
	 */
	private String deviceId;

	/**
	 * 机型名称
	 */
	private String deviceName;
	/**
	 * 机型启用日期，格式为"yyyy-mm"
	 */
	private String devBornDate;
	/**
	 * 机型停产日期，格式为"yyyy-mm"
	 */
	private String devDieDate;

	/**
	 * 机型当前状态
	 */
	private String devStatus;

	/**
	 * J1939配置的变量数组，通过解析配置文件的<DataSet>标记及其子标记形成
	 */
	public DataSetVO dataSetVO;

	/**
	 * J1939配置，通过解析配置文件形成 在解析配置文件时实例化
	 */
	public J1939PgSetVO j1939PgSetVO;

	/**
	 * 检查项集合，通过解析配置文件的<QCSet>标记及其子标记形成
	 */
	public List<CheckItemVO> checkItemList = new ArrayList<CheckItemVO>();

	private List<RealTimeParamVO> realTimeParamList = new ArrayList<RealTimeParamVO>();

	/**
	 * Gets j 1939 pg set vo.
	 *
	 * @return the j 1939 pg set vo
	 */
	public J1939PgSetVO getJ1939PgSetVO() {
		return j1939PgSetVO;
	}

	/**
	 * Gets check item list.
	 *
	 * @return the check item list
	 */
	public List<CheckItemVO> getCheckItemList() {
		return checkItemList;
	}

	/**
	 * Gets check item vo.
	 *
	 * @param checkItemId the check item id
	 * @return the check item vo
	 */
	public CheckItemVO getCheckItemVO(String checkItemId) {
		for (CheckItemVO item : checkItemList) {
			if (item.getId().equals(checkItemId)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Gets data set vo.
	 *
	 * @return the data set vo
	 */
	public DataSetVO getDataSetVO() {
		return dataSetVO;
	}

	/**
	 * Sets data set vo.
	 *
	 * @param dataSetVO the data set vo
	 */
	public void setDataSetVO(DataSetVO dataSetVO) {
		this.dataSetVO = dataSetVO;
	}

	/**
	 * Add check item.
	 *
	 * @param checkItemVO the check item vo
	 */
	public void addCheckItem(CheckItemVO checkItemVO) {
		checkItemList.add(checkItemVO);
	}

	/**
	 * Gets device id.
	 *
	 * @return the device id
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets device id.
	 *
	 * @param deviceId the device id
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Gets device name.
	 *
	 * @return the device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Sets device name.
	 *
	 * @param deviceName the device name
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * Gets dev born date.
	 *
	 * @return the dev born date
	 */
	public String getDevBornDate() {
		return devBornDate;
	}

	/**
	 * Sets dev born date.
	 *
	 * @param devBornDate the dev born date
	 */
	public void setDevBornDate(String devBornDate) {
		this.devBornDate = devBornDate;
	}

	/**
	 * Gets dev die date.
	 *
	 * @return the dev die date
	 */
	public String getDevDieDate() {
		return devDieDate;
	}

	/**
	 * Sets dev die date.
	 *
	 * @param devDieDate the dev die date
	 */
	public void setDevDieDate(String devDieDate) {
		this.devDieDate = devDieDate;
	}

	/**
	 * Gets dev status.
	 *
	 * @return the dev status
	 */
	public String getDevStatus() {
		return devStatus;
	}

	/**
	 * Sets dev status.
	 *
	 * @param devStatus the dev status
	 */
	public void setDevStatus(String devStatus) {
		this.devStatus = devStatus;
	}

	/**
	 * Add real time param.
	 *
	 * @param realTimeVO the real time vo
	 */
	public void addRealTimeParam(RealTimeParamVO realTimeVO) {
		realTimeParamList.add(realTimeVO);
	}

	/**
	 * Gets real time param list.
	 *
	 * @return the real time param list
	 */
	public List<RealTimeParamVO> getRealTimeParamList() {
		return realTimeParamList;
	}

	/**
	 * Read from file device model file.
	 *
	 * @param filePath the file path of 机型文件
	 * @return the device model file
	 * @throws ExcException the exc exception
	 */
	public static DeviceModelFile readFromFile(String filePath, Context context) throws ExcException {
		DeviceModelFile result = new DeviceModelFile();

		Document document;
		try {
			// 从sd卡下读取
			InputStream is = getRefInpuStream(filePath);
			// 从工程assets下读取
			//InputStream is = context.getAssets().open(filePath);
			SAXReader reader = new SAXReader();
			document = reader.read(is);
		} catch (Exception e) {
			throw new ExcException(e, "机型文件"+filePath+"不存在");
		}

		Element root = document.getRootElement();
		// 校验文件
		validateXmlFile(root);

		// 解析device的基本属性
		result.setDeviceId(root.attribute("Id").getText());
		result.setDeviceName(root.attribute("Name").getText());
		result.setDevBornDate(root.attribute("DevBornDate").getText());
		result.setDevDieDate(root.attribute("DevDieDate").getText());
		result.setDevStatus(root.attribute("DevStatus").getText());

		parseDataSet(result, root);

		parseJ1939(result, root);

		parseQCSet(result, root);

		parseRealTimeSet(result, root);

		return result;
	}

	private static InputStream getRefInpuStream(String deviceName) throws FileNotFoundException {
		InputStream is = null;
		File file = new File(Globals.MODELPATH+deviceName);
		if(file.exists()){
			is = new FileInputStream(file);
			Log.i("FILE","找到文件");
		}
		return is ;
	}

	@SuppressWarnings({ "unchecked" })
	private static void parseRealTimeSet(DeviceModelFile result, Element root) {
		// 解析RealTimeParam
		Element realTimeSet = root.element("RealTimeSet");
		// 获取RealTimeSet节点 下的 RealTimeParam 节点集合
		List<Element> realTimeParamElementList = realTimeSet
				.elements("RealTimeParam");
		for (int i = 0; i < realTimeParamElementList.size(); i++) {
			Element elm = realTimeParamElementList.get(i);
			RealTimeParamVO realTimeParam = new RealTimeParamVO();
			String name = elm.attributeValue("Name");
			realTimeParam.setName(name);
			realTimeParam.setUnit(result.getDataSetVO().getDSItem(name).sUnit);
			result.addRealTimeParam(realTimeParam);
			// // TODO: 2017/5/24  realtime 给J1939_DataVar_ts对象的监听器，进行初始化
//			result.getDataSetVO().getDSItem(name).listener = new RealTimeChangedListener(
//					result.getDataSetVO().getDSItem(name));
		}
	}

	@SuppressWarnings("unchecked")
	private static void parseJ1939(DeviceModelFile result, Element root) {
		J1939PgSetVO j1939Cfg = new J1939PgSetVO();
		// 赋值给父类对象 whb
		J1939_Context.j1939_Cfg = j1939Cfg;
		Element j1939Node = root.element("J1939");
		j1939Cfg.bNodeAddr = Byte.valueOf(hexToInteger(j1939Node
				.attributeValue("NodeAddr")));
		j1939Cfg.bTaskPrio = Byte.valueOf(j1939Node.attributeValue("TaskPrio"));
		j1939Cfg.wCycle = Integer.valueOf(j1939Node.attributeValue("Cycle"));

		List<J1939_PGCfg_ts> pgList = new ArrayList<J1939_PGCfg_ts>();
		List<Element> pgNodeList = j1939Node.elements("PG");
		// 统计pg节点个数 whb
		j1939Cfg.wPGNums = (short)pgNodeList.size();

		for (short i = 0; i < pgNodeList.size(); i++) {
			Element pgNode = pgNodeList.get(i);
			J1939_PGCfg_ts pg = new J1939_PGCfg_ts();
			pg.bPGType = Byte.valueOf(pgNode.attributeValue("Type"));
			pg.bDir = Byte
					.valueOf(pgNode.attributeValue("Dir").equals("Rx") ? "0"
							: "1");
			pg.bPrio = Byte.valueOf(pgNode.attributeValue("Prio"));
			pg.wLen = Short.valueOf(pgNode.attributeValue("Len"));
			pg.dwRate = Integer.valueOf(pgNode.attributeValue("Rate"));
			pg.dwPGN = Integer.valueOf(hexToInteger(pgNode
					.attributeValue("PGN")));
			pg.bSA = Byte.valueOf(hexToInteger(pgNode.attributeValue("SA")));
			pg.bReq = Byte.valueOf(pgNode.attributeValue("Req"));
			pg.dwReqCycle = Integer.valueOf(pgNode.attributeValue("ReqCyc"));
			List<J1939_SPCfg_ts> spList = new ArrayList<J1939_SPCfg_ts>();
			List<Element> spNodeList = pgNode.elements("SP");
			for (Element spNode : spNodeList) {
				J1939_SPCfg_ts sp = new J1939_SPCfg_ts();
				sp.bSPType = Byte.valueOf(spNode.attributeValue("Type"));
				sp.dwSPN = Integer
						.valueOf(spNode.attributeValue("SPN") == null ? "0"
								: spNode.attributeValue("SPN"));
				sp.wStartByte = Short.valueOf(spNode.attributeValue("SByte"));
				sp.bStartBit = Byte.valueOf(spNode.attributeValue("SBit"));
				try {
					sp.bBytes = Byte.valueOf(spNode.attributeValue("Bytes"));
				} catch (NumberFormatException e) {
					// 从关联项item中，读取字节大小
					J1939_DataVar_ts dsItem = result.dataSetVO.getDSItem(spNode.attributeValue("Bytes"));
					sp.bBytes = (byte) dsItem.getValue();
				}
				sp.bBits = Byte.valueOf(spNode.attributeValue("Bits"));
				sp.fRes = Float.valueOf(spNode.attributeValue("Res"));
				sp.fOffset = Float.valueOf(spNode.attributeValue("Off"));
				// 参数关联的变量名。当从接收PGN中解析出参数值后赋值给该变量，
				// 或要发送PGN时根据该变量值得到参数发送的原始值填入PGN数据区相应位置
				String refValue = spNode.attributeValue("Ref");
				short itemIndex = result.dataSetVO.getItemIndex(refValue);
				sp.wRefDataIdx = itemIndex;
				sp.bRefDataType = result.dataSetVO.getJ1939_DataVarCfg()[itemIndex]
						.getDataType();

				List<Element> dtcNodeList = spNode.elements("DTC");
				List<J1939_DTCfg_ts> dtcList = new ArrayList<J1939_DTCfg_ts>();
				for (Element dtcNode : dtcNodeList) {
					J1939_DTCfg_ts dtc = new J1939_DTCfg_ts();
					dtc.bFMI = Byte.valueOf(dtcNode.attributeValue("FMI"));
					dtc.wDescId = Globals.getResConfig().getResourceVO()
							.getMsgIndex(dtcNode.attributeValue("MsgId"));
					dtc.wIconId = Globals.getResConfig().getResourceVO()
							.getImageIndex(dtcNode.attributeValue("Icon"));
					dtcList.add(dtc);
				}
				sp.pDTCfg = dtcList.toArray(new J1939_DTCfg_ts[dtcList.size()]);
				// 统计dtc节点个数 whb
				if (dtcList.size()>0) {
					sp.bDTCNums = (byte)dtcList.size();
				}
				// whb
				sp.pPGCfg = pg;
				j1939Cfg.putSpRef(refValue, sp);
				// 统计sp节点个数 whb
				spList.add(sp);
			}
			pg.pSPCfg = spList.toArray(new J1939_SPCfg_ts[spList.size()]);
			// 统计sp节点个数 whb
			if (spNodeList.size()>0) {
				pg.bSPNums = (short)spNodeList.size();
			}
			j1939Cfg.putPgIndex(pg.dwPGN, i);
			pgList.add(pg);
		}
		j1939Cfg.pPGCfg = pgList.toArray(new J1939_PGCfg_ts[pgList.size()]);
		result.j1939PgSetVO = j1939Cfg;
	}

	private static void parseDataSet(DeviceModelFile result, Element root) {
		DataSetVO dataSetVO = new DataSetVO();
		Element dataSetNode = root.element("DataSet");
		List<?> dsTtemNodes = dataSetNode.elements("DSItem");

		Map<String, Short> itemMap = new HashMap<String, Short>();
		for (short i = 0; i < dsTtemNodes.size(); i++) {
			Element itemNode = (Element) dsTtemNodes.get(i);
			String name = getAttribute(itemNode, "Name");
			itemMap.put(name, i);
		}
		dataSetVO.setItemIntexMap(itemMap);
		List<J1939_DataVar_ts> j1939_DataVarCfg = new ArrayList<J1939_DataVar_ts>(
				dsTtemNodes.size());
		for (int i = 0; i < dsTtemNodes.size(); i++) {
			J1939_DataVar_ts itemData = null;

			Element itemNode = (Element) dsTtemNodes.get(i);
			String dataType = itemNode.attribute("DataType").getText();
			String value = getAttribute(itemNode, "Value");
			Attribute rowsAttr = itemNode.attribute("Rows");
			short rows = -1;
			if (rowsAttr != null) {
				rows = Short.valueOf(rowsAttr.getText().trim());
			}
			if (rows > 0) {// 说明是向量
				String indexBy = getAttribute(itemNode, "IndexBy");
				if (indexBy != null) {// 一维向量
					Short index = itemMap.get(indexBy);
					itemData = new J1939_DataVar_ts(dataType, rows, index, null);

				} else {// 二维向量

					List<?> dataNodes = itemNode.elements("Data");
					List<Object> dataValueList = new ArrayList<Object>();
					for (int j = 0; j < dataNodes.size(); j++) {
						Element dataNode = (Element) dataNodes.get(j);
						dataValueList.add(convertValue(dataType,
								getAttribute(dataNode, "Value")));
					}
					Object[] values = dataValueList.toArray();
					itemData = new J1939_DataVar_ts(dataType, rows, (short) -1,
							values);
				}

			} else {// 说明是基础类型
				itemData = new J1939_DataVar_ts(dataType, value);
			}
			itemData.setName(getAttribute(itemNode, "Name"));
			itemData.setUnit(getAttribute(itemNode, "Unit"));
			itemData.setDecLen(getAttribute(itemNode, "DecLen"));
			String remarkIdStr = getAttribute(itemNode, "RemarkID");
			// itemData.setDataID(getAttribute(itemNode, "Id"));
			if (remarkIdStr != null) {
				Short index = Globals.getResConfig().getResourceVO()
						.getMsgIndex(remarkIdStr);
				if (index != null) {
					itemData.setRemarkID(index);
				}
			}

			String linkTo = getAttribute(itemNode, "LinkTo");
			if (linkTo != null) {
				Short index = itemMap.get(linkTo);
				if (index != null) {
					itemData.setLinkTo(index);
				}
			}
			j1939_DataVarCfg.add(itemData);
		}
		dataSetVO.setJ1939_DataVarCfg(j1939_DataVarCfg);
		// whb
		J1939_Context.j1939_DataVarCfg = j1939_DataVarCfg.toArray(new J1939_DataVar_ts[j1939_DataVarCfg.size()]);
		result.dataSetVO = dataSetVO;
	}

	@SuppressWarnings("unchecked")
	private static void parseQCSet(DeviceModelFile result, Element root) throws ExcException{
		Element QCSet = root.element("QCSet");
		// 获取DeviceSet节点 下的 Device 节点集合
		List<Element> QCItemList = QCSet.elements("QCItem");
		for (Element qcItemNode : QCItemList) {

			CheckItemVO checkItem = new CheckItemVO();
			checkItem.setId(qcItemNode.attributeValue("Id"));
			checkItem.setName(qcItemNode.attributeValue("Name"));
			checkItem.setRequire(qcItemNode.attributeValue("Require"));
			checkItem.setTimes(qcItemNode.attributeValue("QCTimes"));
			checkItem
					.setReadyTimeout(qcItemNode.attributeValue("ReadyTimeout"));
			checkItem.setQcTimeout(qcItemNode.attributeValue("QCTimeout"));
			// 解析Msgs
			Element msgsElement = qcItemNode.element("Msgs");
			checkItem.setReadyMsg(msgsElement.attributeValue("ReadyMsg"));
			checkItem.setNotReadyMsg(msgsElement.attributeValue("NotReadyMsg"));
			checkItem.setAbortMsg(msgsElement.attributeValue("AbortMsg"));
			checkItem.setOkMsg(msgsElement.attributeValue("OkMsg"));

			List<Element> progressElemList = msgsElement.element(
					"QCProgressMsg").elements("QCProgress");
			for (Element progressElem : progressElemList) {
				checkItem.putProgressMsg(progressElem.attributeValue("Code"),
						progressElem.attributeValue("Msg"));
			}
			List<Element> errElemList = msgsElement.element("QCErrMsg")
					.elements("QCErr");
			for (Element errElem : errElemList) {
				checkItem.putErrorMsg(errElem.attributeValue("Code"),
						errElem.attributeValue("Msg"));
			}

			// 解析param
			for (Element qcParamElement : (List<Element>) qcItemNode.element(
					"QCParams").elements("QCParam")) {
				checkItem.addQcParam(qcParamElement.attributeValue("Param"),
						qcParamElement.attributeValue("ValidMin"),
						qcParamElement.attributeValue("ValidMax"),
						qcParamElement.attributeValue("ValidAvg"));
			}
			for (Element envParamElement : (List<Element>) qcItemNode.element(
					"ENVParams").elements("ENVParam")) {
				checkItem.addEnvParam(envParamElement.attributeValue("Param"),
						envParamElement.attributeValue("ValidMin"),
						envParamElement.attributeValue("ValidMax"),
						envParamElement.attributeValue("ValidAvg"));
			}
			for (Element rtParamElement : (List<Element>) qcItemNode.element(
					"RealTimeParams").elements("RealTimeParam")) {

				RealTimeParamVO realTimeParam = new RealTimeParamVO();
				String name = rtParamElement.attributeValue("Name");
				realTimeParam.setName(name);
				realTimeParam
						.setUnit(result.getDataSetVO().getDSItem(name).sUnit);
				checkItem.addRtParam(realTimeParam);
				// // TODO: 2017/5/24 qcset 给J1939_DataVar_ts对象的监听器，进行初始化
			}
			checkItem.sortParamList();
			result.addCheckItem(checkItem);
		}
	}

	private static Object convertValue(String dataType, String dataValue) {
		if (dataType.equals("BYTE")) {
			return Byte.valueOf(dataValue);
		} else if (dataType.equals("FLOAT")) {
			return Float.valueOf(dataValue);
		} else if (dataType.equals("WORD") || dataType.equals("DWORD")
				|| dataType.equals("INT")) {
			return Integer.valueOf(dataValue);
		} else if (dataType.equals("SHORT")) {
			return Short.valueOf(dataValue);
		}
		return null;
	}

	private static String getAttribute(Element node, String attrName) {
		Attribute attr = node.attribute(attrName);
		if (attr == null) {
			return null;
		} else {
			return attr.getText().trim();
		}
	}

	private static String hexToInteger(String hex) {
		if (hex.startsWith("0x") || hex.startsWith("0X")) {
			hex = hex.substring(2);
		}
		return String.valueOf(Integer.valueOf(hex, 16));
	}

	/**
	 * 校验文件
	 *
	 * @param root
	 *            根节点
	 * @author zhaopuqing
	 * @created 2016年8月30日 下午6:17:13
	 */
	private static void validateXmlFile(Element root) {

	}

}

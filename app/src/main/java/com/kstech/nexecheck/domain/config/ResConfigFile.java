package com.kstech.nexecheck.domain.config;

import android.content.Context;
import android.util.Log;

import com.kstech.nexecheck.domain.config.vo.CheckLineVO;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.domain.config.vo.FtpServerVO;
import com.kstech.nexecheck.domain.config.vo.ResourceVO;
import com.kstech.nexecheck.domain.config.vo.ResourceVO.MsgVO;
import com.kstech.nexecheck.domain.config.vo.SubDeviceVO;
import com.kstech.nexecheck.utils.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 与资源文件res_conf.xml的内容相对应
 */
public class ResConfigFile {
	/**
	 * 对应的资源文件名字
	 */
	private static final String CONFIG_FILE_PATH = "res_conf.xml";

	private List<CheckLineVO> checkLineList = new ArrayList<CheckLineVO>();

	private List<DeviceVO> deviceList = new ArrayList<DeviceVO>();

	private FtpServerVO ftpServerVO;

	private ResourceVO resourceVO;

	public void addDevice(DeviceVO deviceVO) {
		deviceList.add(deviceVO);
	}

	public void addCheckLine(CheckLineVO checkLineVO) {
		checkLineList.add(checkLineVO);
	}


	public FtpServerVO getFtpServerVO() {
		return ftpServerVO;
	}

	public void setFtpServerVO(FtpServerVO ftpServerVO) {
		this.ftpServerVO = ftpServerVO;
	}


	public ResourceVO getResourceVO() {
		return resourceVO;
	}

	public void setResourceVO(ResourceVO resourceVO) {
		this.resourceVO = resourceVO;
	}

	public List<CheckLineVO> getCheckLineList() {
		return checkLineList;
	}

	public String getUpLoadSSID(){
		for (CheckLineVO line:checkLineList) {
			if(line.getName().equals("数据上传")){
				return line.getSsid();
			}
		}
		return "";
	}

	public List<DeviceVO> getDeviceList() {
		return deviceList;
	}


	/**
	 * 加载资源文件，读取文件内容
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ResConfigFile readFromFile(Context context) {
		ResConfigFile result = new ResConfigFile();
		Document document;
		try {
			// 从sd卡下读取
			InputStream is = getRefInpuStream(CONFIG_FILE_PATH);
			// 从工程assets下读取
			//InputStream is = context.getAssets().open(CONFIG_FILE_PATH);
			SAXReader reader = new SAXReader();
			document = reader.read(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// 获取跟节点
		Element root = document.getRootElement();
		validateXmlFile(root);
		// 获取检线信息
		// 获取CheckLineSet节点
		Element checkLineSet = root.element("CheckLineSet");
		// 获取CheckLineSet节点 下的 CheckLine 节点集合
		List<Element> checkLineElementList = checkLineSet.elements("CheckLine");
		for (int i = 0; i < checkLineElementList.size(); i++) {
			Element elm = checkLineElementList.get(i);

			CheckLineVO checkLine = new CheckLineVO(elm.attribute("Name")
					.getText(), elm.attribute("SSID").getText(), elm.attribute(
					"Password").getText(), elm.attribute("IP").getText(), elm
					.attribute("TerminalID").getText());

			result.addCheckLine(checkLine);
		}

		// 获取Ftp服务器的配置信息
		Element ftpServerNode = root.element("FtpServer");
		FtpServerVO ftpServerVO = new FtpServerVO();
		Attribute ipAttr = ftpServerNode.attribute("IP");
		if (ipAttr != null) {
			ftpServerVO.setIp(ipAttr.getText());
		}
		Attribute portAttr = ftpServerNode.attribute("Port");
		if (portAttr != null) {
			ftpServerVO.setPort(portAttr.getText());
		}
		Attribute userAttr = ftpServerNode.attribute("User");
		if (userAttr != null) {
			ftpServerVO.setUser(userAttr.getText());
		}
		Attribute passwordAttr = ftpServerNode.attribute("Password");
		if (passwordAttr != null) {
			ftpServerVO.setPassword(passwordAttr.getText());
		}
		result.setFtpServerVO(ftpServerVO);

		// 获取DeviceSet节点
		Element deviceSet = root.element("DeviceSet");
		// 获取DeviceSet节点 下的 Device 节点集合
		List<Element> deviceElementList = deviceSet.elements("Device");
		for (int i = 0; i < deviceElementList.size(); i++) {
			Element elm = deviceElementList.get(i);

			DeviceVO device = new DeviceVO(elm.attribute("Id").getText(), elm
					.attribute("Name").getText(), elm.attribute("DevBornDate")
					.getText(), elm.attribute("DevDieDate").getText(), elm
					.attribute("DevStatus").getText(), null);

			List<Element> subDeviceElements = elm.elements("SubDevice");
			List<SubDeviceVO> subDeviceList = new ArrayList<SubDeviceVO>();
			for (int j = 0; j < subDeviceElements.size(); j++) {
				Element subDeviceElement = subDeviceElements.get(j);
				SubDeviceVO sub = new SubDeviceVO(subDeviceElement.attribute(
						"subDevId").getText(), subDeviceElement.attribute(
						"subDevName").getText());
				subDeviceList.add(sub);
			}

			device.setSubDeviceList(subDeviceList);
			result.addDevice(device);
		}

		// 解析Resource
		ResourceVO resourceVO = new ResourceVO();
		Element resourceNode = root.element("Resource");
		Element msgSetNode = resourceNode.element("MsgSet");
		List<?> msgNodes = msgSetNode.elements("Msg");
		List<MsgVO> msgList = new ArrayList<MsgVO>();
		for (int i = 0; i < msgNodes.size(); i++) {
			Element msgNode = (Element) msgNodes.get(i);
			MsgVO msgVO = new MsgVO(getAttribute(msgNode, "Id"), getAttribute(msgNode, "RefName"), getAttribute(msgNode, "Content"), (short) (i + 1));
			msgList.add(msgVO);
			resourceVO.putMsg(msgVO);
		}
		resourceVO.setMsgArray(msgList);

		Element imageSetNode = resourceNode.element("ImageSet");
		List<?> imageNodes = imageSetNode.elements("Image");
		for (int i = 0; i < imageNodes.size(); i++) {
			Element imgNode = (Element) imageNodes.get(i);
			resourceVO.putImage(getAttribute(imgNode, "Id"),
					getAttribute(imgNode, "RefName"),
					getAttribute(imgNode, "Content"), (short) (i + 1));
		}
		result.setResourceVO(resourceVO);

		return result;
	}

	private static InputStream getRefInpuStream(String name) throws FileNotFoundException {
		InputStream is = null;
		File file = new File(Globals.REFPATH + name);
		if(file.exists()){
			is = new FileInputStream(file);
			Log.i("FILE","找到文件RES");
		}
		return is ;
	}

	private static String getAttribute(Element node, String attrName) {
		Attribute attr = node.attribute(attrName);
		if (attr == null) {
			return null;
		} else {
			return attr.getText().trim();
		}
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

//		RealtimeParam个数不能超过14否则报错
	}

}

package com.kstech.nexecheck.domain.config.vo;
import android.net.wifi.WifiConfiguration;

/**
 * 检线配置VO
 */
public class CheckLineVO {
	/**
	 * 检线名称
	 */
	private String name;

	/**
	 * 网络热点ID
	 */
	private String ssid;
	/**
	 * 连接网络热点的密码
	 */
	private String password;
	/**
	 * 检线IP地址
	 */
	private String ip;
	/**
	 * 车上检测终端的ID
	 */
	private String terminalID;
	/**
	 * 用来表示信号强弱的图片
	 */
	private int image;

	public CheckLineVO() {
		super();
	}

	public CheckLineVO(String name, String ssid, String password,
					   String ip, String terminalID) {
		super();
		this.name = name;
		this.ssid = ssid;
		this.password = password;
		this.ip = ip;
		this.terminalID = terminalID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getTerminalID() {
		return terminalID;
	}

	public void setTerminalID(String terminalID) {
		this.terminalID = terminalID;
	}

	public int getImage() {
		return image;
	}

	public void setImage(int image) {
		this.image = image;
	}
	public boolean equalsSSID(String wifiSSID){
		return wifiSSID.equals("\"" + ssid + "\"") ;
	}
	public WifiConfiguration toWifiConfiguration(){
		WifiConfiguration result = new WifiConfiguration();
		result.SSID = "\"" + ssid + "\"";// \"转义字符，代表"
		result.preSharedKey = "\"" + password + "\"";// WPA-PSK密码
		result.hiddenSSID = false;
		result.priority = 100000;
		result.status = WifiConfiguration.Status.ENABLED;
		return result;
	}
}

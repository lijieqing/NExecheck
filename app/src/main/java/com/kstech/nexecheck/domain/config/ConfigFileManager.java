/**
 * 
 */
package com.kstech.nexecheck.domain.config;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.bluetooth.BluetoothClass.Device;

import java.util.List;


/**
 * The type Config file manager.
 *
 * @author tan
 */
public class ConfigFileManager {

	private Context context;
	// 最近一次使用系统的用户名字
	private static final String LAST_USER_NAME = "last_user_name";
	// 挖机编号
	private static final String LAST_EXCID = "last_excid";
	// 项目编号
	private static final String LAST_ITEMID = "last_itemid";
	// 距离上次是否有数据改变
	private static final String CHANGED = "changed";

	// 最近一次使用的检线名字
	private static final String LAST_CHECKLINE_NAME = "last_checkline_name";
	//最近一次使用的ssid
	private static final String LAST_CHECKLINE_SSID = "last_checkline_ssid";

	// 所有机型
	private static List<Device> allDevice = null;

	private static ConfigFileManager instance;
	private SharedPreferences preferences;

	private ConfigFileManager(Context context) {
		// 初始化首选项
		preferences = context.getSharedPreferences("LOGININFO",
				Activity.MODE_MULTI_PROCESS);
	}

	/**
	 * Gets instance.
	 *
	 * @return the instance
	 */
	public static synchronized ConfigFileManager getInstance(Context context) {
		if (instance == null) {
			instance = new ConfigFileManager(context);
		}
		return instance;
	}

	/**
	 * Gets last user name.
	 *
	 * @return the last user name
	 */
	public String getLastUserName() {
		return preferences.getString(LAST_USER_NAME, "");
	}

	/**
	 * Save last user name.
	 *
	 * @param userName the user name
	 */
	public void saveLastUserName(String userName) {
		Editor editor = preferences.edit();
		editor.putString(LAST_USER_NAME, userName);
		editor.commit();
	}

	/**
	 * Gets last check line name.
	 *
	 * @return the last check line name
	 */
	public String getLastCheckLineName() {
		return preferences.getString(LAST_CHECKLINE_NAME, "");
	}

	/**
	 * Save check line name.
	 *
	 * @param checkLineName the check line name
	 */
	public void saveCheckLineName(String checkLineName) {
		Editor editor = preferences.edit();
		editor.putString(LAST_CHECKLINE_NAME, checkLineName);
		editor.commit();
	}

	/**
	 * Save check line ssid.
	 *
	 * @param ssid the ssid
	 */
	public void saveCheckLineSsid(String ssid) {
		Editor editor = preferences.edit();
		editor.putString(LAST_CHECKLINE_SSID, ssid);
		editor.commit();
	}

	/**
	 * Gets last ssid.
	 *
	 * @return the last ssid
	 */
	public String getLastSsid() {
		return preferences.getString(LAST_CHECKLINE_SSID, "nossid");
	}

	/**
	 * Gets last excid.
	 *
	 * @return the last excid
	 */
	public String getLastExcid() {
		return preferences.getString(LAST_EXCID, "");
	}

	/**
	 * Save last excid.
	 *
	 * @param excId the exc id
	 */
	public void saveLastExcid(String excId) {
		Editor editor = preferences.edit();
		editor.putString(LAST_EXCID, excId);
		editor.commit();
	}

	/**
	 * Gets last itemid.
	 *
	 * @return the last itemid
	 */
	public String getLastItemid() {
		return preferences.getString(LAST_ITEMID, "");
	}

	/**
	 * Save last itemid.
	 *
	 * @param itemId the item id
	 */
	public void saveLastItemid(String itemId) {
		Editor editor = preferences.edit();
		editor.putString(LAST_ITEMID, itemId);
		editor.commit();
	}

}

package com.kstech.nexecheck.utils;

import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 *
 */
public class TCPConnectUtil {
	private static String TAG = "WIFIREFLECT";
	public static boolean connect(){
		// TODO
		return true;
	}
	public static boolean checkConnect(){
		// TODO
		return true;
	}
	/**
	 * 通过反射出不同版本的connect方法来连接Wifi
	 *
	 * @author jiangping.li
	 * @param netId
	 * @return
	 * @since MT 1.0
	 *
	 */
	public static Method connectWifiByReflectMethod(int netId, WifiManager mWifiManager) {
		Method connectMethod = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Log.i(TAG, "connectWifiByReflectMethod road 1");
			// 反射方法： connect(int, listener) , 4.2 <= phone's android version
			for (Method methodSub : mWifiManager.getClass()
					.getDeclaredMethods()) {
				if ("connect".equalsIgnoreCase(methodSub.getName())) {
					Class<?>[] types = methodSub.getParameterTypes();
					if (types != null && types.length > 0) {
						if ("int".equalsIgnoreCase(types[0].getName())) {
							connectMethod = methodSub;
						}
					}
				}
			}
			if (connectMethod != null) {
				try {
					connectMethod.invoke(mWifiManager, netId, null);
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "connectWifiByReflectMethod Android "
							+ Build.VERSION.SDK_INT + " error!");
					return null;
				}
			}
		} else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
			// 反射方法: connect(Channel c, int networkId, ActionListener listener)
			// 暂时不处理4.1的情况 , 4.1 == phone's android version
			Log.i(TAG, "connectWifiByReflectMethod road 2");
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
				&& Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			Log.i(TAG, "connectWifiByReflectMethod road 3");
			// 反射方法：connectNetwork(int networkId) ,
			// 4.0 <= phone's android version < 4.1
			for (Method methodSub : mWifiManager.getClass()
					.getDeclaredMethods()) {
				if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
					Class<?>[] types = methodSub.getParameterTypes();
					if (types != null && types.length > 0) {
						if ("int".equalsIgnoreCase(types[0].getName())) {
							connectMethod = methodSub;
						}
					}
				}
			}
			if (connectMethod != null) {
				try {
					connectMethod.invoke(mWifiManager, netId);
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "connectWifiByReflectMethod Android "
							+ Build.VERSION.SDK_INT + " error!");
					return null;
				}
			}
		} else {
			// < android 4.0
			return null;
		}
		return connectMethod;
	}
}

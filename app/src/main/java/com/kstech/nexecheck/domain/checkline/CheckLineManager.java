package com.kstech.nexecheck.domain.checkline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckLineVO;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.TCPConnectUtil;


/**
 * @author tan
 * 
 */
public class CheckLineManager {


	/**
	 * 获取缺省使用的检线
	 *
	 * @return
	 */
	public static CheckLineVO getDefaultCheckLine(Context context) {
		// 获取配置文件中的检线
		String lastCheckLineName = ConfigFileManager.getInstance(context)
				.getLastCheckLineName();
		for (CheckLineVO checkLineVO : Globals.getResConfig().getCheckLineList()) {
			if (checkLineVO.getName().equals(lastCheckLineName)) {
				return checkLineVO;
			}
		}
		return Globals.getResConfig().getCheckLineList().get(0);

	}

	/**
	 * 获取有效的检线
	 *
	 * @return
	 */
	public static List<CheckLineVO> getValidateCheckLines(Context context) {
		// 获取wifi环境下有效的检线
		List<CheckLineVO> localWifiList = getLocalWifiList(context);
		// 处理检线信号
		for (CheckLineVO config :  Globals.getResConfig().getCheckLineList()) {
			boolean flag = false;
			for (CheckLineVO local : localWifiList) {
				if (config.getSsid().equals(local.getSsid())) {
					config.setImage(local.getImage());
					flag = true;
					break;
				}
			}
			if (!flag) {
				config.setImage(R.drawable.wifi05);
			}
		}
		return  Globals.getResConfig().getCheckLineList();
	}

	public static void connectCheckLine(CheckLineVO checkLineVO,Context context) {
		// 连接wifi
		connectWifi(checkLineVO,context);

	}

	/**
	 * 连接wifi
	 *
	 * @param wifiItemSSID
	 * @param wifiPassword
	 */
	private static synchronized void connectWifi(CheckLineVO checkLineVO,Context context) {
		WifiManager localWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if(localWifiManager.isWifiEnabled()){
			List<WifiConfiguration> wifiConfigList = localWifiManager.getConfiguredNetworks();
			int networkId = -1;
			for (WifiConfiguration wifi : wifiConfigList) {
				if (checkLineVO.equalsSSID(wifi.SSID)) {
					networkId = wifi.networkId;
					break;
				}
			}
			if (networkId == -1) {// 没有配置好信息，配置
				networkId = localWifiManager.addNetwork(checkLineVO
						.toWifiConfiguration());// 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
			}
			Method method = TCPConnectUtil.connectWifiByReflectMethod(networkId, localWifiManager);
			if(method == null)localWifiManager.enableNetwork(networkId, true);
		}
	}

	private static List<CheckLineVO> getLocalWifiList(Context context) {
		List<CheckLineVO> result = new ArrayList<>();
		WifiManager localWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (!localWifiManager.isWifiEnabled()) {
			localWifiManager.setWifiEnabled(true);
		}
		localWifiManager.startScan();
		// 0正在关闭,1WIFi不可用,2正在打开,3可用,4状态不可zhi
		while (localWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			// 等待Wifi开启
			Log.i("WifiState", String.valueOf(localWifiManager.getWifiState()));
		}
		// 休眠3s，不休眠则会在程序首次开启WIFI时候，处理getScanResults结果，wifiResultList.size()发生异常
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<ScanResult> wifiResultList = localWifiManager.getScanResults();

		for (int i = 0; i < wifiResultList.size(); i++) {
			ScanResult strScan = wifiResultList.get(i);
			CheckLineVO vo = new CheckLineVO();
			vo.setSsid(strScan.SSID);
			// 信号强度处理
			int resultLv = WifiManager.calculateSignalLevel(strScan.level, 100);
			Log.i("CheckLineManager", "检线的SSID=" + strScan.SSID + "   原始信号强度="
					+ strScan.level + "   处理信号强度=" + resultLv);
			if (resultLv > 90) {
				vo.setImage(R.drawable.wifi01);
			} else if (resultLv > 60) {
				vo.setImage(R.drawable.wifi02);
			} else if (resultLv > 30) {
				vo.setImage(R.drawable.wifi03);
			} else {
				vo.setImage(R.drawable.wifi04);
			}
			result.add(vo);
		}
		return result;
	}
}

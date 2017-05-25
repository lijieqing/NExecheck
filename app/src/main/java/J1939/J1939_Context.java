package J1939;

/**
 * J1939环境变量，包括数据变量、数据箱、API等，
 * 由于所有成员都是静态变量，所以无需实例化本类
 */
public class J1939_Context {

	/**
	 * J1939配置的变量数组，通过解析配置文件的<DataSet>标记及其子标记形成
	 * 在解析配置文件时实例化
	 */
	public static J1939_DataVar_ts[] 	j1939_DataVarCfg;

	/**
	 * J1939配置，通过解析配置文件形成
	 * 在解析配置文件时实例化
	 */
	public static J1939_Cfg_ts   		j1939_Cfg;
	
	/**
	 * J1939 API类
	 * 在J1939任务初始化时实例化
	 */
	public static J1939					j1939_API;

	/**
	 * J1939通讯相关信息类
	 * 在J1939任务初始化时实例化
	 */
	public static J1939_CommCfg_ts 		j1939_CommCfg;

}

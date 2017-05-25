package J1939;

//定义J1939配置结构
public class J1939_Cfg_ts {

	/**
	 * 运行J1939的CAN通道号（1 -- n )
	 */
	public byte					bCanCh;

	/**
	 * 本J1939节点的地址 （ 0 -- 255 ），配置文件<J1939>标记的“NodeAddr”属性值
	 * 本手持检测终端的J1939节点地址
	 */
	public short				bNodeAddr;

	/**
	 * J1939任务运行周期, 配置文件<J1939>标记的“Cycle”属性值
	 * J1939任务的运行周期。在运行周期中处理所有PGN的接收、发送和请求
	 */
	public int					wCycle;

	/**
	 * J1939任务运行优先级，配置文件<J1939>标记的“TaskPrio”属性值。
	 * J1939任务的优先级，无意义
	 */
	public byte					bTaskPrio;

	/**
	 * 配置的PGN数量。根据配置文件得到
	 */
	public short				wPGNums;

	/**
	 * 指向配置的PGN数组。当解析配置文件的<PG>标记时生成此数组
	 */
	public J1939_PGCfg_ts[]		pPGCfg;

	/**
	 * 指向配置的第一个SP。所有SP配置形成一个单向链表
	 */
	public J1939_SPCfg_ts		pFirstSPCfg;



}


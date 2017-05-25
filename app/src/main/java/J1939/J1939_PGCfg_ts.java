package J1939;

public class J1939_PGCfg_ts {
	
	public int dwPGN; 			// PGN = (DP+PF+DA)
	public int dwRate; 			// 发送或接收频率（ms)
	public int dwReqCycle; 		// 周期性请求时的请求周期。为0表示非周期性请求
	public byte bPGType; 		// PGN类型
	public byte bDir; 			// 方向 （ 0 -- RX, 1 -- TX)
	public byte bPrio;	 		// 优先级
	public byte bSA; 			// 源节点地址
	public short wLen; 			// 数据长度
	public byte bReq; 			// 本节点是否请求该PGN
	public short bSPNums; 		// 组成该PG的SP个数
	public J1939_SPCfg_ts[] pSPCfg; 	// 指向该PG的SP配置数组
	public byte[] pData; 		// 该PGN的运行数据
	public int wDBNumber; 		// 接收该PG的数据箱号

	public J1939_PGCfg_ts() {
		// TODO Auto-generated constructor stub
	}

	// 构造函数 
	public J1939_PGCfg_ts(int dwPGN, int dwRate, int dwReqCycle, byte bPGType,
			byte bDir, byte bPrio, byte bSA, short wLen, byte bReq,
			short bSPNums, J1939_SPCfg_ts[] pSPCfg) {
		super();
		this.dwPGN = dwPGN;
		this.dwRate = dwRate;
		this.dwReqCycle = dwReqCycle;
		this.bPGType = bPGType;
		this.bDir = bDir;
		this.bPrio = bPrio;
		this.bSA = bSA;
		this.wLen = wLen;
		this.bReq = bReq;
		this.bSPNums = bSPNums;
		this.pSPCfg = pSPCfg;
	}
	
}

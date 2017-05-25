package J1939;

public class J1939_SPCfg_ts {
	public J1939_SPCfg_ts pNextSPCfg; 			// 所有SP形成单向链表
	public J1939_PGCfg_ts pPGCfg; 				// 上级PG
	public float fRes; 							// 精度
	public float fOffset; 						// 修正量
	public int dwSPN; 							// SPN
	public short wStartByte; 					// 该SP起始字节（1基）
	public byte bStartBit; 						// 在起始字节中的起始位
	public byte bBytes; 						// 该SP占用的完整字节数。如果非0则起始位必须为1
	public byte bBits; 							// 该SP占用位数。字节数为0时有效
	public byte bSPType; 						// SP类型 ( 0 -- Measured, 1 -- status)
	public byte bFMI; 							// 该SPN的当前故障状态码
	public byte bDTCNums; 						// 该SP的DTC数量
	public J1939_DTCfg_ts[] pDTCfg; 			// 指向该SP的DTC配置数组
	public J1939_DTCfg_ts pLastDTC; 			// 该SPN最后一次故障的关联DTC配置项


	/**
	 * 根据FMI值判断是否有对应的DTC存在，有则返回DTC，没有返回NULL
	 * @param fmi
	 * @return
	 */
	public J1939_DTCfg_ts getDtc(byte fmi){
		if(pDTCfg==null){
			return null;
		}
		for(J1939_DTCfg_ts dtc:pDTCfg){
			if(dtc.bFMI==fmi){
				return dtc;
			}
		}
		return null;
	}

	/**
	 * SPN关联数据索引
	 */
	public short wRefDataIdx;
	/**
	 * SPN关联数据类型
	 */
	public byte bRefDataType;

}

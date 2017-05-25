package J1939;

//定义故障诊断码结构

public class J1939_dtc_ts {
	public int 	spn_u32;					// 故障源对应的SPN
	public byte	fmi_u8;						// 故障模式
	public byte	occ_u8;						// 故障出现次数
}



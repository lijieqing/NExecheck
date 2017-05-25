package J1939;

//接收数据箱
public class J1939_TxCfg_ts {

	// 配置属性
	short  				length_u16;						// J1939接收数据箱个数
	J1939_txMsg_ts[] 	start_pas;						// 指向接收数据箱数组

	// 运行时属性
	int			rts_PGN_u32;							// 正在使用传输协议(RTS-CTS)向指定目的节点发送的长消息帧PGN
	short		rts_boxNumber_u16;						// 点对点传输的长帧对应的发送数据箱号
	byte		cts_Frames_u8;							// CTS指定的可连续发送的帧数
	byte		cts_txFrames_u8;						// 已发送帧数
	byte		cts_txFrameNo_u8;						// 发送帧顺序号
	long		cm_Timeout_u32;							// 各状态的超时时间	
	//uint32	cm_minTimeout_u32;						// 最小超时时间（必须等待至此时刻才能进行下一步）, 点对点长帧传输协议不需要此字段
	
	int  		bam_PGN_u32;							// 正在使用传输协议(BAM)广播发送的长消息帧PGN
	short		bam_boxNumber_u16;						//	广播传输的长帧对应的发送数据箱号
	byte		bam_txFrameNo_u8;						//	广播帧顺序号
	long		bam_minTimeout_u32;						// 最小超时时间（必须等待至此时刻才能进行下一步），广播长帧传输协议需要此字段
	long		bam_Timeout_u32;						// 各状态的超时时间	，广播长帧传输协议需要此字段

	byte		dm1TxInCycle_u8;						// DM1发送周期是否已发送额外DM1消息
	byte		dm1Changed_u8;							// DM1发送周期中DM1队列是否已改变
	
	// 构造函数 
	public J1939_TxCfg_ts() {
		super();
		this.length_u16 = 0;							// 发送数据箱数量 
		this.start_pas = null;							// 发送数据箱数组 
	}
	
}


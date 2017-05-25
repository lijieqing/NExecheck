package J1939;

// 发送数据箱
public class J1939_txMsg_ts {

	public int		canID_u;							// CAN-ID （PGN+DA+SA)
	public short	cycle_u16;							// 发送周期;
	public short	counter_u16;						// 已	 
	public int		status_u16;							// 数据箱状态
	public short	lenTx_u16;							// 已发送字节
	public short	lenAct_u16;							// 消息数据区长度（字节数）， 1 -- 196 
	public byte		respDA_u8;							// 请求PGN的应答目标地点地址。其他节点请求该PGN时填写，应答完成后清除。
	public byte[]	data_pau8;							// 指向消息数据区
	public ITxDBoxCallback func_pf; 					// 发送消息前调用的回调函数
	public Object	pfCallbackArg;						// 回调函数调用参数
	public long		nextTxTime_u32;						// 运行时属性：下次发送时间
	public int 		respPGN_u32;						// 运行时属性：响应PGN请求时的应答PGN

	public J1939_txMsg_ts() {
		// TODO Auto-generated constructor stub
		super();
	}

}

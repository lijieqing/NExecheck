package J1939;

public class J1939_rxMsg_ts {
	public int		canID_u;						// CAN-ID
	public int		timeout_u16;					// 周期性接收消息的接收周期。0表示非周期性接收消息 
	public int		startTimeout_u16;				// 启动后首次收到该消息的时限	
	public int		counter_u16;					// 接收到此消息的次数
	public int		status_u16;						// 数据箱状态
	public int		action_u16;						//
	public short	lenAct_u16;						// 应接收的消息长度
	public short	lenRx_u16;						// 已接收到的消息长度
	public short	lenMax_u16;						// 可接收消息最大长度。如果应接收的消息长度大于此值则导致溢出
	public Object 	data_pau8;						// 指向接收消息数据区, 对普通消息，为byte[8]; 对DM1， 为J1939_dtcList_ts[][]
	public byte[]  	rxbuf_pau8;						// 指向临时接收消息数据区。接收长帧时先将数据帧中的数据放在此缓冲区中，接收完成时复制到data_pau8中
	public long		cm_Timeout_u32;					// 长帧接收期间的超时时间
	public IRxDBoxCallback func_pf;					// 接收消息后调用的回调函数
	public Object	pfArg;							// 调用回调函数时的参数
	
	// 构造函数 
	public J1939_rxMsg_ts() {
		super();
	}
	
};

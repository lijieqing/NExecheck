package J1939;

import java.util.LinkedList;
import java.util.List;

public interface IJ1939_API {

	static final int DM1_MAXDTCS	 = 20;		//  最大激活故障数
	static final int DM2_MAXDTCS	 = 20;		//  最大激活故障数

	static final int CAN_RXFIFO_SIZE = 200;		//  CAN帧接收缓冲区大小
	static final int CAN_TXFIFO_SIZE = 200;		//  CAN帧发送缓冲区大小

	static final int PGN_DM1 	=	0xFECA;		//	当前故障表
	static final int PGN_DM2 	=	0xFECB;		//

	static final int PF_REQPGN =	0xEA;		// 234,请求PGN
	static final int PF_REQACK	= 	0xE8;		// 232,确认PGN
	static final byte ACK		=	0x00;		//		确认帧中的控制字节 -- OK
	static final byte NACK		=	0x01;		//		确认帧中的控制字节 -- 不支持所请求的PGN
	static final byte DENY		=	0x02;		//		确认帧中的控制字节 -- 禁止访问
	static final byte NRESP		=	0x03;		//		确认帧中的控制字节 -- 不响应

	static final int PF_PRIV	=	0xEF;		// 239, 厂家自定义的PGN,可指定接收节点
	static final int PF_PRICBC	= 	0xFF;		// 255, 厂家自定义的PGN，只能广播发送

	static final int DA_GLOBAL	= 	0xFF;		// 255, 广播地址
	static final int DA_INVALID =	0xFE;		// 254, 无效节点地址
	static final int DA_NONE	= 	0xFE;		// 254, 未指定节点地址

	static final int PF_DT		= 	0xEB;		// 235, 传输协议的数据帧PGN

	static final int PF_CM		= 	0xEC;		// 236, 传输协议的连接管理帧PGN
	static final int CM_RTS		=	0x10;		//		连接管理帧中的控制字节  -- 请求发送
	static final int CM_CTS		=	0x11;		//		连接管理帧中的控制字节  -- 已准备好接收
	static final int CM_EOMACK	=	0x13;		//		连接管理帧中的控制字节  -- 已完成接收
	static final int CM_ABORT	=	0xFF;		//		连接管理帧中的控制字节  -- 中止连接
	static final int CM_BAM		= 	0x20;		//		连接管理帧中的控制字节  -- 即将广播发送

	static final int ABORT_REASON_TOOMANY	= 0x01;		// 中止原因：不支持更多的连接管理会话
	static final int ABORT_REASON_NORES		= 0x02;		// 中止原因：因别的任务需要系统资源而中止连接管理会话
	static final int ABORT_REASON_TIMEOUT	= 0x03;		// 中止原因：因超时而中止连接
	static final int ABORT_READON_UNKNOWN	= 0x04;		// 中止原因：不明

	// 定义CAN帧类型
	static final byte CAN_STD	=	0;		// 标准帧
	static final byte CAN_EXD	=	1;		// 扩展帧

	// 定义PGN格式
	static final byte MODE_PDU1	=	1;		// PDU1
	static final byte MODE_PDU2	=	2;		// PDU2

	// 定义CAN帧FIFO操作返回码
	static final int CAN_ERR_NO_ERRORS_DU16			= 0;		//  Function is called without errors. Buffer was registered successfully.
	static final int CAN_ERR_CHNL_DU16				= 14;		//  Unknown CAN channel.
	static final int CAN_ERR_NUM_MSG_DU16			= 18;		//  Invalid number of messages.
	static final int CAN_ERR_BUF_COUNT_DU16			= 50;		//  To less transmit buffer registered. (BODAS-design)
	static final int CAN_ERR_OVERLAP_ADDR_DU16		= 55;		//  Buffer overlaps with buffer registered already.
	static final int CAN_ERR_INVALID_ADDR_DU16		= 60;		//  Invalid address.
	static final int CAN_ERR_CFG_NOTALLOWED_DU16	= 99;		//  Configuration is not allowed.
	//  Note: Configuration has to be made only in function sys_main() (C-API).

	// 定义J1939协议栈状态
	static final int J1939_CASTATUS_NONE_DU160		= 0x0000;  	// Controller application is not yet initialized,
	// or invalid function parameter.

	static final int J1939_CASTATUS_INIT_DU16		= 0x0100;	// Controller application is initialized.
	static final int J1939_CASTATUS_WAIT_CLAIM_DU16	= 0x0200;	// Controller application waits for answers during an address
	//claim procedure.

	static final int J1939_CASTATUS_ADDR_CLAIMED_DU16 =	0x0300;	// Controller application could claim an address. Message traffic
	// was started.  This is the normal operation state.

	static final int J1939_CASTATUS_CANNOT_CLAIM_DU16 =	0x0400;	// Controller application could not claim an address. There was
	// an address conflict with another controller application. The
	// message traffic will not be started.

	// The following sub status may be set in addition to the
	// main status above

	static final int J1939_SUBSTATUS_TIMEOUT_DU16	  =	0x0001;	// The J1939 communication stack could not handle all J1939 messages
	// within one cycle.
	//		Possible reasons:
	//		- The maximum execution time, which is defined with
	//		  j1939_initComm, is to small.
	//		- The execution time of one or more callback functions is
	//		  to high.

	static final int J1939_SUBSTATUS_RXOVFL_DU16	  =	0x0002;	// A J1939 CAN receive buffer overflow occured. One or more
	// CAN RX message(s) may be lost.
	// 		Possible reasons:
	//		- The registered CAN RX buffer on the corresponding
	//		  CAN channel is to small.
	//		- The J1939 cycle time is to high. Therefore to many
	//		  CAN messages will be received between two J1939 cycles

	static final int J1939_SUBSTATUS_TXOVFL_DU16 = 0x0004;		// A J1939 CAN transmit buffer overflow occured. One or more
	// CAN RX message(s) may be lost.
	// 		Possible reasons:
	//		- The registered CAN TX buffer on the corresponding
	//		  CAN channel is to small.

	// 定义故障范围
	static final long J1939_SPN_ALL_DU32 			= 0xFFFFFFFFl;
	static final int J1939_FMI_ALL_DU8				= 0xFF;


	// 定义J1939 数据箱的状态
	static final int J1939_BOXSTATUS_NONE_DU16 			= 0x0000;	// Message databox is not yet initialized.
	static final int J1939_BOXSTATUS_INIT_DU16 			= 0x0100;	// Message databox is initialized.
	static final int J1939_BOXSTATUS_VALID_DU16 		= 0x0200;	// The data within the receive message databox are valid.

	static final int J1939_BOXSTATUS_VAL_OVFL_DU16 		= 0x0201;	// The data within the receive message databox are valid,
	// but incomplete. A buffer overflow occurs (received DM1/2
	// msg was to long).

	static final int J1939_BOXSTATUS_INV_SEQNBR_DU16 	= 0x0301;	// Databox data are invalid, wrong sequence nbr received.
	static final int J1939_BOXSTATUS_INV_ABORT_DU16  	= 0x0302;	// Databox data are invalid, rx/tx transfer aborted.
	static final int J1939_BOXSTATUS_INV_TIMEOUT_DU16 	= 0x0303;	// Databox data are invalid, timeout occurs.
	static final int J1939_BOXSTATUS_INV_OVFL_DU16 	 	= 0x0304;	// Databox data are invalid, overflow error.

	static final int J1939_BOXSTATUS_PENDING_DU16		= 0x0400;	// 发送数据箱待发送，或接收数据箱接收未完成
	static final int J1939_BOXSTATUS_BAM_RX_DU16		= 0x0500;	// Receiving of a BAM message is in progress.
	static final int J1939_BOXSTATUS_BAM_TX_DU16		= 0x0600;	// Transmitting of a BAM message is in progress
	static final int J1939_BOXSTATUS_UNPACK_DU16		= 0x0700;	// 正在调用回调函数

	static final int J1939_BOXSTATUS_RTS_DU16			= 0x0800;	// 已发送RTS帧，等待接收CTS帧 或已收到RTS
	static final int J1939_BOXSTATUS_CTS0_DU16			= 0x0900;	// 已收到CTS(0)连接保持帧，等待接收CTS
	static final int J1939_BOXSTATUS_DT_TX_DU16			= 0x0A00;	// 已收到CTS帧，正在发送 DT(s)帧
	static final int J1939_BOXSTATUS_WAIT_EOM_DU16		= 0x0B00;	// 所有DT帧已发送，等待接收EndOfMsgAck帧

	// 定义函数返回值
	static final int J1939_RC_NO_ERRORS_DU16			= 0; 		// Function was executed without errors.
	static final int J1939_RC_INVALID_ADDR_DU16			= 1;		// Address to data array is Invalid.
	static final int J1939_RC_INV_DATABOX_DU16			= 2;		// Invalid databox number.
	static final int J1939_RC_INV_PGN_DU16				= 3;		// Invalid parameter group number (PGN).
	static final int J1939_RC_INV_SRC_DU16				= 4;		// Invalid source address.
	static final int J1939_RC_INV_TIMEOUT_DU16			= 5;		// Invalid timeout value.
	static final int J1939_RC_INV_DATA_LEN_DU16			= 6;		// Invalid data length.
	static final int J1939_RC_INV_DATA_PTR_DU16			= 7;		// Invalid address pointer.
	static final int J1939_RC_INV_STARTTIMEOUT_DU16		= 9;		// Invalid start up timeout value.
	static final int J1939_RC_INV_CYCLE_DU16			= 10;		// Invalid cycle time.
	static final int J1939_RC_INV_OFFSET_DU16 			= 11;		// Invalid offset time.
	static final int J1939_RC_INV_PRIO_DU16				= 12;		// Invalid priority.

	static final int J1939_RC_PGN_SRC_EXIST_DU16		= 20;		// Another message box with the same PGN and source address is
	// alraedy defined.

	static final int J1939_RC_PGN_PRIO_EXIST_DU16		= 21;		// Another message box with the same PGN and priority is
	// alraedy defined.

	static final int J1939_RC_CFG_NOTALLOWED_DU16		= 22;		// Function must be called before j1939_initComm(), but it was
	// called afterwards.

	static final int J1939_RC_INV_BOX_STATUS_DU16		= 30;		// Invalid databox status. Databox is not yet initialized.

	static final int J1939_RC_INV_SPN_DU16 				= 40;		// Invalid suspect parameter number.
	static final int J1939_RC_INV_FMI_DU16				= 41;		// Invalid failure mode indicator.

	static final int J1939_RC_DM1_FULL_DU16				= 42;		// Failure could not be stored in DM1 list, because maximum
	// number of failures reached.

	static final int J1939_RC_DM2_FULL_DU16 			= 43;		// Failure could not be stored in DM2 list, because maximum
	// number of failures reached.

	static final int J1939_RC_SPN_NOT_FOUND_DU16		= 44;		// Failure could not be found within the DM1 list.

	static final int J1939_RC_INV_ECU_ADDR_DU16			= 50;		// Invalid controller (ECU) address.
	static final int J1939_RC_INV_DM_TYPE_DU16 			= 51;		// Invalid DM failure type.

	static final int J1939_RC_INV_LIST_SIZE_DU16		= 60;		// Invalid failure list size.
	static final int J1939_RC_INV_EE_PAGE_DU16			= 61;		// Invalid eeprom page number.
	static final int J1939_RC_INV_EE_IDX_DU16			= 62;		// Invalid eeprom page index.

	static final int J1939_RC_INIT_MISSING_DU16			= 63;		// Function is not possible, because j1939_initFailureList()
	// was not yet executed.

	static final int J1939_RC_EEP_OVERFLOW_DU16			= 70;		// Failures could not be deleted, because internal EEPROM
	// queue overflow.

	static final int	J1939_RC_DM1_NOT_EXIST_DU16		= 0;		// DM1 failure does not exist within DM1 failure list.
	static final int	J1939_RC_DM1_EXIST_DU16			= 1;		// DM1 failure(s) exist(s) within DM1 failure list.


	// 定义故障表类型
	static final int J1939_LIST_DM1_DU16				= 0x01; 	// DM1 failure list, use CM version 3 for CM-Bit=1
	static final int J1939_LIST_DM2_DU16 				= 0x02;		// DM2 failure list, use CM version 3 for CM-Bit=1
	static final int J1939_LIST_DM1_V3_DU16				= 0x31; 	// DM1 failure list, use CM version 3 for CM-Bit=1
	static final int J1939_LIST_DM2_V3_DU16 			= 0x32;		// DM2 failure list, use CM version 3 for CM-Bit=1
	static final int J1939_LIST_DM1_V1_DU16 			= 0x11;		// DM1 failure list, use CM version 1 for CM-Bit=1
	static final int J1939_LIST_DM2_V1_DU16 			= 0x12;		// DM2 failure list, use CM version 1 for CM-Bit=1
	static final int J1939_LIST_DM1_V2_DU16 			= 0x21;		// DM1 failure list, use CM version 2 for CM-Bit=1
	static final int J1939_LIST_DM2_V2_DU16				= 0x22;		// DM2 failure list, use CM version 2 for CM-Bit=1


	// 超时定义
	static final int TIMEOUT_T1							= 750;			// T1
	static final int TIMEOUT_RXBAM_TO_RXDT				= TIMEOUT_T1;	// BAM:		接收BAM帧与第一个DT帧之间的超时值
	static final int TIMEOUT_RXDT_TO_RXDT				= TIMEOUT_T1;	// BAM&RTS:相邻DT接收帧的超时值

	static final int TIMEOUT_T2							= 1250;			// T2
	static final int TIMEOUT_TXCTS_TO_RXFIRSTDT			= TIMEOUT_T2;	// 接收方：送出CTS帧后等待第一个DT帧到达的超时值

	static final int TIMEOUT_T3							= 1250;			// T3
	static final int TIMEOUT_TXRTS_TO_RXCTS				= TIMEOUT_T3;	// 发送方：发送RTS至接收到CTS帧的超时值
	static final int TIMEOUT_TXDT_TO_RXCTS				= TIMEOUT_T3;	// 发送方：发送CTS期间最后一帧至接收到下一个CTS帧间的超时值
	static final int TIMEOUT_TXDT_TO_RXEOM				= TIMEOUT_T3;	// 发送方：发送完所有DT帧，等待接收EndOfMessageAck帧的超时值

	static final int TIMEOUT_T4							= 1050;			// T4
	static final int TIMEOUT_RXCTS0_TO_RXCTS			= TIMEOUT_T4;	// 发送方：接收到CTS(0)保持连接至接收下一个CTS帧的超时值

	static final int TIMEOUT_Th							= 500;			// Th
	static final int TIMEOUT_TXCTS0_TO_TXCTS			= TIMEOUT_Th;	// 接收方：发出CTS(0)保持连接帧至发出CTS帧的超时值

	static final int TIMEOUT_Tr							= 200;			// Tr
	static final int TIMEOUT_RX_TO_RESP					= TIMEOUT_Tr;	// 发送方、接收方：收到帧后到发出应答帧之间的超时值

	static final int BAM_TXDT_TO_TXDT_MIN				= 50;			// BAM发送方: 帧间最小时间间隔
	static final int BAM_TXDT_TO_TXDT_MAX				= 200;			// BAM发送方：帧间最大时间间隔

// 接口方法

	// 挂接CAN帧接收缓冲区
	public int can_registerRxBuf(
			byte chnl_u8,							//	CAN通道号, 0 -- n;
			List<can_Message_ts> buf_pas,		//	CAN帧接收缓冲区FIFO队列
			short numMsg_16							//	接收缓冲区大小（FIFO项数）
	);

	// 挂接CAN发送FIFO缓冲区
	public int can_registerTxBuf(
			byte chnl_u8,							//	CAN通道号, 0 -- n;
			List<can_Message_ts> buf_pas,		//	CAN帧发送缓冲区FIFO队列
			short numMsg_16							//	接收缓冲区大小（FIFO项数）
	);

	// J1939任务初始化
	public int j1939_initComm(
			byte canChnl_u8 , 						// 	CAN通道号, 0 -- n
			short ownAddr_u8 , 						// 	本CAN节点地址, 0x00 -- 0xFD
			byte priority_u8 , 						// 	J1939任务的优先级， 0 -- 255
			short cycleTime_u8 , 					// 	J1939任务的运行周期 ， 1 -- 90ms, 推荐10ms
			byte  maxTime_u8 						// 	J1939任务的运行时间与运行周期的比率的最大值，1 -- 99， 推荐20%
	);

	// 挂接J1939消息接收数据箱
	public int  j1939_registerRxDataboxes(
			//j1939_rxMsg_ts *mem_pas ,				//
			J1939_rxMsg_ts[] mem_pas,				//	接收数据箱数组
			short num_u16 							// 	J1939消息接收数据箱数组大小（数据箱个数）
	);

	// 挂接J1939消息发送数据箱
	public int j1939_registerTxDataboxes(
			//j1939_txMsg_ts *mem_pas , 			//
			J1939_txMsg_ts[] mem_pas,				//	发送数据箱数组
			short num_u16 							// 	J1939消息消息发送数据箱数组大小（数据箱个数）
	);

	// 初始化J1939消息接收数据箱
	public int  j1939_initRxDatabox(
			short boxNum_u16 , 						//	消息接收数据箱索引, 1 -- n
			int pgn_u32 , 							//	此数据箱要接收的PGN，0 -- 0x3FFFF (18位）
			short src_u8 , 							//	此数据箱要接收的PGN的发送源地址， 0x00 -- 0xFD
			int timeout_u16 , 						//	消息接收周期（ms), 10 -- 65000。0表示非周期性接收数据
			int startTimeout_u16 , 					//	系统启动后第一次接收至此消息的时限（ms)，10 -- 65000。0表示无时限
			short lenMax_u16 , 						//	消息数据区最大长度（字节数）， 1 -- 196
			Object data_pau8 , 						//	指向消息数据区
			IRxDBoxCallback func_pf, 				//	接收到此消息时调用的回调函数
			Object	pCallbackArg					//  调用回调函数时使用的参数值
	);

	// 初始化J1939消息发送数据箱
	public int j1939_initTxDatabox(
			short boxNum_u16 , 						//	消息发送数据箱索引, 1 -- n
			int pgn_u32 , 							// 	此数据箱要发送的PGN，0 -- 0x3FFFF (18位）
			byte prio_u8 , 							// 	发送此消息的优先级， 0 -- 7 ，默认为6
			int cycle_u16 , 						//	消息发送周期（ms), J1939任务周期 -- 65000， 0表示非周期性发送消息（只能通过j1939_SendDatabox发送）
			int offset_u16 , 						//	消息首次发送时间提前量（ms), 0 -- cycle_u16, 用于避免多个同周期发送消息引起的总线过载。
			short lenAct_u16 , 						//	消息数据区中实际数据长度（字节数）， 1 -- 196
			byte[] data_pau8 , 						//	指向消息数据区
			ITxDBoxCallback func_pf, 				// 	发送此消息前调用的回调函数
			Object pCallbackArg						// 	调用回调函数时使用的参数值
	);

	// 获取J1939接收数据箱状态
	public int  j1939_getRxDataboxStatus(
			short boxNum_u16 , 						//	接收数据箱索引， 1 -- n
			int[]  msgLen_pu16 						//	返回时存放数据箱中已接收数据长度
	);

	// 发送指定数据箱
	public int  j1939_sendDatabox(
			short boxNum_u16 						//	发送数据箱索引， 1 -- n
	);

	// 	获取J1939协议栈状态
	public int j1939_getStatus(
			byte canChnl_u8 						//	CAN通道号
	);

	// 发送请求PGN
	public int  j1939_sendRequestPGN (
			short destAddr_u8 ,						// 被请求节点地址（0--253， 255）
			int  pgn_u32							// 被请求PGN ( 0 -- 0x003FFFF)
	);

	// 准备接收其他节点故障表
	public int  j1939_initDiagnostic (
			short srcAddr_u8 , 						//	本节点要接收其DM1/DM2故障表的的故障源（即其他节点）地址
			int dmType_u16 , 						//	要接收的故障表类型及CM=1时的转换方法版本。对CM=0,总是使用转换方法版本4.
			J1939_dtcList_ts dtcList_ps ,			//	指向应用定义的故障表
			short  boxNum_u16 , 					//	接收DM1/DM2故障源所使用的接收数据箱编号
			IRxDBoxCallback func_pf, 				//	完整接收DM1/DM2故障消息后调用的回调函数
			Object pCallbackArg						//	调用回调函数时使用的参数值
	);

	// 请求其它节点发送其DM1或DM2故障表
	public int j1939_getDiagnostic (
			short destAddr_u8 ,						//	被请求节点地址
			int dmType_u16							//	被请求的故障类型
	);

	//	初始化本控制器应用的DM1和DM2故障处理
	public int  j1939_initFailureList (
			J1939_failure_ts listFailure_pas ,			//	指向 j1939_failure_ts类型的故障表
			short listSize_u16 ,						//	故障表大小（项数）, 有效值范围 1 -- 85，受限于可用内存及EEPROM
			short eePage_u16 ,							//	存贮DM2故障表的EEPROM页号（1 -- ？？），必须根据硬件的具体情况设置
			short eeIndex_u16 ,							//	DM2故障表在EEPROM页中的起始位置（字索引），有效值范围 0 -- 255，但必须保证整个故障表能存贮在EEPROM页中。每个故障表项占用4个字。
			short txBoxDM1_u16 ,						//	发送DM1故障表的数据箱号，此数据箱不能用j1939_initTxDatabox()初始化
			short txBoxDM2_u16							//	发送DM2故障表的数据箱号，此数据箱不能用j1939_initTxDatabox()初始化
	);

	// 从本节点的DM1故障表中删除1个、多个或所有故障
	public int j1939_resetFailureDM1 (
			int spn_u32 ,								//	故障的SPN, 有效值范围 0 -- 524278。指定为J1939_SPN_ALL_DU32 (= 0xFFFFFFFF)是删除所有故障
			byte fmi_u8									//	故障模式，有效值范围 0 -- 31。 指定为J1939_FMI_ALL_DU8 (= 0xFF) 时删除指定SPN的所有模式的故障
	);

	// 从本节点的DM2故障表中删除所有故障
	public int j1939_resetFailureDM2 (
	);

	// 在本节点的DM1故障表中添加1个故障
	public int  j1939_setFailureDM1 (
			int spn_u32 ,								//	故障的SPN
			byte fmi_u8 ,								//	故障模式
			byte lampState_u8 ,							//	故障指示灯状态（每两位对应一种指示灯，00 -- 指示灯灭 01 -- 指示灯亮）
			byte lampFlash_u8							//	指示灯闪烁状态（每两位对应一种指示灯闪烁状态，00 -- 慢闪 01 -- 快闪  11 -- 不闪烁）
	);

	// 检查本节点的故障表中是否存在指定的故障
	public int  j1939_testFailureDM1 (
			int spn_u32 ,								//	被检查故障的SPN
			byte fmi_u8									//	被检查故障的模式
	);

	// 按指定周期被周期性调用的函数，执行J1939协议
	public void j1939_CycAction();

}


//���嵥�����������ṹ
//typedef struct _failureLink_ts {
//	j1939_failure_ts		*pFailure;				// ָ����ϱ���
//	struct _failureLink_ts	*pNext;					// ָ�����е���һ��
//} failureLink_ts;





package J1939;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class J1939 implements IJ1939_API {

	/**
	 *  源节点连接管理数组。最多256个源节点，每个源节点可同时接收来自于多个节点（每个节点最多多只能1个点对点+1个广播）的连接，
	 *  在构造函数中实例化
	 */
	public J1939_RxCM_ts[] 				j1939_RxCM;

	/**
	 * J1939接收数据箱配置, 在J1939任务初始化时根据j1939_Cfg形成
	 */
	public static J1939_RxCfg_ts 		j1939_RxCfg;

	/**
	 * J1939发送数据箱配置,在J1939任务初始化时根据j1939_Cfg形成
	 */
	public static J1939_TxCfg_ts 		j1939_TxCfg;

	/**
	 *  接收数据箱超时双向链表头, 在构造函数中实例化
	 */
	private LinkedList<J1939_rxMsg_ts>	pRxTimeoutHeader;


	/**
	 * DM1故障表项链表的链头
	 */
	LinkedList<J1939_failure_ts>				pFailureDM1;					//

	/**
	 * DM2故障表项链表的链头
	 */
	LinkedList<J1939_failure_ts>				pFailureDM2;					//


	/**
	 * 在接收数据箱数组中查找指定的PGN所在的数据箱号
	 *
	 *@param  canID_u32: 	指定的PGN
	 *@param  canIDMask:	查找屏蔽码，查找时只匹配屏蔽码中为1的位
	 *
	 *@return：
	 *			0  -- 未找到与给定PGN对应的数据箱号
	 *			>0 -- 与给定PGN对应的数据箱号
	 */
	private short GetRxBoxNumberByPGN(int canID_u32, int canIDMask)
	{
		short	iRet=0, i;

		for ( i=0; i< j1939_RxCfg.length_u16; i++ ) {
			if (j1939_RxCfg.start_pas[i] == null ) continue;
			if ( ( ( j1939_RxCfg.start_pas[i].canID_u ^ canID_u32 ) & canIDMask ) == 0 ) {
				iRet = (short)(i+1);												// 数据箱编号从1起
				break;
			}
		}

		return (iRet);
	}

	/**
	 * 在发送数据箱数组中查找指定的PGN所在的数据箱号
	 *
	 *@param  reqPGN: 	指定的PGN
	 *@param  pgnMask:	查找屏蔽码，查找时只匹配屏蔽码中为1的位
	 *
	 *@return：
	 *			0  -- 未找到与给定PGN对应的数据箱号
	 *			>0 -- 与给定PGN对应的数据箱号
	 */
	private short GetTxBoxNumberByPGN(int reqPGN, int pgnMask) {

		J1939_CANID_ts		tx_canID;
		J1939_txMsg_ts		pTxMsg;
		int					iRet=0, i;
		int					txPGN;

		for ( i=0; i<j1939_TxCfg.length_u16; i++ ) {

			pTxMsg = j1939_TxCfg.start_pas[i];
			if ( pTxMsg == null ) continue;

			tx_canID = new J1939_CANID_ts(pTxMsg.canID_u);
			txPGN = tx_canID.PGN();

			if ( ( ( txPGN ^ reqPGN ) & pgnMask ) == 0 ) {
				iRet = (i+1);
			}

			if ( txPGN == reqPGN ) {
				iRet = (i+1);
				break;
			}
		}

		return (short)(iRet);														// 返回匹配或基本匹配的数据箱号。未找到时返回0xFFFF

	}

	/**
	 *  从超时双向链表中摘除指定的接收数据箱
	 *
	 *@param pRxMsg	: 指向接收数据箱
	 */
	private void RemoveRxTimeout(J1939_rxMsg_ts pRxMsg) {
		pRxTimeoutHeader.remove(pRxMsg);
	}


	/**
	 *	 将有接收超时需要的接收数据箱链入超时双向链表
	 *
	 *@param pRxMsg	: 指向接收数据箱
	 */
	private void LinkRxTimeout(J1939_rxMsg_ts pRxMsg) {
		for ( int i=0; i<pRxTimeoutHeader.size(); i++ ) {
			if ( pRxMsg.cm_Timeout_u32 < pRxTimeoutHeader.get(i).cm_Timeout_u32 ) {
				pRxTimeoutHeader.add(i, pRxMsg);
				return;
			}
		}
		pRxTimeoutHeader.add(pRxMsg);
	}

	/**
	 *	 发送方关闭BAM会话
	 *
	 *@param pTxMsg：指向当前正在使用BAM发送的发送数据箱
	 */
	private void TxCloseBAM(J1939_txMsg_ts pTxMsg) {
		j1939_TxCfg.bam_boxNumber_u16 = 0;							//	终止广播（协议规定无需发送ABORT帧）
		j1939_TxCfg.bam_PGN_u32  = 0xFFFFFFFF;						//	置BAM通道状态为空闲
		j1939_TxCfg.bam_txFrameNo_u8 = 0;							//	复位帧顺序号

		pTxMsg.status_u16 = J1939_BOXSTATUS_INIT_DU16;				//	复位发送数据箱状态
		pTxMsg.respDA_u8 = (byte)DA_NONE;							//
	}


	/**
	 * 接收方关闭BAM会话
	 *
	 *@param pRxMsg：		指向当前正在使用BAM发送的发送数据箱
	 *@param status_u16:	接收数据箱最新状态
	 */
	private void RxCloseBAM(J1939_rxMsg_ts pRxMsg, short status_u16)
	{
		J1939_RxCM_ts	pRxCM;

		pRxCM = j1939_RxCM [pRxMsg.canID_u & 0xFF];				// 接收数据箱的源节点的会话结构
		pRxCM.bam_boxNumber_u16  = 0;							// 广播通道空闲
		pRxCM.bam_FrameNo_u8  = 0;								//		接收帧顺序号复位
		pRxCM.bam_Frames_u8 = 0;								//		总接收帧数复位
		pRxCM.bam_rxFrames_u8  = 0;								//		已接收帧数复位

		if ( pRxTimeoutHeader.contains(pRxMsg) == true )  {		// 数据箱在超时链表中
			RemoveRxTimeout(pRxMsg);							//		则从链表中摘除
		}

		pRxMsg.lenRx_u16 = 0;									// 已接收数据字节数复位
		pRxMsg.status_u16 = status_u16;							// 数据箱状态
		pRxMsg.cm_Timeout_u32  = 0xFFFFFFFF;					// 超时时间复位
	}


	/**
	 *  发送方关闭RTS会话
	 *
	 *			当连接管理会话的发送方因超时或接收到ABORT帧时调用
	 *
	 *@param pTxMsg：指向当前正在使用RTS发送的发送数据箱
	 */
	private void TxCloseRTS(J1939_txMsg_ts pTxMsg) 	{

		j1939_TxCfg.rts_boxNumber_u16 = 0;
		j1939_TxCfg.rts_PGN_u32  = 0xFFFFFFFF;
		j1939_TxCfg.cts_txFrameNo_u8  = 0;
		j1939_TxCfg.cts_txFrames_u8 = 0;
		j1939_TxCfg.cts_Frames_u8 = 0;
		j1939_TxCfg.cm_Timeout_u32 = 0xFFFFFFFF;

		pTxMsg.respDA_u8  = (byte)DA_NONE;
		pTxMsg.status_u16 = J1939_BOXSTATUS_INIT_DU16;
	}


	/**
	 *  接收方关闭RTS会话
	 *
	 *@param pRxMsg	: 			被关闭连接的接收数据箱
	 *@param rtsAddr_u8		: 	连接发送方节点地址
	 *@param boxStatus_u16	: 	数据箱当前状态
	 */
	private void RxCloseRTS(J1939_rxMsg_ts pRxMsg, short rtsAddr_u8, short boxStatus_u16)
	{
		J1939_RxCM_ts pRxCM;

		pRxCM = j1939_RxCM[rtsAddr_u8];

		// 复位本节点对节点rtsAddr_u8的RTS接收信息
		pRxCM.rts_boxNumber_u16 = 0;							//
		pRxCM.cts_FrameNo_u8 = 0;								//
		pRxCM.cts_Frames_u8 = 0;								//
		pRxCM.cts_rxFrames_u8 = 0;								//

		if ( pRxTimeoutHeader.contains(pRxMsg) == true )  {		// 数据箱在超时链表中
			RemoveRxTimeout(pRxMsg);							//		则从链表中摘除
		}

		// 复位接收数据箱的接收状态
		pRxMsg.lenRx_u16  = 0;									// 已接收长度置0
		pRxMsg.status_u16 = boxStatus_u16;						// 数据箱状态置为设置值
		pRxMsg.cm_Timeout_u32 = 0xFFFFFFFF;						// 不在超时队列中

	}


	/**
	 *	 通过指定的Slot发送CAN帧
	 *
	 *@param  pCanMsg:		指向要发送的CAN帧
	 *@param  slotIdx_u16:	发送该帧时应使用的Slot编号。
	 *						如为0xFFFF,则从通用的发送Slot集合中选择空闲的Slot
	 *@return :
	 *				0 -- 发送失败
	 *				1 -- CAN帧已写入发送Slot 或发送FIFO缓冲区中
	 */
	private byte SendFrame(can_Message_ts pCanMsg, short slotIdx_u16)
	{
		/*
		short	txUsedSlot_u16 = slotIdx_u16;
		int		i;
		*/

		/*
		System.out.print("Time=" + System.currentTimeMillis()+ ", CanID=" + Integer.toHexString(pCanMsg.id_u32));
		for ( int i=0; i<8; i++)
			System.out.print(" " + Integer.toHexString(( pCanMsg.data_au8[i] & 0xF0 ) >> 4 ) + Integer.toHexString(pCanMsg.data_au8[i] & 0x0F ) ) ;
		*/

		if ( J1939_Context.j1939_CommCfg.can_TxFIFO.size() < CAN_TXFIFO_SIZE ) {
			J1939_Context.j1939_CommCfg.can_TxFIFO.add(pCanMsg);
			// System.out.println(" ");
			return (1);
		}
		else {
			System.out.println("    CAN zhen fa she qu man ！");
			return (0);
		}

		/*
		if ( TxFIFOEmpty() ) {													// 发送FIFO空，试着直接通过Slot发送
			if ( txUsedSlot_u16 == 0xFFFF ) {									// 		未指定发送帧使用的Slot
				for ( i=0; i<TX_MSGOBJECTS; i++) {								//			在通用发送Slot组中找出一个空闲的Slot
					if ( SlotIsFree(MSGOBJ_TX_START_IDX+i) ) {					//
						txUsedSlot_u16 = MSGOBJ_TX_START_IDX+i;					//
						break;													//
					}
				}
			}
			else {																//		指定了发送Slot
				if ( SlotIsFree(txUsedSlot_u16) == 0 ) {						//			Slot不空闲
					txUsedSlot_u16 = 0xFFFF;									//				放入发送FIFO
				}																//
			}
		}
		else {																	// 发送FIFO不空
			txUsedSlot_u16 = 0xFFFF;											//		放入发送队列，以使发送序列化
		}

		if ( txUsedSlot_u16 == 0xFFFF ) {										// 应放入发送FIFO
			return ( WriteFrameToTxFIFO(pCanMsg, slotIdx_u16) );				//		写入发送FIFO
		}																		//
		else {																	// 应通过Slot发送
			return ( SendFrameBySlot(pCanMsg, txUsedSlot_u16) );				//		立即发送
		}
		*/

	}


	/**
	 *	发送传输协议之ABORT(中止点对点长帧传送）
	 *
	 *			发送方和接收方都有可能发出此帧。在下列情况下发送ABORT帧
	 *			1. 发送方：发送RTS或CTS期间的最后一帧后,接收CTS帧超时
	 *			2. 发送方：接收到连接保持帧CTS(0)后，接收CTS帧超时
	 *			3. 接收方：一个CTS期间收到DT帧后，接收下一个DT帧超时
	 *			4. 接收方：送出CTS帧后，接收第一个DT帧超时
	 *			5. 接收方：发出连连保持帧CTS(0)后，发送CTS帧超时
	 *
	 *		参数：
	 *			uint32			cmPGN_u32:		要中止传送消息的PGN
	 *			uint8			destAddr_u8:	ABORT帧的接收方节点地址
	 *			uint8			abReason_u8:	中止原因
	 *
	 *		返回值：
	 *			1 -- ABORT帧已缓存
	 *			0 -- ABORT帧缓存失败
	 */

	int SendAbortFrame(int cmPGN_u32, short destAddr_u8, byte abReason_u8) {

		J1939_CANID_ts		canID;
		can_Message_ts		canMsg;

		canID = new J1939_CANID_ts(0);

		canID.setPF((byte)PF_CM);
		canID.setDP((byte)0x18);													// ABORT帧优先级
		canID.setPS((byte)destAddr_u8);												// DA-SPECIFIC
		canID.setSA((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8));				// 本节点地址

		canMsg = new can_Message_ts();

		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;
		canMsg.id_u32 = canID.canID_u32;

		canMsg.data_au8[0] = (byte)CM_ABORT;										// 控制字节:ABORT
		canMsg.data_au8[1] = abReason_u8 ;											// 中止原因
		canMsg.data_au8[2] = (byte)0xFF ;											//
		canMsg.data_au8[3] = (byte)0xFF ;											//
		canMsg.data_au8[4] = (byte)0xFF ;											//

		// 将要用传输协议发送的PGN
		canMsg.data_au8[5] = (byte)(cmPGN_u32)  ;									// PGN.PS
		canMsg.data_au8[6] = (byte)(cmPGN_u32 >> 8);								// PGN.PF
		canMsg.data_au8[7] = (byte)(cmPGN_u32 >> 16);								// PGN.DP

		return ( SendFrame(canMsg, (short)0xFFFF) );								// 通过 RTS通道发送ABORT帧

	}

	/**
	 *	 发送传输协议之CTS(准备接收长帧），收到RTS后应答RTS帧的发送节点
	 *
	 *			向CAN发送FIFO缓冲区写入CTS
	 *
	 *@param rxPGN_u32:			将要使用传输协议接收的PGN
	 *@param destAddr_u8		发送此PGN的源节点地址，CTS帧将发送给此节点
	 *@param rxFrames_u8:		接收帧数
	 *@param nextFrameNo_u8：	下一个接收帧的帧顺序号
	 *
	 *@return :
	 *			1 -- RTS帧已缓存
	 *			0 -- RTS帧缓存失败
	 */
	private byte SendCTSFrame(int rxPGN_u32, short destAddr_u8, short rxFrames_u8, short nextFrameNo_u8)
	{
		J1939_CANID_ts		canID;
		can_Message_ts		canMsg;

		canID = new J1939_CANID_ts(0);
		canID.setPF((byte)PF_CM);
		canID.setDP((byte)0x18);													// 优先级6
		canID.setPS((byte)destAddr_u8);												// 接收CTS帧的目的节点地址
		canID.setSA((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8));				// 本节点源地址

		canMsg = new can_Message_ts();
		canMsg.id_u32 = canID.canID_u32;
		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;

		canMsg.data_au8[0] = CM_CTS;
		canMsg.data_au8[1] = (byte)rxFrames_u8 ;									// 一个CTS期间可接收的帧数
		canMsg.data_au8[2] = (byte)nextFrameNo_u8;									// 待接收下一帧的帧顺序号
		canMsg.data_au8[3] = (byte)0xFF;											// 保留字节
		canMsg.data_au8[4] = (byte)0xFF;											// 保留字节

		// 将要用传输协议发送的PGN
		canMsg.data_au8[5] = (byte)(rxPGN_u32);										// PGN.PS
		canMsg.data_au8[6] = (byte)(rxPGN_u32 >> 8 );								// PGN.PF
		canMsg.data_au8[7] = (byte)(rxPGN_u32 >> 16 );								// PGN.DP

		return ( SendFrame(canMsg, (short)0xFFFF) );								// 通过 RTS通道发送CTS帧

	}


	/**
	 *	 发送传输协议之EndOfMsgAck(长帧接收完成应答）
	 *
	 *			点对点长帧接收完成后调用此函数发送EOM帧
	 *
	 *@param cmPGN_u32:		已完成接收的PGN号
	 *@param destAddr_u8:	EOM帧的接收节点地址
	 *@param rxLen_u16:		已接收数据长度
	 *@param rxFrames_u8:	已接收DT帧帧数
	 *
	 *@return
	 *			1 -- ABORT帧已缓存
	 *			0 -- ABORT帧缓存失败
	 */
	private int SendEOMFrame(int cmPGN_u32, short rxLen_u16, short rxFrames_u8, short destAddr_u8)
	{
		J1939_CANID_ts		canID;
		can_Message_ts		canMsg;

		canID = new J1939_CANID_ts(0);

		canID.setPF((byte)PF_CM);
		canID.setDP((byte)0x18);													// EOM帧优先级
		canID.setPS((byte)destAddr_u8);												// DA-SPECIFIC
		canID.setSA((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8));				// 本节点地址

		canMsg = new can_Message_ts();

		canMsg.id_u32 = canID.canID_u32;
		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;

		canMsg.data_au8[0] = CM_EOMACK;												// 控制字节:EndOfMsgAck
		canMsg.data_au8[1] = (byte)rxLen_u16 ;										// 已接收字节数
		canMsg.data_au8[2] = (byte)(rxLen_u16 >> 8) ;								//
		canMsg.data_au8[3] = (byte)rxFrames_u8;										// 已接收帧数
		canMsg.data_au8[4] = (byte)0xFF ;											// 保留

		// 已完成接收消息的PGN
		canMsg.data_au8[5] = (byte)(cmPGN_u32 ) ;									// PGN.PS
		canMsg.data_au8[6] = (byte)(cmPGN_u32 >> 8 );								// PGN.PF
		canMsg.data_au8[7] = (byte)(cmPGN_u32 >> 16);								// PGN.DP

		return ( SendFrame(canMsg, (short)0xFFFF) );								// 通过 RTS通道发送EOM帧

	}

	/**
	 *	 发送传输协议之RTS(请求发送长帧），发送PDU1格式的长数据前发送
	 *
	 *			向CAN发送FIFO缓冲区写入RTS
	 *
	 *@param pTxMsg:				指向发送数据箱
	 *@param destAddr_u8:			特定目标节点地址
	 *@param txPGN_u32:				将要使用传输协议传输的PGN
	 *
	 *@return:
	 *			1 -- RTS帧已缓存
	 *			0 -- RTS帧缓存失败
	 */
	private byte SendRTSFrame(J1939_txMsg_ts pTxMsg, short destAddr_u8, int txPGN_u32 )
	{
		J1939_CANID_ts	canID;
		can_Message_ts	canMsg;

		canID = new J1939_CANID_ts(0);

		canID.setPF((byte)PF_CM);
		canID.setDP((byte)(pTxMsg.canID_u >> 24) );							// 发送数据箱 的 优先级
		canID.setPS((byte)destAddr_u8);										// 发送目的地址
		canID.setSA((byte)(pTxMsg.canID_u));								// 发送数据箱 的 源地址

		canMsg = new can_Message_ts();

		canMsg.id_u32 = canID.canID_u32;
		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;

		canMsg.data_au8[0] = CM_RTS;
		canMsg.data_au8[1] = (byte)(pTxMsg.lenAct_u16) ;					//	发送总长度
		canMsg.data_au8[2] = (byte)(pTxMsg.lenAct_u16 >> 8) ;				//
		canMsg.data_au8[3] = (byte)(( pTxMsg.lenAct_u16 + 6 ) / 7);			//	发送总帧数
		canMsg.data_au8[4] = canMsg.data_au8[3];							//	1次CTS期间可发送的帧数上限=全部帧

		// 将要用传输协议发送的PGN
		canMsg.data_au8[5] = (byte)(txPGN_u32);								// PGN.PS
		canMsg.data_au8[6] = (byte)(txPGN_u32 >> 8);						// PGN.PF
		canMsg.data_au8[7] = (byte)(txPGN_u32 >> 16);						// PGN.DP

		return ( SendFrame(canMsg, (short)0xFFFF) );						// 通过 RTS通道发送RTS帧

	}

	/**
	 *	 发送传输协议之BAM(请求广播长帧）
	 *
	 *			向CAN发送FIFO缓冲区写入BAM
	 *
	 *@param pTxMsg:				指向发送数据箱
	 *@param txPGN_u32:				要广播发送的PGN
	 *
	 *@return:
	 *			1 -- BAM帧已缓存
	 *			0 -- BAM帧缓存失败
	 */
	private byte SendBAMFrame(J1939_txMsg_ts pTxMsg, int txPGN_u32)
	{
		J1939_CANID_ts	canID;
		can_Message_ts	canMsg;

		canID = new J1939_CANID_ts(0);

		canID.setPF((byte)PF_CM);
		canID.setDP((byte)(pTxMsg.canID_u>>24));							// 发送数据箱 的 优先级
		canID.setPS((byte)DA_GLOBAL);										// 发送目的地址
		canID.setSA((byte)(pTxMsg.canID_u));								// 发送数据箱 的 源地址

		canMsg = new can_Message_ts();

		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;
		canMsg.id_u32 = canID.canID_u32;

		canMsg.data_au8[0] = CM_BAM;												// 控制字节
		canMsg.data_au8[1] = (byte)(pTxMsg.lenAct_u16) ;							// 广播数据总长度
		canMsg.data_au8[2] = (byte)(pTxMsg.lenAct_u16 >> 8) ;						//
		canMsg.data_au8[3] = (byte)((pTxMsg.lenAct_u16 + 6 ) / 7);					// 广播帧数
		canMsg.data_au8[4] = (byte)0xFF;											// 保留字节

		// 将要用传输协议发送的PGN
		canMsg.data_au8[5] = (byte)(txPGN_u32);										// PGN.PS
		canMsg.data_au8[6] = (byte)(txPGN_u32 >> 8);								// PGN.PF
		canMsg.data_au8[7] = (byte)(txPGN_u32 >> 16);								// PGN.DP

		return ( SendFrame(canMsg, (short)0xFFFF) );								// 通过 BAM通道发送BAM帧

	}

	/**
	 *	 传输协议，发送多帧数据(TP.DT消息）
	 *
	 *			向CAN发送FIFO缓冲区连续写入指定发送数据箱的多个TP.DT直到FIFO满或该数据箱的TP.DT帧已全部发送完成
	 *
	 *@param	j1939_txMsg_ts *pTxMsg:	指向发送数据箱
	 *@param	uint8 frameNo_u8		:	数据帧顺序号
	 *@param	uint8 frameNums_u8		:	最多发送帧数
	 *@param	uint16 slotIdx_u16		:	发送数据帧时使用的Slot编号
	 *
	 *@return:
	 *			发送的帧数
	 */
	private int SendDTFrames(J1939_txMsg_ts pTxMsg, short frameNo_u8, short frameNums_u8, short slotIdx_u16)
	{
		J1939_CANID_ts	canID;
		can_Message_ts	canMsg;
		int				i, iDtLen;
		int				iRet = 0;

		canID = new J1939_CANID_ts(pTxMsg.canID_u);

		canID.setPF((byte)(PF_DT));																//	DT帧
		canID.setPS((byte)(pTxMsg.respDA_u8));													//	接收节点地址（特定或全局）
		//		请求本节点的PGN则为特定
		//		请求全局的PGN则为全局
		//		发送PDU2格式的PGN为全局
		//		发送未指定目标节点的PDU1格式的PGN则为全局
		//		发送指定目标节点的PDU1格式的PGN则为特定
		for ( i=frameNo_u8; i<(frameNo_u8+frameNums_u8 ); i++ ){

			iDtLen = (int)(pTxMsg.lenAct_u16) - (i-1)*7;
			if ( iDtLen > 7 ) iDtLen = 7;
			if ( iDtLen <= 0 ) break;

			canMsg = new can_Message_ts();

			canMsg.id_u32 = canID.canID_u32;													// 帧ID
			canMsg.format_u8 = CAN_EXD;															// 扩展帧
			canMsg.numBytes_u8 = (byte)(iDtLen+1);														// 帧长度

			canMsg.data_au8[0] = (byte)i;																// 帧序号
			//memcpy(canMsg.data_au8+1, pTxMsg->data_pau8 + (i-1)*7, iDtLen);						// 最多7字节帧数据
			System.arraycopy(pTxMsg.data_pau8, (i-1)*7,
					canMsg.data_au8, 1,
					iDtLen);
			if ( SendFrame(canMsg, (short)0xFFFF/*slotIdx_u16*/) == 0 ) {										// Slot忙且发送FIFO缓冲区满
				break;																			// 停止发送，余下帧等待下个J1939任务周期再试
			}
			else {
				iRet++;																			// 已发送帧数
			}
		}

		return ( iRet);

	}


	/**
	 *
	 *	发送广播DT帧（最多一帧）
	 *
	 *			根据长帧广播通道的状态发送广播数据帧并修改广播通道状态
	 *
	 *@param pTxMsg:	指向被广播的发送数据箱
	 *
	 */
	private void BAM_SendDTFrame(J1939_txMsg_ts pTxMsg)
	{
		int	txFrames;

		if ( System.currentTimeMillis() < j1939_TxCfg.bam_minTimeout_u32 ) return;					//	BAM帧间隔太小，还未到广播下一帧的时间
		if ( System.currentTimeMillis() > j1939_TxCfg.bam_Timeout_u32 ) {							//	BAM帧间隔超时,
			TxCloseBAM(pTxMsg);														//	传输发起方关闭BAM
			return;																	//
		}

		// 广播1个DT帧
		txFrames = SendDTFrames(													//
				pTxMsg,														//	指向广播长帧消息数据所在的数据箱
				(short)(j1939_TxCfg.bam_txFrameNo_u8 & 0xFF),				//	广播帧顺序号
				(short)1,													//  连续发送帧数为1
				(short)0xFFFF/* MSGOBJ_BAMTX_IDX*/											//  通过BAM通道发送
		);

		if ( txFrames > 0 ) {															// 成功发送或缓存1个广播DT帧
			j1939_TxCfg.bam_txFrameNo_u8 += txFrames;								//		修改广播发送通道状态之下一帧帧号
			j1939_TxCfg.bam_minTimeout_u32 = System.currentTimeMillis() + BAM_TXDT_TO_TXDT_MIN;		//		修改广播下一帧的时间窗口：不能早于此时刻
			j1939_TxCfg.bam_Timeout_u32 = System.currentTimeMillis() + BAM_TXDT_TO_TXDT_MAX;			//								   不能晚于此时刻
			if ( (short)((j1939_TxCfg.bam_txFrameNo_u8 - 1) * 7) >= pTxMsg.lenAct_u16 ) {	//		所有帧已广播，结束BAM，标记BAM通道空闲
				TxCloseBAM(pTxMsg);													//			传输发起方关闭BAM
			}
		}
	}


	/**
	 *	发送点对点DT帧（多帧）
	 *
	 *			根据长帧点对点通道的状态发送点对点数据帧并修改点对点通道状态
	 *
	 *@param pTxMsg:	指向被广播的发送数据箱
	 *
	 *@return:
	 *
	 */
	private void RTS_SendDTFrames(J1939_txMsg_ts pTxMsg)
	{
		int	txFrames;

		txFrames = SendDTFrames(													//	尽可能多地发送或缓存RTS_DT帧
				pTxMsg,																//		指向发送消息所在数据箱
				(short)(j1939_TxCfg.cts_txFrameNo_u8&0xFF),							//		帧顺序号
				(short)(j1939_TxCfg.cts_Frames_u8 -	j1939_TxCfg.cts_txFrames_u8),	//		允许连续发送的最大帧数
				(short)0xFFFF/*MSGOBJ_CMTX_IDX*/														//		发送帧时应使用的Slot编号
		);

		if ( txFrames > 0  ) {															//  至少发送或缓存了一帧数据
			j1939_TxCfg.cts_txFrameNo_u8 += txFrames;								//		修改下次继续发送的起始帧号
			j1939_TxCfg.cts_txFrames_u8 += txFrames;								//		修改已发送帧数
			if ( (j1939_TxCfg.cts_txFrameNo_u8 - 1) * 7 >= (pTxMsg.lenAct_u16) ) {	// 所有DT帧已发送，等待EndOfMessage帧
				j1939_TxCfg.cm_Timeout_u32 = System.currentTimeMillis() + TIMEOUT_TXDT_TO_RXEOM;		//		收到EndOfMsgAck帧的最后时间限
				pTxMsg.status_u16 = J1939_BOXSTATUS_WAIT_EOM_DU16;					//		进入等待接收EndOfMsgAck状态
			}																		//
			else {																	// 还有DT帧未发送
				if ( j1939_TxCfg.cts_Frames_u8 == j1939_TxCfg.cts_txFrames_u8 ) {	//		CTS期间允许连续发送的帧已全部发送，等待下一个CTS帧
					j1939_TxCfg.cm_Timeout_u32 = System.currentTimeMillis() + TIMEOUT_TXDT_TO_RXCTS;	//		接收下个CTS帧的最后时间限
					pTxMsg.status_u16 = J1939_BOXSTATUS_CTS0_DU16;					//		进入等待接收CTS状态
				}																	//
				else {																//		CTS期间允许的帧数还未全部发送（由于发送缓冲区满）
				}																	//		等待下个任务周期继续发送
			}
		}
	}


	/**
	 *	 对请求PGN的否定应答
	 *
	 *@param  ReqPGN_u32: 被请求的PGN
	 *@param  ctrlByte_u8: 控制字节
	 *
	 *@return:
	 *		1 --  发送成功
	 *		0 --  发送失败
	 */
	int AckForRequest(int ReqPGN_u32, byte	ctrlByte_u8)
	{
		J1939_CANID_ts		canID;
		can_Message_ts		canMsg;

		canID= new J1939_CANID_ts(0);

		canID.setSA((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8));				//	本节点地址
		canID.setPF((byte)PF_REQACK);										//	请求应答帧的PGN
		canID.setPS((byte)DA_GLOBAL);										//	全局发送
		canID.setDP((byte)0x18);											//	CAN优先级6

		canMsg = new can_Message_ts();

		canMsg.id_u32 = canID.canID_u32;
		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8 = 8;

		canMsg.data_au8[0] = ctrlByte_u8 ;									//	NACK
		canMsg.data_au8[1] = 0x00;											//	组功能值
		canMsg.data_au8[2] = (byte)0xFF;									//	保留字节
		canMsg.data_au8[3] = (byte)0xFF;									//	保留字节
		canMsg.data_au8[4] = (byte)0xFF;									//	保留字节
		canMsg.data_au8[5] = (byte)(ReqPGN_u32);							//	被请求PGN按小端格式装配到NACK帧中
		canMsg.data_au8[6] = (byte)(ReqPGN_u32 >> 8);						//
		canMsg.data_au8[7] = (byte)(ReqPGN_u32 >> 16);						//

		return ( SendFrame(canMsg, (short)0xFFFF) );						// 通过通用Slot发送请求应答帧

	}


	/**
	 *	发出PGN请求帧的节点处理对PGN请求的应答帧
	 *
	 *@param pRxCanMsg:	指向接收到的PGN请求应答帧
	 *
	 */
	private void RxFrameProc_REQACK(can_Message_ts pRxCanMsg ) 	{

		J1939_rxMsg_ts		pRxMsg;
		J1939_CANID_ts		canID;
		J1939_CANID_ts		canID_req;
		int					reqPGN;
		short				boxNumber;

		canID = new J1939_CANID_ts(pRxCanMsg.id_u32);					// 应答帧的CAN-ID

		canID_req = new J1939_CANID_ts(0);

		canID_req.setDP(pRxCanMsg.data_au8[7]);							//		之前请求的PGN;
		canID_req.setPF(pRxCanMsg.data_au8[6]);							//
		canID_req.setPS(pRxCanMsg.data_au8[5]);							//

		canID_req.setSA(canID.SA());
		reqPGN = canID_req.PGN();										//

		boxNumber = GetRxBoxNumberByPGN(								// 是否有能接收所请求PGN的接收数据箱？
				canID_req.canID_u32,					//		精确匹配 PGN+SA
				0xFFFFFF								//
		);

		if ( boxNumber == 0 ) return;									// 无，忽略此应答帧

		pRxMsg = j1939_RxCfg.start_pas[boxNumber-1];					//	有，处理该应答帧

		switch ( pRxCanMsg.data_au8[0] ) {								//	根据应答帧中的控制字节处理
			case ACK:													//		肯定应答：对方支持被请求的PGN，即将应答。
				break;													//
			case NACK:													//		否定应答：对方不支持被请求的PGN
				break;													//
			case DENY:													//		拒绝应答：对方因安全等原因拒绝应答PGN请求
				break;													//
			case NRESP:													//		忙应答：对方因忙于其他处理而不能应答
				break;													//
			default:													//		其他应答：
				break;													//
		}

	}

	/**
	 *	处理接收到的连接管理帧
	 *
	 *@param pRxCanMsg:		指向接收到的DT帧
	 */
	private void RxFrameProc_CM(can_Message_ts pRxCanMsg)
	{
		J1939_CANID_ts		cm_canID;		//
		J1939_CANID_ts		canID;			// 连接管理帧CAN ID
		int					cmPGN;			// 被连接管理的PGN
		short				srcAddr;
		short				boxNumber;

		J1939_txMsg_ts	pTxMsg;
		J1939_rxMsg_ts	pRxMsg;
		J1939_RxCM_ts	pRxCM;

		canID = new J1939_CANID_ts(pRxCanMsg.id_u32);									// 连接管理帧CAN_ID

		cm_canID = new J1939_CANID_ts(0);
		cm_canID.setDP(pRxCanMsg.data_au8[7]);											//	被连接管理的PGN
		cm_canID.setPF(pRxCanMsg.data_au8[6]);											//
		cm_canID.setPS(pRxCanMsg.data_au8[5]);											//

		srcAddr = canID.SA();															//		源节点地址
		cmPGN   = cm_canID.PGN();														//		被连接管理的PGN值

		switch ( pRxCanMsg.data_au8[0] ) {												//

			// 只接收方
			case CM_RTS:																//	收到“长帧传送请求”，需判定可否接收请求传送的J1939消息

				boxNumber = j1939_RxCM[srcAddr].rts_boxNumber_u16;						//
				if ( boxNumber > 0  ) {													//	之前该源节点正在向本节点传送长帧
					pRxMsg = j1939_RxCfg.start_pas[boxNumber-1];						//		J1939协议规定在同一时刻从同一节点只能接收一帧点对点长消息
					if ( (pRxMsg.canID_u >> 8 ) != cmPGN ) {							//
						pRxMsg.status_u16 = J1939_BOXSTATUS_INV_ABORT_DU16;				//		还未完成接收的长帧接收数据箱状态置为中止（其中的数据无效）
						boxNumber = 0;
					}																	//
					else {																//		相同的PGN, 前一次点对点传送还未结束，又收到RTS
					}																	//		则接收数据箱不变
				}
				if ( boxNumber == 0 ) {
					boxNumber = GetRxBoxNumberByPGN((cmPGN<<8)|srcAddr, 0xFFFFFF);		//  接收被请求传送长帧的数据箱
				}
				if ( boxNumber == 0 ) {													//		无数据箱，不能接收，通知发送方终止传送
					SendAbortFrame(cmPGN, srcAddr, (byte)ABORT_REASON_NORES);			//			ABORT原因：无接收数据箱
				}																		//
				else {																	//	接收数据箱中有被请求传送的长帧PGN
					if ( SendCTSFrame(cmPGN,srcAddr,pRxCanMsg.data_au8[4],(short)1) > 0 ){		//		发送CTS帧

						pRxCM = j1939_RxCM[srcAddr];									//
						pRxCM.rts_boxNumber_u16 = boxNumber;							//		本节点从源节点接收长帧的的数据箱号
						pRxCM.cts_Frames_u8 = pRxCanMsg.data_au8[4];					//		一个CTS期间能接收的数据帧数
						pRxCM.cts_rxFrames_u8 = 0;										//		一个CTS期间已接收帧数复位
						pRxCM.cts_FrameNo_u8 = 1;										//		待接收帧顺序号

						pRxMsg = j1939_RxCfg.start_pas[boxNumber-1];					//
						pRxMsg.lenAct_u16 =(short)((pRxCanMsg.data_au8[2]<<8) +			//		应接收数据字节数
								pRxCanMsg.data_au8[1]);					//
						pRxMsg.lenRx_u16 = 0;											//		已接收数据字节数
						pRxMsg.status_u16 = J1939_BOXSTATUS_RTS_DU16;					//		接收数据箱状态：已收到RTS

						pRxMsg.cm_Timeout_u32 = System.currentTimeMillis()+TIMEOUT_TXCTS_TO_RXFIRSTDT;	//		接收第一个DT帧的超时时刻
						LinkRxTimeout(pRxMsg);											//		将此数据箱放入超时单向链表
					}
				}
				break;

			// 只发送方
			case CM_CTS:									// 收到“准备接收长帧”应答，置消息状态为“正在发送长帧数据”

				if ( j1939_TxCfg.rts_PGN_u32 == cmPGN ) {								// CTS帧中的PGN与发送RTS帧时的PGN相同
					pTxMsg = j1939_TxCfg.start_pas[j1939_TxCfg.rts_boxNumber_u16-1];	//		指向发送数据箱
					if ( pRxCanMsg.data_au8[1] == 0 ) {									//		接收方请求连接保持帧(CTS0)
						pTxMsg.status_u16 = J1939_BOXSTATUS_CTS0_DU16;					//			发送数据箱状态: 连接保持
						j1939_TxCfg.cm_Timeout_u32 =  System.currentTimeMillis()+TIMEOUT_RXCTS0_TO_RXCTS;	//			保持连接的时间窗口
					}																	//
					else {																//		或接收方已准备好接收指示帧（CTS)
						j1939_TxCfg.cts_Frames_u8 = pRxCanMsg.data_au8[1];				//			一个CTS可发送的帧数
						j1939_TxCfg.cts_txFrames_u8 = 0;								//			已发送帧数
						j1939_TxCfg.cts_txFrameNo_u8 = pRxCanMsg.data_au8[2];			//			发送帧顺序号
						pTxMsg.status_u16 = J1939_BOXSTATUS_DT_TX_DU16;					//			发送数据箱状态: 连接已建立，准备发送数据帧
					}																	//
				}																		//
				else {																	//	非预期的PGN,简单忽略
				}
				break;

			// 只接收方
			case CM_BAM:									// 收到“准备发送长帧广播”，
				boxNumber = j1939_RxCM[srcAddr].bam_boxNumber_u16;						//
				if ( boxNumber != 0  ) {												//	之前该源节点正在向本节点广播长帧
					pRxMsg = j1939_RxCfg.start_pas[boxNumber - 1];						//		J1939协议规定在同一时刻从同一节点只能接收一帧点对点长消息
					if ( (pRxMsg.canID_u >> 8 ) != cmPGN ) {							//
						if ( j1939_RxCM[srcAddr].bam_FrameNo_u8 > 1 ) {					//
							pRxMsg.status_u16 = J1939_BOXSTATUS_INV_ABORT_DU16;		//		已接收了一些广播帧，中止接收（其中的数据无效）
						}
						boxNumber = 0;													//
					}																	//
					else {																//		相同的PGN, 前一次点对点传送还未结束，又收到BAM
					}																	//		则接收数据箱不变
				}																		//
				if ( boxNumber == 0 ) {													//
					boxNumber = GetRxBoxNumberByPGN((cmPGN<<8)|srcAddr, 0xFFFFFF);		//  得到接收被广播长帧的数据箱
				}
				if ( boxNumber != 0 ) {													//	有数据箱
					pRxCM = j1939_RxCM[srcAddr];
					pRxCM.bam_boxNumber_u16 = boxNumber;								//		本节点从源节点接收广播长帧的的数据箱号
					pRxCM.bam_Frames_u8  = pRxCanMsg.data_au8[3];						//		广播总帧数
					pRxCM.bam_FrameNo_u8 = 1;											//		广播接收帧的帧顺序号
					pRxCM.bam_rxFrames_u8 = 0;											//		已接收广播数据帧数量								//		待接收帧顺序号

					pRxMsg = j1939_RxCfg.start_pas[boxNumber - 1];						//
					pRxMsg.lenAct_u16 = (short)((pRxCanMsg.data_au8[2]<<8) +			//		应接收数据字节数
							pRxCanMsg.data_au8[1]);						//
					pRxMsg.lenRx_u16 = 0;												//		已接收数据字节数
					pRxMsg.status_u16 = J1939_BOXSTATUS_BAM_RX_DU16;					//		接收数据箱状态：正在接收广播数据帧
					pRxMsg.cm_Timeout_u32 = System.currentTimeMillis() + TIMEOUT_RXBAM_TO_RXDT;			//		收到BAM帧后必须在此时间里收到第一个DT帧
					LinkRxTimeout(pRxMsg);												//		将此数据箱放入超时双向链表
				}																		//
				else {																	// 没有数据箱，简单忽略
				}
				break;

			// 接收方、发送方
			case (byte)CM_ABORT:									// 收到“中止长帧传送”，中止

				if ( ( j1939_TxCfg.rts_PGN_u32 == cmPGN)  && ( srcAddr != 0 ) ) {					// 发送节点收到了ABORT帧
					pTxMsg = j1939_TxCfg.start_pas[j1939_TxCfg.rts_boxNumber_u16-1];	//		指向发送数据箱
					TxCloseRTS(pTxMsg);													//		发送方关闭RTS连接
				}																		//
				else {																	// 接收节点收到了ABORT帧														//
					boxNumber = j1939_RxCM[srcAddr].rts_boxNumber_u16 ;					//
					if ( boxNumber != 0 ) {												//		之前该源节点正在向本节点传送长帧
						pRxMsg = j1939_RxCfg.start_pas[boxNumber-1];					//		J1939协议规定在同一时刻从同一节点只能接收一帧点对点长消息
						if ( (pRxMsg.canID_u >> 8) == cmPGN ) {							//		为接收被管理PGN的数据箱
							RxCloseRTS(pRxMsg, srcAddr, (short)J1939_BOXSTATUS_INV_ABORT_DU16);//			接收方中止接收并关闭RTS连接
						}																//
						else {															//		非预期的ABORT帧，简单忽略
						}																//
					}
				}
				break;

			// 只发送方
			case CM_EOMACK:
				if ( j1939_TxCfg.rts_PGN_u32 == cmPGN ) {
					pTxMsg = j1939_TxCfg.start_pas[j1939_TxCfg.rts_boxNumber_u16-1];	// 指向发送数据箱
					TxCloseRTS(pTxMsg);													// 发送方关闭RTS连接
				}
				break;
		}
	}


	/*
	 *	 将接收数据箱中接收到的完整的DM1或DM2消息解析还原为DM1或DM2故障项
	 *
	 *@param dtcList_pts: 	指向存放解析结果的故障表
	 *@param dmData_pu8： 	指向接收到的DM消息数据,
	 *@param dmDatalen_u16：DM消息数据长度
	 *
	 *@return:
	 *			DM消息数据中的故障项数
	 */
	short DM_ParseDBoxData(Object dtcList_pts, byte[] dmData_pu8, short dmDatalen_u16)
	{
		J1939_dtcList_ts	pDTCList;
		short				wDtcNums = 0;
		int					iLeftBytes;
		short				pDataIdx;

		pDTCList = (J1939_dtcList_ts)dtcList_pts;

		if ( dtcList_pts != null  &&
				dmData_pu8 != null  &&
				dmDatalen_u16 > 0  ) {

			pDTCList.lamp_s.lampState_u8 = dmData_pu8[0];			// 指示灯状态
			pDTCList.lamp_s.lampFlash_u8 = dmData_pu8[1];			//
			pDataIdx = 2;
			iLeftBytes = dmDatalen_u16 - 2;

			if ( dmDatalen_u16 != 8 ) {

				while ( ( wDtcNums < 20 ) && ( iLeftBytes > 3 ) ) {
					pDTCList.dtc_as[wDtcNums].spn_u32 = ( ( dmData_pu8[pDataIdx+2] & 0xE0 ) << 11 ) |
							( ( dmData_pu8[pDataIdx+1] & 0xFF) << 8 ) |
							( dmData_pu8[pDataIdx] & 0xFF);
					pDTCList.dtc_as[wDtcNums].fmi_u8 = (byte)(dmData_pu8[pDataIdx+2] & 0x1F);
					pDTCList.dtc_as[wDtcNums].occ_u8 = dmData_pu8[pDataIdx+3];

					wDtcNums++;
					pDataIdx += 4;
					iLeftBytes -= 4;
				}
			}

		}

		return ( wDtcNums );
	}

	/**
	 *
	 *	根据DM1故障链生成DM1发送数据箱的发送数据和数据长度
	 *
	 *			DM1和DM2发送消息数据组成如下： a, b, c, d [, b, c, d]
	 *			其中： a = 指示灯状态，2个字节（1字节指示灯开关状态+1字节指示灯闪烁状态）
	 *				   b = 故障SPN(19位）
	 *				   c = 故障FMI(5位）
	 *				   d = 转换方法指示位（1位）和 故障发生次数（7位）
	 *
	 *					b+c占用3字节，d占用1字节
	 *@param pTxMsg: 指向发送数据箱
	 *@param pDM:	 DM1或DM2故障链表
	 *
	 */
	private void DM_UpdateDBoxData(J1939_txMsg_ts pTxMsg, LinkedList<J1939_failure_ts> pDM) {

		//failureLink_ts *pThis, *pNext;

		short			wDataLen=0;
		byte[]			pData;
		int				dwSPN;

		pData = pTxMsg.data_pau8;

		if ( pDM.size() > 0  ) {										// DM1链非空(有DM1故障项）

			wDataLen = 2;												//

			if ( pFailureDM1.size() > 0  ) {
				pData[0] = pFailureDM1.getLast().lamp_s.lampState_u8;	// DM链头所指故障为最后发生的故障，其状态作为DM1和DM2消息中的状态
				pData[1] = pFailureDM1.getLast().lamp_s.lampFlash_u8;	//
			}
			else {														// 无DM1故障项时
				pData[0] = 0;											//		指示灯状态置默认值
				pData[1] = (byte)0xFF;									//
			}

			for ( J1939_failure_ts pThis: pDM ) {						// 每个DM1故障项在DM1消息中占4字节
				dwSPN = pThis.dtc_s.spn_u32;							//
				pData[wDataLen+0] = (byte)(dwSPN &0xFF);				//
				pData[wDataLen+1] = (byte)((dwSPN >> 8 )&0xFF);			//
				pData[wDataLen+2] = (byte)(((dwSPN & 0x70000) >> 11) | pThis.dtc_s.fmi_u8);						//
				pData[wDataLen+3] = pThis.dtc_s.occ_u8;					//
				wDataLen += 4;											//
			}
		}

		if ( wDataLen == 0 ) {											// 当前无DM1故障项，DM1消息固定为8字节长
			pData[0] = 0;												//
			pData[1] = (byte)0xFF;										//
			pData[2] = 0;												//
			pData[3] = 0;												//
			pData[4] = 0;												//
			pData[5] = 0;												//
			pData[6] = (byte)0xFF;										//
			pData[7] = (byte)0xFF;										//
			wDataLen = 8;												//
		}

		pTxMsg.lenAct_u16 = wDataLen;									// 要发送的DM1消息长度

	}


	/**
	 *
	 *	 处理接收到的广播DT帧
	 *
	 *		参数:
	 *			can_Message_ts	*pRxCanMsg:	指向接收到的DT帧
	 */
	private void RxFrameProc_BAMDT(can_Message_ts pRxCanMsg ) {

		J1939_RxCM_ts	pRxCM;
		J1939_rxMsg_ts	pRxMsg;
		short			dataLen;
		short			srcAddr_u8;
		int			dwValidPGN;

		srcAddr_u8 = (short)(pRxCanMsg.id_u32 & 0xFF);							// DT帧源节点地址
		pRxCM = j1939_RxCM[srcAddr_u8];											// 指向DT帧源节点管理结构
		if ( pRxCM.bam_boxNumber_u16 == 0 ) {
			return;
		}

		pRxMsg = j1939_RxCfg.start_pas[pRxCM.bam_boxNumber_u16 - 1];			// 广播源的接收数据箱
		if ( pRxMsg.status_u16 != J1939_BOXSTATUS_BAM_RX_DU16 ){
			// 数据箱未处于接收广播长帧状态，忽略DT帧。出现这种情况是因为接收超时而中止了接收后，
			// 发送方继续广播数据帧
			return;
		}

		// 接收数据箱当前正在接收广播长帧状态
		if ( (pRxCanMsg.data_au8[0] & 0xFF) != pRxCM.bam_FrameNo_u8 ) {			// 帧顺序号错，
			RxCloseBAM(pRxMsg, (short)J1939_BOXSTATUS_INV_SEQNBR_DU16);			//		中止BAM会话,并标记帧顺序错
		}
		else {																	// DT帧顺序号正确
			dataLen = (short)((pRxCanMsg.numBytes_u8 & 0xFF ) - 1);				//
			if ( ( pRxMsg.lenAct_u16 - pRxMsg.lenRx_u16 ) < dataLen ) {			//		帧数据长度大于 （应接收长度 - 已接收长度）, 则帧中有填充字节
				dataLen = (short)(pRxMsg.lenAct_u16 - pRxMsg.lenRx_u16);		//			只考虑帧中的有效数据
			}

			if ( (pRxMsg.lenRx_u16 + dataLen) > pRxMsg.lenMax_u16 ) {			//		长度溢出
				RxCloseBAM(pRxMsg, (short)J1939_BOXSTATUS_INV_OVFL_DU16);		//			中止BAM会话，并标记数据溢出
			}
			else {																//		长度未溢出
				System.arraycopy(pRxCanMsg.data_au8, 1,
						pRxMsg.rxbuf_pau8, pRxMsg.lenRx_u16,
						dataLen);
				/*
				memcpy( pRxMsg.rxbuf_pau8 + pRxMsg.lenRx_u16,					//			将DT帧中的数据拷贝至数据箱接收缓冲区中
						pRxCanMsg.data_au8 + 1,								//
						dataLen													//
						);														//
				*/
				pRxCM.bam_rxFrames_u8++;										//			已接收帧数增加1
				pRxMsg.lenRx_u16 = (short)((pRxCM.bam_FrameNo_u8-1)*7 + dataLen);		//			已接收字节数
				pRxCM.bam_FrameNo_u8++;											//			期望接收的下一帧的帧顺序号

				if ( pRxMsg.lenRx_u16  == pRxMsg.lenAct_u16 ) {					//			已接收字节数=期待字节数

					dwValidPGN =  ( pRxMsg.canID_u >> 8 ) & 0x3FFFF;

					if ( ( dwValidPGN == PGN_DM1 ) ||							//			收到DM1或DM2消息
							( dwValidPGN == PGN_DM2 ) ) {							//
						pRxMsg.counter_u16  = DM_ParseDBoxData(					//				将数据从接收缓冲区解析到DM1或DM2故障数据区
								pRxMsg.data_pau8,			//					DM1或DM2故障数据区
								pRxMsg.rxbuf_pau8,			//					DM1或DM2消息数据
								pRxMsg.lenRx_u16			//					DM1或DM2消息长度
						);							//
					}															//
					else {														//			收到其它消息
						/*
						memcpy( pRxMsg.data_pau8,								//				接收完成，将数据从接收缓冲区拷贝至有效数据区
								pRxMsg.rxbuf_pau8,								//
								pRxMsg.lenRx_u16								//
								);												//
						*/
						System.arraycopy(pRxMsg.rxbuf_pau8, 0,
								pRxMsg.data_pau8, 0,
								pRxMsg.lenRx_u16);

					}

					if ( pRxMsg.func_pf != null ) {								//				有接收回调函数时
						pRxMsg.func_pf.j1939_RxDBoxCallback_tpf(pRxMsg.pfArg);	//				调用回调函数以处理接收数据
					}															//
					RxCloseBAM(pRxMsg, (short)J1939_BOXSTATUS_VALID_DU16);		//				接收完成，中止BAM会话并标记数据可用
				}																//
				else {															//			接收未完成
					pRxMsg.cm_Timeout_u32 = System.currentTimeMillis() + TIMEOUT_RXDT_TO_RXDT;	//				收到下一DT帧的超时时间
					LinkRxTimeout(pRxMsg);										//				将接收数据箱加入超时链表
				}
			}
		}

	}

	/**
	 *	 处理接收到的点对点DT帧
	 *
	 *		参数:
	 *			can_Message_ts	*pRxCanMsg:	指向接收到的DT帧
	 */
	private void RxFrameProc_RTSDT(can_Message_ts pRxCanMsg )
	{
		J1939_RxCM_ts	pRxCM;
		J1939_rxMsg_ts	pRxMsg;
		short			dataLen;
		short			srcAddr_u8;
		int				dwValidPGN;

		srcAddr_u8 = (short)(pRxCanMsg.id_u32 & 0xFF);							// DT帧源节点地址
		pRxCM = j1939_RxCM[srcAddr_u8];											// 指向DT帧源节点连接管理结构
		if ( pRxCM.rts_boxNumber_u16 == 0 ) {									// 没有DT源结点的点对点接收数据箱
			return;																//		忽略DT帧
		}

		// 有DT源结点的点对点接收数据箱
		pRxMsg = j1939_RxCfg.start_pas[pRxCM.rts_boxNumber_u16-1];				// 本节点正在从DT帧的源节点接收的数据箱号
		if (  pRxMsg.status_u16 != J1939_BOXSTATUS_RTS_DU16 )  {				// 接收数据箱当前不在接收点对点DT帧状态。
			// 因为接收超时而中止了接收后，发送方继续
			// 发送数据帧时出现这种情况
			return;																// 忽略DT帧
		}

		if ( (pRxCanMsg.data_au8[0] & 0xFF) != pRxCM.cts_FrameNo_u8 ) {			// 帧顺序号错
			RxCloseRTS(pRxMsg, srcAddr_u8, (short)J1939_BOXSTATUS_INV_SEQNBR_DU16);	//		接收端中止RTS会话,并标记帧顺序错
		}
		else {																	// DT帧顺序号正确
			dataLen = (short)((pRxCanMsg.numBytes_u8 & 0xFF) - 1);				//
			if ( ( pRxMsg.lenAct_u16 - pRxMsg.lenRx_u16 ) < dataLen ) {			//		帧数据长度大于 （应接收长度 - 已接收长度）, 则帧中有填充字节
				dataLen = (short)(pRxMsg.lenAct_u16 - pRxMsg.lenRx_u16);		//			只考虑帧中的有效数据
			}
			if ( (pRxMsg.lenRx_u16 + dataLen) > pRxMsg.lenMax_u16 ) {			//		长度溢出
				RxCloseRTS(pRxMsg, srcAddr_u8, (short)J1939_BOXSTATUS_INV_OVFL_DU16);	//			接收端中止RTS会话，并标记数据溢出
			}
			else {																//		长度未溢出
				/*
				memcpy( pRxMsg.rxbuf_pau8 + pRxMsg.lenRx_u16,					//			将DT帧中的数据拷贝至数据箱接收缓冲区中
						pRxCanMsg.data_au8 + 1,								//
						dataLen													//
						);														//
				*/
				System.arraycopy(pRxCanMsg.data_au8, 1,
						pRxMsg.rxbuf_pau8, 0,
						dataLen);
				pRxCM.cts_rxFrames_u8++;										//			已接收帧数增加1
				pRxMsg.lenRx_u16 = (short)((pRxCM.cts_FrameNo_u8-1)*7 + dataLen);		//			已接收字节数
				pRxCM.cts_FrameNo_u8++;											//			期望接收的下一帧的帧顺序号

				if ( pRxMsg.lenRx_u16  == pRxMsg.lenAct_u16 ) {					//			已接收字节数=期待字节数

					dwValidPGN =  ( pRxMsg.canID_u >> 8 ) & 0x3FFFF;

					if ( ( dwValidPGN == PGN_DM1 ) ||							//			收到DM1或DM2消息
							( dwValidPGN == PGN_DM2 ) ) {							//
						pRxMsg.counter_u16  = DM_ParseDBoxData(					//				将数据从接收缓冲区解析到DM1或DM2故障数据区
								pRxMsg.data_pau8,			//					DM1或DM2故障数据区
								pRxMsg.rxbuf_pau8,			//					DM1或DM2消息数据
								pRxMsg.lenRx_u16			//					DM1或DM2消息长度
						);							//
					}															//
					else {														//			收到其它消息
						/*
						memcpy( pRxMsg.data_pau8,								//				接收完成，将数据从接收缓冲区拷贝至有效数据区
								pRxMsg.rxbuf_pau8,								//
								pRxMsg.lenRx_u16								//
								);
						*/
						System.arraycopy(pRxMsg.rxbuf_pau8, 0,
								pRxMsg.data_pau8, 0,
								pRxMsg.lenRx_u16);

						//
					}

					if ( pRxMsg.func_pf != null  ) {							//				有接收回调函数时
						pRxMsg.func_pf.j1939_RxDBoxCallback_tpf(pRxMsg.pfArg);	//				调用回调函数以处理接收数据
					}															//
					SendEOMFrame(												//				向DT源发送EndOfMsgAck帧
							(pRxMsg.canID_u>>8 ) & 0x3FFFF,						//					PGN
							pRxMsg.lenRx_u16,									//					已接收数据长度
							pRxCM.cts_rxFrames_u8,								//					已接收帧数
							srcAddr_u8											//					DT源节点地址
					);
					RxCloseRTS(pRxMsg, srcAddr_u8, (short)J1939_BOXSTATUS_VALID_DU16);	//				接收完成，中止RTS会话并标记数据可用
				}																//
				else {															//			接收未完成
					pRxMsg.cm_Timeout_u32 =  System.currentTimeMillis()  + TIMEOUT_RXDT_TO_RXDT;	//				收到下一DT帧的超时时间
					LinkRxTimeout(pRxMsg);										//				将接收数据箱加入超时链表
				}
			}
		}																		//

	}

	/**
	 *
	 *	 处理收到的PGN请求帧
	 *
	 *			收到的请求帧中被请求的PGN为有效PGN( 如节点0x1A请求时指定PGN=0x2201, src=0x0A, 则有效PGN = 0x221A
	 *																	   PGN=0x22FF, src=0x0A, 则有效PGN = 0x221A
	 *																	   PGN=0xF101, src=0x0A, 则有效PGN = 0xF101
	 *@param  pRxCanMsg:	指向接收到的PGN请求帧
	 *
	 *@return:
	 *			0 -- 请求待处理
	 *			1 -- 请求已处理
	 */
	byte RxFrameProc_REQPGN(can_Message_ts pRxCanMsg )
	{
		J1939_CANID_ts		canID;
		J1939_CANID_ts		canID_req;

		int					reqPGN;
		byte				respDA;
		int					pgnMask = 0xFFFF;								// PGN 屏蔽码（PF+PS)

		short			reqMode;
		short			boxNumber;
		J1939_txMsg_ts	pTxMsg;
		byte			bRet = 1;										// 返回值默认为请求已处理

		canID = new J1939_CANID_ts(pRxCanMsg.id_u32);					// 请求帧的CAN-ID
		canID_req = new J1939_CANID_ts(0);

		canID_req.setDP(pRxCanMsg.data_au8[2]);							// 被请求的CAN_ID
		canID_req.setPF(pRxCanMsg.data_au8[1]);							//
		canID_req.setPS(pRxCanMsg.data_au8[0]);							//

		reqPGN = canID_req.PGN();										// 被请求的PGN
		respDA = canID.PS();											// 应答模式与请求模式相同（全局请求则全局应答，特定请求则特定应答）

		if ( ( j1939_TxCfg.bam_PGN_u32 == reqPGN ) ||					// 当前正在使用传输协议传输被请求的PGN
				( j1939_TxCfg.rts_PGN_u32 == reqPGN ) ) {					//
			return (0);													//		延缓处理请求帧至下个J1939任务周期
		}																//

		if ( respDA != (byte)DA_GLOBAL ) {								// 特定请求
			if 	( respDA != (byte)(J1939_Context.j1939_CommCfg.ownAddr_u8 ) )
				return (bRet);											//		跳过对其他节点的请求
			respDA = canID.SA();										// 请求源地址为应答目的地址
		}

		if ( ( canID_req.PF() & 0xFF ) <= PF_PRIV ) {					// 被请求的PGN格式为PDU1
			pgnMask = 0xFF00;											//		在发送数据箱中查找 0x22**，
		}																//

		boxNumber = GetTxBoxNumberByPGN(reqPGN, pgnMask);				// 本节点是否支持被请求的PGN？

		if ( boxNumber == 0 ){											// 不支持
			if ( respDA != (byte)DA_GLOBAL ) {							//
				AckForRequest(reqPGN, NACK);							//		只对非全局性请求发NACK应答帧
			}															//
		}																//
		else {															// 支持
			pTxMsg = j1939_TxCfg.start_pas[boxNumber-1];				//

			if ( pTxMsg.status_u16 != J1939_BOXSTATUS_INIT_DU16 ) {	//		该发送数据箱还未完成先前的发送
				return (0);												//			延缓处理请求帧至下个J1939任务周期
			}

			// 发送数据箱当前空闲

			if ( pTxMsg.func_pf != null ) {										//
				pTxMsg.func_pf.j1939_TxDBoxCallback_tpf(pTxMsg.pfCallbackArg);	// 发送数据箱有回调函数，在发送前调用回调函数以更新发送数据
			}

			if ( pTxMsg.lenAct_u16 <= 8 ){								// 短消息
				pTxMsg.status_u16  = J1939_BOXSTATUS_PENDING_DU16;		//		标记该消息需要发送
				pTxMsg.respDA_u8 = respDA;								//		发送目标节点
				pTxMsg.respPGN_u32 = reqPGN;							//		被请求的PGN
			}															//
			else {														// 长消息，一个节点在同一时刻只能发送一条长消息和广播一条长消息
				if ( respDA == (byte)DA_GLOBAL )  {						//		需要全局应答，
					if ( j1939_TxCfg.bam_PGN_u32 == 0xFFFFFFFF ) {		//		本节点BAM通道空闲
						j1939_TxCfg.bam_PGN_u32 = reqPGN;				//			标记使用BAM协议发送被请求的PGN
						j1939_TxCfg.bam_boxNumber_u16 = boxNumber;		//			使用BAM发送该数据箱
						pTxMsg.respDA_u8 = respDA;						//			发送目标节点
						pTxMsg.respPGN_u32 = reqPGN;					//			被请求的PGN
						pTxMsg.status_u16  = J1939_BOXSTATUS_PENDING_DU16;
					}
					else {												//		本节点BAM广播通道忙
						bRet = 0;										//			请求待处理
					}
				}														//
				else {													//		需要特定应答
					if ( j1939_TxCfg.rts_PGN_u32 != 0xFFFFFFFF ) {		//		本节点RTS发送通道忙
						//AckForRequest(reqPGN, NRESP);				//			应答“ECU忙”
						bRet = 0;										//			请求待处理
					}													//
					else {												//		本节点RTS发送通道空闲
						j1939_TxCfg.rts_PGN_u32 = reqPGN;				//			标记使用RTS协议发送被请求的PGN
						j1939_TxCfg.rts_boxNumber_u16 = boxNumber;		//			使用RTS发送该数据箱
						pTxMsg.status_u16  = J1939_BOXSTATUS_PENDING_DU16;
						pTxMsg.respDA_u8 = respDA;						//			发送目标节点
						pTxMsg.respPGN_u32 = reqPGN;					//			被请求的PGN
					}
				}

			}
		}

		return ( bRet);

	}

	/**
	 *	 中止接收数据箱的接收会话
	 *
	 *@param pRxMsg	: 被关闭连接的接收数据箱
	 *
	 *@return：
	 *			0 -- 中止失败
	 *			1 -- 中止成功
	 */
	byte AbortRx(J1939_rxMsg_ts pRxMsg)
	{
		J1939_RxCM_ts	pRxCM;
		byte			iRet = 0;

		if ( pRxMsg.status_u16 == J1939_BOXSTATUS_BAM_RX_DU16 ) {		// 数据箱正在接收广播长消息
			RxCloseBAM(pRxMsg, (short)J1939_BOXSTATUS_INV_TIMEOUT_DU16);//		复位该广播源相关信息，并置数据箱状态为超时
			iRet = 1;
		}																//
		else {															// 数据箱正在接收点对点传输长消息
			pRxCM = j1939_RxCM[pRxMsg.canID_u & 0xFF];
			if (pRxCM.rts_boxNumber_u16 == 0 ) return (iRet);

			if ( ( ( pRxCM.rts_boxNumber_u16-1) < j1939_RxCfg.length_u16  ) &&
					( pRxMsg == j1939_RxCfg.start_pas[pRxCM.rts_boxNumber_u16-1] ) ) {
				// if ( (pRxCM.rts_boxNumber_u16-1) == (pRxMsg - j1939_RxCfg.start_pas) ) {
				if ( SendAbortFrame(									//		通知发送方中止点对点传送
						(pRxMsg.canID_u >> 8 ) & 0xFFFF,			//			PGN
						(short)(pRxMsg.canID_u & 0xFF),				//			RTS发送方节点地址
						(byte)ABORT_REASON_TIMEOUT						//			中止原因=超时
				) == (byte)1  ) {							//
					RxCloseRTS(											//		接收方关闭RTS会话
							pRxMsg,
							(short)(pRxMsg.canID_u & 0xFF),
							(short)J1939_BOXSTATUS_INV_TIMEOUT_DU16
					);
					iRet = 1;
				}
			}
		}

		return (iRet);
	}




	/**
	 * 挂接CAN发送FIFO缓冲区，这是应用任务中必须第1个调用的J1939 API函数
	 *
	 *@param chnl_u8: 		CAN通道号, 0 -- n, 无意义
	 *@param buf_pas:		发送缓冲区
	 *@param uint16:		numMsg_u16:	发送缓冲区大小（FIFO项数）
	 *
	 *@return：
	 *						CAN_ERR_NO_ERRORS_DU16 :	无错误
	 *						CAN_ERR_CHNL_DU16:			无效通道号
	 *						CAN_ERR_INVALID_ADDR_DU16：	无效缓冲区地址
	 *						CAN_ERR_NUM_MSG_DU16:		无效缓冲区大小
	 */
	@Override
	public int can_registerTxBuf(byte chnl_u8,
								 List<can_Message_ts> buf_pas, short numMsg_16) {

		// TODO Auto-generated method stub

		if ( buf_pas == null ) {
			J1939_Context.j1939_CommCfg.can_TxFIFO  = Collections.synchronizedList(new LinkedList<can_Message_ts>());
		}
		else {
			J1939_Context.j1939_CommCfg.can_TxFIFO = buf_pas;
		}

		return (CAN_ERR_NO_ERRORS_DU16);

	}


	/**
	 *  挂接CAN接收FIFO缓冲区
	 *
	 *@param chnl_u8: 		CAN通道号, 0 -- n
	 *@param buf_pas:		CAN帧接收链表
	 *@param numMsg_u16:	接收缓冲区大小（FIFO项数）
	 *
	 *@return ：
	 *						CAN_ERR_NO_ERRORS_DU16 :		无错误
	 *						CAN_ERR_CHNL_DU16:				无效通道号
	 *						CAN_ERR_INVALID_ADDR_DU16：	无效缓冲区地址
	 *						CAN_ERR_NUM_MSG_DU16:			无效缓冲区大小
	 */
	@Override
	public int can_registerRxBuf(byte chnl_u8,
								 List<can_Message_ts> buf_pas, short numMsg_16) {

		// TODO Auto-generated method stub

		if ( buf_pas == null ) {
			J1939_Context.j1939_CommCfg.can_RxFIFO  = Collections.synchronizedList(new LinkedList<can_Message_ts>());
		}
		else {
			J1939_Context.j1939_CommCfg.can_RxFIFO = buf_pas;
		}

		// 实例化来自于其它节点的PGN请求帧队列
		// can_ReqFIFO =  new LinkedList<can_Message_ts>();

		//pRxTimeoutHeader = null;									// 超时队列头
		//pPendReqMsg = null;

		return (CAN_ERR_NO_ERRORS_DU16);

	}


	/**
	 * 初始化J1939堆栈
	 *
	 *@param canChnl_u8 :		CAN通道号, 0 -- n
	 *@param ownAddr_u8 : 		本CAN节点地址, 0x00 -- 0xFD
	 *@param priority_u8 :		J1939任务的优先级， 0 -- 255
	 *@param cycleTime_u8 :		J1939任务的运行周期 ， 1 -- 90ms, 推荐10ms
	 *@param maxTime_u8 :		J1939任务的运行时间与运行周期的比率的最大值，1 -- 99， 推荐20%
	 *
	 *@return
	 *							无
	 */
	@Override
	public int j1939_initComm(byte canChnl_u8, short ownAddr_u8,
							  byte priority_u8, short cycleTime_u16, byte maxTime_u8) {

		// TODO Auto-generated method stub

		int					i, j;
//		j1939_rxDBIndex_ts	tmp;

		J1939_Context.j1939_CommCfg.canChnl_u8 = canChnl_u8;
		J1939_Context.j1939_CommCfg.cycleTime_u16 = cycleTime_u16;
		J1939_Context.j1939_CommCfg.priority_u8 = priority_u8;
		J1939_Context.j1939_CommCfg.ownAddr_u8 = ownAddr_u8;
		J1939_Context.j1939_CommCfg.maxTime_u8 = maxTime_u8;

		// TODO: 必要的J1939协议任务初始化

		//  CAN_ID -- 接收数据箱号对照表按CAN-ID 排序(升序）
//		for ( i=0; i<(j1939_RxCfg.length_u16-1); i++ ) {
//			for ( j=(i+1); j<j1939_RxCfg.length_u16; j++ ) {
//				if ( pRxIdx[i].canID_u32 > pRxIdx[j].canID_u32 ) {
//					memcpy(&tmp, &(pRxIdx[i]), sizeof(j1939_rxDBIndex_ts));
//					memcpy(&(pRxIdx[i]), &(pRxIdx[j]), sizeof(j1939_rxDBIndex_ts));
//					memcpy(&(pRxIdx[j]), &tmp, sizeof(j1939_rxDBIndex_ts));
//				}
//			}
//		}

		// 在发送数据箱的CAN-ID中加入本结点地址
		for ( i=0; i<(j1939_TxCfg.length_u16-1); i++ ) {  // 最后一个发送数据箱为DM2发送数据箱（未使用）
			if ( j1939_TxCfg.start_pas[i].canID_u > 0 )
				j1939_TxCfg.start_pas[i].canID_u |= ownAddr_u8;
		}

		J1939_Context.j1939_CommCfg.status_u16  = J1939_CASTATUS_INIT_DU16;			// 置J1939协议栈状态为已初始化

		// 传输协议使用变量的初始化
		j1939_TxCfg.rts_boxNumber_u16 = 0;
		j1939_TxCfg.rts_PGN_u32  = 0xFFFFFFFF;
		j1939_TxCfg.cts_txFrameNo_u8  = 0;
		j1939_TxCfg.cts_txFrames_u8 = 0;
		j1939_TxCfg.cts_Frames_u8 = 0;
		j1939_TxCfg.cm_Timeout_u32 = 0xFFFFFFFF;

		j1939_TxCfg.bam_boxNumber_u16 = 0;
		j1939_TxCfg.bam_PGN_u32 = 0xFFFFFFFF;							// 正在使用传输协议(BAM)广播发送的长消息帧PGN
		j1939_TxCfg.bam_txFrameNo_u8 = 0;								// 广播帧顺序号
		j1939_TxCfg.bam_Timeout_u32 = 0xFFFFFFFF;

		return (0);
	}


	/**
	 * 挂接J1939消息接收数据箱
	 *
	 *@param 	mem_pas:	保留参数，用来与32位控制器J1939 API兼容
	 *						(j1939_rxMsg_ts	*mem_pas ：指向J1939消息接收数据箱数组)
	 *@param	num_u16：	J1939消息接收数据箱数组大小（数据箱个数）
	 *
	 *@return ：
	 *				J1939_RC_NO_ERRORS_DU16 :		无错误
	 *				J1939_RC_INVALID_ADDR_DU16：		无效地址
	 *				J1939_RC_CFG_NOTALLOWED_DU16:	必须在j1939_initComm()之前调用本函数
	 */
	@Override
	public int j1939_registerRxDataboxes(J1939_rxMsg_ts[] mem_pas, short num_u16) {

		// TODO Auto-generated method stub

		if ( J1939_Context.j1939_CommCfg.status_u16 == J1939_CASTATUS_INIT_DU16 )
			return ( J1939_RC_CFG_NOTALLOWED_DU16 );

		if (j1939_RxCfg == null ) j1939_RxCfg = new J1939_RxCfg_ts();

		// 实例化接收数据箱数组
		if ( mem_pas == null ) {
			j1939_RxCfg.start_pas = new J1939_rxMsg_ts[num_u16];
		}
		else {
			if ( mem_pas.length == (int)num_u16 ) {
				j1939_RxCfg.start_pas =  mem_pas;
			}
			else {
				return (J1939_RC_INVALID_ADDR_DU16);
			}
		}

		j1939_RxCfg.length_u16 = num_u16;

		// 分配CAN-ID -- 接收数据箱号对照表
		// pRxIdx = (j1939_rxDBIndex_ts *)jalloc(num_u16*sizeof(j1939_rxDBIndex_ts));
		// if ( pRxIdx == NULL )return ( J1939_RC_INVALID_ADDR_DU16);

		return (J1939_RC_NO_ERRORS_DU16);
	}


	/**
	 * 挂接J1939消息发送数据箱
	 *
	 *@param 	mem_pas:	保留参数，用来与32位控制器J1939 API兼容
	 * 						(j1939_txMsg_ts	*mem_pas ：指向J1939消息发送数据箱数组)
	 *@param  	num_u16：	J1939消息发送数据箱数组大小（数据箱个数）
	 *
	 *@return ：
	 * 			J1939_RC_NO_ERRORS_DU16 :		无错误
	 * 			J1939_RC_INVALID_ADDR_DU16：		无效地址
	 * 			J1939_RC_CFG_NOTALLOWED_DU16:	必须在j1939_initComm()之前调用本函数
	 */
	@Override
	public int j1939_registerTxDataboxes(J1939_txMsg_ts[] mem_pas, short num_u16) {

		// TODO Auto-generated method stub

		if ( J1939_Context.j1939_CommCfg.status_u16 == J1939_CASTATUS_INIT_DU16 )
			return ( J1939_RC_CFG_NOTALLOWED_DU16 );

		if (j1939_TxCfg == null ) j1939_TxCfg = new J1939_TxCfg_ts();

		// 实例化发送数据箱数组
		if ( mem_pas == null ) {
			j1939_TxCfg.start_pas = new J1939_txMsg_ts[num_u16];
		}
		else {
			if ( mem_pas.length == (int)num_u16 ) {
				j1939_TxCfg.start_pas =  mem_pas;
			}
			else {
				return (J1939_RC_INVALID_ADDR_DU16);
			}
		}

		j1939_TxCfg.length_u16 = num_u16;

		return (J1939_RC_NO_ERRORS_DU16);
	}


	/**
	 * 初始化1个J1939消息接收数据箱
	 *
	 *@param boxNum_u16 : 					消息接收数据箱索引, 1 -- n
	 *@param pgn_u32 : 					此数据箱要接收的PGN，0 -- 0x3FFFF (18位）
	 *@param src_u8 : 						此数据箱要接收的PGN的发送源地址， 0x00 -- 0xFD
	 *@param timeout_u16 : 				消息接收周期（ms), 10 -- 65000。0表示非周期性接收数据
	 *@param startTimeout_u16 : 			系统启动后第一次接收至此消息的时限（ms)，10 -- 65000。0表示无时限
	 *@param lenMax_u16 : 					消息数据区最大长度（字节数）， 1 -- 196
	 *@param *data_pau8 : 					存放接收到消息的数据区地址
	 *@param func_pf :						接收到此消息时调用的回调函数
	 *@param pCallbackArg:					回调函数调用参数
	 *
	 *@return ：
	 *			J1939_RC_NO_ERRORS_DU16 :			无错误
	 *			J1939_RC_INV_DATABOX_DU16：			数据箱索引无效
	 *			J1939_RC_INV_PGN_DU16:				PGN无效
	 *			J1939_RC_INV_SRC_DU16:				源地址无效
	 *			J1939_RC_INV_TIMEOUT_DU16:			接收周期无效
	 *			J1939_RC_INV_DATA_LEN_DU16:			数据区长度无效
	 *			J1939_RC_INV_DATA_PTR_DU16：			数据区地址无效
	 *			J1939_RC_INV_STARTTIMEOUT_DU16:		首次接收时限无效
	 *			J1939_RC_PGN_SRC_EXIST_DU16:		PGN+源地址已被另一个数据箱占用
	 */
	@Override
	public int j1939_initRxDatabox(short boxNum_u16, int pgn_u32, short src_u8,
								   int timeout_u16, int startTimeout_u16, short lenMax_u16,
								   Object data_pau8, IRxDBoxCallback func_pf, Object pCallbackArg) {

		// TODO Auto-generated method stub

		J1939_rxMsg_ts	pRxMsg;
		int				id_u32;
		int				i;

		id_u32 = ( pgn_u32 << 8 ) | (src_u8 & 0xFF);

		if ( ( boxNum_u16 < 1 ) || ( boxNum_u16 > j1939_RxCfg.length_u16) )
			return  ( J1939_RC_INV_DATABOX_DU16 );

		if ( pgn_u32 > 0x0003FFFF )
			return ( J1939_RC_INV_PGN_DU16 );

		if ( src_u8 > 0xFD )
			return ( J1939_RC_INV_SRC_DU16 );

		if ( ( timeout_u16 != 0 ) && ( ( timeout_u16 < 10 ) || ( timeout_u16 > 65000 ) ) )
			return ( J1939_RC_INV_TIMEOUT_DU16 );

		if ( ( lenMax_u16 == 0 ) || ( lenMax_u16 > 196 ) )
			return ( J1939_RC_INV_DATA_LEN_DU16 );

		if ( data_pau8 == null )
			return ( J1939_RC_INV_DATA_PTR_DU16 );

		if ( ( startTimeout_u16 != 0 ) && ( ( startTimeout_u16 < 10 ) || ( startTimeout_u16 > 65000 ) ) )
			return ( J1939_RC_INV_STARTTIMEOUT_DU16 );

		if (  GetRxBoxNumberByPGN(id_u32, 0xFFFFFF) > 0  ) {
			return ( J1939_RC_PGN_SRC_EXIST_DU16 );
		}

		// 实例化接收数据箱
		pRxMsg = new J1939_rxMsg_ts();
		if ( pRxMsg != null ) {
			pRxMsg.rxbuf_pau8 = new byte[lenMax_u16];
		}

		if ( ( pRxMsg == null ) || ( pRxMsg.rxbuf_pau8 == null ) ) {
			return (J1939_RC_INV_DATA_PTR_DU16);					// 返回
		}
		else {
			j1939_RxCfg.start_pas[boxNum_u16 -1] = pRxMsg;
		}

		pRxMsg.data_pau8 = data_pau8;								// 接收数据箱数据区
		pRxMsg.canID_u = id_u32;									//
		pRxMsg.lenMax_u16 = lenMax_u16;								// 接收数据箱数据区最大长度
		pRxMsg.timeout_u16 = timeout_u16;							// 接收超时
		pRxMsg.startTimeout_u16 = startTimeout_u16;					//
		pRxMsg.func_pf = func_pf;									// 回调函数
		pRxMsg.pfArg = pCallbackArg;								// 回调函数调用参数

		//for ( i=0; i<data_pau8.length; i++) data_pau8[i] = 0;		// 本数据箱的数据区清0
		//pRxIdx[boxNum_u16 -1].boxNum_u16 = boxNum_u16;			// 填充 CAN-ID -- 数据箱号对照表
		//pRxIdx[boxNum_u16 -1].canID_u32 = id_u32;					//

		pRxMsg.status_u16 = J1939_BOXSTATUS_INIT_DU16;				// 接收数据箱已初始化

		return (J1939_RC_NO_ERRORS_DU16);

	}


	/**
	 *  初始化1个J1939消息发送数据箱
	 *
	 *@param boxNum_u16 :	消息发送数据箱索引, 1 -- n
	 *@param pgn_u32 : 		此数据箱要发送的PGN，0 -- 0x3FFFF (18位）。如为PDU1格式，则包含
	 *@param prio_u8 : 		发送此消息的优先级， 0 -- 7 ，默认为6
	 *@param cycle_u16 :	消息发送周期（ms), J1939任务周期 -- 65000， 0表示非周期性发送消息（只能通过j1939_SendDatabox发送）
	 *@param offset_u16 :	消息首次发送时间提前量（ms), 0 -- cycle_u16, 用于避免多个同周期发送消息引起的总线过载。
	 *@param lenAct_u16 :	消息数据区中实际数据长度（字节数）， 1 -- 196
	 *@param data_pau8 :	指向消息数据区
	 *@param func_pf:		发送此消息前调用的回调函数
	 *@param pCallbackArg：	回调函数调用参数
	 *
	 *@return：
	 *			J1939_RC_NO_ERRORS_DU16 :			无错误
	 *			J1939_RC_INV_DATABOX_DU16：			数据箱索引无效
	 *			J1939_RC_INV_PGN_DU16:				PGN无效
	 *			J1939_RC_INV_PRIO_DU16:				优先级址无效
	 *			J1939_RC_INV_CYCLE_DU16:			发送周期无效
	 *			J1939_RC_INV_DATA_LEN_DU16:			数据区长度无效
	 *			J1939_RC_INV_DATA_PTR_DU16：			数据区地址无效
	 *			J1939_RC_INV_OFFSET_DU16:			首次发送时间提前量无效
	 *			J1939_RC_PGN_PRIO_EXIST_DU16:		PGN+优先级已被另一个数据箱占用
	 */
	@Override
	public int j1939_initTxDatabox(short boxNum_u16, int pgn_u32, byte prio_u8,
								   int cycle_u16, int offset_u16, short lenAct_u16, byte[] data_pau8,
								   ITxDBoxCallback func_pf, Object pCallbackArg) {

		// TODO Auto-generated method stub

		J1939_txMsg_ts	pTxMsg;
		int				id_u32;
		int				i;

		if ( ( boxNum_u16 == 0 ) || ( boxNum_u16 > j1939_TxCfg.length_u16) )
			return  ( J1939_RC_INV_DATABOX_DU16 );

		if ( pgn_u32 > 0x0003FFFF )
			return ( J1939_RC_INV_PGN_DU16 );

		if ( prio_u8 > 7 )
			return ( J1939_RC_INV_PRIO_DU16 );

		if ( ( cycle_u16 != 0 ) && ( ( cycle_u16 < 10 ) || ( cycle_u16 > 65000 ) ) )
			return ( J1939_RC_INV_CYCLE_DU16 );

		if ( ( lenAct_u16 == 0 ) || ( lenAct_u16 > 196 ) )
			return ( J1939_RC_INV_DATA_LEN_DU16 );

		if ( data_pau8 == null )
			return ( J1939_RC_INV_DATA_PTR_DU16 );

		if ( ( offset_u16 != 0 ) && ( offset_u16 > cycle_u16 ) )
			return ( J1939_RC_INV_OFFSET_DU16 );

		id_u32 = ( prio_u8 << 26 ) | ( pgn_u32 << 8 );
		for ( i = 0; i< j1939_TxCfg.length_u16; i++ ) {
			pTxMsg = j1939_TxCfg.start_pas[i];
			if ( pTxMsg == null ) continue;
			if( pTxMsg.canID_u  == id_u32 ) {
				return ( J1939_RC_PGN_PRIO_EXIST_DU16 );
			}
		}

		// 实例化发送数据箱
		pTxMsg = new J1939_txMsg_ts();
		if ( pTxMsg == null ) {
			return ( J1939_RC_INV_DATA_PTR_DU16 );					// 返回
		}
		else {
			j1939_TxCfg.start_pas[boxNum_u16 - 1] = pTxMsg;
		}

		pTxMsg.canID_u = id_u32;
		pTxMsg.data_pau8 = data_pau8;
		pTxMsg.lenAct_u16 = lenAct_u16;
		pTxMsg.cycle_u16 = (short)cycle_u16;
		pTxMsg.func_pf = func_pf;									// 回调函数
		pTxMsg.pfCallbackArg = pCallbackArg;						// 回调函数调用参数

		if ( cycle_u16 != 0 ) {
			pTxMsg.nextTxTime_u32 = cycle_u16 - offset_u16;
		}

		for (i=0; i<data_pau8.length; i++ ) data_pau8[i] = 0;		// 数据箱的数据区清0

		pTxMsg.status_u16 = J1939_BOXSTATUS_INIT_DU16;				// 发送数据箱已初始化
		pTxMsg.respDA_u8 = (byte)DA_NONE;							// 未被请求

		return (J1939_RC_NO_ERRORS_DU16);

	}


	/**
	 * 获取J1939接收数据箱状态
	 *
	 *@param  boxNum_u16 :				接收数据箱索引， 1 -- n
	 *@param  msgLen_pu16 				存放已接收数据长度的地址
	 *
	 *@return ：
	 *			J1939_BOXSTATUS_NONE_DU16			数据箱未初始化
	 *			J1939_BOXSTATUS_INIT_DU16			数据箱已初始化
	 *			J1939_BOXSTATUS_VALID_DU16			数据箱中的数据有效
	 *			J1939_BOXSTATUS_VAL_OVFL_DU16		数据箱中的有数据但未完全接收（DM1/2 发生缓冲区溢出）
	 *			J1939_BOXSTATUS_INV_SEQNBR_DU16		数据箱中的数据无效（使用传输协议但接收顺序错误）
	 *			J1939_BOXSTATUS_INV_ABORT_DU16		数据箱中的数据无效（rx/tx中止）
	 *			J1939_BOXSTATUS_INV_TIMEOUT_DU16	数据箱中的数据无效（接收超时）
	 *			J1939_BOXSTATUS_INV_OVFL_DU16		数据箱中的数据无效（数据溢出）
	 *			J1939_BOXSTATUS_BAM_RX_DU16			正在接收长广播消息
	 */
	@Override
	public int j1939_getRxDataboxStatus(short boxNum_u16, int[] msgLen_pu16) {
		// TODO Auto-generated method stub

		J1939_rxMsg_ts	pRxMsg;
		int				dwValidPGN;

		// 数据箱索引必须有效，否则状态为未初始化
		if ( ( boxNum_u16 == 0 ) || ( boxNum_u16 > j1939_RxCfg.length_u16 ) )
			return  (J1939_BOXSTATUS_NONE_DU16);

		// 数据箱中数据有效性，输出数据长度
		pRxMsg = j1939_RxCfg.start_pas[boxNum_u16 - 10];
		if ( ( msgLen_pu16 != null ) && ( pRxMsg.status_u16  == J1939_BOXSTATUS_VALID_DU16 ) ) {
			dwValidPGN = ( pRxMsg.canID_u >> 8 ) & 0x3FFFF;
			if ( ( dwValidPGN == PGN_DM1 ) || ( dwValidPGN == PGN_DM2 ) ) {			// 接收DM1或DM2消息的数据箱
				msgLen_pu16[0] = pRxMsg.counter_u16;								//		返回DM1或DM2故障表中的故障诊断码数量
			}																		//
			else {																	// 其它接收数据箱
				msgLen_pu16[0] = (int)(pRxMsg.lenAct_u16);							//		返回完整接收的消息数据长度
			}
		}

		return ( pRxMsg.status_u16 );												// 接收数据箱状态
	}


	/**
	 *  发送指定数据箱
	 *
	 *@param  boxNum_u16 :		消息发送数据箱索引, 1 -- n
	 *
	 *@return ：
	 *			J1939_RC_NO_ERRORS_DU16 :		无错误
	 *			J1939_RC_INV_DATABOX_DU16：		数据箱索引无效
	 *			J1939_RC_INV_BOX_STATUS_DU16:	数据箱未初始化
	 */
	@Override
	public int j1939_sendDatabox(short boxNum_u16) {

		// TODO Auto-generated method stub

		J1939_txMsg_ts	pTxBox;
		can_Message_ts	canMsg;

		if ( ( boxNum_u16 == 0 ) || ( boxNum_u16 > j1939_TxCfg.length_u16) )
			return  ( J1939_RC_INV_DATABOX_DU16 );

		pTxBox = j1939_TxCfg.start_pas[boxNum_u16 - 1];
		if (  pTxBox.canID_u == 0 ) {
			return ( J1939_RC_INV_BOX_STATUS_DU16 );
		}

		if ( pTxBox.status_u16  == J1939_BOXSTATUS_INIT_DU16 ){					// 该数据箱状态为静止

			if ( pTxBox.func_pf != null ) {
				pTxBox.func_pf.j1939_TxDBoxCallback_tpf(pTxBox.pfCallbackArg);	// 发送数据箱有回调函数，在发送前调用回调函数以更新发送数据
			}

			if ( pTxBox.lenAct_u16 > 8 ) {										// J1939消息长度大于8
				pTxBox.status_u16 = J1939_BOXSTATUS_PENDING_DU16;				//		通知J1939任务使用传输协议发送J1939消息
			}																	//
			else {																// J1939消息长度小于等于8, 直接将消息放入CAN发送FIFO队列															//
				canMsg = new can_Message_ts();									//		实例化can帧对象
				canMsg.id_u32 =  pTxBox.canID_u;								//		形成发送帧
				canMsg.format_u8 = CAN_EXD;										//
				canMsg.numBytes_u8 = (byte)pTxBox.lenAct_u16;					//
				System.arraycopy(pTxBox.data_pau8, 0,
						canMsg.data_au8, 0,
						pTxBox.lenAct_u16);
				if ( SendFrame(canMsg, (short)0xFFFF) == 0 ) {					//		未发送且未发送缓存
					pTxBox.status_u16 = J1939_BOXSTATUS_PENDING_DU16;			//			发送FIFO队列满，暂缓发送（通知J1939任务发送该帧）
				}																//
			}

			if ( pTxBox.cycle_u16 > 0 ) {										// 对周期性发送的数据箱
				pTxBox.nextTxTime_u32 =  System.currentTimeMillis() + pTxBox.cycle_u16;
				//		更新此发送数据箱的下次发送时间
			}																	//
		}
		else {																	// 本数据箱的前次发送未完成，忽略发送请求
			// 该数据箱处于活动状态，不发送
		}

		return (J1939_RC_NO_ERRORS_DU16);
	}


	/**
	 *
	 *	获取J1939协议栈状态
	 *
	 *@param  canChnl_u8:					CAN通道号
	 *
	 *@return
	 *	 			高字节：
	 *				J1939_CASTATUS_NONE_DU16 :			J1939协议栈还未初始化或无效参数
	 *				J1939_CASTATUS_INIT_DU16：			J1939协议栈已初始化
	 *				J1939_CASTATUS_WAIT_CLAIM_DU16:		处于地址声明期间
	 *				J1939_CASTATUS_ADDR_CLAIMED_DU16:	声明地址成功，开始收发J1939消息
	 *				J1939_CASTATUS_CANNOT_CLAIM_DU16:	声明地址失败（与总线上其他J1939节点地址冲突），不能收发J1939消息
	 *
	 *				低字节：
	 *				J1939_SUBSTATUS_TIMEOUT_DU16:		在J1939任务周期内无法处理所有消息。可能的原因有：
	 *														j1939_initComm()指定的最大执行时间太小
	 *														消息回调函数执行时间太长
	 *				J1939_SUBSTATUS_RXOVFL_DU16：		J1939 CAN接收缓冲区溢出，可能丢帧。可能的原因有：
	 *														指定CAN通道的接收缓冲区太小
	 *														J1939任务周期太长，因此缓冲区中堆积了太多的消息
	 */
	@Override
	public int j1939_getStatus(byte canChnl_u8) {
		// TODO Auto-generated method stub
		return ( J1939_Context.j1939_CommCfg.status_u16 );
	}



	/**
	 *
	 *	发送请求PGN
	 *
	 *			在接收数据箱中必须有接收 被请求节点应答 的接收数据箱
	 *
	 *@param 	destAddr_u8 ,					被请求节点地址（0--253， 255）
	 *			pgn_u32							被请求PGN ( 0 -- 0x003FFFF)
	 *
	 *@return ：
	 *			J1939_RC_NO_ERRORS_DU16 :			无错误
	 *			J1939_RC_INV_PGN_DU16：				PGN无效
	 *			J1939_RC_INV_ECU_ADDR_DU16:			被请求节点地址无效
	 */
	@Override
	public int j1939_sendRequestPGN(short destAddr_u8, int pgn_u32) {

		// TODO Auto-generated method stub
		J1939_CANID_ts	canID;
		short			boxNumber;
		int				validPGN, canIDMask;
		J1939_rxMsg_ts	pRxMsg;
		can_Message_ts	canMsg;

		if ( pgn_u32 > 0x0003FFFF )
			return ( J1939_RC_INV_PGN_DU16 );

		if ( ( destAddr_u8 == DA_INVALID ) || ( destAddr_u8 == J1939_Context.j1939_CommCfg.ownAddr_u8  )){
			// 请求PGN帧中的目标地址不能是节点本身
			return ( J1939_RC_INV_ECU_ADDR_DU16 );
		}

		canID = new J1939_CANID_ts( pgn_u32 << 8 );
		canIDMask = 0xFFFFFFFF;

		if ( ( canID.PF() & 0xFF) <= PF_PRIV ) {								// PDU1格式的PGN
			if ( destAddr_u8 != DA_GLOBAL ) {									//
				canID.setPS((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8) );	//
				canID.setSA((byte)destAddr_u8);									//		特定源，如请求PGN=0x22##, 本节点地址=0x0A, 源节点地址=0x1A, 则应接收CAN-ID=0x18220A1A
			}																//
			else {															//
				canID.setPS((byte)DA_GLOBAL);								//
				canIDMask = 0xFFFFFF00;										//		全局源，如请求PGN=0x22##, 源节点地址=0xFF, 则应接收CAN-ID=0x1822FF**
			}																//
		}																	//
		else {																// PDU2格式的PGN
			if (  (byte)destAddr_u8 != (byte)DA_GLOBAL ) {								//
				canID.setSA((byte)destAddr_u8);									//		特定源，如请求PGN=0xF201, 源节点地址=0x09, 则应接收CAN-ID=0x18F20109
			}																//
			else {															//
				canIDMask = 0xFFFFFF00;										//		全局源, 如请求PGN=0xF201, 节点地址=0xFF, 由可接收CAN-ID=0x18F201**
			}																//
		}																	//

		//if ( ( boxNumber = GetRxBoxNumberByPGN(pgn_u32, destAddr_u8, 0)) == 0xFFFF ) {

		if ( ( boxNumber = GetRxBoxNumberByPGN(canID.canID_u32, canIDMask) ) == 0 ) {
			// 接收数据箱中无被请求的PGN消息配置，即使有请求应答也不能处理，所以不发送请求
			return ( J1939_RC_INV_PGN_DU16 );
		}

		validPGN = canID.canID_u32 >> 8;
		pRxMsg = j1939_RxCfg.start_pas[boxNumber-1];

		canID.setSA((byte)(J1939_Context.j1939_CommCfg.ownAddr_u8));	// 本节点地址
		canID.setPF((byte)PF_REQPGN);									// 请求PGN
		canID.setPS((byte)destAddr_u8);										// 被请求节点地址
		canID.setDP((byte)0x18);										// CAN优先级6

		canMsg = new can_Message_ts();

		canMsg.id_u32 = canID.canID_u32;
		canMsg.format_u8 = CAN_EXD;
		canMsg.numBytes_u8  = 3;

		canMsg.data_au8[0] =(byte)(validPGN) ;						// 被请求PGN按小端格式装配到请求PGN帧的数据域
		canMsg.data_au8[1] =(byte)(validPGN >> 8);					//
		canMsg.data_au8[2] =(byte)(validPGN >> 16);					//

		// 将请求PGN帧写入CAN发送缓冲区
		if (  SendFrame(canMsg, (short)0xFFFF ) == 1 ) {			// 发送或缓存成功，
			//pRxMsg.status_u16 = J1939_BOXSTATUS_PENDING_DU16;		//		收到被请求的PGN数据前数据箱内容不可靠
		}

		return (J1939_RC_NO_ERRORS_DU16);
	}

	/**
	 *
	 * 初始化以接收来自于其他节点的故障表
	 *
	 *			像接收普通J1939消息一样接收来自于其他节点的DM1和DM2消息，区别在于PGN和对消息数据的解析。
	 *			当DM1/DM2接收数据数据箱接收完DM1/DM2消息后，将解析其中每个DTC并写入	dtcList_ps指定的故障表中。
	 *
	 *			由于DM1消息的发送周期为1秒且为广播发送，而协议规定广播DT帧的间隔不小于50ms，因此如果激活的DTC数量为29，则
	 *			DM1消息最快传送时间为 50 * ( ( (29*4 + 2 ） + 6 )/ 7 + 1 )   = 50*18 = 900ms。如果激活的DTC数量大于29则
	 *			不能保证能在1秒之内送出DM1消息。
	 *
	 *			考虑到实际应用中同时激活的故障码不会太多，因此故障表设置为最多容纳20个DTC。
	 *
	 *
	 *@param srcAddr_u8 : 	本节点要接收其DM1/DM2故障表的的故障源（即其他节点）地址, 有效范围 0 -- 253
	 *@param dmType_u16 : 	要接收的故障表类型及CM=1时的转换方法版本。对CM=0,总是使用转换方法版本4.
	 *								J1939_LIST_DM1_DU16		--  DM1 failure list, use CM version 3 for CM-Bit=1
	 *								J1939_LIST_DM2_DU16		--  DM2 failure list, use CM version 3 for CM-Bit=1
	 *								J1939_LIST_DM1_V1_DU16	--  DM1 failure list, use CM version 1 for CM-Bit=1
	 *								J1939_LIST_DM2_V1_DU16	--  DM2 failure list, use CM version 1 for CM-Bit=1
	 *								J1939_LIST_DM1_V2_DU16	--  DM1 failure list, use CM version 2 for CM-Bit=1
	 *								J1939_LIST_DM2_V2_DU16	--  DM2 failure list, use CM version 2 for CM-Bit=1
	 *								J1939_LIST_DM1_V3_DU16	--  DM1 failure list, use CM version 3 for CM-Bit=1
	 *								J1939_LIST_DM2_V3_DU16	--  DM2 failure list, use CM version 3 for CM-Bit=1
	 *@param dtcList_ps :	指向应用定义的故障表
	 *@param boxNum_u16 : 	接收DM1/DM2故障源所使用的接收数据箱编号
	 *@param func_pf:		完整接收DM1/DM2故障消息后调用的回调函数
	 *
	 *@return：
	 *			J1939_RC_NO_ERRORS_DU16		--  Function was executed without errors.
	 *			J1939_RC_INVALID_ADDR_DU16	--  Address to data structure is Invalid.
	 *			J1939_RC_INV_DATABOX_DU16	--  Invalid databox number.
	 *			J1939_RC_PGN_SRC_EXIST_DU16	--  Another message box with the same PGN and source address is alraedy defined.
	 *			J1939_RC_INV_ECU_ADDR_DU16	--	Invalid controller (ECU) address.
	 *			J1939_RC_INV_DM_TYPE_DU16	--	Invalid DM failure type.
	 */
	@Override
	public int j1939_initDiagnostic(short srcAddr_u8, int dmType_u16,
									J1939_dtcList_ts dtcList_ps, short boxNum_u16,
									IRxDBoxCallback func_pf, Object pCallbackArg) {

		// TODO Auto-generated method stub

		int		dmPGN;
		short	wLen;

		J1939_dtcList_ts p;

		if ( srcAddr_u8 > (short)0xFD ) {
			return (J1939_RC_INVALID_ADDR_DU16);
		}

		if (  dmType_u16 == J1939_LIST_DM1_DU16 ) {
			dmPGN = PGN_DM1;
			wLen = (short)(2+4*DM1_MAXDTCS);
		}
		else if ( dmType_u16 == J1939_LIST_DM2_DU16 ) {
			dmPGN = PGN_DM2;
			wLen = (short)(2+4*DM2_MAXDTCS);
		}
		else {
			return ( J1939_RC_INV_DM_TYPE_DU16 );
		}

		p = dtcList_ps;
		if ( p == null ) {
			p = new J1939_dtcList_ts();
			if ( p == null ) {
				return (J1939_RC_INVALID_ADDR_DU16);
			}
		}

		return j1939_initRxDatabox(
				boxNum_u16,										// 接收数据箱编号
				dmPGN,											// 接收PGN
				srcAddr_u8,										// 数据源节点地址
				0,												// 接收周期
				0,												// 启动后接收时间提前量
				wLen,											// 接收消息的最大长度，应为DM1或DM2消息的长度，而不是DM1或DM2 DTC表的长度。
				p,												// DM1或DM2 DTC表地址。完整接收DM1或DM2消息后，将解析并将结果存放到此地址指定的DTC表中
				func_pf,										// 回调函数
				pCallbackArg									// 回调函数调用参数
		);

	}



	/**
	 *
	 *	 请求其它节点发送其DM1或DM2故障表
	 *
	 *			通过发送PGN请求帧来请求其他节点发送其DM1或DM2消息。必须先前已通过 j1939_initDiagnostic()
	 *			为应答的DM1或DM2消息配置了接收数据箱。
	 *
	 *@param destAddr_u8:		被请求节点地址, 有效范围 0 -- 253
	 *@param dmType_u16:		被请求的故障类型
	 *							J1939_LIST_DM1_DU16 --  DM1 failure list
	 *							J1939_LIST_DM2_DU16 --  DM2 failure list
	 *@return
	 *			J1939_RC_NO_ERRORS_DU16		--  Function was executed without errors.
	 *			J1939_RC_INVALID_ADDR_DU16	--  Address to data structure is Invalid.
	 *			J1939_RC_INV_DM_TYPE_DU16		--	Invalid DM failure type.
	 */
	@Override
	public int j1939_getDiagnostic(short destAddr_u8, int dmType_u16) {

		// TODO Auto-generated method stub
		int	dmPGN;

		if ( destAddr_u8 > 253 ) {
			return (J1939_RC_INVALID_ADDR_DU16);
		}

		if (  dmType_u16 == J1939_LIST_DM1_DU16 ) {
			dmPGN = PGN_DM1;
		}
		else if ( dmType_u16 == J1939_LIST_DM2_DU16 ) {
			dmPGN = PGN_DM2;
		}
		else {
			return ( J1939_RC_INV_DM_TYPE_DU16 );
		}

		j1939_sendRequestPGN(destAddr_u8, dmPGN);

		return ( J1939_RC_NO_ERRORS_DU16 );
	}


	/**
	 *
	 *	 初始化本控制器应用的DM1和DM2故障处理
	 *
	 *@param listFailure_pas:	指向 j1939_failure_ts类型的故障表。如果为NULL,则自动分配存贮区
	 *@param listSize_u16 :		故障表大小（项数）, 有效值范围 1 -- 85，受限于可用内存及EEPROM
	 *@param eePage_u16 :		存贮DM2故障表的EEPROM页号（1 -- ？？），必须根据硬件的具体情况设置
	 *@param eeIndex_u16 :		DM2故障表在EEPROM页中的起始位置（字索引），有效值范围 0 -- 255，但必须保证整个故障表能存贮在EEPROM页中。每个故障诊断码占用4个字。
	 *@param txBoxDM1_u16 :		发送DM1故障表的数据箱号，此数据箱不能用j1939_initTxDatabox()初始化
	 *@param txBoxDM2_u16:		发送DM2故障表的数据箱号，此数据箱不能用j1939_initTxDatabox()初始化
	 *
	 *@return
	 *			J1939_RC_NO_ERRORS_DU16		--  Function was executed without errors.
	 *			J1939_RC_INVALID_ADDR_DU16	--  Address to data structure is Invalid.
	 *			J1939_RC_INV_DATABOX_DU16		--  Invalid databox number.
	 *			J1939_RC_INV_LIST_SIZE_DU16	--  Invalid failure list size.
	 *			J1939_RC_INV_EE_PAGE_DU16		--  Invalid eeprom page number.
	 *			J1939_RC_INV_EE_IDX_DU16		--  Invalid eeprom page index.
	 */
	@Override
	public int j1939_initFailureList(J1939_failure_ts listFailure_pas,
									 short listSize_u16, short eePage_u16, short eeIndex_u16,
									 short txBoxDM1_u16, short txBoxDM2_u16) {

		// TODO Auto-generated method stub

		// void	*pTxBuf;
		J1939_failure_ts	plistFailure;

		short	wTxBufLen;
		short 	wFailureDM2Size;
		int		i;
		int		wRet;

		if ( listSize_u16 > 85 ) {
			return ( J1939_RC_INV_LIST_SIZE_DU16 );
		}
		if ( eePage_u16 > 15 ) {
			return ( J1939_RC_INV_EE_PAGE_DU16 );
		}
		if ( eeIndex_u16 > 255 ) {
			return ( J1939_RC_INV_EE_IDX_DU16 );
		}

		if ( ( GetTxBoxNumberByPGN(PGN_DM1, 0xFFFF) > 0 ) ||
				( GetTxBoxNumberByPGN(PGN_DM2, 0xFFFF) > 0 ) ) {
			return ( J1939_RC_INVALID_ADDR_DU16 );
		}


		// 初始化DM1发送数据箱
		wTxBufLen = DM1_MAXDTCS*4+2;											// DM1消息数据区最大长度 = 最大激活故障诊断码*4+2字节的指示灯开关及闪烁状态
		wRet = j1939_initTxDatabox(												//		指定的数据箱初始化为DM1发送数据箱
				txBoxDM1_u16,											//		发送DM1的数据箱编号
				PGN_DM1,												//		DM1的PGN
				(byte)6,												//		发送优先级
				1000,													//		发送周期为1秒
				0,														//		发送时间提前量
				wTxBufLen,												//		DM1消息发送数据区最大长度
				new byte[wTxBufLen],									//		DM1消息发送数据区起始指针
				null,													//		无回调函数
				null													//		无回调参数
		);														//

		if ( wRet != J1939_RC_NO_ERRORS_DU16 ) {								// DM1发送数据箱初始化出错
			return ( J1939_RC_INV_DATABOX_DU16 );								//		返回错误码
		}

		// 初始化DM2发送数据箱
		wTxBufLen = DM2_MAXDTCS*4+2;										// DM2消息数据区最大长度
		wRet = j1939_initTxDatabox(											//		指定的数据箱初始化为DM2发送数据箱：
				txBoxDM2_u16,										//		发送DM2的数据箱号
				PGN_DM2,											//		DM2的PGN
				(byte)6,											//		发送优先级
				0,													//		非周期性发送
				0,													//
				wTxBufLen,											//		DM2消息发送数据区最大长度
				new byte[wTxBufLen],								//		DM2消息发送数据区起始地址
				null,												//		无回调函数
				null												//		无回调参数
		);

		if ( wRet == J1939_RC_NO_ERRORS_DU16 ) {							// DM1、DM2发送数据箱均初始化成功

			pFailureDM1 = new LinkedList<J1939_failure_ts>();
			pFailureDM2 = new LinkedList<J1939_failure_ts>();

			if ( (pFailureDM1 == null ) || ( pFailureDM2 == null ) ) {
				return (J1939_RC_INVALID_ADDR_DU16);						//			中止并返回错误码
			}

			// 如果在Flash中保存了历史故障表项，则先将各历史故障项填充到故障表项开头
			// 并形成DM2故障链
			//		TODO

		}

		return (wRet);
	}

	/**
	 *
	 *	 从本节点的DM1故障表中删除1个、多个或所有故障
	 *
	 *			遍历DM1故障链，将其中适合指定条件的链表项摘除并加入到DM2故障链的链头。
	 *
	 *@param spn_u32:	故障的SPN, 有效值范围 0 -- 524278。指定为J1939_SPN_ALL_DU32 (= 0xFFFFFFFF)是删除所有故障
	 *@param fmi_u8:	故障模式，有效值范围 0 -- 31。 指定为J1939_FMI_ALL_DU8 (= 0xFF) 时删除指定SPN的所有模式的故障
	 *
	 *@return ：
	 *			J1939_RC_NO_ERRORS_DU16		--  Function was executed without errors.
	 *			J1939_RC_INV_SPN_DU16			--  Invalid suspect parameter number.
	 *			J1939_RC_INV_FMI_DU16			--  Invalid failure mode indicator.
	 *			J1939_RC_SPN_NOT_FOUND_DU16	--  Failure could not be found within the DM1 list.
	 *			J1939_RC_INIT_MISSING_DU16	--  Function is not possible, because j1939_initFailureList() was not yet executed.
	 */
	@Override
	public int j1939_resetFailureDM1(int spn_u32, byte fmi_u8) {

		// TODO Auto-generated method stub

		byte		bMatched = 0;

		if ( ( spn_u32 > 524278 ) && ( spn_u32 != 0xFFFFFFFF ) )
			return ( J1939_RC_INV_SPN_DU16 );

		if ( ( fmi_u8 > 31 )  && (fmi_u8 != 0xFF ) )
			return ( J1939_RC_INV_FMI_DU16 );

		for ( J1939_failure_ts dm1: pFailureDM1 ) {

			if  ( ( ( spn_u32 == 0xFFFFFFFF ) || ( dm1.dtc_s.spn_u32 == spn_u32 ) ) &&
					( ( fmi_u8 == 0xFF ) || ( dm1.dtc_s.fmi_u8 == fmi_u8 ) ) ) {

				pFailureDM2.add(dm1);									// 将被遍历项链在DM2链尾
				pFailureDM1.remove(dm1);								// 将被遍历项从DM1中摘除

				bMatched = 1;											// 标示在DM链中找到匹配的SPN+FMI
				break;
			}
		}

		if ( bMatched != 0  ) {											// 遍历过程中找到了与指定SPN+FMI匹配的故障项
			j1939_TxCfg.dm1Changed_u8 = 1;								//		标志DM1发生改变，
			return ( J1939_RC_NO_ERRORS_DU16 );							//
		}																//
		else {															// 遍历过程中未找到匹配的故障项
			return ( J1939_RC_SPN_NOT_FOUND_DU16 );						//
		}

	}


	/**
	 *
	 *	 从本节点的DM2故障表中删除所有故障
	 *
	 *			从DM2故障链中摘除所有故障项，清除故障信息后链入空闲故障项链。
	 *
	 *		参数：
	 *			无
	 *
	 *		返回值：
	 *			J1939_RC_NO_ERRORS_DU16			--   Function was executed without errors.
	 *			J1939_RC_INIT_MISSING_DU16		--   Function is not possible, because j1939_initFailureList() was not yet executed.
	 *			J1939_RC_EEP_OVERFLOW_DU16		--   Failures could not be deleted, because internal EEPROM queue overflow
	 */
	@Override
	public int j1939_resetFailureDM2() {

		// TODO Auto-generated method stub

		if ( pFailureDM2 == null ) {
			return ( J1939_RC_INIT_MISSING_DU16 );
		}

		for ( J1939_failure_ts dm2: pFailureDM2 ) {
			pFailureDM2.remove(dm2);
		}

		//
		// TODO: 清除EEPROM中的DM2数据
		//

		return ( J1939_RC_NO_ERRORS_DU16 );
	}



	/**
	 *
	 *	 在本节点的DM1故障表中添加1个故障
	 *
	 *			如果在DM1故障链中存在指定的故障，则直接无错返回；
	 *			如果在DM2故障链中存在指定的故障，则从DM2链中摘除并链入DM1且故障计数加1；
	 *			如果DM1、DM2链中均不存在该故障，则从空闲链头摘下一项并链入DM1,其故障计数为1。
	 *
	 *@param  spn_u32 :			故障的SPN
	 *@param  fmi_u8 :			故障模式
	 *@param  lampState_u8 :	故障指示灯状态（每两位对应一种指示灯，00 -- 指示灯灭 01 -- 指示灯亮）
	 *@param  lampFlash_u8	 :	指示灯闪烁状态（每两位对应一种指示灯闪烁状态，00 -- 慢闪 01 -- 快闪  11 -- 不闪烁）
	 *
	 *@return ：
	 *			J1939_RC_NO_ERRORS_DU16		--  Function was executed without errors.
	 *			J1939_RC_INV_SPN_DU16			--  Invalid suspect parameter number.
	 *			J1939_RC_INV_FMI_DU16			--  Invalid failure mode indicator.
	 *			J1939_RC_DM1_FULL_DU16		--  Failure could not be stored in DM1 list, because maximum number of failures reached.
	 *			J1939_RC_DM2_FULL_DU16		--  Failure could not be stored in DM2 list, because maximum number of failures reached.
	 *			J1939_RC_EEP_OVERFLOW_DU16	--  Failure could not be stored, because internal EEPROM queue overflow.
	 *			J1939_RC_INIT_MISSING_DU16	--  Function is not possible, because j1939_initFailureList() was not yet executed.
	 */
	@Override
	public int j1939_setFailureDM1(int spn_u32, byte fmi_u8, byte lampState_u8,
								   byte lampFlash_u8) {

		// TODO Auto-generated method stub

		if (  spn_u32 > 524278 )
			return ( J1939_RC_INV_SPN_DU16 );

		if (  fmi_u8 > 31 )
			return ( J1939_RC_INV_FMI_DU16 );

		if ( pFailureDM1 == null )
			return ( J1939_RC_INIT_MISSING_DU16 );

		for ( J1939_failure_ts dm1: pFailureDM1 ) {
			if ( ( dm1.dtc_s.spn_u32 == spn_u32 ) &&						//
					( dm1.dtc_s.fmi_u8 == fmi_u8 ) ) {							//
				return ( J1939_RC_NO_ERRORS_DU16 );							//		DM1中存在此故障项，直接返回
			}																//
		}

		// 此时，在DM1链中未找到匹配故障项
		for ( J1939_failure_ts dm2: pFailureDM2 ) {
			if ( ( dm2.dtc_s.spn_u32 == spn_u32 ) &&						//
					( dm2.dtc_s.fmi_u8 == fmi_u8 ) ) {							//
				pFailureDM1.add(dm2);										//	在DM1链尾添加故障项
				pFailureDM2.remove(dm2);									//	从DM2链中删除故障项
				if ( dm2.dtc_s.occ_u8 <= 125 ) {							//  修改故障发生次数。最大计数为126。127表示故障次数未知
					dm2.dtc_s.occ_u8++;										//
				}															//
				dm2.lamp_s.lampFlash_u8 = lampFlash_u8;						//
				dm2.lamp_s.lampState_u8 = lampState_u8;						//
				return ( J1939_RC_NO_ERRORS_DU16 );							//
			}
		}

		// 在DM1和DM2链中都未找到匹配故障项
		J1939_failure_ts dm = new J1939_failure_ts();
		dm.dtc_s.spn_u32 = spn_u32;
		dm.dtc_s.fmi_u8 = fmi_u8;											//
		dm.dtc_s.occ_u8 = 1;												//
		dm.lamp_s.lampFlash_u8 = lampFlash_u8;								//
		dm.lamp_s.lampState_u8 = lampState_u8;								//

		pFailureDM1.add(dm);												// 在DM链尾添加故障项

		//
		// TODO: 将 DM1 + DM2 写入EEPROM
		//

		//
		// 在DM1消息发送周期之间最多只能发送一条额外的DM1消息
		//
		j1939_TxCfg.dm1Changed_u8 = 1;

		return ( J1939_RC_NO_ERRORS_DU16 );									//
	}


	/**
	 *  检查本节点的故障表中是否存在指定的故障
	 *
	 *@param spn_u32 :	被检查故障的SPN, 0 -- 524287  或 J1939_SPN_ALL_DU32 (= 0xFFFFFFFF)
	 *@param fmi_u8 :	被检查故障的模式，0 -- 31 或 J1939_FMI_ALL_DU8 (= 0xFF)
	 *
	 *@return:
	 *			J1939_RC_DM1_NOT_EXIST_DU16		--   DM1 failure does not exist within DM1 failure list.
	 *			J1939_RC_DM1_EXIST_DU16			--   DM1 failure(s) exist(s) within DM1 failure list.
	 */
	@Override
	public int j1939_testFailureDM1(int spn_u32, byte fmi_u8) {
		// TODO Auto-generated method stub

		for ( J1939_failure_ts dm1: pFailureDM1 ) {
			if  ( ( ( dm1.dtc_s.spn_u32 == spn_u32 ) || ( spn_u32 == 0xFFFFFFFF ) ) &&
					( ( dm1.dtc_s.fmi_u8 == fmi_u8 ) || ( fmi_u8 == 0xFF ) ) ) {
				return ( J1939_RC_DM1_EXIST_DU16 );
			}
		}

		return ( J1939_RC_DM1_NOT_EXIST_DU16 );
	}




	/**
	 * J1939协议栈任务函数，实现
	 *
	 *		1. 从CAN接收FIFO队列中逐帧读出CAN帧，并组装至J1939接收数据箱中
	 *		2. 检查J1939发送数据箱状态，并
	 */
	@Override
	public void j1939_CycAction() {

		int				i;

		J1939_txMsg_ts	pTxMsg;
		J1939_rxMsg_ts	pRxMsg;

		J1939_CANID_ts	canID = null;			//
		can_Message_ts	pRxCanMsg = null;		// 从CAN帧接收FIFO中读出的CAN帧地址
		can_Message_ts	canMsg = null;			// 发送帧
		can_Message_ts  pPendReqMsg=null;		// 来自于其它节点的PGN请求帧

		int				dwValidPGN;				// 有效PGN
		//int			dwMaskPGN;				// 计算有效PGN的屏蔽字

		byte			TP_CtrlByte_u8;			// 传输协议帧中的控制字节
		byte			TP_DestAddr_u8;			// 传输协议帧中的目的节点地址

		short			boxNumber;

		List<can_Message_ts> pRxFIFO;

		// 1. 从CAN FIFO接收队列中逐帧读出CAN帧并处理
		pRxFIFO = J1939_Context.j1939_CommCfg.can_RxFIFO;
		while ( pRxFIFO.size() > 0 ) {

			pRxCanMsg = pRxFIFO.get(0);
			pRxFIFO.remove(0);
			//Log.i("pRxFIFO.size()======", pRxFIFO.size()+"");

			canID = new J1939_CANID_ts(pRxCanMsg.id_u32);						// 接收到的CAN帧id

			switch ( canID.PF() ) {
				//case PF_REQPGN:												// 请求PGN
				//  只有数据发送方能收到PGN请求帧
				//	RxFrameProc_REQPGN(pRxCanMsg);
				//	break;

				case (byte)PF_REQACK:											// 对请求PGN的应答
					// 只有数据接收方(PGN请求方)能收到PGN请求应答帧
					RxFrameProc_REQACK(pRxCanMsg);
					break;

				case (byte)PF_CM:												// 连接管理帧
					// 发送或接收方都能收到连接管理帧
					RxFrameProc_CM(pRxCanMsg);									//		处理连接管理帧
					break;

				case (byte)PF_DT:												// 长帧传输的数据帧
					// 只接收方
					if ( canID.PS() == (byte)DA_GLOBAL ) {						// 是广播DT
						RxFrameProc_BAMDT(pRxCanMsg);							//		处理该广播DT帧
					}															//
					else if ( canID.PS() == (byte)(J1939_Context.j1939_CommCfg.ownAddr_u8) ) {			// 是传送给本节的点对点DT帧
						RxFrameProc_RTSDT(pRxCanMsg);							//		处理该点对点DT帧
					}															//
					else {														//	是传送给其他节点的点对点DT帧
					}															//		简单忽略
					break;

				default:														// 其他帧
					boxNumber = GetRxBoxNumberByPGN(							// 		在接收数据箱中查找匹配的数据箱
							canID.canID_u32,								//		精确匹配 PGN+SA
							0xFFFFFF										//
					);
					if ( boxNumber > 0  ) {
						pRxMsg = j1939_RxCfg.start_pas[boxNumber - 1];			//		将CAN帧中的数据拷贝至数据箱中

						dwValidPGN =  ( pRxMsg.canID_u >> 8 ) & 0x3FFFF;

						if ( ( dwValidPGN == PGN_DM1 ) ||						//			收到DM1或DM2消息
								( dwValidPGN == PGN_DM2 ) ) {						//
							pRxMsg.counter_u16  = DM_ParseDBoxData(			//				将数据从CAN消息数据区解析到DM1或DM2故障数据区
									pRxMsg.data_pau8,		//					DM1或DM2故障数据区
									pRxCanMsg.data_au8,	//					DM1或DM2消息数据
									pRxCanMsg.numBytes_u8	//					DM1或DM2消息长度
							);						//
						}														//
						else {													//			收到其它消息
							/*
							memcpy(pRxMsg.data_pau8, pRxCanMsg.data_au8,		//				将数据从接收缓冲区拷贝至接收数据区中
								   pRxCanMsg.numBytes_u8);
							*/
							System.arraycopy(pRxCanMsg.data_au8, 0,
									pRxMsg.data_pau8, 0,
									pRxCanMsg.numBytes_u8);

						}

						if ( pRxMsg.func_pf != null ) {								//
							pRxMsg.func_pf.j1939_RxDBoxCallback_tpf(pRxMsg.pfArg);	//		如果指定了回调函数则执行
						}
						pRxMsg.status_u16 = J1939_BOXSTATUS_VALID_DU16;				//		数据箱中数据有效
					}
					break;
			}
		}

		// 2. 检查PGN请求队列
		List<can_Message_ts> pReqFIFO = J1939_Context.j1939_CommCfg.can_ReqFIFO;
		while ( pReqFIFO.size() > 0 ) {														// 遍历未完成的请求项
			pPendReqMsg = pReqFIFO.get(0);
			pReqFIFO.remove(0);
			if ( pPendReqMsg == null ) break;
			if ( RxFrameProc_REQPGN(pPendReqMsg) == 0 ) {					//		处理该请求项
				break;														//			处理不成功，下个任务周期继续处理该请求
			}																//
		}

		// 3. 检查接收超时双向链表
		//pRxMsg = pRxTimeoutHeader;
		while ( true ) { 														// 遍历已提成时间排序的超时链表
			if (pRxTimeoutHeader.size() == 0 ) break;
			pRxMsg = pRxTimeoutHeader.getFirst();								// 总是取出链头
			if ( pRxMsg == null ) break;
			if ( System.currentTimeMillis() > pRxMsg.cm_Timeout_u32 ) { 		// 链表项接收超时
				AbortRx(pRxMsg);												//		中止超时项接收
				if ( pRxTimeoutHeader.contains(pRxMsg) ) {
					pRxTimeoutHeader.remove();									// 		从链表中删除
				}
			}
			else {
				break;
			}
		}

		// 4. 检查发送数据箱的状态
		for ( i=0; i<j1939_TxCfg.length_u16; i++ ) {

			pTxMsg = j1939_TxCfg.start_pas[i];
			if ( pTxMsg == null ) continue;										// 跳过未实例化的发送数据箱
			if ( pTxMsg.status_u16 == J1939_BOXSTATUS_NONE_DU16 ) continue;		// 跳过未初始化的发送数据箱

			switch ( pTxMsg.status_u16 ) {

				case J1939_BOXSTATUS_INIT_DU16:											// 该J1939消息当前无未完成的发送

					if ( pTxMsg.cycle_u16  > 0 ) {													// 周期性发送消息

						if 	( System.currentTimeMillis() >= pTxMsg.nextTxTime_u32 ) {			//		发送时间到
							if ( pTxMsg.func_pf != null ) {										//
								pTxMsg.func_pf.j1939_TxDBoxCallback_tpf(pTxMsg.pfCallbackArg);	//			发送数据箱有回调函数，在发送前调用回调函数以更新发送数据
							}
							pTxMsg.nextTxTime_u32 = System.currentTimeMillis() + pTxMsg.cycle_u16;
							//			修改下次发送时间
							pTxMsg.status_u16 = J1939_BOXSTATUS_PENDING_DU16;					//			该消息需要发送
							if ( ( ( pTxMsg.canID_u >> 8 ) & 0x3FFFF ) == PGN_DM1 ) {			//				DM1发送数据箱
								j1939_TxCfg.dm1Changed_u8 = 0;									//					标示DM1队列无变化
								j1939_TxCfg.dm1TxInCycle_u8 = 0;								//					标示DM1发送周期中未发送额外的DM1消息
							}
						}
						else {															//		未到发送时间，
							if ( ((( pTxMsg.canID_u >> 8) & 0x3FFFF )== PGN_DM1 ) && 	//			DM1发送数据箱且
									( j1939_TxCfg.dm1Changed_u8 != 0 ) &&					//			DM1队列有变化且
									( j1939_TxCfg.dm1TxInCycle_u8 == 0 ) ) {				//			DM1消息发送周期中还未额外发送DM1消息
								pTxMsg.status_u16 = J1939_BOXSTATUS_PENDING_DU16;		//				该消息需要发送
								j1939_TxCfg.dm1TxInCycle_u8 = 1;						//				标示DM1消息发送周期中已额外发送DM1消息
							}
							else {														//			非DM1发送数据箱、或DM1队列无变化、或已额外发送DM1数据箱
								continue;												//				跳过发送
							}															//
						}
					}
					else {																// 非周期性数据箱
						continue;														//		跳过
					}

				case J1939_BOXSTATUS_PENDING_DU16:									// 该1939消息需要发送

					if ( canID == null ) {
						canID = new J1939_CANID_ts(0);
					}

					if ( pTxMsg.respDA_u8 != (byte)DA_NONE ) {						// 请求应答时
						// canID = new J1939_CANID_ts(( pTxMsg.canID_u & 0x3F0000FF )|( pTxMsg.respPGN_u32 <<8 ));
						canID.setID(( pTxMsg.canID_u & 0x3F0000FF )|( pTxMsg.respPGN_u32 <<8 ));
						/*
						canID.canID_u32 = ( pTxMsg.canID_u & 0x3F0000FF )|			//
										  ( pTxMsg.respPGN_u32 <<8 );				//		发送PGN 为被请求的PGN,可能与数据箱配置的PGN不同（只PS)
						*/
						if ( pTxMsg.func_pf != null ) {								//
							pTxMsg.func_pf.j1939_TxDBoxCallback_tpf(pTxMsg.pfCallbackArg);					//		应答前调用数据箱回调函数以更新应答数据
						}

					}																//
					else {															// 非请求应答时
						/*
						canID.canID_u32 = pTxMsg.canID_u;							//		数据箱配置的PGN
						*/
						//canID = new J1939_CANID_ts(pTxMsg.canID_u);
						canID.setID(pTxMsg.canID_u);
					}

					dwValidPGN = canID.PGN();										// 发送数据箱的有效的PGN = DP + PF + PS

					if ( dwValidPGN == PGN_DM1 ) {									// 发送数据箱配置为发送DM1
						DM_UpdateDBoxData(pTxMsg, pFailureDM1);						//		根据DM1故障链形成DM1发送数据
					}
					else if ( dwValidPGN == PGN_DM2 ) {								// 发送数据箱配置为发送DM2
						DM_UpdateDBoxData(pTxMsg, pFailureDM2);						//		根据DM1故障链形成DM1发送数据
					}

					if ( pTxMsg.lenAct_u16 <= 8 ) {									// 短消息帧
						if ( ( canID.PF() & 0xFF ) <= PF_PRIV ) {					//		格式为PDU1
							if (  pTxMsg.respDA_u8 == (byte)DA_NONE ) {				//			周期性或调用发送数据箱函数
								if ( canID.PS() == (byte)(DA_NONE) ) {				//				初始化数据箱时未指定目标节点
									canID.setPS((byte)DA_GLOBAL);					//					广播发送
								}													//
							}														//
							else {													//			应答PGN请求时，被请求的PGN
							}														//
						}															//
						else {														//		格式为PDU2
							// 			不改变PGN
						}

						canMsg = new can_Message_ts();

						canMsg.id_u32 = canID.canID_u32;
						canMsg.format_u8 = CAN_EXD;
						canMsg.numBytes_u8 = (byte)(pTxMsg.lenAct_u16);
						/*
						memcpy(canMsg.data_au8, pTxMsg.data_pau8, pTxMsg.lenAct_u16);
						*/
						System.arraycopy(pTxMsg.data_pau8, 0,
								canMsg.data_au8, 0,
								pTxMsg.lenAct_u16);

						if ( SendFrame(canMsg, (short)0xFFFF) != 0 ) {				// 使用通用发送Slot集合中的Slot发送,J1939消息帧已放入发送FIFO队列或Slot
							if ( pTxMsg.respDA_u8 != (byte)DA_NONE ) {				//		对PGN请求的响应应答
								pTxMsg.respDA_u8 = (byte)DA_NONE;					//			标示已应答
								pTxMsg.respPGN_u32 = 0xFFFFFFFF;					//
							}
							pTxMsg.status_u16  = J1939_BOXSTATUS_INIT_DU16;			//		标示发送完成
						}															//
						else {														// 发送FIFO队列满，等待下个J1939任务周期再发送，
							if ( pTxMsg.respDA_u8 != (byte)DA_NONE ) {				//		应答失败，取消应答
								pTxMsg.status_u16  = J1939_BOXSTATUS_INIT_DU16;		//
								pTxMsg.respDA_u8 =(byte)DA_NONE;					//
								pTxMsg.respPGN_u32 = 0xFFFFFFFF;					//
							}														//
							else {													// 		周期性或调用发送失败
								if ( pTxMsg.cycle_u16 != 0 ) {						// 			周期性发送消息，
									if  ( System.currentTimeMillis() >= pTxMsg.nextTxTime_u32 ) {
										// 				在发送周期(由几个J1939任务周期组成）内都未送出，
										pTxMsg.status_u16  = J1939_BOXSTATUS_INIT_DU16;//			则取消本次发送，开始下次发送
									}												//
								}													//
								else {												// 			非周期性发送消息，
									pTxMsg.status_u16  = J1939_BOXSTATUS_INIT_DU16;//				取消本次发送
								}
							}
						}
					}
					else {															// 长消息帧
						//validPGN_u32 = PGN(canID);								// 		发送数据箱的有效的PGN = DP + PF + PS
						if ( pTxMsg.respDA_u8 == (byte)DA_NONE ) {					// 		非应答发送
							if ( (canID.PF() & 0xFF) <= PF_PRIV ) {					//			PDU1
								TP_DestAddr_u8 = canID.PS();						//				从PGN中得到目的节点地址
								if ( (canID.PS() == (byte)DA_NONE) ||				//
										(canID.PS() == (byte)DA_GLOBAL)) {				//				PGN未指定目标节点，或指定为全局发送
									canID.setPS((byte)DA_GLOBAL);					//
									TP_CtrlByte_u8 = CM_BAM;						//					使用BAM广播
								}													//
								else {												//				PGN中指定了目标节点
									TP_CtrlByte_u8 = CM_RTS;						//					使用RTS发送
								}													//
							}														//
							else {													//			PDU2
								TP_CtrlByte_u8 = CM_BAM;							//				只能使用BAM发送
								TP_DestAddr_u8 = (byte)DA_GLOBAL;					//
							}
						}
						else {														//		应答发送
							TP_DestAddr_u8 = pTxMsg.respDA_u8;						//			应答目的地址
							if ( TP_DestAddr_u8 == DA_GLOBAL ) {					//			全局应答
								TP_CtrlByte_u8 = CM_BAM;							//				使用BAM
							}														//
							else {													//			特定应答
								TP_CtrlByte_u8 = CM_RTS;							//				使用RTS
							}														//
						}

						if ( TP_CtrlByte_u8 == CM_RTS ) {							//	应使用RTS发送长消息
							if ( j1939_TxCfg.rts_PGN_u32 == 0xFFFFFFFF ) {			//		当前本节点的RTS发送通道空闲
								j1939_TxCfg.rts_PGN_u32 = dwValidPGN;				//			该发送消息成为当前使用RTS传送的长消息
								j1939_TxCfg.rts_boxNumber_u16 = (short)(i+1);		//			发送消息所在数据箱号
							}														//
							else {													//		RTS通道不空闲
								if ( ( j1939_TxCfg.rts_PGN_u32 ^ dwValidPGN ) != 0 ) {	//			同一时刻只能用RTS发送一条长消息
									continue;										//			此时暂不发送该消息
								}
							}
							if ( SendRTSFrame(										//		发送RTS帧
									pTxMsg,									//			数据箱
									TP_DestAddr_u8,							//			目标节点地址
									j1939_TxCfg.rts_PGN_u32					//			PGN
							) != 0 ){								//
								j1939_TxCfg.cm_Timeout_u32 = System.currentTimeMillis() +			//
										TIMEOUT_TXRTS_TO_RXCTS; //
								//			RTS帧已送出，必须在此时间之前收到CTS帧
								pTxMsg.counter_u16 = 0;								//			已发送总帧数
								pTxMsg.status_u16 = J1939_BOXSTATUS_RTS_DU16;		//			已发送RTS，等待接收CTS
								//tm_rtsReq = OSTime;								//

								if ( pTxMsg.respDA_u8 == (byte)DA_NONE ) {			//			非应答发送，
									pTxMsg.respDA_u8 = TP_DestAddr_u8;				//				DT帧中的目标节点地址（接收节点地址）
								}
							}
						}
						else {														//	应使用BAM广播长消息
							if ( j1939_TxCfg.bam_PGN_u32 == 0xFFFFFFFF ) {			//		当前本节点的BAM广播通道空闲
								j1939_TxCfg.bam_PGN_u32 = dwValidPGN;				//			该发送消息成为当前使用BAM广播的长消息
								j1939_TxCfg.bam_boxNumber_u16 = (short)(i+1);				//			发送消息所在数据箱号
							}														//
							else {													//		当前本节点的BAM广播通道不空闲
								if ( ( j1939_TxCfg.bam_PGN_u32 ^ dwValidPGN ) != 0 ) {	//			BAM广播通道正在广播的不是本消息
									continue;										//				同一时刻只能用BAM广播一条长消息，所以本消息的广播延后
								}													//
							}														//
							if ( SendBAMFrame(pTxMsg, j1939_TxCfg.bam_PGN_u32) != 0 ) {	//		发送BAM帧成功
								pTxMsg.counter_u16 = 0;							//			已发送帧数
								pTxMsg.status_u16 = J1939_BOXSTATUS_DT_TX_DU16;	//			已发送BAM,开始长帧数据发送
								pTxMsg.respDA_u8 = TP_DestAddr_u8;					//
								j1939_TxCfg.bam_txFrameNo_u8 = 1;					//			发送帧序号
								j1939_TxCfg.bam_minTimeout_u32 = System.currentTimeMillis() +		//			DT帧发送时间窗口:不能早于此时刻
										BAM_TXDT_TO_TXDT_MIN;	//
								j1939_TxCfg.bam_Timeout_u32 = System.currentTimeMillis() +			//							  不能晚于此此该
										BAM_TXDT_TO_TXDT_MAX;	//
							}														//
							else {													//		BAM帧发送不成功，不修改状态，下个任务周期继续发送
							}
						}

					}
					break;

				// 只发送方，超时判断
				case J1939_BOXSTATUS_RTS_DU16:										// 已发送RTS帧 或收到CTS0(连接保持帧）
				case J1939_BOXSTATUS_CTS0_DU16:										//		则等待接收CTS
				case J1939_BOXSTATUS_WAIT_EOM_DU16:									// 或等待接收EndOfMsgAck帧
					if ( i == (j1939_TxCfg.rts_boxNumber_u16 - 1) ){				//
						if ( System.currentTimeMillis() > j1939_TxCfg.cm_Timeout_u32 ) {				// 超时判断:未在规定的时间内收到CTS应答，
							if ( SendAbortFrame(									//		中止点对点传送
									j1939_TxCfg.rts_PGN_u32,					//			PGN
									(short)(pTxMsg.respDA_u8 & 0xFF),			//			接收方节点地址
									(byte)ABORT_REASON_TIMEOUT					//			中止原因=超时
							) != 0 ) {									//
								TxCloseRTS(pTxMsg);									//	传输发起方关闭RTS连接
								//tm_abortReq = OSTime;
							}
						}
					}
					break;

				// 只发送方
				case J1939_BOXSTATUS_DT_TX_DU16:										// 长帧数据发送未完成
					if ( i == (j1939_TxCfg.bam_boxNumber_u16-1) ){						// 广播长帧
						BAM_SendDTFrame(pTxMsg);
					}
					else if ( i == (j1939_TxCfg.rts_boxNumber_u16-1) ) {				// 点对点发送长帧
						RTS_SendDTFrames(pTxMsg);
					}
					break;

				default:
					break;
			}
		}

	}


	/*
	 * 构造函数：初始化变量(只实例化直属对象）
	 */
	public J1939() {
		super();
		j1939_RxCM = new J1939_RxCM_ts[256]; 					// 实例化连接管理数组及其各元素
		for ( int i=0; i<j1939_RxCM.length; i++ ) {				//
			j1939_RxCM[i] = new J1939_RxCM_ts();				//
		}

		j1939_RxCfg = new J1939_RxCfg_ts();						// 实例化接收数据箱（但无内容）
		j1939_TxCfg = new J1939_TxCfg_ts();						// 实例化发送数据箱（但无内容）
		pRxTimeoutHeader = new LinkedList<J1939_rxMsg_ts>();	// 实例化接收超时管理队列（空队列）

	};


}

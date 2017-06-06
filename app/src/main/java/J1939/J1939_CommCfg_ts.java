package J1939;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

//J1939通讯配置类
public class J1939_CommCfg_ts {
	/**
	 *  CAN通道号, 无意义
	 */
	public byte		canChnl_u8;								
	
	/**
	 * 节点地址
	 */
	public short	ownAddr_u8;								
	
	/**
	 * J1939任务的优先级
	 */
	public byte		priority_u8;							
	
	/**
	 *  J1939任务运行周期
	 */
	public short	cycleTime_u16;							
	
	/**
	 * J1939任务运行时间与运行周期的比率的最大值
	 */
	public byte		maxTime_u8;								
	
	/**
	 * J1939协议栈状态	
	 */
	public short	status_u16;
	
	/**
	 * CAN帧发送缓冲区,在can_registerTxBuf()中实例化
	 */
	public ArrayBlockingQueue<can_Message_ts> can_TxFIFO;
    //public List<can_Message_ts> can_TxFIFO;

    /**
	 * CAN帧接收缓冲区 ，can_registerRxBuf()中实例化
	 */
	public ArrayBlockingQueue<can_Message_ts> can_RxFIFO;
    //public List<can_Message_ts> can_RxFIFO;
	
	/**
	 *  本节点收到的来自于其它节点的PGN请求帧队列， 
	 *  在构造函数中中实例化
	 */
	public List<can_Message_ts> can_ReqFIFO;
	
	/**
	 * 周期性请求PGN数量。根据配置文件得到
	 */
	public byte					bReqPGNums;						
	
	/**
	 * 指向周期性请求PGN表
	 */
	public J1939_ReqCfg_ts[]	pReqPGCfg;
	
	/**
	 * 构造函数
	 */
	public J1939_CommCfg_ts() {
		super();
		can_ReqFIFO = Collections.synchronizedList(new LinkedList<can_Message_ts>());
	}						 
	
}

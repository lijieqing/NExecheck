package J1939;

//定义长帧接收管理结构
public class J1939_RxCM_ts {

	/**
	 * 正在从源节点接收点对点长帧的接收数据箱， 1 -- n 
	 */
	public short	rts_boxNumber_u16;						// 
	
	/**
	 * CTS期间期待接收的帧数
	 */
	public short	cts_Frames_u8;							//								
	
	/**
	 * CTS期间已收到的帧数
	 */
	public short	cts_rxFrames_u8;						//	
	
	/**
	 * 期待接收的下一帧帧顺序号	
	 */
	public short	cts_FrameNo_u8;							//		
	
	/**
	 * 正在从源节点接收广播长帧的接收数据箱, 1 -- m 
	 */
	public short	bam_boxNumber_u16;						//	
	
	/**
	 * 广播总帧数
	 */
	public short	bam_Frames_u8;							//	
	
	/**
	 * 期待接收的广播数据帧顺序号
	 */
	public short	bam_FrameNo_u8;							//	
	
	/**
	 * 已接收广播数据帧数
	 */
	public short	bam_rxFrames_u8;						//  

}


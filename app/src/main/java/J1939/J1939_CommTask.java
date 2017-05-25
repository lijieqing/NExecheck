package J1939;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class J1939_CommTask extends Thread {

	/**
	 * Tcp服务器(检测终端）的IP地址
	 */
	public byte[] ServerIPAddress;

	/**
	 * Tcp服务器（检测终端）侦听端口号
	 */
	public int ServerListenPort;

	/**
	 * 最近一次从服务器接收数据的时间
	 */
	public long lastRecvTime ;

	/**
	 * 临时变量
	 */
	private J1939_CANID_ts	canID;

	/**
	 * 线程终止标志
	 */
	private boolean	bStop;

	/**
	 * 生成一串字节的校验码（异或）
	 *
	 * @param pData			校验数组
	 * @param iStartPos		校验起始位置
	 * @param iLen			校验字节数
	 *
	 * @return	异或结果字节
	 */
	private byte GenVerifyByte(byte[] pData, int iStartPos, int iLen) {
		byte b = 0;
		for ( int i=0; i<iLen; i++ ) {
			b = (byte)( b ^ pData[iStartPos+i]);
		}
		return (b);
	}

	/**
	 * 处理通过TCP收到的CAN帧：将对本节点的的PGN请求帧放入 can_ReqFIFO链，将其它扩展帧放入can_RxFIFO链
	 *
	 * @param RecvBuf： 		接收缓冲区
	 * @param iByteStart:   CAN帧在接收缓冲区中的位置
	 *
	 */
	private void RecvCanMessage(byte[] RecvBuf, int iByteStart){

		byte 			bFrameInfo;
		byte 			bFrameMode;
		int				dwCanId;

		bFrameMode = RecvBuf[iByteStart+2];
		bFrameInfo = RecvBuf[iByteStart+3];

		if ( ( bFrameInfo & 0xC0 ) != 0x80 )  {
			// 忽略标准帧或非数据帧
			return;
		}

		// 处理扩展数据帧，
		if ( canID == null ) canID = new J1939_CANID_ts(0);

		dwCanId = ( (RecvBuf[iByteStart+4] << 24) & 0xFF000000 ) |
				( (RecvBuf[iByteStart+5] << 16) & 0x00FF0000 ) |
				( (RecvBuf[iByteStart+6] << 8) & 0x0000FF00 )  |
				( RecvBuf[iByteStart+7] & 0x000000FF );

		canID.setID(dwCanId);

		// 帧数据长度
		bFrameInfo &= 0x0F;

		if ( ( canID.PF() > (short)J1939.PF_PRIV ) ||
				( canID.PS() == (short)J1939.DA_GLOBAL ) ||
				( canID.PS() == J1939_Context.j1939_CommCfg.ownAddr_u8) ) {

		}
		else {

		}

		can_Message_ts rxCanMsg = new can_Message_ts();

		rxCanMsg.id_u32 = (dwCanId & 0x03FFFFFF);					// 	去掉29位ID中的优先级位
		rxCanMsg.numBytes_u8 = bFrameInfo;							//	帧数据长度
		rxCanMsg.format_u8 = J1939.CAN_EXD;							//	帧格式

		System.arraycopy(RecvBuf, iByteStart+8,						// 	读帧数据到帧数据接收缓冲区
				rxCanMsg.data_au8, 0,
				8);

		if ( canID.PF() == J1939.PF_REQPGN ) {						//		
			J1939_Context.j1939_CommCfg.can_ReqFIFO.add(rxCanMsg);	// 将读到的消息放入请求FIFO中 
		}															//
		else {														//
			if (J1939_Context.j1939_CommCfg.can_RxFIFO.size() < J1939.CAN_RXFIFO_SIZE ) {
				Log.e("FIFO",rxCanMsg+"-------未满--------");
				J1939_Context.j1939_CommCfg.can_RxFIFO.add(rxCanMsg);	// 将读到的消息放入接收FIFO中 
			}
			else {
				System.out.println("can_RxFIFO 已满");
				Log.e("FIFO",rxCanMsg+"-------已满--------");
			}
		}

	}

	/*
	 * 将待发送的CAN帧组装进发送缓冲区
	 * 
	 *@param SendBuf:  发送缓冲区
	 *@param iSendLen: 发送缓冲区已有数据长度
	 *@param canMsg:   待发送的CAN帧	
	 * 
	 */
	private void SendCanMessage(byte[] SendBuf, int iSendLen, can_Message_ts canMsg) {

		long tm;

		SendBuf[iSendLen+0] = (byte)0xFE;
		SendBuf[iSendLen+1] = (byte)0xFD;
		SendBuf[iSendLen+2] = (byte)0x00;
		SendBuf[iSendLen+3] = (byte)( 0x80|canMsg.numBytes_u8);
		SendBuf[iSendLen+4] = (byte)(canMsg.id_u32 >> 24);
		SendBuf[iSendLen+5] = (byte)(canMsg.id_u32 >> 16);
		SendBuf[iSendLen+6] = (byte)(canMsg.id_u32 >> 8);
		SendBuf[iSendLen+7] = (byte)(canMsg.id_u32);

		System.arraycopy(canMsg.data_au8, 0,
				SendBuf, iSendLen+8,
				8);

		tm = System.currentTimeMillis();
		SendBuf[iSendLen+16] = (byte)( tm >> 16 );
		SendBuf[iSendLen+17] = (byte)( tm >> 8 );
		SendBuf[iSendLen+18] = (byte)( tm );

		SendBuf[iSendLen+19] = GenVerifyByte(SendBuf, iSendLen, 19);

	}


	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void run()  {

		// TODO Auto-generated method stub

		Socket sockTcp;

		byte[] 	RecvBuf = new byte[1000];			// TCP数据接收缓冲区
		byte[]	SendBuf = new byte[1000];			// TCP发送缓冲区

		int	   	iRecvLen = 0;						// 缓冲区中有效数据（还未处理数据）字节数
		int		iByteStart = 0;						// 缓冲区中有效数据（还未处理数据）的起始位置
		int		iRecvBytes = 0;						// 单次调用 read（）读到的字节数

		int 	iSendLen = 0;
		boolean bNop;								// 空任务循环周期标志。
		// 当循环过程中接收到数据或发送了CAN帧时置为false

		try {

			sockTcp = new Socket(InetAddress.getByAddress(ServerIPAddress), ServerListenPort);
			InputStream In = sockTcp.getInputStream();
			OutputStream Out = sockTcp.getOutputStream();

			lastRecvTime = System.currentTimeMillis();
			List<can_Message_ts> msgList = J1939_Context.j1939_CommCfg.can_TxFIFO;

			// 任务循环
			while (true ) {

				bNop = true;

				// 线程中止
				if ( bStop ) break;

				if ( ( System.currentTimeMillis() - lastRecvTime  ) > 60000 ) {
					// CAN接收中断超过1分钟，通讯错误
				}

				if ( In.available() > 0 ) {

					iRecvBytes = In.read(RecvBuf, iRecvLen, 1000 - iRecvLen);
					iRecvLen += iRecvBytes;

					bNop = false;													// 标示空操作周期（）

					// 最近一次读通讯数据的时间
					lastRecvTime = System.currentTimeMillis();

					// 缓冲区数据处理循环
					while ( true ) {

						if ( iRecvLen >= 8   ) {
							if ( ( RecvBuf[iByteStart] == (byte)0xAA ) &&
									( RecvBuf[iByteStart+1] == 0 ) &&
									( RecvBuf[iByteStart+7] == (byte)0x55 ) ) {
								// CAN通道状态指示数据
								iByteStart += 8;
								iRecvLen -= 8;
							}
						}

						if (iRecvLen >= 20 ){
							if ( ( RecvBuf[iByteStart] == (byte)0xFE ) &&
									( RecvBuf[iByteStart+1] == (byte)0xFD ) &&
									( RecvBuf[iByteStart+19] == GenVerifyByte(RecvBuf,iByteStart, 19) ) ) {

								// 完整的CAN数据帧
								RecvCanMessage(RecvBuf,iByteStart);

								iByteStart += 20;
								iRecvLen -= 20;

							}
							else {
								// 坏数据帧，丢弃开头字节
								iByteStart += 1;
								iRecvLen -= 1;
							}
						}

						if ( iRecvLen < 20 ) {
							// 还未处理的字节数据小于一帧数据字节数，
							if ( iByteStart > 0 ) {
								// 将剩余数据移至缓冲区起始位置
								System.arraycopy(RecvBuf, iByteStart, RecvBuf, 0, iRecvLen);
								iByteStart = 0;
							}
							break;												// 跳出处理循环，继续接收数据
						}
					}															// 处理循环尾

				}
				else {
					// 套接口无可读数据，判断发送缓冲区状态
					while ( msgList.size() > 0  ) {
						can_Message_ts canMsg = msgList.get(0); 				// 链头消息
						msgList.remove(0);										// 	
						bNop = false;											// 标示非空操作周期
						SendCanMessage(SendBuf, iSendLen, canMsg);				// 组装发送帧到发送数据区
						iSendLen += 20;											//
						if ( iSendLen >= 100 ) {								// 发送数据区足够长了
							Out.write(SendBuf, 0, iSendLen);					// 送出  
							iSendLen = 0;										//
						}
					}
					// 发送数据区还有未发送数据则发送
					if ( iSendLen > 0 ) {
						Out.write(SendBuf, 0, iSendLen);
						iSendLen = 0;
					}
				}

				if ( bNop ) {
					// 任务循环中未收发数据，则休眠5ms, 否则继续任务循环 
					Thread.sleep(1);
				}
			}

			// 线程终止，关闭套接口
			sockTcp.close();

		}
		catch ( Exception e ) {
			System.out.println("其它错误：" + e.toString());
			e.printStackTrace();
		}

		super.run();

	}

	/*
	 * 构造函数 
	 * 
	 * 根据服务器IP地址和侦听端口号实例化本对象
	 */
	public J1939_CommTask(byte[] serverIPAddress, int serverListenPort) {
		super();
		this.ServerIPAddress = serverIPAddress;
		this.ServerListenPort = serverListenPort;
		bStop = false;
	}



}

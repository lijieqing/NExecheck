package J1939;


// J1939任务类
public class J1939_Task extends Thread {

	private boolean stop = true;

	public boolean isRunning = false;

	public J1939_Task(){
		// 启动任务之前，需要重新初始化1939的部分对象，否则数据箱有问题，无法下发命令
		J1939_Context.j1939_API = null;
		J1939_Context.j1939_CommCfg = null;
	}

	@Override
	public synchronized void start() {
		stop = false;
		super.start();
	}

	@Override
	public void run() {

		int				i;
		long			tmCycleStart;

		J1939_CommCfg_ts 	pCommCfg;
		J1939_ReqCfg_ts		pReqCfg;
		J1939_PGCfg_ts		pPGCfg;

		pCommCfg = J1939_Context.j1939_CommCfg;
		isRunning = true;
		for ( ; ; ) {
			if (isStop()){
				isRunning = false;
				return;
			}
			tmCycleStart = System.currentTimeMillis();

			for ( i=0; i< pCommCfg.bReqPGNums; i++ ) {
				pReqCfg = pCommCfg.pReqPGCfg[i];
				pPGCfg = pReqCfg.pPGCfg;
				if ( ( tmCycleStart - pReqCfg.dwLastReqTime ) >= pPGCfg.dwReqCycle ) {
					J1939_Context.j1939_API.j1939_sendRequestPGN(pPGCfg.bSA, pPGCfg.dwPGN);
					pReqCfg.dwLastReqTime = System.currentTimeMillis();
				}
			}
			if (isStop()){
				isRunning = false;
				return;
			}
			J1939_Context.j1939_API.j1939_CycAction();

			if ( ( ( System.currentTimeMillis() - tmCycleStart) * 100 / J1939_Context.j1939_Cfg.wCycle ) > 20 ) {
				// J1939任务运行时间与运行周期的比率超过设定值
			}

			try {
				long lLeftCycleTime = tmCycleStart + pCommCfg.cycleTime_u16 - System.currentTimeMillis();
				if ( lLeftCycleTime > 0) Thread.sleep( lLeftCycleTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public boolean isStop() {
		return stop;
	}

	/**
	 *  通过已配置的接收数据箱得到本节点感兴趣的节点数量及每个节点地址
	 *
	 *  @param  nodeAddr_au8[]:	指向节点地址存放区
	 *
	 *  @return 与J1939有关的CAN节点数
	 *
	 */
	private byte GetNodesAddr(byte[] nodeAddr_au8) {

		int				nodes = 0;
		int				i, j;

		J1939_rxMsg_ts	pRxMsg;
		J1939_RxCfg_ts	pRxCfg = J1939.j1939_RxCfg;

		for ( i=0; i<pRxCfg.length_u16; i++ ) {
			pRxMsg = pRxCfg.start_pas[i];
			if ( pRxMsg == null ) continue;
			if ( ( pRxMsg.lenMax_u16 > 0 )  || ( pRxMsg.lenAct_u16 > 0 ) ) {
				for ( j=0; j<nodes; j++ ) {
					if ( nodeAddr_au8[j] == (byte)(pRxMsg.canID_u & 0xFF) ) {
						break;
					}
				}
				if ( j == nodes ) {
					nodeAddr_au8[nodes] = (byte)(pRxMsg.canID_u & 0xFF);
					nodes++;
					if ( nodes >= 10 ) {						// 最多支持10个J1939节点
						break;
					}
				}
			}
		}

		return ( (byte)nodes );
	}


	/**
	 * J1939任务线程初始化：根据解析配置文件得到的PGN和SPN配置来生成数据箱
	 *
	 * @return 1 -- 初始化成功  0 -- 初始化失败
	 */
	public synchronized int Init() {

		int			wJRet;
		byte		nodes_u8;
		byte[]		nodes_addr = new byte[20];
		int			i, j;

		short	wRxDataboxNums=0, wTxDataboxNums=0, wReqDataboxNums=0;

		J1939_SPCfg_ts 	pLastSPCfg;
		J1939_Cfg_ts 	pJ1939Cfg;

		// 实例化回调函数类
		GenericRxDBoxCallback cbGenericRxDBox 	= new GenericRxDBoxCallback();
		DM1RxDBoxCallback     cbDM1RxDBox 		= new DM1RxDBoxCallback();
		GenericTxDBoxCallback cbGenericTxDBox 	= new GenericTxDBoxCallback();

		// J1939_Context.j1939_Cfg 应已在解析配置文件时实例化
		pJ1939Cfg = J1939_Context.j1939_Cfg;
		if ( pJ1939Cfg == null ) {
			System.out.println("未设置 J1939_Context.j1939_Cfg");
			return (0);
		}

		pLastSPCfg = null;

		if (J1939_Context.j1939_API == null ) {
			// 实例化 J1939_Context.j1939_API
			J1939_Context.j1939_API = new J1939();

			// 实例化静态对象 J1939_Context.j1939_CommCfg
			J1939_Context.j1939_CommCfg = new J1939_CommCfg_ts();
		}

		// 分配CAN帧发送FIFO。由于使用LinkdList,不用考虑其大小，队列无穷大
		wJRet = J1939_Context.j1939_API.can_registerTxBuf((byte)1, null, (short)0) ;
		if ( wJRet != 0  ) {
			System.out.println("未设置 J1939_Context.j1939_Cfg");
			return (1);
		}

		// 分配CAN帧接收FIFO。由于使用LinkdList,不用考虑其大小，队列无穷大
		wJRet = J1939_Context.j1939_API.can_registerRxBuf((byte)1, null, (short)0 );
		if ( wJRet != 0 ) {
			return (1);
		}

		// 统计J1939配置中接收PGN和发送PGN的数量，并将所有SP配置用单向链表链起来
		for ( i=0; i<pJ1939Cfg.wPGNums; i++ ) {
			for ( j=0; j<pJ1939Cfg.pPGCfg[i].bSPNums; j++ ) {
				if ( pLastSPCfg == null  ) {
					pJ1939Cfg.pFirstSPCfg = pJ1939Cfg.pPGCfg[i].pSPCfg[j];
				}
				else {
					pLastSPCfg.pNextSPCfg = pJ1939Cfg.pPGCfg[i].pSPCfg[j];
				}
				pLastSPCfg = pJ1939Cfg.pPGCfg[i].pSPCfg[j];
			}
			if ( pJ1939Cfg.pPGCfg[i].bDir == (byte)0 ) {
				wRxDataboxNums++;												// 接收PGN数量
				if ( ( pJ1939Cfg.pPGCfg[i].bReq != 0 ) &&
						( pJ1939Cfg.pPGCfg[i].dwReqCycle != 0 ) ) {
					wReqDataboxNums++;											// 周期性请求PGN数量
				}
			}
			else {
				wTxDataboxNums++;												// 发送PGN数量
			}
		}

		// 分配J1939消息接收数据箱( 配置的接收PG + 最多接收10个其它节点的DM1)
		wJRet = J1939_Context.j1939_API.j1939_registerRxDataboxes(null, (short)(wRxDataboxNums + 10) );
		if ( wJRet != 0 ) {
			return (1);
		}

		// 分配J1939消息发送数据箱(配置的发送PG + 本节点发送DM1和DM2数据箱）
		wJRet = J1939_Context.j1939_API.j1939_registerTxDataboxes(null, (short)(wTxDataboxNums + 2) );
		if (wJRet != 0 ){
			return (1);
		}

		// 分配自动请求PGN表, 实例化 J1939_Context.j1939_CommCfg.pReqPGCfg及其各元素
		if ( wReqDataboxNums > 0  ) {
			J1939_Context.j1939_CommCfg.bReqPGNums = (byte)wReqDataboxNums;
			J1939_Context.j1939_CommCfg.pReqPGCfg = new J1939_ReqCfg_ts[wReqDataboxNums];
			if ( J1939_Context.j1939_CommCfg.pReqPGCfg == null ) {
				return (1);
			}
			else {
				for (i=0; i<J1939_Context.j1939_CommCfg.pReqPGCfg.length; i++ ) {
					J1939_Context.j1939_CommCfg.pReqPGCfg[i] = new J1939_ReqCfg_ts();
				}
			}
		}

		wRxDataboxNums = 1;
		wTxDataboxNums = 1;
		wReqDataboxNums = 0;

		for ( i=0; i<pJ1939Cfg.wPGNums; i++ ) {

			// 静态分配PGN的数据区
			pJ1939Cfg.pPGCfg[i].pData = new byte[pJ1939Cfg.pPGCfg[i].wLen];

			if ( pJ1939Cfg.pPGCfg[i].bDir == 0 ) {					// 本节点可接收的PG

				J1939_Context.j1939_API.j1939_initRxDatabox(		// 添加接收数据箱
						wRxDataboxNums,								//		接收数据箱编号
						pJ1939Cfg.pPGCfg[i].dwPGN,					//		PGN
						pJ1939Cfg.pPGCfg[i].bSA,					//		源节点地址
						pJ1939Cfg.pPGCfg[i].dwRate,					//		周期
						0,											//		首次接收时限要求
						pJ1939Cfg.pPGCfg[i].wLen,					//		数据长度
						pJ1939Cfg.pPGCfg[i].pData,					//		数据区
						cbGenericRxDBox,							// 		接收回调函数
						pJ1939Cfg.pPGCfg[i]							//		回调函数调用参数: 与数据箱关联的PG配置对象
				);

				pJ1939Cfg.pPGCfg[i].wDBNumber = wRxDataboxNums; 	// 本PG对应的接收数据箱号

				if ( ( pJ1939Cfg.pPGCfg[i].bReq != 0 )  &&			// 填充需要周期性请求的PGN表
						( pJ1939Cfg.pPGCfg[i].dwReqCycle > 0 ) ) {		//
					J1939_Context.j1939_CommCfg.pReqPGCfg[wReqDataboxNums].pPGCfg = pJ1939Cfg.pPGCfg[i];
					wReqDataboxNums++;
				}

				wRxDataboxNums++;

			}
			else {													// 本节点可发送PG
				J1939_Context.j1939_API.j1939_initTxDatabox( 		// 添加发送数据箱
						wTxDataboxNums,								//		发送数据箱编号
						pJ1939Cfg.pPGCfg[i].dwPGN,					//		PGN
						pJ1939Cfg.pPGCfg[i].bPrio, 					//		优先级
						pJ1939Cfg.pPGCfg[i].dwRate,					//		发送周期
						0, 											//		首次发送时间提前量（避免多个发送数据箱同时发送）
						pJ1939Cfg.pPGCfg[i].wLen,					//		数据长度
						pJ1939Cfg.pPGCfg[i].pData,					//		数据区
						cbGenericTxDBox,							// 		发送回调函数
						pJ1939Cfg.pPGCfg[i]							//		回调函数调用参数
				);
				pJ1939Cfg.pPGCfg[i].wDBNumber = wTxDataboxNums; 	// 本PG对应的发送数据箱号
				wTxDataboxNums++;
			}
		}

		// 为每个接收源节点添加DM1接收数据箱
		nodes_u8 = GetNodesAddr(nodes_addr);						// 根据接收数据箱配置得到J1939节点数量
		if ( nodes_u8 > 10 ) nodes_u8 = 10; 						// 最多支持10个节点

		for ( i=0; i<nodes_u8; i++) {
			J1939_Context.j1939_API.j1939_initDiagnostic(
					nodes_addr[i], 								// 源节点地址
					J1939.J1939_LIST_DM1_DU16, 					// 接收当前激活的故障码
					null, 										// 自动分配存贮区
					wRxDataboxNums,								// 接收该节点DM1消息的数据箱
					cbDM1RxDBox,								// 回调函数
					(int)wRxDataboxNums							// 数据箱编号
			);
			wRxDataboxNums++;
		}

		// 分配本节点故障表
		J1939_Context.j1939_API.j1939_initFailureList(
				null, 										// ptr to failure list (array)
				(short)50,             						// failure list size (max nbr failures)
				(short)4,              						// eeprom page nbr for DM2 failure list
				(short)0,                    				// eeprom start index for DM2 failure list
				wTxDataboxNums,         					// TX box nbr for sending the DM1 failures
				(short)(wTxDataboxNums+1)					// TX box nbr for sending the DM2 failures
		);

		// 设置J1939任务
		J1939_Context.j1939_API.j1939_initComm(
				(byte)(pJ1939Cfg.bCanCh - 1), 						// 	CAN通道号, 0 -- n
				pJ1939Cfg.bNodeAddr,								//	CAN地址
				pJ1939Cfg.bTaskPrio,								//	优先级，
				(short)(pJ1939Cfg.wCycle),							//  J1939任务周期
				(byte)20											//  最大的J1939任务负载率
		);

		return (0);													// 返回初始化结果
	}


}

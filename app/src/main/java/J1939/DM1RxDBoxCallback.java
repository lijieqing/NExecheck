package J1939;

public class DM1RxDBoxCallback implements IRxDBoxCallback {

	public DM1RxDBoxCallback() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object j1939_RxDBoxCallback_tpf(Object obj) {
		// TODO Auto-generated method stub

		short 				wDTCNums;				// 故障数量 
		short 				wRxDBoxNumber_DM1;		// DM1接收数据箱编号
		J1939_dtcList_ts 	pDTCList;				// 接收到的活动故障表
		J1939_dtc_ts		pDTC;
		J1939_rxMsg_ts		pRxMsg;					// 故障接收数据箱

		J1939_SPCfg_ts 		pSPCfg;					//  = gHMICtx.tsJ1939Cfg.pFirstSPCfg;
		J1939_PGCfg_ts 		pPGCfg;					//
		J1939_DTCfg_ts		pDTCfg;					//
		int					i, j;
		
		wRxDBoxNumber_DM1 = ((Integer)obj).shortValue();
		
		// 读DM1接收数据箱状态
		// j1939_getRxDataboxStatus(wRxDBoxNumber_DM1, &wDTCNums);

		// 从接收数据箱中得到故障表
		pRxMsg = J1939.j1939_RxCfg.start_pas[wRxDBoxNumber_DM1 - 1];
		wDTCNums =  (short)(pRxMsg.counter_u16);
		 
		pDTCList = (J1939_dtcList_ts)(pRxMsg.data_pau8);
		pSPCfg = J1939_Context.j1939_Cfg.pFirstSPCfg;
				
		// 老故障是否出现在新故障表中	
		while ( pSPCfg != null ) {															// 遍历该源节点的SPN
			pPGCfg = pSPCfg.pPGCfg;
			if ( ( pPGCfg.bSA==(byte)(pRxMsg.canID_u)) && ( pSPCfg.pLastDTC != null ) ) {	// 该源节点SPN有上次故障
				for ( i=0; i<wDTCNums; i++ ) {												// 在本次故障表中查找该上次故障	
					pDTC = pDTCList.dtc_as[i];												//
					if ( ( pDTC.spn_u32 == pSPCfg.dwSPN ) && 								//		该上次故障出现在本次故障表中	
				   	 	 ( pDTC.fmi_u8 == pSPCfg.bFMI ) ) {									//
						pDTC.spn_u32 = 0xFFFFFFFF;											//		标记该DTC上次出现过，本次不用处理
						break;																//		
					}																		//
				}																			//
				if ( ( wDTCNums == 0 ) || ( i == wDTCNums ) ) {								//		该上次故障未出现在本次故障表中时，
					//ClearDD_Bit(pSPCfg->pLastDTC);										//			清除该上次故障的指示位
					pSPCfg.pLastDTC = null;													//
					pSPCfg.bFMI = 0;														//			 			
					// RefreshSPRefWidgt(pSPCfg);											//		刷新与SPN相关联的组件的显示 
				}																			//
			}																				//
			pSPCfg = pSPCfg.pNextSPCfg;														// 继续遍历SPN单向链表
		}		 																			//
		
		// 处理本次故障表中新增加的故障项
		for ( i=0; i<wDTCNums; i++ ) {														// 遍历本次故障表	
			
			pDTC = pDTCList.dtc_as[i];														//
			if ( pDTC.spn_u32 == 0xFFFFFFFF ) continue;										//  	跳过上次出现过的（未改变）的故障项
			
			// 定位新增加故障项所在的SP配置
			pSPCfg = J1939_Context.j1939_Cfg.pFirstSPCfg;									// SPN单向链表头
			while ( pSPCfg != null  ) {														// 遍历该源节点的SPN
				pPGCfg = pSPCfg.pPGCfg;
				if ( ( pPGCfg.bSA == (byte)(pRxMsg.canID_u) ) && 							//	
				   	 ( pSPCfg.dwSPN == pDTC.spn_u32 ) ) {									// 		故障SPN属于该源节点
					for ( j=0; j<pSPCfg.bDTCNums; j++ ) {									//		检查该SPN是否接收FMI
						pDTCfg = pSPCfg.pDTCfg[j];											//
						if ( pDTCfg.bFMI == pDTC.fmi_u8 ) {									//			接收
							//SetDD_Bit(pDTCfg);											//			置故障状态位
							pSPCfg.pLastDTC = pDTCfg;										//			在SP配置中记录当前故障信息
							pSPCfg.bFMI = pDTC.fmi_u8;										//
							/*
							if ( pDTCfg->wWarnByIdx != 0xFFFF )	{										// 有关联报警虚拟DI通道，
								gHMICtx.pDIData[pDTCfg->wWarnByIdx].wWarnDescID = pDTCfg->wDescId;		//   	修改报警描述串
								gHMICtx.pDIData[pDTCfg->wWarnByIdx].wWarnIconID = pDTCfg->wIconId;		//		修改报警图标
							}
							RefreshSPRefWidgt(pSPCfg);										//			刷新与SPN相关联的组件
							*/																//
							break;															//			
						}																	//
					}																		//
					break;																	//
				}																			//
				pSPCfg = pSPCfg.pNextSPCfg;													//  	
			}																				//
		}	
																							//
		return (null);
	}


}


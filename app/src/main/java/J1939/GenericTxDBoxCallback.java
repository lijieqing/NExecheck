package J1939;

import java.util.Arrays;

/**
 * 实现 发送数据箱回调接口的类
 * 
 * @author wuwb
 * 
 */
public class GenericTxDBoxCallback implements ITxDBoxCallback {

	public GenericTxDBoxCallback() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 获取变量值。根据变量类型取值（非浮点类型值转换为浮点类型值）
	 * 
	 * @param wIdx：		变量索引
	 * @param fValue：	变量值
	 */
	private float GetDataVarValue(short wIdx) {
	
		J1939_DataVar_ts 	pDD;
		VARTYPE				vt;
		float				fValue;
		
		pDD = J1939_Context.j1939_DataVarCfg[wIdx];			// SPN对应的数据变量对象
		if ( pDD == null ) return (0.0f);
		
		vt = pDD.bDataType;
		
		if ( vt == VARTYPE.FLOAT )
			fValue = pDD.getFloatValue();
		else
			fValue = (float)(pDD.getValue());
		
		return (fValue);
	}

	
	@Override
	public Object j1939_TxDBoxCallback_tpf(Object obj) {

		long				dwValue=0;							// 
		int 				i, j, k;							// 循环控制变量
		
		short 				wSByte;								//
		byte				bBytes, bSBit, bBits;				//
		
		J1939_PGCfg_ts		pPGCfg = (J1939_PGCfg_ts)obj;		// 回调函数参数为PG配置对象
		J1939_SPCfg_ts		pSPCfg; 							// 

		J1939_DataVar_ts 	pDD;								// 遍历SP时，SP关联的数据变量对象
		float				fValue;								//

		int iLastSPPackedBytes = 0;								// 上个遍历的SP的组装字节数
		int iLastSPStartByte = 1; 								// 上个遍历的SP组装位置
		int iValueNums;											// 遍历的SP对应变量的值数量
		
		// 装配前将发送数据区清0
		Arrays.fill(pPGCfg.pData, (byte)0);
				
		for ( i=0; i< pPGCfg.bSPNums; i++) {							// 将各SPN值装配进PGN数据区
		
			pSPCfg = pPGCfg.pSPCfg[i];									// 该PGN的第i个SPN配置											
			if ( pSPCfg == null ) continue;								// 跳过未定义的SPN
			if ( pSPCfg.wRefDataIdx < 0 ) continue;						// 跳过未关联变量的SPN
		
			// 未指定该SP的起始字节，则紧挨着上个SP的数据装配
			if ( ( iLastSPPackedBytes > 0 ) && ( pSPCfg.wStartByte == 0 ) ) {
				pSPCfg.wStartByte = (short)(iLastSPStartByte + iLastSPPackedBytes);
			}
			
			pDD = J1939_Context.j1939_DataVarCfg[pSPCfg.wRefDataIdx];	// SPN对应的数据变量对象
			if ( pDD == null ) continue;								// 跳过未定义的数据变量
			
			if ( pDD.wRows == 0 ) {										// SP对应简单变量 
				iValueNums = 1;											//		只需要组装一个值	
			}															//		
			else {														// SP对应向量
				iValueNums = pDD.wRows;									//		需要组装多个值
			}
			
			wSByte = (short)(pSPCfg.wStartByte - 1);					// 起始字节（0基。配置中为1基）
			bSBit = (byte)(pSPCfg.bStartBit - 1);						// 超始位（0基。配置中为1基）
							
			if ( pSPCfg.bBytes == 0 ) {									// 按位取值
				bBits = pSPCfg.bBits;									// 位数
				bBytes = (byte)( ( bBits + bSBit + 7 ) / 8);			// 涉及字节数 
			}
			else {														// 按字节取值
				bBytes = pSPCfg.bBytes;									// 字节数
				bBits = (byte)(bBytes * 8);								// 涉及位数
			}
			
			iLastSPPackedBytes = 0;										// 本SPN已装配字节数清0
			for ( k=0; k<iValueNums; k++) {
				
				fValue = pDD.getFloatValueByRow((short)k);				// 取数据变量的第k个位置的值。对简单变量，不同的位置返回相同值。
				
				fValue -= pSPCfg.fOffset;								// 根据偏移量和精度转换为原始值
				if ( pSPCfg.fRes != 0 )  fValue /= pSPCfg.fRes;			//

				dwValue = ((long)fValue) << bSBit;						// 低字节 
			
				for ( j=0; j<bBytes; j++ )	{							// 将数据内容按小端序装配到各涉及字节
					pPGCfg.pData[wSByte+j] |= ((byte)dwValue);			//
					dwValue >>= 8;										//
				}
				
				wSByte += bBytes;										// 向量的下一个值的装配位置
				iLastSPPackedBytes += bBytes;							// 本SP已装配字节数
			
			}	
			
			iLastSPStartByte = pSPCfg.wStartByte;						// 本SP的装配起始位置
		}	
		
		return (null);
	}

}

package J1939;

import android.util.Log;

import com.kstech.nexecheck.utils.Globals;

public class GenericRxDBoxCallback implements IRxDBoxCallback {

	/**
	 * 设置变量值。根据变量类型保存给定的浮点类型的变量值（存为浮点数据或整数）
	 *
	 * @param wIdx：		变量索引
	 * @param fValue：	变量值
	 */
	private void SetDataVarValue(short wIdx, float fValue) {

		J1939_DataVar_ts 	pDD;
		VARTYPE		vt;

		pDD = J1939_Context.j1939_DataVarCfg[wIdx];
		vt = pDD.bDataType;

		if ( vt == VARTYPE.FLOAT ){
			pDD.setFloatValue(fValue);
		}
		else{
			pDD.setValue((long)fValue);
		}
		// TODO: 2017/5/24  监听回调
		pDD.notifyListener(fValue);
		Log.e("GenericRxDBoxCallback", "fValue");
	}


	// 构造函数
	public GenericRxDBoxCallback() {
		// TODO Auto-generated constructor stub
	}

	// 接口方法实现
	@Override
	public Object j1939_RxDBoxCallback_tpf(Object obj) {

		long			dwValue;
		float			fValue;
		short 			i, j;

		short 			wSByte;
		byte			bBytes,  bSBit, bBits;

		if ( obj == null ) return (null);

		J1939_PGCfg_ts pPGCfg = (J1939_PGCfg_ts)obj;
		J1939_SPCfg_ts pSPCfg;

		for ( i=0; i< pPGCfg.bSPNums; i++) {

			pSPCfg = pPGCfg.pSPCfg[i];

			if ( pSPCfg == null ) continue;
			if ( pSPCfg.wRefDataIdx < 0 ) continue;

			wSByte = (short) (pSPCfg.wStartByte - 1);		// 起始字节（0基。配置中为1基）
			bSBit = (byte) (pSPCfg.bStartBit - 1);			// 超始位（0基。配置中为1基）
			dwValue = 0L;

			if ( pSPCfg.bBytes == 0 ) {						// 按位取值
				bBits = pSPCfg.bBits;						// 位数
				bBytes = (byte) ((bBits+bSBit+7)/8);		// 涉及字节数
			}
			else {
				bBytes = pSPCfg.bBytes;						// 按字节取值
				bBits = (byte)(bBytes << 3 );				//
			}

			for ( j= (short)(bBytes-1); j>=0; j--) {		// 将所涉及的字节按小端序列组装成32位字
				dwValue =  (dwValue << 8 ) | (pPGCfg.pData[wSByte+j] & 0xFF);
			}

			dwValue >>= ( bSBit);							// 将起始位移至最低位
			if ( bBits < 32 ) {
				dwValue &= ( ( 1 << bBits ) - 1 );			// 将前面多出的位清0
			}

			fValue = pSPCfg.fRes * ( (float)dwValue ) + pSPCfg.fOffset;

			SetDataVarValue(pSPCfg.wRefDataIdx, fValue);

		}

		if ( pPGCfg.dwPGN >= (0xFF20 & 0x0000FFFF) &&
			 pPGCfg.dwPGN < (0xFF80 & 0x0000FFFF) ) {
			J1939_DataVar_ts lastCheckValueDSItem = Globals.getModelFile()
					.getDataSetVO().getLastCheckValueDSItem();
			lastCheckValueDSItem.setValue(pPGCfg.dwPGN);
//			if(pPGCfg.dwPGN == 0xFF2F){
////				Log.e("PGNP", "-----PGN---re value-- s t a r t -----PGN-----");
////				for (int k = 0; k < pPGCfg.pData.length; k++) {
////					Log.e("PGNP", "-----PGN-----"+pPGCfg.dwPGN+"-----pPGCfg.pData["+k+"]----"+pPGCfg.pData[k]);
////				}
//				Globals.list2F.add(J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[0].wRefDataIdx].getFloatValue()+"");
////				Log.e("PGNP", "-----PGN----- e  n  d -----PGN-----");
//			}
//			if(pPGCfg.dwPGN == 0xFF30){
//				Globals.list30.add(J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[0].wRefDataIdx].getFloatValue()+"");
//			}
//			if(pPGCfg.dwPGN == 0xFF31){
//				Globals.list31.add(J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[0].wRefDataIdx].getFloatValue()+"");
//			}
//			if(pPGCfg.dwPGN == 0xFF32){
//				Globals.list32.add(J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[0].wRefDataIdx].getFloatValue()+"");
//			}
//			if(pPGCfg.dwPGN == 0xFF33){
//				Globals.list33.add(J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[0].wRefDataIdx].getFloatValue()+"");
//			}

			// 发送测量值接受确认帧（0xFF83）
			J1939_Context.j1939_API.j1939_sendDatabox((short)(Globals.getModelFile().j1939PgSetVO
					.getPg(0xFF83).wDBNumber));
		}

		if (pPGCfg.dwPGN == 0xFF81 ) {
			// 发送应答状态接受确认帧（0xFF82）
			J1939_Context.j1939_API.j1939_sendDatabox((short)(Globals.getModelFile().j1939PgSetVO
					.getPg(0xFF82).wDBNumber));
			Log.e("PGN", "-----PGN--re-81-- s t a r t -----PGN-----");
			for (int k = 0; k < pPGCfg.pData.length; k++) {
				Log.e("PGN", "-----PGN-----"+pPGCfg.dwPGN+"-----pPGCfg.pData--"+k+"--"+pPGCfg.pData[k]);
			}
			Log.e("PGN", "-----PGN----- e  n  d -----PGN-----");
		}
//测试是否丢帧代码
//		if ( pPGCfg.dwPGN == 0xFF2E ) {
//			Globals.progress++;
//			// Log.e("0xFF2E", "0xFF2E---------------"+Globals.progress);
//			long dwFrameSNO = J1939_Context.j1939_DataVarCfg[pPGCfg.pSPCfg[1].wRefDataIdx].getValue();
//			if ( Globals.dwExpectedFrameSNO != dwFrameSNO ) {
//				for ( long l=Globals.dwExpectedFrameSNO; l<dwFrameSNO; l++) {
//					Log.e("0xFF2E", "丢失第"+ l + "帧" );
//				}
//			}
//			Globals.dwExpectedFrameSNO =  dwFrameSNO + 1;
//			if ( (dwFrameSNO % 1000) == 0 ) {
//				Log.e("0xFF2E", "0xFF2E------dwFrameSNO---------"+dwFrameSNO);
//				Log.e("0xFF2E", "0xFF2E---------------"+Globals.progress);
//			}
//		}

		return (null);
	}

}


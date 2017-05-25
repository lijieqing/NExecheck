package J1939;

// 接收数据箱
public class J1939_RxCfg_ts {
	short  				length_u16;						// J1939接收数据箱个数
	J1939_rxMsg_ts[] 	start_pas;						// 指向接收数据箱数组

	public J1939_RxCfg_ts( ) {
		super();
		this.length_u16 = 0;							// 接收数据箱数量 
		this.start_pas = null;							// 接收数据箱数组 
	}
	
}

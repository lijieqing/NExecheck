package J1939;

//定义当前活动的故障诊断表结构
public class J1939_dtcList_ts 
{
	J1939_lamp_ts	lamp_s;						// 当前的指示灯状态
	J1939_dtc_ts[]	dtc_as;						// 当前活动的故障表（最多20项）
	
	public J1939_dtcList_ts() {
		dtc_as = new J1939_dtc_ts[20];			//	实例化数组对象
		lamp_s = new J1939_lamp_ts();			//
		for (int i=0; i<dtc_as.length; i++)		//  实例化数组元素
			dtc_as[i] = new J1939_dtc_ts();		
	}					

} 

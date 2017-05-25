package J1939;

//定义故障结构
public class J1939_failure_ts {
	
	J1939_dtc_ts	dtc_s;					// 故障诊断码
	J1939_lamp_ts	lamp_s;					// 指示灯状态
	
	public J1939_failure_ts() {
		super();
		dtc_s = new J1939_dtc_ts();
		lamp_s = new J1939_lamp_ts();
	}
	
} ;


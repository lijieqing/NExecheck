package J1939;

public class J1939_DTCfg_ts {

	public J1939_SPCfg_ts pSPCfg; 		// 关联SP
	public byte 	bFMI; 				// FMI 代码
	public byte 	bIndDataBit; 		// 置位位号
	public short 	wIndDataIdx; 		// 出现此FMI码时在此数据（配置参数）中置位
	public short 	wDescId; 			// 此故障的描述信息串的Id
	public short 	wIconId; 			// 此故障的显示图标
	public short	wWarnByIdx; 		// 此故障通过此虚拟DI通道报警
	
	public J1939_DTCfg_ts() {
		super();
	}
	
}

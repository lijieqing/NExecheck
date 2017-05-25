package J1939;

/**
 * J1939 CAN-ID 类。
 * 
 *   CAN-ID由29位组成，用一个有符号整型数表示，由DP、PF、PS、SA四个字节组成
 * 
 * @author wuwb
 *
 */
public class J1939_CANID_ts {

	int			canID_u32;

	public byte DP() {
		return ( (byte) ( canID_u32 >> 24 ) );
	}

	public void setDP(byte dp_u8) {
		canID_u32 = ( canID_u32 & 0x00FFFFFF ) | ( ( dp_u8 & 0xFF ) << 24 ); 
	}
	
	public byte PF() {
		return ( (byte) ( canID_u32 >> 16 ) );
	}

	public void setPF(byte pf_u8) {
		canID_u32 = ( canID_u32 & 0xFF00FFFF ) | ( ( pf_u8 & 0xFF ) << 16 ) ; 
	}
	
	public byte PS() {
		return ( (byte) ( canID_u32 >> 8 ) );
	}

	public void setPS(byte ps_u8) {
		canID_u32 = ( canID_u32 & 0xFFFF00FF ) |  ( ( ps_u8 & 0xFF) << 8 ); 
	}

	public void setID(int canID_u32) {
		this.canID_u32 = canID_u32; 
	}

	public byte SA() {
		return ( (byte) canID_u32 );
	}

	public void setSA(byte sa_u8) {
		canID_u32 = ( canID_u32 & 0xFFFFFF00 ) | (sa_u8 & 0xFF) ; 
	}

	public J1939_CANID_ts(int canID_u32) {
		super();
		this.canID_u32 = canID_u32;
	}
	
	public int PGN() {
		return ( ( canID_u32 >> 8  ) & 0x3FFFF );
	}
	
}

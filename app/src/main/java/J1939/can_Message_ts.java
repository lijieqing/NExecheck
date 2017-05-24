package J1939;

public class can_Message_ts {

	public int		id_u32;						// identifier of the message
	public byte		format_u8;					// format which will be transmitted, either STD, XTD
	public byte		numBytes_u8;				// number of data bytes which will be transmitted
	public byte[]	data_au8;					// data which will be transmitted

	/**
	 * 默认构造函数，为CAN帧数据分配存贮空间
	 */
	public can_Message_ts() {
		data_au8 = new byte[8];
	}

	/**
	 *  指定CAN帧内容的构造函数
	 */
	public can_Message_ts(int id_u32, byte format_u8, byte numBytes_u8, byte[] data_au8) {

		//super();
		this.data_au8 = new byte[8];								// 总是分配新的字节数组对象而不是引用参数

		this.id_u32 = id_u32;										// CAN-ID
		this.format_u8 = format_u8;									// CAN帧类型	
		this.numBytes_u8 = numBytes_u8;								// CAN帧数据字节数
		
		for ( int i=0; i<data_au8.length; i++) {					// 复制CAN帧数据,最多8字节
			if ( i>= 8 ) break; 
			this.data_au8[i] = data_au8[i];
		}
	}
	
};

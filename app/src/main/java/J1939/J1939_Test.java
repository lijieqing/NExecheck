package J1939;



public class J1939_Test {

	private static void ChangeInt(Integer objInt){
		objInt = 6;
		System.out.print(objInt);
	}

	private void TestArrayType() {

		int[] aInt = new int[3];
		short[] aShort = new short[3];
		float[] aFloat = new float[3];
		byte[] aByte = new byte[3];
		long[] aLong = new long[3];

		//System.out.println("VARTYPE.BYTE="+VARTYPE.BYTE.ordinal() );

		System.out.print("aInt.getClass()：" + aInt.getClass());
		System.out.print("aInt.getClass().getName()："+ aInt.getClass().getName());
		System.out.print("aInt.getClass().getComponentType(): " + aInt.getClass().getComponentType());
		System.out.print("aInt.getClass().getComponentType().getName()：" + aInt.getClass().getComponentType().getName());

		System.out.print("aShort.getClass()："+ aShort.getClass());
		System.out.print("aShort.getClass().getName()："+ aShort.getClass().getName());
		System.out.print("aShort.getClass().getComponentType()：" + aShort.getClass().getComponentType());

		System.out.print("aShort.getClass().getComponentType().getName()："+ aShort.getClass().getComponentType().getName());
		System.out.print("aFloat.getClass()："+ aFloat.getClass());
		System.out.print("aFloat.getClass().getName()："+ aFloat.getClass().getName());

		System.out.print("aFloat.getClass().getComponentType()："+ aFloat.getClass().getComponentType());
		System.out.print("aFloat.getClass().getComponentType().getName()：" + aFloat.getClass().getComponentType().getName());

		System.out.print("aByte.getClass()："+ aByte.getClass());
		System.out.print("aByte.getClass().getName()：" + aByte.getClass().getName());
		System.out.print("aByte.getClass().getComponentType()：" + aByte.getClass().getComponentType());


		System.out.print("aLong.getClass()："+ aLong.getClass());
		System.out.print("aLong.getClass().getName()："+ aLong.getClass().getName());
		System.out.print("aLong.getClass().getComponentType()："+ aLong.getClass().getComponentType());

	}

	private void TestSimpleTypeConvert() {

		int 	i;
		byte 	b;
		short 	si;

		b = (byte)0xFE;
		i = 0xFE;
		si = 0xFE;

		System.out.println(" b=(byte)0xFE, i=0xFE, si=0xFE" );

		System.out.println("b == i ?  " + (b==i));
		System.out.println("si == i ?  " + (si==i));

		System.out.println("b == (byte)i ?  " + (b==(byte)i));
		System.out.println("(int)b == i ?  " + ((int)b == i));
		System.out.println("(int)(b&0xFF) == i ?  " + ((int)(b&0xFF) == i));

		System.out.println("i=" + Integer.toBinaryString(i));
		System.out.println("b=" + Integer.toBinaryString(b));
		System.out.println("si=" + Integer.toBinaryString(si));

		si = b;
		System.out.println("si=b" + Integer.toBinaryString(si));

		System.out.println(" b<<8 = " + (b<<8) );
		System.out.println(" (b<<8)|b = " + ((b<<8)|b) );
		System.out.println(" ((b&0xFF)<<8)|(b&0xFF) = " + (((b&0xFF)<<8)|(b&0xFF)) );

	}

	public static void main(String[] args) {

		// 实例化 J1939_Context.j1939_CommCfg
		/*
		J1939_Context.j1939_CommCfg  = new J1939_CommCfg_ts();
		J1939_Context.j1939_CommCfg.canChnl_u8 = 1;
		J1939_Context.j1939_CommCfg.ownAddr_u8 = 0x0A;
		J1939_Context.j1939_CommCfg.bReqPGNums = 0;
		J1939_Context.j1939_CommCfg.cycleTime_u16 = 10;
		J1939_Context.j1939_CommCfg.priority_u8 = 6;
		J1939_Context.j1939_CommCfg.maxTime_u8 = 30;
		J1939_Context.j1939_CommCfg.status_u16 = IJ1939_API.J1939_CASTATUS_NONE_DU160;

		J1939_Context.j1939_CommCfg.can_RxFIFO = new LinkedList<can_Message_ts>();
		J1939_Context.j1939_CommCfg.can_TxFIFO = new LinkedList<can_Message_ts>();
		*/

		// 实例化J1939配置
		J1939_Context.j1939_Cfg = new J1939_Cfg_ts();				// 配置对象
		J1939_Context.j1939_Cfg.bCanCh = (byte)1;					// 使用的CAN通道
		J1939_Context.j1939_Cfg.bNodeAddr = 0x0A;					// 本节点地址
		J1939_Context.j1939_Cfg.bTaskPrio = 100;					// J1939任务优先级
		J1939_Context.j1939_Cfg.wCycle = 5;							// J1939任务周期（单位为ms）

		J1939_Context.j1939_Cfg.wPGNums = 10;						// 配置的PGN数量
		J1939_Context.j1939_Cfg.pPGCfg = new J1939_PGCfg_ts[10];	// 实例化PGN配置数组对象（数组元素未实例化）

		//														dwPGN  dwRate  dwReqCyc bType    bDir     bPrio     bSA         wLen      bReq     bSPNums  pSPCfg
		//														  |     |       |       |        |        |         |           |          |        |        |
		// 接收PGN配置，实例化PGN数组中各接收PGN元素
		J1939_Context.j1939_Cfg.pPGCfg[0] = new J1939_PGCfg_ts(0x120A, 1000,    0,      (byte)1, (byte)0, (byte)6, (byte)0x0B, (short)8,  (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[1] = new J1939_PGCfg_ts(0x130A, 1000,    0,      (byte)1, (byte)0, (byte)6, (byte)0x0B, (short)30, (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[2] = new J1939_PGCfg_ts(0xF101, 1000,    0,      (byte)1, (byte)0, (byte)6, (byte)0x0B, (short)8,  (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[3] = new J1939_PGCfg_ts(0xF102, 1000,    0,      (byte)1, (byte)0, (byte)6, (byte)0x0B, (short)40, (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[4] = new J1939_PGCfg_ts(0xFF02, 0,       2500,   (byte)1, (byte)0, (byte)6, (byte)0x0B, (short)80, (byte)1, (short)0, null);

		// 发送PGN配置，实例化PGN数组中各发送PGN元素
		J1939_Context.j1939_Cfg.pPGCfg[5] = new J1939_PGCfg_ts(0x020B, 1000,    0,      (byte)1, (byte)1, (byte)6, (byte)0x0B, (short)8,  (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[6] = new J1939_PGCfg_ts(0x030B, 1000,    0,      (byte)1, (byte)1, (byte)6, (byte)0x0B, (short)30, (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[7] = new J1939_PGCfg_ts(0xF001, 1000,    0,      (byte)1, (byte)1, (byte)6, (byte)0,    (short)8,  (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[8] = new J1939_PGCfg_ts(0xF002, 1000,    0,      (byte)1, (byte)1, (byte)6, (byte)0,    (short)30, (byte)0, (short)0, null);
		J1939_Context.j1939_Cfg.pPGCfg[9] = new J1939_PGCfg_ts(0xFF02, 0,       0,      (byte)1, (byte)1, (byte)6, (byte)0,    (short)30, (byte)0, (short)0, null);

		//实例化J1939协议任务并运行
		J1939_Task j1939ProtTask = new J1939_Task();				// 创建 J1939协议任务对象
		j1939ProtTask.Init();										// J1939协议任务初始化
		j1939ProtTask.start();										// 启动J1939协议任务

		// 实例化J1939收发任务并运行。此任务必须 在J1939协议任务启动后
		byte[] serverIP = new byte[4];
		serverIP[0] = (byte)192;									// CAN-WIFI的IP地址
		serverIP[1] = (byte)168;									//
		serverIP[2] = (byte)0;										//
		serverIP[3] = (byte)178;									//

		J1939_CommTask j1939CommTask = new J1939_CommTask(serverIP, 4001);
		// 实例化J1939通讯任务，
		j1939CommTask.start();										// 启动J1939收发任务

		System.out.println("main() exit. ");						// 主线程退出，但1939协议任务线程和J1939通讯任务线程仍在运行

	}

}

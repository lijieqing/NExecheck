package J1939;


enum VARTYPE {
	BYTE, WORD, DWORD, SHORT, INT, FLOAT
}

/**
 * 数据变量类，检验配置文件中的的每个<DSItem>标记产生一个本类的实例对象
 */
public class J1939_DataVar_ts {

	/**
	 * 数据变量值类型， 对应配置文件中
	 * <DSItem>标记的"DataType"属性值（BYTE、WORD、DWORD、CHAR、SHORT、INT、FLOAT）
	 */
	public VARTYPE bDataType;

	/**
	 * 简单类型（非向量）的数据变量的值，对应配置文件中 <DSItem>标记的"Value"属性值
	 * 此long类型属性的地址空间可存放BYTE、WORD、SHORT、INT、DWORD、FLOAT类型的值
	 */
	private long unValue;

	/**
	 * 向量长度，对应 <DSItem>标记的”Rows"属性值 为0表示本变量为简单类型变量，变量值为unValue;
	 * 非0表示本变量为向量，变量值为vValue
	 */
	public short wRows;

	/**
	 * 变量索引，对应 <DSItem>标记的”IndexBy"属性值，此属性值为某变量名称 该索引所指变量的值作为访问本向量元素的索引
	 */
	private short wRowIndexBy;

	/**
	 * 向量类型的数据变量值，对应 <DSItem>标记的所有<Data>子标记的”Value"属性值 向量元素的值类型由bDataType确定
	 */
	private Object vValue;

	/**
	 * 数据变量的ID, 对应配置文件中的 <DSItem>标记的"ID"属性值
	 */
	public short wDataID;

	/**
	 * 数据变量的描述在字符串资源表中的索引，对应配置文件中 <DSItem>标记的"RemarkID"属性值
	 */
	public short wRemarkStrID;

	/**
	 * 数据变量名称(简称），对应配置文件中 <DSItem>标记的"Name"属性值。配置文件中通过此名称来引用数据变量
	 */
	public String sName;

	/**
	 * 数据变量值的单位， 对应配置文件中 <DSItem>标记的"Unit"属性值
	 */
	public String sUnit;

	/**
	 * 数据变量值显示精度（小数点后显示位数），对应配置文件中 <DSItem>标记的"DecLen"属性值
	 */
	public byte bDataDec;

	/**
	 * 变量索引，对应<DSItem>标记的”LinkTo"属性值，此属性值为某变量名称 该索引所指变量是本变量真正连接的传感器。
	 */
	public short wLinkTo;

	/**
	 * 变量关联的SP配置。 当某SP配置的Ref指向本变量时，以该SP配置来设置本属性
	 */
	public J1939_SPCfg_ts pSPCfg;

//	public RealTimeChangedListener listener;
//
//	public void notifyListener() {
//		if (listener == null) {
//			return;
//		}
//		listener.onChanged();
//	}

	public boolean isFloatType() {
		return bDataType.equals(VARTYPE.FLOAT);
	}

	public byte getDataType() {
		return Byte.valueOf(bDataType.ordinal() + "");
	}

	public void setName(String sName) {
		this.sName = sName;
	}

	public void setUnit(String sUnit) {
		this.sUnit = sUnit;
	}

	public void setDecLen(String sDataDec) {
		if (sDataDec != null) {
			this.bDataDec = Byte.valueOf(sDataDec);
		}
	}

	public void setLinkTo(short wLinkTo) {
		this.wLinkTo = wLinkTo;
	}

	public void setRemarkID(short remarkID) {
		this.wRemarkStrID = remarkID;
	}

	public void setDataID(String dataID) {
		this.wDataID = Short.valueOf(dataID);
	}

	/**
	 * 根据数据变量索引 ，得到变量值以作为向量类型的数据变量的元素索引
	 *
	 * @param wDataVarIndex
	 *            : 变量索引
	 * @return 变量索引所指向变量的当前值
	 */
	public static short GetRowIndexByDataVarIndex(short wDataVarIndex) {
		return (short) (J1939_Context.j1939_DataVarCfg[wDataVarIndex]
				.getValue());
	}

	/**
	 * 简单类型变量构造函数
	 *
	 * @param dataType
	 *            : 变量的数据类型
	 * @param value
	 *            : 变量值
	 */
	public J1939_DataVar_ts(String dataType, String value) {
		VARTYPE bDataType = VARTYPE.valueOf(dataType);
		this.bDataType = bDataType; //
		if (value == null || value.trim().length() == 0) {
			// TODO:需要跟老吴商量
		} else if (bDataType == VARTYPE.FLOAT) {
			this.unValue = Float.floatToIntBits(Float.valueOf(value));
		} else {
			int iValue = Integer.valueOf(value);
			if (bDataType.equals(VARTYPE.SHORT)
					|| bDataType.equals(VARTYPE.INT)) {

			} else if (bDataType == VARTYPE.BYTE) {
				this.unValue = iValue & 0x000000FF;
			} else if (bDataType == VARTYPE.WORD) {
				this.unValue = iValue & 0x0000FFFF;
			} else if (bDataType == VARTYPE.DWORD) {
				this.unValue = ((long) iValue) & 0xFFFFFFFFl;
			} else {
				this.unValue = (long) iValue; // Short, int类型
			}
		}
		this.wRows = 0; //
		this.wRowIndexBy = (short) 0xFFFF; // 简单类型无元素索引
		this.wLinkTo = (short) 0xFFFF; // 初始化为无 LinkTo
	}

	/**
	 * 简单类型整数（字节、字、双字类型）变量构造函数
	 *
	 * @param bDataType
	 *            : 变量的数据类型
	 * @param iValue
	 *            : 变量值（WORD, DWORD类型值要转化为int类型值后再实例化变量）
	 */
	public J1939_DataVar_ts(VARTYPE bDataType, int iValue) {
		super();
		if (bDataType != VARTYPE.FLOAT) {
			if (bDataType == VARTYPE.BYTE) {
				this.unValue = iValue & 0x000000FF;
			} else if (bDataType == VARTYPE.WORD) {
				this.unValue = iValue & 0x0000FFFF;
			} else if (bDataType == VARTYPE.DWORD) {
				this.unValue = ((long) iValue) & 0xFFFFFFFFl;
			} else {
				this.unValue = (long) iValue; // Short, int类型
			}
			this.bDataType = bDataType; //
			this.wRows = 0; //
			this.wRowIndexBy = (short) 0xFFFF; // 简单类型无元素索引
			this.wLinkTo = (short) 0xFFFF; // 初始化为无 LinkTo
		} else { //
			// 指定类型与值类型不匹配
		}
	}

	/**
	 * 向量类型变量构造函数
	 *
	 * @param bDataType
	 *            : 向量元素的数据类型
	 * @param wRows
	 *            : 向量长度
	 * @param wRows
	 *            : 向量长度
	 * @param wRowIndexBy
	 *            : 向量索引指示变量索引，由该索引变量的值指示向量元素位置
	 * @param objValue
	 *            : 数据数组，其类型必须与bDataType匹配。如果 objValue=NULL,
	 *            则自动按指定类型分配指定长度的数组并初始化为0
	 * */
	public J1939_DataVar_ts(String dataType, short wRows, short wRowIndexBy,
							Object[] objValue) {
		VARTYPE bDataType = VARTYPE.valueOf(dataType);
		if (objValue != null) {
//			String sObjType = objValue.getClass().getName();
			String sObjType = dataType;
			if ((sObjType.startsWith("B") && (bDataType == VARTYPE.BYTE))
					|| (sObjType.startsWith("I") && ((bDataType == VARTYPE.INT) || (bDataType == VARTYPE.DWORD)))
					|| (sObjType.startsWith("S") && ((bDataType == VARTYPE.SHORT) || (bDataType == VARTYPE.WORD)))
					|| (sObjType.startsWith("F") && (bDataType == VARTYPE.FLOAT))) {
				if ((objValue).length == (int) wRows) {
					this.vValue = objValue;
				} else {
					// 向量长度不匹配
				}
			} else {
				// 向量元素类型不匹配
			}
		} else {
			// 未指定向量数组，则按指定类型和长度生成
			if (bDataType == VARTYPE.BYTE) {
				this.vValue = new byte[(int) wRows];
			} else if ((bDataType == VARTYPE.SHORT)
					|| (bDataType == VARTYPE.WORD)) {
				this.vValue = new short[(int) wRows];
			} else if ((bDataType == VARTYPE.INT)
					|| (bDataType == VARTYPE.DWORD)) {
				this.vValue = new int[(int) wRows];
			} else if (bDataType == VARTYPE.FLOAT) {
				this.vValue = new float[(int) wRows];
			} else {
				// 无效类型
			}

		}

		this.bDataType = bDataType; //
		this.wRows = wRows; //
		this.wRowIndexBy = wRowIndexBy; // 初始化为未设置索引
		this.wLinkTo = (short) 0xFFFF; // 初始化为无 LinkTo

	}

	/**
	 * 获取浮点类型的变量值
	 *
	 * @param 无
	 * @return 浮点类型的变量值
	 */
	public float getFloatValue() {

		short wRowIndex;

		if (bDataType == VARTYPE.FLOAT) {
			if (wRows == 0) {
				return Float.intBitsToFloat((int) unValue);
			} else {
				if (wRowIndexBy != 0xFFFF && vValue != null) {
					wRowIndex = GetRowIndexByDataVarIndex(wRowIndexBy);
					if (wRowIndex >= 0 && wRowIndex < wRows) {
						return ((float[]) vValue)[wRowIndex];
					}
				}
			}
		}

		// 不能获取到浮点变量值
		return (11111.11f);

	}

	/**
	 * 设置浮点类型的变量值
	 *
	 * @param
	 * @return
	 */
	public byte setFloatValue(float fValue) {

		short wRowIndex;

		if (bDataType == VARTYPE.FLOAT) {
			if (wRows == 0) {
				this.unValue = Float.floatToIntBits(fValue);
				return (1);
			} else {
				if (wRowIndexBy != 0xFFFF && vValue != null) {
					wRowIndex = GetRowIndexByDataVarIndex(wRowIndexBy);
					if (wRowIndex >= 0 && wRowIndex < wRows) {
						((float[]) vValue)[wRowIndex] = fValue;
						return (1);
					}
				}
			}
		}

		// 不能设置浮点类型的变量值
		return (0);
	}

	/**
	 * 获取整数类型的变量值
	 *
	 * @return 长整型变量值
	 */
	public long getValue() {

		short wRowIndex;

		if (bDataType != VARTYPE.FLOAT) {
			if (wRows == 0) {
				return (unValue);
			} else {
				if ((wRowIndexBy != 0xFFFF) && (vValue != null)) {
					wRowIndex = GetRowIndexByDataVarIndex(wRowIndexBy);
					if ((wRowIndex >= 0) && (wRowIndex < wRows)) {
						if (bDataType == VARTYPE.BYTE)
							return ((byte[]) vValue)[wRowIndex];
						else if ((bDataType == VARTYPE.SHORT)
								|| (bDataType == VARTYPE.WORD))
							return ((short[]) vValue)[wRowIndex];
						else if ((bDataType == VARTYPE.INT)
								|| (bDataType == VARTYPE.DWORD))
							return ((int[]) vValue)[wRowIndex];
						else {
							// 无效的数据类型
						}
					}
				}
			}
		}

		// 不能获取到整数变量值
		return (-1l);

	}

	/**
	 * 设置整数类型的的变量值
	 *
	 * @param lValue
	 *            : 已转换为长整型的变量值
	 * @return 是否设置成功，1 -- 设置成功 0 -- 设置失败
	 */
	public byte setValue(long lValue) {

		short wRowIndex;

		if (bDataType != VARTYPE.FLOAT) {
			if (wRows == 0) {
				this.unValue = lValue;
				return (1);
			} else {
				if (wRowIndexBy != 0xFFFF && vValue != null) {
					wRowIndex = GetRowIndexByDataVarIndex(wRowIndexBy);
					if ((wRowIndex >= 0) && (wRowIndex < wRows)) {
						if (bDataType == VARTYPE.BYTE)
							((byte[]) vValue)[wRowIndex] = (byte) (lValue & 0xFFl);
						else if ((bDataType == VARTYPE.SHORT)
								|| (bDataType == VARTYPE.WORD))
							((short[]) vValue)[wRowIndex] = (short) (lValue & 0xFFl);
						else if ((bDataType == VARTYPE.INT)
								|| (bDataType == VARTYPE.DWORD))
							((int[]) vValue)[wRowIndex] = (int) (lValue & 0xFFFFFFFFl);
						else {
							// 无效的数据类型
						}
					}
				}
			}
		}

		// 不能设置浮点类型的变量值
		return (0);
	}

	/**
	 * 获取变量指定位置元素的值（将整数类型转换为对应的浮点类型数）
	 *
	 * @param wIdx
	 * @return
	 */
	public float getFloatValueByRow(short wRowIdx) {

		float fRet = 0.0f;

		if (wRows == 0) {
			if (bDataType == VARTYPE.FLOAT) {
				return Float.intBitsToFloat((int) unValue);
			} else if (bDataType == VARTYPE.BYTE) {
				return (float) (unValue & 0xFF);
			} else if (bDataType == VARTYPE.SHORT) {
				return (float) ((short) unValue);
			} else if (bDataType == VARTYPE.WORD) {
				return (float) (unValue & 0xFFFFl);
			} else if (bDataType == VARTYPE.INT) {
				return (float) ((int) (unValue));
			} else if (bDataType == VARTYPE.DWORD) {
				return (float) (unValue & 0xFFFFFFFFl);
			}
		} else {
			if (bDataType == VARTYPE.FLOAT) {
				return (Float)((Object[]) vValue)[wRowIdx];
			} else if (bDataType == VARTYPE.BYTE) {
				return (float) (((byte[]) vValue)[wRowIdx] & 0xFF);
			} else if (bDataType == VARTYPE.SHORT) {
				return (float) (((short[]) vValue)[wRowIdx]);
			} else if (bDataType == VARTYPE.WORD) {
				return (float) (((short[]) vValue)[wRowIdx] & 0xFFFF);
			} else if (bDataType == VARTYPE.INT) {
				return (float) (((int[]) vValue)[wRowIdx]);
			} else if (bDataType == VARTYPE.DWORD) {
				return (float) (((int[]) vValue)[wRowIdx] & 0xFFFFFFFFl);
			}
		}

		return (fRet);
	}

}

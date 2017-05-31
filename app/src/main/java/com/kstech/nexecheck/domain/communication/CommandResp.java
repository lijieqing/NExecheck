package com.kstech.nexecheck.domain.communication;

import com.kstech.nexecheck.utils.Globals;

import J1939.J1939_DataVar_ts;

/**
 * Created by lenovo on 2016/11/2.
 */

public class CommandResp {


    /**
     * 准备检测指令
     */
    private static final long READY_TO_CHECK = 0x01;

    /**
     * 开始检测指令
     */
    private static final long START_CHECK = 0x02;

    /**
     * 中止检测指令
     */
    private static final long STOP_CHECK = 0x03;
    /**
     *
     */
    private static final int SEND_PGN = 0xFF80;

    /**
     * 检测应答状态： 测量终端收到检测命令后的应答，其值有
     * 准备就绪
     */
    private static final long READY_SUCCESS = 0x11;
    /**
     * 传感器故障，具体故障查年相关传感器的DTC
     */
    private static final long CGQ_ERROR = 0x12;
    /**
     * 正在检测, 检测进度指示在”检测指示码中"
     */
    private static final long CHECK_ING = 0x21;
    /**
     * 检测完成，测量值从检测项目相关变量中获得，这就要求在发送此完成应答前先发送相关变量值帧
     */
    private static final long CHECK_SUCCESS = 0x22;
    /**
     * 测量失败，其失败原因指示码在“检测指示码"域中
     */
    private static final long CHECK_FAIL = 0x23;
    /**
     * 本次测量中止
     */
    private static final long CHECK_STOP = 0x31;

    /**
     * 轮训“准备检测命令”应答
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static String getReadyToCheckCommandResp(String qcItemId, int times) {
        return getCommandResp(qcItemId, times,READY_TO_CHECK);
    }
    /**
     * 轮训“开始检测命令”应答
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static String getStartCheckCommandResp(String qcItemId, int times) {
        return getCommandResp(qcItemId, times,START_CHECK);
    }

    /**
     * 轮训“中止检测命令”应答
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static String getStopCheckCommandResp(String qcItemId, int times) {
        return getCommandResp(qcItemId, times,STOP_CHECK);
    }

    /* 命令应答处理任务
        前提：发出命令前将 <应答的当前检测项目>、<应答的当前检测次序>、<应答的检测命令> 这三个变量值复位（0xFF)
        发送完轮询应答变量取出：应答的当前检测项目、应答的当前检测次序、应答的检测命令、检测应答状态、检测指示码

     if ( ( <当前检测项目> == <应答的当前检测项目> ) &&
          ( <当前检测次序> == <应答的当前检测次序> ) &&
          ( <检测命令> == <应答的检测命令> ) ) {

         根据   <检测命令> 、 <当前检测项目>、<检测应答状态>、<检测指示码>的值进行相应处理：
          1) 成功、失败判断 、显示进度指示信息
          2) 根据检测项目的配置，取出各被检测和环境参数值
          3) 保存检测记录,等等

         清除<当前检测项目>、<当前检测次序>、<检测命令> （0x00)
     }*/
    private static String getCommandResp(String qcItemId, int times, long command) {
        // 当前检测项目
        J1939_DataVar_ts checkItemDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckItemDSItemResp();
        // 当前检测次序
        J1939_DataVar_ts checkTimesDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckTimesDSItemResp();
        // 检测命令
        J1939_DataVar_ts commandDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCommandDSItemResp();
        // 检测应答状态
        J1939_DataVar_ts checkStatusDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckStatusDSItemResp();
        // 检测指示码
        J1939_DataVar_ts checkCodeDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckCodeDSItemResp();
        if ((Long.valueOf(qcItemId) == checkItemDSItemResp.getValue()) &&
                ( (long)times == checkTimesDSItemResp.getValue() ) &&
                ( command == commandDSItemResp.getValue() ) ) {
			 	/*根据   <检测命令> 、 <当前检测项目>、<检测应答状态>、<检测指示码>的值进行相应处理：
			 	 1) 成功、失败判断 、显示进度指示信息
			 	 2) 根据检测项目的配置，取出各被检测和环境参数值
			 	 3) 保存检测记录,等等
			 	清除<当前检测项目>、<当前检测次序>、<检测命令> （0x00)*/
            String resultStr = "";
            if (checkStatusDSItemResp.getValue() == READY_SUCCESS ){
                // 准备就绪
                resultStr = "准备就绪";
            } else if (checkStatusDSItemResp.getValue() == CGQ_ERROR ){
                resultStr = "传感器故障";
            } else if (checkStatusDSItemResp.getValue() == CHECK_ING ){
                // 正在检测, 检测进度指示在”检测指示码中"
                resultStr = "正在检测";
            } else if (checkStatusDSItemResp.getValue() == CHECK_SUCCESS ){
                // 检测完成，测量值从检测项目相关变量中获得，这就要求在发送此完成应答前先发送相关变量值帧
                resultStr = "检测完成";
            } else if (checkStatusDSItemResp.getValue() == CHECK_FAIL ){
                // 测量失败，其失败原因指示码在“检测指示码"域中
                resultStr = "检测失败";
            } else if (checkStatusDSItemResp.getValue() == CHECK_STOP ){
                // 本次测量中止
                resultStr = "本次测量中止 ";
            }
            // 清除<当前检测项目>、<当前检测次序>、<检测命令> （0x00)
            clearLastSend();
            return resultStr;
        }
        return "";
    }

    /* 命令应答处理任务
        前提：发出命令前将 <应答的当前检测项目>、<应答的当前检测次序>、<应答的检测命令> 这三个变量值复位（0xFF)
        发送完轮询应答变量取出：应答的当前检测项目、应答的当前检测次序、应答的检测命令、检测应答状态、检测指示码

     if ( ( <当前检测项目> == <应答的当前检测项目> ) &&
          ( <当前检测次序> == <应答的当前检测次序> ) &&
          ( <检测命令> == <应答的检测命令> ) ) {

         根据   <检测命令> 、 <当前检测项目>、<检测应答状态>、<检测指示码>的值进行相应处理：
          1) 成功、失败判断 、显示进度指示信息
          2) 根据检测项目的配置，取出各被检测和环境参数值
          3) 保存检测记录,等等

         清除<当前检测项目>、<当前检测次序>、<检测命令> （0x00)
     }*/
    public static void clearLastSend(){
        //当前检测项目
        J1939_DataVar_ts checkItemDSItem = Globals.getModelFile()
                .getDataSetVO().getCheckItemDSItem();
        checkItemDSItem.setValue(0x00);
        //当前检测次序
        J1939_DataVar_ts checkTimesDSItem = Globals.getModelFile()
                .getDataSetVO().getCheckTimesDSItem();
        checkTimesDSItem.setValue(0x00);
        //检测命令
        J1939_DataVar_ts commandDSItem = Globals.getModelFile()
                .getDataSetVO().getCommandDSItem();
        commandDSItem.setValue(0x00);
    }
}

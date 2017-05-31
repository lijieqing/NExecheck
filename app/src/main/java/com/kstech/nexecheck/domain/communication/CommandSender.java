package com.kstech.nexecheck.domain.communication;

import com.kstech.nexecheck.utils.Globals;

import J1939.J1939_Context;
import J1939.J1939_DataVar_ts;
import J1939.J1939_PGCfg_ts;

/**
 * 命令发送类
 * Created by lenovo on 2016/10/20.
 */
public class CommandSender {


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
     * 发送“准备检测命令”
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static void sendReadyToCheckCommand(String qcItemId, int times) {

        sendCommand(qcItemId, times,READY_TO_CHECK);

    }

    /**
     * 发送“开始检测命令”
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static void sendStartCheckCommand(String qcItemId, int times) {
        sendCommand(qcItemId, times,START_CHECK);
    }

    /**
     * 发送“中止检测命令”
     *
     * @param qcItemId
     *            检验项ID
     * @param times
     *            当前检验次序
     */
    public static void sendStopCheckCommand(String qcItemId, int times) {
        sendCommand(qcItemId, times,STOP_CHECK);
    }
    /*
     * 1. 通过解析已知全局变量 “当前检测项目”、“当前检测次序”、“检测命令”的索引 idxDD_CurrentCheckItem,
     * idxDD_CurrentCheckNo， idxDD_CheckCmd
     *
     * 2. 通过解析已知PGN=0xFF80的PGN配置项索引 idxPGCfg_Cmd
     *
     * 3. 设置“当前检测项目”、“当前检测次序”、“检测命令”这三个数据变量的值
     *
     * J1939_Context.j1939_DataVarCfg[idxDD_CurrentCheckItem].SetValue(...);
     * J1939_Context.j1939_DataVarCfg[idxDD_CurrentCheckNo].SetValue(...);
     * J1939_Context.j1939_DataVarCfg[idxDD_CheckCmd].SetValue(...);
     *
     * 4. 发送命令 j1939_SendDatabox(J1939_Context.j1939_Cfg.pPGCfg[idxPGCfg_Cmd]
     * .wDBNumber)
     *
     * 5. 通过检查与命令应答有关的变量值来判断命令应答情况
     */
    private static void sendCommand(String qcItemId, int times, long command) {

        //当前检测项目
        J1939_DataVar_ts checkItemDSItem = Globals.getModelFile()
                .getDataSetVO().getCheckItemDSItem();
        checkItemDSItem.setValue(Integer.valueOf(qcItemId));
        //当前检测次序
        J1939_DataVar_ts checkTimesDSItem = Globals.getModelFile()
                .getDataSetVO().getCheckTimesDSItem();
        checkTimesDSItem.setValue(times);
        //检测命令
        J1939_DataVar_ts commandDSItem = Globals.getModelFile()
                .getDataSetVO().getCommandDSItem();
        commandDSItem.setValue(command);

        J1939_PGCfg_ts pg = Globals.getModelFile().j1939PgSetVO
                .getPg(SEND_PGN);

        // 前提：发出命令前将 <应答的当前检测项目>、<应答的当前检测次序>、<应答的检测命令> 这三个变量值复位（0xFF)
        clearLastResp();

        int i = J1939_Context.j1939_API.j1939_sendDatabox((short) pg.wDBNumber);
        System.out.println(i);
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
    public static void clearLastResp(){
        // 当前检测项目
        J1939_DataVar_ts checkItemDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckItemDSItemResp();
        // 当前检测次序
        J1939_DataVar_ts checkTimesDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCheckTimesDSItemResp();
        // 检测命令
        J1939_DataVar_ts commandDSItemResp = Globals.getModelFile()
                .getDataSetVO().getCommandDSItemResp();
        checkItemDSItemResp.setValue(0xFF);
        checkTimesDSItemResp.setValue(0xFF);
        commandDSItemResp.setValue(0xFF);
    }
}

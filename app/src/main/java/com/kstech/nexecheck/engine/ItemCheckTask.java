package com.kstech.nexecheck.engine;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.communication.CommandResp;
import com.kstech.nexecheck.domain.communication.CommandSender;
import com.kstech.nexecheck.domain.config.vo.CheckItemParamValueVO;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.dao.CheckItemDao;
import com.kstech.nexecheck.domain.db.dao.CheckItemDetailDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemDetailStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.fragment.DoCheckFragment;

import java.text.DecimalFormat;
import java.util.List;

import J1939.J1939_DataVar_ts;

/**
 * Created by lijie on 2017/5/24.
 */

public class ItemCheckTask extends AsyncTask<Void, String, Void> {

    private HomeActivity context;
    private int remainSeconds = 0;
    private CheckItemVO checkItemVO;
    private TextView msgTv;
    private Chronometer chronometer;
    private DoCheckFragment.MsgAdapter msgAdapter;

    private List<CheckItemParamValueVO> headers;

    public boolean isRunning = false;

    private String detailStatus = CheckItemDetailStatusEnum.PASS.getCode();

    public ItemCheckTask(HomeActivity context, Chronometer chronometer, DoCheckFragment.MsgAdapter msgAdapter, TextView msgTv) {
        this.context = context;
        this.chronometer = chronometer;
        this.msgAdapter = msgAdapter;
        this.msgTv = msgTv;
    }

    @Override
    protected void onPreExecute() {
        checkItemVO = Globals.getModelFile().getCheckItemVO(context.checkItemEntity.getItemId());
        headers = Globals.getModelFile().getCheckItemVO(context.checkItemEntity.getItemId()).getParamNameList();
    }

    //publishprogress 参数 0 状态；1
    @Override
    protected Void doInBackground(Void... params) {
        isRunning = true;
        // 发送检测命令
        CommandSender.sendStartCheckCommand(context.checkItemEntity.getItemId(), context.checkItemEntity.getSumTimes() + 1);

        publishProgress("start", "发送准备检测命令", "");

        while (remainSeconds < checkItemVO.getQcTimeout() && isRunning) {

            String startCheckCommandResp = CommandResp.getStartCheckCommandResp(context.checkItemEntity.getItemId(), context.checkItemEntity.getSumTimes() + 1);
            Log.e("startCheckCommandResp", startCheckCommandResp + "---------------");

            if ("".equals(startCheckCommandResp)) {
                // 还没有响应，继续轮循
                SystemClock.sleep(1000);
                remainSeconds++;
            } else if ("正在检测".equals(startCheckCommandResp)) {
                // 检测指示码
                J1939_DataVar_ts checkCodeDSItemResp = Globals.getModelFile()
                        .getDataSetVO().getCheckCodeDSItemResp();
                String progressMsg = "";
                if (checkCodeDSItemResp.isFloatType()) {
                    progressMsg = checkItemVO.getProgressMsg(String.valueOf(checkCodeDSItemResp.getFloatValue()));
                } else {
                    progressMsg = checkItemVO.getProgressMsg(String.valueOf(checkCodeDSItemResp.getValue()));
                }
                String content = "正在检测 - - - ";
                if (progressMsg != null && !progressMsg.equals("")) {
                    content = Globals.getResConfig().getResourceVO().getMsg(progressMsg).getContent();
                }
                publishProgress("running","正在检测 - - - ","");
                // 通知UI线程检测进度，继续循环判断状态，至检测完成
                SystemClock.sleep(1000);
                remainSeconds++;

            } else if ("检测完成".equals(startCheckCommandResp)) {

                for (CheckItemParamValueVO h : headers) {
                    // 从数据区中获取 检查参数对应的数据区
                    J1939_DataVar_ts dsItem = Globals.getModelFile().getDataSetVO().getDSItem(h.getParam());
                    //数值转换
                    byte bDataDec = dsItem.bDataDec;
                    StringBuffer sb = new StringBuffer();
                    if (bDataDec != 0) {
                        sb.append(".");
                        for (int i = 0; i < bDataDec; i++) {
                            sb.append("0");
                        }
                    }
                    DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
                    if (dsItem.isFloatType()) {
                        Float value = dsItem.getFloatValue();
                        Log.e("MIAO", "收到得知values" + value);
                        String formatValue = decimalFormat.format(value);
                        if (".".equals(formatValue.substring(0, 1))) {
                            formatValue = "0" + formatValue;
                        }
                        if ("0".equals(formatValue)) {
                            formatValue = "0";
                        }
                        Float checkvalue = Float.parseFloat(formatValue);
                        h.setValue(formatValue);
                        Log.e("MIAO", "收到得知" + formatValue);
                        // 有一个参数不合格，那么当次检测不合格
                        if (Float.valueOf(h.getValidMin()) > checkvalue || checkvalue > Float.valueOf(h.getValidMax())) {
                            detailStatus = CheckItemDetailStatusEnum.UN_PASS.getCode();
                        }

                    } else {
                        h.setValue(dsItem.getValue() + "");
                        // 有一个参数不合格，那么当次检测不合格
                        if (Long.valueOf(h.getValidMin()) > dsItem.getValue() || dsItem.getValue() > Long.valueOf(h.getValidMax())) {
                            detailStatus = CheckItemDetailStatusEnum.UN_PASS.getCode();
                        }
                    }
                }

                String okMsg = checkItemVO.getOkMsg();
                String content = "";
                if (okMsg != null && !okMsg.equals("")) {
                    content = Globals.getResConfig().getResourceVO().getMsg(okMsg).getContent();
                }
                // 通知UI线程检测完成，程序终止

                publishProgress("finish", "检测完成", content);

                for (CheckItemParamValueVO h : headers) {
                    // 从数据区中获取 检查参数对应的数据区
                    J1939_DataVar_ts dsItem = Globals.getModelFile().getDataSetVO().getDSItem(h.getParam());
                    //数值转换
                    byte bDataDec = dsItem.bDataDec;
                    StringBuffer sb = new StringBuffer();
                    if (bDataDec != 0) {
                        sb.append(".");
                        for (int i = 0; i < bDataDec; i++) {
                            sb.append("0");
                        }
                    }
                    DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
                    if (dsItem.isFloatType()) {
                        Float value = 0f;
                        String formatValue = decimalFormat.format(value);
                        if (".".equals(formatValue.substring(0, 1))) {
                            formatValue = "0" + formatValue;
                        }
                        if ("0".equals(formatValue)) {
                            formatValue = "0";
                        }
                        h.setValue(formatValue);
                    }
                }

                return null;
            } else if ("传感器故障".equals(startCheckCommandResp) || "检测失败".equals(startCheckCommandResp)) {
                for (CheckItemParamValueVO h : headers) {
                    // 从数据区中获取 检查参数对应的数据区
                    J1939_DataVar_ts dsItem = Globals.getModelFile().getDataSetVO().getDSItem(h.getParam());
                    //数值转换
                    byte bDataDec = dsItem.bDataDec;
                    StringBuffer sb = new StringBuffer();
                    if (bDataDec != 0) {
                        sb.append(".");
                        for (int i = 0; i < bDataDec; i++) {
                            sb.append("0");
                        }
                    }
                    DecimalFormat decimalFormat = new DecimalFormat(sb.toString());
                    if (dsItem.isFloatType()) {
                        Float value = 0f;
                        String formatValue = decimalFormat.format(value);
                        if (".".equals(formatValue.substring(0, 1))) {
                            formatValue = "0" + formatValue;
                        }
                        if ("0".equals(formatValue)) {
                            formatValue = "0";
                        }
                        h.setValue(formatValue);
                    }
                }
                // 检测指示码
                J1939_DataVar_ts checkCodeDSItemResp = Globals.getModelFile().getDataSetVO().getCheckCodeDSItemResp();
                String errorMsg = "";
                if (checkCodeDSItemResp.isFloatType()) {
                    errorMsg = checkItemVO.getErrorMsg(String.valueOf(checkCodeDSItemResp.getFloatValue()));
                } else {
                    errorMsg = checkItemVO.getErrorMsg(String.valueOf(checkCodeDSItemResp.getValue()));
                }
                String content = "";
                if (errorMsg != null && !errorMsg.equals("")) {
                    content = Globals.getResConfig().getResourceVO().getMsg(errorMsg).getContent();
                }

                publishProgress("error","检测失败",content);

                return null;
            }
        }

        //// TODO: 2017/6/2 通讯超时处理
        // 保存记录，结论，通讯超时
        publishProgress("timeout","通讯超时","");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //dialog.cancel();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        CheckItemEntity checkItemEntity;
        switch (values[0]){
            case "start":
                msgTv.setText(values[1]+values[2]);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                break;
            case "finish":
                msgTv.setText(values[1]+"，"+values[2]);
                chronometer.stop();
                isRunning = false;
                this.cancel(true);
                context.doCheckFragment.singleCheckBeginCheckLeftBtn.setText("开始测量");
                context.doCheckFragment.singleCheckBeginCheckRightBtn.setText("开始测量");
                // 插入详情记录，更新项目记录
                CheckItemDetailDao.insertDetailAndUpdateItem(context, detailStatus, context.checkItemEntity, headers, checkItemVO);
                checkItemEntity = CheckItemDao.getSingleCheckItemFromDB(context.excID,context.checkItemEntity.getItemId(),context);
                context.doCheckFragment.checkItemSingleView.initCheckItemParamList(checkItemEntity);
                context.homeCheckEntityFragment.currentCheckItemView.initCheckItemParamList(checkItemEntity);
                break;
            case "error":
                msgTv.setText(values[1]+"，"+values[2]);
                chronometer.stop();
                isRunning = false;
                this.cancel(true);
                context.doCheckFragment.singleCheckBeginCheckLeftBtn.setText("开始测量");
                context.doCheckFragment.singleCheckBeginCheckRightBtn.setText("开始测量");
                // 保存记录，结论，传感器故障
                CheckItemDetailDao.insertDetailAndUpdateItem(context, CheckItemDetailStatusEnum.OTHER.getCode(), context.checkItemEntity, headers, checkItemVO);
                checkItemEntity = CheckItemDao.getSingleCheckItemFromDB(context.excID,context.checkItemEntity.getItemId(),context);
                context.doCheckFragment.checkItemSingleView.initCheckItemParamList(checkItemEntity);
                context.homeCheckEntityFragment.currentCheckItemView.initCheckItemParamList(checkItemEntity);
                break;
            case "running":
                msgTv.setText(values[1]+values[2]);
                break;
            case "timeout":
                msgTv.setText(values[1]+"，"+values[2]);
                chronometer.stop();
                isRunning = false;
                this.cancel(true);
                context.doCheckFragment.singleCheckBeginCheckLeftBtn.setText("开始测量");
                context.doCheckFragment.singleCheckBeginCheckRightBtn.setText("开始测量");
                // 保存记录，结论，超时
                CheckItemDetailDao.insertDetailAndUpdateItem(context,CheckItemDetailStatusEnum.CONNECT_TIMEOUT.getCode(),context.checkItemEntity,headers,checkItemVO);
                checkItemEntity = CheckItemDao.getSingleCheckItemFromDB(context.excID,context.checkItemEntity.getItemId(),context);
                context.doCheckFragment.checkItemSingleView.initCheckItemParamList(checkItemEntity);
                context.homeCheckEntityFragment.currentCheckItemView.initCheckItemParamList(checkItemEntity);
                break;
        }
    }
}

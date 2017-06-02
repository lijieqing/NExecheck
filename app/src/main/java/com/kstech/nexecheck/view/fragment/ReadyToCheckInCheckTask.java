package com.kstech.nexecheck.view.fragment;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.communication.CommandResp;
import com.kstech.nexecheck.domain.communication.CommandSender;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.config.vo.RealTimeParamVO;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.widget.RealTimeView;

/**
 * Created by lijie on 2017/5/24.
 */

public class ReadyToCheckInCheckTask extends AsyncTask<Void,String,Void> {

    private AlertDialog dialog;
    private HomeActivity context;
    private boolean isSingle = false;
    private int remainSeconds = 0;
    private CheckItemVO checkItemVO;
    private TextView tvMsg;
    private Chronometer chronometer;
    private Button btnIn;
    private Button btnCancel;

    public ReadyToCheckInCheckTask(HomeActivity context, CheckItemVO checkItemVO) {
        this.context = context;
        this.checkItemVO = checkItemVO;
    }

    @Override
    protected void onPreExecute() {

        View view = View.inflate(context, R.layout.progress_view, null);
        tvMsg = (TextView) view.findViewById(R.id.tv_msg_ready_check);
        chronometer = (Chronometer) view.findViewById(R.id.chronom);
        btnIn = (Button) view.findViewById(R.id.btn_in);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setVisibility(View.INVISIBLE);
        btnIn.setVisibility(View.INVISIBLE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                //Globals.HomeLastPosition = Globals.HomeLastPosition-1;

                context.doCheckFragment.checkItemSingleView.initCheckItemParamList(context.checkItemEntity);
                Globals.CheckItemRealtimeViews.clear();
                for (RealTimeParamVO realTimeParamVO : checkItemVO.getRtParamList()) {
                    RealTimeView realTimeView = new RealTimeView(context,realTimeParamVO);
                    Globals.CheckItemRealtimeViews.add(realTimeView);
                }
                context.doCheckFragment.registRealTimeListener();
                context.doCheckFragment.msgTv.setText("提示信息");
                context.doCheckFragment.myAdapter.notifyDataSetChanged();
                context.doCheckFragment.msgAdapter.notifyDataSetChanged();
                context.doCheckFragment.chronometer.setBase(SystemClock.elapsedRealtime());
                context.homeCheckEntityFragment.updateFragment();
            }
        });
        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                context.doCheckFragment.checkItemSingleView.initCheckItemParamList(context.checkItemEntity);
                Globals.CheckItemRealtimeViews.clear();
                for (RealTimeParamVO realTimeParamVO : checkItemVO.getRtParamList()) {
                    RealTimeView realTimeView = new RealTimeView(context,realTimeParamVO);
                    Globals.CheckItemRealtimeViews.add(realTimeView);
                }
                context.doCheckFragment.registRealTimeListener();
                context.doCheckFragment.msgTv.setText("提示信息");
                context.doCheckFragment.myAdapter.notifyDataSetChanged();
                context.doCheckFragment.msgAdapter.notifyDataSetChanged();
                context.doCheckFragment.chronometer.setBase(SystemClock.elapsedRealtime());
                context.homeCheckEntityFragment.updateFragment();

            }
        });

        chronometer.setFormat("耗时：%s");
        dialog = new AlertDialog.Builder(context)
                .setView(view).setCancelable(false)
                .create();
        dialog.show();
        chronometer.start();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // 发送准备检测命令
        CommandSender.sendReadyToCheckCommand(context.checkItemEntity.getItemId(), context.checkItemEntity.getSumTimes() + 1);

        publishProgress("progress","与终端通讯进行准备检测--最大耗时--",""+checkItemVO.getReadyTimeout()*60);
        while (remainSeconds < checkItemVO.getReadyTimeout()*60){
            String readyToCheckCommandResp = CommandResp.getReadyToCheckCommandResp(context.checkItemEntity.getItemId(), context.checkItemEntity.getSumTimes() + 1);
            if ("准备就绪".equals(readyToCheckCommandResp)) {
                String readyMsg = checkItemVO.getReadyMsg();
                String content = "";
                if (readyMsg != null && !readyMsg.equals("")) {
                    content = Globals.getResConfig().getResourceVO().getMsg(readyMsg).getContent();
                }
                // 通知UI线程准备就绪，退出循环程序继续执行
                publishProgress("ok","与终端进行准备检测通讯--准备就绪--",content);
                SystemClock.sleep(1000);
                return null;

            } else if ("传感器故障".equals(readyToCheckCommandResp)) {
                // 有响应，但是不是准备就绪，则通知UI，传感器故障。程序终止
                String notReadyMsg = checkItemVO.getNotReadyMsg();
                Log.e("ReadyToCheckTask",notReadyMsg);
                String content = "";
                if (notReadyMsg != null && !notReadyMsg.equals("")) {
                    content = Globals.getResConfig().getResourceVO().getMsg(notReadyMsg).getContent();
                }
                publishProgress("error","--无法进入检测--",content);
                SystemClock.sleep(1000);
                return null;

            }else {
                //延时1s后 继续
                SystemClock.sleep(1000);
                remainSeconds++;
                publishProgress("progress","--与终端进行准备检测通讯--正在连接--","");
            }

        }
        publishProgress("timeout","--与终端进行准备检测通讯--超时--","无法开始检测");
        SystemClock.sleep(1000);
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
        if ("progress".equals(values[0])){
            tvMsg.setText(values[1]+values[2]);
            btnCancel.setVisibility(View.VISIBLE);
        }
        if ("error".equals(values[0])){
            tvMsg.setText(values[1]+values[2]);
            chronometer.stop();
            btnCancel.setVisibility(View.VISIBLE);
        }
        if ("ok".equals(values[0])){
            tvMsg.setText(values[1]+values[2]);
            chronometer.stop();
            btnIn.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }
        if ("timeout".equals(values[0])){
            tvMsg.setText(values[1]+values[2]);
            chronometer.stop();
            btnCancel.setVisibility(View.VISIBLE);
        }
    }
}

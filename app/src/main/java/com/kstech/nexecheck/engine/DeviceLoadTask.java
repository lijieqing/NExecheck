package com.kstech.nexecheck.engine;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.checkline.CheckLineListAdapter;
import com.kstech.nexecheck.domain.checkline.CheckLineManager;
import com.kstech.nexecheck.domain.communication.CommunicationWorker;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckLineVO;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.Globals;

import java.util.List;

import J1939.J1939_Task;

import static java.lang.Thread.State.TERMINATED;

public class DeviceLoadTask extends AsyncTask<Void, String, Void> {
    // 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

    private ProgressDialog mProgressDialog = null;
    private CheckRecordEntity checkRecordEntity = null;
    private HomeActivity context;
    private Handler handler;
    private String InExc;

    // 1939任务
    public  J1939_Task j1939ProtTask = null;

    // 1939任务
    public CommunicationWorker j1939CommTask = null;

    public DeviceLoadTask(String InExc,CheckRecordEntity checkRecordEntity, Handler handler,
                          J1939_Task j1939ProtTask, CommunicationWorker j1939CommTask, HomeActivity context) {
        this.checkRecordEntity = checkRecordEntity;
        this.context = context;
        this.handler = handler;
        this.InExc = InExc;
        this.j1939CommTask = j1939CommTask;
        this.j1939ProtTask = j1939ProtTask;
    }
    /**
     * 主要实现类 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
     * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
     *
     * @see AsyncTask#doInBackground(Params[])
     */

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Globals.loadDeviceModelFile(checkRecordEntity.getDeviceId(), checkRecordEntity.getSubdeviceId(), context);
        } catch (ExcException excException) {
            Toast.makeText(context, excException.getErrorMsg(), Toast.LENGTH_SHORT).show();
            Log.e("HomeActivity", excException.getErrorMsg());
            handler.sendEmptyMessage(0);
            return null;
        }
        context.excID = InExc;
        handler.sendEmptyMessage(1);
        SystemClock.sleep(1000);
        publishProgress("msg");
        // 配置文件加载后需要重新初始化1939任务，任务中包括实时参数的增量初始化
        if (j1939ProtTask != null && j1939ProtTask.isRunning) {
            // 停止通讯任务
            j1939CommTask.setStop(true);
            while (TERMINATED != j1939CommTask.getState()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // 停止协议任务
            j1939ProtTask.setStop(true);
            while(TERMINATED != j1939ProtTask.getState()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        j1939ProtTask = new J1939_Task();
        j1939ProtTask.Init();
        // 启动协议任务
        publishProgress("pro");
        SystemClock.sleep(1000);
        // 启动通讯任务
        j1939CommTask = new CommunicationWorker("192.168.1.178", 4001,context);
        j1939CommTask.setNetWorkStatusListener(context);
        Log.e("hahah","j1939CommTask start");
        publishProgress("comm");

        SystemClock.sleep(1000);
        return null;
    }

    /*
         * 第一个执行的方法 执行时机：在执行实际的后台操作前，被UI 线程调用
         * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
    @Override
    protected void onPreExecute() {
        mProgressDialog = new ProgressDialog(context);
        // 设置进度条风格，风格为圆形，旋转的
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // 设置ProgressDialog 提示信息
        mProgressDialog.setMessage("正在加载机型信息，请稍等。。。");

        // 设置ProgressDialog 的进度条是否不明确
        mProgressDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    /*
     * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI
     * 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
     * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
     *
     * @see android.os.AsyncTask#onProgressUpdate(Progress[])
     */
    @Override
    protected void onProgressUpdate(String... values) {
        // mTextView.setText(values[0]+"%");
        super.onProgressUpdate(values);
        if ("msg".equals(values[0])){
            mProgressDialog.setMessage("正在初始化通讯线程，请稍等。。。");
        }
        if ("pro".equals(values[0])){
            j1939ProtTask.start();
            mProgressDialog.setMessage("正在启动协议线程，请稍等。。。");
        }
        if ("comm".equals(values[0])){
            j1939CommTask.start();
            mProgressDialog.setMessage("正在启动通讯线程，请稍等。。。");
        }
    }

    /*
     * 执行时机：在doInBackground 执行完成后，将被UI 线程调用 作用：后台的计算结果将通过该方法传递到UI 线程，并且在界面上展示给用户
     * result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mProgressDialog.cancel();
    }

}
package com.kstech.nexecheck.engine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.communication.CommunicationWorker;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.Globals;

import J1939.J1939_Task;

import static java.lang.Thread.State.TERMINATED;

public class J1939TaskService extends Service {
    public HomeActivity context;

    // 1939任务
    public J1939_Task j1939ProtTask = null;

    // 1939任务
    public CommunicationWorker j1939CommTask = null;

    public J1939TaskService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean reload = intent.getBooleanExtra("reload",false);
        context = (HomeActivity) intent.getCharSequenceExtra("home");
        if (reload) {
            stopJ1939Service();

            j1939ProtTask = new J1939_Task();
            j1939ProtTask.Init();
            // 启动协议任务
            j1939ProtTask.start();

            // 启动通讯任务
            //// TODO: 2017/6/2 IP 此处写死 正式应用前记得改回
            //j1939CommTask = new CommunicationWorker("192.168.1.178", 4001, getApplicationContext());
            j1939CommTask = new CommunicationWorker(Globals.getCurrentCheckLine().getIp(), 4001, getApplicationContext());

            j1939CommTask.start();
            Log.e("hahah", "j1939CommTask start");
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder(this);
    }

    public class MyBinder extends Binder {
        public J1939TaskService task;

        MyBinder(J1939TaskService task) {
            this.task = task;
        }
    }

    public void stopJ1939Service(){
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
            while (TERMINATED != j1939ProtTask.getState()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

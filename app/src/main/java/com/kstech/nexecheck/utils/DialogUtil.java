package com.kstech.nexecheck.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class DialogUtil {
    public static AlertDialog dialog;

    // 定义一个显示消息的对话框
    public static void showDialog(final Context ctx
            , String msg, boolean goHome) {
        // 创建一个AlertDialog.Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setMessage(msg).setCancelable(false);
        builder.setNegativeButton("取消",null);
        builder.setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }

    // 定义一个显示指定组件的对话框
    public static void showDialog(Context ctx, View view) {
        dialog = new AlertDialog.Builder(ctx)
                .setView(view).setCancelable(false)
                .setPositiveButton("确定", null)
                .create();
        dialog.show();
    }
}
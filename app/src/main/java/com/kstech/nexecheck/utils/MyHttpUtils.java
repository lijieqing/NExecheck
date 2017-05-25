package com.kstech.nexecheck.utils;

import android.util.Log;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class MyHttpUtils {
    String results = "";

    /**
     * @param whereRequest 请求名字
     * @param url          请求地址
     * @param map          请求参数map
     * @param callback     回调方法
     * @return
     */
    public void xutilsGet(final String whereRequest, String url, HashMap<String, String> maps, final MyHttpCallback callback) {
        RequestParams params = new RequestParams(url);
        if (null != maps && !maps.isEmpty()){
            for (Map.Entry<String,String> entry : maps.entrySet()){
                params.addQueryStringParameter(entry.getKey(),entry.getValue());
            }
        }
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("", "");
                callback.onSuccess(result, whereRequest);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("", "");
                callback.onError("出错了" + ex.toString(), whereRequest);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("", "");
                callback.onError("出错了" + "onCancelled", whereRequest);
            }

            @Override
            public void onFinished() {
                Log.e("", "");
            }
        });
    }

    /**
     * @param whereRequest 请求名字
     * @param url          请求地址
     * @param map          请求参数map
     * @param callback     回调方法
     */
    public void xutilsPost(final String whereRequest, String url, HashMap<String, String> maps, final MyHttpCallback callback) {
        RequestParams params = new RequestParams(url);
        if (null != maps && !maps.isEmpty()){
            for (Map.Entry<String,String> entry : maps.entrySet()){
                params.addQueryStringParameter(entry.getKey(),entry.getValue());
            }
        }
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("", "");
                callback.onSuccess(result, whereRequest);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("", "");
                callback.onError("出错了" + "onError", whereRequest);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.e("", "");
                callback.onError("出错了" + "onCancelled", whereRequest);
            }

            @Override
            public void onFinished() {
                Log.e("", "");
            }
        });
    }

    /**
     * @param whereRequest 请求名字
     * @param url          请求地址
     * @param map          请求参数map
     * @param callback     回调方法
     * @return
     */
    public void xutilsGetFile(String url,final MyHttpCallback callback) {
        RequestParams params = new RequestParams(url);
        //自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
        params.setSaveFilePath(Globals.UPDATE);
        //自动为文件命名
        params.setAutoRename(true);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                callback.onSuccess(result,null);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callback.onError(ex.toString(),null);
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
            //网络请求之前回调
            @Override
            public void onWaiting() {
            }
            //网络请求开始的时候回调
            @Override
            public void onStarted() {
            }
            //下载的时候不断回调的方法
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                callback.onLoading(total,current,isDownloading);
                //当前进度和文件总大小
                Log.i("JAVA","current："+ current +"，total："+total);
            }
        });
    }





    public interface MyHttpCallback {
        void onSuccess(Object result, String whereRequest);

        void onError(Object errorMsg, String whereRequest);

        void onLoading(long total, long current, boolean isDownloading);
    }

}
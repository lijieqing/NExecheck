package com.kstech.nexecheck.activity.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.user.LoginActivity;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.BinaryFile;
import com.kstech.nexecheck.utils.InStreamUtils;
import com.kstech.nexecheck.utils.MyHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 软件更新引导页
 */
public class UpgradeActivity extends Activity implements MyHttpUtils.MyHttpCallback{
    private static final int INFO_UPDATE = 1;
    private static final int INFO_TOMAIN=2;
    private static final int INFO_NETERROR=3;
    private static final int INFO_DATAERROR=4;
    private String mVersonName;
    private int mVersonCode ;
    private String mDes;
    private LinearLayout rlroot;
    private TextView tv_progress;
    private String mUrl ;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INFO_UPDATE:
                    tv_progress.setText("检测完成");
                    showUpdateDialog();
                    break;
                case INFO_TOMAIN:
                    tv_progress.setText("检测完成");
                    toHome();
                    break;
                case INFO_NETERROR:
                    Toast.makeText(UpgradeActivity.this,"网络异常",Toast.LENGTH_SHORT).show();
                    toHome();
                    break;
                case INFO_DATAERROR:
                    Toast.makeText(UpgradeActivity.this,"数据异常",Toast.LENGTH_SHORT).show();
                    toHome();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        TextView tv = (TextView) findViewById(R.id.tv_verson);
        tv.setText("版本号："+getVersionName());
        rlroot = (LinearLayout) findViewById(R.id.rl_root);
        tv_progress = (TextView) findViewById(R.id.tv_download);
        tv_progress.setVisibility(View.VISIBLE);
        tv_progress.setText("正在检测版本");
        checkVerson();
        //mHandler.sendEmptyMessageDelayed(INFO_TOMAIN,2000);


        AlphaAnimation ala = new AlphaAnimation(0.2f,1);
        ala.setDuration(2000);
        rlroot.setAnimation(ala);

    }

    /**
     * 跳转主界面
     */
    private void toHome(){
        Intent intent =  new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * 请求网络 检查版本是否一致
     */
    private void checkVerson(){
        Thread t = new Thread(){
            @Override
            public void run() {
                Message msg = Message.obtain();
                HttpURLConnection conn = null;
                long startTime = System.currentTimeMillis();
                String path = "http://gps.kaishang.com:8990/update.json";
                if (Globals.getResConfig().getApkVO() != null){
                    path = Globals.getResConfig().getApkVO().getUrl();
                    Log.e("checkVerson","not null"+path);
                }
                try {
                    URL url = new URL(path);
                     conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);
                    if(conn.getResponseCode()==200){
                        InputStream is = conn.getInputStream();
                        String result = InStreamUtils.Stream2String(is);
                        Log.i("hahah","$$"+result);
                        JSONObject json = new JSONObject(result);
                        mVersonName = json.getString("versionName");
                        mVersonCode = json.getInt("versionCode");
                         mDes = json.getString("desc");
                         mUrl = json.getString("url");
                        Log.i("hahah","$$&&&&&"+mVersonCode+getVersonCode());
                        if(mVersonCode>getVersonCode()){
                            msg.what = INFO_UPDATE;
                        }else {
                            msg.what = INFO_TOMAIN;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what=INFO_NETERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what=INFO_DATAERROR;
                } finally {
                    if(conn!=null){
                        conn.disconnect();
                    }
                    long endTime = System.currentTimeMillis();
                    try {
                        if((endTime-startTime)<3000){
                            Thread.sleep(3000 - (endTime-startTime));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendMessage(msg);
                }

            }
        };
        t.start();
    }

    /**
     * 版本更新对话框
     */
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本：" + mVersonName);
        builder.setMessage(mDes);
        builder.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downLoadApk();
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toHome();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                toHome();
            }
        });
        builder.show();
    }



    /**
     * 文件下载
     */
    private void downLoadApk(){
        //initDB();
        tv_progress.setVisibility(View.VISIBLE);
        new MyHttpUtils().xutilsGetFile(mUrl,this);
    }

    /**
     * Init db.
     */
    public void initDB() {
        String s = "/data/data/com.kstech.nexecheck/databases/newBee";
        File file = new File(s);
        byte[] data = null;
        try {
            if(file.exists()){
                file.delete();
                file.createNewFile();
            }
            BufferedInputStream in = new BufferedInputStream(getAssets().open("kstech.db"));
            try {
                data = new byte[in.available()];
                in.read(data);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BinaryFile.write(s,data);
    }


    /**
     * 获取版本名称
     */
    private String getVersionName() {
        PackageManager pm = getPackageManager();// 包管理器
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);// 根据包名,获取相关信息
            String versionName = packageInfo.versionName;// 版本名称
            // int versionCode = packageInfo.versionCode;// 版本号
            // System.out.println("versionName:" + versionName);
            // System.out.println("versionCode:" + versionCode);
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // 包名未找到异常
            e.printStackTrace();
        }

        return "";
    }
    private int getVersonCode(){
        PackageManager pm = getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);// 根据包名,获取相关信息
            int versionCode = packageInfo.versionCode;// 版本号
            Log.i("hahah","&&&&&&&&"+versionCode);
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // 包名未找到异常
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public void onSuccess(Object result, String whereRequest) {
        File file = (File) result;
        //apk下载完成后，调用系统的安装方法 注意 flag加入非常重要 不然无法正常安装
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Object errorMsg, String whereRequest) {
        Log.i("JAVA","errorMsg："+ errorMsg );
    }

    @Override
    public void onLoading(long total, long current, boolean isDownloading) {
        final int percent = (int) (100 * current/total);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_progress.setText("下载进度："+percent+"%");
            }
        });
    }


}
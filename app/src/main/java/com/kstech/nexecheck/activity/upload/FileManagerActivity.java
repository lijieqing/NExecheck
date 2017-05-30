package com.kstech.nexecheck.activity.upload;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.config.vo.FtpServerVO;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.fragment.LocalFragment;
import com.kstech.nexecheck.view.fragment.RemoteFragment;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

public class FileManagerActivity extends BaseActivity {
    private FragmentManager manager;
    private FragmentTransaction trascation;
    private LocalFragment localFragment;
    private RemoteFragment remoteFragment;
    private FTPClient ftpClient;
    private FileInputStream fis;
    private boolean isUpLoad = true;
    private String downLoadPath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_down_load);

        // 初始化菜单，传入参数，设置子标题
        updateSubtitle("---  配置文件管理");

        manager = getFragmentManager();
        trascation = manager.beginTransaction();
        localFragment = new LocalFragment();
        remoteFragment = new RemoteFragment();
        trascation.replace(R.id.ll_local,localFragment,"local");
        trascation.replace(R.id.ll_ftp,remoteFragment,"remote");
        trascation.commit();
    }

    @Override
    public Activity getactivity() {
        return this;
    }

    public void upload(View view) {
        ftpClient = remoteFragment.ftp.getFtpClient();
        if(Globals.upload.size()>0 && isUpLoad){
            new Thread(){
                @Override
                public void run() {
                    ftpUpload(Globals.upload.get(0));
                }
            }.start();
        }else {
            Toast.makeText(this,"正在上传，请稍后！！！！！",Toast.LENGTH_SHORT).show();
        }
    }

    public void copyDataBase(View view) {
        copyDB();
    }
    public void copyDB() {
        String path  = getactivity().getDatabasePath("newBee").getPath();
        File f = new File(path);
        File target = new File("/storage/sdcard1/DB/"+Globals.getCurrentUser().getName()+"_"+ DateUtil.getDateTimeFormat14(new Date())+".db");
        if(f.exists()){
            try {
                File db = new File("/storage/sdcard1/DB");
                if(!db.exists()){
                    db.mkdir();
                }
                FileChannel outF = new FileOutputStream(target).getChannel();
                FileChannel inF = new FileInputStream(f).getChannel();
                inF.transferTo(0,f.length(),outF);
                inF.close();
                outF.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            localFragment.localHandler.sendEmptyMessage(0);
            Toast.makeText(getApplicationContext(),"数据库备份完成，文件路径:/SD卡/DB/"+target.getName(),Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(),"数据库备份失败，请重试",Toast.LENGTH_SHORT).show();
        }
    }
    public void download(View view) {

        if(Globals.download.size()>0){
            Log.i("hahah",">>>>>>>>>>下载-------------");
            new Thread(){
                @Override
                public void run() {
                    fileDown();
                    remoteFragment.handler.sendEmptyMessage(2);
                }
            }.start();
        }
    }

    private void fileDown(){
        downLoadPath = FTP.REMOTE_PATH + Globals.REMOTE_FILE + "/";
        try {
            Log.i("hahah",">>>>>====下载到路径====="+Globals.LOCAL_CURRENT_FILE);
            remoteFragment.ftp.download(downLoadPath,Globals.download.get(0),Globals.LOCAL_CURRENT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String ftpUpload(String fileName) {

        String returnMessage = "0";
        try {
            isUpLoad = false;
            if(ftpClient == null){
                ftpClient = new FTPClient();
                FtpServerVO ftpServerVO = Globals.getResConfig().getFtpServerVO();
                ftpClient.connect(ftpServerVO.getIp(), ftpServerVO.getPort());
                boolean loginResult = ftpClient.login(ftpServerVO.getUser(), ftpServerVO.getPassword());
                int returnCode = ftpClient.getReplyCode();
                Log.i("hahah", "-------------%%%%%%%%------" + Globals.LOCAL_CURRENT_FILE+ Globals.upload.get(0));
                fis = new FileInputStream(Globals.LOCAL_CURRENT_FILE+ Globals.upload.get(0));
                if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                    ftpClient.makeDirectory("/upload");
                    ftpClient.changeWorkingDirectory("/upload");
                    ftpClient.setBufferSize(8192);
                    ftpClient.setControlEncoding("UTF-8");
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.storeFile(fileName, fis);
                    ftpClient.setFileType(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
                    returnMessage = "1";   //上传成功
                    remoteFragment.handler.sendEmptyMessage(1);
                }
            }else {
                fis = new FileInputStream(Globals.LOCAL_CURRENT_FILE+ Globals.upload.get(0));
                ftpClient.makeDirectory("/upload");
                ftpClient.changeWorkingDirectory("/upload");
                ftpClient.setBufferSize(8192);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(fileName, fis);
                ftpClient.setFileType(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
                returnMessage = "1";   //上传成功
                remoteFragment.handler.sendEmptyMessage(1);
            }
            isUpLoad = true;

        } catch (IOException e) {
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"请刷新尝试！！！！",Toast.LENGTH_SHORT).show();
            Looper.loop();
            throw new RuntimeException("FTP客户端出错！", e);
        }
        return returnMessage;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

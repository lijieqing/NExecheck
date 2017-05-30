package com.kstech.nexecheck.view.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.upload.FTP;
import com.kstech.nexecheck.domain.config.vo.FtpServerVO;
import com.kstech.nexecheck.utils.FormatUtil;
import com.kstech.nexecheck.utils.Globals;

import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/9/18.
 * ftp端文件展示
 */
public class RemoteFragment extends Fragment {
    private TextView tvStatus;
    private Button btnRefresh;
    private ListView lvRemote;
    public FTP ftp;
    private List<FTPFile> remoteFile;
    private MyRemoteAdapter remoteAdapter;

    private String downLoadPath = "";
    private String ftpCurrentPath = FTP.REMOTE_PATH;
    /**
     * 文件夹显示图片.
     */
    private Bitmap icon1;

    /**
     * 文件显示图片.
     */
    private Bitmap icon2;

    private LocalFragment localFragment;

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    tvStatus.setText("FTP链接成功");
                    remoteAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    Toast.makeText(getActivity(),"数据更新成功",Toast.LENGTH_SHORT).show();
                    localFragment = (LocalFragment) getActivity().getFragmentManager().findFragmentByTag("local");
                    localFragment.localHandler.sendEmptyMessage(1);
                    new Thread(){
                        @Override
                        public void run() {
                            loadRemoteView();
                            if(remoteFile.size()>0){
                                SystemClock.sleep(500);
                                handler.sendEmptyMessage(0);
                                System.out.print("刷新成功");
                            }
                        }
                    }.start();
                    break;
                case 2:
                    mLastPosition = -1;
                    Globals.download.clear();
                    Toast.makeText(getActivity(),"数据下载完成！！",Toast.LENGTH_SHORT).show();
                    localFragment = (LocalFragment) getActivity().getFragmentManager().findFragmentByTag("local");
                    localFragment.localHandler.sendEmptyMessage(0);
                    handler.sendEmptyMessage(1);
                    break;
                case 3:
                    Toast.makeText(getActivity(),"连接异常，请刷新一下！！！！！！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private int mLastPosition = -1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        icon1 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.folder);
        icon2 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.doc);
        remoteFile = new ArrayList<FTPFile>();
        new Thread(){
            @Override
            public void run() {
                loadRemoteView();
                if(remoteFile.size()>0){
                    SystemClock.sleep(500);
                    handler.sendEmptyMessage(0);
                }
            }
        }.start();

    }
    private void loadRemoteView() {
        try {
            if (ftp == null) {
                FtpServerVO ftpServerVO = Globals.getResConfig().getFtpServerVO();
                ftp = new FTP(ftpServerVO.getIp(),ftpServerVO.getUser(),ftpServerVO.getPassword());
            }
            // 打开FTP服务
            ftp.openConnect();
            // 初始化FTP列表

            // 加载FTP列表
            remoteFile = ftp.listFiles(FTP.REMOTE_PATH);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_remote,null);
        tvStatus = (TextView) view.findViewById(R.id.tv_status);
        btnRefresh = (Button) view.findViewById(R.id.btn_connect);
        lvRemote = (ListView) view.findViewById(R.id.remote_list);
        remoteAdapter = new MyRemoteAdapter();
        lvRemote.setAdapter(remoteAdapter);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        loadRemoteView();
                        if(remoteFile.size()>0){
                            SystemClock.sleep(500);
                            handler.sendEmptyMessage(0);
                            System.out.print("刷新成功");
                        }
                    }
                }.start();
            }
        });

        lvRemote.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (remoteFile.get(i).isDirectory()) {
                    Globals.download.clear();
                    mLastPosition = -1;
                    if(!"..".equals(FormatUtil.convertString(remoteFile.get(i).getName(), "UTF-8"))){
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    downLoadPath = FTP.REMOTE_PATH + remoteFile.get(i).getName() + "/";
                                    Log.i("hahah",">>>>>>>>>>remote file<<<<<<<<<<<<<<<<<<<"+downLoadPath);
                                    remoteFile = ftp.listFiles(downLoadPath);
                                    Log.i("hahah",">>>>>>>>>>remote file size<<<<<<<<<<<<<<<<<<<"+remoteFile.size());
                                    handler.sendEmptyMessage(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    handler.sendEmptyMessage(3);
                                }
                            }
                        }.start();
                    }
                    Globals.REMOTE_FILE = remoteFile.get(i).getName();
                }else {
                    RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.bg_list);
                    if(!Globals.download.contains(remoteFile.get(i).getName())){
                        rl.setBackgroundColor(Color.GRAY);
                        mLastPosition =i;
                        Globals.download.clear();
                        Globals.download.add(remoteFile.get(i).getName());
                        remoteAdapter.notifyDataSetChanged();
                    }else {
                        mLastPosition = -1;
                        rl.setBackgroundColor(Color.WHITE);
                        Globals.download.remove(remoteFile.get(i).getName());
                    }
                }
            }
        });



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    class MyRemoteAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return remoteFile.size();
        }

        @Override
        public FTPFile getItem(int i) {
            return remoteFile.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                // 设置视图
                view = View.inflate(getActivity(),R.layout.list_local_file, null);
                // 获取控件实例
                holder = new ViewHolder();
                holder.icon = (ImageView) view.findViewById(R.id.image_icon);
                holder.fileName = (TextView) view.findViewById(R.id.text_name);
                holder.fileSize = (TextView) view.findViewById(R.id.text_size);
                holder.rl = (RelativeLayout) view.findViewById(R.id.bg_list);
                // 设置标签
                view.setTag(holder);
            } else {
                // 获取标签
                holder = (ViewHolder) view.getTag();
            }
            // 获取文件名
            holder.fileName.setText(FormatUtil.convertString(remoteFile.get(position).getName(), "UTF-8"));
            if (!remoteFile.get(position).isDirectory()) {
                // 获取显示文件图片
                holder.icon.setImageBitmap(icon2);
                // 获取文件大小
                //holder.fileSize.setText(FormatUtil.getFormatSize(remoteFile.get(position).getSize()));
            } else {
                // 获取显示文件夹图片
                holder.icon.setImageBitmap(icon1);
            }
            if(position == mLastPosition){
                holder.rl.setBackgroundColor(Color.GRAY);
            }else {
                holder.rl.setBackgroundColor(Color.WHITE);
            }

            return view;
        }

        /**
         * 获取控件.
         */
        private class ViewHolder {
            /**
             * 图片.
             */
            private ImageView icon;

            /**
             * 文件名.
             */
            private TextView fileName;

            /**
             * 文件大小.
             */
            private TextView fileSize;

            private RelativeLayout rl ;
        }
    }}

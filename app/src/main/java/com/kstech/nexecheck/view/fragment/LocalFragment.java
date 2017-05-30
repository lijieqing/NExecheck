package com.kstech.nexecheck.view.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.utils.Globals;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


/**
 * Created by lenovo on 2016/9/18.
 * fragment 本地文件展示界面
 */
public class LocalFragment extends Fragment {
    private static final String LOCAL_PATH = "/storage/sdcard1/";
    private ArrayList<File> localFile = null;
    private ListView localList;
    private TextView tvBack;
    private String currentPath = LOCAL_PATH;
    private String forwardPath ="";
    private MyLocalAdapter adapter;
    private int mLastPosition = -1;
    public Handler localHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    localFile.clear();
                    currentPath = LOCAL_PATH;
                    getFileDir(LOCAL_PATH);
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    mLastPosition = -1;
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    /**
     * 文件夹显示图片.
     */
    private Bitmap icon1;

    /**
     * 文件显示图片.
     */
    private Bitmap icon2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localFile = new ArrayList<File>();
        getFileDir(LOCAL_PATH);
        icon1 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.folder);
        icon2 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.doc);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_local,null);
        localList = (ListView) view.findViewById(R.id.local_list);
        tvBack = (TextView) view.findViewById(R.id.tv_back);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPath.equals(LOCAL_PATH)){
                    Toast.makeText(getActivity(),"已经是根目录",Toast.LENGTH_SHORT).show();
                } else {
                    if(currentPath.equals(forwardPath)){
                        forwardPath = path(forwardPath);
                        localFile.clear();
                        getFileDir(forwardPath);
                        currentPath = forwardPath;
                        Globals.LOCAL_CURRENT_FILE = currentPath;
                        adapter.notifyDataSetChanged();
                    }else {
                        localFile.clear();
                        getFileDir(forwardPath);
                        currentPath = forwardPath;
                        Globals.LOCAL_CURRENT_FILE = currentPath;
                        adapter.notifyDataSetChanged();
                    }
                    mLastPosition = -1;
                    Globals.upload.clear();
                }
            }
        });
        adapter = new MyLocalAdapter();
        localList.setAdapter(adapter);
        localList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(localFile.get(i).isDirectory()){
                    String name = localFile.get(i).getName();
                    forwardPath = localFile.get(0).getParent()+"/";
                    Log.i("hahah","foward parent-------"+name+localFile.get(0).getParent());
                    localFile.clear();
                    currentPath = currentPath+name+"/";
                    Log.i("hahah","current >>>====="+currentPath);
                    getFileDir(currentPath);
                    Globals.LOCAL_CURRENT_FILE = currentPath;
                    adapter.notifyDataSetChanged();
                }else{
                    RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.bg_list);
                    if(!Globals.upload.contains(localFile.get(i).getName())){
                        rl.setBackgroundColor(Color.GRAY);
                        mLastPosition =i;
                        Globals.upload.clear();
                        Log.i("hahah",">>>要上传的文件===="+localFile.get(i).getName());
                        Globals.upload.add(localFile.get(i).getName());
                        adapter.notifyDataSetChanged();
                    }else {
                        mLastPosition = -1;
                        rl.setBackgroundColor(Color.WHITE);
                        Globals.upload.remove(localFile.get(i).getName());
                    }

                }
            }
        });
        return view;
    }
    private  String path(String s){
        String result = "";
        if(s.lastIndexOf("/") == s.length()-1){
            s = s.substring(1);
            String[] r = s.split("/");
            StringBuffer sb = new StringBuffer();
            sb.append("/");
            for(int i=0;i<r.length-1;i++){
                sb.append(r[i]+"/");
                System.out.println(r[i]);
            }
            result =sb.toString();
            result.trim();
            System.out.println(result);
        }
        return result;
    }
    private void getFileDir(String filePath) {
        // 获取根目录
        File f = new File(filePath);
        // 获取根目录下所有文件
        File[] files = f.listFiles();
        // 循环添加到本地列表
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isHidden() || file.getName().equals("LOST.DIR")) {
                continue;
            }
            localFile.add(file);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    class MyLocalAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return localFile.size();
        }

        @Override
        public File getItem(int i) {
            return localFile.get(i);
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
                holder.fileName = (TextView) view.findViewById(R.id.text_name);
                holder.fileSize = (TextView) view.findViewById(R.id.text_size);
                holder.icon = (ImageView) view.findViewById(R.id.image_icon);
                holder.rl = (RelativeLayout) view.findViewById(R.id.bg_list);
                // 设置标签
                view.setTag(holder);
            } else {
                // 获取标签
                holder = (ViewHolder) view.getTag();
            }
            // 获取文件
            File file = localFile.get(position);
            // 判断是否为一个目录
            if (!file.isDirectory()) {
                try {
                    // 创建输入流
                    FileInputStream inputStream = new FileInputStream(file);
                    // 获得流大小
                    double size = (double) inputStream.available() / 1;
                    // 获取文件大小
                    //holder.fileSize.setText(FormatUtil.getFormatSize(size));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 获取文件名
            holder.fileName.setText(file.getName());
            if (file.isDirectory()) {
                // 获取显示文件夹图片
                holder.icon.setImageBitmap(icon1);
            } else {
                // 获取显示文件图片
                holder.icon.setImageBitmap(icon2);
            }
            if(position==mLastPosition){
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
            private RelativeLayout rl;
        }
    }
}

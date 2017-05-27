package com.kstech.nexecheck.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.checkline.CheckLineLoadTask;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 标题头 相关变量
     */
    // 子标题
    private TextView subTitle;
    public TextView checkLineNameTV, userManagerRowUpTV, dataBaseRowUpTV;
    // 菜单按钮
    private ImageView indexMenuId;

    private ImageView netWorkStatus;
    // 菜单中的行
    private TableRow dataUploadRowId, userManagerRowId, modPwdRowId,
            setCheckLineRowId, exitSystemRowId, fileDownLoadRowId, dataBaseRowID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式状态栏 sdk 21以上
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) actionbar.hide();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 设置为横屏
         */
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // 设置检线名称
        if (null != checkLineNameTV) {
            checkLineNameTV.setText(Globals.getCurrentCheckLine() == null ? "未连接" : Globals.getCurrentCheckLine().getName());
        }

    }
    /**
     * 初始化菜单
     *
     * @param subTitleName 子菜单名称
     */
    public void initMenu(String subTitleName) {
        subTitle = (TextView) findViewById(R.id.subTitle);

        checkLineNameTV = (TextView) findViewById(R.id.checkLineNameTV);

        // 初始化菜单组件
        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.home_menu, null);
        final PopupWindow changeStatusPopUp = new PopupWindow(this);
        changeStatusPopUp.setContentView(layout);
        changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setFocusable(true);
        netWorkStatus = (ImageView) findViewById(R.id.connStatusId);
        //connStatusReceiver.initConnStatusImageView(this);

        // 初始化菜单按钮
        indexMenuId = (ImageView) findViewById(R.id.indexMenuId);
        indexMenuId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int yHeight = -160;
                if (Globals.getCurrentUser().getType().getCode().equals("1")) {
                    yHeight = -247;
                }
                changeStatusPopUp.showAtLocation(layout, Gravity.RIGHT, 0,
                        yHeight);
            }
        });

        // 初始化菜单组件下的用户管理
        userManagerRowId = (TableRow) layout.findViewById(R.id.userManagerRowId);
        dataBaseRowID = (TableRow) layout.findViewById(R.id.DataBaseRowId);
        // 如果是检测员，则隐藏用户管理模块
        if (Globals.getCurrentUser().getType().getCode().equals("1")) {
            userManagerRowId.setVisibility(View.GONE);
            userManagerRowUpTV = (TextView) layout.findViewById(R.id.userManagerRowUpTV);
            userManagerRowUpTV.setVisibility(View.GONE);

            dataBaseRowID.setVisibility(View.GONE);
            dataBaseRowUpTV = (TextView) layout.findViewById(R.id.DataBaseRowUpTV);
            dataBaseRowUpTV.setVisibility(View.GONE);

        } else {
            userManagerRowId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
//                    Intent intent = new Intent(this, CurrentUserActivity.class);
//                    startActivity(intent);
                    changeStatusPopUp.dismiss();
                }
            });
            dataBaseRowID.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeStatusPopUp.dismiss();
                    DBInject();
                }
            });
        }

        fileDownLoadRowId = (TableRow) layout.findViewById(R.id.fileDownLoadRowId);
        fileDownLoadRowId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                Intent intent = new Intent(this, FileManagerActivity.class);
//                startActivity(intent);
                changeStatusPopUp.dismiss();
            }
        });

        // 初始化菜单组件下的数据上传
        dataUploadRowId = (TableRow) layout.findViewById(R.id.dataUploadRowId);
        dataUploadRowId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                Intent intent = new Intent(this, DataUploadActivity.class);
//                startActivity(intent);
                changeStatusPopUp.dismiss();
            }
        });

        // 初始化菜单组件下的修改密码
        modPwdRowId = (TableRow) layout.findViewById(R.id.modPwdRowId);
        modPwdRowId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                Intent intent = new Intent(this, ModPwdActivity.class);
//                startActivity(intent);
                changeStatusPopUp.dismiss();
            }
        });

        // 初始化菜单组件下的检线设置
        setCheckLineRowId = (TableRow) layout
                .findViewById(R.id.setCheckLineRowId);
        setCheckLineRowId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                changeStatusPopUp.dismiss();
                // 获取检线
                CheckLineLoadTask loadTask = new CheckLineLoadTask(checkLineNameTV,getactivity());
                loadTask.execute(100);
            }
        });
        // 初始化菜单组件下的退出系统按钮
        exitSystemRowId = (TableRow) layout.findViewById(R.id.exitSystemRowId);
        exitSystemRowId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                changeStatusPopUp.dismiss();
                new AlertDialog.Builder(getactivity())
                        .setTitle(R.string.diaLogWakeup)
                        .setMessage(R.string.exitSystemConfirm)
                        .setNegativeButton(R.string.str_close, null)
                        .setPositiveButton(R.string.str_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                }).show();
            }
        });

        subTitle.setText(subTitleName);
        checkLineNameTV.setText(Globals.getCurrentCheckLine().getName());
    }

    /**
     * 数据库导入
     */
    private void DBInject() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getactivity());
        AlertDialog salert = null;
        View view = View.inflate(getactivity(), R.layout.dblist_ll, null);
        File file = new File("/storage/sdcard1/DB");
        ListView lv = (ListView) view.findViewById(R.id.lv_db);
        TextView tv = (TextView)view.findViewById(R.id.tv_data_num);
        final List<String> values = new ArrayList<>();
        if(file.exists()){
            File[] files = file.listFiles();
            for (File f:files) {
                if(f.getName().matches("\\w+.db")){
                    values.add(f.getName());
                }
            }
            if(values.size()==0){
                tv.setText("DB文件夹下未找到数据库文件");
            }else {
                tv.setText("共扫描到"+values.size()+"个数据库");
            }
            lv.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return values.size();
                }

                @Override
                public Object getItem(int position) {
                    return values.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if(convertView==null){
                        convertView = View.inflate(getactivity(),R.layout.ll_data_item,null);
                    }
                    TextView tv = (TextView) convertView.findViewById(R.id.dbfile);
                    tv.setText(values.get(position));
                    return convertView;
                }
            });
            salert = dialog.setView(view).create();
            final AlertDialog finalSalert = salert;
            salert.setCanceledOnTouchOutside(true);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    finalSalert.dismiss();
                    final String name = values.get(position);
                    new AlertDialog.Builder(getactivity())
                            .setTitle("确定？")
                            .setMessage("确定要把数据库"+name+"导入吗？")
                            .setNegativeButton("取消",null)
                            .setPositiveButton("导入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean suc = SystemUtil.copyDB(name,getactivity());
                                    if(suc){
                                        Toast.makeText(getactivity(),"数据库成功导入",Toast.LENGTH_SHORT).show();
                                        //startActivityForResult(new Intent(getactivity(),OpenCheckRecordActivity.class),1);
                                    }else {
                                        Toast.makeText(getactivity(),"导入失败，请重试",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                }
            });
            salert.show();
        }else {
            Toast.makeText(getactivity(),"备份数据库不存在，备份数据库应存放在\"SD卡/DB\"中--",Toast.LENGTH_SHORT).show();
        }
    }

    //调用接口 子类实现返回activity实例 很优雅
    public abstract Activity getactivity();

    public void updateSubtitle(String title){
        subTitle.setText(title);
    }

}

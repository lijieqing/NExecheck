package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.LoginUserListAdapter;
import com.kstech.nexecheck.engine.CheckLineLoadTask;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.db.dao.UserDao;
import com.kstech.nexecheck.domain.db.dbenum.UserStatusEnum;
import com.kstech.nexecheck.domain.db.entity.User;
import com.kstech.nexecheck.utils.DeviceUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    // 用户名
    private TextView userNameET, checkLineET;
    private EditText passwordET;
    // 密码
    private Button loginBtn;
    private LinearLayout userNameLy, checkLineLy;

    private ImageView ivToUpgrade;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                // 如果不是首次登录，则默认显示上一次登录的用户
                String lastUserName = ConfigFileManager.getInstance(getApplicationContext()).getLastUserName();
                if (lastUserName == null || lastUserName.length() == 0) {
                    userNameET.setText(UserDao.getAdminName());
                } else {
                    userNameET.setText(lastUserName);
                }
                // 默认显示上一次检线的名称
                checkLineET.setText(Globals.getCurrentCheckLine().getName());
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        new Thread(){
            @Override
            public void run() {
                Globals.initResource(getApplicationContext());
                handler.sendEmptyMessage(1);
            }
        }.start();
    }

    @Override
    public Activity getactivity() {
        return this;
    }

    /*
    * 初始化视图组件
    */
    private void initView() {
        // 用户名组件
        userNameET = (TextView) findViewById(R.id.userNameET);
        // 密码组件
        passwordET = (EditText) findViewById(R.id.passwordET);
        // 检线
        checkLineET = (TextView) findViewById(R.id.checkLineET);
        // 登录按钮组件
        loginBtn = (Button) findViewById(R.id.loginBtn);
        // 整个用户名的layout框
        userNameLy = (LinearLayout) findViewById(R.id.userNameLy);
        checkLineLy = (LinearLayout) findViewById(R.id.checkLineLy);
        ivToUpgrade = (ImageView) findViewById(R.id.iv_to_xml_edit);

        ivToUpgrade.setOnClickListener(new View.OnClickListener() {
            long[] mHints = new long[4];//初始全部为0
            @Override
            public void onClick(View view) {
                //将mHints数组内的所有元素左移一个位置
                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                //获得当前系统已经启动的时间
                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis()-mHints[0]<=1000) {
                    startActivity(new Intent(LoginActivity.this, UpgradeActivity.class));
                    finish();
                }
            }
        });

        loginBtn.setOnClickListener(this);
        userNameLy.setOnClickListener(this);
        checkLineLy.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.userNameLy:
                // 展示用户选择dialog
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View login_user_view = inflater.inflate(R.layout.login_user_view, null);
                ListView loginUserList = (ListView) login_user_view.findViewById(R.id.loginUserView);
                final List<Map<String, Object>> result = UserDao.findUserListReturnListMap(UserStatusEnum.ENABLE.getCode(),this);
                LoginUserListAdapter adapter = new LoginUserListAdapter(this, result);
                loginUserList.setAdapter(adapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(login_user_view);
                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout(DeviceUtil.deviceWidth(this)/5, DeviceUtil.deviceHeight(this)/4);
                // end

                loginUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        // 显示选择的用户名称
                        userNameET.setText(result.get(arg2).get("name").toString());
                        dialog.cancel();
                    }
                });
                break;
            case R.id.checkLineLy:
                // 获取检线
                CheckLineLoadTask loadTask = new CheckLineLoadTask(checkLineET,this);
                loadTask.execute(100);
                break;
            case R.id.loginBtn:
                String userName = userNameET.getText().toString();
                String pwd = passwordET.getText().toString();

                // 校验页面必填项信息
                if ("".equals(pwd)) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(R.string.passwordEmpty)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }

                User currentUser = UserDao.login(userName, pwd,this);
                if (currentUser == null) {// 说明登录失败
                    // 错误提示
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(R.string.passwordError)
                            .setNeutralButton(R.string.str_ok, null).show();
                } else {
                    ConfigFileManager.getInstance(this).saveCheckLineName(Globals.getCurrentCheckLine().getName());
                    ConfigFileManager.getInstance(this).saveCheckLineSsid(Globals.getCurrentCheckLine().getSsid());
                    // 跳转到管理员首页
                    Intent adminIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(adminIntent);
                    finish();
                }
                break;
        }
    }
}

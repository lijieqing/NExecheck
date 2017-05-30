package com.kstech.nexecheck.activity.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.CurrentUserListAdapter;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.dao.UserDao;
import com.kstech.nexecheck.domain.db.dbenum.UserStatusEnum;
import com.kstech.nexecheck.domain.db.dbenum.UserTypeEnum;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@SuppressLint("NewApi")
public class CurrentUserActivity extends BaseActivity {

    // 用户列表组件
    private ListView listView = null;

    // 定义页面组件
    private TextView addUserETForEdit, addTimeETForEdit;
    private EditText userNameET, userCodeET, userTypeET, pwdET, pwdConfirmET;
    private EditText userNameETForEdit, userCodeETForEdit, userOldNameForEdit,
            userOldCodeForEdit, pwdETForEdit, pwdConfirmETForEdit;
    private TextView userTypeSPForEdit;
    private Button addCheckBtn, addAdminBtn, saveUserBtn, backBtn,
            showHistoryUserBtn;
    private TableLayout addUserTable, editUserTable;
    // 用户修改类型选择
    private TextView userTypeSpinnerAdminTV,userTypeSpinnerCheckTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user);
        // 初始化菜单
        updateSubtitle("---   用户管理");

        // 初始化用户列表
        initUserList();

        // 初始化页面组件
        initViewComp();
    }

    @Override
    public Activity getactivity() {
        return this;
    }

    /**
     * 初始化用户列表
     */
    public void initUserList() {
        listView = (ListView) findViewById(R.id.userlist);
        List<Map<String, Object>> list = UserDao.findUserListReturnListMap(UserStatusEnum.ENABLE.getCode(),getactivity());
        listView.setAdapter(new CurrentUserListAdapter(this, list));
        listView.setOnItemClickListener(listViewOnitemListener);
    }

    // 当前操作 类型-----AddUser：用户添加，EditUser：用户修改。默认进来是添加用户
    private String currentOperator = "AddUser";
    /*
     * 添加检验员按钮，监听事件
     */
    View.OnClickListener addCheckBtnListener = new View.OnClickListener() {
        @SuppressLint("NewApi")
        public void onClick(View arg0) {
            addUserTable.setVisibility(View.VISIBLE);
            editUserTable.setVisibility(View.GONE);
            currentOperator = "AddUser";
            userTypeET.setText(UserTypeEnum.CHECKER.getCode());

            addCheckBtn.setBackground(getDrawable(R.drawable.login_btn));
            addAdminBtn.setBackground(getDrawable(R.drawable.btn_normal_rect_black));

            if (null != selectedLine) {
                ((TextView)selectedLine.getChildAt(2)).setTextColor(getResources().getColor(R.color.userListTextColor));
                selectedLine = null;
            }
        }
    };

    /*
     * 添加管理员按钮，监听事件
     */
    View.OnClickListener addAdminBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            addUserTable.setVisibility(View.VISIBLE);
            editUserTable.setVisibility(View.GONE);
            currentOperator = "AddUser";
            userTypeET.setText(UserTypeEnum.MANAGER.getCode());

            addAdminBtn.setBackground(getDrawable(R.drawable.login_btn));
            addCheckBtn.setBackground(getDrawable(R.drawable.btn_normal_rect_black));
        }
    };

    // 当前选中的用户
    private LinearLayout selectedLine;
    /*
     * listView点击，监听事件
     */
    OnItemClickListener listViewOnitemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View currentLine, int arg2,
                                long arg3) {
            if (null != selectedLine) {
                ((TextView)selectedLine.getChildAt(2)).setTextColor(getResources().getColor(R.color.userListTextColor));
            }
            selectedLine = (LinearLayout)currentLine;
            ((TextView)selectedLine.getChildAt(2)).setTextColor(getResources().getColor(R.color.userListSelectedColor));

            addUserTable.setVisibility(View.GONE);
            editUserTable.setVisibility(View.VISIBLE);
            currentOperator = "EditUser";
            addCheckBtn.setBackground(getDrawable(R.drawable.btn_normal_rect_black));
            addAdminBtn.setBackground(getDrawable(R.drawable.btn_normal_rect_black));

            // arg0:就是你的listview arg2:点击的item的位置。和你的数组的下标相等。arg3:被电击view的id
            HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(arg2);
            String code = (String) map.get("code");
            String name = (String) map.get("name");
            Cursor user = DatabaseManager.getInstance(getactivity()).query("user", null,
                    "code=? and name=?", new String[] { code, name }, null,
                    null, null);
            if (user.moveToNext()) {
                userNameETForEdit.setText(name);
                userOldNameForEdit.setText(name);
                userCodeETForEdit.setText(code);
                userOldCodeForEdit.setText(code);
                userTypeSPForEdit.setText(UserTypeEnum.getName(user.getString(user
                        .getColumnIndex("type"))));
                pwdETForEdit
                        .setText(user.getString(user.getColumnIndex("pwd")));
                pwdConfirmETForEdit.setText(user.getString(user
                        .getColumnIndex("pwd")));
                addUserETForEdit.setText(user.getString(user
                        .getColumnIndex("creator_code")));
                addTimeETForEdit.setText(user.getString(user
                        .getColumnIndex("create_time")));
            }
        }
    };

    /*
     * 查看历史用户，监听事件
     */
    View.OnClickListener showHistoryUserBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            Intent intent = new Intent(CurrentUserActivity.this,
                    HistoryUserActivity.class);
            startActivity(intent);
            CurrentUserActivity.this.finish();
        }
    };

    /*
     * 保存用户，监听事件
     */
    View.OnClickListener saveUserBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            ContentValues cv = new ContentValues();
            // 判断当前操作类型--AddUser:添加用户，EditUser：修改用户
            if (currentOperator.equals("AddUser")) {
                // 校验用户名与密码是否重复
                boolean vflag = validateUserNameAndCode(userNameET.getText()
                        .toString(), userCodeET.getText().toString(), "", "",pwdET
                        .getText().toString(), pwdConfirmET.getText()
                        .toString());
                // 添加用户
                if (vflag) {
                    cv.put("name", userNameET.getText().toString());
                    cv.put("code", userCodeET.getText().toString());
                    cv.put("pwd", pwdET.getText().toString());
                    cv.put("type", userTypeET.getText().toString());
                    cv.put("status", UserStatusEnum.ENABLE.getCode());
                    cv.put("create_time", DateUtil.getDateTimeFormat(new Date()));
                    // 从上下文中获取当前用户工号
                    String currentCode = Globals.getCurrentUser().getCode();
                    cv.put("creator_code", currentCode);
                    DatabaseManager.getInstance(getactivity()).insert("user", cv);
                } else {
                    return;
                }
            } else if (currentOperator.equals("EditUser")) {
                // 校验用户名与密码是否重复
                boolean vflag = validateUserNameAndCode(
                        userNameETForEdit.getText().toString(),
                        userCodeETForEdit.getText().toString(),
                        userOldNameForEdit.getText().toString(),
                        userOldCodeForEdit.getText().toString(), pwdETForEdit
                                .getText().toString(), pwdConfirmETForEdit
                                .getText().toString());
                // 修改当前用户,工号和姓名可以与自己重复，但是不能与别人重复
                if (vflag) {
                    cv.put("name", userNameETForEdit.getText().toString());
                    cv.put("code", userCodeETForEdit.getText().toString());
                    cv.put("pwd", pwdETForEdit.getText().toString());
                    // 下拉框 type 0:管理员 1：检验员
                    cv.put("type", UserTypeEnum.getCode(userTypeSPForEdit.getText().toString()));
                    DatabaseManager.getInstance(getactivity()).update(
                            "user",
                            cv,
                            "code=?",
                            new String[] { userOldCodeForEdit.getText()
                                    .toString() });
                } else {
                    return;
                }
            }
            // 刷新用户列表
            initUserList();
            Toast.makeText(CurrentUserActivity.this, R.string.saveSuccess,
                    Toast.LENGTH_LONG).show();
        }
    };

    /*
     * 退出按钮，监听事件
     */
    View.OnClickListener backBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            CurrentUserActivity.this.finish();
        }
    };

    // 初始化页面组件
    private void initViewComp() {
        userNameET = (EditText) findViewById(R.id.userNameET);
        userCodeET = (EditText) findViewById(R.id.userCodeET);
        userTypeET = (EditText) findViewById(R.id.userTypeET);
        pwdET = (EditText) findViewById(R.id.pwdET);
        pwdConfirmET = (EditText) findViewById(R.id.pwdConfirmET);

        userNameETForEdit = (EditText) findViewById(R.id.userNameETForEdit);
        userOldNameForEdit = (EditText) findViewById(R.id.userOldNameForEdit);
        userCodeETForEdit = (EditText) findViewById(R.id.userCodeETForEdit);
        userOldCodeForEdit = (EditText) findViewById(R.id.userOldCodeForEdit);
        userTypeSPForEdit = (TextView) findViewById(R.id.userTypeSPForEdit);

        LayoutInflater layoutInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(
                R.layout.user_type_spinner, null);
        final PopupWindow changeStatusPopUp = new PopupWindow(this);
        changeStatusPopUp.setContentView(layout);
        changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setBackgroundDrawable(getDrawable(R.drawable.rect_gray));
        changeStatusPopUp.setFocusable(true);
        userTypeSPForEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                changeStatusPopUp
                        .showAtLocation(layout, Gravity.CENTER, 191, -85);
            }
        });
        userTypeSpinnerAdminTV = (TextView)layout.findViewById(R.id.userTypeSpinnerAdminTV);
        userTypeSpinnerAdminTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                userTypeSPForEdit.setText(userTypeSpinnerAdminTV.getText());
                changeStatusPopUp.dismiss();
            }
        });
        userTypeSpinnerCheckTV = (TextView)layout.findViewById(R.id.userTypeSpinnerCheckTV);
        userTypeSpinnerCheckTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                userTypeSPForEdit.setText(userTypeSpinnerCheckTV.getText());
                changeStatusPopUp.dismiss();
            }
        });
//		userTypeSPForEdit.setAdapter(new ArrayAdapter<String>(this,
//				R.layout.user_type_spinner, new String[] { UserTypeEnum.MANAGER.getName(),
//						UserTypeEnum.CHECKER.getName() }));

        pwdETForEdit = (EditText) findViewById(R.id.pwdETForEdit);
        pwdConfirmETForEdit = (EditText) findViewById(R.id.pwdConfirmETForEdit);
        addUserETForEdit = (TextView) findViewById(R.id.addUserETForEdit);
        addTimeETForEdit = (TextView) findViewById(R.id.addTimeETForEdit);

        addCheckBtn = (Button) findViewById(R.id.addCheckBtn);
        addCheckBtn.setOnClickListener(addCheckBtnListener);

        addAdminBtn = (Button) findViewById(R.id.addAdminBtn);
        addAdminBtn.setOnClickListener(addAdminBtnListener);

        showHistoryUserBtn = (Button) findViewById(R.id.showHistoryUserBtn);
        showHistoryUserBtn.setOnClickListener(showHistoryUserBtnListener);

        saveUserBtn = (Button) findViewById(R.id.saveUserBtn);
        saveUserBtn.setOnClickListener(saveUserBtnListener);

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnListener);

        addUserTable = (TableLayout) findViewById(R.id.addUserTable);
        editUserTable = (TableLayout) findViewById(R.id.editUserTable);
    }

    /**
     * 校验用户名与密码是否重复
     * @param name
     * @param code
     * @return
     */
    public boolean validateUserNameAndCode(String name, String code,
                                           String oldName, String oldCode, String pwd, String confirmPwd) {
        int message = 0;
        // 如果姓名和工号都没有改变，则没问题
        if ("".equals(name) || "".equals(code) || "".equals(pwd) || "".equals(confirmPwd)){
            message = R.string.pleaseInputAllMsg;
        } else if (!name.equals(oldName) || "".equals(oldName)) {
            Cursor nameValid = DatabaseManager.getInstance(getactivity()).query("user",
                    null, "name=?", new String[] { name }, null, null, null);
            if (nameValid.moveToNext()) {
                message = R.string.nameRepeat;
            }
        } else if (!code.equals(oldCode) || "".equals(oldCode)) {
            Cursor codeValid = DatabaseManager.getInstance(getactivity()).query("user",
                    null, "code=?", new String[] { code }, null, null, null);
            if (codeValid.moveToNext()) {
                message = R.string.codeRepeat;
            }
        } else if (!pwd.equals(confirmPwd)) {
            message = R.string.pwsNotSame;
            pwdETForEdit.setText("");
            pwdConfirmETForEdit.setText("");
        }
        if (message != 0) {
            // 错误提示
            new AlertDialog.Builder(CurrentUserActivity.this)
                    .setMessage(message)
                    .setNeutralButton(R.string.str_ok, null).show();
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

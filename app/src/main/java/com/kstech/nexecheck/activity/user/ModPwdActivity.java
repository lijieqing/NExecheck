package com.kstech.nexecheck.activity.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.utils.Globals;

public class ModPwdActivity extends BaseActivity {

    private EditText oldPwdETForEdit, newPwdETForEdit, pwdConfirmETForEdit;
    private Button saveUserBtn, backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_pwd);
        // 初始化菜单
        updateSubtitle("---   修改密码");

        // 初始化页面组件
        initViewComp();

    }

    @Override
    public Activity getactivity() {
        return this;
    }

    // 初始化页面组件
    private void initViewComp() {
        oldPwdETForEdit = (EditText) findViewById(R.id.oldPwdETForEdit);
        newPwdETForEdit = (EditText) findViewById(R.id.newPwdETForEdit);
        pwdConfirmETForEdit = (EditText) findViewById(R.id.pwdConfirmETForEdit);

        saveUserBtn = (Button) findViewById(R.id.saveUserBtn);
        saveUserBtn.setOnClickListener(saveUserBtnListener);

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnListener);
    }

    View.OnClickListener saveUserBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            String msg = "";
            if ("".equals(oldPwdETForEdit.getText().toString()) || "".equals(newPwdETForEdit.getText().toString())
                    || "".equals(pwdConfirmETForEdit.getText().toString())) {
                msg = "请填写完整信息";
            } else if (!newPwdETForEdit.getText().toString().equals(pwdConfirmETForEdit.getText().toString())) {
                msg = "两次输入的新密码不相同，请重新输入";
                newPwdETForEdit.setText("");
                pwdConfirmETForEdit.setText("");
            } else if (!oldPwdETForEdit.getText().toString().equals(Globals.getCurrentUser().getPwd())){
                // 验证原密码是否正确
                msg = "原密码不正确，请重新输入";
            }
            if (!"".equals(msg)){
                new AlertDialog.Builder(ModPwdActivity.this)
                        .setMessage(msg)
                        .setNeutralButton(R.string.str_ok, null).show();
            } else {
                // 更新用户密码，并更新上下文用户密码信息
                ContentValues cv = new ContentValues();
                cv.put("pwd", newPwdETForEdit.getText().toString());
                DatabaseManager.getInstance(getactivity()).update("user", cv, "code=?", new String[]{Globals.getCurrentUser().getCode()});
                Globals.getCurrentUser().setPwd(newPwdETForEdit.getText().toString());
                Toast.makeText(ModPwdActivity.this, R.string.saveSuccess,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    /*
     * 退出按钮，监听事件
     */
    View.OnClickListener backBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            finish();
        }
    };

}

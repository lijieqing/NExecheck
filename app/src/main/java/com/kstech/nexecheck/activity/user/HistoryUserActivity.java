package com.kstech.nexecheck.activity.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.HistoryUserListAdapter;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.dao.UserDao;
import com.kstech.nexecheck.domain.db.dbenum.UserStatusEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryUserActivity extends BaseActivity {

    private ListView listView = null;

    // 定义页面组件
    private TextView userNameETForRe,userCodeETForRe,delUserETForRe,delTimeETForRe,addUserETForRe,addTimeETForRe;
    private Button recoverUserBtn,backBtn,showCurrentUserBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_user);
        // 初始化菜单
        updateSubtitle("---   历史用户管理");

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
        listView = (ListView)findViewById(R.id.userlist);
        List<Map<String, Object>> list = UserDao.findUserListReturnListMap(UserStatusEnum.DISABLE.getCode(),this);
        listView.setAdapter(new HistoryUserListAdapter(list,getactivity()));
        listView.setOnItemClickListener(listViewOnitemListener);
    }

    // 当前选中的用户
    private LinearLayout selectedLine;
    /*
     * listView点击，监听事件
     */
    OnItemClickListener listViewOnitemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View currentLine, int arg2,long arg3) {
            if (null != selectedLine) {
                ((TextView)selectedLine.getChildAt(2)).setTextColor(getResources().getColor(R.color.userListTextColor));
            }
            selectedLine = (LinearLayout)currentLine;
            ((TextView)selectedLine.getChildAt(2)).setTextColor(getResources().getColor(R.color.userListSelectedColor));

            // arg0:就是你的listview   arg2:点击的item的位置。和你的数组的下标相等。arg3:被电击view的id
            HashMap<String,Object> map=(HashMap<String,Object>)listView.getItemAtPosition(arg2);
            String code = (String)map.get("code");
            Cursor user = DatabaseManager.getInstance(getactivity()).query("user", null, "code=?", new String[]{code}, null, null, null);
            if (user.moveToNext()) {
                userNameETForRe.setText(user.getString(user.getColumnIndex("name")));
                userCodeETForRe.setText(user.getString(user.getColumnIndex("code")));
                delUserETForRe.setText(user.getString(user.getColumnIndex("stop_user_code")));
                delTimeETForRe.setText(user.getString(user.getColumnIndex("stop_time")));
                addUserETForRe.setText(user.getString(user.getColumnIndex("creator_code")));
                addTimeETForRe.setText(user.getString(user.getColumnIndex("create_time")));
            }
        }
    };

    /*
     * 查看当前用户，监听事件
     */
    View.OnClickListener showCurrentUserBtnBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            Intent intent = new Intent(HistoryUserActivity.this,CurrentUserActivity.class);
            startActivity(intent);
            HistoryUserActivity.this.finish();
        }
    };
    /*
     * 恢复用户，监听事件
     */
    View.OnClickListener recoverUserBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if (!userCodeETForRe.getText().toString().equals("")) {
                new AlertDialog.Builder(HistoryUserActivity.this).setTitle(R.string.diaLogWakeup).setMessage(R.string.recoverUserConfirm).setNegativeButton(R.string.str_close,null).setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues cv = new ContentValues();
                        cv.put("status",UserStatusEnum.ENABLE.getCode());
                        DatabaseManager.getInstance(getactivity()).update("user", cv, "code=?", new String[]{userCodeETForRe.getText().toString()});
                        userNameETForRe.setText("");
                        userCodeETForRe.setText("");
                        delUserETForRe.setText("");
                        delTimeETForRe.setText("");
                        addUserETForRe.setText("");
                        addTimeETForRe.setText("");
                        // 刷新列表
                        initUserList();
                        Toast.makeText(HistoryUserActivity.this,R.string.reSuccess,Toast.LENGTH_LONG).show();
                    }
                }).show();
            }
        }
    };
    /*
     * 退出按钮，监听事件
     */
    View.OnClickListener backBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            HistoryUserActivity.this.finish();
        }
    };


    // 初始化页面组件
    private void initViewComp() {
        userNameETForRe = (TextView)findViewById(R.id.userNameETForRe);
        userCodeETForRe = (TextView)findViewById(R.id.userCodeETForRe);
        delUserETForRe = (TextView)findViewById(R.id.delUserETForRe);
        delTimeETForRe = (TextView)findViewById(R.id.delTimeETForRe);
        addUserETForRe = (TextView)findViewById(R.id.addUserETForRe);
        addTimeETForRe = (TextView)findViewById(R.id.addTimeETForRe);

        recoverUserBtn = (Button) findViewById(R.id.recoverUserBtn);
        recoverUserBtn.setOnClickListener(recoverUserBtnListener);

        showCurrentUserBtn = (Button) findViewById(R.id.showCurrentUserBtn);
        showCurrentUserBtn.setOnClickListener(showCurrentUserBtnBtnListener);

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(backBtnListener);
    }

}

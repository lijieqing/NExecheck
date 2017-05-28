package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;

import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.CheckItemListAdapter;
import com.kstech.nexecheck.domain.db.dao.CheckItemDetailDao;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.fragment.CreateCheckRecordFragment;
import com.kstech.nexecheck.view.fragment.HomeCheckEntityFragment;

public class HomeActivity extends BaseActivity implements View.OnClickListener{

    // 主页面组件变量
    private Button btnCreateCheckRecord, btnOpenCheckRecord, liuchengCheckBtn,
            singleCheckBtn, wholePassBtn, wholeNoPassBtn, wholeForcePassBtn;
    private TextView deviceNameTV, subdeviceNameTV, excIdTV,
            wholeCheckStatusTV, wholeCheckerNameTV, wholeFinishTimeTV,
            wholeSumTimesTV, wholeCheckDescTV;
    private TableRow wholeCheckDescTableRow;

    // 当前机型检查项目列表
    private ListView currentMachineCheckItemList;
    // 检查项目的 适配器和适配器数据
    private CheckItemListAdapter checkItemListAdspter;

    // 当前检验记录
    private CheckRecordEntity checkRecordEntity;

    //替代布局
    public LinearLayout llCheck;

    //fragment 相关变量
    public Fragment showFg = null;
    private FragmentManager fragmentManager;
    public HomeCheckEntityFragment homeCheckEntityFragment;
    public CreateCheckRecordFragment createCheckRecordFragment;

    /**
     * 当前选中的检验项
     */
    private CheckItemEntity checkItemEntity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fragmentManager = getFragmentManager();
        homeCheckEntityFragment = new HomeCheckEntityFragment();
        createCheckRecordFragment = new CreateCheckRecordFragment();
        createCheckRecordFragment.setActivity(this);
        initMenu("");
        initViewComp();
        initListener();

        Globals.HomeRealtimeViews.clear();
        Globals.loadDeviceModelFile("0004","00001001",this);
        fragmentManager.beginTransaction().add(R.id.ll_home_show,homeCheckEntityFragment).commit();
    }

    @Override
    public Activity getactivity() {
        return this;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    /**
     * 初始化页面组件
     */
    private void initViewComp() {
        btnCreateCheckRecord = (Button) findViewById(R.id.btnCreateCheckRecord);
        btnOpenCheckRecord = (Button) findViewById(R.id.btnOpenCheckRecord);
        liuchengCheckBtn = (Button) findViewById(R.id.liuchengCheckBtn);
        singleCheckBtn = (Button) findViewById(R.id.singleCheckBtn);

        deviceNameTV = (TextView) findViewById(R.id.deviceNameTV);
        subdeviceNameTV = (TextView) findViewById(R.id.subdeviceNameTV);
        excIdTV = (TextView) findViewById(R.id.excIdTV);

        wholeCheckStatusTV = (TextView) findViewById(R.id.wholeCheckStatusTV);
        wholeCheckerNameTV = (TextView) findViewById(R.id.wholeCheckerNameTV);
        wholeFinishTimeTV = (TextView) findViewById(R.id.wholeFinishTimeTV);
        wholeSumTimesTV = (TextView) findViewById(R.id.wholeSumTimesTV);
        wholeCheckDescTV = (TextView) findViewById(R.id.wholeCheckDescTV);

        wholeCheckDescTableRow = (TableRow) findViewById(R.id.wholeCheckDescTableRow);

        wholePassBtn = (Button) findViewById(R.id.wholePassBtn);
        wholeNoPassBtn = (Button) findViewById(R.id.wholeNoPassBtn);
        wholeForcePassBtn = (Button) findViewById(R.id.wholeForcePassBtn);

        llCheck = (LinearLayout) findViewById(R.id.ll_check);
        // 如果是检测员，则隐藏强制合格按钮
        if (Globals.getCurrentUser().getType().getCode().equals("1")) {
            wholeForcePassBtn.setVisibility(View.GONE);
        }
    }
    /**
     * 初始化整机检验状态
     *
     * @param excId
     * @param cr
     */
    private void initWholeCheckStatus(CheckRecordEntity cr) {
        wholeCheckStatusTV.setText(cr.getCheckStatus());
        wholeCheckerNameTV.setText(cr.getCheckerName());
        wholeFinishTimeTV.setText(cr.getFinishTime() == null ? "无" : cr.getFinishTime());
        String s = CheckItemDetailDao.getAllTimes(this,cr.getExcId()) + "次";
        wholeSumTimesTV.setText(s);
        wholeCheckDescTV.setText(cr.getDesc());
    }

    /**
     * 初始化按钮监听器
     */
    public void initListener() {
        btnCreateCheckRecord.setOnClickListener(this);
        btnOpenCheckRecord.setOnClickListener(this);
        liuchengCheckBtn.setOnClickListener(this);
        singleCheckBtn.setOnClickListener(this);

        wholeCheckDescTableRow.setOnClickListener(this);

        wholePassBtn.setOnClickListener(this);
        wholeNoPassBtn.setOnClickListener(this);
        wholeForcePassBtn.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateCheckRecord:
                //Intent cintent = new Intent(AdminIndexActivity.this, CreateCheckRecordActivity.class);
                //startActivityForResult(cintent, 0);
                llCheck.setVisibility(View.VISIBLE);
                showFragment(createCheckRecordFragment,"CreateFragment",R.id.ll_check);
                break;
            case R.id.btnOpenCheckRecord:
                //Intent ointent = new Intent(AdminIndexActivity.this, OpenCheckRecordActivity.class);
                //startActivityForResult(ointent, 1);
                break;
            case R.id.liuchengCheckBtn:
                //checkButHandle("liucheng");
                break;
            case R.id.singleCheckBtn:
                //checkButHandle("single");
                break;
            case R.id.wholeCheckDescTableRow:
                if (null == checkRecordEntity) {
                    return;
                }
                // 初始化检验说明弹出菜单
                LayoutInflater layoutInflater = (LayoutInflater) this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = layoutInflater.inflate(
                        R.layout.activity_home_whole_check_desc, null);
                final PopupWindow changeStatusPopUp = new PopupWindow(this);
                changeStatusPopUp.setContentView(layout);
                changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                changeStatusPopUp.setFocusable(true);
                // changeStatusPopUp.setBackgroundDrawable(getDrawable(R.drawable.rect_gray));
                changeStatusPopUp.setBackgroundDrawable(null);
                //// TODO: 2017/5/25 弹窗位置需要修改
                changeStatusPopUp.showAtLocation(layout, Gravity.TOP, 0, 100);

                Button saveCheckDescBtn = (Button) layout
                        .findViewById(R.id.saveCheckDescBtn);
                Button closeCheckDescBtn = (Button) layout
                        .findViewById(R.id.closeCheckDescBtn);
                final EditText checkDescET = (EditText) layout
                        .findViewById(R.id.checkDescET);
                checkDescET.setText(CheckRecordDao.findCheckRecordByExcId(getApplicationContext(), excIdTV.getText().toString()).getDesc());
                saveCheckDescBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        CheckRecordDao.updateCheckDesc(getApplicationContext(), excIdTV.getText().toString(), checkDescET.getText().toString());
                        wholeCheckDescTV.setText(checkDescET.getText().toString());
                        changeStatusPopUp.dismiss();
                    }
                });
                closeCheckDescBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        changeStatusPopUp.dismiss();
                    }
                });
                break;
        }
    }

    private void showFragment(Fragment f, String tagPage, int rID) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!f.isAdded() && null == getFragmentManager().findFragmentByTag(tagPage)) {
            if (showFg != null) {
                ft.hide(showFg).add(rID, f, "TAG" + tagPage);
            } else {
                ft.add(rID, f, "TAG" + tagPage);
            }
        } else { //已经加载进容器里去了....
            if (showFg != null) {
                ft.hide(showFg).show(f);
            } else {
                ft.show(f);
            }
        }
        showFg = f;
        ft.commit();
    }
}

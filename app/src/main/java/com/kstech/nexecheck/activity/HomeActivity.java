package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.CheckItemListAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.dao.CheckItemDao;
import com.kstech.nexecheck.domain.db.dao.CheckItemDetailDao;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.fragment.CreateCheckRecordFragment;
import com.kstech.nexecheck.view.fragment.HomeCheckEntityFragment;
import com.kstech.nexecheck.view.fragment.OpenCheckRecordFragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.State.TERMINATED;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

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
    private CheckItemListAdapter checkItemListAdapter;

    // 当前检验记录相关
    private String excID;
    private CheckRecordEntity checkRecordEntity;

    //替代布局
    public LinearLayout llCheck;

    //fragment 相关变量
    public Fragment showFg = null;
    private FragmentManager fragmentManager;
    public HomeCheckEntityFragment homeCheckEntityFragment;
    public CreateCheckRecordFragment createCheckRecordFragment;
    public OpenCheckRecordFragment openCheckRecordFragment;

    /**
     * 当前选中的检验项
     */
    private CheckItemEntity checkItemEntity;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    updateHome(excID);

                    // 初始化整机检验状态
                    initWholeCheckStatus(checkRecordEntity);
                    break;
                case 2:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fragmentManager = getFragmentManager();
        homeCheckEntityFragment = new HomeCheckEntityFragment();
        createCheckRecordFragment = new CreateCheckRecordFragment();
        openCheckRecordFragment = new OpenCheckRecordFragment();
        createCheckRecordFragment.setActivity(this);
        openCheckRecordFragment.setActivity(this);

        initMenu("");
        initViewComp();
        initListener();
        initRecordItem(ConfigFileManager.getInstance(this).getLastExcid());
        fragmentManager.beginTransaction().add(R.id.ll_home_show, homeCheckEntityFragment).commit();
    }

    @Override
    public Activity getactivity() {
        return this;
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
        //检测项目列表相关初始化
        currentMachineCheckItemList = (ListView) findViewById(R.id.currentMachineCheckItemList);
        checkItemListAdapter = new CheckItemListAdapter(this);
        currentMachineCheckItemList.setAdapter(checkItemListAdapter);
        currentMachineCheckItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout ly = (LinearLayout) view;

                String itemId = ((TextView) ly.getChildAt(0)).getText().toString();

                Globals.HomeLastPosition = position;
                checkItemListAdapter.notifyDataSetChanged();
            }
        });
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
        String s = CheckItemDetailDao.getAllTimes(this, cr.getExcId()) + "次";
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
                llCheck.setVisibility(View.VISIBLE);
                showFragment(createCheckRecordFragment, "CreateFragment", R.id.ll_check);
                break;
            case R.id.btnOpenCheckRecord:
                llCheck.setVisibility(View.VISIBLE);
                showFragment(openCheckRecordFragment, "OpenFragment", R.id.ll_check);
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

    //fragment 切换
    private void showFragment(BaseFragment f, String tagPage, int rID) {
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
        if (!baseFragments.contains(f)) baseFragments.add(f);
    }

    public void updateHome(String excID) {
        ConfigFileManager.getInstance(this).saveLastExcid(excID);
        checkRecordEntity = CheckRecordDao.findCheckRecordByExcId(this, excID);
        Globals.loadDeviceModelFile(checkRecordEntity.getDeviceId(), checkRecordEntity.getSubdeviceId(), this);
        deviceNameTV.setText(checkRecordEntity.getDeviceName());
        subdeviceNameTV.setText(checkRecordEntity.getSubdeviceName());
        excIdTV.setText(checkRecordEntity.getExcId());
        Globals.HomeItems = (ArrayList<CheckItemVO>) Globals.getModelFile().getCheckItemList();
        Globals.HomeLastPosition = -1;
        checkItemListAdapter.notifyDataSetChanged();
        if (homeCheckEntityFragment.myAdapter != null)
            homeCheckEntityFragment.myAdapter.notifyDataSetChanged();
    }

    private void initRecordItem(final String excId) {
        // 初始化的时候excId 为 ""
        if (excId == null || excId.equals("")) {
            return;
        }
        checkRecordEntity = CheckRecordDao.findCheckRecordByExcId(this, excId);
        if (checkRecordEntity == null) {
            return;
        }
        LinkedList<String> excIdList = CheckRecordDao.findCheckRecordByUserName(this, Globals.getCurrentUser().getName());

        if (!excIdList.contains(excId)) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    Globals.loadDeviceModelFile(checkRecordEntity.getDeviceId(), checkRecordEntity.getSubdeviceId(), getApplicationContext());
                } catch (ExcException excException) {
                    Toast.makeText(getApplicationContext(), excException.getErrorMsg(), Toast.LENGTH_SHORT).show();
                    Log.e("HomeActivity", excException.getErrorMsg());
                    handler.sendEmptyMessage(0);
                    return;
                }
                excID = excId;
                handler.sendEmptyMessage(1);
            }
        }.start();



    }
}

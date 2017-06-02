package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
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
import com.kstech.nexecheck.base.NetWorkStatusListener;
import com.kstech.nexecheck.domain.communication.CommunicationWorker;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.config.vo.RealTimeParamVO;
import com.kstech.nexecheck.domain.db.dao.CheckItemDao;
import com.kstech.nexecheck.domain.db.dao.CheckItemDetailDao;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;
import com.kstech.nexecheck.domain.db.dbenum.CheckRecordStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.engine.DeviceLoadTask;
import com.kstech.nexecheck.engine.SingleReadyToCheckTask;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.fragment.CreateCheckRecordFragment;
import com.kstech.nexecheck.view.fragment.DoCheckFragment;
import com.kstech.nexecheck.view.fragment.HomeCheckEntityFragment;
import com.kstech.nexecheck.view.fragment.OpenCheckRecordFragment;
import com.kstech.nexecheck.view.widget.CheckItemSummaryView;
import com.kstech.nexecheck.view.widget.RealTimeView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import J1939.J1939_Task;

import static java.lang.Thread.State.TERMINATED;

public class HomeActivity extends BaseActivity implements View.OnClickListener,NetWorkStatusListener {

    // 主页面组件变量
    private Button btnCreateCheckRecord, btnOpenCheckRecord, liuchengCheckBtn,
            singleCheckBtn, wholePassBtn, wholeNoPassBtn, wholeForcePassBtn;
    private TextView deviceNameTV, subdeviceNameTV, excIdTV,
            wholeCheckStatusTV, wholeCheckerNameTV, wholeFinishTimeTV,
            wholeSumTimesTV, wholeCheckDescTV;
    private TableRow wholeCheckDescTableRow;

    private ImageView connStatus;

    private boolean isFirstOncreate = false;

    // 当前机型检查项目列表
    private ListView currentMachineCheckItemList;
    // 检查项目的 适配器和适配器数据
    private CheckItemListAdapter checkItemListAdapter;

    // 当前检验记录相关
    public String excID;
    public CheckRecordEntity checkRecordEntity;

    //替代布局
    public LinearLayout llCheck;

    //fragment 相关变量
    public Fragment showFg = null;
    public Fragment showChFg = null;
    private FragmentManager fragmentManager;
    public HomeCheckEntityFragment homeCheckEntityFragment;
    public CreateCheckRecordFragment createCheckRecordFragment;
    public OpenCheckRecordFragment openCheckRecordFragment;
    public DoCheckFragment doCheckFragment;

    /**
     * 当前选中的检验项
     */
    public CheckItemEntity checkItemEntity;

    /**
     * The constant j1939ProtTask.
     */

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
                    initRecordItem(null,false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        isFirstOncreate = true;

        fragmentManager = getFragmentManager();
        homeCheckEntityFragment = new HomeCheckEntityFragment();
        createCheckRecordFragment = new CreateCheckRecordFragment();
        openCheckRecordFragment = new OpenCheckRecordFragment();
        doCheckFragment = new DoCheckFragment();
        createCheckRecordFragment.setActivity(this);
        openCheckRecordFragment.setActivity(this);
        homeCheckEntityFragment.setActivity(this);
        doCheckFragment.setActivity(this);

        initMenu("");
        initViewComp();
        initListener();
        initRecordItem(ConfigFileManager.getInstance(this).getLastExcid(),true);
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

        connStatus = (ImageView) findViewById(R.id.connStatusId);
        //检测项目列表相关初始化
        currentMachineCheckItemList = (ListView) findViewById(R.id.currentMachineCheckItemList);
        checkItemListAdapter = new CheckItemListAdapter(this);
        currentMachineCheckItemList.setAdapter(checkItemListAdapter);
        currentMachineCheckItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout ly = (LinearLayout) view;

                String itemId = ((TextView) ly.getChildAt(0)).getText().toString();
                checkItemEntity = CheckItemDao.getSingleCheckItemFromDB(excID,itemId,getactivity());
                if(checkItemEntity == null){
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.xmlAlreadyChanged)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }
                if(Globals.getModelFile().getCheckItemVO(checkItemEntity.getItemId()) != null ){
                    // 加载 项目参数列表
                    homeCheckEntityFragment.currentCheckItemView.initCheckItemParamList(checkItemEntity);
                    // 保存最后一次点击的 itemID
                    ConfigFileManager.getInstance(getactivity()).saveLastItemid(itemId);
                }else {
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.xmlAlreadyChanged)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }
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
     * 判断当前是否有检验项目被选中
     *
     * @return boolean
     */
    public boolean isCheckItemSelected() {
        return checkRecordEntity != null && checkItemEntity != null;
    }

    public CheckItemEntity getSelectedCheckItem() {
        return checkItemEntity;
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
                showCheckFragment(createCheckRecordFragment, "CreateFragment", R.id.ll_check);
                break;
            case R.id.btnOpenCheckRecord:
                llCheck.setVisibility(View.VISIBLE);
                showCheckFragment(openCheckRecordFragment, "OpenFragment", R.id.ll_check);
                break;
            case R.id.liuchengCheckBtn:
                if (checkItemEntity != null){
                    //通过 entity id获取到需要初始化的 实施参数
                    List<RealTimeParamVO> reals = Globals.getModelFile().getCheckItemVO(checkItemEntity.getItemId()).getRtParamList();
                    Globals.CheckItemRealtimeViews.clear();
                    for (RealTimeParamVO real : reals) {
                        //// TODO: 2017/6/1 此时将实时显示参数添加到集合 但是未注册监听 在fragment初始化时注册
                        RealTimeView realTimeView = new RealTimeView(getactivity(),real);
                        Globals.CheckItemRealtimeViews.add(realTimeView);
                    }
                }else {
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.please_selectOne_check_item)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }
                llCheck.setVisibility(View.VISIBLE);
                showCheckFragment(doCheckFragment, "DoFragment", R.id.ll_check);
                break;
            case R.id.singleCheckBtn:
                if (checkItemEntity != null){
                    //通过 entity id获取到需要初始化的 实施参数
                    List<RealTimeParamVO> reals = Globals.getModelFile().getCheckItemVO(checkItemEntity.getItemId()).getRtParamList();
                    Globals.CheckItemRealtimeViews.clear();
                    for (RealTimeParamVO real : reals) {
                        //// TODO: 2017/6/1 此时将实时显示参数添加到集合 但是未注册监听 在fragment初始化时注册
                        RealTimeView realTimeView = new RealTimeView(getactivity(),real);
                        Globals.CheckItemRealtimeViews.add(realTimeView);
                    }
                    SingleReadyToCheckTask singleReadyToCheckTask = new SingleReadyToCheckTask(this);
                    singleReadyToCheckTask.execute();
                }else {
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.please_selectOne_check_item)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }

//                llCheck.setVisibility(View.VISIBLE);
//                showCheckFragment(doCheckFragment, "SingleFragment", R.id.ll_check);
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
            case R.id.wholePassBtn:
                if (checkRecordEntity == null){
                    return;
                }
                if("未完成".equals(checkRecordEntity.getCheckStatus())){

                    //未检测 或 未完成 无法合格
                    List<CheckItemEntity> itemList = checkRecordEntity.getCheckItemList();
                    for (CheckItemEntity item : itemList) {
                        String status = item.getCheckStatus();
                        if ((status.equals(CheckItemStatusEnum.UN_CHECK.getCode()) || status.equals(CheckItemStatusEnum.UN_FINISH.getCode())) && !item.getCheckDesc().contains("ignore$")) {
                            new AlertDialog.Builder(getactivity())
                                    .setMessage(R.string.recordNotFinish)
                                    .setNeutralButton(R.string.str_ok, null).show();
                            return;
                        }
                    }
                    new AlertDialog.Builder(getactivity())
                            .setTitle(R.string.diaLogWakeup)
                            .setMessage(R.string.passConfirm)
                            .setNegativeButton(R.string.str_close, null)
                            .setPositiveButton(R.string.str_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            updateWholeArea(CheckRecordStatusEnum.PASS.getCode(),CheckRecordStatusEnum.PASS.getName());
                                            checkRecordEntity.setCheckStatus(CheckRecordStatusEnum.PASS.getName());
                                        }
                                    }).show();

                    return;
                }else {
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.recordAlreadyFinish)
                            .setNeutralButton(R.string.str_ok, null).show();
                }
                break;
            case R.id.wholeNoPassBtn:
                if (checkRecordEntity == null){
                    return;
                }
                if("未完成".equals(checkRecordEntity.getCheckStatus())){

                    //未检测 或 未完成 无法合格
                    List<CheckItemEntity> itemList = checkRecordEntity.getCheckItemList();
                    Log.e("CBHomeActivity",itemList.size()+"-------------");
                    for (CheckItemEntity item : itemList) {
                        String status = item.getCheckStatus();
                        if ((status.equals(CheckItemStatusEnum.UN_CHECK.getCode()) || status.equals(CheckItemStatusEnum.UN_FINISH.getCode())) && !item.getCheckDesc().contains("ignore$")) {
                            new AlertDialog.Builder(getactivity())
                                    .setMessage(R.string.recordNotFinish)
                                    .setNeutralButton(R.string.str_ok, null).show();
                            return;
                        }
                    }
                    new AlertDialog.Builder(getactivity())
                            .setTitle(R.string.diaLogWakeup)
                            .setMessage(R.string.unpassConfirm)
                            .setNegativeButton(R.string.str_close, null)
                            .setPositiveButton(R.string.str_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            updateWholeArea(CheckRecordStatusEnum.UN_PASS.getCode(), CheckRecordStatusEnum.UN_PASS.getName());
                                            checkRecordEntity.setCheckStatus(CheckRecordStatusEnum.UN_PASS.getName());
                                        }
                                    }).show();

                    return;
                }else {
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.recordAlreadyFinish)
                            .setNeutralButton(R.string.str_ok, null).show();
                }
                break;
            case R.id.wholeForcePassBtn:
                if (checkRecordEntity == null){
                    return;
                }
                if("未完成".equals(checkRecordEntity.getCheckStatus())){
                    //未检测 或 未完成 无法合格
                    List<CheckItemEntity> itemList = checkRecordEntity.getCheckItemList();
                    for (CheckItemEntity item : itemList) {
                        String status = item.getCheckStatus();
                        if ((status.equals(CheckItemStatusEnum.UN_CHECK.getCode()) || status.equals(CheckItemStatusEnum.UN_FINISH.getCode())) && !item.getCheckDesc().contains("ignore$")) {
                            new AlertDialog.Builder(getactivity())
                                    .setMessage(R.string.recordNotFinish)
                                    .setNeutralButton(R.string.str_ok, null).show();
                            return;
                        }
                    }
                    new AlertDialog.Builder(getactivity())
                            .setTitle(R.string.diaLogWakeup)
                            .setMessage(R.string.forcepassConfirm)
                            .setNegativeButton(R.string.str_close, null)
                            .setPositiveButton(R.string.str_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            updateWholeArea(CheckRecordStatusEnum.FORCE_PASS.getCode(), CheckRecordStatusEnum.FORCE_PASS.getName());
                                            checkRecordEntity.setCheckStatus(CheckRecordStatusEnum.FORCE_PASS.getName());
                                        }
                                    }).show();

                    return;
                }else if("合格".equals(checkRecordEntity.getCheckStatus()) || "强制合格".equals(checkRecordEntity.getCheckStatus())){
                    Log.e("CB","else"+checkRecordEntity.getCheckStatus());
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.recordAlreadyhegeFinish)
                            .setNeutralButton(R.string.str_ok, null).show();
                } else if("未合格".equals(checkRecordEntity.getCheckStatus())){
                    new AlertDialog.Builder(getactivity())
                            .setTitle(R.string.diaLogWakeup)
                            .setMessage(R.string.forcepassConfirm)
                            .setNegativeButton(R.string.str_close, null)
                            .setPositiveButton(R.string.str_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            updateWholeArea(CheckRecordStatusEnum.FORCE_PASS.getCode(), CheckRecordStatusEnum.FORCE_PASS.getName());
                                            checkRecordEntity.setCheckStatus(CheckRecordStatusEnum.FORCE_PASS.getName());
                                        }
                                    }).show();
                }


                break;
            default:
                break;
        }
    }

    //和检测相关的 fragment 切换
    public void showCheckFragment(BaseFragment f, String tagPage, int rID) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!f.isAdded() && null == getFragmentManager().findFragmentByTag(tagPage)) {
            if (showChFg != null) {
                ft.hide(showChFg).add(rID, f, tagPage);
            } else {
                ft.add(rID, f, tagPage);
            }
        } else { //已经加载进容器里去了....
            if (showChFg != null) {
                ft.hide(showChFg).show(f);
            } else {
                ft.show(f);
            }
        }
        showChFg = f;
        ft.commit();
        if (!baseFragments.contains(f)) baseFragments.add(f);
    }
    //和home activity 局部相关的fragment 切换
    public void showFragment(BaseFragment f, String tagPage, int rID) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!f.isAdded() && null == getFragmentManager().findFragmentByTag(tagPage)) {
            if (showFg != null) {
                ft.hide(showFg).add(rID, f, tagPage);
            } else {
                ft.add(rID, f, tagPage);
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

        Globals.loadDeviceModelFile(checkRecordEntity.getDeviceId(), checkRecordEntity.getSubdeviceId(), this);

        deviceNameTV.setText(checkRecordEntity.getDeviceName());
        subdeviceNameTV.setText(checkRecordEntity.getSubdeviceName());
        excIdTV.setText(checkRecordEntity.getExcId());

        Globals.HomeItems = (ArrayList<CheckItemVO>) Globals.getModelFile().getCheckItemList();
        Globals.HomeLastPosition = -1;
        checkItemListAdapter.notifyDataSetChanged();

        showFragment(homeCheckEntityFragment,"HomeCheckEntity",R.id.ll_home_show);

    }

    public void initRecordItem(final String excId,boolean restart) {
        // 初始化的时候excId 为 ""
        if (excId == null || excId.equals("")) {
            clear();
            return;
        }
        checkRecordEntity = CheckRecordDao.findCheckRecordByExcId(this, excId);
        if (checkRecordEntity == null) {
            clear();
            return;
        }
        checkRecordEntity.setCheckItemList(CheckItemDao.getCheckItemListFromDB(excId,getactivity()));
        LinkedList<String> excIdList = CheckRecordDao.findCheckRecordByUserName(this, Globals.getCurrentUser().getName());

        if (!excIdList.contains(excId)) {
            //如果权限不够就清空之前加载的
            clear();
            return;
        }
        if (restart){
            new DeviceLoadTask(excId,checkRecordEntity,handler,this).execute();
        }
    }

    @Override
    public void onStatusChanged(final boolean off) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (off){
                    connStatus.setBackgroundResource(R.drawable.link_no);
                }else {
                    connStatus.setBackgroundResource(R.drawable.link);
                }
            }
        });
        if (off){
            for (RealTimeView homeRealtimeView : Globals.HomeRealtimeViews) {
                homeRealtimeView.reset();
            }
            for (RealTimeView checkItemRealtimeView : Globals.CheckItemRealtimeViews) {
                checkItemRealtimeView.reset();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOncreate){
            isFirstOncreate = false;
        }else if (checkRecordEntity != null ) {
            initRecordItem(checkRecordEntity.getExcId(),false);
        } else {
            initRecordItem(null,false);
        }
    }

    private void clear() {
        deviceNameTV.setText("");
        subdeviceNameTV.setText("");
        excIdTV.setText("");
        //检测项目列表清空
        Globals.HomeItems.clear();
        checkItemListAdapter.notifyDataSetChanged();
        //当前检测项详情清空
        if (homeCheckEntityFragment.currentCheckItemView!=null) homeCheckEntityFragment.currentCheckItemView.clear();
        //实时显示区域清空
        Globals.HomeRealtimeViews.clear();
        if (homeCheckEntityFragment.myAdapter!=null)homeCheckEntityFragment.myAdapter.notifyDataSetChanged();
        fragmentManager.beginTransaction().remove(homeCheckEntityFragment).commit();
        wholeCheckStatusTV.setText("");
        wholeCheckerNameTV.setText("");
        wholeFinishTimeTV.setText("");
        wholeSumTimesTV.setText("");
        wholeCheckDescTV.setText("");
    }

    /**
     * 更新整机显示区域内容，更新数据库
     *
     * @param stateCode the state code
     * @param stateName the state name
     */
    public void updateWholeArea(String stateCode,String stateName){
        if (checkRecordEntity!=null) {
            String finishTime = DateUtil.getCurrentEndMin();
            CheckRecordDao.updateCheckStatus(getactivity(),checkRecordEntity.getExcId(), stateCode,finishTime);
            wholeCheckStatusTV.setText(stateName);
            wholeCheckerNameTV.setText(Globals.getCurrentUser().getName());
            wholeFinishTimeTV.setText(finishTime);
        }
    }
}

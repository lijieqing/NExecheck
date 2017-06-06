package com.kstech.nexecheck.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.adapter.MyCheckAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.engine.ItemCheckTask;
import com.kstech.nexecheck.engine.ReadyToCheckInCheckTask;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.widget.CheckItemSingleView;
import com.kstech.nexecheck.view.widget.DividerItemDecoration;

import java.util.List;

/**
 * Created by lijie on 2017/6/1.
 */

public class DoCheckFragment extends BaseFragment implements View.OnClickListener{

    protected TextView deviceNameTV, subdeviceNameTV, excIdTV;
    public Button singleCheckBeginCheckLeftBtn, singleCheckExitCheckBtn, singleCheckBeginCheckRightBtn;

    public Chronometer chronometer;
    // 下一项目按钮，或，退出测量按钮，两者可见其一
    protected Button singleCheckNextItemBtn;

    /**
     * 主动指令信息提示区
     */
    public TextView msgTv;
    /**
     * 被动接收信息提示区
     */
    public ListView msgListView;


    public CheckItemSingleView checkItemSingleView;

    // 实时参数表体
    protected RecyclerView recyclerView;
    public MyCheckAdapter myAdapter;

    // 检查项列表，用于，下一项目，使用
    protected List<CheckItemEntity> checkItemList;

    protected ItemCheckTask checkTask;

    public MsgAdapter msgAdapter;

    protected boolean isSingle;

    public CheckItemEntity currentCheckItemEntity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSingle = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(activity, R.layout.fragment_do_check,null);
        initViewComp(view);
        initListener();
        //设备信息初始化
        String deviceName = ((HomeActivity)activity).checkRecordEntity.getDeviceName();
        String subDeviceName = ((HomeActivity)activity).checkRecordEntity.getSubdeviceName();
        String excId = ((HomeActivity)activity).excID;
        deviceNameTV.setText(deviceName);
        subdeviceNameTV.setText(subDeviceName);
        excIdTV.setText(excId);

        //实时显示参数初始化
        registRealTimeListener();
        msgAdapter.notifyDataSetChanged();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        myAdapter = new MyCheckAdapter ();
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL_LIST));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(myAdapter);

        //检测记录详情初始化
        checkItemSingleView = new CheckItemSingleView(activity,view);
        checkItemSingleView.initView();
        checkItemSingleView.initCheckItemParamList(((HomeActivity)activity).checkItemEntity);

        //检测线程初始化
        checkTask = new ItemCheckTask((HomeActivity)activity,chronometer,msgAdapter,msgTv,isSingle);
        return view;
    }

    /**
     * Init view.
     */
    public void initViewComp(View view) {
        deviceNameTV = (TextView) view.findViewById(R.id.deviceNameTV);
        subdeviceNameTV = (TextView) view.findViewById(R.id.subdeviceNameTV);
        excIdTV = (TextView) view.findViewById(R.id.excIdTV);

        singleCheckBeginCheckLeftBtn = (Button) view.findViewById(R.id.singleCheckBeginCheckLeftBtn);
        singleCheckNextItemBtn = (Button) view.findViewById(R.id.singleCheckNextItemBtn);
        chronometer = (Chronometer) view.findViewById(R.id.singleCheckChronometer);

        singleCheckExitCheckBtn = (Button) view.findViewById(R.id.singleCheckExitCheckBtn);
        singleCheckBeginCheckRightBtn = (Button) view.findViewById(R.id.singleCheckBeginCheckRightBtn);

        msgTv = (TextView) view.findViewById(R.id.msgTv);
        msgListView = (ListView) view.findViewById(R.id.lv_msg_check);
        msgAdapter = new MsgAdapter();
        msgListView.setAdapter(msgAdapter);

        recyclerView = (RecyclerView) view.findViewById(R.id.singleRealTimeParamBody);
    }

    /**
     * 初始化按钮监听器
     */
    public void initListener() {
        singleCheckBeginCheckLeftBtn.setOnClickListener(this);
        singleCheckNextItemBtn.setOnClickListener(this);
        singleCheckExitCheckBtn.setOnClickListener(this);
        singleCheckBeginCheckRightBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.singleCheckBeginCheckLeftBtn:
                btnCheck();
                break;
            case R.id.singleCheckNextItemBtn:
                // 如果计时器是停止状态，则可以点击下一项目按钮，否则不可点击
                if (!checkTask.isRunning) {
                    checkItemList = ((HomeActivity)activity).checkRecordEntity.getCheckItemList();
                    // 求出下一项
                    for (int i=0;i<checkItemList.size();i++) {
                        if(checkItemList.get(i).getItemId().equals(((HomeActivity)activity).checkItemEntity.getItemId())) {
                            // 如果当前项不是最后一项
                            if (i+1 < checkItemList.size()){
                                currentCheckItemEntity = ((HomeActivity)activity).checkItemEntity;
                                ((HomeActivity)activity).checkItemEntity = checkItemList.get(i+1);
                                Globals.HomeLastPosition = i+1;
                                // 初始化 项目参数列表
                                // 点击下一项后，重新初始化实时参数配置
                                unRegistRealTimeListener();

                                CheckItemVO itemVO = Globals.getModelFile().getCheckItemVO(((HomeActivity) activity).checkItemEntity.getItemId());

                                // 自动发送准备检测命令
                                ReadyToCheckInCheckTask readyToCheckTask = new ReadyToCheckInCheckTask((HomeActivity) activity,itemVO);
                                readyToCheckTask.execute();

                                // 保存最后一次点击的 itemID
                                //ConfigFileManager.getInstance(activity).saveLastItemid(((HomeActivity)activity).checkItemEntity.getItemId());
                                break;
                            } else {
                                new AlertDialog.Builder(activity).setMessage(R.string.current_item_is_last_item).setNeutralButton(R.string.str_ok, null).show();
                            }
                        }
                    }
                } else {
                    new AlertDialog.Builder(activity).setMessage(R.string.please_wait_currentTask_over).setNeutralButton(R.string.str_ok, null).show();
                }
                break;
            case R.id.singleCheckBeginCheckRightBtn:
                btnCheck();
                break;
            case R.id.singleCheckExitCheckBtn:
                if (!checkTask.isRunning) {
                    exitFragment();
                }else {
                    new AlertDialog.Builder(activity).setMessage(R.string.please_wait_currentTask_over).setNeutralButton(R.string.str_ok, null).show();
                }
                break;
        }
    }

    protected void btnCheck(){
        if (checkTask.isRunning){
            stopConfirm();
        }else {
            checkTask = new ItemCheckTask((HomeActivity)activity,chronometer,msgAdapter,msgTv, isSingle);
            checkTask.execute();
            singleCheckBeginCheckLeftBtn.setText("停止测量");
            singleCheckBeginCheckRightBtn.setText("停止测量");
        }
    }

    //退出fragment
    protected void exitFragment(){
        // 提示，确认退出测量吗？
        new AlertDialog.Builder(activity)
                .setTitle(R.string.diaLogWakeup)
                .setMessage(R.string.stopCheckConfirm)
                .setNegativeButton(R.string.str_close, null)
                .setPositiveButton(R.string.str_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                checkTask.isRunning = false;
                                checkTask.cancel(true);

                                unRegistRealTimeListener();
                                Globals.CheckItemRealtimeViews.clear();
                                ((HomeActivity)activity).showChFg = null;
                                ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                                ((HomeActivity)activity).checkItemListAdapter.notifyDataSetChanged();
                                getFragmentManager().beginTransaction().remove(getFragment()).commit();
                                // 回传响应码
                                //CommandSender.sendStopCheckCommand(checkItemEntity.getItemId(),checkItemEntity.getSumTimes());
                            }
                        }).show();
    }

    //退出fragment
    protected void stopConfirm(){
        // 提示，确认退出测量吗？
        new AlertDialog.Builder(activity)
                .setTitle(R.string.diaLogWakeup)
                .setMessage(R.string.stopCheckingConfirm)
                .setNegativeButton(R.string.str_close, null)
                .setPositiveButton(R.string.str_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                singleCheckBeginCheckLeftBtn.setText("开始测量");
                                singleCheckBeginCheckRightBtn.setText("开始测量");
                                msgTv.setText("人工终止");
                                chronometer.stop();
                                checkTask.isRunning = false;
                                checkTask.cancel(true);
                                //检测线程初始化
                                checkTask = new ItemCheckTask((HomeActivity)activity,chronometer,msgAdapter,msgTv, isSingle);
                            }
                        }).show();
    }

    @Override
    protected BaseFragment getFragment() {
        return this;
    }

    public class MsgAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return Globals.CheckMsgTextView.size();
        }

        @Override
        public Object getItem(int position) {
            return Globals.CheckMsgTextView.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return Globals.CheckMsgTextView.get(position);
        }
    }
}

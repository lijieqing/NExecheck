package com.kstech.nexecheck.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.adapter.MyCheckAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.communication.CommandSender;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.widget.CheckItemSingleView;
import com.kstech.nexecheck.view.widget.DividerItemDecoration;

import java.util.List;

/**
 * Created by lijie on 2017/6/1.
 */

public class DoCheckFragment extends BaseFragment implements View.OnClickListener{

    private TextView deviceNameTV, subdeviceNameTV, excIdTV;
    public Button singleCheckBeginCheckLeftBtn, singleCheckTimeBtn,
            singleCheckExitCheckBtn, singleCheckBeginCheckRightBtn;

    // 下一项目按钮，或，退出测量按钮，两者可见其一
    private Button singleCheckNextItemBtn;

    /**
     * 主动指令信息提示区
     */
    private TextView msgTv;
    /**
     * 被动接收信息提示区
     */
    private LinearLayout msgLayoutView;


    private CheckItemSingleView checkItemSingleView;

    // 实时参数表体
    private RecyclerView recyclerView;
    public MyCheckAdapter myAdapter;

    // 检查项列表，用于，下一项目，使用
    private List<CheckItemEntity> checkItemList;

    // 主页面的按钮，单项检测或流程检测
    private String checkBtnName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        singleCheckTimeBtn = (Button) view.findViewById(R.id.singleCheckTimeBtn);
        singleCheckExitCheckBtn = (Button) view.findViewById(R.id.singleCheckExitCheckBtn);
        singleCheckBeginCheckRightBtn = (Button) view.findViewById(R.id.singleCheckBeginCheckRightBtn);

        msgTv = (TextView) view.findViewById(R.id.msgTv);
        msgLayoutView = (LinearLayout) view.findViewById(R.id.msgLayoutView);

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
            case R.id.singleCheckExitCheckBtn:
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
                                        unRegistRealTimeListener();
                                        ((HomeActivity)activity).showChFg = null;
                                        ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                                        getFragmentManager().beginTransaction().remove(DoCheckFragment.this).commit();
                                        // 回传响应码
                                        //CommandSender.sendStopCheckCommand(checkItemEntity.getItemId(),checkItemEntity.getSumTimes());
                                    }
                                }).show();

                break;
        }
    }
}

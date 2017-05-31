package com.kstech.nexecheck.view.fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.adapter.CheckRecordListAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.utils.Globals;

import java.util.List;

/**
 * Created by lijie on 2017/5/28.
 */

public class OpenCheckRecordFragment extends BaseFragment {
    // 列表组件
    private ListView listView = null;

    // 被选中的机型与子机型单元格
    private LinearLayout selectedColumlayout,selectedSubColumlayout;

    // 页面组件
    private LinearLayout checkFinishTimeLayout,checkStatusLayout,excIdLayout,deviceIdLayout;
    private TextView checkFinishTimeTV,checkStatusTV,excIdTV,deviceIdTV,subdeviceIdTV;
    private Button deviceDialogBtnSure,deviceDialogBtnClose;

    // 弹出窗口
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    private EditText excIdSearchET;
    private ImageView excIdSearchBtn;

    // 按钮
    private Button openCheckRecordClearBtn,openCheckRecordSearchBtn;
    private Button openCheckRecordOpenBtn,openCheckRecordExitBtn;

    // 当前被选中的检测记录
    private LinearLayout currentSelectedRecord;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_open_check_record,null);
        initViewComp(view);
        refreshCheckRecordList(checkFinishTimeTV.getText().toString(),checkStatusTV.getText().toString(), excIdTV.getText().toString(),deviceIdTV.getText().toString(),subdeviceIdTV.getText().toString());
        return view;
    }

    private void initViewComp(View view){
        checkFinishTimeLayout = (LinearLayout)view.findViewById(R.id.checkFinishTimeLayout);
        checkFinishTimeTV = (TextView)view.findViewById(R.id.checkFinishTimeTV);
        checkStatusLayout = (LinearLayout)view.findViewById(R.id.checkStatusLayout);
        checkStatusTV = (TextView)view.findViewById(R.id.checkStatusTV);
        excIdLayout = (LinearLayout)view.findViewById(R.id.excIdLayout);
        excIdTV = (TextView)view.findViewById(R.id.excIdTV);
        deviceIdLayout = (LinearLayout)view.findViewById(R.id.deviceIdLayout);
        deviceIdTV = (TextView)view.findViewById(R.id.deviceIdTV);
        subdeviceIdTV = (TextView)view.findViewById(R.id.subdeviceIdTV);
        openCheckRecordClearBtn = (Button) view.findViewById(R.id.openCheckRecordClearBtn);
        openCheckRecordSearchBtn = (Button) view.findViewById(R.id.openCheckRecordSearchBtn);
        openCheckRecordOpenBtn = (Button) view.findViewById(R.id.openCheckRecordOpenBtn);
        openCheckRecordExitBtn = (Button) view.findViewById(R.id.openCheckRecordExitBtn);
        listView = (ListView) view.findViewById(R.id.checkRecordList);

        listView.setOnItemClickListener(listViewOnitemListener);
        openCheckRecordOpenBtn.setOnClickListener(openCheckRecordOpenBtnListener);
        openCheckRecordExitBtn.setOnClickListener(openCheckRecordExitBtnListener);
    }

    /**
     * 刷新检查记录列表
     */
    private void refreshCheckRecordList(String finishTime,String checkStatus,
                                        String excId,String deviceId,String subdeviceId) {

        List<CheckRecordEntity> list = CheckRecordDao.findCheckRecordByCondition(getActivity(),finishTime,checkStatus,excId,deviceId,subdeviceId);
        listView.setAdapter(new CheckRecordListAdapter(getActivity(), list));

    }

    /*
     * listView，检查记录列表点击，监听事件
     */
    AdapterView.OnItemClickListener listViewOnitemListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            currentSelectedRecord = (LinearLayout) arg1;
        }
    };

    /*
     * 检测记录，打开按钮，监听事件
     */
    View.OnClickListener openCheckRecordOpenBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if (null == currentSelectedRecord) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.please_selectOne_check_record)
                        .setNeutralButton(R.string.str_ok, null).show();
            } else {
                String excId = ((TextView)currentSelectedRecord.getChildAt(4)).getText().toString();
                ((HomeActivity)activity).initRecordItem(excId);
                ((HomeActivity)activity).showFg = null;
                ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                getFragmentManager().beginTransaction().remove(OpenCheckRecordFragment.this).commit();
            }
        }
    };
    /*
     * 检测记录，退出按钮，监听事件
     */
    View.OnClickListener openCheckRecordExitBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            ((HomeActivity)activity).showFg = null;
            ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
            getFragmentManager().beginTransaction().remove(OpenCheckRecordFragment.this).commit();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentSelectedRecord = null;
    }
}

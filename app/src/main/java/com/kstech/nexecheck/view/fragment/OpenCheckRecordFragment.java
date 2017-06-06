package com.kstech.nexecheck.view.fragment;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.activity.upload.DataUploadActivity;
import com.kstech.nexecheck.adapter.CheckRecordListAdapter;
import com.kstech.nexecheck.adapter.CheckStatusListAdspter;
import com.kstech.nexecheck.adapter.GVUploadAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.domain.config.vo.SubDeviceVO;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckRecordStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.ViewTemplateUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
        checkFinishTimeLayout.setOnClickListener(checkFinishTimeLayoutListener);
        checkFinishTimeTV = (TextView)view.findViewById(R.id.checkFinishTimeTV);

        checkStatusLayout = (LinearLayout)view.findViewById(R.id.checkStatusLayout);
        checkStatusLayout.setOnClickListener(checkStatusLayoutListener);
        checkStatusTV = (TextView)view.findViewById(R.id.checkStatusTV);

        excIdLayout = (LinearLayout)view.findViewById(R.id.excIdLayout);
        excIdLayout.setOnClickListener(excIdLayoutListener);
        excIdTV = (TextView)view.findViewById(R.id.excIdTV);

        deviceIdLayout = (LinearLayout)view.findViewById(R.id.deviceIdLayout);
        deviceIdLayout.setOnClickListener(deviceIdLayoutListener);
        deviceIdTV = (TextView)view.findViewById(R.id.deviceIdTV);

        subdeviceIdTV = (TextView)view.findViewById(R.id.subdeviceIdTV);

        openCheckRecordClearBtn = (Button) view.findViewById(R.id.openCheckRecordClearBtn);
        openCheckRecordClearBtn.setOnClickListener(openCheckRecordClearBtnListener);
        openCheckRecordSearchBtn = (Button) view.findViewById(R.id.openCheckRecordSearchBtn);
        openCheckRecordSearchBtn.setOnClickListener(openCheckRecordSearchBtnListener);

        openCheckRecordOpenBtn = (Button) view.findViewById(R.id.openCheckRecordOpenBtn);

        openCheckRecordExitBtn = (Button) view.findViewById(R.id.openCheckRecordExitBtn);
        openCheckRecordExitBtn.setOnClickListener(openCheckRecordExitBtnListener);

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
                ((HomeActivity)activity).initRecordItem(excId,true);
                ((HomeActivity)activity).showChFg = null;
                ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                ((HomeActivity)activity).checkItemEntity = null;
                getFragmentManager().beginTransaction().remove(OpenCheckRecordFragment.this).commit();
            }
        }
    };
    /*
     * 检测记录，退出按钮，监听事件
     */
    View.OnClickListener openCheckRecordExitBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            ((HomeActivity)activity).showChFg = null;
            ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
            getFragmentManager().beginTransaction().remove(OpenCheckRecordFragment.this).commit();
        }
    };

    // 被选中的机型与子机型单元格
    private View selectedDevice;
    private LinearLayout selectedSubDevice;

    /**
     * 清除按钮
     */
    View.OnClickListener openCheckRecordClearBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            checkFinishTimeTV.setText("");
            checkStatusTV.setText("");
            excIdTV.setText("");
            deviceIdTV.setText("");
            subdeviceIdTV.setText("");
        }
    };
    /**
     * 查询按钮
     */
    View.OnClickListener openCheckRecordSearchBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            // 重新加载列表
            refreshCheckRecordList(checkFinishTimeTV.getText().toString(), CheckRecordStatusEnum.getCode(checkStatusTV.getText().toString()),
                    excIdTV.getText().toString(),deviceIdTV.getText().toString(),subdeviceIdTV.getText().toString());
        }
    };

    View.OnClickListener checkFinishTimeLayoutListener = new View.OnClickListener() {
        Calendar cal;
        @Override
        public void onClick(View arg0) {
//			DateTimePickDialogUtil dp = new DateTimePickDialogUtil(OpenCheckRecordActivity.this,checkFinishTimeTV.getText().toString());
//			dp.dateTimePicKDialog(checkFinishTimeTV);
            cal = Calendar.getInstance();
            final DatePickerDialog mDialog;
            if ("".equals(checkFinishTimeTV.getText().toString())) {
                mDialog = new DatePickerDialog(activity, null,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            } else {
                int[] yearMonthDay = DateUtil.getYearMonthDay(checkFinishTimeTV.getText().toString());
                mDialog = new DatePickerDialog(activity, null,
                        yearMonthDay[0], yearMonthDay[1], yearMonthDay[2]);
            }
            mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "完成", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatePicker datePicker = mDialog.getDatePicker();
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth()+1;
                    int day = datePicker.getDayOfMonth();
                    checkFinishTimeTV.setText(year+"-"+(month<10?("0"+month):month)+"-"+(day<10?("0"+day):day));
                }
            });
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            mDialog.show();
        }
    };

    View.OnClickListener checkStatusLayoutListener = new View.OnClickListener() {
        LayoutInflater inflater;
        @Override
        public void onClick(View arg0) {
            // 展示状态选择dialog
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.check_status_view, null);
            ListView listView = (ListView) view
                    .findViewById(R.id.checkStatusListView);
            final List<Map<String, Object>> result = CheckRecordDao.findAllCheckStatus();
            CheckStatusListAdspter adapter = new CheckStatusListAdspter(activity,result);
            listView.setAdapter(adapter);
            // 此处的上下文必须是承载该dialog的activity。否则dialog无法显示，也不抛出异常，纠结了我1个多小时
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            // 此处需要注意，setview的视图需要时父视图，而不能使父下的子视图
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            dialog.show();
            // 宽度设置不合理，则点击屏幕任意位置收键盘事件就不好使了
            dialog.getWindow().setLayout(600, 400);
            // end

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    // 显示选择的状态
                    checkStatusTV.setText(result.get(arg2).get("name").toString());
                    dialog.cancel();
                }
            });
        }
    };
    /**
     * 出厂编号Dialog监听事件
     */
    View.OnClickListener excIdLayoutListener = new View.OnClickListener() {

        TableLayout exiIdDialogTableLayout;
        @Override
        public void onClick(View arg0) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.excid_dialog, null);
            exiIdDialogTableLayout = (TableLayout)view.findViewById(R.id.exiIdDialogTableLayout);
            // 展示机型选择dialog
            excIdTable(exiIdDialogTableLayout,null);
            builder = new AlertDialog.Builder(activity);
            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            // end
            excIdSearchET = (EditText)view.findViewById(R.id.excIdSearchET);
            excIdSearchBtn = (ImageView)view.findViewById(R.id.excIdSearchBtn);
            excIdSearchBtn.setOnClickListener(excIdSearchBtnListener);
        }
        /*
         * 按照出厂编号，进行条件查询
         */
        View.OnClickListener excIdSearchBtnListener = new View.OnClickListener() {
            public void onClick(View arg0) {
                String excIdSearch = excIdSearchET.getText().toString();
                excIdTable(exiIdDialogTableLayout,excIdSearch);
            }
        };
        private void excIdTable(TableLayout exiIdDialogTableLayout,String excIdSearch) {
            exiIdDialogTableLayout.removeAllViews();
            final List<CheckRecordEntity> list = CheckRecordDao.findCheckRecordByCondition(activity,null,null,excIdSearch,null,null);
            // 整行数
            int whole = list.size() / 3;
            // 余数
            int remain = list.size() % 3;
            // 总行数
            int trTotal = remain == 0 ? whole : whole + 1;

            for (int i = 0; i < trTotal; i++) {
                ViewTemplateUtil tru = new ViewTemplateUtil(activity);
                TableLayout tb = tru.getDeviceTableLayout();
                TableRow tr = (TableRow)tb.getChildAt(0);

                for (int j = 0; i == trTotal - 1 && remain != 0 ? j < remain
                        : j < 3; j++) {
                    // 赋值列列单元格
                    LinearLayout ly = (LinearLayout)tr.getChildAt(j);
                    final TextView excId = (TextView) ly.getChildAt(0);
                    excId.setText(list.get(3 * i + j).getExcId());
                    // 监听机型列单元格点击事件
                    ly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            excIdTV.setText(excId.getText());
                            dialog.cancel();
                        }
                    });
                }
                tb.removeView(tr);
                exiIdDialogTableLayout.addView(tr);
            }
        }
    };

    View.OnClickListener deviceIdLayoutListener = new View.OnClickListener() {
        LayoutInflater inflater;
        @Override
        public void onClick(View arg0) {
            // 展示机型选择dialog
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.device_dialog, null);
            GridView gvDevice = (GridView)view.findViewById(R.id.gv_device_update_activity);
            final TableLayout deviceDialogSubDeviceTableLayout = (TableLayout)view.findViewById(R.id.deviceDialogSubDeviceTableLayout);

            deviceDialogBtnSure = (Button)view.findViewById(R.id.deviceDialogBtnSure);
            deviceDialogBtnSure.setOnClickListener(deviceDialogBtnSureListener);

            deviceDialogBtnClose = (Button)view.findViewById(R.id.deviceDialogBtnClose);
            deviceDialogBtnClose.setOnClickListener(deviceDialogBtnCloseListener);

            final List<DeviceVO> deviceList = Globals.getResConfig().getDeviceList();
            final GVUploadAdapter gvUploadAdapter = new GVUploadAdapter(deviceList,activity);
            gvDevice.setAdapter(gvUploadAdapter);
            gvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Globals.UploatLastPosition = position;
                    selectedDevice = view;
                    // 单元格机型下的子机型数据
                    List<SubDeviceVO> subList = deviceList.get(position).getSubDeviceList();
                    // 监听机型列单元格点击事件
                    setSubDeviceLayout(subList, deviceDialogSubDeviceTableLayout);
                    gvUploadAdapter.notifyDataSetChanged();
                }
            });

            builder = new AlertDialog.Builder(activity);
            builder.setView(view);
            dialog = builder.create();
            dialog.show();
            // end
        }
    };

    private void setSubDeviceLayout(List<SubDeviceVO> subList,TableLayout deviceDialogSubDeviceTableLayout) {
        // 移除所有子机型区域的视图
        deviceDialogSubDeviceTableLayout.removeAllViews();

        // 重新加载子机型区域视图
        for (int k = 0; k < subList.size(); k++) {
            ViewTemplateUtil tru = new ViewTemplateUtil(activity);
            TableLayout tb = tru.getSubDeviceTableLayout();
            TableRow tr = (TableRow)tb.getChildAt(0);

            // 赋值列列单元格
            final LinearLayout subColumlayout = (LinearLayout)tr.getChildAt(0);
            TextView deviceName = (TextView) subColumlayout.getChildAt(0);
            TextView deviceId = (TextView) subColumlayout.getChildAt(2);
            deviceName.setText(subList.get(k).getSubDevName());
            deviceId.setText(subList.get(k).getSubDevId());

            // 监听子机型点击事件
            subColumlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (selectedSubDevice != null) {
                        // 重置上一个机型，取消选中状态
                        ((TextView) selectedSubDevice
                                .getChildAt(0))
                                .setTextColor(getResources().getColor(
                                        R.color.deviceTextColor));
                        selectedSubDevice.getChildAt(1)
                                .setVisibility(View.GONE);
                    }
                    selectedSubDevice = subColumlayout;
                    // 设置当前点击机型被选中
                    ((TextView) subColumlayout.getChildAt(0))
                            .setTextColor(getResources().getColor(
                                    R.color.deviceSecectedTextColor));
                    subColumlayout.getChildAt(1).setVisibility(
                            View.VISIBLE);
                }
            });

            tb.removeView(tr);
            deviceDialogSubDeviceTableLayout.addView(tr);
        }
    }

    /**
     * 机型选择确定按钮
     */
    View.OnClickListener deviceDialogBtnSureListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if (selectedDevice != null) {
                TextView name = (TextView) selectedDevice.findViewById(R.id.tv_device_name);
                deviceIdTV.setText(name.getText().toString());
            }
            if (selectedSubDevice != null) {
                subdeviceIdTV.setText(((TextView) selectedSubDevice.getChildAt(0))
                        .getText());
            } else {
                subdeviceIdTV.setText("");
            }
            dialog.cancel();
        }
    };
    /**
     * 机型选择关闭按钮
     */
    View.OnClickListener deviceDialogBtnCloseListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            dialog.cancel();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentSelectedRecord = null;
    }

    @Override
    protected BaseFragment getFragment() {
        return this;
    }
}

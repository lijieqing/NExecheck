package com.kstech.nexecheck.activity.upload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.CheckStatusListAdspter;
import com.kstech.nexecheck.adapter.DataUploadListAdapter;
import com.kstech.nexecheck.adapter.GVUploadAdapter;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.domain.config.vo.SubDeviceVO;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckRecordStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.domain.excel.ExcelUtil;
import com.kstech.nexecheck.domain.excel.FtpUploadTask;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.utils.ViewTemplateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DataUploadActivity extends BaseActivity {

    // 列表组件
    private ListView listView = null;

    // 被选中的机型与子机型单元格
    private View selectedDevice;
    private LinearLayout selectedSubDevice;

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
    private Button openCheckRecordExitBtn;
    // 数据删除按钮
    private Button deleteDataBtn;
    // 数据上传按钮
    private Button uploadDataBtn;

    // 全选按钮
    private CheckBox allSelectedCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_upload);
        // 初始化菜单，传入参数，设置子标题
        updateSubtitle("---  数据上传");


        // 初始化页面组件
        initViewComp();

        // 刷新检查记录列表
        loadCheckRecordList(checkFinishTimeTV.getText().toString(),checkStatusTV.getText().toString(),
                excIdTV.getText().toString(),deviceIdTV.getText().toString(),subdeviceIdTV.getText().toString());

    }

    @Override
    public Activity getactivity() {
        return this;
    }

    private void initViewComp(){
        checkFinishTimeLayout = (LinearLayout)findViewById(R.id.checkFinishTimeLayout);
        checkFinishTimeLayout.setOnClickListener(checkFinishTimeLayoutListener);
        checkFinishTimeTV = (TextView)findViewById(R.id.checkFinishTimeTV);

        checkStatusLayout = (LinearLayout)findViewById(R.id.checkStatusLayout);
        checkStatusLayout.setOnClickListener(checkStatusLayoutListener);
        checkStatusTV = (TextView)findViewById(R.id.checkStatusTV);

        excIdLayout = (LinearLayout)findViewById(R.id.excIdLayout);
        excIdLayout.setOnClickListener(excIdLayoutListener);
        excIdTV = (TextView)findViewById(R.id.excIdTV);

        deviceIdLayout = (LinearLayout)findViewById(R.id.deviceIdLayout);
        deviceIdLayout.setOnClickListener(deviceIdLayoutListener);
        deviceIdTV = (TextView)findViewById(R.id.deviceIdTV);

        subdeviceIdTV = (TextView)findViewById(R.id.subdeviceIdTV);

        openCheckRecordClearBtn = (Button) findViewById(R.id.openCheckRecordClearBtn);
        openCheckRecordClearBtn.setOnClickListener(openCheckRecordClearBtnListener);
        openCheckRecordSearchBtn = (Button) findViewById(R.id.openCheckRecordSearchBtn);
        openCheckRecordSearchBtn.setOnClickListener(openCheckRecordSearchBtnListener);

        openCheckRecordExitBtn = (Button) findViewById(R.id.openCheckRecordExitBtn);
        openCheckRecordExitBtn.setOnClickListener(openCheckRecordExitBtnListener);

        allSelectedCB = (CheckBox) findViewById(R.id.allSelectedCB);
        allSelectedCB.setOnCheckedChangeListener(allSelectedCBListener);
        deleteDataBtn = (Button) findViewById(R.id.deleteDataBtn);
        deleteDataBtn.setOnClickListener(deleteDataBtnListener);

        uploadDataBtn = (Button) findViewById(R.id.uploadDataBtn);
        uploadDataBtn.setOnClickListener(uploadDataBtnListener);
    }

    private DataUploadListAdapter adspter;
    private List<CheckRecordEntity> listData;
    /**
     * 刷新检查记录列表
     */
    private void loadCheckRecordList(String finishTime,String checkStatus,
                                     String excId,String deviceId,String subdeviceId) {
        listView = (ListView) findViewById(R.id.checkRecordList);
        listData = CheckRecordDao.findCheckRecordByCondition(this,finishTime,checkStatus, excId,deviceId,subdeviceId);
        adspter = new DataUploadListAdapter(this, listData);
        listView.setAdapter(adspter);
        listView.setOnItemClickListener(listViewOnitemListener);
    }
    /**
     * 全选，复选框点击监听事件
     */
    OnCheckedChangeListener allSelectedCBListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                for (int i = 0; i < listData.size(); i++) {
                    listData.get(i).setCheckBoxState(true);
                }
                //通知适配器更新UI
                adspter.notifyDataSetChanged();
            } else {
                for (int i = 0; i < listData.size(); i++) {
                    listData.get(i).setCheckBoxState(false);
                }
                //通知适配器更新UI
                adspter.notifyDataSetChanged();
            }
        }
    };
    /**
     * 数据删除，按钮监听事件
     */
    View.OnClickListener deleteDataBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            //创建一个要删除内容的集合，不能直接在数据源data集合中直接进行操作，否则会报异常
            final List<CheckRecordEntity> deleSelect = new ArrayList<CheckRecordEntity>();
            //把选中的条目要删除的条目放在deleSelect这个集合中
            for (int i = 0; i < listData.size(); i++) {
                if (listData.get(i).getCheckBoxState()) {
                    deleSelect.add(listData.get(i));
                }
            }
            //判断用户是否选中要删除的数据及是否有数据
            if (deleSelect.size() != 0 && listData.size() != 0) {
                new AlertDialog.Builder(DataUploadActivity.this).setTitle(R.string.diaLogWakeup).setMessage(R.string.delCheckRecordConfirm).setNegativeButton(R.string.str_close,null).setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //从数据源listData中删除数据
                        listData.removeAll(deleSelect);
                        //从数据库中删除数据
                        DatabaseManager dm = DatabaseManager.getInstance(getactivity());
                        for (CheckRecordEntity cr:deleSelect) {
                            dm.delete("check_record", "exc_id=?", new String[]{cr.getExcId()});
                            dm.delete("check_item", "exc_id=?", new String[]{cr.getExcId()});
                            dm.delete("check_item_detail", "exc_id=?", new String[]{cr.getExcId()});
                        }
                        //把deleSelect集合中的数据清空
                        deleSelect.clear();
                        //把全选复选框设置为false
                        allSelectedCB.setChecked(false);
                        //通知适配器更新UI
                        adspter.notifyDataSetChanged();
                        Toast.makeText(DataUploadActivity.this, R.string.delSuccess, Toast.LENGTH_SHORT).show();
                    }
                }).show();
            } else if (listData.size() == 0) {
                Toast.makeText(DataUploadActivity.this, R.string.noNeedDeleteData, Toast.LENGTH_SHORT).show();
            } else if (deleSelect.size() == 0) {
                Toast.makeText(DataUploadActivity.this, R.string.pleaseCheckedNeedDeleteData, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 数据上传按钮点击事件
     */
    View.OnClickListener uploadDataBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            try {
                String ssid = "";
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiinfo = manager.getConnectionInfo();
                if(wifiinfo!=null){
                    ssid = wifiinfo.getSSID();
                    Log.e("LOGIN","---S S I D---"+ssid);
                }
                Log.e("LOGINT","---S S I D---"+ssid+"---"+ Globals.getCurrentCheckLine().getName());
                if(!ssid.equals("\""+ Globals.getCurrentCheckLine().getSsid()+"\"")){
                    Log.e("LOGIN","---L O C A L  S S I D---"+ ConfigFileManager.getInstance(getactivity()).getLastSsid());
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.uploadError)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }
                if(!ssid.equals("\""+ Globals.getResConfig().getUpLoadSSID()+"\"")){
                    Log.e("LOGIN","---L O C A L  S S I D---"+ConfigFileManager.getInstance(getactivity()).getLastSsid());
                    new AlertDialog.Builder(getactivity())
                            .setMessage(R.string.uploadError)
                            .setNeutralButton(R.string.str_ok, null).show();
                    return;
                }else {
                    Log.e("LOGIN","---L O C A L  S S I D---"+ConfigFileManager.getInstance(getactivity()).getLastSsid());
                }
                //创建一个要删除内容的集合，不能直接在数据源data集合中直接进行操作，否则会报异常
                final List<CheckRecordEntity> uploadSelect = new ArrayList<CheckRecordEntity>();
                //把选中的条目要删除的条目放在deleSelect这个集合中
                for (int i = 0; i < listData.size(); i++) {
                    if (listData.get(i).getCheckBoxState()) {
                        uploadSelect.add(listData.get(i));
                    }
                }
                if (uploadSelect.size() == 0) {
                    Toast.makeText(DataUploadActivity.this, R.string.pleaseCheckedNeedUploadData, Toast.LENGTH_SHORT).show();
                    return;
                }
                // 查询需要上传的数据
                List<CheckRecordEntity> uploadData = CheckRecordDao.findUploadData(getactivity(),uploadSelect);

                for (CheckRecordEntity record:uploadData) {
                    if("0".equals(record.getCheckStatus())){
                        new AlertDialog.Builder(getactivity())
                                .setMessage(R.string.recordStateError)
                                .setNeutralButton(R.string.str_ok, null).show();
                        return;
                    }
                }

                // 生成excel文件
//				Map<String, String> filePathMap = ExcelUtil.writeExcel(DataUploadActivity.this,uploadData);
                Map<String, String> filePathMap = ExcelUtil.UpdateExcelByTemplate(uploadData);
                // 异步任务上传文件
                FtpUploadTask ftpTask = new FtpUploadTask(filePathMap,getactivity());
                ftpTask.execute(100);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("UPLOAD","DataUpload异常"+e.toString());
            }
        }
    };

    /**
     * listView，检查记录列表点击，监听事件
     */
    OnItemClickListener listViewOnitemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // 点击列表记录，更改复选框的状态
            listData.get(arg2).setCheckBoxState(!listData.get(arg2).getCheckBoxState());
            // 通知UI更新视图
            adspter.notifyDataSetChanged();
        }
    };

    View.OnClickListener checkFinishTimeLayoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View arg0) {
//			DateTimePickDialogUtil dp = new DateTimePickDialogUtil(OpenCheckRecordActivity.this,checkFinishTimeTV.getText().toString());
//			dp.dateTimePicKDialog(checkFinishTimeTV);
            Calendar cal = Calendar.getInstance();
            final DatePickerDialog mDialog;
            if ("".equals(checkFinishTimeTV.getText().toString())) {
                mDialog = new DatePickerDialog(DataUploadActivity.this, null,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            } else {
                int[] yearMonthDay = DateUtil.getYearMonthDay(checkFinishTimeTV.getText().toString());
                mDialog = new DatePickerDialog(DataUploadActivity.this, null,
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
        @Override
        public void onClick(View arg0) {
            // 展示状态选择dialog
            LayoutInflater inflater = (LayoutInflater) getactivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.check_status_view, null);
            ListView listView = (ListView) view
                    .findViewById(R.id.checkStatusListView);
            final List<Map<String, Object>> result = CheckRecordDao.findAllCheckStatus();
            CheckStatusListAdspter adapter = new CheckStatusListAdspter(getactivity(),result);
            listView.setAdapter(adapter);
            // 此处的上下文必须是承载该dialog的activity。否则dialog无法显示，也不抛出异常，纠结了我1个多小时
            AlertDialog.Builder builder = new AlertDialog.Builder(DataUploadActivity.this);
            // 此处需要注意，setview的视图需要时父视图，而不能使父下的子视图
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            dialog.show();
            // 宽度设置不合理，则点击屏幕任意位置收键盘事件就不好使了
            dialog.getWindow().setLayout(600, 400);
            // end

            listView.setOnItemClickListener(new OnItemClickListener() {
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
            LayoutInflater inflater = (LayoutInflater) DataUploadActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.excid_dialog, null);
            exiIdDialogTableLayout = (TableLayout)view.findViewById(R.id.exiIdDialogTableLayout);
            // 展示机型选择dialog
            excIdTable(exiIdDialogTableLayout,null);
            builder = new AlertDialog.Builder(DataUploadActivity.this);
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
            final List<CheckRecordEntity> list = CheckRecordDao.findCheckRecordByCondition(getactivity(),null,null,excIdSearch,null,null);
            // 整行数
            int whole = list.size() / 3;
            // 余数
            int remain = list.size() % 3;
            // 总行数
            int trTotal = remain == 0 ? whole : whole + 1;

            for (int i = 0; i < trTotal; i++) {
                ViewTemplateUtil tru = new ViewTemplateUtil(DataUploadActivity.this);
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
        @Override
        public void onClick(View arg0) {
            // 展示机型选择dialog
            LayoutInflater inflater = (LayoutInflater) DataUploadActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.device_dialog, null);
            GridView gvDevice = (GridView)view.findViewById(R.id.gv_device_update_activity);
            final TableLayout deviceDialogSubDeviceTableLayout = (TableLayout)view.findViewById(R.id.deviceDialogSubDeviceTableLayout);

            deviceDialogBtnSure = (Button)view.findViewById(R.id.deviceDialogBtnSure);
            deviceDialogBtnSure.setOnClickListener(deviceDialogBtnSureListener);

            deviceDialogBtnClose = (Button)view.findViewById(R.id.deviceDialogBtnClose);
            deviceDialogBtnClose.setOnClickListener(deviceDialogBtnCloseListener);

            final List<DeviceVO> deviceList = Globals.getResConfig().getDeviceList();
            final GVUploadAdapter gvUploadAdapter = new GVUploadAdapter(deviceList,getactivity());
            gvDevice.setAdapter(gvUploadAdapter);
            gvDevice.setOnItemClickListener(new OnItemClickListener() {
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

            builder = new AlertDialog.Builder(DataUploadActivity.this);
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
            ViewTemplateUtil tru = new ViewTemplateUtil(DataUploadActivity.this);
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
            loadCheckRecordList(checkFinishTimeTV.getText().toString(), CheckRecordStatusEnum.getCode(checkStatusTV.getText().toString()),
                    excIdTV.getText().toString(),deviceIdTV.getText().toString(),subdeviceIdTV.getText().toString());
        }
    };

    /*
     * 检测记录，退出按钮，监听事件
     */
    View.OnClickListener openCheckRecordExitBtnListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        finish();
    }
}



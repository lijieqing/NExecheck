package com.kstech.nexecheck.view.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.domain.config.vo.SubDeviceVO;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.exception.ExcException;
import com.kstech.nexecheck.utils.DeviceUtil;
import com.kstech.nexecheck.utils.Globals;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijie on 2017/5/27.
 */

public class CreateCheckRecordFragment extends BaseFragment {
    private GridView gridView;
    private ListView listView;
    private Button btnSureId, btnExitId;
    private List<DeviceVO> deviceVOs;
    private List<SubDeviceVO> subDeviceVOs;
    private GVDeviceAdapter deviceAdapter;
    private SubDeviceAdapter subDevAdapter;
    private int currentPosition = -1;
    private int currentSubPosition = -1;
    private String devID;
    private String devName;
    private String subDevID;
    private String subDevName;

    private EditText excIdET;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceVOs = Globals.getResConfig().getDeviceList();
        subDeviceVOs = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_create_check_record,null);
        gridView = (GridView) view.findViewById(R.id.gv_create_view);
        listView = (ListView) view.findViewById(R.id.lv_create_record);
        btnExitId = (Button) view.findViewById(R.id.btnExitId);
        btnSureId = (Button) view.findViewById(R.id.btnSureId);
        excIdET = (EditText) view.findViewById(R.id.excIdET);
        //view 初始化相关
        deviceAdapter = new GVDeviceAdapter();
        subDevAdapter = new SubDeviceAdapter();

        gridView.setAdapter(deviceAdapter);
        listView.setAdapter(subDevAdapter);

        //监听事件
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvID = (TextView) view.findViewById(R.id.tv_device_id);
                TextView tvName = (TextView) view.findViewById(R.id.tv_device_name);
                if (currentPosition == position){
                    currentPosition = -1;
                    devID = null;
                    devName = null;
                    subDeviceVOs.clear();
                }else {
                    currentSubPosition = -1;
                    subDevID = null;
                    subDevName = null;

                    currentPosition = position;
                    subDeviceVOs.clear();
                    subDeviceVOs.addAll(deviceVOs.get(position).getSubDeviceList());
                    devID = tvID.getText().toString();
                    devName = tvName.getText().toString();
                }
                deviceAdapter.notifyDataSetChanged();
                subDevAdapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvID = (TextView) view.findViewById(R.id.tv_device_id);
                TextView tvName = (TextView) view.findViewById(R.id.tv_device_name);
                if (currentSubPosition == position){
                    currentSubPosition = -1;
                    subDevID = null;
                    subDevName = null;
                }else {
                    currentSubPosition = position;
                    subDevID = tvID.getText().toString();
                    subDevName = tvName.getText().toString();
                }

                subDevAdapter.notifyDataSetChanged();
            }
        });

        btnExitId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                excIdET.setText("");
                currentPosition = -1;
                currentSubPosition = -1;
                ((HomeActivity)activity).showFg = null;
                ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                getFragmentManager().beginTransaction().remove(CreateCheckRecordFragment.this).commit();
            }
        });

        btnSureId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    // 根据用户选择，新建机型
                    try {
                        Globals.loadDeviceModelFile(devID, subDevID,getActivity());
                    } catch (ExcException excException) {
                        Toast.makeText(getActivity(), excException.getErrorMsg(), Toast.LENGTH_SHORT).show();
                        Log.e("CreateCheckRecord", excException.getErrorMsg());
                        return;
                    }
                    // 插入 检验记录表
                    CheckRecordDao.addCheckRecord(getActivity(),excIdET.getText().toString(), devID, devName, subDevID, subDevName);

                    Toast.makeText(getActivity(), R.string.saveSuccess, Toast.LENGTH_LONG).show();

                    ((HomeActivity)activity).homeCheckEntityFragment.myAdapter.notifyDataSetChanged();

                    ((HomeActivity)activity).showFg = null;
                    ((HomeActivity)activity).llCheck.setVisibility(View.INVISIBLE);
                    ((HomeActivity)activity).initRecordItem(excIdET.getText().toString());

                    //还原默认值
                    excIdET.setText("");
                    currentPosition = -1;
                    currentSubPosition = -1;
                    getFragmentManager().beginTransaction().remove(CreateCheckRecordFragment.this).commit();
                }
            }
        });

        return view;
    }


    private boolean validate() {
        // 判断机型与挖掘机出厂编号是否为空
        if (currentPosition == -1) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.pleaseSelectedDevice)
                    .setNeutralButton(R.string.str_ok, null).show();
            return false;
        }else {
            if (currentSubPosition == -1){
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.pleaseSelectedSubDevice)
                        .setNeutralButton(R.string.str_ok, null).show();
                return false;
            }
        }
        String excId = excIdET.getText().toString().trim();
        if ("".equals(excId)) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.pleaseInputExcId)
                    .setNeutralButton(R.string.str_ok, null).show();
            return false;
        }
        if (null != CheckRecordDao.findCheckRecordByExcId(getActivity(),excId)) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.excIdIsExist)
                    .setNeutralButton(R.string.str_ok, null).show();
            return false;
        }
        return true;
    }

    //gridview 适配器
    private class GVDeviceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return deviceVOs.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceVOs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GVHolder holder = null;
            if (convertView == null){
                holder = new GVHolder();
                convertView = View.inflate(getActivity(),R.layout.create_record_item,null);
                holder.tv = (TextView) convertView.findViewById(R.id.tv_device_name);
                holder.iv = (ImageView) convertView.findViewById(R.id.iv_device_select);
                holder.id = (TextView) convertView.findViewById(R.id.tv_device_id);
                holder.cl = (ConstraintLayout) convertView.findViewById(R.id.ll_rv_item);
                convertView.setTag(holder);
            }else {
                holder = (GVHolder) convertView.getTag();
            }
            holder.tv.setText(deviceVOs.get(position).getDeviceName());

            holder.id.setText(deviceVOs.get(position).getDeviceId());
            holder.cl.setMaxHeight(DeviceUtil.deviceHeight(getActivity())/9);
            if (currentPosition == position){
                holder.iv.setVisibility(View.VISIBLE);
                holder.tv.setTextSize(20);
                holder.tv.setTextColor(Color.parseColor("#f2741b"));
            }else {
                holder.iv.setVisibility(View.INVISIBLE);
                holder.tv.setTextSize(18);
                holder.tv.setTextColor(Color.parseColor("#1b1a1a"));
            }

            return convertView;
        }

        class GVHolder{
            TextView tv;
            TextView id;
            ImageView iv;
            ConstraintLayout cl;
            GVHolder() {
            }
        }

    }

    private class SubDeviceAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return subDeviceVOs.size();
        }

        @Override
        public Object getItem(int position) {
            return subDeviceVOs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LVHolder holder = null;
            if (convertView == null){
                holder = new LVHolder();
                convertView = View.inflate(getActivity(),R.layout.create_record_item,null);
                holder.tv = (TextView) convertView.findViewById(R.id.tv_device_name);
                holder.iv = (ImageView) convertView.findViewById(R.id.iv_device_select);
                holder.id = (TextView) convertView.findViewById(R.id.tv_device_id);
                holder.cl = (ConstraintLayout) convertView.findViewById(R.id.ll_rv_item);
                convertView.setTag(holder);
            }else {
                holder = (LVHolder) convertView.getTag();
            }
            holder.tv.setText(subDeviceVOs.get(position).getSubDevName());
            holder.id.setText(subDeviceVOs.get(position).getSubDevId());
            holder.cl.setMaxHeight(DeviceUtil.deviceHeight(getActivity())/9);
            if (currentSubPosition == position){
                holder.iv.setVisibility(View.VISIBLE);
                holder.tv.setTextSize(20);
                holder.tv.setTextColor(Color.parseColor("#f2741b"));
            }else {
                holder.iv.setVisibility(View.INVISIBLE);
                holder.tv.setTextSize(18);
                holder.tv.setTextColor(Color.parseColor("#1b1a1a"));
            }

            return convertView;
        }
        class LVHolder{
            TextView tv;
            TextView id;
            ImageView iv;
            ConstraintLayout cl;
            LVHolder() {
            }
        }
    }
}

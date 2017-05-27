package com.kstech.nexecheck.view.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.domain.config.vo.SubDeviceVO;
import com.kstech.nexecheck.utils.DeviceUtil;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijie on 2017/5/27.
 */

public class CreateCheckRecordFragment extends Fragment {
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
    private String subDevID;
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
                if (currentPosition == position){
                    currentPosition = -1;
                    devID = null;
                    subDeviceVOs.clear();
                }else {
                    currentSubPosition = -1;
                    subDevID = null;

                    currentPosition = position;
                    subDeviceVOs.clear();
                    subDeviceVOs.addAll(deviceVOs.get(position).getSubDeviceList());
                    devID = tvID.getText().toString();
                }
                deviceAdapter.notifyDataSetChanged();
                subDevAdapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvID = (TextView) view.findViewById(R.id.tv_device_id);
                if (currentSubPosition == position){
                    currentSubPosition = -1;
                    subDevID = null;
                }else {
                    currentSubPosition = position;
                    subDevID = tvID.getText().toString();
                }

                subDevAdapter.notifyDataSetChanged();
            }
        });

        return view;
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

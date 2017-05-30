package com.kstech.nexecheck.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.vo.DeviceVO;
import com.kstech.nexecheck.utils.DeviceUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.List;

//gridview 适配器
public class GVUploadAdapter extends BaseAdapter {
    private List<DeviceVO> deviceVOs;
    private Context context;

    public GVUploadAdapter(List<DeviceVO> deviceVOs, Context context) {
        this.deviceVOs = deviceVOs;
        this.context = context;
    }

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
        if (convertView == null) {
            holder = new GVHolder();
            convertView = View.inflate(context, R.layout.create_record_item, null);
            holder.tv = (TextView) convertView.findViewById(R.id.tv_device_name);
            holder.iv = (ImageView) convertView.findViewById(R.id.iv_device_select);
            holder.id = (TextView) convertView.findViewById(R.id.tv_device_id);
            holder.cl = (ConstraintLayout) convertView.findViewById(R.id.ll_rv_item);
            convertView.setTag(holder);
        } else {
            holder = (GVHolder) convertView.getTag();
        }
        holder.tv.setText(deviceVOs.get(position).getDeviceName());

        holder.id.setText(deviceVOs.get(position).getDeviceId());
        holder.cl.setMaxHeight(DeviceUtil.deviceHeight(context) / 9);
        if (Globals.UploatLastPosition == position) {
            holder.iv.setVisibility(View.VISIBLE);
            holder.tv.setTextSize(20);
            holder.tv.setTextColor(Color.parseColor("#f2741b"));
        } else {
            holder.iv.setVisibility(View.INVISIBLE);
            holder.tv.setTextSize(18);
            holder.tv.setTextColor(Color.parseColor("#1b1a1a"));
        }

        return convertView;
    }

    class GVHolder {
        TextView tv;
        TextView id;
        ImageView iv;
        ConstraintLayout cl;

        GVHolder() {
        }
    }

}
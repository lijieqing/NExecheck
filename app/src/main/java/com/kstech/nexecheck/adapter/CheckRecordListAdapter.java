package com.kstech.nexecheck.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.db.dbenum.CheckRecordStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;

import java.util.List;

public class CheckRecordListAdapter extends BaseAdapter {

	private List<CheckRecordEntity> data;
	private LayoutInflater layoutInflater;
	public CheckRecordListAdapter(Context context, List<CheckRecordEntity> data){
		this.data=data;
		this.layoutInflater=LayoutInflater.from(context);
	}
	/**
	 * 组件集合，对应list.xml中的控件
	 * @author Administrator
	 */
	public final class CheckRecordList{
		public TextView finishTimeTV;
		public TextView deviceNameTV;
		public TextView subdeviceNameTV;
		public TextView excIdTV;
		public TextView checkStatusImage;
		public TextView checkerNameTV;
		public TextView checklineNameTV;
	}
	@Override
	public int getCount() {
		return data.size();
	}
	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		return data.get(position);
	}
	/**
	 * 获得唯一标识
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		CheckRecordList crList = null;
		if(convertView==null){
			crList = new CheckRecordList();
			///获得组件，实例化组件
			convertView=layoutInflater.inflate(R.layout.check_record_list, null);
			crList.finishTimeTV=(TextView)convertView.findViewById(R.id.finishTimeTV);
			crList.deviceNameTV=(TextView)convertView.findViewById(R.id.deviceNameTV);
			crList.subdeviceNameTV=(TextView)convertView.findViewById(R.id.subdeviceNameTV);
			crList.excIdTV=(TextView)convertView.findViewById(R.id.excIdTV);
			crList.checkStatusImage=(TextView)convertView.findViewById(R.id.checkStatusImage);
			crList.checkerNameTV=(TextView)convertView.findViewById(R.id.checkerNameTV);
			crList.checklineNameTV=(TextView)convertView.findViewById(R.id.checklineNameTV);
			convertView.setTag(crList);
		}else{
			crList=(CheckRecordList)convertView.getTag();
		}
		//绑定数据
		crList.finishTimeTV.setText((String)data.get(position).getFinishTime());
		crList.deviceNameTV.setText((String)data.get(position).getDeviceName());
		crList.subdeviceNameTV.setText((String)data.get(position).getSubdeviceName());
		crList.excIdTV.setText((String)data.get(position).getExcId());
		crList.checkStatusImage.setText(CheckRecordStatusEnum.getName(data.get(position).getCheckStatus()));
		crList.checkerNameTV.setText((String)data.get(position).getCheckerName());
		crList.checklineNameTV.setText((String)data.get(position).getChecklineName());
		return convertView;
	}
}

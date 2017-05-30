package com.kstech.nexecheck.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;

import com.kstech.nexecheck.R;


/**
 * 
 * @author wanghaibin
 * @created 2016-10-2 ����11:20:22
 * @since v1.0
 */
@SuppressWarnings("ResourceAsColor")
public class ViewTemplateUtil extends View {

	private Context context;
	public ViewTemplateUtil(Context context) {
		super(context);
		this.context = context;
	}

	public TableLayout getDeviceTableLayout(){
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.table_template, null);
		TableLayout tb = (TableLayout)view.findViewById(R.id.deviceTableLayoutTemplate);

		return tb;
	}

	public TableLayout getSubDeviceTableLayout(){
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.table_template, null);
		TableLayout tb = (TableLayout)view.findViewById(R.id.subDeviceTableLayoutTemplate);

		return tb;
	}

}

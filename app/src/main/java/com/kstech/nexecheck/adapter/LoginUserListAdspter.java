package com.kstech.nexecheck.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.kstech.nexecheck.R;

import java.util.List;
import java.util.Map;

public class LoginUserListAdspter extends BaseAdapter {
	private List<Map<String, Object>> data;
	private LayoutInflater layoutInflater;
	public LoginUserListAdspter(Context context, List<Map<String, Object>> data){
		this.data=data;
		this.layoutInflater=LayoutInflater.from(context);
	}
	/**
	 * 组件集合，对应list.xml中的控件
	 * @author Administrator
	 */
	public final class UserList{
		public TextView name;
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
		UserList userList = null;
		if(convertView==null){
			userList = new UserList();
			///获得组件，实例化组件
			convertView=layoutInflater.inflate(R.layout.login_user_list, null);
			userList.name=(TextView)convertView.findViewById(R.id.loginUserName);
			convertView.setTag(userList);
		}else{
			userList=(UserList)convertView.getTag();
		}
		//绑定数据
		userList.name.setText((String)data.get(position).get("name"));
		return convertView;
	}
}

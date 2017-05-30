package com.kstech.nexecheck.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.user.CurrentUserActivity;
import com.kstech.nexecheck.base.BaseActivity;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.db.DatabaseManager;
import com.kstech.nexecheck.domain.db.dbenum.UserStatusEnum;
import com.kstech.nexecheck.utils.DateUtil;
import com.kstech.nexecheck.utils.Globals;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CurrentUserListAdapter extends BaseAdapter {

	private List<Map<String, Object>> data;
	private LayoutInflater layoutInflater;
	private BaseActivity context;
	public CurrentUserListAdapter(BaseActivity context, List<Map<String, Object>> data){
		this.context = context;
		this.data=data;
		this.layoutInflater=LayoutInflater.from(context);
	}
	/**
	 * 组件集合，对应list.xml中的控件
	 * @author Administrator
	 */
	public final class UserList{
		public TextView code;
		public ImageView image;
		public TextView name;
		public ImageView delUserBtn;
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
			convertView=layoutInflater.inflate(R.layout.current_user_list, null);
			userList.code=(TextView)convertView.findViewById(R.id.code);
			userList.image=(ImageView)convertView.findViewById(R.id.image);
			userList.name=(TextView)convertView.findViewById(R.id.name);
			userList.delUserBtn=(ImageView)convertView.findViewById(R.id.delUserBtn);
			convertView.setTag(userList);
		}else{
			userList=(UserList)convertView.getTag();
		}
		//绑定数据
		userList.code.setText((String)data.get(position).get("code"));
		userList.image.setBackgroundResource((Integer)data.get(position).get("image"));
		userList.name.setText((String)data.get(position).get("name"));

		//给delUserBtn添加单击事件  添加Button之后ListView将失去焦点  需要的直接把Button的焦点去掉
		userList.delUserBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ConfigFileManager.getInstance(context).getLastUserName().equals((String)data.get(position).get("name"))){
					Toast.makeText(context,"不能删除当前用户",Toast.LENGTH_SHORT).show();
					return;
				}
				new AlertDialog.Builder(context).setTitle(R.string.diaLogWakeup).setMessage(R.string.delUserConfirm).setNegativeButton(R.string.str_close,null).setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Map<String, Object> map = (Map<String, Object>) getItem(position);
						ContentValues cv = new ContentValues();
						cv.put("status", UserStatusEnum.DISABLE.getCode());
						cv.put("stop_user_code", Globals.getCurrentUser().getCode());
						cv.put("stop_time", DateUtil.getDateTimeFormat(new Date()));
						DatabaseManager.getInstance(context).update("user", cv, "code=?", new String[]{(String)map.get("code")});
						// 刷新列表
						((CurrentUserActivity)context).initUserList();
						Toast.makeText(context,R.string.delSuccess,Toast.LENGTH_LONG).show();
					}
				}).show();
			}
		});

		return convertView;
	}
}

package com.kstech.nexecheck.domain.checkline;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.vo.CheckLineVO;


/**
 * adapter for check line display
 */
public class CheckLineListAdapter extends BaseAdapter {

	private List<CheckLineVO> data;
	private LayoutInflater layoutInflater;

	/**
	 * Instantiates a new Check line list adapter.
	 *
	 * @param data the data
	 */
	public CheckLineListAdapter(List<CheckLineVO> data, Context context){
		this.data=data;
		this.layoutInflater=LayoutInflater.from(context);
	}

	/**
	 * 组件集合，对应list.xml中的控件
	 *
	 * @author Administrator
	 */
	public final class CheckLineList{
		/**
		 * The Name.
		 */
		public TextView name;
		/**
		 * The Ssid.
		 */
		public TextView ssid;
		/**
		 * The Password.
		 */
		public TextView password;
		/**
		 * The Ip.
		 */
		public TextView ip;
		/**
		 * The Terminal id.
		 */
		public TextView terminalID;
		/**
		 * The Image.
		 */
		public ImageView image;
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
		CheckLineList checkLineList = null;
		if(convertView==null){
			checkLineList = new CheckLineList();
			///获得组件，实例化组件
			convertView=layoutInflater.inflate(R.layout.check_line_list, null);
			checkLineList.ssid=(TextView)convertView.findViewById(R.id.checkLineSsid);
			checkLineList.name=(TextView)convertView.findViewById(R.id.checkLineName);
			checkLineList.ip=(TextView)convertView.findViewById(R.id.checkLineIP);
			checkLineList.password=(TextView)convertView.findViewById(R.id.checkLinePwd);
			checkLineList.terminalID=(TextView)convertView.findViewById(R.id.checkLineTerminalID);
			checkLineList.image=(ImageView)convertView.findViewById(R.id.checkLineImage);
			convertView.setTag(checkLineList);
		}else{
			checkLineList=(CheckLineList)convertView.getTag();
		}
		//绑定数
		checkLineList.ssid.setText((String)data.get(position).getSsid());
		checkLineList.name.setText((String)data.get(position).getName());
		checkLineList.ip.setText((String)data.get(position).getIp());
		checkLineList.password.setText((String)data.get(position).getPassword());
		checkLineList.terminalID.setText((String)data.get(position).getTerminalID());
		checkLineList.image.setBackgroundResource((Integer)data.get(position).getImage());

		return convertView;
	}
}

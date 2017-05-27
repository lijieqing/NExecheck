package com.kstech.nexecheck.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckItemVO;
import com.kstech.nexecheck.domain.db.dao.CheckItemDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.utils.Globals;


/**
 * @author lijie
 * @created 2016-9-24 ����1:39:34
 * @since v1.0
 *
 * adapter for 检测项目列表
 */
public class CheckItemListAdapter extends BaseAdapter {

	private List<CheckItemEntity> data;
	private LayoutInflater layoutInflater;
	private Context context;
	private List<CheckItemVO> values;

	public CheckItemListAdapter(Context context, List<CheckItemEntity> data) {
		this.context = context;
		this.data = data;
		this.layoutInflater = LayoutInflater.from(context);
	}

	public CheckItemListAdapter(Context context, ArrayList<CheckItemVO> values) {
		this.context = context;
		this.values = values;
		this.layoutInflater = LayoutInflater.from(context);
	}
	/**
	 * 组件集合，对应list.xml中的控件
	 *
	 * @author Administrator
	 */
	public final class CheckItemView {
		public ImageView arrRight;
		public TextView itemIdTv, itemNameTv;
		public ImageView itemCheckResultImage;
	}

	@Override
	public int getCount() {
		return values.size();
	}

	/**
	 * 获得某一位置的数据
	 */
	@Override
	public Object getItem(int position) {
		return values.get(position);
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
		CheckItemView viewHolder = null;
		if (convertView == null) {
			viewHolder = new CheckItemView();
			// /获得组件，实例化组件
			convertView = layoutInflater
					.inflate(R.layout.check_item_list, null);
			viewHolder.arrRight = (ImageView) convertView
					.findViewById(R.id.item_arr_rightImage);
			viewHolder.itemIdTv = (TextView) convertView
					.findViewById(R.id.itemIdTv);
			viewHolder.itemNameTv = (TextView) convertView
					.findViewById(R.id.itemNameTv);
			viewHolder.itemCheckResultImage = (ImageView) convertView
					.findViewById(R.id.itemCheckResultImage);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (CheckItemView) convertView.getTag();
		}

		// 绑定数据
		viewHolder.itemIdTv.setText(values.get(position).getId());
		viewHolder.itemNameTv.setText(values.get(position).getName());

		String status = CheckItemDao.getSingleCheckItemFromDB(ConfigFileManager.getInstance(context).getLastExcid(), values.get(position).getId(),context).getCheckStatus();
		int statusImage;
		if (status == null || status.equals(CheckItemStatusEnum.UN_CHECK.getCode())) {
			statusImage = R.drawable.index_item_uncheck;
		} else if (status.equals(CheckItemStatusEnum.UN_FINISH.getCode())) {
			statusImage = R.drawable.index_item_unfinish;
		} else if (status.equals(CheckItemStatusEnum.PASS.getCode())) {
			statusImage = R.drawable.index_item_pass;
		} else {
			// CheckItemStatusEnum.UN_PASS.getCode()
			statusImage = R.drawable.index_item_unpass;
		}
		viewHolder.itemCheckResultImage.setBackgroundResource(statusImage);

		// 设置UI被选中的状态
		/*CheckItemEntity selectedCheckItem = context.getSelectedCheckItem();
		if (selectedCheckItem != null
				&& selectedCheckItem.getItemId().equals(viewHolder.itemIdTv.getText())) {
			viewHolder.arrRight.setVisibility(View.VISIBLE);
			viewHolder.itemNameTv.setTextColor(context.getResources().getColor(
					R.color.selectedCheckItemTextColor));
		} else {
			viewHolder.arrRight.setVisibility(View.GONE);
			viewHolder.itemNameTv.setTextColor(context.getResources().getColor(
					R.color.white));
		}*/
		if (ConfigFileManager.getInstance(context).getLastItemid().equals(viewHolder.itemIdTv.getText())) {

		}

		if (Globals.HomeLastPosition == position) {
			viewHolder.arrRight.setVisibility(View.VISIBLE);
			viewHolder.itemNameTv.setTextColor(context.getResources().getColor(
					R.color.selectedCheckItemTextColor));
		} else {
			viewHolder.arrRight.setVisibility(View.GONE);
			viewHolder.itemNameTv.setTextColor(context.getResources().getColor(
					R.color.white));
		}

		return convertView;
	}
}

/**
 * 
 */
package com.kstech.nexecheck.view.widget;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;
import com.kstech.nexecheck.utils.Globals;


/**
 * @author lijie
 */
public class CheckItemSingleView {

	protected Activity activity;
	private View view;

	protected CheckItemTableView itemTableView;

	/**
	 * 检验项目名字
	 */
	protected TextView currentItemNameTV;
	/**
	 * 检验项目状态
	 */
	protected TextView currentItemStatusTV;
	/**
	 * 检验项目的汇总的检验次数
	 */
	protected TextView currentItemSumTimesTV;

	protected TextView showMoreDataTV;

	private ImageView dataFreshIV;

	public CheckItemSingleView(Activity activity,View view) {
		super();
		this.activity = activity;
		this.view = view;
		itemTableView = new CheckItemTableView(activity,view);
	}

	public void initView() {
		currentItemNameTV = (TextView) view
				.findViewById(R.id.currentItemNameTV);
		currentItemStatusTV = (TextView) view
				.findViewById(R.id.currentItemStatusTV);
		currentItemSumTimesTV = (TextView) view
				.findViewById(R.id.currentItemSumTimesTV);
		showMoreDataTV = (TextView) view
				.findViewById(R.id.tv_show_more_data);
		dataFreshIV = (ImageView) view.findViewById(R.id.iv_data_fresh);
		itemTableView.initView();

	}

	/**
	 * 初始化当前检查项目参数列表
	 */
	public void initCheckItemParamList(final CheckItemEntity checkItem) {
		// 删除所有视图，重新加载
		clear();
		currentItemNameTV.setText(checkItem.getItemName());
		if (null != currentItemStatusTV){
			currentItemStatusTV.setText(checkItem.getCheckStatusName());
		}
		currentItemSumTimesTV.setText("检验次数：" + checkItem.getSumTimes() + "次");
		if(checkItem.getSumTimes()>5){
			showMoreDataTV.setText("点击加载更多");
			dataFreshIV.setBackgroundResource(R.drawable.addok);
		}else {
			showMoreDataTV.setText("已全部加载");
			dataFreshIV.setBackgroundResource(R.drawable.addmore);
		}
		showMoreDataTV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if("点击加载更多".equals(showMoreDataTV.getText().toString())){
					itemTableView.clearcurrentCheckItemParamList();
					itemTableView.setCheckItemParamBodyTVAll(checkItem.getExcId(), checkItem.getItemId());
					showMoreDataTV.setText("已全部加载");
					dataFreshIV.setBackgroundResource(R.drawable.addmore);
				}else {
					Toast.makeText(activity,"数据已全部加载",Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 拼装列表 参数头信息
		itemTableView.setCheckItemParamHeaderTV(Globals.getModelFile().getCheckItemVO(checkItem.getItemId()).getParamNameList());
		// 拼装列表 参数值信息
		itemTableView.setCheckItemParamBodyTV(checkItem.getExcId(), checkItem.getItemId());
		// 设置列表 检查结果信息
		itemTableView.setCheckItemParamResultTV(checkItem);

	}

	public void clear() {
		currentItemNameTV.setText("");
		currentItemSumTimesTV.setText("");
		itemTableView.clear();
	}

}

/**
 * 
 */
package com.kstech.nexecheck.view.widget;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.domain.config.vo.CheckItemParamValueVO;
import com.kstech.nexecheck.domain.db.dao.CheckItemDetailDao;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemDetailStatusEnum;
import com.kstech.nexecheck.domain.db.dbenum.CheckItemStatusEnum;
import com.kstech.nexecheck.domain.db.entity.CheckItemDetailEntity;
import com.kstech.nexecheck.domain.db.entity.CheckItemEntity;


/**
 * @author lijie
 */
@SuppressWarnings("ResourceAsColor")
public class CheckItemTableView {
	private View view;
	// 表头
	private LinearLayout checkItemParamHeaderLinerLayout;
	// 表体
	private TableLayout currentCheckItemParamList;
	// 表结果
	private TableLayout currentCheckItemParamResult;

	private Activity activity;

	public CheckItemTableView(Activity activity,View view) {
		this.activity = activity;
		this.view = view;
	}

	public void initView() {
		checkItemParamHeaderLinerLayout = (LinearLayout) view
				.findViewById(R.id.checkItemParamHeaderLinerLayout);
		currentCheckItemParamList = (TableLayout) view
				.findViewById(R.id.currentCheckItemParamList);
		currentCheckItemParamResult = (TableLayout) view
				.findViewById(R.id.currentCheckItemParamResult);
	}

	public void clear() {
		checkItemParamHeaderLinerLayout.removeAllViews();
		currentCheckItemParamList.removeAllViews();
		currentCheckItemParamResult.removeAllViews();
	}
	public void clearcurrentCheckItemParamList() {
		currentCheckItemParamList.removeAllViews();
	}

	/**
	 * 检查项目参数列表 头
	 */
	public void setCheckItemParamHeaderTV(List<CheckItemParamValueVO> headerList){
		LayoutInflater inflater0 = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view0 = inflater0.inflate(R.layout.check_item_param_template, null);
		LinearLayout ly0 = (LinearLayout)view0.findViewById(R.id.checkItemParamLinerLayout);
		TextView tv0 = (TextView)ly0.getChildAt(0);
		tv0.setText("检验时间");
		tv0.setGravity(Gravity.CENTER);
		tv0.setLayoutParams(new LinearLayout.LayoutParams(300,40));
		ly0.removeView(tv0);
		checkItemParamHeaderLinerLayout.addView(tv0);

		for(CheckItemParamValueVO param:headerList) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.check_item_param_template, null);
			LinearLayout ly = (LinearLayout)view.findViewById(R.id.checkItemParamLinerLayout);
			TextView tv = (TextView)ly.getChildAt(0);
			tv.setGravity(Gravity.CENTER);
			tv.setText(param.getParam());
			tv.setPadding(0,0,15,0);
			ly.removeView(tv);
			checkItemParamHeaderLinerLayout.addView(tv);
		}
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.check_item_param_template, null);
		LinearLayout ly = (LinearLayout)view.findViewById(R.id.checkItemParamLinerLayout);
		TextView tv = (TextView)ly.getChildAt(0);
		tv.setGravity(Gravity.CENTER);
		tv.setText("自检结论");
		ly.removeView(tv);
		checkItemParamHeaderLinerLayout.addView(tv);

		LayoutInflater inflater1 = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view1 = inflater1.inflate(R.layout.check_item_param_template, null);
		LinearLayout ly1 = (LinearLayout)view1.findViewById(R.id.checkItemParamLinerLayout);
		TextView tv1 = (TextView)ly1.getChildAt(0);
		tv1.setGravity(Gravity.CENTER);
		tv1.setText("检验员");
		ly1.removeView(tv1);
		checkItemParamHeaderLinerLayout.addView(tv1);
	}

	/**
	 * 检查项目参数列表 体
	 */
	@SuppressLint("ResourceAsColor")
	public void setCheckItemParamBodyTV( String excId,String itemId){
		List<CheckItemDetailEntity> checkParamList = CheckItemDetailDao.getCheckItemDetailListLimit(activity,excId, itemId);
		for(CheckItemDetailEntity body:checkParamList) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.check_item_param_template, null);
			LinearLayout ly = (LinearLayout)view.findViewById(R.id.checkItemParamLinerLayout);
			TableLayout table = (TableLayout)ly.getChildAt(1);
			TableRow row = (TableRow)table.getChildAt(0);
			// 时间
			TextView checkTimeTV = new TextView(activity);
			checkTimeTV.setText(body.getCheckTime());
			checkTimeTV.setTextColor(R.color.checkItemParamTextColor);
			checkTimeTV.setTextSize(18f);
			checkTimeTV.setWidth(300);
			checkTimeTV.setGravity(Gravity.CENTER);
			checkTimeTV.setHeight(50);
			row.addView(checkTimeTV);
			// 不固定参数
			for (CheckItemParamValueVO param:body.getParam()) {
				TextView tempTV = new TextView(activity);
				tempTV.setText(param.getValue());
				tempTV.setTextColor(R.color.checkItemParamTextColor);
				tempTV.setTextSize(20f);
				tempTV.setGravity(Gravity.CENTER);
				tempTV.setWidth(225);
				tempTV.setHeight(50);
				row.addView(tempTV);
			}
			// 自检结论
			TextView imageTV = new TextView(activity);
			imageTV.setText(CheckItemDetailStatusEnum.getName(body.getCheckStatus()));
			imageTV.setTextColor(R.color.checkItemParamTextColor);
			imageTV.setGravity(Gravity.CENTER);
			imageTV.setTextSize(20f);
			imageTV.setWidth(225);
			imageTV.setHeight(50);
			row.addView(imageTV);
			// 检验员
			TextView checkTV = new TextView(activity);
			checkTV.setText(body.getCheckerName());
			checkTV.setTextColor(R.color.checkItemParamTextColor);
			checkTV.setTextSize(20f);
			checkTV.setWidth(225);
			checkTV.setGravity(Gravity.CENTER);
			checkTV.setHeight(50);
			row.addView(checkTV);

			table.removeView(row);
			currentCheckItemParamList.addView(row);
		}
	}
	public void setCheckItemParamBodyTVAll( String excId,String itemId){
		List<CheckItemDetailEntity> checkParamList = CheckItemDetailDao.getCheckItemDetailList(activity,excId, itemId);
		for(CheckItemDetailEntity body:checkParamList) {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.check_item_param_template, null);
			LinearLayout ly = (LinearLayout)view.findViewById(R.id.checkItemParamLinerLayout);
			TableLayout table = (TableLayout)ly.getChildAt(1);
			TableRow row = (TableRow)table.getChildAt(0);
			// 时间
			TextView checkTimeTV = new TextView(activity);
			checkTimeTV.setText(body.getCheckTime());
			checkTimeTV.setTextColor(R.color.checkItemParamTextColor);
			checkTimeTV.setTextSize(20f);
			checkTimeTV.setWidth(300);
			checkTimeTV.setGravity(Gravity.CENTER);
			checkTimeTV.setHeight(50);
			row.addView(checkTimeTV);
			// 不固定参数
			for (CheckItemParamValueVO param:body.getParam()) {
				TextView tempTV = new TextView(activity);
				tempTV.setText(param.getValue());
				tempTV.setTextColor(R.color.checkItemParamTextColor);
				tempTV.setTextSize(20f);
				tempTV.setGravity(Gravity.CENTER);
				tempTV.setWidth(225);
				tempTV.setHeight(50);
				row.addView(tempTV);
			}
			// 自检结论
			TextView imageTV = new TextView(activity);
			imageTV.setText(CheckItemDetailStatusEnum.getName(body.getCheckStatus()));
			imageTV.setTextColor(R.color.checkItemParamTextColor);
			imageTV.setGravity(Gravity.CENTER);
			imageTV.setTextSize(20f);
			imageTV.setWidth(225);
			imageTV.setHeight(50);
			row.addView(imageTV);
			// 检验员
			TextView checkTV = new TextView(activity);
			checkTV.setText(body.getCheckerName());
			checkTV.setTextColor(R.color.checkItemParamTextColor);
			checkTV.setTextSize(20f);
			checkTV.setWidth(225);
			checkTV.setGravity(Gravity.CENTER);
			checkTV.setHeight(50);
			row.addView(checkTV);

			table.removeView(row);
			currentCheckItemParamList.addView(row);
		}
	}
	/**
	 * 检查项目参数列表 结果
	 */
	@SuppressWarnings("ResourceAsColor")
	@SuppressLint("ResourceAsColor")
	public void setCheckItemParamResultTV(CheckItemEntity result){
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.check_item_param_template, null);
		LinearLayout ly = (LinearLayout)view.findViewById(R.id.checkItemParamLinerLayout);
		TableLayout table = (TableLayout)ly.getChildAt(1);
		TableRow row = (TableRow)table.getChildAt(0);
		row.setBackgroundResource(R.color.checkItemParamResultRow);
		// 检验结果
		TextView checkTimeTV = new TextView(activity);
		checkTimeTV.setText("检验结果");
		checkTimeTV.setTextColor(R.color.checkItemParamTextColor);
		checkTimeTV.setTextSize(20f);
		checkTimeTV.setWidth(300);
		checkTimeTV.setGravity(Gravity.CENTER);
		checkTimeTV.setHeight(50);
		row.addView(checkTimeTV);

		//展示检验结果时 进行平均值的计算
		List<CheckItemDetailEntity> checitemList = CheckItemDetailDao.getCheckItemDetailListLimit(activity,result.getExcId(), result.getItemId(), 3);
		LinkedList<Float> several = new LinkedList<>();
		float taketimes = checitemList.size();
		float paramsize = -1;
		if (checitemList.size() > 0){
			for (int i = 0; i < checitemList.size(); i++) {
				List<CheckItemParamValueVO> params = checitemList.get(i).getParam();
				paramsize = params.size();
				for (int i1 = 0; i1 < params.size(); i1++) {
					if ("".equals(params.get(i1).getValue())){
						several.add(0f);
					}else {
						several.add(Float.parseFloat(params.get(i1).getValue()));
					}
				}
			}
		}
		//求每个参数的平均值 哈哈哈哈  快晕了
		if (several.size() > 0){
			float cursor;
			for (int i = 0; i < paramsize; i++) {
				cursor = i;
				float values = 0f;
				for (int j = 0; j < taketimes; j++) {
					Log.e("CheckItemTableView",""+cursor);
					values = values+several.get((int) cursor);
					cursor = cursor+paramsize;
				}
				values = values/taketimes;
				DecimalFormat decimalFormat=new DecimalFormat("0.00");

				String formatValue = decimalFormat.format(values);
				System.out.println(formatValue);
				if(".".equals(formatValue.substring(0,1))) {
					formatValue = "0" + formatValue;
				}
				if("0".equals(formatValue)){
					formatValue = "0";
				}
				TextView tempTV = new TextView(activity);
				tempTV.setText(formatValue);
				tempTV.setTextColor(R.color.checkItemParamTextColor);
				tempTV.setTextSize(20f);
				tempTV.setGravity(Gravity.CENTER);
				tempTV.setWidth(225);
				tempTV.setHeight(50);
				row.addView(tempTV);
			}
		}else {
			// 不固定参数
			for (CheckItemParamValueVO param:result.getParam()) {
				TextView tempTV = new TextView(activity);
				tempTV.setText(param.getValue());
				tempTV.setTextColor(R.color.checkItemParamTextColor);
				tempTV.setTextSize(20f);
				tempTV.setGravity(Gravity.CENTER);
				tempTV.setWidth(225);
				tempTV.setHeight(50);
				row.addView(tempTV);
			}
		}

		// 自检结论
		TextView imageTV = new TextView(activity);
		imageTV.setText(CheckItemStatusEnum.getName(result.getCheckStatus()));
		imageTV.setTextColor(R.color.checkItemParamTextColor);
		imageTV.setTextSize(20f);
		imageTV.setWidth(225);
		imageTV.setGravity(Gravity.CENTER);
		imageTV.setHeight(50);
		row.addView(imageTV);
		// 检验员
		TextView checkTV = new TextView(activity);
		checkTV.setText(result.getCheckerName());
		checkTV.setTextColor(R.color.checkItemParamTextColor);
		checkTV.setTextSize(20f);
		checkTV.setGravity(Gravity.CENTER);
		checkTV.setWidth(225);
		checkTV.setHeight(50);
		row.addView(checkTV);

		table.removeView(row);
		currentCheckItemParamResult.addView(row);
	}
}

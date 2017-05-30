/**
 * 
 */
package com.kstech.nexecheck.view.widget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.db.dao.CheckItemDao;


/**
 * @author lijie
 */
public class CheckItemSummaryView extends CheckItemSingleView implements OnClickListener {
	private HomeActivity activity;
	private View view;

	/**
	 * 检验项目的检验说明
	 */
	private TextView itemCheckDescTV;

	public CheckItemSummaryView(Activity activity,View view) {
		super(activity,view);
		this.activity = (HomeActivity)activity;
		this.view = view;
		new CheckItemTableView(activity,view);
	}

	public void initView() {
		super.initView();
		itemCheckDescTV = (TextView) view.findViewById(R.id.itemCheckDescTV);

		itemCheckDescTV.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.itemCheckDescTV:
				if (!activity.isCheckItemSelected()) {
					return;
				}
				// 初始化检验说明弹出菜单
				LayoutInflater layoutInflaterItem = (LayoutInflater) activity
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View layoutItem = layoutInflaterItem.inflate(
						R.layout.activity_home_item_check_desc, null);
				final PopupWindow checkDescWin = new PopupWindow(activity);
				checkDescWin.setContentView(layoutItem);
				checkDescWin.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
				checkDescWin.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
				checkDescWin.setFocusable(true);
				// checkDescWin.setBackgroundDrawable(getDrawable(R.drawable.rect_gray));
				checkDescWin.setBackgroundDrawable(null);
				checkDescWin.showAtLocation(layoutItem, Gravity.TOP, 0, 100);

				// 检验说明弹出窗口上面的保存按钮
				Button saveCheckDescBtn = (Button) layoutItem
						.findViewById(R.id.saveCheckDescBtn);
				// 检验说明弹出窗口上面的关闭按钮
				Button closeCheckDescBtn = (Button) layoutItem
						.findViewById(R.id.closeCheckDescBtn);
				// 检验说明文本域
				final EditText checkDescET = (EditText) layoutItem
						.findViewById(R.id.checkDescET);
				//跳过此项目单选框
				final CheckBox cb = (CheckBox) layoutItem.findViewById(R.id.cb_ignore);
				String desc = activity.getSelectedCheckItem().getCheckDesc();
				if(desc.contains("ignore$")){
					cb.setChecked(true);
					checkDescET.setText(desc.replace("ignore$",""));
					checkDescET.setEnabled(false);
				}else if(!cb.isChecked()){
					checkDescET.setEnabled(false);
				}
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if(isChecked){
							checkDescET.setText("");
							checkDescET.setEnabled(true);
						}else {
							checkDescET.setText("");
							checkDescET.setEnabled(false);
						}
					}
				});
				saveCheckDescBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(cb.isChecked()){
							CheckItemDao.updateCheckDesc(activity.getSelectedCheckItem().getExcId(),
									activity.getSelectedCheckItem().getItemId(),
									"ignore$"+checkDescET.getText().toString(),
									activity);
							activity.getSelectedCheckItem().setCheckDesc("ignore$"+checkDescET.getText().toString());
						}else {
							CheckItemDao.updateCheckDesc(activity.getSelectedCheckItem().getExcId(),
									activity.getSelectedCheckItem().getItemId(), checkDescET.getText().toString(),
									activity);
							activity.getSelectedCheckItem().setCheckDesc(checkDescET.getText().toString());
						}

						checkDescWin.dismiss();
					}
				});
				closeCheckDescBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						checkDescWin.dismiss();
					}
				});
				break;
			default:
				break;
		}
	}
}

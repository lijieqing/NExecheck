package com.kstech.nexecheck.domain.excel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.Toast;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.upload.DataUploadActivity;
import com.kstech.nexecheck.domain.config.vo.FtpServerVO;
import com.kstech.nexecheck.domain.db.dao.CheckRecordDao;
import com.kstech.nexecheck.domain.db.entity.CheckRecordEntity;
import com.kstech.nexecheck.utils.Globals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件ftp 上传 任务.
 */
public class FtpUploadTask extends
		AsyncTask<Integer, Integer, String> {

	private final List<CheckRecordEntity> listData;
	private Map<String, String> filePathMap;
	private ProgressDialog mProgressDialog = null;
	private FtpUtil ftpUtil = null;


	private Context context;

	/**
	 * Instantiates a new Ftp upload task.
	 *
	 * @param filePathMap the file path map
	 */
	public FtpUploadTask(List<CheckRecordEntity> listData, Context context) {
		this.listData = listData;
		this.context = context;
	}

	/*
	 * 第一个执行的方法 执行时机：在执行实际的后台操作前，被UI 线程调用
	 * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
	 *
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		mProgressDialog = new ProgressDialog(context);
		// 设置进度条风格，风格为圆形，旋转的
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		// 设置ProgressDialog 提示信息
		mProgressDialog.setMessage("正在上传文件，请稍等。。。");

		// 设置ProgressDialog 的进度条是否不明确
		mProgressDialog.setIndeterminate(false);
		// 设置ProgressDialog 是否可以按退回按键取消
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	/**
	 * 主要实现类 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
	 * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
	 * @return
	 *
	 * @see AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(Integer... params) {
		publishProgress(666);
		SystemClock.sleep(1000);

		//创建一个要删除内容的集合，不能直接在数据源data集合中直接进行操作，否则会报异常
		final List<CheckRecordEntity> uploadSelect = new ArrayList<CheckRecordEntity>();
		//把选中的条目要删除的条目放在deleSelect这个集合中
		for (int i = 0; i < listData.size(); i++) {
			if (listData.get(i).getCheckBoxState()) {
				uploadSelect.add(listData.get(i));
			}
		}
		if (uploadSelect.size() == 0) {
			publishProgress(1);
			return null;
		}

		publishProgress(999);
		SystemClock.sleep(1000);

		// 查询需要上传的数据
		List<CheckRecordEntity> uploadData = CheckRecordDao.findUploadData(context,uploadSelect);
		for (CheckRecordEntity record:uploadData) {
			if("0".equals(record.getCheckStatus())){
				publishProgress(2);
				return null;
			}
		}

		// 生成excel文件
//				Map<String, String> filePathMap = ExcelUtil.writeExcel(DataUploadActivity.this,uploadData);
		try {
			publishProgress(3);
			filePathMap = ExcelUtil.UpdateExcelByTemplate(uploadData);
			FtpServerVO ftpServerVO = Globals.getResConfig().getFtpServerVO();
			ftpUtil = new FtpUtil(ftpServerVO.getIp(), ftpServerVO.getPort(), ftpServerVO.getUser(), ftpServerVO.getPassword());
			for (String excId:filePathMap.keySet()){
				ftpUtil.uploadFile(filePathMap.get(excId), excId);
			}
			ftpUtil.closeFTPClient();

			publishProgress(888);
		} catch (Exception e) {
			e.printStackTrace();
			publishProgress(0);
		}
		return null;
	}

	/*
	 * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI
	 * 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
	 * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
	 *
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		switch (values[0]){
			case 0:
			    mProgressDialog.setMessage("上传失败");
				Toast.makeText(context,"上传失败",Toast.LENGTH_SHORT).show();
				break;
			case 1:
                mProgressDialog.setMessage("未检测到数据");
				Toast.makeText(context, R.string.pleaseCheckedNeedUploadData, Toast.LENGTH_SHORT).show();
				break;
			case 2:
				new AlertDialog.Builder(context)
						.setMessage(R.string.recordStateError)
						.setNeutralButton(R.string.str_ok, null).show();
				break;
			case 3:
				mProgressDialog.setMessage("正在上传，请稍候---  	总时间：10 s");
				countDownTimer.start();
				break;
			case 666:
                mProgressDialog.setMessage("正在提取数据，请稍候---");
				break;
			case 888:
				countDownTimer.cancel();
				Toast.makeText(context,"上传成功",Toast.LENGTH_SHORT).show();
				break;
			case 999:
                mProgressDialog.setMessage("正在检测数据完整性，请稍候---");
				break;

		}
	}
	private CountDownTimer countDownTimer = new CountDownTimer(10000,1000) {
		@Override
		public void onTick(long millisUntilFinished) {
			mProgressDialog.setMessage("正在上传，请稍候---  	剩余时间 "+millisUntilFinished/1000+" s");
		}

		@Override
		public void onFinish() {
			mProgressDialog.cancel();
			new AlertDialog.Builder(context)
					.setMessage("上传超时，请确认ftp IP是否一致")
					.setNeutralButton(R.string.str_ok, null).show();
			try {
				ftpUtil.closeFTPClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/*
	 * 执行时机：在doInBackground 执行完成后，将被UI 线程调用 作用：后台的计算结果将通过该方法传递到UI 线程，并且在界面上展示给用户
	 * result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(final String result) {
		super.onPostExecute(result);
		mProgressDialog.cancel();
	}

}
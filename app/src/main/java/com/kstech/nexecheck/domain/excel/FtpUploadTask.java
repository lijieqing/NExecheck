package com.kstech.nexecheck.domain.excel;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.kstech.nexecheck.domain.config.vo.FtpServerVO;
import com.kstech.nexecheck.utils.Globals;

import java.util.Map;

/**
 * 文件ftp 上传 任务.
 */
public class FtpUploadTask extends
		AsyncTask<Integer, Integer, String> {

	private ProgressDialog mProgressDialog = null;

	private Map<String, String> filePathMap;
	private Context context;

	/**
	 * Instantiates a new Ftp upload task.
	 *
	 * @param filePathMap the file path map
	 */
	public FtpUploadTask(Map<String, String> filePathMap, Context context) {
		this.filePathMap = filePathMap;
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
		try {
			FtpServerVO ftpServerVO = Globals.getResConfig().getFtpServerVO();
			FtpUtil ftpUtil = new FtpUtil(ftpServerVO.getIp(), ftpServerVO.getPort(), ftpServerVO.getUser(), ftpServerVO.getPassword());
			for (String excId:filePathMap.keySet()){
				ftpUtil.uploadFile(filePathMap.get(excId), excId);
			}
			ftpUtil.closeFTPClient();
		} catch (Exception e) {
			e.printStackTrace();
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
		// mTextView.setText(values[0]+"%");
		super.onProgressUpdate(values);
	}

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
		Toast.makeText(context,"上传成功，请到ftp共享目录查看",Toast.LENGTH_SHORT).show();
	}

}
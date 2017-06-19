package com.kstech.nexecheck.engine;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.activity.HomeActivity;
import com.kstech.nexecheck.domain.checkline.CheckLineListAdapter;
import com.kstech.nexecheck.domain.checkline.CheckLineManager;
import com.kstech.nexecheck.domain.config.ConfigFileManager;
import com.kstech.nexecheck.domain.config.vo.CheckLineVO;
import com.kstech.nexecheck.utils.Globals;

public class CheckLineLoadTask extends AsyncTask<Integer, Integer, List<CheckLineVO>> {
    // 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

    private ProgressDialog mProgressDialog = null;
    private TextView checkLineET = null;
    private Context context;

    public CheckLineLoadTask(TextView tv,Context context) {
        this.checkLineET = tv;
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
        mProgressDialog.setMessage("正在检测检线信息，请稍等。。。");

        // 设置ProgressDialog 的进度条是否不明确
        mProgressDialog.setIndeterminate(false);
        // 设置ProgressDialog 是否可以按退回按键取消
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    /**
     * 主要实现类 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
     * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
     *
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected List<CheckLineVO> doInBackground(Integer... params) {
        return CheckLineManager.getValidateCheckLines(context);
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
    protected void onPostExecute(final List<CheckLineVO> result) {
        super.onPostExecute(result);
        mProgressDialog.cancel();

        // 弹出检线列表
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View check_line_view = inflater.inflate(R.layout.check_line_view, null);
        ListView checkLineView = (ListView) check_line_view
                .findViewById(R.id.checkLineView);
        CheckLineListAdapter cladapter = new CheckLineListAdapter(result,context);
        checkLineView.setAdapter(cladapter);
        AlertDialog.Builder checkBuilder = new AlertDialog.Builder(context);
        checkBuilder.setView(check_line_view);
        final AlertDialog checkDialog = checkBuilder.create();
        checkDialog.show();

        checkLineView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                CheckLineManager.connectCheckLine(result.get(arg2),context);
                // 显示选择的检线名称
                checkLineET.setText(result.get(arg2).getName());
                if (!Globals.getCurrentCheckLine().getIp().equals(result.get(arg2).getIp())){
                    if (context instanceof HomeActivity){
                        Globals.setCurrentCheckLine(result.get(arg2));
                        ((HomeActivity)context).handler.sendEmptyMessage(3);
                    }
                }
                Globals.setCurrentCheckLine(result.get(arg2));
                ConfigFileManager.getInstance(context).saveCheckLineName(result.get(arg2).getName());
                ConfigFileManager.getInstance(context).saveCheckLineSsid(result.get(arg2).getSsid());
                checkDialog.cancel();
            }
        });

    }

}
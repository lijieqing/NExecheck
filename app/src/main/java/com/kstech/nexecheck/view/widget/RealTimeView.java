package com.kstech.nexecheck.view.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.base.RealtimeChangeListener;
import com.kstech.nexecheck.domain.config.vo.RealTimeParamVO;
import com.kstech.nexecheck.utils.Globals;

import java.text.DecimalFormat;

import J1939.J1939_DataVar_ts;


/**
 * Created by lijie on 2017/5/24.
 */

public class RealTimeView extends RelativeLayout implements RealtimeChangeListener{
    private RealTimeParamVO realTimeParamVO;
    private Activity context;
    private TextView tvName ;
    private TextView tvUnit ;
    private TextView tvValue ;
    private String formatValue;
    public RealTimeView(Activity context,RealTimeParamVO realTimeParamVO) {
        super(context);
        this.context = context;
        this.realTimeParamVO = realTimeParamVO;
        initView(context);
    }

    public RealTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RealTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context){
        View view = View.inflate(context, R.layout.widget_realtime_view,null);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        tvUnit = (TextView) view.findViewById(R.id.tv_unit);
        tvValue = (TextView) view.findViewById(R.id.tv_value);
        if (realTimeParamVO != null){
            tvName.setText(realTimeParamVO.getName());
            tvUnit.setText(realTimeParamVO.getUnit());
            //// TODO: 2017/5/25 撤销测试代码
            tvValue.setText("----");
        }
        this.addView(view);
    }

    public RealTimeParamVO getRealTimeParamVO() {
        return realTimeParamVO;
    }

    @Override
    public void onDataChanged(final float value) {
        J1939_DataVar_ts dataVar = Globals.getModelFile().getDataSetVO().getDSItem(tvName.getText().toString());
        formatValue(dataVar);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvValue.setText(formatValue+"");
            }
        });

    }

    //对接收到的数据进行精度转换
    private void formatValue(J1939_DataVar_ts dataVar){
        // 保留小数点位数
        byte bDataDec = dataVar.bDataDec;
        StringBuffer sb = new StringBuffer();
        if (bDataDec!=0) {
            sb.append(".");
            for (int i=0;i < bDataDec;i++) {
                sb.append("0");
            }
        }
        DecimalFormat decimalFormat=new DecimalFormat(sb.toString());//构造方法的字符格式这里如果小数不足2位,会以0补足.

        if(dataVar.isFloatType()){
            Float fvalue = dataVar.getFloatValue();
            formatValue = decimalFormat.format(fvalue);

            if(".".equals(formatValue.substring(0,1))) {
                formatValue = "0" + formatValue;
            }
            if("0".equals(formatValue)){
                formatValue = "0";
            }
        }else{
            Long lvalue = dataVar.getValue();
            formatValue = decimalFormat.format(lvalue);
            if(lvalue == 0) {
                formatValue = "0" + formatValue;
            }
        }
    }

}

package com.kstech.nexecheck.view.widget;

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


/**
 * Created by lijie on 2017/5/24.
 */

public class RealTimeView extends RelativeLayout implements RealtimeChangeListener{
    private RealTimeParamVO realTimeParamVO;
    private Context context;
    private TextView tvName ;
    private TextView tvUnit ;
    private TextView tvValue ;
    public RealTimeView(Context context,RealTimeParamVO realTimeParamVO) {
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
            tvValue.setText("382.14");
        }
        this.addView(view);
    }

    public RealTimeParamVO getRealTimeParamVO() {
        return realTimeParamVO;
    }

    @Override
    public void onDataChanged(float value) {
        tvValue.setText(value+"");
    }

}
